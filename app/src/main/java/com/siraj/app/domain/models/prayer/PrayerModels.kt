package com.siraj.app.domain.models.prayer

data class PrayerTimes(
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val dateHijri: String,
    val dateGregorian: String,
    val meta: PrayerMeta,
)

data class PrayerMeta(
    val method: String,
    val timezone: String,
    val city: String,
    val isCached: Boolean = false,
)

data class PrayerSettings(
    val city: String = "Makkah",
    val country: String = "Saudi Arabia",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val useLocation: Boolean = false,
    val methodId: Int = 4, // 4 = Umm Al-Qura
    val isAsrHanafi: Boolean = false,
    val fajrNotificationEnabled: Boolean = true,
    val dhuhrNotificationEnabled: Boolean = true,
    val asrNotificationEnabled: Boolean = true,
    val maghribNotificationEnabled: Boolean = true,
    val ishaNotificationEnabled: Boolean = true,
    val isQuietTimeEnabled: Boolean = false,
    val quietTimeStart: String = "23:00",
    val quietTimeEnd: String = "06:00",
)
