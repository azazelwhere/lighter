package com.lighter.browser.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lighter.browser.browser.TabManager
import com.lighter.browser.ui.theme.*

@Composable
fun TabsScreen(
    tabManager: TabManager,
    onOpenTab: (Int) -> Unit,
    onClose: () -> Unit
) {
    val tabs by remember { derivedStateOf { tabManager.tabs } }

    Column(Modifier.fillMaxSize().background(HoloBackground)) {
        // Header
        Surface(color = HoloActionBar) {
            Row(
                Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${tabs.size} tab",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { tabManager.newTab(); onClose() }) {
                    Icon(Icons.Filled.Add, "New tab", tint = HoloBlueBright)
                }
                IconButton(onClick = { tabManager.newIncognitoTab(); onClose() }) {
                    Icon(Icons.Filled.VisibilityOff, "Incognito", tint = HoloBlueBright)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, "Close", tint = Color.White)
                }
            }
        }

        if (tabs.isEmpty()) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Tab, null, Modifier.size(48.dp), tint = HoloTextHint)
                    Spacer(Modifier.height(12.dp))
                    Text("Belum ada tab terbuka", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { tabManager.newTab(); onClose() }) {
                        Text("Buka tab baru")
                    }
                }
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tabs, key = { it.state.id }) { tab ->
                    val index = tabs.indexOf(tab)
                    TabRow(
                        title = tab.state.title.ifBlank { tab.state.url },
                        url = tab.state.url,
                        loading = tab.state.loading,
                        incognito = tab.state.incognito,
                        onClick = { onOpenTab(index) },
                        onClose = { tabManager.closeTab(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TabRow(
    title: String,
    url: String,
    loading: Boolean,
    incognito: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit
) {
    val bg = if (incognito) IncognitoPurple.copy(alpha = 0.7f) else HoloPanel
    Surface(
        color = bg,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (incognito) {
                Icon(Icons.Filled.VisibilityOff, null, tint = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Icon(Icons.Filled.Public, null, tint = HoloBlueBright, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    url,
                    style = MaterialTheme.typography.bodySmall,
                    color = HoloTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (loading) {
                CircularProgressIndicator(
                    Modifier.size(16.dp).padding(end = 8.dp),
                    strokeWidth = 2.dp,
                    color = HoloBlueBright
                )
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, "Close", tint = Color.White)
            }
        }
    }
}
