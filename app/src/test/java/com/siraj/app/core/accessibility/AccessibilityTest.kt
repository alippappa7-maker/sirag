package com.siraj.app.core.accessibility

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.siraj.app.domain.models.UserPreferences


import com.siraj.app.ui.theme.Typography
import com.siraj.app.ui.theme.getScaledTypography
import org.junit.Assert.*
import org.junit.Test

class AccessibilityTest {

    @Test
    fun testBlackAndWhiteContrastRatio_isMaximal() {
        val ratio = ColorContrastHelper.getContrastRatio(Color.Black, Color.White)
        assertEquals(21.0, ratio, 0.1)
    }

    @Test
    fun testIdenticalColorsContrastRatio_isOne() {
        val ratio = ColorContrastHelper.getContrastRatio(Color(0xFF123456), Color(0xFF123456))
        assertEquals(1.0, ratio, 0.01)
    }

    @Test
    fun testWcagComplianceRules() {
        // Black on White is 21:1 -> passes both AA and AAA
        assertTrue(ColorContrastHelper.meetsWcagAA(Color.Black, Color.White, isLargeText = false))
        assertTrue(ColorContrastHelper.meetsWcagAAA(Color.Black, Color.White, isLargeText = false))

        // Poor contrast pair (light gray on white)
        val lightGray = Color(0xFFCCCCCC)
        val white = Color.White
        assertFalse(ColorContrastHelper.meetsWcagAA(lightGray, white, isLargeText = false))
        assertFalse(ColorContrastHelper.meetsWcagAAA(lightGray, white, isLargeText = false))
    }

    @Test
    fun testTypographyScaling_increasesFontSizeProportionately() {
        val baseTypography = Typography
        val scaledTypography = getScaledTypography(1.5f)

        assertEquals(
            baseTypography.bodyLarge.fontSize.value * 1.5f,
            scaledTypography.bodyLarge.fontSize.value,
            0.01f
        )
        assertEquals(
            baseTypography.titleMedium.fontSize.value * 1.5f,
            scaledTypography.titleMedium.fontSize.value,
            0.01f
        )
        assertEquals(
            baseTypography.headlineMedium.fontSize.value * 1.5f,
            scaledTypography.headlineMedium.fontSize.value,
            0.01f
        )
    }

    @Test
    fun testUserPreferences_defaultAccessibilityValues() {
        val defaultPrefs = UserPreferences()
        assertFalse(defaultPrefs.highContrastMode)
        assertEquals(1.0f, defaultPrefs.fontScaleMultiplier, 0.01f)
        assertFalse(defaultPrefs.reduceMotion)
        assertTrue(defaultPrefs.showCaptions)
        assertTrue(defaultPrefs.showTranscripts)
        assertFalse(defaultPrefs.screenReaderOptimized)
        assertTrue(defaultPrefs.soundAlertsWithHaptic)
    }

    @Test
    fun testUserPreferences_customAccessibilityConfiguration() {
        val customPrefs = UserPreferences(
            highContrastMode = true,
            fontScaleMultiplier = 1.3f,
            reduceMotion = true,
            showCaptions = true,
            showTranscripts = true,
            screenReaderOptimized = true,
            soundAlertsWithHaptic = true
        )
        assertTrue(customPrefs.highContrastMode)
        assertEquals(1.3f, customPrefs.fontScaleMultiplier, 0.01f)
        assertTrue(customPrefs.reduceMotion)
        assertTrue(customPrefs.showCaptions)
        assertTrue(customPrefs.showTranscripts)
        assertTrue(customPrefs.screenReaderOptimized)
    }
}
