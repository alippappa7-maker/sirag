package com.siraj.app.core.accessibility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import com.siraj.app.domain.models.UserPreferences

@Immutable
data class AccessibilityConfig(
    val highContrastMode: Boolean = false,
    val fontScaleMultiplier: Float = 1.0f,
    val reduceMotion: Boolean = false,
    val showCaptions: Boolean = true,
    val showTranscripts: Boolean = true,
    val screenReaderOptimized: Boolean = false,
    val soundAlertsWithHaptic: Boolean = true
) {
    companion object {
        fun fromPreferences(preferences: UserPreferences): AccessibilityConfig {
            return AccessibilityConfig(
                highContrastMode = preferences.highContrastMode,
                fontScaleMultiplier = preferences.fontScaleMultiplier.coerceIn(0.85f, 2.0f),
                reduceMotion = preferences.reduceMotion,
                showCaptions = preferences.showCaptions,
                showTranscripts = preferences.showTranscripts,
                screenReaderOptimized = preferences.screenReaderOptimized,
                soundAlertsWithHaptic = preferences.soundAlertsWithHaptic
            )
        }
    }
}

val LocalAccessibilityConfig = staticCompositionLocalOf { AccessibilityConfig() }

@Composable
fun ProvideAccessibilityConfig(
    config: AccessibilityConfig,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalAccessibilityConfig provides config,
        content = content
    )
}
