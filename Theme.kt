package com.emad.smartdialer.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF0B57D0),
    onPrimary = Color.White,
    secondary = Color(0xFF146C2E),
    surfaceVariant = Color(0xFFE1E3E1),
    error = Color(0xFFBA1A1A)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA8C7FA),
    secondary = Color(0xFF6DD58C),
    surfaceVariant = Color(0xFF444746),
    error = Color(0xFFF2B8B5)
)

@Composable
fun SmartDialerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val scheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(androidx.compose.ui.platform.LocalContext.current)
            else dynamicLightColorScheme(androidx.compose.ui.platform.LocalContext.current)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
