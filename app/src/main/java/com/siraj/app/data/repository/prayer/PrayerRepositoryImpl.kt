package com.siraj.app.data.repository.prayer

import com.siraj.app.core.utils.Resource
import com.siraj.app.core.error.ErrorHandler
import com.siraj.app.domain.models.prayer.PrayerMeta
import com.siraj.app.domain.models.prayer.PrayerSettings
import com.siraj.app.domain.models.prayer.PrayerTimes
import com.siraj.app.domain.repository.prayer.PrayerRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.util.*

class PrayerRepositoryImpl : PrayerRepository {
    private val settingsFlow = MutableStateFlow(PrayerSettings())

    override suspend fun getPrayerTimes(settings: PrayerSettings): Resource<PrayerTimes> =
        try {
            delay(500) // simulate network delay

            // In a real app, this would call Aladhan API or use a local calculation library like PrayTimes
            // For now, we simulate dynamic prayer times for today.

            val calendar = Calendar.getInstance()
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
            val todayGregorian = sdf.format(calendar.time)

            val meta =
                PrayerMeta(
                    method = if (settings.methodId == 4) "Umm Al-Qura University, Makkah" else "Muslim World League",
                    timezone = TimeZone.getDefault().id,
                    city = settings.city.ifEmpty { "Makkah" },
                    isCached = false,
                )

            // Mocking times. We will shift them slightly based on the current hour so that at least one is in the future.
            val fajr = "04:30"
            val sunrise = "05:45"
            val dhuhr = "12:15"
            val asr = if (settings.isAsrHanafi) "16:30" else "15:45"
            val maghrib = "18:45"
            val isha = "20:15"

            val times =
                PrayerTimes(
                    fajr = fajr,
                    sunrise = sunrise,
                    dhuhr = dhuhr,
                    asr = asr,
                    maghrib = maghrib,
                    isha = isha,
                    dateHijri = "15 Shawwal 1447", // Simulated
                    dateGregorian = todayGregorian,
                    meta = meta,
                )

            Resource.Success(times)
        } catch (e: Exception) {
            val error = ErrorHandler.handle(e)
            Resource.Error(error.userMessage, error)
        }

    override fun getPrayerSettings(): Flow<PrayerSettings> = settingsFlow

    override suspend fun updateSettings(settings: PrayerSettings) {
        settingsFlow.value = settings
    }
}
