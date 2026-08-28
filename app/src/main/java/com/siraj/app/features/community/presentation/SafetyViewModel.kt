package com.siraj.app.features.community.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.community.ReportTargetType
import com.siraj.app.domain.models.community.ReportType
import com.siraj.app.domain.repository.community.SafetyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SafetyStateUI(
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class SafetyViewModel(
    private val safetyRepository: SafetyRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SafetyStateUI())
    val state: StateFlow<SafetyStateUI> = _state.asStateFlow()

    fun submitReport(
        reporterId: String,
        targetType: ReportTargetType,
        targetId: String,
        targetOwnerId: String,
        reportType: ReportType,
        description: String
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true, error = null, successMessage = null)
            val result = safetyRepository.submitReport(
                reporterId, targetType, targetId, targetOwnerId, reportType, description
            )
            when (result) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        successMessage = "تم إرسال البلاغ بنجاح. سيتم مراجعته في أقرب وقت."
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(isSubmitting = false, error = result.message)
                }
                else -> {}
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(error = null, successMessage = null)
    }
}

class SafetyViewModelFactory(
    private val repository: SafetyRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SafetyViewModel(repository) as T
    }
}
