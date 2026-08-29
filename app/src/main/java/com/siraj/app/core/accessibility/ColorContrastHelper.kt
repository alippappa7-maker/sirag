package com.siraj.app.core.accessibility

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Utility to calculate and verify WCAG 2.1 contrast ratios for text and UI elements.
 */
object ColorContrastHelper {

    /**
     * Calculate relative luminance of a Compose Color according to WCAG 2.1 specs.
     */
    fun calculateLuminance(color: Color): Double {
        val r = linearizeColorComponent(color.red)
        val g = linearizeColorComponent(color.green)
        val b = linearizeColorComponent(color.blue)
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    private fun linearizeColorComponent(component: Float): Double {
        return if (component <= 0.04045f) {
            component / 12.92
        } else {
            ((component + 0.055) / 1.055).pow(2.4)
        }
    }

    /**
     * Returns the contrast ratio between foreground and background (range 1.0 to 21.0).
     */
    fun getContrastRatio(foreground: Color, background: Color): Double {
        val lum1 = calculateLuminance(foreground)
        val lum2 = calculateLuminance(background)
        val brightest = max(lum1, lum2)
        val darkest = min(lum1, lum2)
        return (brightest + 0.05) / (darkest + 0.05)
    }

    /**
     * Checks if contrast meets WCAG 2.1 AA requirement (>= 4.5:1 for normal text, >= 3.0:1 for large text).
     */
    fun meetsWcagAA(foreground: Color, background: Color, isLargeText: Boolean = false): Boolean {
        val ratio = getContrastRatio(foreground, background)
        return if (isLargeText) ratio >= 3.0 else ratio >= 4.5
    }

    /**
     * Checks if contrast meets WCAG 2.1 AAA requirement (>= 7.0:1 for normal text, >= 4.5:1 for large text).
     */
    fun meetsWcagAAA(foreground: Color, background: Color, isLargeText: Boolean = false): Boolean {
        val ratio = getContrastRatio(foreground, background)
        return if (isLargeText) ratio >= 4.5 else ratio >= 7.0
    }
}
