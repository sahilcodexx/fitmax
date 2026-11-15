package com.fixmax.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// FitAir Organic Wellness Palette from Reference Design
val MintBackground = Color(0xFFF7FCF9)
val SurfaceCard = Color(0xFFFFFFFF)

val ForestGreenPrimary = Color(0xFF005A43)
val ForestGreenPressed = Color(0xFF004432)
val ForestGreenLight = Color(0xFF287D64)

// Concentric Ripple Ring Colors (Layered Waves)
val RingGreen = Color(0xFFD2EBD9)
val RingTeal = Color(0xFFCEECEF)
val RingSkyBlue = Color(0xFFD6E6F8)
val RingWarmSand = Color(0xFFF5EEDB)

// Pastel Badges
val BadgePurpleBg = Color(0xFFEFE8FA)
val BadgePurpleText = Color(0xFF5E35B1)
val BadgeGreenBg = Color(0xFFE0F5E9)
val BadgeGreenText = Color(0xFF1B5E20)
val BadgePeachBg = Color(0xFFFDECE3)
val BadgePeachText = Color(0xFFD84315)

// Typography Colors
val TextPrimary = Color(0xFF1A2E26)
val TextSecondary = Color(0xFF5A7268)
val TextMuted = Color(0xFF8FA79D)

@Composable
fun FixMaxWellnessTheme(content: @Composable () -> Unit) {
    val colorScheme = lightColorScheme(
        primary = ForestGreenPrimary,
        onPrimary = Color.White,
        primaryContainer = RingGreen,
        onPrimaryContainer = ForestGreenPrimary,
        background = MintBackground,
        onBackground = TextPrimary,
        surface = SurfaceCard,
        onSurface = TextPrimary,
        surfaceVariant = MintBackground,
        onSurfaceVariant = TextSecondary
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
