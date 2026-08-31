package com.siraj.app.features.review

import com.siraj.app.domain.models.governance.*
import com.siraj.app.domain.models.review.CriticalTopic
import com.siraj.app.domain.models.review.RiskLevel
import com.siraj.app.features.review.domain.ReviewerGovernanceEngine
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ReviewerGovernanceTest {
    private lateinit var activeFiqhReviewer: ReviewerProfile
    private lateinit var pendingReviewer: ReviewerProfile
    private lateinit var suspendedReviewer: ReviewerProfile

    @Before
    fun setUp() {
        val now = System.currentTimeMillis()
        activeFiqhReviewer =
            ReviewerProfile(
                id = "rev_fiqh_01",
                displayName = "د. عبد الله الفقهي",
                email = "dr.abdullah@sharia.org",
                organization = "مجمع الفقه الإسلامي",
                qualifications =
                    listOf(
                        ReviewerQualification(
                            degreeTitle = "دكتوراه في الفقه المقارن",
                            institution = "جامعة الأزهر",
                            graduationYear = 2015,
                            isVerified = true,
                            verifiedByOwnerId = "owner_admin",
                            verifiedAt = now - 100000,
                        ),
                    ),
                specialties = setOf(ReviewerDomain.FIQH, ReviewerDomain.GENERAL),
                scope =
                    ReviewerScope(
                        allowedDomains = setOf(ReviewerDomain.FIQH, ReviewerDomain.GENERAL),
                        excludedTopics = setOf(CriticalTopic.TAKFIER),
                        maxRiskLevelAllowed = RiskLevel.CRITICAL,
                        canBePrimaryReviewer = true,
                        canBeSecondReviewer = true,
                    ),
                status = ReviewerStatus.ACTIVE,
                verifiedByOwnerId = "owner_admin",
                verificationDate = now - 100000,
                nextReverificationDue = now + 10000000L,
            )

        pendingReviewer =
            activeFiqhReviewer.copy(
                id = "rev_pending_01",
                status = ReviewerStatus.PENDING_VERIFICATION,
                verifiedByOwnerId = null,
            )

        suspendedReviewer =
            activeFiqhReviewer.copy(
                id = "rev_suspended_01",
                status = ReviewerStatus.SUSPENDED,
            )
    }

    @Test
    fun testReviewerMustBeActiveAndVerifiedByOwner() {
        val pendingResult =
            ReviewerGovernanceEngine.validateReviewerEligibility(
                reviewer = pendingReviewer,
                domain = ReviewerDomain.FIQH,
                riskLevel = RiskLevel.LOW,
                criticalTopic = CriticalTopic.NONE,
            )
        assertTrue(pendingResult is ReviewerGovernanceEngine.EligibilityResult.Ineligible)

        val suspendedResult =
            ReviewerGovernanceEngine.validateReviewerEligibility(
                reviewer = suspendedReviewer,
                domain = ReviewerDomain.FIQH,
                riskLevel = RiskLevel.LOW,
                criticalTopic = CriticalTopic.NONE,
            )
        assertTrue(suspendedResult is ReviewerGovernanceEngine.EligibilityResult.Ineligible)

        val activeResult =
            ReviewerGovernanceEngine.validateReviewerEligibility(
                reviewer = activeFiqhReviewer,
                domain = ReviewerDomain.FIQH,
                riskLevel = RiskLevel.LOW,
                criticalTopic = CriticalTopic.NONE,
            )
        assertTrue(activeResult is ReviewerGovernanceEngine.EligibilityResult.Eligible)
    }

    @Test
    fun testDomainScopeRestriction() {
        // مراجع الفقه لا يمكنه مراجعة الحديث إذا لم يكن في نطاقه
        val hadithResult =
            ReviewerGovernanceEngine.validateReviewerEligibility(
                reviewer = activeFiqhReviewer,
                domain = ReviewerDomain.HADITH,
                riskLevel = RiskLevel.LOW,
                criticalTopic = CriticalTopic.NONE,
            )
        assertTrue(hadithResult is ReviewerGovernanceEngine.EligibilityResult.Ineligible)

        // مراجع الفقه يمكنه مراجعة الفقه
        val fiqhResult =
            ReviewerGovernanceEngine.validateReviewerEligibility(
                reviewer = activeFiqhReviewer,
                domain = ReviewerDomain.FIQH,
                riskLevel = RiskLevel.LOW,
                criticalTopic = CriticalTopic.NONE,
            )
        assertTrue(fiqhResult is ReviewerGovernanceEngine.EligibilityResult.Eligible)
    }

    @Test
    fun testExcludedCriticalTopics() {
        // تم استثناء التكفير من نطاق هذا المراجع
        val takfierResult =
            ReviewerGovernanceEngine.validateReviewerEligibility(
                reviewer = activeFiqhReviewer,
                domain = ReviewerDomain.FIQH,
                riskLevel = RiskLevel.CRITICAL,
                criticalTopic = CriticalTopic.TAKFIER,
            )
        assertTrue(takfierResult is ReviewerGovernanceEngine.EligibilityResult.Ineligible)

        // موضوع حرج آخر غير مستثنى مثل الفتوى
        val fatwaResult =
            ReviewerGovernanceEngine.validateReviewerEligibility(
                reviewer = activeFiqhReviewer,
                domain = ReviewerDomain.FIQH,
                riskLevel = RiskLevel.HIGH,
                criticalTopic = CriticalTopic.FATWA,
            )
        assertTrue(fatwaResult is ReviewerGovernanceEngine.EligibilityResult.Eligible)
    }

    @Test
    fun testConflictOfInterest_CreatorCannotReviewOwnContent() {
        val conflictResult =
            ReviewerGovernanceEngine.checkConflictOfInterest(
                reviewerId = "creator_user_123",
                creatorId = "creator_user_123",
            )
        assertTrue(conflictResult is ReviewerGovernanceEngine.ConflictCheckResult.ConflictDetected)
        assertEquals(
            ConflictType.OWN_CONTENT,
            (conflictResult as ReviewerGovernanceEngine.ConflictCheckResult.ConflictDetected).conflictType,
        )
    }

    @Test
    fun testConflictOfInterest_RecordedConflictBlocksReview() {
        val recordedConflicts =
            listOf(
                ReviewerConflict(
                    reviewerId = "rev_fiqh_01",
                    creatorId = "company_abc",
                    conflictType = ConflictType.ORGANIZATIONAL,
                    reason = "عضو مجلس إدارة في الشركة المنتجة",
                    isRestricted = true,
                ),
            )

        val conflictResult =
            ReviewerGovernanceEngine.checkConflictOfInterest(
                reviewerId = "rev_fiqh_01",
                creatorId = "company_abc",
                recordedConflicts = recordedConflicts,
            )
        assertTrue(conflictResult is ReviewerGovernanceEngine.ConflictCheckResult.ConflictDetected)
        assertEquals(
            ConflictType.ORGANIZATIONAL,
            (conflictResult as ReviewerGovernanceEngine.ConflictCheckResult.ConflictDetected).conflictType,
        )
    }

    @Test
    fun testSecondReviewerRequiredForCriticalTopics() {
        assertTrue(ReviewerGovernanceEngine.isSecondReviewRequired(RiskLevel.CRITICAL, CriticalTopic.NONE))
        assertTrue(ReviewerGovernanceEngine.isSecondReviewRequired(RiskLevel.LOW, CriticalTopic.DIVORCE))
        assertTrue(ReviewerGovernanceEngine.isSecondReviewRequired(RiskLevel.HIGH, CriticalTopic.FATWA))
        assertFalse(ReviewerGovernanceEngine.isSecondReviewRequired(RiskLevel.LOW, CriticalTopic.NONE))
    }

    @Test
    fun testDecisionImmutabilityAndVersionTracking() {
        val assignment =
            ReviewerAssignment(
                id = "assign_100",
                itemId = "item_100",
                contentTitle = "فيديو فقهي",
                contentVersion = 2,
                domain = ReviewerDomain.FIQH,
                riskLevel = RiskLevel.MEDIUM,
                primaryReviewerId = activeFiqhReviewer.id,
                primaryReviewerName = activeFiqhReviewer.displayName,
                assignedByOwnerId = "owner_admin",
            )

        val decision1 =
            ReviewerGovernanceEngine.createImmutableDecision(
                assignment = assignment,
                reviewer = activeFiqhReviewer,
                isSecondReviewer = false,
                outcome = DecisionOutcome.CHANGES_REQUESTED,
                notes = "الحديث الوارد في المشهد الثاني يحتاج تخريجاً دقيقاً",
                evidences = listOf("صحيح مسلم"),
            )

        assertEquals(2, decision1.contentVersion)
        assertTrue(decision1.isImmutable)
        assertNull(decision1.supersedesDecisionId)

        // قرار لاحق بعد التعديل
        val decision2 =
            ReviewerGovernanceEngine.createImmutableDecision(
                assignment = assignment.copy(contentVersion = 3),
                reviewer = activeFiqhReviewer,
                isSecondReviewer = false,
                outcome = DecisionOutcome.APPROVED,
                notes = "تم تصحيح التخريج بنجاح",
                supersedesDecisionId = decision1.decisionId,
            )

        assertEquals(3, decision2.contentVersion)
        assertEquals(decision1.decisionId, decision2.supersedesDecisionId)
        assertNotEquals(decision1.decisionId, decision2.decisionId)
    }
}
