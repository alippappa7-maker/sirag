package com.siraj.app.features.review

import com.siraj.app.core.utils.Resource
import com.siraj.app.data.repository.review.FirebaseShariaReviewRepositoryImpl
import com.siraj.app.domain.models.review.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ShariaReviewTest {
    private lateinit var repository: FirebaseShariaReviewRepositoryImpl

    @Before
    fun setUp() {
        repository = FirebaseShariaReviewRepositoryImpl()
    }

    private suspend fun getItem(id: String): ShariaReviewItem {
        val res = repository.getReviewItemById(id).first()
        assertTrue("Expected Success but got $res", res is Resource.Success)
        return (res as Resource.Success).data!!
    }

    @Test
    fun testGetReviewQueue_ReturnsDefaultPreSeededItems() =
        runTest {
            val queueResult = repository.getReviewQueue(ShariaReviewFilter()).first()
            assertTrue(queueResult is Resource.Success)
            val items = (queueResult as Resource.Success).data
            assertNotNull(items)
            assertTrue(items!!.isNotEmpty())
        }

    @Test
    fun testFilterQueue_ByRiskLevel() =
        runTest {
            val criticalFilter = ShariaReviewFilter(riskLevel = RiskLevel.CRITICAL)
            val result = repository.getReviewQueue(criticalFilter).first()
            val items = (result as Resource.Success).data
            assertNotNull(items)
            assertTrue(items!!.all { it.riskLevel == RiskLevel.CRITICAL })
        }

    @Test
    fun testRule_CreatorCannotReviewOwnContent() =
        runTest {
            val item = getItem("review_item_001")
            val creatorId = item.creatorId

            // Attempting to claim/review own content must fail
            val claimResult =
                repository.claimReview(
                    itemId = item.id,
                    reviewerId = creatorId,
                    reviewerName = "صانع المحتوى",
                )
            assertTrue(claimResult is Resource.Error)
            assertEquals("لا يحق لصانع المحتوى مراجعة أو حجز محتواه الخاص", (claimResult as Resource.Error).message)

            // Attempting to approve own content must also fail
            val approveResult =
                repository.approveItem(
                    itemId = item.id,
                    reviewerId = creatorId,
                    reviewerName = "صانع المحتوى",
                    reason = "موافق",
                )
            assertTrue(approveResult is Resource.Error)
        }

    @Test
    fun testStandardApproval_LowRiskContent() =
        runTest {
            val itemId = "review_item_001" // Low risk hadith item
            val reviewerId = "rev_independent_1"
            val reviewerName = "د. محمد المستقل"

            val approveResult =
                repository.approveItem(
                    itemId = itemId,
                    reviewerId = reviewerId,
                    reviewerName = reviewerName,
                    reason = "تم التحقق من صحة الحديث وتخريجه من صحيح البخاري",
                )
            assertTrue(approveResult is Resource.Success)

            val updatedItem = getItem(itemId)
            assertEquals(ShariaReviewStatus.APPROVED, updatedItem.status)
            assertFalse(updatedItem.isDualApprovalRequired)
            assertEquals(reviewerId, updatedItem.decision?.primaryReviewerId)
        }

    @Test
    fun testCriticalTopic_RequiresDualApproval() =
        runTest {
            val itemId = "review_item_003" // High risk creed / fatwa content
            val reviewerId = "rev_primary_scholar"
            val reviewerName = "الشيخ عبد الله (المراجع الأول)"

            val approveResult =
                repository.approveItem(
                    itemId = itemId,
                    reviewerId = reviewerId,
                    reviewerName = reviewerName,
                    reason = "موافقة أولية على صحة النقول العقدية",
                )
            assertTrue(approveResult is Resource.Success)

            val updatedItem = getItem(itemId)
            // Should transition to DUAL_APPROVAL_PENDING because it's a critical topic
            assertEquals(ShariaReviewStatus.DUAL_APPROVAL_PENDING, updatedItem.status)
            assertTrue(updatedItem.isDualApprovalRequired)
            assertEquals(reviewerId, updatedItem.decision?.primaryReviewerId)

            // Now, second reviewer co-signs
            val secondReviewerId = "rev_expert_creed"
            val secondReviewerName = "د. طارق السلمان"
            val secondReviewResult =
                repository.submitSecondReviewDecision(
                    itemId = itemId,
                    secondReviewerId = secondReviewerId,
                    secondReviewerName = secondReviewerName,
                    approve = true,
                    reason = "تم التدقيق والتوقيع المشترك",
                )
            assertTrue(secondReviewResult is Resource.Success)

            val finalizedItem = getItem(itemId)
            assertEquals(ShariaReviewStatus.APPROVED, finalizedItem.status)
            assertEquals(secondReviewerId, finalizedItem.decision?.secondReviewerId)
        }

    @Test
    fun testRejection_WithReason() =
        runTest {
            val itemId = "review_item_001"
            val reviewerId = "rev_strict_scholar"
            val reviewerName = "الشيخ فهد"

            val rejectResult =
                repository.rejectItem(
                    itemId = itemId,
                    reviewerId = reviewerId,
                    reviewerName = reviewerName,
                    reason = "المحتوى يحتوي على تأويل غير معتمد ومخالف لإجماع المفسرين",
                )
            assertTrue(rejectResult is Resource.Success)

            val updatedItem = getItem(itemId)
            assertEquals(ShariaReviewStatus.REJECTED, updatedItem.status)
        }

    @Test
    fun testRequestChanges_UpdatesStatusAndLogs() =
        runTest {
            val itemId = "review_item_001"
            val reviewerId = "rev_reviewer_1"
            val reviewerName = "الشيخ أحمد"

            val res =
                repository.requestChanges(
                    itemId = itemId,
                    reviewerId = reviewerId,
                    reviewerName = reviewerName,
                    requiredChanges = "يرجى تعديل صياغة الحكم الشرعي في المشهد الأول",
                )
            assertTrue(res is Resource.Success)

            val updatedItem = getItem(itemId)
            assertEquals(ShariaReviewStatus.CHANGES_REQUESTED, updatedItem.status)
        }

    @Test
    fun testRule_ModifyingApprovedContent_ResetsToPendingReview() =
        runTest {
            // First approve review_item_001
            repository.approveItem(
                itemId = "review_item_001",
                reviewerId = "rev_scholar",
                reviewerName = "المراجع",
                reason = "معتمد",
            )
            var item = getItem("review_item_001")
            assertEquals(ShariaReviewStatus.APPROVED, item.status)

            // Creator edits content
            val modifyResult =
                repository.notifyContentModified(
                    itemId = "review_item_001",
                    editorId = item.creatorId,
                    editorName = item.creatorName,
                    summary = "تحديث صياغة الجملة الأخيرة",
                    newFullText = "نص معدل جديد تم تحديثه بعد الاعتماد",
                )
            assertTrue(modifyResult is Resource.Success)

            // Must now be reset to PENDING and version increased
            item = getItem("review_item_001")
            assertEquals(ShariaReviewStatus.PENDING, item.status)
            assertEquals(2, item.contentVersion)
            assertTrue(item.revisions.any { it.changeSummary == "تحديث صياغة الجملة الأخيرة" })
        }

    @Test
    fun testInternalNotesAndAuditLogs() =
        runTest {
            val itemId = "review_item_001"
            val addNoteResult =
                repository.addInternalNote(
                    itemId = itemId,
                    authorId = "rev_scholar",
                    authorName = "د. خالد",
                    note = "ملاحظة سرية: المصدر يحتاج متابعة في الطبعة الثانية",
                )
            assertTrue(addNoteResult is Resource.Success)

            val item = getItem(itemId)
            assertTrue(item.internalNotes.any { it.noteText.contains("ملاحظة سرية") })
            assertTrue(item.auditLogs.isNotEmpty())
        }
}
