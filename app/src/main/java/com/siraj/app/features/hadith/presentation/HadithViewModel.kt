package com.siraj.app.features.hadith.presentation

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.data.repository.hadith.FirebaseHadithRepositoryImpl
import com.siraj.app.domain.models.hadith.Hadith
import com.siraj.app.domain.models.hadith.HadithCollection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HadithUiState(
    val collections: List<HadithCollection> = emptyList(),
    val selectedCollection: HadithCollection? = null,
    val hadiths: List<Hadith> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<Hadith> = emptyList(),
    val isSearching: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class HadithViewModel(
    application: Application,
) : ViewModel() {
    private val repository = FirebaseHadithRepositoryImpl()
    private val _uiState = MutableStateFlow(HadithUiState())
    val uiState: StateFlow<HadithUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val collections = repository.getCollections()
                _uiState.value = _uiState.value.copy(
                    collections = collections,
                    selectedCollection = collections.firstOrNull(),
                    isLoading = false,
                )
                collections.firstOrNull()?.let { selectCollection(it) }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun selectCollection(collection: HadithCollection) {
        _uiState.value = _uiState.value.copy(selectedCollection = collection, isLoading = true)
        viewModelScope.launch {
            try {
                val hadiths = repository.getHadithsByCollection(collection.id)
                _uiState.value = _uiState.value.copy(hadiths = hadiths, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
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
                val collectionId = _uiState.value.selectedCollection?.id
                val results = repository.searchHadiths(query, collectionId)
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
            override fun <T : ViewModel> create(modelClass: Class<T>): T = HadithViewModel(application) as T
        }
    }
}
