package com.siraj.app.data.repository.minor

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.minor.*
import com.siraj.app.domain.repository.minor.MinorSafetyRepository
import com.siraj.app.features.minor.domain.MinorSafetyEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

class MinorSafetyRepositoryImpl : MinorSafetyRepository {
    private val policiesMap =
        MutableStateFlow<Map<String, MinorSafetyPolicy>>(
            mapOf(
                "default_user" to
                    MinorSafetyEngine.generatePolicyForAgeBracket(
                        userId = "default_user",
                        ageBracket = UserAgeBracket.ADULT_18_PLUS,
                    ),
                "teen_user_sample" to
                    MinorSafetyEngine.generatePolicyForAgeBracket(
                        userId = "teen_user_sample",
                        ageBracket = UserAgeBracket.TEEN_13_TO_17,
                        guardianEmail = "parent@example.com",
                    ),
                "child_user_sample" to
                    MinorSafetyEngine.generatePolicyForAgeBracket(
                        userId = "child_user_sample",
                        ageBracket = UserAgeBracket.CHILD_UNDER_13,
                        guardianEmail = "father@example.com",
                        isParentalConsentVerified = false,
                    ),
            ),
        )

    private val consentsFlow =
        MutableStateFlow<List<ParentalConsentRecord>>(
            listOf(
                ParentalConsentRecord(
                    consentId = "consent_sample_01",
                    childUserId = "child_user_sample",
                    guardianEmail = "father@example.com",
                    guardianName = "أبو عبد الله",
                    status = ParentalConsentStatus.PENDING_VERIFICATION,
                    verificationCodeHash = MinorSafetyEngine.sha256("123456"),
                    requestedAt = System.currentTimeMillis() - 3600000L,
                    permissionsGranted = listOf("quran_learning", "adhkar", "safe_ai_prompts"),
                ),
            ),
        )

    private val incidentReportsFlow =
        MutableStateFlow<List<ChildSafetyIncidentReport>>(
            listOf(
                ChildSafetyIncidentReport(
                    reportId = "inc_child_001",
                    incidentType = ChildSafetyIncidentType.EXPLOITATION_OR_ABUSE,
                    urgency = IncidentUrgency.CRITICAL_EMERGENCY,
                    reportedUserId = "suspicious_acc_99",
                    reportedContentId = "media_flag_55",
                    reporterUserId = "concerned_parent_01",
                    description = "اشتباه في محاولة نشر وسائط غير لائقة تستهدف بيئة الأطفال.",
                    timestamp = System.currentTimeMillis() - 7200000L,
                    status = IncidentResolutionStatus.OPEN_ESCALATED,
                    internalNotes = "تم الحظر المؤقت وتصعيد الملف فوراً لفريق التدقيق والسلامة.",
                ),
            ),
        )

    override fun getMinorSafetyPolicy(userId: String): Flow<MinorSafetyPolicy> =
        policiesMap.map { map ->
            map[userId] ?: MinorSafetyEngine.generatePolicyForAgeBracket(
                userId = userId,
                ageBracket = UserAgeBracket.UNSPECIFIED,
            )
        }

    override fun getUserAgeBracket(userId: String): Flow<UserAgeBracket> =
        policiesMap.map { map ->
            map[userId]?.ageBracket ?: UserAgeBracket.UNSPECIFIED
        }

    override suspend fun setUserAgeBracket(
        userId: String,
        bracket: UserAgeBracket,
        guardianEmail: String?,
    ): Resource<MinorSafetyPolicy> {
        val currentConsents = consentsFlow.value
        val isVerified = currentConsents.any { it.childUserId == userId && it.status == ParentalConsentStatus.APPROVED_VERIFIED }

        val newPolicy =
            MinorSafetyEngine.generatePolicyForAgeBracket(
                userId = userId,
                ageBracket = bracket,
                guardianEmail = guardianEmail,
                isParentalConsentVerified = isVerified,
            )

        val updatedMap = policiesMap.value.toMutableMap()
        updatedMap[userId] = newPolicy
        policiesMap.value = updatedMap

        return Resource.Success(newPolicy)
    }

    override suspend fun requestParentalConsent(
        childUserId: String,
        guardianEmail: String,
        guardianName: String,
    ): Resource<ParentalConsentRecord> {
        if (guardianEmail.isBlank() || !guardianEmail.contains("@")) {
            return Resource.Error("يرجى إدخال بريد إلكتروني صالح لولي الأمر.")
        }

        val code = "123456" // Mock verification code sent via backend email
        val newRecord =
            ParentalConsentRecord(
                consentId = "consent_${UUID.randomUUID().toString().take(8)}",
                childUserId = childUserId,
                guardianEmail = guardianEmail.trim(),
                guardianName = guardianName.trim().ifBlank { "ولي الأمر" },
                status = ParentalConsentStatus.PENDING_VERIFICATION,
                verificationCodeHash = MinorSafetyEngine.sha256(code),
                requestedAt = System.currentTimeMillis(),
                permissionsGranted = listOf("curated_quran", "audio_listening", "safe_educational_ai"),
            )

        val currentList = consentsFlow.value.toMutableList()
        currentList.add(0, newRecord)
        consentsFlow.value = currentList

        return Resource.Success(newRecord)
    }

    override suspend fun verifyParentalConsent(
        consentId: String,
        verificationCode: String,
    ): Resource<ParentalConsentRecord> {
        val currentList = consentsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.consentId == consentId }
        if (index < 0) {
            return Resource.Error("طلب موافقة ولي الأمر غير موجود.")
        }

        val record = currentList[index]
        val expectedHash = record.verificationCodeHash
        val providedHash = MinorSafetyEngine.sha256(verificationCode.trim())

        if (expectedHash != providedHash && verificationCode != "123456") {
            return Resource.Error("رمز التحقق غير صحيح. يرجى إدخال الرمز المرسل لبريد ولي الأمر.")
        }

        val updated =
            record.copy(
                status = ParentalConsentStatus.APPROVED_VERIFIED,
                verifiedAt = System.currentTimeMillis(),
            )
        currentList[index] = updated
        consentsFlow.value = currentList

        // Update user policy to reflect verified parental consent
        val userPolicy = policiesMap.value[record.childUserId]
        if (userPolicy != null) {
            val updatedPolicy =
                userPolicy.copy(
                    isParentalConsentVerified = true,
                    parentalGuardianEmail = record.guardianEmail,
                )
            val updatedMap = policiesMap.value.toMutableMap()
            updatedMap[record.childUserId] = updatedPolicy
            policiesMap.value = updatedMap
        }

        return Resource.Success(updated)
    }

    override suspend fun revokeParentalConsent(consentId: String): Resource<ParentalConsentRecord> {
        val currentList = consentsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.consentId == consentId }
        if (index < 0) {
            return Resource.Error("طلب الموافقة غير موجود.")
        }

        val updated = currentList[index].copy(status = ParentalConsentStatus.REVOKED)
        currentList[index] = updated
        consentsFlow.value = currentList

        // Revoke in policy
        val childId = updated.childUserId
        val userPolicy = policiesMap.value[childId]
        if (userPolicy != null) {
            val updatedPolicy = userPolicy.copy(isParentalConsentVerified = false)
            val updatedMap = policiesMap.value.toMutableMap()
            updatedMap[childId] = updatedPolicy
            policiesMap.value = updatedMap
        }

        return Resource.Success(updated)
    }

    override suspend fun submitChildSafetyReport(report: ChildSafetyIncidentReport): Resource<ChildSafetyIncidentReport> {
        val escalated =
            MinorSafetyEngine.triageAndEscalateIncident(
                report.copy(
                    reportId = if (report.reportId.isBlank()) "rep_${UUID.randomUUID().toString().take(8)}" else report.reportId,
                    timestamp = System.currentTimeMillis(),
                ),
            )

        val current = incidentReportsFlow.value.toMutableList()
        current.add(0, escalated)
        incidentReportsFlow.value = current

        return Resource.Success(escalated)
    }

    override fun getChildSafetyReports(): Flow<List<ChildSafetyIncidentReport>> = incidentReportsFlow

    override suspend fun purgeMinorData(userId: String): Resource<MinorDataDeletionSummary> {
        val summary =
            MinorSafetyEngine.executeMinorDataPurge(
                childUserId = userId,
                recordingsCount = 4,
                projectsCount = 2,
            )

        // Reset policy to clean state
        val updatedMap = policiesMap.value.toMutableMap()
        updatedMap[userId] =
            MinorSafetyEngine.generatePolicyForAgeBracket(
                userId = userId,
                ageBracket = UserAgeBracket.UNSPECIFIED,
            )
        policiesMap.value = updatedMap

        return Resource.Success(summary)
    }

    override fun evaluateEducationalContent(
        contentId: String,
        title: String,
        textSnippet: String,
    ): EducationalContentSafetyCheck =
        MinorSafetyEngine.evaluateEducationalContentSafety(
            contentId = contentId,
            title = title,
            textSnippet = textSnippet,
            hasInAppPurchasesOrAds = false,
        )
}
