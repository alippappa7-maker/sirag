package com.siraj.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Arabic text requires slightly larger line heights (increased by ~15% from M3 defaults)
fun getScaledTypography(multiplier: Float = 1.0f): Typography {
    val scale = multiplier.coerceIn(0.85f, 2.0f)
    return Typography(
        displayLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = (32 * scale).sp,
            lineHeight = (46 * scale).sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = (24 * scale).sp,
            lineHeight = (36 * scale).sp,
            letterSpacing = 0.sp
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (20 * scale).sp,
            lineHeight = (32 * scale).sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (18 * scale).sp,
            lineHeight = (28 * scale).sp,
            letterSpacing = 0.15.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (16 * scale).sp,
            lineHeight = (28 * scale).sp, // +15%
            letterSpacing = 0.15.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (14 * scale).sp,
            lineHeight = (24 * scale).sp, // +15%
            letterSpacing = 0.25.sp
        ),
        labelLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (14 * scale).sp,
            lineHeight = (22 * scale).sp,
            letterSpacing = 0.1.sp
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (11 * scale).sp,
            lineHeight = (18 * scale).sp,
            letterSpacing = 0.5.sp
        )
    )
}

val Typography = getScaledTypography(1.0f)
