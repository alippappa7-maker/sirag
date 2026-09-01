package com.siraj.app.features.mihrab.ramadan.presentation

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.domain.models.ramadan.RamadanDua
import com.siraj.app.domain.models.ramadan.RamadanDayInfo
import com.siraj.app.domain.models.ramadan.RamadanPhase
import com.siraj.app.domain.models.ramadan.RamadanStats
import com.siraj.app.domain.models.ramadan.LailatulQadrInfo
import com.siraj.app.domain.models.ramadan.RamadanInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RamadanUiState(
    val info: RamadanInfo = createDefaultInfo(),
    val currentDay: Int = 1,
    val fastingProgress: Float = 0f,
    val isLoading: Boolean = false,
)

private fun createDefaultInfo(): RamadanInfo {
    val days = (1..30).map { day ->
        RamadanDayInfo(
            dayNumber = day,
            hijriDate = "رمضان $day",
            phase = when {
                day <= 10 -> RamadanPhase.FIRST_TEN
                day <= 20 -> RamadanPhase.SECOND_TEN
                else -> RamadanPhase.LAST_TEN
            },
            fajrTime = null,
            suhoorTime = null,
            iftarTime = null,
            ishaTime = null,
            taraweehTime = null,
        )
    }

    val duas = listOf(
        RamadanDua(
            id = "suhoor",
            title = "دعاء السحور",
            arabicText = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ اللَّهُمَّ إِنِّي أَصُومُ غَدًا لِوَجْهِكَ فَاغْفِرْ لِي مَا قَدَّمْتُ وَمَا أَخَّرْتُ",
            translation = "اللهم إني أصوم غداً لوجهك فاغفر لي ما قدمت وما أخرت",
            source = "أبو داود",
            occasion = "السحور",
        ),
        RamadanDua(
            id = "iftar",
            title = "دعاء الإفطار",
            arabicText = "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ وَثَبَتَ الْأَجْرُ إِنْ شَاءَ اللَّهُ",
            translation = "ذهب الظمأ وابتلت العروق وثبت الأجر إن شاء الله",
            source = "أبو داود",
            occasion = "الإفطار",
        ),
        RamadanDua(
            id = "lailatul_qadr",
            title = "دعاء ليلة القدر",
            arabicText = "اللَّهُمَّ إِنَّكَ عَفُوٌّ كَرِيمٌ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي",
            translation = "اللهم إنك عفو كريم تحب العفو فاعف عني",
            source = "الترمذي",
            occasion = "ليلة القدر",
        ),
    )

    return RamadanInfo(
        days = days,
        stats = RamadanStats(fastingDays = 0, totalDays = 30, missedDays = 0, currentStreak = 0, bestStreak = 0),
        lailatulQadr = LailatulQadrInfo(
            isLastTenDays = true,
            oddNights = listOf("ليلة 21", "ليلة 23", "ليلة 25", "ليلة 27", "ليلة 29"),
            recommendedNights = listOf("ليلة 27"),
        ),
        duas = duas,
        startDate = null,
        endDate = null,
    )
}

class RamadanViewModel(
    application: Application,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RamadanUiState())
    val uiState: StateFlow<RamadanUiState> = _uiState.asStateFlow()

    fun setCurrentDay(day: Int) {
        _uiState.value = _uiState.value.copy(
            currentDay = day,
            fastingProgress = day / 30f,
        )
    }

    companion object {
        fun factory(application: Application) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = RamadanViewModel(application) as T
        }
    }
}
