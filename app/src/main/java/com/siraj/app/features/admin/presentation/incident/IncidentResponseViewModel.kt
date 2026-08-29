package com.siraj.app.features.admin.presentation.incident

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siraj.app.domain.models.incident.EmergencyActionRecord
import com.siraj.app.domain.models.incident.IncidentContact
import com.siraj.app.domain.models.incident.IncidentPhase
import com.siraj.app.domain.models.incident.IncidentPostMortemReport
import com.siraj.app.domain.models.incident.IncidentResponseState
import com.siraj.app.domain.models.incident.IncidentRole
import com.siraj.app.domain.models.incident.IncidentSeverity
import com.siraj.app.domain.models.incident.IncidentType
import com.siraj.app.domain.models.incident.ShariaIncidentCorrection
import com.siraj.app.domain.repository.incident.IncidentResponseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class IncidentResponseUiState(
    val isGlobalPublishingHalted: Boolean = false,
    val reports: List<IncidentPostMortemReport> = emptyList(),
    val emergencyActions: List<EmergencyActionRecord> = emptyList(),
    val contacts: List<IncidentContact> = emptyList(),
    val selectedIncidentType: IncidentType = IncidentType.SERVICE_OUTAGE,
    val selectedReportForDetail: IncidentPostMortemReport? = null,
    val isPerformingEmergencyAction: Boolean = false,
    val bannerMessage: String? = null
)

class IncidentResponseViewModel(
    private val repository: IncidentResponseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(IncidentResponseUiState())
    val uiState: StateFlow<IncidentResponseUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                repository.getIncidentResponseStateStream(),
                repository.getPostMortemReportsStream(),
                repository.getEmergencyActionsStream(),
                repository.getContactsMatrixStream()
            ) { state, reports, actions, contacts ->
                state to Triple(reports, actions, contacts)
            }.collect { (state, details) ->
                val (reports, actions, contacts) = details
                _uiState.value = _uiState.value.copy(
                    isGlobalPublishingHalted = state.isGlobalPublishingHalted,
                    reports = reports,
                    emergencyActions = actions,
                    contacts = contacts
                )
            }
        }
    }

    fun selectIncidentType(type: IncidentType) {
        _uiState.value = _uiState.value.copy(selectedIncidentType = type)
    }

    fun toggleGlobalPublishing(halt: Boolean, reasonArabic: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPerformingEmergencyAction = true)
            val result = repository.toggleGlobalPublishingHalt(
                halt = halt,
                executedByUserId = "ADM-INCIDENT-COMM",
                executedByRole = IncidentRole.INCIDENT_COMMANDER,
                reasonArabic = reasonArabic
            )
            _uiState.value = _uiState.value.copy(
                isPerformingEmergencyAction = false,
                isGlobalPublishingHalted = halt,
                bannerMessage = if (result.isSuccess) {
                    if (halt) "تم تفعيل الإيقاف الطارئ للنشر العام (Kill Switch) وتجميد البوابات"
                    else "تم إلغاء تجميد النشر العام واستعادة المسار الطبيعي"
                } else {
                    "تعذر تنفيذ الإجراء: ${result.exceptionOrNull()?.message}"
                }
            )
        }
    }

    fun rotateSecretCredential(secretKeyId: String, reasonArabic: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPerformingEmergencyAction = true)
            val result = repository.rotateSecretKey(
                secretIdentifier = secretKeyId,
                executedByUserId = "ADM-SECURITY-CISO",
                reasonArabic = reasonArabic
            )
            _uiState.value = _uiState.value.copy(
                isPerformingEmergencyAction = false,
                bannerMessage = if (result.isSuccess) {
                    "تم إبطال المفتاح القديم وتوليد اعتماد جديد بنجاح في Secret Manager"
                } else {
                    "تعذر تدوير المفتاح: ${result.exceptionOrNull()?.message}"
                }
            )
        }
    }

    fun suspendPublishedContent(projectId: String, reasonArabic: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPerformingEmergencyAction = true)
            val result = repository.suspendPublishedProject(
                projectId = projectId,
                executedByUserId = "ADM-SHARIA-01",
                reasonArabic = reasonArabic
            )
            _uiState.value = _uiState.value.copy(
                isPerformingEmergencyAction = false,
                bannerMessage = if (result.isSuccess) {
                    "تم تعليق وسحب المشروع $projectId فوراً من العرض العام"
                } else {
                    "تعذر سحب المشروع: ${result.exceptionOrNull()?.message}"
                }
            )
        }
    }

    fun executeRefundBatch(targetUserOrBatchId: String, refundAmountCredits: Int, reasonArabic: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPerformingEmergencyAction = true)
            val result = repository.executeBatchRefund(
                targetUserOrBatchId = targetUserOrBatchId,
                refundAmountCredits = refundAmountCredits,
                executedByUserId = "ADM-BILLING-01",
                reasonArabic = reasonArabic
            )
            _uiState.value = _uiState.value.copy(
                isPerformingEmergencyAction = false,
                bannerMessage = if (result.isSuccess) {
                    "تم استرداد $refundAmountCredits رصيد بنجاح للمتأثرين ($targetUserOrBatchId)"
                } else {
                    "تعذر تنفيذ الاسترداد: ${result.exceptionOrNull()?.message}"
                }
            )
        }
    }

    fun submitShariaCorrection(
        incidentId: String,
        projectId: String,
        faultyText: String,
        correctText: String,
        sourceRef: String,
        rev1Id: String,
        rev1Notes: String,
        rev2Id: String,
        rev2Notes: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPerformingEmergencyAction = true)
            val correction = ShariaIncidentCorrection(
                incidentId = incidentId,
                projectId = projectId,
                faultyText = faultyText,
                verifiedCorrectText = correctText,
                primarySourceReference = sourceRef,
                reviewer1Id = rev1Id,
                reviewer1NotesArabic = rev1Notes,
                reviewer2Id = rev2Id,
                reviewer2NotesArabic = rev2Notes,
                approvedByBothReviewers = true,
                publishedVersion = 2
            )
            val result = repository.submitShariaCorrection(correction)
            _uiState.value = _uiState.value.copy(
                isPerformingEmergencyAction = false,
                bannerMessage = if (result.isSuccess) {
                    "تم اعتماد التصحيح الشرعي ونشر الإصدار المعتمد الجديد بنجاح"
                } else {
                    "تعذر تسجيل التصحيح: ${result.exceptionOrNull()?.message}"
                }
            )
        }
    }

    fun createPostMortemReport(
        incidentId: String,
        incidentType: IncidentType,
        severity: IncidentSeverity,
        titleArabic: String,
        rootCauseSummary: String,
        containmentSteps: List<String>,
        correctiveActions: List<String>,
        preventiveTasks: List<String>
    ) {
        viewModelScope.launch {
            val report = IncidentPostMortemReport(
                incidentId = incidentId,
                incidentType = incidentType,
                severity = severity,
                titleArabic = titleArabic,
                leadInvestigator = "فريق الاستجابة للحوادث",
                detectionTimestamp = System.currentTimeMillis() - 3600000L,
                containmentTimestamp = System.currentTimeMillis() - 1800000L,
                resolutionTimestamp = System.currentTimeMillis(),
                rootCauseSummaryArabic = rootCauseSummary,
                containmentStepsArabic = containmentSteps,
                correctiveActionsArabic = correctiveActions,
                preventiveTasksArabic = preventiveTasks,
                userNotificationIssued = true,
                userNoticeContentArabic = "تم حل الخلل واستعادة الاستقرار الكامل للخدمة."
            )
            val result = repository.createPostMortemReport(report)
            _uiState.value = _uiState.value.copy(
                bannerMessage = if (result.isSuccess) "تم حفظ تقرير الحادث (${report.reportId}) بنجاح" else "فشل الحفظ"
            )
        }
    }

    fun selectReportForDetail(report: IncidentPostMortemReport?) {
        _uiState.value = _uiState.value.copy(selectedReportForDetail = report)
    }

    fun clearBanner() {
        _uiState.value = _uiState.value.copy(bannerMessage = null)
    }
}
