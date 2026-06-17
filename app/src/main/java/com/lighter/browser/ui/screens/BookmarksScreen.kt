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
import com.lighter.browser.data.BookmarkEntity
import com.lighter.browser.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun BookmarksScreen(
    database: AppDatabase,
    onOpen: (String) -> Unit,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var bookmarks by remember { mutableStateOf<List<BookmarkEntity>>(emptyList()) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val liveData = database.bookmarkDao().observeAll()
        val observer = Observer<List<BookmarkEntity>> { bookmarks = it }
        liveData.observe(lifecycleOwner, observer)
        onDispose { liveData.removeObserver(observer) }
    }

    Column(Modifier.fillMaxSize().background(HoloBackground)) {
        Surface(color = HoloActionBar) {
            Row(
                Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                }
                Text("Bookmark", style = MaterialTheme.typography.titleMedium, color = Color.White, modifier = Modifier.weight(1f))
                if (bookmarks.isNotEmpty()) {
                    IconButton(onClick = {
                        scope.launch(Dispatchers.IO) { database.bookmarkDao().clear() }
                    }) {
                        Icon(Icons.Filled.DeleteSweep, "Clear", tint = HoloBlueBright)
                    }
                }
            }
        }

        if (bookmarks.isEmpty()) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.BookmarkBorder, null, Modifier.size(48.dp), tint = HoloTextHint)
                    Spacer(Modifier.height(12.dp))
                    Text("Belum ada bookmark", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(bookmarks, key = { it.id }) { bm ->
                    Surface(
                        color = HoloPanel,
                        modifier = Modifier.fillMaxWidth().clickable { onOpen(bm.url) }
                    ) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Bookmark, null, tint = HoloBlueBright, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(bm.title, style = MaterialTheme.typography.bodyLarge, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(bm.url, style = MaterialTheme.typography.bodySmall, color = HoloTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            IconButton(onClick = {
                                scope.launch(Dispatchers.IO) { database.bookmarkDao().delete(bm) }
                            }) {
                                Icon(Icons.Filled.Delete, "Delete", tint = HoloRed)
                            }
                        }
                    }
                }
            }
        }
    }
}
