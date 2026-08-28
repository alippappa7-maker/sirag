package com.siraj.app.features.quran.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.data.api.RetrofitClient
import com.siraj.app.data.local.QuranDatabase
import com.siraj.app.data.repository.QuranRepositoryImpl
import com.siraj.app.domain.models.quran.Ayah
import com.siraj.app.domain.models.quran.QuranReaderSettings
import com.siraj.app.domain.models.quran.Surah
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class QuranViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = QuranRepositoryImpl(
        RetrofitClient.quranApi,
        QuranDatabase.getDatabase(application).quranDao()
    )
    private val _surahs = MutableStateFlow<Resource<List<Surah>>>(Resource.Loading)
    val surahs: StateFlow<Resource<List<Surah>>> = _surahs.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    init {
        loadSurahs()
    }

    private fun loadSurahs() {
        viewModelScope.launch {
            _surahs.value = Resource.Loading
            _surahs.value = repository.getSurahs()
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}

class QuranReaderViewModel(application: Application, private val surahId: Int) : AndroidViewModel(application) {
    private val repository = QuranRepositoryImpl(
        RetrofitClient.quranApi,
        QuranDatabase.getDatabase(application).quranDao()
    )
    private val _ayahs = MutableStateFlow<Resource<List<Ayah>>>(Resource.Loading)
    
    val ayahsWithBookmarks = combine(_ayahs, repository.getBookmarkedVerseKeys()) { result, bookmarks ->
        if (result is Resource.Success) {
            Resource.Success(result.data.map { it.copy(isBookmarked = bookmarks.contains(it.verseKey)) })
        } else {
            result
        }
    }

    private val _settings = MutableStateFlow(QuranReaderSettings())
    val settings = _settings.asStateFlow()

    init {
        loadAyahs()
    }

    private fun loadAyahs() {
        viewModelScope.launch {
            _ayahs.value = Resource.Loading
            _ayahs.value = repository.getAyahs(surahId)
        }
    }

    fun toggleBookmark(verseKey: String, verseNumber: Int, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.toggleBookmark(verseKey, surahId, verseNumber, !currentStatus)
        }
    }
    
    fun saveNote(verseKey: String, verseNumber: Int, note: String) {
        viewModelScope.launch {
            repository.saveNote(verseKey, surahId, verseNumber, note)
            loadAyahs()
        }
    }

    fun updateFontSize(newSize: Float) {
        _settings.value = _settings.value.copy(fontSize = newSize)
    }

    fun toggleNightMode(isNightMode: Boolean) {
        _settings.value = _settings.value.copy(isNightMode = isNightMode)
    }

    fun changeReciter(reciterId: Int) {
        _settings.value = _settings.value.copy(selectedReciterId = reciterId)
    }

    fun updatePlaybackSpeed(speed: Float) {
        _settings.value = _settings.value.copy(playbackSpeed = speed)
    }
}
