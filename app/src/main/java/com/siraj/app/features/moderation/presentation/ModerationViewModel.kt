package com.siraj.app.features.moderation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.community.Report
import com.siraj.app.domain.repository.community.SafetyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ModerationStateUI(
    val isLoading: Boolean = false,
    val reports: List<Report> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

class ModerationViewModel(
    private val safetyRepository: SafetyRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ModerationStateUI())
    val state: StateFlow<ModerationStateUI> = _state.asStateFlow()

    fun loadReports(role: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val res = safetyRepository.getPendingReports(role)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(isLoading = false, reports = res.data ?: emptyList())
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = res.message)
                }
                else -> {}
            }
        }
    }

    fun resolveReport(reportId: String, resolverId: String, resolution: String, notes: String, role: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            when (val res = safetyRepository.resolveReport(reportId, resolverId, resolution, notes)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(successMessage = "تم حل البلاغ بنجاح")
                    loadReports(role)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = res.message)
                }
                else -> {}
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(error = null, successMessage = null)
    }
}

class ModerationViewModelFactory(
    private val repository: SafetyRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ModerationViewModel(repository) as T
    }
}
