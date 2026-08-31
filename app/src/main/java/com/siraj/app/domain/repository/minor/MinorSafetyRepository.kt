package com.siraj.app.domain.repository.minor

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.minor.*
import kotlinx.coroutines.flow.Flow

interface MinorSafetyRepository {
    fun getMinorSafetyPolicy(userId: String): Flow<MinorSafetyPolicy>

    fun getUserAgeBracket(userId: String): Flow<UserAgeBracket>

    suspend fun setUserAgeBracket(
        userId: String,
        bracket: UserAgeBracket,
        guardianEmail: String?,
    ): Resource<MinorSafetyPolicy>

    suspend fun requestParentalConsent(
        childUserId: String,
        guardianEmail: String,
        guardianName: String,
    ): Resource<ParentalConsentRecord>

    suspend fun verifyParentalConsent(
        consentId: String,
        verificationCode: String,
    ): Resource<ParentalConsentRecord>

    suspend fun revokeParentalConsent(consentId: String): Resource<ParentalConsentRecord>

    suspend fun submitChildSafetyReport(report: ChildSafetyIncidentReport): Resource<ChildSafetyIncidentReport>

    fun getChildSafetyReports(): Flow<List<ChildSafetyIncidentReport>>

    suspend fun purgeMinorData(userId: String): Resource<MinorDataDeletionSummary>

    fun evaluateEducationalContent(
        contentId: String,
        title: String,
        textSnippet: String,
    ): EducationalContentSafetyCheck
}
