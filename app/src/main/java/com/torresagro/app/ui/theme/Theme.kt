package com.torresagro.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = TertiaryBlue,
    tertiary = AccentGold,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    primaryContainer = SecondaryBlue,
    secondaryContainer = Color(0xFF304437),
    tertiaryContainer = Color(0xFF5E5125),
    outline = Color(0xFF3E5648),
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFD3E0D4),
    onPrimaryContainer = Color(0xFFF3FFF5),
    onSecondaryContainer = Color(0xFFE7F3E7),
    onTertiaryContainer = Color(0xFFFFF3CC)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = SecondaryBlue,
    tertiary = AccentGold,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    primaryContainer = Color(0xFFDCEFE3),
    secondaryContainer = Color(0xFFDDE9E2),
    tertiaryContainer = Color(0xFFFFE8A6),
    outline = BorderLight,
    onPrimary = Color.White,
    onBackground = Color(0xFF1A211B),
    onSurface = Color(0xFF19221B),
    onSurfaceVariant = Color(0xFF5B6B61),
    onPrimaryContainer = Color(0xFF163A29),
    onSecondaryContainer = Color(0xFF183428),
    onTertiaryContainer = Color(0xFF4B3B00)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6),
    small = RoundedCornerShape(8),
    medium = RoundedCornerShape(8),
    large = RoundedCornerShape(8),
    extraLarge = RoundedCornerShape(8)
)

@Composable
fun TorresAgroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
