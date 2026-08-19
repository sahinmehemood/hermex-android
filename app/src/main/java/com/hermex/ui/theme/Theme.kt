package com.hermex.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val OptimusDarkScheme = darkColorScheme(
    primary = OptimusPrimary,
    onPrimary = Color.White,
    primaryContainer = OptimusSurfaceVariant,
    onPrimaryContainer = OptimusTextPrimary,
    secondary = OptimusSecondary,
    background = OptimusBackground,
    onBackground = OptimusTextPrimary,
    surface = OptimusSurface,
    onSurface = OptimusTextPrimary,
    surfaceVariant = OptimusSurfaceVariant,
    onSurfaceVariant = OptimusTextSecondary,
    error = OptimusError,
    onError = Color.Black
)

private val OptimusLightScheme = lightColorScheme(
    primary = Color(0xFF5145C9),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE7E3FF),
    onPrimaryContainer = Color(0xFF1A1650),
    secondary = Color(0xFF6B4DB2),
    background = Color(0xFFF7F7FA),
    onBackground = Color(0xFF17181D),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF17181D),
    surfaceVariant = Color(0xFFECECF1),
    onSurfaceVariant = Color(0xFF5E606A),
    error = Color(0xFFB3261E),
    onError = Color.White
)

@Composable
fun HermexTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Hermes/Optimus deliberately uses a stable product palette instead of OEM-derived dynamic colors.
    val colorScheme = if (darkTheme) OptimusDarkScheme else OptimusLightScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
