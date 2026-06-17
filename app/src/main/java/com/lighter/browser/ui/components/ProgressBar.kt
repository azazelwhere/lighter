package com.lighter.browser.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lighter.browser.ui.theme.HoloBlueBright
import com.lighter.browser.ui.theme.HoloDivider
import com.lighter.browser.ui.theme.IncognitoPurpleLight

/**
 * Thin progress bar at the top of the WebView area (Chrome-style).
 */
@Composable
fun ProgressBar(
    progress: Int,
    loading: Boolean,
    incognito: Boolean,
    modifier: Modifier = Modifier
) {
    val animated by animateFloatAsState(
        targetValue = progress / 100f,
        label = "progress"
    )
    val color = if (incognito) IncognitoPurpleLight else HoloBlueBright
    if (loading || progress in 1..99) {
        Box(
            modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(HoloDivider.copy(alpha = 0.3f))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(animated)
                    .fillMaxHeight()
                    .background(color)
            )
        }
    }
}
