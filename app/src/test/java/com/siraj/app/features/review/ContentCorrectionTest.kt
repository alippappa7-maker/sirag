package com.siraj.app.features.review

import com.siraj.app.core.utils.Resource
import com.siraj.app.data.repository.review.ContentCorrectionRepositoryImpl
import com.siraj.app.domain.models.Source
import com.siraj.app.domain.models.SourceType
import com.siraj.app.domain.models.SourceVerificationStatus
import com.siraj.app.domain.models.correction.*
import com.siraj.app.domain.models.review.ShariaClaim
import com.siraj.app.domain.models.review.ShariaReviewStatus
import com.siraj.app.features.review.domain.ContentCorrectionEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ContentCorrectionTest {

    private lateinit var sampleV1: ContentVersion

    @Before
    fun setup() {
        sampleV1 = ContentVersion(
            id = "ver_v1",
            contentId = "proj_101",
            versionNumber = 1,
            title = "فضل طلب العلم",
            fullContentText = "من سلك طريقا يلتمس فيه علما سهل الله له به طريقا إلى الجنة",
            claims = listOf(
                ShariaClaim(
                    id = "claim_1",
                    claimText = "من سلك طريقا...",
                    positionContext = "المشهد 1",
                    sourceType = "HADITH",
                    sourceTitle = "صحيح مسلم",
                    sourceReference = "2699",
                    originalSourceText = "من سلك طريقا يلتمس فيه علما",
                    isVerified = true
                )
            ),
            sources = listOf(
                Source(
                    id = "src_1",
                    type = SourceType.HADITH,
                    title = "صحيح مسلم",
                    reference = "2699",
                    reviewStatus = SourceVerificationStatus.VERIFIED
                )
            ),
            status = VersionStatus.ACTIVE_PUBLISHED,
            createdBy = "creator_1",
            createdByName = "أحمد",
            createdAt = 1000L,
            publishedAt = 1000L
        )
    }

    @Test
    fun testCreateCorrectionDraft_createsNewVersionWithoutMutatingOldVersion() {
        val draftResult = ContentCorrectionEngine.createCorrectionDraft(
            currentVersion = sampleV1,
            correctionType = CorrectionType.SOURCE_ERROR,
            reason = "تحديد رقم الحديث بدقة",
            detailedExplanation = "تم تعديل رقم الصفحة وإضافة تخريج البخاري أيضاً",
            discoveredBy = "د. خالد",
            discoveredByType = DiscoveredByType.REVIEWER_AUDIT,
            correctedTitle = "فضل طلب العلم في الكتاب والسنة",
            correctedFullContentText = "من سلك طريقاً يلتمس فيه علماً سهّل الله له به طريقاً إلى الجنة",
            correctedClaims = sampleV1.claims,
            correctedSources = sampleV1.sources,
            sourceRevisions = listOf(
                SourceRevision(
                    correctionNoticeId = "",
                    originalSourceId = "src_1",
                    originalSourceTitle = "صحيح مسلم",
                    originalReference = "2699",
                    originalText = "...",
                    correctedSourceTitle = "صحيح مسلم ورواه البخاري معلقاً",
                    correctedReference = "كتاب العلم، رقم 2699",
                    correctedText = "...",
                    correctionReason = "إتمام التخريج"
                )
            ),
            affectedAssets = listOf(
                AffectedAsset(
                    contentId = sampleV1.contentId,
                    correctionNoticeId = "",
                    projectId = "proj_101",
                    projectTitle = sampleV1.title,
                    assetType = AffectedAssetType.SCENE,
                    assetName = "المشهد الأول",
                    impactDescription = "يحتوي على النص المراد تصحيحه",
                    remediationAction = "إعادة الرندرة"
                )
            ),
            createdBy = "creator_1",
            createdByName = "أحمد",
            changeSummary = "تحديث التخريج"
        )

        // 1. New version incremented and in review
        assertEquals(2, draftResult.newVersion.versionNumber)
        assertEquals(VersionStatus.IN_REVIEW, draftResult.newVersion.status)
        assertTrue(draftResult.newVersion.immutableHash.isNotBlank())

        // 2. Notice created properly
        assertEquals(1, draftResult.correctionNotice.fromVersionNumber)
        assertEquals(2, draftResult.correctionNotice.toVersionNumber)
        assertEquals(CorrectionType.SOURCE_ERROR, draftResult.correctionNotice.correctionType)
        assertEquals(ShariaReviewStatus.PENDING, draftResult.correctionNotice.status)

        // 3. Old version remains immutable in history
        assertEquals(1, sampleV1.versionNumber)
        assertEquals(VersionStatus.ACTIVE_PUBLISHED, sampleV1.status)
    }

    @Test
    fun testRightsIssue_triggersImmediateSuspension() {
        val draftResult = ContentCorrectionEngine.createCorrectionDraft(
            currentVersion = sampleV1,
            correctionType = CorrectionType.RIGHTS_ISSUE,
            reason = "انتهاء ترخيص الصورة التوضيحية",
            detailedExplanation = "طالب صاحب الصورة بإيقافها فورياً",
            discoveredBy = "النظام الآلي",
            discoveredByType = DiscoveredByType.SYSTEM_SCAN,
            correctedTitle = sampleV1.title,
            correctedFullContentText = sampleV1.fullContentText,
            correctedClaims = sampleV1.claims,
            correctedSources = sampleV1.sources,
            createdBy = "admin",
            createdByName = "المشرف",
            changeSummary = "استبدال الصورة المنتهية ترخيصها"
        )

        // Verifies immediate suspension
        assertTrue(draftResult.correctionNotice.isImmediateSuspensionApplied)
        assertEquals(VersionStatus.RESTRICTED_SUSPENDED, draftResult.updatedPreviousVersion.status)
        assertTrue(draftResult.updatedPreviousVersion.isRestricted)
        assertTrue(draftResult.updatedPreviousVersion.restrictionReason!!.contains("تم تعليق النشر فورياً"))
    }

    @Test
    fun testApprovalWorkflow_publishesNewVersionAndSupersedesOldVersion() {
        val draftResult = ContentCorrectionEngine.createCorrectionDraft(
            currentVersion = sampleV1,
            correctionType = CorrectionType.WORDING_ERROR,
            reason = "تصحيح حركة إعرابية",
            detailedExplanation = "تصحيح ضبط كلمة رضاً",
            discoveredBy = "مستخدم مهتم",
            discoveredByType = DiscoveredByType.USER_REPORT,
            correctedTitle = sampleV1.title,
            correctedFullContentText = sampleV1.fullContentText,
            correctedClaims = sampleV1.claims,
            correctedSources = sampleV1.sources,
            createdBy = "creator_1",
            createdByName = "أحمد",
            changeSummary = "ضبط الحركات"
        )

        val review = CorrectionReview(
            correctionNoticeId = draftResult.correctionNotice.id,
            fromVersionNumber = 1,
            toVersionNumber = 2,
            reviewerId = "rev_sharia_1",
            reviewerName = "الشيخ عبد الله",
            reviewerSpecialty = "اللغة والتفسير",
            status = ShariaReviewStatus.APPROVED,
            reviewerNotes = "تصحيح دقيق وصائب ومعتمد",
            shariaEvidences = listOf("معجم مقاييس اللغة"),
            isApproved = true
        )

        val approvalResult = ContentCorrectionEngine.approveAndPublishCorrection(
            notice = draftResult.correctionNotice,
            draftVersion = draftResult.newVersion,
            previousVersion = sampleV1,
            review = review,
            affectedAssets = draftResult.affectedAssets
        )

        // 1. New version is now active and published
        assertEquals(VersionStatus.ACTIVE_PUBLISHED, approvalResult.newPublishedVersion.status)
        assertNotNull(approvalResult.newPublishedVersion.publishedAt)

        // 2. Old version is superseded and retained in history
        assertEquals(VersionStatus.SUPERSEDED, approvalResult.supersededPreviousVersion.status)
        assertEquals(2, approvalResult.supersededPreviousVersion.supersededByVersion)
        assertNotNull(approvalResult.supersededPreviousVersion.supersededAt)

        // 3. Notice is approved and notification flagged
        assertEquals(ShariaReviewStatus.APPROVED, approvalResult.approvedNotice.status)
        assertTrue(approvalResult.approvedNotice.notificationSent)
    }

    @Test
    fun testRejectionWorkflow_retainsDraftAndLogsNotes() {
        val draftResult = ContentCorrectionEngine.createCorrectionDraft(
            currentVersion = sampleV1,
            correctionType = CorrectionType.ATTRIBUTION_ERROR,
            reason = "ادعاء عزو خاطئ",
            detailedExplanation = "محاولة تغيير العزو إلى مصدر آخر غير معتمد",
            discoveredBy = "محرر",
            discoveredByType = DiscoveredByType.CREATOR_SELF_DISCOVERY,
            correctedTitle = sampleV1.title,
            correctedFullContentText = sampleV1.fullContentText,
            correctedClaims = sampleV1.claims,
            correctedSources = sampleV1.sources,
            createdBy = "creator_1",
            createdByName = "أحمد",
            changeSummary = "تغيير عزو"
        )

        val review = CorrectionReview(
            correctionNoticeId = draftResult.correctionNotice.id,
            fromVersionNumber = 1,
            toVersionNumber = 2,
            reviewerId = "rev_sharia_2",
            reviewerName = "د. محمد",
            reviewerSpecialty = "الحديث الشريف",
            status = ShariaReviewStatus.REJECTED,
            reviewerNotes = "العزو المقترح غير دقيق والنسخة v1 هي الصحيحة",
            isApproved = false
        )

        val rejectionResult = ContentCorrectionEngine.rejectCorrection(
            notice = draftResult.correctionNotice,
            draftVersion = draftResult.newVersion,
            review = review
        )

        assertEquals(VersionStatus.DRAFT, rejectionResult.rejectedDraftVersion.status)
        assertEquals(ShariaReviewStatus.REJECTED, rejectionResult.rejectedNotice.status)
        assertTrue(rejectionResult.rejectedDraftVersion.changeSummary.contains("مرفوض شرعياً"))
    }

    @Test
    fun testGenerateImpactReport_calculatesStatisticsCorrectly() {
        val notice = CorrectionNotice(
            id = "not_1",
            contentId = "proj_101",
            fromVersionNumber = 1,
            toVersionNumber = 2,
            correctionType = CorrectionType.SOURCE_ERROR,
            reason = "تصحيح تخريج",
            detailedExplanation = "...",
            discoveredBy = "باحث",
            discoveredByType = DiscoveredByType.REVIEWER_AUDIT
        )

        val assets = listOf(
            AffectedAsset(
                contentId = "proj_101",
                correctionNoticeId = "not_1",
                projectId = "proj_101",
                projectTitle = "مشروع 1",
                assetType = AffectedAssetType.PROJECT,
                assetName = "مشروع فضل العلم",
                impactDescription = "مشروع رئيسي",
                remediationAction = "إعادة التصدير"
            ),
            AffectedAsset(
                contentId = "proj_101",
                correctionNoticeId = "not_1",
                projectId = "proj_101",
                projectTitle = "مشروع 1",
                assetType = AffectedAssetType.SCENE,
                assetName = "مشهد 1",
                impactDescription = "مشهد نصي",
                remediationAction = "تحديث"
            ),
            AffectedAsset(
                contentId = "proj_101",
                correctionNoticeId = "not_1",
                projectId = "proj_101",
                projectTitle = "مشروع 1",
                assetType = AffectedAssetType.VIDEO_RENDER,
                assetName = "فيديو كامل 1080p",
                impactDescription = "فيديو مرندر سابقاً",
                remediationAction = "إعادة رندرة"
            ),
            AffectedAsset(
                contentId = "proj_101",
                correctionNoticeId = "not_1",
                projectId = "proj_101",
                projectTitle = "مشروع 1",
                assetType = AffectedAssetType.PUBLISHED_FLASH,
                assetName = "ومضة دعوية",
                impactDescription = "منشورة في الموجز العام",
                remediationAction = "استبدال الفيديو"
            )
        )

        val report = ContentCorrectionEngine.generateImpactReport(notice, assets)

        assertEquals(4, report.totalAffectedAssetsCount)
        assertEquals(1, report.affectedProjectsCount)
        assertEquals(1, report.affectedScenesCount)
        assertEquals(1, report.affectedVideoRendersCount)
        assertEquals(1, report.affectedPublishedFlashesCount)
        assertTrue(report.summary.contains("تقرير حصر الأثر"))
    }

    @Test
    fun testRepositoryIntegration_fullLifecycle() = runTest {
        val repo = ContentCorrectionRepositoryImpl()

        // 1. Initial version exists
        val versions = repo.getContentVersions("proj_siraj_101").first()
        assertEquals(1, versions.size)
        assertEquals(1, versions.first().versionNumber)

        // 2. Create correction
        val draftResult = ContentCorrectionEngine.createCorrectionDraft(
            currentVersion = versions.first(),
            correctionType = CorrectionType.SOURCE_ERROR,
            reason = "تحديث سند",
            detailedExplanation = "توضيح أن الحديث متفق عليه بألفاظ متقاربة",
            discoveredBy = "فريق التدقيق",
            discoveredByType = DiscoveredByType.REVIEWER_AUDIT,
            correctedTitle = "فضل طلب العلم",
            correctedFullContentText = versions.first().fullContentText,
            correctedClaims = versions.first().claims,
            correctedSources = versions.first().sources,
            createdBy = "creator_1",
            createdByName = "أحمد",
            changeSummary = "تحديث السند"
        )

        val createRes = repo.createCorrection(draftResult)
        assertTrue(createRes is Resource.Success)

        // 3. Now 2 versions exist
        val updatedVersions = repo.getContentVersions("proj_siraj_101").first()
        assertEquals(2, updatedVersions.size)
        assertEquals(2, updatedVersions.first().versionNumber) // Sorted descending

        // 4. Submit review approval
        val review = CorrectionReview(
            correctionNoticeId = draftResult.correctionNotice.id,
            fromVersionNumber = 1,
            toVersionNumber = 2,
            reviewerId = "rev_1",
            reviewerName = "المراجع",
            reviewerSpecialty = "الحديث",
            status = ShariaReviewStatus.APPROVED,
            reviewerNotes = "معتمد",
            isApproved = true
        )

        val reviewRes = repo.submitReview(draftResult.correctionNotice.id, review)
        assertTrue(reviewRes is Resource.Success)

        // 5. Check active version is v2 and superseded is v1
        val finalVersions = repo.getContentVersions("proj_siraj_101").first()
        val v2 = finalVersions.find { it.versionNumber == 2 }!!
        val v1 = finalVersions.find { it.versionNumber == 1 }!!

        assertEquals(VersionStatus.ACTIVE_PUBLISHED, v2.status)
        assertEquals(VersionStatus.SUPERSEDED, v1.status)
        assertEquals(2, v1.supersededByVersion)
    }
}
