package com.siraj.app.features.minor

import com.siraj.app.core.utils.Resource
import com.siraj.app.data.repository.minor.MinorSafetyRepositoryImpl
import com.siraj.app.domain.models.minor.*
import com.siraj.app.features.minor.domain.MinorAction
import com.siraj.app.features.minor.domain.MinorSafetyEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MinorSafetyTest {
    private lateinit var repository: MinorSafetyRepositoryImpl

    @Before
    fun setup() {
        repository = MinorSafetyRepositoryImpl()
    }

    @Test
    fun testPolicyForAdultEnforcesSirajZeroLocationAndNoDMs() {
        val policy =
            MinorSafetyEngine.generatePolicyForAgeBracket(
                userId = "user_adult_01",
                ageBracket = UserAgeBracket.ADULT_18_PLUS,
            )

        assertFalse("Minor protection should be inactive for adults", policy.isMinorProtectionActive)
        assertFalse("Adults are not private by default", policy.isPrivateByDefault)
        assertTrue("Direct messages are disabled platform-wide in Siraj", policy.blockDirectMessages)
        assertTrue("Fine location GPS collection is disabled", policy.disableFineLocation)
        assertTrue("Personalized ads are disabled", policy.disablePersonalizedAds)
        assertTrue("AI model training on personal data is prohibited", policy.disableModelTrainingOnData)
        assertFalse("Parental approval not required for adult publishing", policy.requireParentalApprovalForPublishing)
    }

    @Test
    fun testPolicyForTeenEnforcesPrivateByDefaultAndGuardrails() {
        val policy =
            MinorSafetyEngine.generatePolicyForAgeBracket(
                userId = "teen_01",
                ageBracket = UserAgeBracket.TEEN_13_TO_17,
                guardianEmail = "parent@example.com",
            )

        assertTrue("Minor protection must be active for teens", policy.isMinorProtectionActive)
        assertTrue("Teens must be private by default", policy.isPrivateByDefault)
        assertTrue("Direct messages must be blocked", policy.blockDirectMessages)
        assertTrue("Fine location must be disabled", policy.disableFineLocation)
        assertTrue("Personalized tracking/ads must be disabled", policy.disablePersonalizedAds)
        assertTrue("Model training on teen data is strictly prohibited", policy.disableModelTrainingOnData)
        assertTrue("Voice cloning of minors is blocked", policy.blockVoiceCloning)
        assertTrue("Hidden from public discovery", policy.hideFromPublicDiscovery)
    }

    @Test
    fun testPolicyForChildUnder13RequiresParentalConsent() {
        val unverifiedPolicy =
            MinorSafetyEngine.generatePolicyForAgeBracket(
                userId = "child_01",
                ageBracket = UserAgeBracket.CHILD_UNDER_13,
                guardianEmail = "father@example.com",
                isParentalConsentVerified = false,
            )

        assertTrue(unverifiedPolicy.isMinorProtectionActive)
        assertTrue(unverifiedPolicy.requireParentalApprovalForPublishing)
        assertTrue(unverifiedPolicy.requireParentalConsentForAiFeatures)
        assertTrue(unverifiedPolicy.allowOnlyCuratedEducationalContent)

        val (canPublish, _) = MinorSafetyEngine.canPerformAction(unverifiedPolicy, MinorAction.PUBLISH_CONTENT_PUBLICLY)
        assertFalse("Unverified child cannot publish publicly", canPublish)

        val (canUseAi, _) = MinorSafetyEngine.canPerformAction(unverifiedPolicy, MinorAction.USE_AI_GENERATION)
        assertFalse("Unverified child cannot use AI generation without parental consent", canUseAi)

        // Verified policy
        val verifiedPolicy = unverifiedPolicy.copy(isParentalConsentVerified = true)
        val (canPublishVerified, _) = MinorSafetyEngine.canPerformAction(verifiedPolicy, MinorAction.PUBLISH_CONTENT_PUBLICLY)
        assertTrue("Child with verified parental consent can publish under supervision", canPublishVerified)

        val (canUseAiVerified, _) = MinorSafetyEngine.canPerformAction(verifiedPolicy, MinorAction.USE_AI_GENERATION)
        assertTrue("Child with verified parental consent can use safe educational AI", canUseAiVerified)
    }

    @Test
    fun testUnspecifiedAgeDefaultsToMaximumSafeMode() {
        val policy =
            MinorSafetyEngine.generatePolicyForAgeBracket(
                userId = "unspecified_user",
                ageBracket = UserAgeBracket.UNSPECIFIED,
            )

        assertTrue(policy.isMinorProtectionActive)
        assertTrue(policy.isPrivateByDefault)
        assertTrue(policy.requireParentalConsentForAiFeatures)
        assertTrue(policy.allowOnlyCuratedEducationalContent)
    }

    @Test
    fun testProhibitionOfDirectMessagesAndStrangerInvites() {
        val childPolicy = MinorSafetyEngine.generatePolicyForAgeBracket("child_02", UserAgeBracket.CHILD_UNDER_13)

        val (canDm, msgDm) = MinorSafetyEngine.canPerformAction(childPolicy, MinorAction.SEND_DIRECT_MESSAGE)
        assertFalse(canDm)
        assertTrue(msgDm.contains("معطلة"))

        val (canInvite, msgInvite) = MinorSafetyEngine.canPerformAction(childPolicy, MinorAction.RECEIVE_STRANGER_INVITE)
        assertFalse(canInvite)
        assertTrue(msgInvite.contains("محمي"))

        val (canBuy, msgBuy) = MinorSafetyEngine.canPerformAction(childPolicy, MinorAction.PURCHASE_SUBSCRIPTION)
        assertFalse(canBuy)
        assertTrue(msgBuy.contains("محظورة على حسابات القاصرين"))
    }

    @Test
    fun testEmergencyChildSafetyReportTriageAndEscalation() {
        val initialReport =
            ChildSafetyIncidentReport(
                incidentType = ChildSafetyIncidentType.EXPLOITATION_OR_ABUSE,
                reporterUserId = "concerned_user",
                description = "محاولة استدراج مشبوهة في مساحة عمل مشتركة.",
            )

        val escalated = MinorSafetyEngine.triageAndEscalateIncident(initialReport)

        assertEquals(IncidentUrgency.CRITICAL_EMERGENCY, escalated.urgency)
        assertEquals(IncidentResolutionStatus.OPEN_ESCALATED, escalated.status)
        assertEquals(15, escalated.urgency.maxResponseSlaMinutes)
        assertTrue(escalated.slaDeadlineTimestamp > System.currentTimeMillis())
        assertTrue(escalated.internalNotes.contains("ESCALATION_ENGINE"))
    }

    @Test
    fun testParentalConsentFlowAndOtpVerification() =
        runBlocking {
            val requestRes =
                repository.requestParentalConsent(
                    childUserId = "child_test_99",
                    guardianEmail = "parent_test@example.com",
                    guardianName = "ولي الأمر التجريبي",
                )

            assertTrue(requestRes is Resource.Success)
            val consentRecord = (requestRes as Resource.Success).data
            assertEquals(ParentalConsentStatus.PENDING_VERIFICATION, consentRecord.status)
            assertEquals("parent_test@example.com", consentRecord.guardianEmail)

            // Verify with invalid code
            val invalidVerify = repository.verifyParentalConsent(consentRecord.consentId, "999999")
            assertTrue(invalidVerify is Resource.Error)

            // Verify with valid code (123456)
            val validVerify = repository.verifyParentalConsent(consentRecord.consentId, "123456")
            assertTrue(validVerify is Resource.Success)
            val validData = (validVerify as Resource.Success).data
            assertEquals(ParentalConsentStatus.APPROVED_VERIFIED, validData.status)
            assertNotNull(validData.verifiedAt)
        }

    @Test
    fun testMinorDataPurgeAndRightToErasure() =
        runBlocking {
            val purgeRes = repository.purgeMinorData("child_user_sample")
            assertTrue(purgeRes is Resource.Success)
            val summary = (purgeRes as Resource.Success).data

            assertEquals("child_user_sample", summary.childUserId)
            assertTrue(summary.deletedRecordingsCount > 0)
            assertTrue(summary.deletedProjectsCount > 0)
            assertTrue(summary.deletedProfileData)
            assertTrue(summary.deletedActivityLogs)
            assertTrue(summary.confirmationReceiptHash.isNotEmpty())
        }

    @Test
    fun testEducationalContentSafetyAuditor() {
        val safeCheck =
            repository.evaluateEducationalContent(
                contentId = "c1",
                title = "قصص الأنبياء للأطفال: قصة يوسف عليه السلام",
                textSnippet = "محتوى قصصي تربوي ميسر يغرس الصبر والتوكل والأخلاق الفاضلة.",
            )

        assertTrue(safeCheck.isChildSafe)
        assertEquals(1.0f, safeCheck.audioVisualSafetyScore, 0.01f)
        assertFalse(safeCheck.hasDeceptiveSubscriptionTriggers)
        assertFalse(safeCheck.hasViolentOrFrighteningElements)

        val unsafeCheck =
            repository.evaluateEducationalContent(
                contentId = "c2",
                title = "ألعاب ومغامرات",
                textSnippet = "اشترك الآن وادفع للحصول على أسلحة وعنف خفيف.",
            )

        assertFalse(unsafeCheck.isChildSafe)
        assertTrue(unsafeCheck.hasDeceptiveSubscriptionTriggers || unsafeCheck.hasViolentOrFrighteningElements)
    }
}
