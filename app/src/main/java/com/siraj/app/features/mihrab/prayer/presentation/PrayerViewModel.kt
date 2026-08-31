package com.siraj.app.features.mihrab.prayer.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.data.repository.prayer.PrayerRepositoryImpl
import com.siraj.app.domain.models.prayer.PrayerSettings
import com.siraj.app.domain.models.prayer.PrayerTimes
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class NextPrayer(
    val name: String,
    val timeStr: String,
    val timeRemainingMs: Long
)

class PrayerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PrayerRepositoryImpl()

    private val _prayerTimes = MutableStateFlow<Resource<PrayerTimes>>(Resource.Loading)
    val prayerTimes = _prayerTimes.asStateFlow()

    private val _settings = MutableStateFlow(PrayerSettings())
    val settings = _settings.asStateFlow()

    private val _nextPrayer = MutableStateFlow<NextPrayer?>(null)
    val nextPrayer = _nextPrayer.asStateFlow()

    private val _currentTime = MutableStateFlow(System.currentTimeMillis())

    private val _locationError = MutableStateFlow<String?>(null)
    val locationError = _locationError.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getPrayerSettings().collect { newSettings ->
                _settings.value = newSettings
                fetchPrayerTimes(newSettings)
            }
        }

        viewModelScope.launch {
            while (true) {
                delay(1000)
                _currentTime.value = System.currentTimeMillis()
                updateNextPrayer()
            }
        }
    }

    private fun fetchPrayerTimes(settings: PrayerSettings) {
        viewModelScope.launch {
            _prayerTimes.value = Resource.Loading
            _prayerTimes.value = repository.getPrayerTimes(settings)
            updateNextPrayer()
        }
    }

    fun updateSettings(newSettings: PrayerSettings) {
        viewModelScope.launch {
            repository.updateSettings(newSettings)
        }
    }

    fun setLocationError(error: String?) {
        _locationError.value = error
    }

    fun requestLocationUpdate() {
        viewModelScope.launch {
            _prayerTimes.value = Resource.Loading
            delay(1000)
            val updatedSettings = _settings.value.copy(
                city = "Riyadh",
                country = "Saudi Arabia",
                useLocation = true
            )
            updateSettings(updatedSettings)
            _locationError.value = null
        }
    }

    private fun updateNextPrayer() {
        val resource = _prayerTimes.value
        if (resource is Resource.Success) {
            val times = resource.data
            val prayers = listOf(
                "الفجر" to times.fajr,
                "الشروق" to times.sunrise,
                "الظهر" to times.dhuhr,
                "العصر" to times.asr,
                "المغرب" to times.maghrib,
                "العشاء" to times.isha
            )

            val sdf = SimpleDateFormat("HH:mm", Locale.US)
            val now = Calendar.getInstance()

            var nextP: NextPrayer? = null

            for ((name, timeStr) in prayers) {
                val timeDate = sdf.parse(timeStr)
                if (timeDate != null) {
                    val timeCal = Calendar.getInstance()
                    timeCal.time = timeDate

                    val pCal = Calendar.getInstance()
                    pCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                    pCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                    pCal.set(Calendar.SECOND, 0)

                    if (pCal.timeInMillis > now.timeInMillis) {
                        nextP = NextPrayer(name, timeStr, pCal.timeInMillis - now.timeInMillis)
                        break
                    }
                }
            }

            if (nextP == null) {
                val fajrDate = sdf.parse(times.fajr)
                if (fajrDate != null) {
                    val timeCal = Calendar.getInstance()
                    timeCal.time = fajrDate

                    val pCal = Calendar.getInstance()
                    pCal.add(Calendar.DAY_OF_YEAR, 1)
                    pCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                    pCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                    pCal.set(Calendar.SECOND, 0)
                    nextP = NextPrayer("الفجر", times.fajr, pCal.timeInMillis - now.timeInMillis)
                }
            }

            _nextPrayer.value = nextP
        }
    }
}
