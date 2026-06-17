package com.lighter.browser.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.lighter.browser.data.AppDatabase
import com.lighter.browser.data.HistoryEntity
import com.lighter.browser.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    database: AppDatabase,
    onOpen: (String) -> Unit,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var items by remember { mutableStateOf<List<HistoryEntity>>(emptyList()) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val liveData = database.historyDao().observeRecent()
        val observer = Observer<List<HistoryEntity>> { items = it }
        liveData.observe(lifecycleOwner, observer)
        onDispose { liveData.removeObserver(observer) }
    }

    val df = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

    Column(Modifier.fillMaxSize().background(HoloBackground)) {
        Surface(color = HoloActionBar) {
            Row(
                Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                }
                Text("Riwayat", style = MaterialTheme.typography.titleMedium, color = Color.White, modifier = Modifier.weight(1f))
                if (items.isNotEmpty()) {
                    IconButton(onClick = {
                        scope.launch(Dispatchers.IO) { database.historyDao().clear() }
                    }) {
                        Icon(Icons.Filled.DeleteSweep, "Clear", tint = HoloBlueBright)
                    }
                }
            }
        }

        if (items.isEmpty()) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.History, null, Modifier.size(48.dp), tint = HoloTextHint)
                    Spacer(Modifier.height(12.dp))
                    Text("Riwayat kosong", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    Surface(
                        color = HoloPanel,
                        modifier = Modifier.fillMaxWidth().clickable { onOpen(item.url) }
                    ) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.History, null, tint = HoloBlueBright, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.title, style = MaterialTheme.typography.bodyLarge, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(item.url, style = MaterialTheme.typography.bodySmall, color = HoloTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Text(
                                df.format(Date(item.visitedAt)),
                                style = MaterialTheme.typography.bodySmall,
                                color = HoloTextHint
                            )
                        }
                    }
                }
            }
        }
    }
}
