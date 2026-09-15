package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Crystal Clear System Font Stack: Inter / Hind Siliguri / Noto Sans Bengali / SansSerif
val JarvisFontFamily = FontFamily.SansSerif

// Header / Title "JARVIS" Style: Font weight 800, letter-spacing 1.sp, Color #00E5FF with glowing shadow
val JarvisTitleStyle = TextStyle(
    fontFamily = JarvisFontFamily,
    fontWeight = FontWeight.ExtraBold, // 800
    fontSize = 30.sp,
    letterSpacing = 1.sp,
    color = ArcCyan,
    shadow = Shadow(
        color = Color(0x9900E5FF),
        offset = Offset(0f, 0f),
        blurRadius = 12f
    )
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = JarvisFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = 1.sp,
        color = Color(0xFFF1F8FD)
    ),
    headlineMedium = TextStyle(
        fontFamily = JarvisFontFamily,
        fontWeight = FontWeight.SemiBold, // 600
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.5.sp,
        color = Color(0xFFF1F8FD)
    ),
    titleLarge = TextStyle(
        fontFamily = JarvisFontFamily,
        fontWeight = FontWeight.SemiBold, // 600
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        color = Color(0xFFF1F8FD)
    ),
    titleMedium = TextStyle(
        fontFamily = JarvisFontFamily,
        fontWeight = FontWeight.SemiBold, // 600
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.3.sp,
        color = Color(0xFFF1F8FD)
    ),
    titleSmall = TextStyle(
        fontFamily = JarvisFontFamily,
        fontWeight = FontWeight.SemiBold, // 600
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.2.sp,
        color = Color(0xFFF1F8FD)
    ),
    bodyLarge = TextStyle(
        fontFamily = JarvisFontFamily,
        fontWeight = FontWeight.Medium, // 500
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.3.sp,
        color = Color(0xFFF1F8FD)
    ),
    bodyMedium = TextStyle(
        fontFamily = JarvisFontFamily,
        fontWeight = FontWeight.Medium, // 500
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
        color = Color(0xFFE2EDF8)
    ),
    bodySmall = TextStyle(
        fontFamily = JarvisFontFamily,
        fontWeight = FontWeight.Medium, // 500
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp,
        color = Color(0xFFB4CDDF)
    ),
    labelLarge = TextStyle(
        fontFamily = JarvisFontFamily,
        fontWeight = FontWeight.SemiBold, // 600
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.4.sp,
        color = Color(0xFFF1F8FD)
    ),
    labelMedium = TextStyle(
        fontFamily = JarvisFontFamily,
        fontWeight = FontWeight.SemiBold, // 600
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp,
        color = Color(0xFF7DF9FF)
    ),
    labelSmall = TextStyle(
        fontFamily = JarvisFontFamily,
        fontWeight = FontWeight.SemiBold, // 600
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.3.sp,
        color = Color(0xFF7DF9FF)
    )
)

