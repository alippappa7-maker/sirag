package com.siraj.app.features.beta.presentation

import com.siraj.app.domain.models.beta.*
import com.siraj.app.domain.repository.BetaTesterDistributionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class TesterDistributionUnitTest {

    private class FakeTesterDistributionRepository : BetaTesterDistributionRepository {
        var registeredTesterId: String? = null
        var lastCompletedJourney: Pair<String, String>? = null
        var lastSubmittedSurvey: TesterExperienceSurvey? = null
        var revokedTesterId: String? = null

        override suspend fun registerTesterSession(
            testerId: String,
            email: String,
            name: String,
            deviceModel: String,
            osVersion: String,
            appVersion: String,
            buildCode: Int
        ): Result<BetaTesterProfile> {
            registeredTesterId = testerId
            return Result.success(
                BetaTesterProfile(
                    testerId = testerId,
                    email = email,
                    name = name,
                    deviceModel = deviceModel,
                    osVersion = osVersion,
                    installedAppVersion = appVersion,
                    installedBuildCode = buildCode
                )
            )
        }

        override fun getTesterProfile(userId: String): Flow<BetaTesterProfile?> {
            return flowOf(
                BetaTesterProfile(
                    testerId = userId,
                    email = "test@siraj.app",
                    name = "Tester",
                    group = TesterGroup.COMMUNITY_BETA,
                    status = TesterStatus.ACTIVE,
                    platform = "Android",
                    deviceModel = "Pixel 8",
                    osVersion = "Android 14",
                    installedAppVersion = "1.0.0-beta.1",
                    installedBuildCode = 100,
                    completedJourneys = listOf("journey_quran_search")
                )
            )
        }

        override suspend fun recordJourneyCompletion(testerId: String, journeyId: String): Result<Unit> {
            lastCompletedJourney = Pair(testerId, journeyId)
            return Result.success(Unit)
        }

        override suspend fun submitExperienceSurvey(survey: TesterExperienceSurvey): Result<String> {
            lastSubmittedSurvey = survey
            return Result.success(survey.id)
        }

        override suspend fun revokeTesterAccess(testerId: String, reason: String): Result<Unit> {
            revokedTesterId = testerId
            return Result.success(Unit)
        }

        override fun getCriticalJourneys(): List<CriticalJourney> {
            return listOf(
                CriticalJourney(
                    id = "journey_ideation_to_video",
                    title = "إنتاج مشهد وسيناريو من الفكرة",
                    description = "توليد فكرة، صياغة سيناريو، وتحويله إلى مشاهد",
                    targetRoute = "studio",
                    iconName = "VideoLibrary"
                )
            )
        }

        override fun getReleaseNotes(): List<BetaReleaseNote> {
            return listOf(
                BetaReleaseNote(
                    versionName = "1.0.0-beta.1",
                    buildCode = 100,
                    releaseDate = "2026-08-29",
                    platform = "Android & iOS",
                    channel = "Firebase App Distribution",
                    highlights = listOf("إطلاق نسخة Beta"),
                    fixedIssues = listOf("تحسين الأداء"),
                    knownLimitations = listOf("معاينة الفيديو تجريبية"),
                    targetGroups = listOf(TesterGroup.COMMUNITY_BETA)
                )
            )
        }

        override fun getDistributionChannels(): List<DistributionChannelInfo> {
            return listOf(
                DistributionChannelInfo(
                    platform = "Android",
                    channelName = "Firebase App Distribution",
                    methodTitle = "تثبيت النسخة التجريبية لأجهزة أندرويد",
                    stepGuide = listOf("قبول الدعوة", "تنزيل App Tester"),
                    updateInstructions = "تصل التحديثات تلقائياً",
                    supportNote = "تواصل مع فريق التطوير"
                )
            )
        }
    }

    @Test
    fun testTesterGroupsAndStatusesIntegrity() {
        val groups = TesterGroup.values()
        assertEquals(5, groups.size)
        assertTrue(groups.contains(TesterGroup.INTERNAL_TEAM))
        assertTrue(groups.contains(TesterGroup.SHARIA_REVIEWERS))
        assertTrue(groups.contains(TesterGroup.CONTENT_CREATORS))
        assertTrue(groups.contains(TesterGroup.ACCESSIBILITY_QA))
        assertTrue(groups.contains(TesterGroup.COMMUNITY_BETA))

        val statuses = TesterStatus.values()
        assertEquals(4, statuses.size)
        assertTrue(statuses.contains(TesterStatus.INVITED))
        assertTrue(statuses.contains(TesterStatus.ACTIVE))
        assertTrue(statuses.contains(TesterStatus.SUSPENDED))
        assertTrue(statuses.contains(TesterStatus.REVOKED))
    }

    @Test
    fun testRepositoryRegisterAndJourneyTracking() = runTest {
        val fakeRepo = FakeTesterDistributionRepository()
        
        // 1. Register Session
        val registerResult = fakeRepo.registerTesterSession(
            testerId = "tester_123",
            email = "tester@siraj.app",
            name = "مختبر تجريبي",
            deviceModel = "Samsung S24",
            osVersion = "Android 14",
            appVersion = "1.0.0-beta.1",
            buildCode = 100
        )
        assertTrue(registerResult.isSuccess)
        assertEquals("tester_123", fakeRepo.registeredTesterId)

        // 2. Complete a Critical Journey
        val journeyResult = fakeRepo.recordJourneyCompletion("tester_123", "journey_ideation_to_video")
        assertTrue(journeyResult.isSuccess)
        assertEquals(Pair("tester_123", "journey_ideation_to_video"), fakeRepo.lastCompletedJourney)

        // 3. Submit Survey
        val survey = TesterExperienceSurvey(
            id = "srv_1",
            testerId = "tester_123",
            testerEmail = "tester@siraj.app",
            overallRating = 5,
            easeOfUseRating = 5,
            shariaContentRating = 5,
            performanceRating = 4,
            mostValuableFeature = "المحراب والبحث القرآني",
            biggestPainPoint = "تصدير الفيديو يستغرق وقتاً",
            generalSuggestions = "إضافة مزيد من أصوات التلاوة"
        )
        val surveyResult = fakeRepo.submitExperienceSurvey(survey)
        assertTrue(surveyResult.isSuccess)
        assertEquals("srv_1", surveyResult.getOrNull())
        assertEquals("srv_1", fakeRepo.lastSubmittedSurvey?.id)
        assertEquals(5, fakeRepo.lastSubmittedSurvey?.overallRating)

        // 4. Revoke access
        val revokeResult = fakeRepo.revokeTesterAccess("tester_123", "User opted out")
        assertTrue(revokeResult.isSuccess)
        assertEquals("tester_123", fakeRepo.revokedTesterId)
    }

    @Test
    fun testDistributionChannelsAndReleaseNotesInvariants() {
        val fakeRepo = FakeTesterDistributionRepository()
        val journeys = fakeRepo.getCriticalJourneys()
        val notes = fakeRepo.getReleaseNotes()
        val channels = fakeRepo.getDistributionChannels()

        assertFalse(journeys.isEmpty())
        assertEquals("journey_ideation_to_video", journeys.first().id)
        assertFalse(notes.isEmpty())
        assertEquals("1.0.0-beta.1", notes.first().versionName)
        assertFalse(channels.isEmpty())
        assertEquals("Android", channels.first().platform)
    }
}
