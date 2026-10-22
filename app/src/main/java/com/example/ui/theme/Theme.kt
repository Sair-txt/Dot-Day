package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

@Composable
fun DotDayTheme(
    accentColor: Color = PrimaryNavy,
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    // Animate global accent transitions so applying a custom colour feels immediate but not abrupt.
    val animatedAccent by animateColorAsState(
        targetValue = accentColor,
        animationSpec = tween(durationMillis = 220),
        label = "themeAccentTransition"
    )
    // Choose foreground contrast based on the current accent's luminance.
    val accentForeground = if (animatedAccent.luminance() > 0.46f) Color(0xFF111216) else Color.White
    val colorScheme = darkColorScheme(
        primary = animatedAccent,
        onPrimary = accentForeground,
        primaryContainer = animatedAccent,
        onPrimaryContainer = accentForeground,
        secondary = TextSecondary,
        onSecondary = TextPrimary,
        background = Background,
        onBackground = TextPrimary,
        surface = SurfacePrimary,
        onSurface = TextPrimary,
        surfaceVariant = ElevatedSurface,
        onSurfaceVariant = TextSecondary,
        outline = OutlineMuted
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
