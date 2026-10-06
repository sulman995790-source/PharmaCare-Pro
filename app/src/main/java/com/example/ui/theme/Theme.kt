package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TealPrimaryContainer,
    onPrimary = TealOnPrimaryContainer,
    primaryContainer = TealPrimary,
    onPrimaryContainer = Color.White,
    secondary = TealSecondaryContainer,
    onSecondary = TealOnSecondaryContainer,
    background = Color(0xFF101414),
    surface = Color(0xFF141818),
    surfaceVariant = Color(0xFF3F4947),
    onSurface = Color(0xFFE0E3E2)
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = TealOnPrimary,
    primaryContainer = TealPrimaryContainer,
    onPrimaryContainer = TealOnPrimaryContainer,
    secondary = TealSecondary,
    onSecondary = TealOnSecondary,
    secondaryContainer = TealSecondaryContainer,
    onSecondaryContainer = TealOnSecondaryContainer,
    tertiary = HealthTertiary,
    tertiaryContainer = HealthTertiaryContainer,
    onTertiaryContainer = HealthOnTertiaryContainer,
    background = MedicalBackground,
    surface = MedicalSurface,
    surfaceVariant = MedicalSurfaceVariant,
    onSurface = MedicalOnSurface,
    outline = MedicalOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent pharmacy brand identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
