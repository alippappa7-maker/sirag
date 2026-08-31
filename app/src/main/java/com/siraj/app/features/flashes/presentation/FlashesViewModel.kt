package com.siraj.app.features.flashes.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.flash.Flash
import com.siraj.app.domain.repository.flash.FlashRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FlashesFeedState(
    val isLoading: Boolean = false,
    val flashes: List<Flash> = emptyList(),
    val error: String? = null,
    val isOffline: Boolean = false,
    val hasMore: Boolean = true,
    val isPaginating: Boolean = false,
    val isMuted: Boolean = false,
)

class FlashesViewModel(
    private val repository: FlashRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(FlashesFeedState())
    val state: StateFlow<FlashesFeedState> = _state.asStateFlow()

    private var pageToken: String? = null

    // Track viewed flashes to prevent duplicates in current session
    private val viewedIds = mutableSetOf<String>()

    init {
        loadFlashes()
    }

    fun loadFlashes(isRefresh: Boolean = false) {
        if (isRefresh) {
            pageToken = null
            viewedIds.clear()
        } else if (_state.value.isLoading || _state.value.isPaginating || !_state.value.hasMore) {
            return
        }

        viewModelScope.launch {
            if (pageToken == null) {
                _state.value = _state.value.copy(isLoading = true, error = null)
            } else {
                _state.value = _state.value.copy(isPaginating = true)
            }

            when (val result = repository.getFlashesFeed(pageToken)) {
                is Resource.Success -> {
                    val newFlashes = result.data?.flashes?.filter { !viewedIds.contains(it.id) } ?: emptyList()
                    viewedIds.addAll(newFlashes.map { it.id })

                    _state.value =
                        _state.value.copy(
                            isLoading = false,
                            isPaginating = false,
                            flashes = if (isRefresh) newFlashes else _state.value.flashes + newFlashes,
                            hasMore = result.data?.hasMore == true,
                            isOffline = false,
                        )
                    pageToken = result.data?.nextPageToken
                }
                is Resource.Error -> {
                    _state.value =
                        _state.value.copy(
                            isLoading = false,
                            isPaginating = false,
                            error = result.message ?: "حدث خطأ غير متوقع",
                            // Simple offline check mock
                            isOffline = result.message?.contains("network", ignoreCase = true) == true,
                        )
                }
                else -> {}
            }
        }
    }

    fun toggleMute() {
        _state.value = _state.value.copy(isMuted = !_state.value.isMuted)
    }

    fun toggleLike(flashId: String) {
        viewModelScope.launch {
            // Optimistic update
            val currentFlashes = _state.value.flashes.toMutableList()
            val index = currentFlashes.indexOfFirst { it.id == flashId }
            if (index != -1) {
                val currentFlash = currentFlashes[index]
                currentFlashes[index] =
                    currentFlash.copy(
                        isLikedByMe = !currentFlash.isLikedByMe,
                        metrics =
                            currentFlash.metrics.copy(
                                likes = if (currentFlash.isLikedByMe) currentFlash.metrics.likes - 1 else currentFlash.metrics.likes + 1,
                            ),
                    )
                _state.value = _state.value.copy(flashes = currentFlashes)
            }

            repository.toggleLike(flashId)
        }
    }

    fun toggleSave(flashId: String) {
        viewModelScope.launch {
            val currentFlashes = _state.value.flashes.toMutableList()
            val index = currentFlashes.indexOfFirst { it.id == flashId }
            if (index != -1) {
                val currentFlash = currentFlashes[index]
                currentFlashes[index] =
                    currentFlash.copy(
                        isSavedByMe = !currentFlash.isSavedByMe,
                        metrics =
                            currentFlash.metrics.copy(
                                saves = if (currentFlash.isSavedByMe) currentFlash.metrics.saves - 1 else currentFlash.metrics.saves + 1,
                            ),
                    )
                _state.value = _state.value.copy(flashes = currentFlashes)
            }

            repository.toggleSave(flashId)
        }
    }

    fun logView(flashId: String) {
        viewModelScope.launch {
            repository.logView(flashId)
        }
    }
}

class FlashesViewModelFactory(
    private val repository: FlashRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FlashesViewModel::class.java)) {
            return FlashesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
