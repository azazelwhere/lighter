package com.lighter.browser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lighter.browser.R
import com.lighter.browser.ui.theme.*

/**
 * Bottom action bar (AOSP Browser style): Home / Bookmarks / Reader / Screenshot / Settings.
 */
@Composable
fun BottomBar(
    incognito: Boolean,
    onHome: () -> Unit,
    onBookmarks: () -> Unit,
    onReader: () -> Unit,
    onScreenshot: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (incognito) IncognitoPurple else HoloActionBar
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = bg,
        tonalElevation = 0.dp
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconAction(Icons.Filled.Home, "Home", onClick = onHome)
            IconAction(Icons.Filled.Bookmark, "Bookmark", onClick = onBookmarks)
            IconAction(Icons.Filled.MenuBook, "Reader", onClick = onReader)
            IconAction(Icons.Filled.PhotoCamera, "Screenshot", onClick = onScreenshot)
            IconAction(Icons.Filled.Settings, "Settings", onClick = onSettings)
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(HoloBlue.copy(alpha = 0.5f))
        )
    }
}

@Composable
private fun RowScope.IconAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.weight(1f).fillMaxHeight(),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, desc, tint = HoloBlueBright, modifier = Modifier.size(22.dp))
        }
    }
}
