package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

import androidx.compose.runtime.CompositionLocalProvider

private val BentoLightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    secondary = Color(0xFF0061A4),
    tertiary = Color(0xFFF3EDF7),
    background = Color(0xFFFEF7FF),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1D1B20),
    onSurface = Color(0xFF1D1B20)
)

private val BentoDarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    secondary = Color(0xFF71A5F5),
    tertiary = Color(0xFF1D1B22),
    background = Color(0xFF0A090D),
    surface = Color(0xFF131116),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color(0xFFF4EFF4),
    onSurface = Color(0xFFF4EFF4)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Force the custom Bento palette for accurate styling matching Design HTML
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkBentoColors else LightBentoColors
    val colorScheme = if (darkTheme) BentoDarkColorScheme else BentoLightColorScheme

    CompositionLocalProvider(LocalBentoColors provides colors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
