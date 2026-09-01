package com.siraj.app.domain.models.prayer

/**
 * مواقيت الصلاة الذكية
 */
data class SmartPrayerSchedule(
    val fajr: PrayerTime,
    val sunrise: PrayerTime,
    val dhuhr: PrayerTime,
    val asr: PrayerTime,
    val maghrib: PrayerTime,
    val isha: PrayerTime,
    val location: String,
    val date: String,
)

data class PrayerTime(
    val name: String,
    val arabicName: String,
    val time24h: String,
    val time12h: String,
    val timestamp: Long,
    val isNext: Boolean = false,
    val timeUntil: String = "",
)

/**
 * إعدادات التنبيه الذكي
 */
data class SmartReminderSettings(
    val enabled: Boolean = true,
    val minutesBefore: Int = 10,
    val silentDuringWork: Boolean = false,
    val silentAtNight: Boolean = true,
    val workStartTime: String = "09:00",
    val workEndTime: String = "17:00",
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val adhanEnabled: Boolean = false,
    val customAdhan: String? = null,
    val locationBased: Boolean = true,
    val travelMode: Boolean = false,
)

/**
 * حالة الصلاة القادمة
 */
data class NextPrayerInfo(
    val name: String,
    val arabicName: String,
    val timeUntil: String,
    val minutesUntil: Int,
    val timestamp: Long,
    val isUrgent: Boolean,
)

/**
 * إحصائيات الأداء
 */
data class PrayerIntelligenceStats(
    val onTimeCount: Int = 0,
    val missedCount: Int = 0,
    val earlyCount: Int = 0,
    val lateCount: Int = 0,
    val averageDelayMinutes: Int = 0,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val weeklyTrend: List<Float> = emptyList(),
    val mostMissedPrayer: String? = null,
)
