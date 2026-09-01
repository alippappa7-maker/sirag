package com.siraj.app.domain.models.prayer

/**
 * نماذج تتبع الصلوات اليومية والإحصائيات
 */

enum class PrayerName(val arabicName: String, val order: Int) {
    FAJR("الفجر", 1),
    DHUHR("الظهر", 2),
    ASR("العصر", 3),
    MAGHRIB("المغرب", 4),
    ISHA("العشاء", 5),
}

enum class PrayerStatus(val arabicName: String) {
    PRAYED("مصلّاة"),
    MISSED("فائتة"),
    PENDING("لم تُصلَّ بعد"),
}

data class PrayerTrackerEntry(
    val date: String,        // YYYY-MM-DD
    val prayerName: PrayerName,
    val status: PrayerStatus,
    val prayedAt: String?,  // HH:mm
    val isGroupPrayer: Boolean = false,  // صلاة جماعة
    val inMosque: Boolean = false,
)

data class PrayerDayRecord(
    val date: String,
    val prayers: Map<PrayerName, PrayerStatus>,
    val completedCount: Int,
    val missedCount: Int,
    val pendingCount: Int,
)

data class PrayerStats(
    val totalPrayed: Int,
    val totalMissed: Int,
    val streak: Int,
    val bestStreak: Int,
    val weeklyCompletionRate: Float,  // 0.0 - 1.0
    val mosqueAttendanceRate: Float,
    val groupPrayerRate: Float,
    val last30Days: List<PrayerDayRecord>,
)

data class PrayerStreak(
    val currentStreak: Int,
    val bestStreak: Int,
    val totalCompletedDays: Int,
    val totalAttemptedDays: Int,
)
