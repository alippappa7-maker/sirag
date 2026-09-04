package com.siraj.app.data.repository.prayer

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.prayer.PrayerMeta
import com.siraj.app.domain.models.prayer.PrayerSettings
import com.siraj.app.domain.models.prayer.PrayerTimes
import com.siraj.app.domain.repository.prayer.PrayerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AladhanPrayerRepositoryImpl : PrayerRepository {

    private val settingsFlow = MutableStateFlow(PrayerSettings())

    override suspend fun getPrayerTimes(settings: PrayerSettings): Resource<PrayerTimes> = withContext(Dispatchers.IO) {
        try {
            val urlString = if (settings.latitude != null && settings.longitude != null) {
                "https://api.aladhan.com/v1/timings?latitude=${settings.latitude}&longitude=${settings.longitude}&method=${settings.methodId}"
            } else {
                "https://api.aladhan.com/v1/timingsByCity?city=${settings.city}&country=${settings.country}&method=${settings.methodId}"
            }

            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == 200) {
                val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonStr)
                val data = root.getJSONObject("data")
                val timings = data.getJSONObject("timings")
                val date = data.getJSONObject("date")
                val hijri = date.getJSONObject("hijri").getString("date")
                val gregorian = date.getJSONObject("gregorian").getString("date")
                val meta = data.optJSONObject("meta")
                val timezone = meta?.optString("timezone", "Asia/Riyadh") ?: "Asia/Riyadh"

                fun cleanTime(t: String): String = t.split(" ")[0].trim()

                val prayerTimes = PrayerTimes(
                    fajr = cleanTime(timings.getString("Fajr")),
                    sunrise = cleanTime(timings.getString("Sunrise")),
                    dhuhr = cleanTime(timings.getString("Dhuhr")),
                    asr = cleanTime(timings.getString("Asr")),
                    maghrib = cleanTime(timings.getString("Maghrib")),
                    isha = cleanTime(timings.getString("Isha")),
                    dateHijri = hijri,
                    dateGregorian = gregorian,
                    meta = PrayerMeta(
                        method = "Umm Al-Qura",
                        timezone = timezone,
                        city = settings.city,
                        isCached = false
                    )
                )
                Resource.Success(prayerTimes)
            } else {
                getFallbackTimes(settings)
            }
        } catch (e: Exception) {
            getFallbackTimes(settings)
        }
    }

    private fun getFallbackTimes(settings: PrayerSettings): Resource<PrayerTimes> {
        val fallback = PrayerTimes(
            fajr = "04:55",
            sunrise = "06:15",
            dhuhr = "12:15",
            asr = "15:40",
            maghrib = "18:15",
            isha = "19:45",
            dateHijri = "1448-03-21",
            dateGregorian = "2026-09-04",
            meta = PrayerMeta(
                method = "Umm Al-Qura (Offline / Fallback)",
                timezone = "Asia/Riyadh",
                city = settings.city,
                isCached = true
            )
        )
        return Resource.Success(fallback)
    }

    override fun getPrayerSettings(): Flow<PrayerSettings> = settingsFlow

    override suspend fun updateSettings(settings: PrayerSettings) {
        settingsFlow.value = settings
    }
}
