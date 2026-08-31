package com.siraj.app.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.data.repository.FirebaseAuthRepositoryImpl
import com.siraj.app.domain.models.UserPreferences
import com.siraj.app.domain.models.UserProfile
import com.siraj.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val saveMessage: String? = null,
)

class SettingsViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepositoryImpl(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { profile ->
                _uiState.update { it.copy(profile = profile) }
            }
        }
    }

    fun updatePreferences(update: (UserPreferences) -> UserPreferences) {
        val currentProfile = _uiState.value.profile ?: return
        val newPrefs = update(currentProfile.preferences)

        // Optimistic update locally
        _uiState.update {
            it.copy(profile = it.profile?.copy(preferences = newPrefs))
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, saveMessage = null) }
            val result = authRepository.updatePreferences(newPrefs)
            if (result is Resource.Success) {
                _uiState.update { it.copy(isLoading = false, saveMessage = "تم حفظ الإعدادات") }
            } else if (result is Resource.Error) {
                _uiState.update { it.copy(isLoading = false, error = result.message) }
                // Revert is handled by the next snapshot from Firestore
            }
        }
    }

    fun showMessage(message: String) {
        _uiState.update { it.copy(saveMessage = message) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(saveMessage = null, error = null) }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.logout()
            _uiState.update { it.copy(isLoading = false) }
            onSuccess()
        }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = authRepository.deleteAccount()
            if (result is Resource.Success) {
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            } else if (result is Resource.Error) {
                _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }
}

class SettingsViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
