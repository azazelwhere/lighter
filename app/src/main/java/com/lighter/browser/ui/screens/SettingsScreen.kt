package com.lighter.browser.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lighter.browser.data.Prefs
import com.lighter.browser.privacy.DohProviders
import com.lighter.browser.privacy.TorManager
import com.lighter.browser.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    prefs: Prefs,
    torManager: TorManager,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val scroll = rememberScrollState()

    // Local state mirrors of prefs
    var jsEnabled by remember { mutableStateOf(true) }
    var cookiesEnabled by remember { mutableStateOf(true) }
    var imagesEnabled by remember { mutableStateOf(true) }
    var domStorage by remember { mutableStateOf(true) }
    var doNotTrack by remember { mutableStateOf(true) }
    var adblock by remember { mutableStateOf(true) }
    var trackerBlock by remember { mutableStateOf(true) }
    var dohEnabled by remember { mutableStateOf(false) }
    var dohProvider by remember { mutableStateOf("https://cloudflare-dns.com/dns-query") }
    var clearOnExit by remember { mutableStateOf(true) }
    var saveHistory by remember { mutableStateOf(true) }
    var proxyEnabled by remember { mutableStateOf(false) }
    var proxyType by remember { mutableStateOf("SOCKS") }
    var proxyHost by remember { mutableStateOf("127.0.0.1") }
    var proxyPort by remember { mutableStateOf(9050) }
    var torEnabled by remember { mutableStateOf(false) }
    var homepage by remember { mutableStateOf("https://duckduckgo.com") }
    var searchEngine by remember { mutableStateOf("https://duckduckgo.com/?q=") }

    // Collect initial values
    LaunchedEffect(Unit) {
        prefs.javascriptEnabled.flow.collectLatest { jsEnabled = it }
    }
    LaunchedEffect(Unit) { prefs.cookiesEnabled.flow.collectLatest { cookiesEnabled = it } }
    LaunchedEffect(Unit) { prefs.imagesEnabled.flow.collectLatest { imagesEnabled = it } }
    LaunchedEffect(Unit) { prefs.domStorageEnabled.flow.collectLatest { domStorage = it } }
    LaunchedEffect(Unit) { prefs.doNotTrack.flow.collectLatest { doNotTrack = it } }
    LaunchedEffect(Unit) { prefs.adBlockEnabled.flow.collectLatest { adblock = it } }
    LaunchedEffect(Unit) { prefs.trackerBlockEnabled.flow.collectLatest { trackerBlock = it } }
    LaunchedEffect(Unit) { prefs.dohEnabled.flow.collectLatest { dohEnabled = it } }
    LaunchedEffect(Unit) { prefs.dohProvider.flow.collectLatest { dohProvider = it } }
    LaunchedEffect(Unit) { prefs.clearOnExit.flow.collectLatest { clearOnExit = it } }
    LaunchedEffect(Unit) { prefs.saveHistory.flow.collectLatest { saveHistory = it } }
    LaunchedEffect(Unit) { prefs.proxyEnabled.flow.collectLatest { proxyEnabled = it } }
    LaunchedEffect(Unit) { prefs.proxyType.flow.collectLatest { proxyType = it } }
    LaunchedEffect(Unit) { prefs.proxyHost.flow.collectLatest { proxyHost = it } }
    LaunchedEffect(Unit) { prefs.proxyPort.flow.collectLatest { proxyPort = it } }
    LaunchedEffect(Unit) { prefs.torEnabled.flow.collectLatest { torEnabled = it } }
    LaunchedEffect(Unit) { prefs.homepage.flow.collectLatest { homepage = it } }
    LaunchedEffect(Unit) { prefs.searchEngine.flow.collectLatest { searchEngine = it } }

    Column(Modifier.fillMaxSize().background(HoloBackground)) {
        Surface(color = HoloActionBar) {
            Row(
                Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                }
                Text("Pengaturan", style = MaterialTheme.typography.titleMedium, color = Color.White, modifier = Modifier.weight(1f))
            }
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(scroll).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionHeader("Umum")
            TextField(
                value = homepage,
                onValueChange = { homepage = it; scope.launch { prefs.homepage.set(it) } },
                label = { Text("Homepage") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = searchEngine,
                onValueChange = { searchEngine = it; scope.launch { prefs.searchEngine.set(it) } },
                label = { Text("Search engine URL (?q=)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(color = HoloDivider)
            SectionHeader("Konten")
            ToggleRow("JavaScript", jsEnabled) { jsEnabled = it; scope.launch { prefs.javascriptEnabled.set(it) } }
            ToggleRow("Cookies", cookiesEnabled) { cookiesEnabled = it; scope.launch { prefs.cookiesEnabled.set(it) } }
            ToggleRow("Gambar", imagesEnabled) { imagesEnabled = it; scope.launch { prefs.imagesEnabled.set(it) } }
            ToggleRow("DOM Storage", domStorage) { domStorage = it; scope.launch { prefs.domStorageEnabled.set(it) } }
            ToggleRow("Do Not Track header", doNotTrack) { doNotTrack = it; scope.launch { prefs.doNotTrack.set(it) } }

            HorizontalDivider(color = HoloDivider)
            SectionHeader("Privasi & Anonimitas")
            ToggleRow("Ad & Tracker Blocker", adblock) { adblock = it; scope.launch { prefs.adBlockEnabled.set(it) } }
            ToggleRow("Tracker Blocker (terpisah)", trackerBlock) { trackerBlock = it; scope.launch { prefs.trackerBlockEnabled.set(it) } }
            ToggleRow("Simpan riwayat", saveHistory) { saveHistory = it; scope.launch { prefs.saveHistory.set(it) } }
            ToggleRow("Bersihkan data saat keluar", clearOnExit) { clearOnExit = it; scope.launch { prefs.clearOnExit.set(it) } }

            HorizontalDivider(color = HoloDivider)
            SectionHeader("DNS over HTTPS")
            ToggleRow("Aktifkan DoH", dohEnabled) { dohEnabled = it; scope.launch { prefs.dohEnabled.set(it) } }
            var dohExpanded by remember { mutableStateOf(false) }
            Text("Penyedia: $dohProvider", style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = { dohExpanded = true }) { Text("Ganti penyedia") }
            DropdownMenu(expanded = dohExpanded, onDismissRequest = { dohExpanded = false }) {
                DohProviders.presets.forEach { (name, url) ->
                    DropdownMenuItem(
                        text = { Text("$name  ($url)") },
                        onClick = {
                            dohProvider = url
                            scope.launch { prefs.dohProvider.set(url) }
                            dohExpanded = false
                        }
                    )
                }
            }

            HorizontalDivider(color = HoloDivider)
            SectionHeader("Proxy")
            ToggleRow("Aktifkan Proxy", proxyEnabled) { proxyEnabled = it; scope.launch { prefs.proxyEnabled.set(it) } }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = proxyType == "HTTP",
                    onClick = { proxyType = "HTTP"; scope.launch { prefs.proxyType.set("HTTP") } },
                    label = { Text("HTTP") }
                )
                FilterChip(
                    selected = proxyType == "SOCKS",
                    onClick = { proxyType = "SOCKS"; scope.launch { prefs.proxyType.set("SOCKS") } },
                    label = { Text("SOCKS5") }
                )
            }
            TextField(
                value = proxyHost,
                onValueChange = { proxyHost = it; scope.launch { prefs.proxyHost.set(it) } },
                label = { Text("Host") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = proxyPort.toString(),
                onValueChange = { proxyPort = it.toIntOrNull() ?: 9050; scope.launch { prefs.proxyPort.set(proxyPort) } },
                label = { Text("Port") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(color = HoloDivider)
            SectionHeader("Tor (via Orbot)")
            val torStatus = torManager.status
            Text("Status: $torStatus", style = MaterialTheme.typography.bodyMedium)
            if (!torManager.isOrbotInstalled()) {
                Text("Orbot belum terpasang.", style = MaterialTheme.typography.bodySmall, color = HoloOrange)
                Button(onClick = { torManager.openInstallPage() }) {
                    Text("Pasang Orbot")
                }
            } else {
                ToggleRow("Routing via Tor", torEnabled) {
                    torEnabled = it
                    scope.launch { prefs.torEnabled.set(it) }
                    if (it) torManager.startOrbot()
                    else torManager.stopOrbot()
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { torManager.startOrbot() }) { Text("Start Orbot") }
                    Button(onClick = { torManager.stopOrbot() }) { Text("Stop") }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        color = HoloBlueBright,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun ToggleRow(label: String, value: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = HoloTextPrimary, modifier = Modifier.weight(1f))
        Switch(checked = value, onCheckedChange = onToggle)
    }
}
