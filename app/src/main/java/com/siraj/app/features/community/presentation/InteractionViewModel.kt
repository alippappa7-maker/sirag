package com.siraj.app.features.community.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.repository.community.InteractionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InteractionState(
    val message: String? = null
)

class InteractionViewModel(
    private val interactionRepository: InteractionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(InteractionState())
    val state: StateFlow<InteractionState> = _state.asStateFlow()

    // These don't have complex UI state, we just update the specific item locally or rely on reactive streams
    // For MVP, we just execute the action and return success message

    fun toggleLike(userId: String, targetId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (userId.isBlank()) return@launch
            when (val res = interactionRepository.toggleLike(userId, targetId)) {
                is Resource.Success -> onResult(res.data ?: false)
                else -> {}
            }
        }
    }

    fun toggleSave(userId: String, targetId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (userId.isBlank()) return@launch
            when (val res = interactionRepository.toggleSave(userId, targetId)) {
                is Resource.Success -> onResult(res.data ?: false)
                else -> {}
            }
        }
    }

    fun toggleFollow(userId: String, targetUserId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (userId.isBlank()) return@launch
            when (val res = interactionRepository.toggleFollow(userId, targetUserId)) {
                is Resource.Success -> {
                    val isFollowing = res.data ?: false
                    _state.value = InteractionState(message = if (isFollowing) "تمت المتابعة" else "تم إلغاء المتابعة")
                    onResult(isFollowing)
                }
                else -> {}
            }
        }
    }

    fun blockUser(userId: String, blockedUserId: String) {
        viewModelScope.launch {
            if (userId.isBlank()) return@launch
            interactionRepository.blockUser(userId, blockedUserId)
            _state.value = InteractionState(message = "تم حظر المستخدم. لن ترى محتواه بعد الآن.")
        }
    }

    fun hideContent(userId: String, contentId: String) {
        viewModelScope.launch {
            if (userId.isBlank()) return@launch
            interactionRepository.hideContent(userId, contentId)
            _state.value = InteractionState(message = "تم إخفاء المحتوى.")
        }
    }

    fun clearMessage() {
        _state.value = InteractionState(message = null)
    }
}

class InteractionViewModelFactory(
    private val repository: InteractionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return InteractionViewModel(repository) as T
    }
}
