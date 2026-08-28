package com.siraj.app.features.audio.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.audio.AudioController
import com.siraj.app.core.utils.Resource
import com.siraj.app.data.repository.audio.AudioRepositoryImpl
import com.siraj.app.domain.models.audio.AudioFilter
import com.siraj.app.domain.models.audio.AudioSortOption
import com.siraj.app.domain.models.audio.AudioTrack
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AudioScreenState(
    val selectedCategory: String = "all",
    val searchQuery: String = "",
    val sortOption: AudioSortOption = AudioSortOption.NEWEST,
    val tracksResource: Resource<List<AudioTrack>> = Resource.Loading
)

class AudioViewModel : ViewModel() {
    private val repository = AudioRepositoryImpl()

    private val _state = MutableStateFlow(AudioScreenState())
    val state = _state.asStateFlow()

    private var searchJob: Job? = null

    val categories = listOf(
        "all" to "أحدث الإضافات",
        "recitation" to "تلاوات",
        "lesson" to "دروس",
        "lecture" to "محاضرات",
        "podcast" to "بودكاست",
        "favorites" to "المفضلة",
        "downloads" to "التنزيلات"
    )

    init {
        loadTracks()
    }

    fun onCategorySelected(categoryId: String) {
        if (_state.value.selectedCategory == categoryId) return
        _state.value = _state.value.copy(selectedCategory = categoryId)
        loadTracks()
    }

    fun onSearchQueryChanged(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500) // debounce
            loadTracks()
        }
    }

    fun onSortOptionChanged(sortOption: AudioSortOption) {
        _state.value = _state.value.copy(sortOption = sortOption)
        loadTracks()
    }

    private fun loadTracks() {
        viewModelScope.launch {
            _state.value = _state.value.copy(tracksResource = Resource.Loading)
            val filter = AudioFilter(
                query = _state.value.searchQuery,
                categoryId = _state.value.selectedCategory,
                sortOption = _state.value.sortOption
            )
            val result = repository.getTracks(filter)
            _state.value = _state.value.copy(tracksResource = result)
        }
    }

    fun toggleFavorite(trackId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(trackId)
            loadTracks() 
        }
    }

    fun playTrack(track: AudioTrack) {
        AudioController.playTrack(track)
    }
    
    fun togglePlayPause() {
        AudioController.togglePlayPause()
    }
    
    fun reportTrack(trackId: String) {
        viewModelScope.launch {
            repository.reportTrack(trackId, "User reported")
        }
    }
}
