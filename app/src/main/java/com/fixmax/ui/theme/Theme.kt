package com.fixmax.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PrimaryGreen = Color(0xFF1B8754)
val SecondaryTeal = Color(0xFF0F6674)
val BackgroundDark = Color(0xFF121212)

@Composable
fun FixMaxTheme(content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        primary = PrimaryGreen,
        secondary = SecondaryTeal,
        background = BackgroundDark
    )
    MaterialTheme(colorScheme = colorScheme, content = content)
}
