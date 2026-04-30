package com.charles.flashlight.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Amber = Color(0xFFFFB300)
private val AmberSoft = Color(0xFFFFE082)
private val Ink = Color(0xFF0A0A0C)
private val SurfaceDark = Color(0xFF121218)
private val SurfaceElevated = Color(0xFF1C1C24)

private val FlashlightColors = darkColorScheme(
    primary = Amber,
    onPrimary = Color(0xFF1A0E00),
    primaryContainer = Color(0xFF4A3800),
    onPrimaryContainer = AmberSoft,
    secondary = AmberSoft,
    onSecondary = Color(0xFF221A00),
    background = Ink,
    onBackground = Color(0xFFF4F4F5),
    surface = SurfaceDark,
    onSurface = Color(0xFFE8E8EA),
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = Color(0xFFB0B0B8),
    outline = Color(0xFF3A3A44),
    outlineVariant = Color(0xFF2A2A32)
)

private val FlashlightTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 42.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)

@Composable
fun FlashlightTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FlashlightColors,
        typography = FlashlightTypography,
        content = content
    )
}
