package com.lighter.browser.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * AOSP Holo dark theme - always dark, ignores system setting
 * (we are an anonymous browser; dark is the default for privacy).
 */
private val HoloColorScheme = darkColorScheme(
    primary = HoloBlue,
    onPrimary = Color.White,
    primaryContainer = HoloActionBar,
    onPrimaryContainer = HoloBlueBright,
    secondary = HoloBlueLight,
    onSecondary = Color.Black,
    background = HoloBackground,
    onBackground = HoloTextPrimary,
    surface = HoloPanel,
    onSurface = HoloTextPrimary,
    surfaceVariant = HoloBackgroundLight,
    onSurfaceVariant = HoloTextSecondary,
    outline = HoloDivider,
    error = HoloRed,
    tertiary = HoloPurple,
    onTertiary = Color.White
)

val LocalIncognito = staticCompositionLocalOf { false }

@Composable
fun LighterTheme(
    incognito: Boolean = false,
    content: @Composable () -> Unit
) {
    val scheme = if (incognito) HoloColorScheme.copy(
        primary = IncognitoPurpleLight,
        primaryContainer = IncognitoPurple,
        secondary = IncognitoPurpleLight
    ) else HoloColorScheme

    CompositionLocalProvider(LocalIncognito provides incognito) {
        MaterialTheme(
            colorScheme = scheme,
            typography = HoloTypography,
            content = content
        )
    }
}
