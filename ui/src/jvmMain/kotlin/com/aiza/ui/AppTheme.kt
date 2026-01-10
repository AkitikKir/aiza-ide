package com.aiza.ui

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AizaPalette {
    // Dark palette
    val DarkPrimary = Color(0xFF90CAF9)   // Light Blue 200
    val DarkPrimaryVariant = Color(0xFF64B5F6)
    val DarkSecondary = Color(0xFF80CBC4) // Teal 200
    val DarkBackground = Color(0xFF0F1115) // Near-black
    val DarkSurface = Color(0xFF161A1F)
    val DarkError = Color(0xFFCF6679)
    val DarkOnPrimary = Color(0xFF0B0E11)
    val DarkOnSecondary = Color(0xFF0B0E11)
    val DarkOnBackground = Color(0xFFE3E6EA)
    val DarkOnSurface = Color(0xFFE3E6EA)
    val DarkOnError = Color(0xFF0B0E11)

    // Light palette
    val LightPrimary = Color(0xFF1976D2)
    val LightPrimaryVariant = Color(0xFF1565C0)
    val LightSecondary = Color(0xFF00897B)
    val LightBackground = Color(0xFFF7F9FC)
    val LightSurface = Color(0xFFFFFFFF)
    val LightError = Color(0xFFB00020)
    val LightOnPrimary = Color(0xFFFFFFFF)
    val LightOnSecondary = Color(0xFFFFFFFF)
    val LightOnBackground = Color(0xFF0F1115)
    val LightOnSurface = Color(0xFF0F1115)
    val LightOnError = Color(0xFFFFFFFF)
}

private fun darkPalette(): Colors = darkColors(
    primary = AizaPalette.DarkPrimary,
    primaryVariant = AizaPalette.DarkPrimaryVariant,
    secondary = AizaPalette.DarkSecondary,
    background = AizaPalette.DarkBackground,
    surface = AizaPalette.DarkSurface,
    error = AizaPalette.DarkError,
    onPrimary = AizaPalette.DarkOnPrimary,
    onSecondary = AizaPalette.DarkOnSecondary,
    onBackground = AizaPalette.DarkOnBackground,
    onSurface = AizaPalette.DarkOnSurface,
    onError = AizaPalette.DarkOnError
)

private fun lightPalette(): Colors = lightColors(
    primary = AizaPalette.LightPrimary,
    primaryVariant = AizaPalette.LightPrimaryVariant,
    secondary = AizaPalette.LightSecondary,
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
 * App-wide theme wrapper with dark/light palettes.
 */
@Composable
fun AizaTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) darkPalette() else lightPalette(),
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}