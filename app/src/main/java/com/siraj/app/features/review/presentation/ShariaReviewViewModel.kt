package com.siraj.app.features.review.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.review.*
import com.siraj.app.domain.repository.review.ShariaReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ShariaReviewUiState(
    val isLoading: Boolean = false,
    val queueItems: List<ShariaReviewItem> = emptyList(),
    val selectedItem: ShariaReviewItem? = null,
    val activeFilter: ShariaReviewFilter = ShariaReviewFilter(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isActionInProgress: Boolean = false
)

class ShariaReviewViewModel(
    private val repository: ShariaReviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShariaReviewUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadQueue(_uiState.value.activeFilter)
    }

    fun loadQueue(filter: ShariaReviewFilter = _uiState.value.activeFilter) {
        _uiState.value = _uiState.value.copy(isLoading = true, activeFilter = filter)
        viewModelScope.launch {
            repository.getReviewQueue(filter).collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            queueItems = result.data ?: emptyList()
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun loadItemDetails(itemId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getReviewItemById(itemId).collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            selectedItem = result.data
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun updateFilter(filter: ShariaReviewFilter) {
        _uiState.value = _uiState.value.copy(activeFilter = filter)
        loadQueue(filter)
    }

    fun claimReview(itemId: String, reviewerId: String, reviewerName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActionInProgress = true)
            when (val res = repository.claimReview(itemId, reviewerId, reviewerName)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isActionInProgress = false,
                        successMessage = "تم حجز المحتوى وبدء المراجعة الشرعية"
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isActionInProgress = false,
                        errorMessage = res.message
                    )
                }
                else -> Unit
            }
        }
    }

    fun releaseReview(itemId: String, reviewerId: String, reviewerName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActionInProgress = true)
            when (val res = repository.releaseReview(itemId, reviewerId, reviewerName)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isActionInProgress = false,
                        successMessage = "تم إلغاء الحجز وإعادة المحتوى للقائمة"
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isActionInProgress = false,
                        errorMessage = res.message
                    )
                }
                else -> Unit
            }
        }
    }

    fun approveItem(
        itemId: String,
        reviewerId: String,
        reviewerName: String,
        reason: String,
        scheduledReReviewDate: Long? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActionInProgress = true)
            when (val res = repository.approveItem(itemId, reviewerId, reviewerName, reason, scheduledReReviewDate)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isActionInProgress = false,
                        successMessage = "تم تسجيل قرار الاعتماد الشرعي بنجاح"
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isActionInProgress = false,
                        errorMessage = res.message
                    )
                }
                else -> Unit
            }
        }
    }

    fun rejectItem(itemId: String, reviewerId: String, reviewerName: String, reason: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActionInProgress = true)
            when (val res = repository.rejectItem(itemId, reviewerId, reviewerName, reason)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isActionInProgress = false,
                        successMessage = "تم تسجيل قرار الرفض الشرعي بنجاح"
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isActionInProgress = false,
                        errorMessage = res.message
                    )
                }
                else -> Unit
            }
        }
    }

    fun requestChanges(itemId: String, reviewerId: String, reviewerName: String, requiredChanges: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActionInProgress = true)
            when (val res = repository.requestChanges(itemId, reviewerId, reviewerName, requiredChanges)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isActionInProgress = false,
                        successMessage = "تم إرسال طلب التعديل الشرعي للمنشئ"
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isActionInProgress = false,
                        errorMessage = res.message
                    )
                }
                else -> Unit
            }
        }
    }

    fun escalateToSecondReviewer(
        itemId: String,
        reviewerId: String,
        reviewerName: String,
        targetReviewerId: String,
        targetReviewerName: String,
        reason: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActionInProgress = true)
            when (val res = repository.escalateToSecondReviewer(
                itemId, reviewerId, reviewerName, targetReviewerId, targetReviewerName, reason
            )) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isActionInProgress = false,
                        successMessage = "تم تحويل المحتوى للمراجع الثاني ($targetReviewerName)"
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isActionInProgress = false,
                        errorMessage = res.message
                    )
                }
                else -> Unit
            }
        }
    }

    fun submitSecondReviewDecision(
        itemId: String,
        secondReviewerId: String,
        secondReviewerName: String,
        approve: Boolean,
        reason: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActionInProgress = true)
            when (val res = repository.submitSecondReviewDecision(
                itemId, secondReviewerId, secondReviewerName, approve, reason
            )) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isActionInProgress = false,
                        successMessage = if (approve) "تم اكتمال الاعتماد المشترك بنجاح" else "تم تسجيل قرار المراجع الثاني"
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isActionInProgress = false,
                        errorMessage = res.message
                    )
                }
                else -> Unit
            }
        }
    }

    fun addClaimComment(itemId: String, claimId: String, reviewerId: String, reviewerName: String, comment: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActionInProgress = true)
            when (val res = repository.addClaimComment(itemId, claimId, reviewerId, reviewerName, comment)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isActionInProgress = false,
                        successMessage = "تم حفظ التعليق الشرعي على المطالبة"
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isActionInProgress = false,
                        errorMessage = res.message
                    )
                }
                else -> Unit
            }
        }
    }

    fun addInternalNote(itemId: String, authorId: String, authorName: String, note: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActionInProgress = true)
            when (val res = repository.addInternalNote(itemId, authorId, authorName, note)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isActionInProgress = false,
                        successMessage = "تمت إضافة الملاحظة الداخلية للمراجعين"
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isActionInProgress = false,
                        errorMessage = res.message
                    )
                }
                else -> Unit
            }
        }
    }

    fun scheduleReReview(itemId: String, reviewerId: String, reviewerName: String, timestamp: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActionInProgress = true)
            when (val res = repository.scheduleReReviewDate(itemId, reviewerId, reviewerName, timestamp)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isActionInProgress = false,
                        successMessage = "تم تحديد موعد إعادة المراجعة الشرعية"
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isActionInProgress = false,
                        errorMessage = res.message
                    )
                }
                else -> Unit
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}

class ShariaReviewViewModelFactory(
    private val repository: ShariaReviewRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShariaReviewViewModel::class.java)) {
            return ShariaReviewViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
