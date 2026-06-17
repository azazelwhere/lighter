package com.lighter.browser.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lighter.browser.spoofing.ProfileManager
import com.lighter.browser.spoofing.ProfilePresets
import com.lighter.browser.spoofing.SpoofProfile
import com.lighter.browser.ui.theme.*
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun SpoofingScreen(
    profileManager: ProfileManager,
    onApply: () -> Unit,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val active by profileManager.activeProfile.collectAsState()
    val custom by profileManager.customProfiles.collectAsState()
    var editing by remember { mutableStateOf<SpoofProfile?>(null) }
    var expandedPreset by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(HoloBackground)) {
        // Header
        Surface(color = HoloActionBar) {
            Row(
                Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                }
                Text("Spoofing Identitas", style = MaterialTheme.typography.titleMedium, color = Color.White, modifier = Modifier.weight(1f))
                IconButton(onClick = { editing = blankNewProfile() }) {
                    Icon(Icons.Filled.Add, "New", tint = HoloBlueBright)
                }
            }
        }

        // Active profile banner
        Surface(
            color = IncognitoPurple.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                Text("Profil aktif", style = MaterialTheme.typography.bodySmall, color = HoloTextSecondary)
                Text(active.name, style = MaterialTheme.typography.titleLarge, color = Color.White)
                Spacer(Modifier.height(4.dp))
                Text("UA: ${active.userAgent}", style = MaterialTheme.typography.bodySmall, color = HoloTextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("Platform: ${active.platform}", style = MaterialTheme.typography.bodySmall, color = HoloTextSecondary)
                Text("TZ: ${active.timezone}", style = MaterialTheme.typography.bodySmall, color = HoloTextSecondary)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onApply) {
                        Icon(Icons.Filled.Check, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Terapkan ke semua tab")
                    }
                }
            }
        }

        if (editing != null) {
            ProfileEditor(
                profile = editing!!,
                onSave = {
                    scope.launch {
                        profileManager.saveCustom(it)
                        profileManager.setActive(it)
                        editing = null
                    }
                },
                onCancel = { editing = null }
            )
        } else {
            Column(Modifier.padding(horizontal = 8.dp)) {
                Text("Preset profil", style = MaterialTheme.typography.titleMedium, color = HoloBlueBright, modifier = Modifier.padding(top = 8.dp))
            }
            LazyColumn(
                Modifier.fillMaxSize().weight(1f).padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(ProfilePresets.all, key = { it.id }) { p ->
                    ProfileCard(
                        profile = p,
                        isActive = p.id == active.id,
                        isCustom = false,
                        onClick = {
                            profileManager.setActive(p)
                            scope.launch { /* re-applied on next page load */ }
                        },
                        onEdit = null,
                        onDelete = null
                    )
                }
                if (custom.isNotEmpty()) {
                    item {
                        Text("Profil kustom", style = MaterialTheme.typography.titleMedium, color = HoloBlueBright, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                    }
                    items(custom, key = { it.id }) { p ->
                        ProfileCard(
                            profile = p,
                            isActive = p.id == active.id,
                            isCustom = true,
                            onClick = { profileManager.setActive(p) },
                            onEdit = { editing = p },
                            onDelete = { profileManager.deleteCustom(p.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileCard(
    profile: SpoofProfile,
    isActive: Boolean,
    isCustom: Boolean,
    onClick: () -> Unit,
    onEdit: (() -> Unit)?,
    onDelete: (() -> Unit)?
) {
    val border = if (isActive) HoloBlueBright else HoloDivider
    Surface(
        color = if (isActive) HoloPanel.copy(alpha = 1f) else HoloBackgroundLight,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, border, RoundedCornerShape(4.dp))
            .clickable { onClick() }
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isActive) {
                        Icon(Icons.Filled.CheckCircle, null, tint = HoloBlueBright, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(profile.name, style = MaterialTheme.typography.bodyLarge, color = Color.White)
                }
                Text(profile.userAgent, style = MaterialTheme.typography.bodySmall, color = HoloTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${profile.platform}  •  ${profile.timezone}  •  ${profile.screenWidth}x${profile.screenHeight}", style = MaterialTheme.typography.bodySmall, color = HoloTextHint)
            }
            if (onEdit != null) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, "Edit", tint = HoloBlueBright, modifier = Modifier.size(18.dp))
                }
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, "Delete", tint = HoloRed, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileEditor(
    profile: SpoofProfile,
    onSave: (SpoofProfile) -> Unit,
    onCancel: () -> Unit
) {
    val scroll = rememberScrollState()
    var p by remember { mutableStateOf(profile) }

    Column(
        Modifier.fillMaxSize().verticalScroll(scroll).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Edit profil", style = MaterialTheme.typography.titleMedium, color = HoloBlueBright)

        TextField(value = p.name, onValueChange = { p = p.copy(name = it) }, label = { Text("Nama profil") }, modifier = Modifier.fillMaxWidth())
        TextField(value = p.userAgent, onValueChange = { p = p.copy(userAgent = it) }, label = { Text("User-Agent") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
        TextField(value = p.platform, onValueChange = { p = p.copy(platform = it) }, label = { Text("navigator.platform") }, modifier = Modifier.fillMaxWidth())
        TextField(value = p.vendor, onValueChange = { p = p.copy(vendor = it) }, label = { Text("navigator.vendor") }, modifier = Modifier.fillMaxWidth())
        TextField(value = p.language, onValueChange = { p = p.copy(language = it) }, label = { Text("navigator.language") }, modifier = Modifier.fillMaxWidth())
        TextField(value = p.languages.joinToString(","), onValueChange = { p = p.copy(languages = it.split(",").map(String::trim).filter(String::isNotEmpty)) }, label = { Text("navigator.languages (comma separated)") }, modifier = Modifier.fillMaxWidth())
        TextField(value = p.timezone, onValueChange = { p = p.copy(timezone = it) }, label = { Text("Timezone (IANA)") }, modifier = Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextField(value = p.screenWidth.toString(), onValueChange = { p = p.copy(screenWidth = it.toIntOrNull() ?: 1920) }, label = { Text("Screen W") }, modifier = Modifier.weight(1f))
            TextField(value = p.screenHeight.toString(), onValueChange = { p = p.copy(screenHeight = it.toIntOrNull() ?: 1080) }, label = { Text("Screen H") }, modifier = Modifier.weight(1f))
            TextField(value = p.devicePixelRatio.toString(), onValueChange = { p = p.copy(devicePixelRatio = it.toFloatOrNull() ?: 1f) }, label = { Text("DPR") }, modifier = Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextField(value = p.colorDepth.toString(), onValueChange = { p = p.copy(colorDepth = it.toIntOrNull() ?: 24) }, label = { Text("Color depth") }, modifier = Modifier.weight(1f))
            TextField(value = p.hardwareConcurrency.toString(), onValueChange = { p = p.copy(hardwareConcurrency = it.toIntOrNull() ?: 8) }, label = { Text("CPU cores") }, modifier = Modifier.weight(1f))
            TextField(value = p.deviceMemory.toString(), onValueChange = { p = p.copy(deviceMemory = it.toIntOrNull() ?: 8) }, label = { Text("Memory (GB)") }, modifier = Modifier.weight(1f))
        }
        TextField(value = p.webglVendor, onValueChange = { p = p.copy(webglVendor = it) }, label = { Text("WebGL vendor") }, modifier = Modifier.fillMaxWidth())
        TextField(value = p.webglRenderer, onValueChange = { p = p.copy(webglRenderer = it) }, label = { Text("WebGL renderer") }, modifier = Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextField(value = p.batteryLevel.toString(), onValueChange = { p = p.copy(batteryLevel = it.toFloatOrNull() ?: 0.5f) }, label = { Text("Battery lvl") }, modifier = Modifier.weight(1f))
            FilterChip(
                selected = p.batteryCharging,
                onClick = { p = p.copy(batteryCharging = !p.batteryCharging) },
                label = { Text("Charging") }
            )
            FilterChip(
                selected = p.blockBattery,
                onClick = { p = p.copy(blockBattery = !p.blockBattery) },
                label = { Text("Block Battery API") }
            )
        }
        FilterChip(
            selected = p.blockSensors,
            onClick = { p = p.copy(blockSensors = !p.blockSensors) },
            label = { Text("Block sensors (accel/gyro/light)") }
        )
        TextField(value = p.fontsList.joinToString("\n"), onValueChange = { p = p.copy(fontsList = it.split("\n").map(String::trim).filter(String::isNotEmpty)) }, label = { Text("Fonts list (one per line)") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
        TextField(value = p.pluginsList.joinToString("\n"), onValueChange = { p = p.copy(pluginsList = it.split("\n").map(String::trim).filter(String::isNotEmpty)) }, label = { Text("Plugins (one per line)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
        TextField(value = p.extraJs, onValueChange = { p = p.copy(extraJs = it) }, label = { Text("Extra JS (optional, runs before page)") }, modifier = Modifier.fillMaxWidth(), minLines = 3)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onSave(p) }) {
                Icon(Icons.Filled.Save, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Simpan & aktifkan")
            }
            TextButton(onClick = onCancel) { Text("Batal") }
        }
    }
}

private fun blankNewProfile(): SpoofProfile = SpoofProfile(
    id = "custom_" + UUID.randomUUID().toString().take(8),
    name = "Profil baru",
    userAgent = "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36",
    platform = "Linux armv8l",
    vendor = "Google Inc.",
    language = "en-US",
    languages = listOf("en-US", "en"),
    timezone = "America/Los_Angeles",
    timezoneOffsetMinutes = 480,
    screenWidth = 1080,
    screenHeight = 2400,
    devicePixelRatio = 2.625f,
    colorDepth = 24,
    hardwareConcurrency = 8,
    deviceMemory = 8,
    webglVendor = "Qualcomm",
    webglRenderer = "Adreno (TM) 730",
    canvasNoise = 0.0001f,
    batteryLevel = 0.5f,
    batteryCharging = false,
    blockBattery = false,
    blockSensors = false,
    fontsList = listOf("Roboto", "Noto Sans"),
    pluginsList = listOf("PDF Viewer")
)
