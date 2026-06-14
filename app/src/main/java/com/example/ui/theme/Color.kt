package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Bento Colors holds our cohesive styled color variables
class BentoColors(
    val background: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val surface: Color,
    val surfaceMuted: Color,
    val border: Color,
    val gridEmpty: Color,
    val gridLevel1: Color,
    val gridLevel2: Color,
    val gridLevel3: Color,
    val gridLevel4: Color,
    val accentBlue: Color,
    val badgeBg: Color,
    val badgeText: Color,
    val redAlert: Color
)

// Light Theme Palette
val LightBentoColors = BentoColors(
    background = Color(0xFFFEF7FF),
    textPrimary = Color(0xFF1D1B20),
    textSecondary = Color(0xFF6750A4),
    surface = Color.White,
    surfaceMuted = Color(0xFFF3EDF7),
    border = Color(0xFFCAC4D0),
    gridEmpty = Color(0xFFEFE8F4),
    gridLevel1 = Color(0xFFD3E3FD),
    gridLevel2 = Color(0xFFA8C7FA),
    gridLevel3 = Color(0xFF7CABF9),
    gridLevel4 = Color(0xFF0061A4),
    accentBlue = Color(0xFF0061A4),
    badgeBg = Color(0xFFEADDFF),
    badgeText = Color(0xFF21005D),
    redAlert = Color(0xFFB3261E)
)

// Luxury Minimalist Dark Theme Palette
val DarkBentoColors = BentoColors(
    background = Color(0xFF0A090D),     // Ultra dark obsidian canvas
    textPrimary = Color(0xFFF4EFF4),     // Bright crisp minimal text
    textSecondary = Color(0xFFD0BCFF),   // Warm soft lavender accent
    surface = Color(0xFF131116),         // Level 1 Slate card container
    surfaceMuted = Color(0xFF1D1B22),    // Inactive muted slate
    border = Color(0xFF2A2830),          // Subtle low-contrast border layout lines
    gridEmpty = Color(0xFF18161D),       // Muted bento empty tile
    gridLevel1 = Color(0xFF23324E),       // Muted storm blue
    gridLevel2 = Color(0xFF345281),       // Soft ocean indigo
    gridLevel3 = Color(0xFF4C78B8),       // Mid blue
    gridLevel4 = Color(0xFF71A5F5),       // Vibrant high-visibility electric bento blue
    accentBlue = Color(0xFF71A5F5),
    badgeBg = Color(0xFF2E224F),         // Deep active badge container
    badgeText = Color(0xFFE6D6FF),        // Soft lilac
    redAlert = Color(0xFFE57373)         // Soft alert rose
)

// Composition local for runtime lookup
val LocalBentoColors = staticCompositionLocalOf { LightBentoColors }

// Expose individual colors as composable getters to avoid breaking existing code
val BentoBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalBentoColors.current.background

val BentoTextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalBentoColors.current.textPrimary

val BentoTextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalBentoColors.current.textSecondary

val BentoSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalBentoColors.current.surface

val BentoSurfaceMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalBentoColors.current.surfaceMuted

val BentoBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalBentoColors.current.border

val BentoGridEmpty: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalBentoColors.current.gridEmpty

val BentoGridLevel1: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalBentoColors.current.gridLevel1

val BentoGridLevel2: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalBentoColors.current.gridLevel2

val BentoGridLevel3: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalBentoColors.current.gridLevel3

val BentoGridLevel4: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalBentoColors.current.gridLevel4

val BentoAccentBlue: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalBentoColors.current.accentBlue

val BentoBadgeBg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalBentoColors.current.badgeBg

val BentoBadgeText: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalBentoColors.current.badgeText

val BentoRedAlert: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalBentoColors.current.redAlert
