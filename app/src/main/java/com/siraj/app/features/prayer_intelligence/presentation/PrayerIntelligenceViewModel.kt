package com.siraj.app.features.prayer_intelligence.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.data.repository.prayer.AladhanPrayerRepositoryImpl
import com.siraj.app.domain.models.prayer.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
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

    private val repository = AladhanPrayerRepositoryImpl()
    private val _uiState = MutableStateFlow(PrayerIntelligenceUiState())
    val uiState: StateFlow<PrayerIntelligenceUiState> = _uiState.asStateFlow()

    init {
        loadSchedule()
    }

    private fun loadSchedule() {
        viewModelScope.launch {
            val result = repository.getPrayerTimes(PrayerSettings())
            val calendar = Calendar.getInstance()
            val baseDateStr = "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"

            if (result is Resource.Success) {
                val times = result.data
                val sdf = SimpleDateFormat("HH:mm", Locale.US)

                fun parseToTodayMillis(timeStr: String): Long {
                    return try {
                        val parsed = sdf.parse(timeStr)
                        if (parsed != null) {
                            val c = Calendar.getInstance()
                            val parsedCal = Calendar.getInstance().apply { time = parsed }
                            c.set(Calendar.HOUR_OF_DAY, parsedCal.get(Calendar.HOUR_OF_DAY))
                            c.set(Calendar.MINUTE, parsedCal.get(Calendar.MINUTE))
                            c.set(Calendar.SECOND, 0)
                            c.timeInMillis
                        } else System.currentTimeMillis()
                    } catch (e: Exception) {
                        System.currentTimeMillis()
                    }
                }

                val schedule = SmartPrayerSchedule(
                    fajr = PrayerTime("Fajr", "الفجر", times.fajr, times.fajr, parseToTodayMillis(times.fajr)),
                    sunrise = PrayerTime("Sunrise", "الشروق", times.sunrise, times.sunrise, parseToTodayMillis(times.sunrise)),
                    dhuhr = PrayerTime("Dhuhr", "الظهر", times.dhuhr, times.dhuhr, parseToTodayMillis(times.dhuhr)),
                    asr = PrayerTime("Asr", "العصر", times.asr, times.asr, parseToTodayMillis(times.asr)),
                    maghrib = PrayerTime("Maghrib", "المغرب", times.maghrib, times.maghrib, parseToTodayMillis(times.maghrib)),
                    isha = PrayerTime("Isha", "العشاء", times.isha, times.isha, parseToTodayMillis(times.isha)),
                    location = times.meta.city,
                    date = baseDateStr,
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

                _uiState.value = _uiState.value.copy(
                    schedule = schedule,
                    nextPrayer = nextPrayer,
                    todayProgress = progress,
                    prayerTimeline = timeline,
                )
            }
        }
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
