package com.siraj.app.features.share.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.share.ShareLink
import com.siraj.app.domain.repository.share.ShareRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SharedContentState {
    object Loading : SharedContentState()
    data class Success(val shareLink: ShareLink) : SharedContentState()
    data class Error(val message: String) : SharedContentState()
}

class SharedContentViewModel(
    private val repository: ShareRepository
) : ViewModel() {

    private val _state = MutableStateFlow<SharedContentState>(SharedContentState.Loading)
    val state: StateFlow<SharedContentState> = _state.asStateFlow()

    fun validateLink(linkId: String, token: String?) {
        viewModelScope.launch {
            _state.value = SharedContentState.Loading
            when (val result = repository.getAndValidateShareLink(linkId, token)) {
                is Resource.Success -> {
                    // Log view asynchronously
                    launch { repository.logAnonymousView(linkId) }
                    _state.value = SharedContentState.Success(result.data)
                }
                is Resource.Error -> {
                    _state.value = SharedContentState.Error(result.message ?: "حدث خطأ غير معروف")
                }
                else -> {}
            }
        }
    }
}

class SharedContentViewModelFactory(
    private val repository: ShareRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SharedContentViewModel::class.java)) {
            return SharedContentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
