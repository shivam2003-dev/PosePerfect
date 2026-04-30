package com.example.poseperfect.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Colour palette ────────────────────────────────────────────────────────────

val NeonGreen    = Color(0xFF00FF87)
val NeonBlue     = Color(0xFF00CFFF)
val DeepBlack    = Color(0xFF0A0A0A)
val DarkSurface  = Color(0xFF1A1A2E)
val CardSurface  = Color(0xFF16213E)
val WarningAmber = Color(0xFFFFB300)
val ErrorRed     = Color(0xFFFF5252)

private val DarkColors = darkColorScheme(
    primary      = NeonGreen,
    secondary    = NeonBlue,
    background   = DeepBlack,
    surface      = DarkSurface,
    onPrimary    = DeepBlack,
    onSecondary  = DeepBlack,
    onBackground = Color.White,
    onSurface    = Color.White,
    error        = ErrorRed
)

@Composable
fun PosePerfectTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content
    )
}

