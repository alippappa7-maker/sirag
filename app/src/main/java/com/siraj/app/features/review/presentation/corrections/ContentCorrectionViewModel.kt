package com.siraj.app.features.review.presentation.corrections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.Source
import com.siraj.app.domain.models.correction.*
import com.siraj.app.domain.models.review.ShariaClaim
import com.siraj.app.domain.models.review.ShariaReviewStatus
import com.siraj.app.domain.repository.review.ContentCorrectionRepository
import com.siraj.app.features.review.domain.ContentCorrectionEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ContentCorrectionUiState(
    val contentId: String = "proj_siraj_101",
    val versions: List<ContentVersion> = emptyList(),
    val notices: List<CorrectionNotice> = emptyList(),
    val selectedVersion: ContentVersion? = null,
    val selectedNotice: CorrectionNotice? = null,
    val sourceRevisions: List<SourceRevision> = emptyList(),
    val affectedAssets: List<AffectedAsset> = emptyList(),
    val reviews: List<CorrectionReview> = emptyList(),
    val impactReport: ImpactReport? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

class ContentCorrectionViewModel(
    private val repository: ContentCorrectionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ContentCorrectionUiState())
    val uiState: StateFlow<ContentCorrectionUiState> = _uiState.asStateFlow()

    init {
        loadContent(_uiState.value.contentId)
    }

    fun loadContent(contentId: String) {
        _uiState.update { it.copy(contentId = contentId, isLoading = true) }

        viewModelScope.launch {
            repository.getContentVersions(contentId).collect { versions ->
                _uiState.update { state ->
                    state.copy(
                        versions = versions,
                        selectedVersion = state.selectedVersion ?: versions.firstOrNull(),
                        isLoading = false,
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.getCorrectionNotices(contentId).collect { notices ->
                _uiState.update { state ->
                    val selNotice = state.selectedNotice ?: notices.firstOrNull()
                    state.copy(
                        notices = notices,
                        selectedNotice = selNotice,
                    )
                }
                if (notices.isNotEmpty()) {
                    loadNoticeDetails(notices.first().id)
                }
            }
        }
    }

    fun selectVersion(version: ContentVersion) {
        _uiState.update { it.copy(selectedVersion = version) }
    }

    fun selectNotice(notice: CorrectionNotice) {
        _uiState.update { it.copy(selectedNotice = notice) }
        loadNoticeDetails(notice.id)
    }

    fun loadNoticeDetails(noticeId: String) {
        viewModelScope.launch {
            repository.getSourceRevisions(noticeId).collect { revs ->
                _uiState.update { it.copy(sourceRevisions = revs) }
            }
        }
        viewModelScope.launch {
            repository.getAffectedAssets(noticeId).collect { assets ->
                _uiState.update { it.copy(affectedAssets = assets) }
            }
        }
        viewModelScope.launch {
            repository.getCorrectionReviews(noticeId).collect { revs ->
                _uiState.update { it.copy(reviews = revs) }
            }
        }
    }

    fun createCorrection(
        correctionType: CorrectionType,
        reason: String,
        detailedExplanation: String,
        discoveredBy: String,
        discoveredByType: DiscoveredByType,
        correctedTitle: String,
        correctedFullContentText: String,
        correctedClaims: List<ShariaClaim>,
        correctedSources: List<Source>,
        sourceRevisions: List<SourceRevision> = emptyList(),
        affectedAssets: List<AffectedAsset> = emptyList(),
        createdBy: String,
        createdByName: String,
        changeSummary: String,
        publicNoticeText: String = "",
        forceImmediateSuspension: Boolean = false,
    ) {
        val currentVer = _uiState.value.versions.maxByOrNull { it.versionNumber }
        if (currentVer == null) {
            _uiState.update { it.copy(errorMessage = "لا يوجد إصدار سابق للمحتوى لإنشاء تصحيح عليه") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val draftResult =
                    ContentCorrectionEngine.createCorrectionDraft(
                        currentVersion = currentVer,
                        correctionType = correctionType,
                        reason = reason,
                        detailedExplanation = detailedExplanation,
                        discoveredBy = discoveredBy,
                        discoveredByType = discoveredByType,
                        correctedTitle = correctedTitle,
                        correctedFullContentText = correctedFullContentText,
                        correctedClaims = correctedClaims,
                        correctedSources = correctedSources,
                        sourceRevisions = sourceRevisions,
                        affectedAssets = affectedAssets,
                        createdBy = createdBy,
                        createdByName = createdByName,
                        changeSummary = changeSummary,
                        publicNoticeText = publicNoticeText,
                        forceImmediateSuspension = forceImmediateSuspension,
                    )

                val result = repository.createCorrection(draftResult)
                when (result) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "تم إنشاء مسودة التصحيح رقم (${draftResult.newVersion.versionNumber}) وربط الأصول المتأثرة بنجاح",
                            )
                        }
                        selectNotice(draftResult.correctionNotice)
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "حدث خطأ غير متوقع") }
            }
        }
    }

    fun submitReview(
        noticeId: String,
        isApproved: Boolean,
        reviewerId: String,
        reviewerName: String,
        reviewerSpecialty: String,
        notes: String,
        shariaEvidences: List<String>,
    ) {
        val notice = _uiState.value.notices.find { it.id == noticeId } ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val review =
                CorrectionReview(
                    correctionNoticeId = noticeId,
                    fromVersionNumber = notice.fromVersionNumber,
                    toVersionNumber = notice.toVersionNumber,
                    reviewerId = reviewerId,
                    reviewerName = reviewerName,
                    reviewerSpecialty = reviewerSpecialty,
                    status = if (isApproved) ShariaReviewStatus.APPROVED else ShariaReviewStatus.REJECTED,
                    reviewerNotes = notes,
                    shariaEvidences = shariaEvidences,
                    reviewedAt = System.currentTimeMillis(),
                    isApproved = isApproved,
                )

            val result = repository.submitReview(noticeId, review)
            when (result) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage =
                                if (isApproved) {
                                    "تم اعتماد التصحيح شرعياً وترقية الإصدار رقم (${notice.toVersionNumber}) إلى نسخة منشورة"
                                } else {
                                    "تم رفض مسودة التصحيح وتسجيل ملاحظات المراجع الشرعي"
                                },
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun updateAssetStatus(
        assetId: String,
        newStatus: AssetImpactStatus,
    ) {
        viewModelScope.launch {
            val result = repository.updateAssetStatus(assetId, newStatus)
            if (result is Resource.Error) {
                _uiState.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    fun generateImpactReport(noticeId: String) {
        viewModelScope.launch {
            val result = repository.generateImpactReport(noticeId)
            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(impactReport = result.data) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearImpactReport() {
        _uiState.update { it.copy(impactReport = null) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
