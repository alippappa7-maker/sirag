package com.siraj.app.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * SIRAJ — Techno-Spiritual Glow System
 * A unique visual language based on electric violet ambiance.
 * Defines reusable glow tokens and gradients used across the app.
 */
object SirajGlow {
    // Subtle glow for regular cards and backgrounds
    val subtle: Color = Color(0x1AA78BFA)      // 10% opacity electric violet

    // Medium glow for active components
    val medium: Color = Color(0x33A78BFA)      // 20% opacity

    // Strong glow for highlighted/selected elements
    val strong: Color = Color(0x66A78BFA)      // 40% opacity

    // Inner glow for elevated components
    val inner: Color = Color(0x0FA78BFA)       // 6% opacity inner halo

    // Hero gradient for main screens (radial from center)
    val heroGradient: Brush =
        Brush.radialGradient(
            colors = listOf(Color(0xFF7C3AED).copy(alpha = 0.25f), Color(0xFF0B0F1A)),
            radius = 500f,
        )

    // Card gradient for featured cards (deep violet blend)
    val cardGradient: Brush =
        Brush.linearGradient(
            colors = listOf(Color(0xFF4C1D95), Color(0xFF1E1B4B)),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
        )

    // Button gradient (electric violet)
    val buttonGradient: Brush =
        Brush.linearGradient(
            colors = listOf(Color(0xFF7C3AED), Color(0xFF6D28D9)),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
        )

    // Accent gradient (cyan to violet)
    val accentGradient: Brush =
        Brush.linearGradient(
            colors = listOf(Color(0xFF22D3EE), Color(0xFFA78BFA)),
        )

    // Bottom navigation glow (horizontal soft gradient)
    val navGlow: Brush =
        Brush.linearGradient(
            colors = listOf(Color(0xFF7C3AED).copy(alpha = 0.2f), Color(0xFF22D3EE).copy(alpha = 0.1f), Color(0xFF7C3AED).copy(alpha = 0.2f)),
        )

    // Status success glow
    val successGlow: Color = Color(0x3334D399)

    // Status warning glow
    val warningGlow: Color = Color(0x33FBBF24)

    // Status error glow
    val errorGlow: Color = Color(0x33FB7185)

    // Processing glow
    val processingGlow: Color = Color(0x33A78BFA)
}
