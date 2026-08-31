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
            delay(1000) // Simulate network/db load

            // Mock data - adhering to rule: NO fake religious data, using placeholders
            _state.value =
                _state.value.copy(
                    isLoading = false,
                    lastRead = LastReadItem("سورة الكهف", "الآية ١٠", "kahf_10"),
                    lastListened = LastListenedItem("سورة البقرة", "عبد الباسط عبد الصمد", "baqarah_audio"),
                    shortcuts =
                        listOf(
                            ShortcutItem("adhkar", "الأذكار", "book"),
                            ShortcutItem("prayer_times", "مواقيت الصلاة", "schedule"),
                            ShortcutItem("qibla", "القبلة", "explore"),
                            ShortcutItem("hijri", "التقويم الهجري", "calendar_today"),
                        ),
                    sections =
                        listOf(
                            MihrabSection("quran", "القرآن الكريم", "menu_book"),
                            MihrabSection("recitations", "التلاوات", "headset"),
                            MihrabSection("tafsir", "التفاسير", "library_books"),
                            MihrabSection("saved_ayahs", "آيات محفوظة", "bookmark"),
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
