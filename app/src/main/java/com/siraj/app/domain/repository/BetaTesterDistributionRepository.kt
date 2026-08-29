package com.siraj.app.domain.repository

import com.siraj.app.domain.models.beta.BetaReleaseNote
import com.siraj.app.domain.models.beta.BetaTesterProfile
import com.siraj.app.domain.models.beta.CriticalJourney
import com.siraj.app.domain.models.beta.DistributionChannelInfo
import com.siraj.app.domain.models.beta.TesterExperienceSurvey
import kotlinx.coroutines.flow.Flow

interface BetaTesterDistributionRepository {
    fun getTesterProfile(userId: String): Flow<BetaTesterProfile?>
    suspend fun registerTesterSession(
        testerId: String,
        email: String,
        name: String,
        deviceModel: String,
        osVersion: String,
        appVersion: String,
        buildCode: Int
    ): Result<BetaTesterProfile>
    suspend fun recordJourneyCompletion(testerId: String, journeyId: String): Result<Unit>
    suspend fun submitExperienceSurvey(survey: TesterExperienceSurvey): Result<String>
    suspend fun revokeTesterAccess(testerId: String, reason: String): Result<Unit>
    fun getReleaseNotes(): List<BetaReleaseNote>
    fun getDistributionChannels(): List<DistributionChannelInfo>
    fun getCriticalJourneys(): List<CriticalJourney>
}
