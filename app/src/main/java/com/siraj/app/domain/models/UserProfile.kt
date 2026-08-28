package com.siraj.app.domain.models

enum class UserRole {
    USER, CREATOR, REVIEWER, ADMIN, OWNER
}

enum class ThemeMode { LIGHT, DARK, SYSTEM }
enum class CalculationMethod { UMM_AL_QURA, MWL, EGYPT, MAKKAH, ISNA, TEHRAN, JAFARI }
enum class Madhab { SHAFI, HANAFI }
enum class VideoQuality { HIGH, MEDIUM, LOW }

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val reduceMotion: Boolean = false,
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
    val analyticsOptIn: Boolean = false
)

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String? = null,
    val role: UserRole = UserRole.USER,
    val preferences: UserPreferences = UserPreferences()
)
