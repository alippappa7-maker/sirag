package com.siraj.app.features.prayer_intelligence.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.siraj.app.domain.models.prayer.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.max
import kotlin.math.min

data class PrayerIntelligenceUiState(
    val nextPrayer: NextPrayerInfo? = null,
    val schedule: SmartPrayerSchedule? = null,
    val settings: SmartReminderSettings = SmartReminderSettings(),
    val stats: PrayerIntelligenceStats = PrayerIntelligenceStats(),
    val todayProgress: Float = 0f,
    val prayerTimeline: List<PrayerTime> = emptyList(),
    val isPrayerTime: Boolean = false,
)

class PrayerIntelligenceViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PrayerIntelligenceUiState())
    val uiState: StateFlow<PrayerIntelligenceUiState> = _uiState.asStateFlow()

    init {
        loadMockSchedule()
    }

    private fun loadMockSchedule() {
        val calendar = Calendar.getInstance()
        val today = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 4)
            set(Calendar.MINUTE, 42)
        }.timeInMillis

        val schedule = SmartPrayerSchedule(
            fajr = PrayerTime("Fajr", "الفجر", "04:42", "4:42 ص", today),
            sunrise = PrayerTime("Sunrise", "الشروق", "06:15", "6:15 ص", calendar.apply { set(Calendar.HOUR_OF_DAY, 6); set(Calendar.MINUTE, 15) }.timeInMillis),
            dhuhr = PrayerTime("Dhuhr", "الظهر", "12:30", "12:30 م", calendar.apply { set(Calendar.HOUR_OF_DAY, 12); set(Calendar.MINUTE, 30) }.timeInMillis),
            asr = PrayerTime("Asr", "العصر", "16:18", "4:18 م", calendar.apply { set(Calendar.HOUR_OF_DAY, 16); set(Calendar.MINUTE, 18) }.timeInMillis),
            maghrib = PrayerTime("Maghrib", "المغرب", "19:02", "7:02 م", calendar.apply { set(Calendar.HOUR_OF_DAY, 19); set(Calendar.MINUTE, 2) }.timeInMillis),
            isha = PrayerTime("Isha", "العشاء", "20:32", "8:32 م", calendar.apply { set(Calendar.HOUR_OF_DAY, 20); set(Calendar.MINUTE, 32) }.timeInMillis),
            location = "موقعك الحالي",
            date = "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}",
        )

        val timeline = listOf(
            schedule.fajr,
            schedule.sunrise,
            schedule.dhuhr,
            schedule.asr,
            schedule.maghrib,
            schedule.isha,
        )

        val nextPrayer = calculateNextPrayer(timeline)
        val progress = calculateTodayProgress(timeline)
        val stats = PrayerIntelligenceStats(
            onTimeCount = 142,
            missedCount = 8,
            earlyCount = 23,
            lateCount = 15,
            averageDelayMinutes = 3,
            streak = 12,
            bestStreak = 27,
            weeklyTrend = listOf(0.8f, 0.9f, 0.7f, 1.0f, 0.85f, 0.92f, 0.88f),
            mostMissedPrayer = "الفجر",
        )

        _uiState.value = _uiState.value.copy(
            schedule = schedule,
            nextPrayer = nextPrayer,
            todayProgress = progress,
            prayerTimeline = timeline,
            stats = stats,
        )
    }

    private fun calculateNextPrayer(timeline: List<PrayerTime>): NextPrayerInfo? {
        val now = System.currentTimeMillis()
        val next = timeline.firstOrNull { it.timestamp > now }
            ?: timeline.first() // اليوم التالي

        val minutesUntil = ((next.timestamp - now) / 60000).toInt().coerceAtLeast(0)
        val hours = minutesUntil / 60
        val mins = minutesUntil % 60
        val timeUntil = if (hours > 0) "$hours ساعة و $mins دقيقة" else "$mins دقيقة"

        return NextPrayerInfo(
            name = next.name,
            arabicName = next.arabicName,
            timeUntil = timeUntil,
            minutesUntil = minutesUntil,
            timestamp = next.timestamp,
            isUrgent = minutesUntil <= 30,
        )
    }

    private fun calculateTodayProgress(timeline: List<PrayerTime>): Float {
        val now = System.currentTimeMillis()
        val firstTime = timeline.first().timestamp
        val lastTime = timeline.last().timestamp
        if (now <= firstTime) return 0f
        if (now >= lastTime) return 1f
        return ((now - firstTime).toFloat() / (lastTime - firstTime).toFloat()).coerceIn(0f, 1f)
    }

    fun updateSettings(newSettings: SmartReminderSettings) {
        _uiState.value = _uiState.value.copy(settings = newSettings)
    }
}
