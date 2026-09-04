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
import kotlinx.coroutines.flow.combine

class FirebaseIncidentResponseRepositoryImpl : IncidentResponseRepository {
    private val isGlobalPublishingHalted = MutableStateFlow(false)

    private val emergencyActions = MutableStateFlow<List<EmergencyActionRecord>>(emptyList())

    private val shariaCorrections = MutableStateFlow<List<ShariaIncidentCorrection>>(emptyList())

    private val postMortemReports = MutableStateFlow<List<IncidentPostMortemReport>>(emptyList())

    private val contactsMatrix = MutableStateFlow(IncidentResponseEngine.STANDARD_CONTACTS_MATRIX)

    override fun getIncidentResponseStateStream(): Flow<IncidentResponseState> =
        combine(
            isGlobalPublishingHalted,
            postMortemReports,
            emergencyActions,
            shariaCorrections,
            contactsMatrix,
        ) { isHalted, reports, actions, sharia, contacts ->
            IncidentResponseState(
                isGlobalPublishingHalted = isHalted,
                activeIncidentsCount = if (isHalted) 1 else 0,
                criticalP0Count = if (isHalted) 1 else 0,
                reportsList = reports,
                emergencyActionsHistory = actions,
                shariaCorrections = sharia,
                contactsMatrix = contacts,
            )
        }

    override fun getPostMortemReportsStream(): Flow<List<IncidentPostMortemReport>> = postMortemReports.asStateFlow()

    override fun getEmergencyActionsStream(): Flow<List<EmergencyActionRecord>> = emergencyActions.asStateFlow()

    override fun getContactsMatrixStream(): Flow<List<IncidentContact>> = contactsMatrix.asStateFlow()

    override suspend fun toggleGlobalPublishingHalt(
        halt: Boolean,
        executedByUserId: String,
        executedByRole: IncidentRole,
        reasonArabic: String,
    ): Result<EmergencyActionRecord> {
        val actionResult =
            IncidentResponseEngine.createEmergencyAction(
                actionType = EmergencyActionType.EMERGENCY_HALT_PUBLISHING,
                executedByUserId = executedByUserId,
                executedByRole = executedByRole,
                targetResource = "GLOBAL_PUBLISHING_GATEWAY",
                reasonArabic = reasonArabic,
            )
        if (actionResult.isFailure) return actionResult

        val record = actionResult.getOrThrow()
        isGlobalPublishingHalted.value = halt
        emergencyActions.value = listOf(record) + emergencyActions.value
        return Result.success(record)
    }

    override suspend fun rotateSecretKey(
        secretIdentifier: String,
        executedByUserId: String,
        reasonArabic: String,
    ): Result<EmergencyActionRecord> {
        val actionResult =
            IncidentResponseEngine.createEmergencyAction(
                actionType = EmergencyActionType.ROTATE_SECRET_CREDENTIAL,
                executedByUserId = executedByUserId,
                executedByRole = IncidentRole.SECURITY_OFFICER,
                targetResource = secretIdentifier,
                reasonArabic = reasonArabic,
            )
        if (actionResult.isFailure) return actionResult

        val record = actionResult.getOrThrow()
        emergencyActions.value = listOf(record) + emergencyActions.value
        return Result.success(record)
    }

    override suspend fun suspendPublishedProject(
        projectId: String,
        executedByUserId: String,
        reasonArabic: String,
    ): Result<EmergencyActionRecord> {
        val actionResult =
            IncidentResponseEngine.createEmergencyAction(
                actionType = EmergencyActionType.SUSPEND_PROJECT_PUBLISHING,
                executedByUserId = executedByUserId,
                executedByRole = IncidentRole.SHARIA_REVIEWER_LEAD,
                targetResource = projectId,
                reasonArabic = reasonArabic,
            )
        if (actionResult.isFailure) return actionResult

        val record = actionResult.getOrThrow()
        emergencyActions.value = listOf(record) + emergencyActions.value
        return Result.success(record)
    }

    override suspend fun executeBatchRefund(
        targetUserOrBatchId: String,
        refundAmountCredits: Int,
        executedByUserId: String,
        reasonArabic: String,
    ): Result<EmergencyActionRecord> {
        val actionResult =
            IncidentResponseEngine.createEmergencyAction(
                actionType = EmergencyActionType.TRIGGER_BATCH_REFUND,
                executedByUserId = executedByUserId,
                executedByRole = IncidentRole.FINANCIAL_BILLING_LEAD,
                targetResource = "$targetUserOrBatchId (Credits: $refundAmountCredits)",
                reasonArabic = reasonArabic,
            )
        if (actionResult.isFailure) return actionResult

        val record = actionResult.getOrThrow()
        emergencyActions.value = listOf(record) + emergencyActions.value
        return Result.success(record)
    }

    override suspend fun submitShariaCorrection(correction: ShariaIncidentCorrection): Result<ShariaIncidentCorrection> {
        val validationResult = IncidentResponseEngine.validateShariaCorrection(correction)
        if (validationResult.isFailure) return validationResult

        val verified = validationResult.getOrThrow()
        shariaCorrections.value = listOf(verified) + shariaCorrections.value
        return Result.success(verified)
    }

    override suspend fun createPostMortemReport(report: IncidentPostMortemReport): Result<IncidentPostMortemReport> {
        postMortemReports.value = listOf(report) + postMortemReports.value
        return Result.success(report)
    }
}
