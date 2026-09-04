package com.siraj.app.features.minor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.minor.*
import com.siraj.app.domain.repository.minor.MinorSafetyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MinorSafetyUiState(
    val currentUserId: String = "",
    val policy: MinorSafetyPolicy = MinorSafetyPolicy(),
    val ageBracket: UserAgeBracket = UserAgeBracket.UNSPECIFIED,
    val consents: List<ParentalConsentRecord> = emptyList(),
    val incidentReports: List<ChildSafetyIncidentReport> = emptyList(),
    val educationalSafetyCheck: EducationalContentSafetyCheck? = null,
    val lastDeletionSummary: MinorDataDeletionSummary? = null,
    val isLoading: Boolean = false,
    val userMessage: String? = null,
    val isErrorMessage: Boolean = false,
    val showConsentDialog: Boolean = false,
    val showVerifyOtpDialog: Boolean = false,
    val selectedConsentIdToVerify: String? = null,
    val showReportDialog: Boolean = false,
    val showPurgeConfirmDialog: Boolean = false,
)

class MinorSafetyViewModel(
    private val repository: MinorSafetyRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MinorSafetyUiState())
    val uiState: StateFlow<MinorSafetyUiState> = _uiState.asStateFlow()

    init {
        loadDataForUser(_uiState.value.currentUserId)
    }

    fun switchUser(userId: String) {
        _uiState.update { it.copy(currentUserId = userId) }
        loadDataForUser(userId)
    }

    private fun loadDataForUser(userId: String) {
        viewModelScope.launch {
            repository.getMinorSafetyPolicy(userId).collect { pol ->
                _uiState.update { it.copy(policy = pol, ageBracket = pol.ageBracket) }
            }
        }

        viewModelScope.launch {
            repository.getChildSafetyReports().collect { reports ->
                _uiState.update { it.copy(incidentReports = reports) }
            }
        }
    }

    fun setUserAgeBracket(
        bracket: UserAgeBracket,
        guardianEmail: String? = null,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val res = repository.setUserAgeBracket(_uiState.value.currentUserId, bracket, guardianEmail)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            policy = res.data ?: it.policy,
                            ageBracket = bracket,
                            isLoading = false,
                            userMessage = "تم تحديث الفئة العمرية وتطبيق سياسة الحماية الصارمة بنجاح.",
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userMessage = res.message ?: "فشل تحديث الفئة العمرية.",
                            isErrorMessage = true,
                        )
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun requestParentalConsent(
        guardianEmail: String,
        guardianName: String,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val res = repository.requestParentalConsent(_uiState.value.currentUserId, guardianEmail, guardianName)) {
                is Resource.Success -> {
                    val updatedConsents = _uiState.value.consents.toMutableList()
                    res.data?.let { updatedConsents.add(0, it) }
                    _uiState.update {
                        it.copy(
                            consents = updatedConsents,
                            isLoading = false,
                            showConsentDialog = false,
                            userMessage = "تم إرسال طلب الموافقة ورمز التحقق إلى بريد ولي الأمر: $guardianEmail",
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userMessage = res.message ?: "فشل إرسال طلب الموافقة.",
                            isErrorMessage = true,
                        )
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun verifyParentalConsent(
        consentId: String,
        code: String,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val res = repository.verifyParentalConsent(consentId, code)) {
                is Resource.Success -> {
                    val updatedConsents =
                        _uiState.value.consents.map {
                            if (it.consentId == consentId) res.data ?: it else it
                        }
                    _uiState.update {
                        it.copy(
                            consents = updatedConsents,
                            isLoading = false,
                            showVerifyOtpDialog = false,
                            selectedConsentIdToVerify = null,
                            userMessage = "تم التحقق وتوثيق موافقة ولي الأمر رسمياً وتفعيل الصلاحيات المعتمدة.",
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userMessage = res.message ?: "رمز التحقق غير صحيح.",
                            isErrorMessage = true,
                        )
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun revokeParentalConsent(consentId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val res = repository.revokeParentalConsent(consentId)) {
                is Resource.Success -> {
                    val updatedConsents =
                        _uiState.value.consents.map {
                            if (it.consentId == consentId) res.data ?: it else it
                        }
                    _uiState.update {
                        it.copy(
                            consents = updatedConsents,
                            isLoading = false,
                            userMessage = "تم سحب موافقة ولي الأمر وإعادة تفعيل وضع الحظر التام.",
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userMessage = res.message ?: "فشل سحب الموافقة.",
                            isErrorMessage = true,
                        )
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun submitEmergencyChildSafetyReport(
        incidentType: ChildSafetyIncidentType,
        description: String,
        reportedUserId: String?,
        reportedContentId: String?,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val report =
                ChildSafetyIncidentReport(
                    incidentType = incidentType,
                    reportedUserId = reportedUserId,
                    reportedContentId = reportedContentId,
                    reporterUserId = _uiState.value.currentUserId,
                    description = description,
                )
            when (val res = repository.submitChildSafetyReport(report)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            showReportDialog = false,
                            userMessage = "تم تسجيل وتصعيد البلاغ ذي الأولوية القصوى (${res.data?.urgency?.titleArabic}) لفريق حماية الأطفال والإدارة فوراً.",
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userMessage = res.message ?: "فشل إرسال البلاغ.",
                            isErrorMessage = true,
                        )
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun purgeMinorData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val res = repository.purgeMinorData(_uiState.value.currentUserId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            showPurgeConfirmDialog = false,
                            lastDeletionSummary = res.data,
                            userMessage = "تم تطهير وحذف كافة بيانات وتسجيلات ومشاريع القاصر نهائياً.",
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userMessage = res.message ?: "فشل عملية الحذف والتطهير.",
                            isErrorMessage = true,
                        )
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun evaluateEducationalContent(
        title: String,
        textSnippet: String,
    ) {
        val check =
            repository.evaluateEducationalContent(
                contentId = "edu_preview_${System.currentTimeMillis()}",
                title = title,
                textSnippet = textSnippet,
            )
        _uiState.update { it.copy(educationalSafetyCheck = check) }
    }

    fun openConsentDialog() = _uiState.update { it.copy(showConsentDialog = true) }

    fun closeConsentDialog() = _uiState.update { it.copy(showConsentDialog = false) }

    fun openVerifyOtpDialog(consentId: String) =
        _uiState.update {
            it.copy(showVerifyOtpDialog = true, selectedConsentIdToVerify = consentId)
        }

    fun closeVerifyOtpDialog() =
        _uiState.update {
            it.copy(showVerifyOtpDialog = false, selectedConsentIdToVerify = null)
        }

    fun openReportDialog() = _uiState.update { it.copy(showReportDialog = true) }

    fun closeReportDialog() = _uiState.update { it.copy(showReportDialog = false) }

    fun openPurgeConfirmDialog() = _uiState.update { it.copy(showPurgeConfirmDialog = true) }

    fun closePurgeConfirmDialog() = _uiState.update { it.copy(showPurgeConfirmDialog = false) }

    fun dismissUserMessage() = _uiState.update { it.copy(userMessage = null, isErrorMessage = false) }
}
