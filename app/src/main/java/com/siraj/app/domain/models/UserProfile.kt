package com.siraj.app.domain.models

enum class UserRole {
    USER,
    CREATOR,
    REVIEWER,
    ADMIN,
    OWNER,
}

enum class ThemeMode { LIGHT, DARK, SYSTEM }

enum class CalculationMethod { UMM_AL_QURA, MWL, EGYPT, MAKKAH, ISNA, TEHRAN, JAFARI }

enum class Madhab { SHAFI, HANAFI }

enum class VideoQuality { HIGH, MEDIUM, LOW }

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val highContrastMode: Boolean = false,
    val fontScaleMultiplier: Float = 1.0f,
    val reduceMotion: Boolean = false,
    val showCaptions: Boolean = true,
    val showTranscripts: Boolean = true,
    val screenReaderOptimized: Boolean = false,
    val soundAlertsWithHaptic: Boolean = true,
    val language: String = "ar",
    val city: String = "",
    val prayerNotifications: Boolean = true,
    val adhkarNotifications: Boolean = true,
    val calculationMethod: CalculationMethod = CalculationMethod.UMM_AL_QURA,
    val madhab: Madhab = Madhab.SHAFI,
    val videoQuality: VideoQuality = VideoQuality.HIGH,
    val downloadWifiOnly: Boolean = true,
    val appLockEnabled: Boolean = false,
    val activeWorkspaceId: String? = null,
    val analyticsOptIn: Boolean = false,
    val crashReportsOptIn: Boolean = true,
    val personalizationOptIn: Boolean = false,
    val locationOptIn: Boolean = true,
    val preciseLocationOptIn: Boolean = false,
    val accountDeletionStatus: String = "NONE",
    val accountDeletionScheduledAt: Long? = null,
)

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String? = null,
    val role: UserRole = UserRole.USER,
    val preferences: UserPreferences = UserPreferences(),
)
