package com.lighter.browser.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "lighter_prefs")

class Prefs(private val context: Context) {

    // ===== General =====
    val homepage = stringPref("homepage", "https://duckduckgo.com")
    val searchEngine = stringPref("search_engine", "https://duckduckgo.com/?q=")
    val homepageEnabled = booleanPref("homepage_enabled", true)

    // ===== Content settings =====
    val javascriptEnabled = booleanPref("js_enabled", true)
    val cookiesEnabled = booleanPref("cookies_enabled", true)
    val imagesEnabled = booleanPref("images_enabled", true)
    val domStorageEnabled = booleanPref("dom_storage_enabled", true)
    val doNotTrack = booleanPref("dnt", true)

    // ===== Privacy =====
    val adBlockEnabled = booleanPref("adblock", true)
    val trackerBlockEnabled = booleanPref("tracker_block", true)
    val dohEnabled = booleanPref("doh", false)
    val dohProvider = stringPref("doh_provider", "https://cloudflare-dns.com/dns-query")
    val clearOnExit = booleanPref("clear_on_exit", true)
    val saveHistory = booleanPref("save_history", true)

    // ===== Proxy =====
    val proxyEnabled = booleanPref("proxy_enabled", false)
    val proxyType = stringPref("proxy_type", "SOCKS") // HTTP / SOCKS
    val proxyHost = stringPref("proxy_host", "127.0.0.1")
    val proxyPort = intPref("proxy_port", 9050)
    val torEnabled = booleanPref("tor_enabled", false)

    // ===== Spoofing =====
    val spoofActiveProfileId = stringPref("spoof_profile_id", "default_android")
    val spoofRandomize = booleanPref("spoof_randomize", false)
    val spoofJsEnabled = booleanPref("spoof_js_enabled", true)

    // ===== Helpers =====
    private fun booleanPref(key: String, default: Boolean) =
        object : BooleanPref {
            override val flow: Flow<Boolean> =
                context.dataStore.data.map { it[booleanPreferencesKey(key)] ?: default }
            override suspend fun set(value: Boolean) {
                context.dataStore.edit { it[booleanPreferencesKey(key)] = value }
            }
            override val keyName = key
        }

    private fun stringPref(key: String, default: String) =
        object : StringPref {
            override val flow: Flow<String> =
                context.dataStore.data.map { it[stringPreferencesKey(key)] ?: default }
            override suspend fun set(value: String) {
                context.dataStore.edit { it[stringPreferencesKey(key)] = value }
            }
            override val keyName = key
        }

    private fun intPref(key: String, default: Int) =
        object : IntPref {
            override val flow: Flow<Int> =
                context.dataStore.data.map { it[intPreferencesKey(key)] ?: default }
            override suspend fun set(value: Int) {
                context.dataStore.edit { it[intPreferencesKey(key)] = value }
            }
            override val keyName = key
        }

    interface BooleanPref {
        val flow: Flow<Boolean>
        suspend fun set(value: Boolean)
        val keyName: String
    }
    interface StringPref {
        val flow: Flow<String>
        suspend fun set(value: String)
        val keyName: String
    }
    interface IntPref {
        val flow: Flow<Int>
        suspend fun set(value: Int)
        val keyName: String
    }
}
