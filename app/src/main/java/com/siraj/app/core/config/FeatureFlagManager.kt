package com.siraj.app.core.config

import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.siraj.app.R
import com.siraj.app.core.security.SanitizedLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object FeatureFlagManager {
    private var remoteConfigInstance: FirebaseRemoteConfig? = null
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    private var isInitializing = false

    // Define Feature Keys
    const val FEATURE_AI_GENERATION = "feature_ai_generation"
    const val FEATURE_VIDEO_EXPORT = "feature_video_export"
    const val FEATURE_AUDIO_SYNTHESIS = "feature_audio_synthesis"
    const val FEATURE_SYSTEM_READ_ONLY_MODE = "feature_system_read_only_mode"
    const val FEATURE_MAINTENANCE_MODE = "feature_maintenance_mode"

    // Local in-memory fallbacks when offline or remote config is unprovisioned
    private val localDefaults = mapOf(
        FEATURE_AI_GENERATION to true,
        FEATURE_VIDEO_EXPORT to true,
        FEATURE_AUDIO_SYNTHESIS to true,
        FEATURE_SYSTEM_READ_ONLY_MODE to false,
        FEATURE_MAINTENANCE_MODE to false
    )

    fun initialize() {
        if (_isInitialized.value || isInitializing) {
            return
        }
        isInitializing = true

        try {
            if (FirebaseApp.getApps(com.siraj.app.SirajApplication.instance).isEmpty()) {
                SanitizedLogger.d("FeatureFlagManager", "FirebaseApp not yet initialized, using local defaults.")
                _isInitialized.value = true
                isInitializing = false
                return
            }

            val remoteConfig = FirebaseRemoteConfig.getInstance().also {
                remoteConfigInstance = it
            }

            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(if (EnvironmentConfig.currentEnvironment == EnvironmentType.DEVELOPMENT) 0 else 3600)
                .setFetchTimeoutInSeconds(5)
                .build()

            remoteConfig.setConfigSettingsAsync(configSettings)
            try {
                remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
            } catch (e: Exception) {
                SanitizedLogger.d("FeatureFlagManager", "Using in-memory defaults fallback.")
            }

            remoteConfig.fetchAndActivate()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        SanitizedLogger.d("FeatureFlagManager", "Remote config updated: ${task.result}")
                    } else {
                        SanitizedLogger.d("FeatureFlagManager", "Remote config fetch bypassed or offline; local defaults active.")
                    }
                    _isInitialized.value = true
                    isInitializing = false
                }
        } catch (e: Exception) {
            SanitizedLogger.d("FeatureFlagManager", "FeatureFlagManager initialized with local defaults fallback.")
            _isInitialized.value = true
            isInitializing = false
        }
    }

    fun isFeatureEnabled(key: String): Boolean {
        return try {
            remoteConfigInstance?.getBoolean(key) ?: localDefaults[key] ?: true
        } catch (e: Exception) {
            localDefaults[key] ?: true
        }
    }
}
