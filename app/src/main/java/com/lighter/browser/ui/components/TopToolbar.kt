package com.lighter.browser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lighter.browser.ui.theme.*

/**
 * Top toolbar above the URL bar: Back / Forward / Tabs / Menu.
 * Mirrors the AOSP Browser top bar layout.
 */
@Composable
fun TopToolbar(
    incognito: Boolean,
    tabCount: Int,
    canGoBack: Boolean,
    canGoForward: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onTabs: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (incognito) IncognitoPurple else HoloActionBar
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(bg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, enabled = canGoBack) {
            Icon(
                Icons.Filled.ArrowBack,
                contentDescription = "Back",
                tint = if (canGoBack) HoloBlueBright else HoloTextHint
            )
        }
        IconButton(onClick = onForward, enabled = canGoForward) {
            Icon(
                Icons.Filled.ArrowForward,
                contentDescription = "Forward",
                tint = if (canGoForward) HoloBlueBright else HoloTextHint
            )
        }
        Spacer(Modifier.weight(1f))
        // Tab count button
        Box(contentAlignment = Alignment.Center) {
            IconButton(onClick = onTabs) {
                Icon(
                    Icons.Filled.Tab,
                    contentDescription = "Tabs",
                    tint = Color.White
                )
            }
            // Badge with count
            Text(
                text = if (tabCount > 99) "99+" else tabCount.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                modifier = Modifier.padding(top = 14.dp)
            )
        }
        IconButton(onClick = onMenu) {
            Icon(
                Icons.Filled.MoreVert,
                contentDescription = "Menu",
                tint = Color.White
            )
        }
    }
}
