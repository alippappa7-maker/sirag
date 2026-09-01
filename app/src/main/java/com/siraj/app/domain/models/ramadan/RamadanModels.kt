package com.siraj.app.domain.models.ramadan

/**
 * نماذج وضع رمضان
 */

enum class RamadanPhase(val arabicName: String) {
    PRE_RAMADAN("ما قبل رمضان"),
    FIRST_TEN("العشر الأولى"),
    SECOND_TEN("العشر الوسط"),
    LAST_TEN("العشر الأواخر"),
    EID("عيد الفطر"),
}

data class RamadanDayInfo(
    val dayNumber: Int,          // 1-30
    val hijriDate: String,
    val phase: RamadanPhase,
    val fajrTime: String?,
    val suhoorTime: String?,    // السحور
    val iftarTime: String?,     // الإفطار
    val ishaTime: String?,
    val taraweehTime: String?,
    val isFastDay: Boolean = true,
    val specialNote: String? = null,
)

data class RamadanStats(
    val fastingDays: Int,
    val totalDays: Int = 30,
    val missedDays: Int,
    val currentStreak: Int,
    val bestStreak: Int,
)

data class LailatulQadrInfo(
    val isLastTenDays: Boolean,
    val oddNights: List<String>,  // ليلة 21، 23، 25، 27، 29
    val recommendedNights: List<String>,
)

data class RamadanDua(
    val id: String,
    val title: String,
    val arabicText: String,
    val translation: String,
    val source: String,
    val occasion: String,  // "السحور", "الإفطار", "العشر الأواخر"...
)

data class RamadanInfo(
    val days: List<RamadanDayInfo>,
    val stats: RamadanStats,
    val lailatulQadr: LailatulQadrInfo,
    val duas: List<RamadanDua>,
    val startDate: String?,
    val endDate: String?,
)
