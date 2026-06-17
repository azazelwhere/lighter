package com.lighter.browser.spoofing

import android.content.Context
import android.webkit.WebSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Central manager for spoofing profiles.
 * - Persists the active profile + custom profiles to /spoofing_profiles/
 * - Provides the resolved profile for the current request (handles "random" rotation)
 * - Exposes flows for the UI to observe
 */
class ProfileManager(private val context: Context) {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private val presetsDir: File by lazy {
        File(context.filesDir, "spoofing_profiles").apply { mkdirs() }
    }

    private val _activeProfile = MutableStateFlow(ProfilePresets.byId(SpoofProfile.DEFAULT_ANDROID))
    val activeProfile: StateFlow<SpoofProfile> = _activeProfile

    private val _customProfiles = MutableStateFlow<List<SpoofProfile>>(emptyList())
    val customProfiles: StateFlow<List<SpoofProfile>> = _customProfiles

    init {
        loadCustomProfiles()
    }

    fun setActive(profile: SpoofProfile) {
        _activeProfile.value = profile
        saveActiveId(profile.id)
    }

    fun setActiveById(id: String) {
        val resolved = _customProfiles.value.firstOrNull { it.id == id }
            ?: ProfilePresets.byId(id)
        setActive(resolved)
    }

    /**
     * Returns a concrete profile for a single page load.
     * If the active profile is "random", generate a fresh randomized instance per call.
     */
    fun resolveForRequest(): SpoofProfile {
        val active = _activeProfile.value
        return if (active.id == "random") ProfilePresets.randomized()
        else active
    }

    fun saveCustom(profile: SpoofProfile) {
        val file = File(presetsDir, "${profile.id}.json")
        file.writeText(json.encodeToString(SpoofProfile.serializer(), profile))
        loadCustomProfiles()
    }

    fun deleteCustom(id: String) {
        File(presetsDir, "$id.json").delete()
        loadCustomProfiles()
    }

    fun exportProfile(profile: SpoofProfile): String =
        json.encodeToString(SpoofProfile.serializer(), profile)

    fun importProfile(jsonString: String): SpoofProfile? = try {
        val p = json.decodeFromString(SpoofProfile.serializer(), jsonString)
        saveCustom(p)
        p
    } catch (t: Throwable) { null }

    private fun loadCustomProfiles() {
        _customProfiles.value = presetsDir.listFiles { _, name -> name.endsWith(".json") }
            ?.mapNotNull { f ->
                try { json.decodeFromString(SpoofProfile.serializer(), f.readText()) }
                catch (t: Throwable) { null }
            }
            ?.sortedBy { it.name }
            ?: emptyList()
    }

    private fun saveActiveId(id: String) {
        context.getSharedPreferences("spoofing_state", Context.MODE_PRIVATE)
            .edit().putString("active_id", id).apply()
    }

    fun loadActiveId(): String =
        context.getSharedPreferences("spoofing_state", Context.MODE_PRIVATE)
            .getString("active_id", SpoofProfile.DEFAULT_ANDROID) ?: SpoofProfile.DEFAULT_ANDROID

    /** Apply UA-related settings to a WebSettings instance. */
    fun applyToWebSettings(settings: WebSettings, profile: SpoofProfile) {
        settings.userAgentString = profile.userAgent
        // Platform hint via UA is enough; WebView handles the rest
    }
}
