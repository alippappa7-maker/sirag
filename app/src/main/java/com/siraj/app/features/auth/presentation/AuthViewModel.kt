package com.siraj.app.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.data.repository.FirebaseAuthRepositoryImpl
import com.siraj.app.domain.models.UserProfile
import com.siraj.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepositoryImpl()
) : ViewModel() {

    private val _authState = MutableStateFlow<Resource<UserProfile?>>(Resource.Loading)
    val authState: StateFlow<Resource<UserProfile?>> = _authState

    private val _actionState = MutableStateFlow<Resource<Unit>?>(null)
    val actionState: StateFlow<Resource<Unit>?> = _actionState

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _authState.value = Resource.Success(user)
            }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            _actionState.value = authRepository.login(email, pass)
        }
    }

    fun register(name: String, email: String, pass: String) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            _actionState.value = authRepository.register(name, email, pass)
        }
    }

    fun logout() {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            _actionState.value = authRepository.logout()
        }
    }

    fun updateProfile(name: String, avatarUrl: String? = null) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            _actionState.value = authRepository.updateProfile(name, avatarUrl)
        }
    }

    fun resetActionState() {
        _actionState.value = null
    }
}

class AuthViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
