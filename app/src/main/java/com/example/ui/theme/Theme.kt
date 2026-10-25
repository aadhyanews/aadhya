package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NewsRedLight,
    onPrimary = Color.White,
    primaryContainer = NewsRedDark,
    onPrimaryContainer = Color.White,
    secondary = NewsGold,
    onSecondary = NewsNavyDark,
    secondaryContainer = NewsNavyLight,
    onSecondaryContainer = Color.White,
    tertiary = NewsGoldLight,
    onTertiary = NewsNavyDark,
    background = NewsDarkBackground,
    onBackground = NewsDarkOnSurface,
    surface = NewsDarkSurface,
    onSurface = NewsDarkOnSurface,
    surfaceVariant = NewsDarkSurfaceVariant,
    onSurfaceVariant = NewsDarkOnSurfaceVariant,
    outline = NewsDarkDivider
)

private val LightColorScheme = lightColorScheme(
    primary = NewsRedPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEBEE),
    onPrimaryContainer = NewsRedDark,
    secondary = NewsNavy,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8EDF5),
    onSecondaryContainer = NewsNavyDark,
    tertiary = NewsGold,
    onTertiary = Color.White,
    background = NewsLightBackground,
    onBackground = NewsLightOnSurface,
    surface = NewsLightSurface,
    onSurface = NewsLightOnSurface,
    surfaceVariant = NewsLightSurfaceVariant,
    onSurfaceVariant = NewsLightOnSurfaceVariant,
    outline = NewsLightDivider
)

@Composable
fun AadhyaNewsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
