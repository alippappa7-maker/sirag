package com.siraj.app.core.config

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.siraj.app.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object FeatureFlagManager {
    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    // Define Feature Keys
    const val FEATURE_AI_GENERATION = "feature_ai_generation"
    const val FEATURE_VIDEO_EXPORT = "feature_video_export"
    const val FEATURE_AUDIO_SYNTHESIS = "feature_audio_synthesis"

    fun initialize() {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(if (EnvironmentConfig.currentEnvironment == EnvironmentType.DEVELOPMENT) 0 else 3600)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FeatureFlagManager", "Config params updated: ${task.result}")
                } else {
                    Log.e("FeatureFlagManager", "Fetch failed")
                }
                _isInitialized.value = true
            }
    }

    fun isFeatureEnabled(key: String): Boolean {
        return remoteConfig.getBoolean(key)
    }
}
