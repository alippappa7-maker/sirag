package com.siraj.app.features.flashes.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.flash.Flash
import com.siraj.app.domain.models.flash.FlashAuditLog
import com.siraj.app.domain.repository.flash.FlashPublishingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class FlashPublishingStateUI(
    val isLoading: Boolean = false,
    val currentFlash: Flash? = null,
    val error: String? = null,
    val successMessage: String? = null,
    val auditLogs: List<FlashAuditLog> = emptyList(),
)

class FlashPublishingViewModel(
    private val repository: FlashPublishingRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(FlashPublishingStateUI())
    val state: StateFlow<FlashPublishingStateUI> = _state.asStateFlow()

    fun createDraft(
        creatorId: String,
        creatorName: String,
        workspaceId: String,
        videoFile: File?,
        assetId: String?,
        durationMs: Long,
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, successMessage = null)
            when (val result = repository.createDraft(creatorId, creatorName, workspaceId, videoFile, assetId, durationMs)) {
                is Resource.Success ->
                    _state.value =
                        _state.value.copy(isLoading = false, currentFlash = result.data, successMessage = "تم إنشاء المسودة")
                is Resource.Error -> _state.value = _state.value.copy(isLoading = false, error = result.message)
                else -> {}
            }
        }
    }

    fun updateDetails(
        flashId: String,
        title: String,
        description: String,
        category: String,
        tags: List<String>,
        visibility: String,
        showCreatorInfo: Boolean,
        sourceIds: List<String>,
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, successMessage = null)
            when (
                val result =
                    repository.updateFlashDetails(
                        flashId,
                        title,
                        description,
                        category,
                        tags,
                        visibility,
                        showCreatorInfo,
                        sourceIds,
                    )
            ) {
                is Resource.Success ->
                    _state.value =
                        _state.value.copy(isLoading = false, currentFlash = result.data, successMessage = "تم تحديث التفاصيل بنجاح")
                is Resource.Error -> _state.value = _state.value.copy(isLoading = false, error = result.message)
                else -> {}
            }
        }
    }

    fun submitForReview(
        flashId: String,
        userId: String,
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, successMessage = null)
            // 1. Run Automated Checks
            val checkResult = repository.runAutomatedChecks(flashId)
            if (checkResult is Resource.Error) {
                _state.value = _state.value.copy(isLoading = false, error = "فشل الفحص التلقائي: ${checkResult.message}")
                return@launch
            }

            // 2. Submit
            when (val result = repository.submitForReview(flashId, userId)) {
                is Resource.Success ->
                    _state.value =
                        _state.value.copy(isLoading = false, currentFlash = result.data, successMessage = "تم الإرسال للمراجعة")
                is Resource.Error -> _state.value = _state.value.copy(isLoading = false, error = result.message)
                else -> {}
            }
        }
    }

    fun publish(
        flashId: String,
        userId: String,
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, successMessage = null)
            when (val result = repository.publishFlash(flashId, userId)) {
                is Resource.Success ->
                    _state.value =
                        _state.value.copy(isLoading = false, currentFlash = result.data, successMessage = "تم النشر بنجاح")
                is Resource.Error -> _state.value = _state.value.copy(isLoading = false, error = result.message)
                else -> {}
            }
        }
    }

    fun approveAsReviewer(
        flashId: String,
        reviewerId: String,
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            when (val result = repository.approveFlash(flashId, reviewerId)) {
                is Resource.Success ->
                    _state.value =
                        _state.value.copy(isLoading = false, currentFlash = result.data, successMessage = "تم الاعتماد الشرعي بنجاح")
                is Resource.Error -> _state.value = _state.value.copy(isLoading = false, error = result.message)
                else -> {}
            }
        }
    }

    fun loadAuditLogs(flashId: String) {
        viewModelScope.launch {
            when (val result = repository.getAuditLogs(flashId)) {
                is Resource.Success -> _state.value = _state.value.copy(auditLogs = result.data ?: emptyList())
                else -> {}
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(error = null, successMessage = null)
    }
}

class FlashPublishingViewModelFactory(
    private val repository: FlashPublishingRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FlashPublishingViewModel::class.java)) {
            return FlashPublishingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
