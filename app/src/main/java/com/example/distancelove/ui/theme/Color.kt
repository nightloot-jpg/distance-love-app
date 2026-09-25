package com.example.distancelove.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val DarkBackground = Color(0xFF140D11)
val DarkSurface = Color(0xFF201318)
val DarkSurfaceElevated = Color(0xFF2C1A22)
val DarkCardBorder = Color(0x33E58B98)

val RosePrimary = Color(0xFFE58B98)
val RoseSecondary = Color(0xFFD6687D)
val RoseAccent = Color(0xFFFF3B69)
val RoseGold = Color(0xFFF3C2A2)

val TextPrimary = Color(0xFFFDF0F3)
val TextMuted = Color(0xFFA68D95)
val TextSubtle = Color(0xFF7A646C)

val SuccessGreen = Color(0xFF4ADE80)
val DestructiveRed = Color(0xFFF87171)

val RoseGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFF5A5B5),
        Color(0xFFD9536F),
        Color(0xFF9E2A4B)
    )
)

val CardGlowGradient = Brush.radialGradient(
    colors = listOf(
        Color(0x33FF3B69),
        Color(0x00000000)
    )
)
