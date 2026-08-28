package com.siraj.app.features.mihrab.calendar.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.Locale

data class HijriEvent(
    val title: String,
    val dateHijri: String,
    val isUpcoming: Boolean = false,
    val source: String = "الحساب الفلكي المعتمد"
)

data class CalendarState(
    val currentGregorianDate: String = "",
    val currentHijriDate: String = "",
    val dayAdjustment: Int = 0,
    val events: List<HijriEvent> = emptyList()
)

class HijriCalendarViewModel : ViewModel() {

    private val _state = MutableStateFlow(CalendarState())
    val state = _state.asStateFlow()
    
    private val hijriMonths = arrayOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    init {
        updateDates()
    }

    fun setDayAdjustment(adjustment: Int) {
        _state.value = _state.value.copy(dayAdjustment = adjustment)
        updateDates()
    }

    private fun updateDates() {
        val today = LocalDate.now().plusDays(_state.value.dayAdjustment.toLong())
        
        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("ar"))
        val gregStr = today.format(formatter)
        
        val hijriDate = HijrahDate.from(today)
        val day = hijriDate.get(ChronoField.DAY_OF_MONTH)
        val month = hijriDate.get(ChronoField.MONTH_OF_YEAR)
        val year = hijriDate.get(ChronoField.YEAR)
        
        val monthName = hijriMonths[month - 1]
        val hijriStr = "$day $monthName $year هـ"
        
        val events = generateEvents(hijriDate)
        
        _state.value = _state.value.copy(
            currentGregorianDate = gregStr,
            currentHijriDate = hijriStr,
            events = events
        )
    }
    
    private fun generateEvents(currentHijriDate: HijrahDate): List<HijriEvent> {
        val year = currentHijriDate.get(ChronoField.YEAR)
        
        // Mock events for the year
        val allEvents = listOf(
            Pair(1, 1) to "رأس السنة الهجرية",
            Pair(1, 10) to "يوم عاشوراء",
            Pair(9, 1) to "بداية شهر رمضان",
            Pair(10, 1) to "عيد الفطر المبارك",
            Pair(12, 9) to "يوم عرفة",
            Pair(12, 10) to "عيد الأضحى المبارك"
        )
        
        val list = mutableListOf<HijriEvent>()
        
        val currentMonth = currentHijriDate.get(ChronoField.MONTH_OF_YEAR)
        val currentDay = currentHijriDate.get(ChronoField.DAY_OF_MONTH)
        
        for ((datePair, title) in allEvents) {
            val (m, d) = datePair
            val isUpcoming = (m > currentMonth) || (m == currentMonth && d >= currentDay)
            
            list.add(
                HijriEvent(
                    title = title,
                    dateHijri = "$d ${hijriMonths[m-1]}",
                    isUpcoming = isUpcoming
                )
            )
        }
        
        // Sort by upcoming first, then historical
        return list.sortedBy { if (it.isUpcoming) 0 else 1 }
    }
}
