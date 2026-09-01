package com.siraj.app.features.mihrab.prayer.presentation

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.domain.models.prayer.PrayerName
import com.siraj.app.domain.models.prayer.PrayerStatus
import com.siraj.app.domain.models.prayer.PrayerDayRecord
import com.siraj.app.domain.models.prayer.PrayerStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

data class PrayerTrackingUiState(
    val todayRecord: PrayerDayRecord = createEmptyDay(),
    val stats: PrayerStats = PrayerStats(0, 0, 0, 0, 0f, 0f, 0f, emptyList()),
    val isLoading: Boolean = false,
)

private fun createEmptyDay(): PrayerDayRecord {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val prayers = PrayerName.entries.associateWith { PrayerStatus.PENDING }
    return PrayerDayRecord(today, prayers, 0, 0, 5)
}

class PrayerTrackingViewModel(
    application: Application,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PrayerTrackingUiState())
    val uiState: StateFlow<PrayerTrackingUiState> = _uiState.asStateFlow()

    fun markPrayer(prayerName: PrayerName, status: PrayerStatus) {
        val current = _uiState.value.todayRecord
        val newPrayers = current.prayers.toMutableMap()
        newPrayers[prayerName] = status

        val completedCount = newPrayers.count { it.value == PrayerStatus.PRAYED }
        val missedCount = newPrayers.count { it.value == PrayerStatus.MISSED }
        val pendingCount = newPrayers.count { it.value == PrayerStatus.PENDING }

        _uiState.value = _uiState.value.copy(
            todayRecord = current.copy(
                prayers = newPrayers,
                completedCount = completedCount,
                missedCount = missedCount,
                pendingCount = pendingCount,
            )
        )
    }

    fun getStats(): PrayerStats {
        return _uiState.value.stats
    }

    companion object {
        fun factory(application: Application) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = PrayerTrackingViewModel(application) as T
        }
    }
}
