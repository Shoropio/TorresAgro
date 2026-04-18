package com.torresagro.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1B5E20),      // Deep Forest Green
    onPrimary = Color.White,
    secondary = Color(0xFF795548),    // Earthy brown
    tertiary = Color(0xFF00796B),     // Teal for accents
    background = Color(0xFFFBFBF2),   // Creamy off-white
    surface = Color(0xFFFFFFFF),
    outline = Color(0xFFCFD8DC)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF81C784),      // Soft Green
    secondary = Color(0xFFA1887F),    // Soft Earthy brown
    tertiary = Color(0xFF4DB6AC),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E)
)

@Composable
fun TorresAgroTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
