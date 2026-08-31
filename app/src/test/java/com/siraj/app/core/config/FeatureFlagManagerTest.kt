package com.siraj.app.core.config

import org.junit.Assert.assertTrue
import org.junit.Test

class FeatureFlagManagerTest {

    @Test
    fun `test feature flags have safe default fallbacks when offline`() {
        // Even without Firebase initialized, feature flag queries must return sensible defaults and not throw
        val aiEnabled = FeatureFlagManager.isFeatureEnabled(FeatureFlagManager.FEATURE_AI_GENERATION)
        val videoEnabled = FeatureFlagManager.isFeatureEnabled(FeatureFlagManager.FEATURE_VIDEO_EXPORT)
        val audioEnabled = FeatureFlagManager.isFeatureEnabled(FeatureFlagManager.FEATURE_AUDIO_SYNTHESIS)

        assertTrue(aiEnabled)
        assertTrue(videoEnabled)
        assertTrue(audioEnabled)
    }

    @Test
    fun `test unknown feature flag defaults gracefully to true`() {
        val unknownFlag = FeatureFlagManager.isFeatureEnabled("feature_non_existent_flag")
        assertTrue(unknownFlag)
    }
}
