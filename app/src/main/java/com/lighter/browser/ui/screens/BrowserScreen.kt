package com.lighter.browser.ui.screens

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.lighter.browser.browser.TabManager
import com.lighter.browser.ui.components.BottomBar
import com.lighter.browser.ui.components.ProgressBar
import com.lighter.browser.ui.components.TopToolbar
import com.lighter.browser.ui.components.UrlBar
import com.lighter.browser.ui.theme.*

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    tabManager: TabManager,
    onOpenTabs: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSpoofing: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenFind: () -> Unit,
    onScreenshot: () -> Unit,
    onReader: () -> Unit,
    onShare: () -> Unit,
    onExit: () -> Unit
) {
    val activeState by remember { derivedStateOf { tabManager.activeState } }
    val state = activeState
    val tabs by remember { derivedStateOf { tabManager.tabs.size } }
    val incognito = state?.incognito == true

    var menuOpen by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(if (incognito) IncognitoPurple else HoloBackground)) {
        TopToolbar(
            incognito = incognito,
            tabCount = tabs,
            canGoBack = tabManager.canGoBack(),
            canGoForward = tabManager.canGoForward(),
            onBack = tabManager::goBack,
            onForward = tabManager::goForward,
            onTabs = onOpenTabs,
            onMenu = { menuOpen = true }
        )

        if (state == null) {
            // No tabs yet - show empty state with a "New tab" CTA
            Box(
                Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (incognito) "Mode penyamaran" else "Lighter Browser",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (incognito) "Tidak ada riwayat tersimpan"
                        else "Ketik URL atau kata kunci di atas",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { tabManager.newTab() }) {
                        Icon(Icons.Filled.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Tab baru")
                    }
                }
            }
        } else {
            UrlBar(
                url = state.url,
                loading = state.loading,
                sslError = state.sslError,
                incognito = incognito,
                onUrlSubmit = { tabManager.loadUrl(it) },
                onReload = tabManager::reload,
                onStop = tabManager::stopLoading
            )

            ProgressBar(
                progress = state.progress,
                loading = state.loading,
                incognito = incognito
            )

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.Black)
            ) {
                val tab = tabManager.tabs.getOrNull(tabManager.activeIndex)
                if (tab != null) {
                    AndroidView(
                        factory = { ctx ->
                            tab.webView.apply {
                                // Re-attach if needed
                                (parent as? ViewGroup)?.removeView(this)
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        BottomBar(
            incognito = incognito,
            onHome = {
                if (state != null) tabManager.loadUrl("https://duckduckgo.com")
                else tabManager.newTab("https://duckduckgo.com")
            },
            onBookmarks = onOpenBookmarks,
            onReader = onReader,
            onScreenshot = onScreenshot,
            onSettings = onOpenSettings
        )
    }

    // Overflow menu
    DropdownMenu(
        expanded = menuOpen,
        onDismissRequest = { menuOpen = false }
    ) {
        DropdownMenuItem(text = { Text("Tab baru") }, onClick = {
            menuOpen = false
            tabManager.newTab()
        }, leadingIcon = { Icon(Icons.Filled.Add, null) })
        DropdownMenuItem(text = { Text("Tab penyamaran") }, onClick = {
            menuOpen = false
            tabManager.newIncognitoTab()
        }, leadingIcon = { Icon(Icons.Filled.VisibilityOff, null) })
        HorizontalDivider()
        DropdownMenuItem(text = { Text("Riwayat") }, onClick = {
            menuOpen = false
            onOpenHistory()
        }, leadingIcon = { Icon(Icons.Filled.History, null) })
        DropdownMenuItem(text = { Text("Bookmark") }, onClick = {
            menuOpen = false
            onOpenBookmarks()
        }, leadingIcon = { Icon(Icons.Filled.Bookmark, null) })
        DropdownMenuItem(text = { Text("Download") }, onClick = {
            menuOpen = false
            onOpenDownloads()
        }, leadingIcon = { Icon(Icons.Filled.Download, null) })
        DropdownMenuItem(text = { Text("Cari di halaman") }, onClick = {
            menuOpen = false
            onOpenFind()
        }, leadingIcon = { Icon(Icons.Filled.Search, null) })
        HorizontalDivider()
        DropdownMenuItem(text = { Text("Spoofing identitas") }, onClick = {
            menuOpen = false
            onOpenSpoofing()
        }, leadingIcon = { Icon(Icons.Filled.Fingerprint, null) })
        DropdownMenuItem(text = { Text("Pengaturan") }, onClick = {
            menuOpen = false
            onOpenSettings()
        }, leadingIcon = { Icon(Icons.Filled.Settings, null) })
        HorizontalDivider()
        DropdownMenuItem(text = { Text("Bagikan URL") }, onClick = {
            menuOpen = false
            onShare()
        }, leadingIcon = { Icon(Icons.Filled.Share, null) })
        DropdownMenuItem(text = { Text("Keluar") }, onClick = {
            menuOpen = false
            onExit()
        }, leadingIcon = { Icon(Icons.Filled.ExitToApp, null) })
    }
}
