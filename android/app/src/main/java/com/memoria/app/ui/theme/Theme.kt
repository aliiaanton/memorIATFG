package com.memoria.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SpringColorScheme = lightColorScheme(
    primary = Color(0xFF6F9F8A),
    onPrimary = Color.White,
    secondary = Color(0xFF8BB7D9),
    onSecondary = Color(0xFF102A3A),
    tertiary = Color(0xFFF29F8D),
    onTertiary = Color(0xFF3A1710),
    background = Color(0xFFF8FBF9),
    onBackground = Color(0xFF1C2521),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C2521),
    error = Color(0xFFB84A4A),
    onError = Color.White
)

@Composable
fun MemoriaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SpringColorScheme,
        content = content
    )
}

