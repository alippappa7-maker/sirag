package com.siraj.app.features.mihrab.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MihrabState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val isOffline: Boolean = false,
    val searchQuery: String = "",
    val lastRead: LastReadItem? = null,
    val lastListened: LastListenedItem? = null,
    val shortcuts: List<ShortcutItem> = emptyList(),
    val sections: List<MihrabSection> = emptyList(),
)

data class LastReadItem(
    val title: String, // e.g. سورة الكهف
    val subtitle: String, // e.g. الآية ١٠
    val id: String,
)

data class LastListenedItem(
    val title: String,
    val reciter: String,
    val id: String,
)

data class ShortcutItem(
    val id: String,
    val title: String,
    val iconName: String,
)

data class MihrabSection(
    val id: String,
    val title: String,
    val iconName: String,
)

class MihrabViewModel : ViewModel() {
    private val _state = MutableStateFlow(MihrabState())
    val state: StateFlow<MihrabState> = _state.asStateFlow()

    init {
        loadMihrabData()
    }

    private fun loadMihrabData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            _state.value =
                _state.value.copy(
                    isLoading = false,
                    lastRead = LastReadItem("سورة الكهف", "الآية ١٠", "kahf_10"),
                    lastListened = LastListenedItem("سورة البقرة", "عبد الباسط عبد الصمد", "baqarah_audio"),
                    shortcuts =
                        listOf(
                            ShortcutItem("copilot", "المساعد الذكي", "auto_awesome"),
                            ShortcutItem("qibla", "اتجاه القبلة", "explore"),
                            ShortcutItem("prayer_intelligence", "ذكاء الصلاة", "insights"),
                            ShortcutItem("dashboard", "لوحة التحكم", "dashboard"),
                            ShortcutItem("adhkar", "الأذكار", "book"),
                            ShortcutItem("prayer_times", "مواقيت الصلاة", "schedule"),
                            ShortcutItem("tasbih", "المسبحة", "touch_app"),
                            ShortcutItem("zakat", "الزكاة", "calculate"),
                            ShortcutItem("ramadan", "رمضان", "nightlight"),
                            ShortcutItem("prayer_tracking", "تتبع الصلوات", "check_circle"),
                        ),
                    sections =
                        listOf(
                            MihrabSection("quran", "القرآن الكريم", "menu_book"),
                            MihrabSection("tafsir", "التفسير", "library_books"),
                            MihrabSection("hadith", "الحديث النبوي", "auto_stories"),
                            MihrabSection("copilot", "المساعد الذكي", "auto_awesome"),
                            MihrabSection("prayer_intelligence", "ذكاء الصلاة", "insights"),
                            MihrabSection("dashboard", "لوحة التحكم", "dashboard"),
                            MihrabSection("recitations", "التلاوات", "headset"),
                            MihrabSection("zakat", "حاسبة الزكاة", "calculate"),
                            MihrabSection("tasbih", "المسبحة الرقمية", "touch_app"),
                            MihrabSection("ramadan", "وضع رمضان", "nightlight"),
                            MihrabSection("prayer_tracking", "تتبع الصلوات", "check_circle"),
                        ),
                )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    fun retry() {
        loadMihrabData()
    }
}
