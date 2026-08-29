package com.siraj.app.data.repository.incident

import com.siraj.app.core.incident.IncidentResponseEngine
import com.siraj.app.domain.models.incident.EmergencyActionRecord
import com.siraj.app.domain.models.incident.EmergencyActionType
import com.siraj.app.domain.models.incident.IncidentContact
import com.siraj.app.domain.models.incident.IncidentPostMortemReport
import com.siraj.app.domain.models.incident.IncidentResponseState
import com.siraj.app.domain.models.incident.IncidentRole
import com.siraj.app.domain.models.incident.IncidentSeverity
import com.siraj.app.domain.models.incident.IncidentType
import com.siraj.app.domain.models.incident.ShariaIncidentCorrection
import com.siraj.app.domain.repository.incident.IncidentResponseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

import kotlinx.coroutines.flow.combine

class FirebaseIncidentResponseRepositoryImpl : IncidentResponseRepository {

    private val isGlobalPublishingHalted = MutableStateFlow(false)

    private val emergencyActions = MutableStateFlow<List<EmergencyActionRecord>>(
        listOf(
            EmergencyActionRecord(
                actionType = EmergencyActionType.ROTATE_SECRET_CREDENTIAL,
                executedByUserId = "USR-SEC-01",
                executedByRole = IncidentRole.SECURITY_OFFICER,
                targetResource = "GEMINI_SERVICE_ACCOUNT_KEY",
                reasonArabic = "تدوير دوري وقائي كل 90 يوماً للمفاتيح السحابية",
                auditLogNotes = "Rotation executed successfully via Secret Manager"
            )
        )
    )

    private val shariaCorrections = MutableStateFlow<List<ShariaIncidentCorrection>>(
        listOf(
            ShariaIncidentCorrection(
                incidentId = "INC-SHARIA-001",
                projectId = "PRJ-TAFSIR-104",
                faultyText = "نقل غير دقيق لكلمة في تفسير سورة النور",
                verifiedCorrectText = "تفسير ابن كثير المعتمد - طبعة دار طيبة ج 6 ص 44",
                primarySourceReference = "تفسير ابن كثير (المعتمد)",
                reviewer1Id = "REV-SCHOLAR-01",
                reviewer1NotesArabic = "تمت المطابقة مع طبعة دار طيبة المعتمدة وتصحيح العبارة بدقة",
                reviewer2Id = "REV-SCHOLAR-02",
                reviewer2NotesArabic = "أؤكد دقة التصحيح واستيفاء سند النقل",
                approvedByBothReviewers = true,
                publishedVersion = 2
            )
        )
    )

    private val postMortemReports = MutableStateFlow<List<IncidentPostMortemReport>>(
        listOf(
            IncidentPostMortemReport(
                reportId = "REP-2026-08A",
                incidentId = "INC-OUTAGE-001",
                incidentType = IncidentType.SERVICE_OUTAGE,
                severity = IncidentSeverity.P1_HIGH,
                titleArabic = "تأخر في معالجة طابور تصيير الفيديو في منطقة غرب أوروبا",
                leadInvestigator = "م. خالد المنصور (Tech Lead)",
                detectionTimestamp = System.currentTimeMillis() - 86400000L,
                containmentTimestamp = System.currentTimeMillis() - 82800000L,
                resolutionTimestamp = System.currentTimeMillis() - 79200000L,
                totalDowntimeMinutes = 60,
                affectedUsersCount = 18,
                rootCauseSummaryArabic = "زيادة مفاجئة في مشاريع التصدير المتزامنة وتأخر التوسع التلقائي لحاويات Cloud Run",
                containmentStepsArabic = listOf(
                    "تفعيل التوسع اليدوي للحاويات إلى 30 حاوية",
                    "إعطاء الأولوية للوظائف المعلقة عبر طابور المعالجة السريعة"
                ),
                correctiveActionsArabic = listOf(
                    "تعديل سياسة التوسع التلقائي لتبدأ عند 60% استهلاك بدلاً من 80%",
                    "إضافة حاويات دافئة جاهزة (Warm Instances)"
                ),
                preventiveTasksArabic = listOf(
                    "تحديث إعدادات Cloud Run Auto-scaler",
                    "ربط تنبيهات Cloud Monitoring بـ PagerDuty"
                ),
                userNotificationIssued = true,
                userNoticeContentArabic = IncidentResponseEngine.sanitizePublicIncidentNotice(
                    IncidentType.SERVICE_OUTAGE,
                    "Cloud Run Scale Delay"
                )
            )
        )
    )

    private val contactsMatrix = MutableStateFlow(IncidentResponseEngine.STANDARD_CONTACTS_MATRIX)

    override fun getIncidentResponseStateStream(): Flow<IncidentResponseState> {
        return combine(
            isGlobalPublishingHalted,
            postMortemReports,
            emergencyActions,
            shariaCorrections,
            contactsMatrix
        ) { isHalted, reports, actions, sharia, contacts ->
            IncidentResponseState(
                isGlobalPublishingHalted = isHalted,
                activeIncidentsCount = if (isHalted) 1 else 0,
                criticalP0Count = if (isHalted) 1 else 0,
                reportsList = reports,
                emergencyActionsHistory = actions,
                shariaCorrections = sharia,
                contactsMatrix = contacts
            )
        }
    }

    override fun getPostMortemReportsStream(): Flow<List<IncidentPostMortemReport>> =
        postMortemReports.asStateFlow()

    override fun getEmergencyActionsStream(): Flow<List<EmergencyActionRecord>> =
        emergencyActions.asStateFlow()

    override fun getContactsMatrixStream(): Flow<List<IncidentContact>> =
        contactsMatrix.asStateFlow()

    override suspend fun toggleGlobalPublishingHalt(
        halt: Boolean,
        executedByUserId: String,
        executedByRole: IncidentRole,
        reasonArabic: String
    ): Result<EmergencyActionRecord> {
        val actionResult = IncidentResponseEngine.createEmergencyAction(
            actionType = EmergencyActionType.EMERGENCY_HALT_PUBLISHING,
            executedByUserId = executedByUserId,
            executedByRole = executedByRole,
            targetResource = "GLOBAL_PUBLISHING_GATEWAY",
            reasonArabic = reasonArabic
        )
        if (actionResult.isFailure) return actionResult

        val record = actionResult.getOrNull()!!
        isGlobalPublishingHalted.value = halt
        emergencyActions.value = listOf(record) + emergencyActions.value
        return Result.success(record)
    }

    override suspend fun rotateSecretKey(
        secretIdentifier: String,
        executedByUserId: String,
        reasonArabic: String
    ): Result<EmergencyActionRecord> {
        val actionResult = IncidentResponseEngine.createEmergencyAction(
            actionType = EmergencyActionType.ROTATE_SECRET_CREDENTIAL,
            executedByUserId = executedByUserId,
            executedByRole = IncidentRole.SECURITY_OFFICER,
            targetResource = secretIdentifier,
            reasonArabic = reasonArabic
        )
        if (actionResult.isFailure) return actionResult

        val record = actionResult.getOrNull()!!
        emergencyActions.value = listOf(record) + emergencyActions.value
        return Result.success(record)
    }

    override suspend fun suspendPublishedProject(
        projectId: String,
        executedByUserId: String,
        reasonArabic: String
    ): Result<EmergencyActionRecord> {
        val actionResult = IncidentResponseEngine.createEmergencyAction(
            actionType = EmergencyActionType.SUSPEND_PROJECT_PUBLISHING,
            executedByUserId = executedByUserId,
            executedByRole = IncidentRole.SHARIA_REVIEWER_LEAD,
            targetResource = projectId,
            reasonArabic = reasonArabic
        )
        if (actionResult.isFailure) return actionResult

        val record = actionResult.getOrNull()!!
        emergencyActions.value = listOf(record) + emergencyActions.value
        return Result.success(record)
    }

    override suspend fun executeBatchRefund(
        targetUserOrBatchId: String,
        refundAmountCredits: Int,
        executedByUserId: String,
        reasonArabic: String
    ): Result<EmergencyActionRecord> {
        val actionResult = IncidentResponseEngine.createEmergencyAction(
            actionType = EmergencyActionType.TRIGGER_BATCH_REFUND,
            executedByUserId = executedByUserId,
            executedByRole = IncidentRole.FINANCIAL_BILLING_LEAD,
            targetResource = "$targetUserOrBatchId (Credits: $refundAmountCredits)",
            reasonArabic = reasonArabic
        )
        if (actionResult.isFailure) return actionResult

        val record = actionResult.getOrNull()!!
        emergencyActions.value = listOf(record) + emergencyActions.value
        return Result.success(record)
    }

    override suspend fun submitShariaCorrection(
        correction: ShariaIncidentCorrection
    ): Result<ShariaIncidentCorrection> {
        val validationResult = IncidentResponseEngine.validateShariaCorrection(correction)
        if (validationResult.isFailure) return validationResult

        val verified = validationResult.getOrNull()!!
        shariaCorrections.value = listOf(verified) + shariaCorrections.value
        return Result.success(verified)
    }

    override suspend fun createPostMortemReport(
        report: IncidentPostMortemReport
    ): Result<IncidentPostMortemReport> {
        postMortemReports.value = listOf(report) + postMortemReports.value
        return Result.success(report)
    }
}
