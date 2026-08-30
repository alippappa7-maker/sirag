package com.siraj.app.features.moderation

import com.siraj.app.core.utils.Resource
import com.siraj.app.data.repository.community.FirebaseSafetyRepositoryImpl
import com.siraj.app.domain.models.community.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UgcModerationTest {

    private lateinit var repository: FirebaseSafetyRepositoryImpl

    @Before
    fun setup() {
        repository = FirebaseSafetyRepositoryImpl()
    }

    @Test
    fun testPreUploadScan_detectsSpamAndRestricts() = runBlocking {
        val spamText = "اضغط هنا للربح السريع وربح ملايين الدولارات وسجل فورا"
        val result = repository.scanUgcContent("فيديو", spamText, "video/mp4", emptyList())

        assertTrue(result is Resource.Success)
        val scan = (result as Resource.Success).data!!
        assertTrue(scan.isSpam)
        assertFalse(scan.passedAutoFilter)
        assertEquals(UgcState.LIMITED, scan.recommendedState)
        assertTrue(scan.detectedFlags.any { it.contains("سبام") || it.contains("spam") || it.contains("احتيال") })
    }

    @Test
    fun testPreUploadScan_detectsHarmfulContentAndRejects() = runBlocking {
        val violentText = "مقطع يحتوي على كراهية وقتل وسفك دماء وتحريض"
        val result = repository.scanUgcContent("فيديو", violentText, "video/mp4", emptyList())

        assertTrue(result is Resource.Success)
        val scan = (result as Resource.Success).data!!
        assertTrue(scan.hasHarmfulContent)
        assertFalse(scan.passedAutoFilter)
        assertEquals(UgcState.REJECTED, scan.recommendedState)
    }

    @Test
    fun testPreUploadScan_detectsReligiousContentAndRoutesToReview() = runBlocking {
        val fatwaText = "فتوى شرعية في حكم الصلاة وتفسير آية كريمة"
        val result = repository.scanUgcContent("مقال", fatwaText, "text/plain", emptyList())

        assertTrue(result is Resource.Success)
        val scan = (result as Resource.Success).data!!
        assertTrue(scan.hasReligiousSensitivity)
        assertTrue(scan.requiresHumanReview)
        assertEquals(UgcState.PENDING_REVIEW, scan.recommendedState)
    }

    @Test
    fun testPreUploadScan_cleanContentApproves() = runBlocking {
        val cleanText = "تسجيل مرئي هادئ لمناظر الطبيعة والجبال الخضراء"
        val result = repository.scanUgcContent("طبيعة", cleanText, "video/mp4", emptyList())

        assertTrue(result is Resource.Success)
        val scan = (result as Resource.Success).data!!
        assertTrue(scan.passedAutoFilter)
        assertEquals(UgcState.APPROVED, scan.recommendedState)
    }

    @Test
    fun testTermsOfServiceConsent_recordingAndCheck() = runBlocking {
        val userId = "user_test_99"
        val consentResult = repository.acceptTermsOfService(userId, "1.2.0")
        assertTrue(consentResult is Resource.Success)

        val checkResult = repository.hasAcceptedTerms(userId, "1.2.0")
        assertTrue(checkResult is Resource.Success)
        assertTrue((checkResult as Resource.Success).data == true)

        val checkOldVersion = repository.hasAcceptedTerms(userId, "2.0.0")
        assertTrue(checkOldVersion is Resource.Success)
        assertTrue((checkOldVersion as Resource.Success).data == false)
    }

    @Test
    fun testModeratorActionOnUgc_limitsAndLogsAudit() = runBlocking {
        val ugcListRes = repository.getUgcQueue("ADMIN", null)
        assertTrue(ugcListRes is Resource.Success)
        val firstItem = (ugcListRes as Resource.Success).data!!.first()

        val actionRes = repository.takeModeratorActionOnUgc(
            ugcId = firstItem.id,
            moderatorId = "mod_senior_1",
            action = ModeratorAction.LIMIT,
            notes = "تقييد الظهور بناء على بلاغات مستمرة"
        )
        assertTrue(actionRes is Resource.Success)

        val logsRes = repository.getAllModerationLogs()
        assertTrue(logsRes is Resource.Success)
        val logs = (logsRes as Resource.Success).data!!
        assertTrue(logs.any { it.targetId == firstItem.id && it.action.contains("LIMIT") })
    }

    @Test
    fun testAppealsWorkflow_submitAndApproveRestoresContent() = runBlocking {
        val userId = "creator_appeal_1"
        val ugcId = "ugc_sample_2" // limited item in mock

        // 1. Submit appeal
        val submitRes = repository.submitAppeal(
            ugcId = ugcId,
            ugcTitle = "مقطع قيد النزاع",
            userId = userId,
            originalReason = "اشتباه في حقوق الملكية",
            appealJustification = "أمتلك ترخيصاً كاملاً وحصرياً من صاحب العمل الأصلي"
        )
        assertTrue(submitRes is Resource.Success)
        val appeal = (submitRes as Resource.Success).data!!
        assertEquals(AppealStatus.PENDING, appeal.status)

        // 2. Moderator approves appeal
        val resolveRes = repository.resolveAppeal(
            appealId = appeal.id,
            moderatorId = "mod_lead",
            isApproved = true,
            notes = "تم الاطلاع على وثيقة الترخيص والموافقة"
        )
        assertTrue(resolveRes is Resource.Success)

        val appealsListRes = repository.getAppeals()
        assertTrue(appealsListRes is Resource.Success)
        val resolvedAppeal = (appealsListRes as Resource.Success).data!!.first { it.id == appeal.id }
        assertEquals(AppealStatus.APPROVED, resolvedAppeal.status)
    }

    @Test
    fun testSlaTargetHours_andOverdueCalculation() {
        val recentReport = Report(
            id = "rep_1",
            targetType = ReportTargetType.FLASH,
            targetId = "ugc_10",
            targetOwnerId = "author_1",
            reporterId = "rep_user",
            reportType = ReportType.SPAM,
            description = "محتوى مزعج",
            createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 2 // 2 hours ago
        )
        assertFalse(recentReport.isOverdue)
        assertTrue(recentReport.remainingHours in 21..22)

        val overdueReport = Report(
            id = "rep_2",
            targetType = ReportTargetType.FLASH,
            targetId = "ugc_11",
            targetOwnerId = "author_2",
            reporterId = "rep_user",
            reportType = ReportType.HARASSMENT,
            description = "مخالفة خطيرة",
            createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 30 // 30 hours ago (>24h)
        )
        assertTrue(overdueReport.isOverdue)
        assertEquals(0, overdueReport.remainingHours)
    }

    @Test
    fun testUserBlockingAndSuspension() = runBlocking {
        val suspendRes = repository.suspendUserAccount("bad_user_1", "mod_admin", "انتهاك متكرر", 7)
        assertTrue(suspendRes is Resource.Success)

        val blockRes = repository.blockUser("viewer_1", "bad_user_1")
        assertTrue(blockRes is Resource.Success)

        val blockedListRes = repository.getBlockedUsers("viewer_1")
        assertTrue(blockedListRes is Resource.Success)
        assertTrue((blockedListRes as Resource.Success).data!!.contains("bad_user_1"))
    }
}
