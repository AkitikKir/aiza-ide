package com.aiza.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AizaPalette {
    // Modern dark palette (Material You inspired)
    val DarkPrimary = Color(0xFF8BABF1)    // Modern blue
    val DarkSecondary = Color(0xFF83C5BE)  // Modern teal
    val DarkTertiary = Color(0xFFF9A8D4)   // Modern pink
    val DarkBackground = Color(0xFF121212) // True dark background
    val DarkSurface = Color(0xFF1E1E1E)    // Elevated surface
    val DarkError = Color(0xFFCF6679)
    val DarkOnPrimary = Color(0xFF000000)
    val DarkOnSecondary = Color(0xFF000000)
    val DarkOnBackground = Color(0xFFFFFFFF)
    val DarkOnSurface = Color(0xFFFFFFFF)
    val DarkOnError = Color(0xFF000000)

    // Modern light palette
    val LightPrimary = Color(0xFF0066CC)   // Vibrant blue
    val LightSecondary = Color(0xFF00857A)  // Vibrant teal
    val LightTertiary = Color(0xFFD53F8C)  // Vibrant pink
    val LightBackground = Color(0xFFF8FAFC) // Soft background
    val LightSurface = Color(0xFFFFFFFF)    // Pure white surface
    val LightError = Color(0xFFB00020)
    val LightOnPrimary = Color(0xFFFFFFFF)
    val LightOnSecondary = Color(0xFFFFFFFF)
    val LightOnBackground = Color(0xFF1A1A1A)
    val LightOnSurface = Color(0xFF1A1A1A)
    val LightOnError = Color(0xFFFFFFFF)
}

private val DarkColorScheme = darkColorScheme(
    primary = AizaPalette.DarkPrimary,
    secondary = AizaPalette.DarkSecondary,
    tertiary = AizaPalette.DarkTertiary,
    background = AizaPalette.DarkBackground,
    surface = AizaPalette.DarkSurface,
    error = AizaPalette.DarkError,
    onPrimary = AizaPalette.DarkOnPrimary,
    onSecondary = AizaPalette.DarkOnSecondary,
    onBackground = AizaPalette.DarkOnBackground,
    onSurface = AizaPalette.DarkOnSurface,
    onError = AizaPalette.DarkOnError
)

private val LightColorScheme = lightColorScheme(
    primary = AizaPalette.LightPrimary,
    secondary = AizaPalette.LightSecondary,
    tertiary = AizaPalette.LightTertiary,
    background = AizaPalette.LightBackground,
    surface = AizaPalette.LightSurface,
    error = AizaPalette.LightError,
    onPrimary = AizaPalette.LightOnPrimary,
    onSecondary = AizaPalette.LightOnSecondary,
    onBackground = AizaPalette.LightOnBackground,
    onSurface = AizaPalette.LightOnSurface,
    onError = AizaPalette.LightOnError
)

/**
 * Modern Material Design 3 theme wrapper with dark/light palettes
 */
@Composable
fun AizaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}