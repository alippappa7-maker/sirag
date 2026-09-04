package com.siraj.app.features.tafsir.presentation

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.data.repository.tafsir.FirebaseTafsirRepositoryImpl
import com.siraj.app.domain.models.tafsir.TafsirEdition
import com.siraj.app.domain.models.tafsir.TafsirSurah
import com.siraj.app.domain.models.tafsir.TafsirVerse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TafsirUiState(
    val editions: List<TafsirEdition> = emptyList(),
    val selectedEdition: TafsirEdition? = null,
    val surahs: List<TafsirSurah> = emptyList(),
    val selectedSurah: TafsirSurah? = null,
    val verses: List<TafsirVerse> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<TafsirVerse> = emptyList(),
    val isSearching: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class TafsirViewModel(
    application: Application,
) : ViewModel() {
    private val repository = FirebaseTafsirRepositoryImpl()
    private val _uiState = MutableStateFlow(TafsirUiState())
    val uiState: StateFlow<TafsirUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val editions = repository.getEditions()
                val surahs = repository.getSurahs()
                _uiState.value = _uiState.value.copy(
                    editions = editions,
                    selectedEdition = editions.firstOrNull(),
                    surahs = surahs,
                    isLoading = false,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "خطأ غير معروف")
            }
        }
    }

    fun selectEdition(edition: TafsirEdition) {
        _uiState.value = _uiState.value.copy(selectedEdition = edition)
    }

    fun selectSurah(surah: TafsirSurah) {
        _uiState.value = _uiState.value.copy(selectedSurah = surah, isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val editionId = _uiState.value.selectedEdition?.id ?: "ibn_kathir"
                val verses = repository.getTafsirBySurah(surah.number, editionId)
                _uiState.value = _uiState.value.copy(verses = verses, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "خطأ غير معروف")
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun search() {
        val query = _uiState.value.searchQuery.trim()
        if (query.isEmpty()) return
        _uiState.value = _uiState.value.copy(isSearching = true)
        viewModelScope.launch {
            try {
                val editionId = _uiState.value.selectedEdition?.id
                val results = repository.searchTafsir(query, editionId)
                _uiState.value = _uiState.value.copy(searchResults = results, isSearching = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSearching = false, error = e.message)
            }
        }
    }

    fun retry() = loadInitialData()

    companion object {
        fun factory(application: Application) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = TafsirViewModel(application) as T
        }
    }
}
