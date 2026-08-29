package com.siraj.app.features.beta.presentation

import com.siraj.app.data.repository.FirebaseBetaDefectManagementRepositoryImpl
import com.siraj.app.domain.models.beta.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DefectManagementUnitTest {

    private lateinit var repository: FirebaseBetaDefectManagementRepositoryImpl

    @Before
    fun setUp() {
        repository = FirebaseBetaDefectManagementRepositoryImpl()
    }

    @Test
    fun `initial defects list contains all 8 required classifications and default items`() = runTest {
        val defects = repository.getAllDefects().first()
        assertTrue("Defects list should not be empty", defects.isNotEmpty())
        assertTrue("Defects list should contain at least 8 items", defects.size >= 8)

        val classifications = defects.map { it.classification }.toSet()
        assertTrue("Should include BLOCKER", classifications.contains(DefectClassification.BLOCKER))
        assertTrue("Should include CRITICAL", classifications.contains(DefectClassification.CRITICAL))
        assertTrue("Should include MAJOR", classifications.contains(DefectClassification.MAJOR))
        assertTrue("Should include MINOR", classifications.contains(DefectClassification.MINOR))
        assertTrue("Should include ENHANCEMENT", classifications.contains(DefectClassification.ENHANCEMENT))
        assertTrue("Should include DUPLICATE", classifications.contains(DefectClassification.DUPLICATE))
        assertTrue("Should include NOT_REPRODUCIBLE", classifications.contains(DefectClassification.NOT_REPRODUCIBLE))
        assertTrue("Should include EXPECTED_BEHAVIOR", classifications.contains(DefectClassification.EXPECTED_BEHAVIOR))
    }

    @Test
    fun `triage summary accurately calculates counts across classifications and domains`() = runTest {
        val summary = repository.getTriageSummary().first()
        assertEquals(8, summary.totalCount)
        assertEquals(1, summary.blockerCount)
        assertEquals(1, summary.criticalCount)
        assertEquals(1, summary.majorCount)
        assertEquals(1, summary.minorCount)
        assertEquals(1, summary.enhancementCount)
        assertEquals(1, summary.duplicateCount)
        assertEquals(1, summary.notReproducibleCount)
        assertEquals(1, summary.expectedBehaviorCount)
        assertEquals(1, summary.shariaDomainCount)
        assertEquals(3, summary.closedCount) // DUPLICATE, NOT_REPRODUCIBLE, EXPECTED_BEHAVIOR are CLOSED
    }

    @Test
    fun `sharia domain defect cannot be downgraded to minor or enhancement`() = runTest {
        // BUG-001 is a Sharia domain defect (Qur'an aya diacritic)
        val result = repository.triageDefect(
            id = "BUG-001",
            classification = DefectClassification.MINOR,
            priority = DefectPriority.P3_LOW,
            assignedRole = "مطور الواجهات",
            targetRelease = "1.0.0-beta.2"
        )

        assertTrue(result.isSuccess)
        val updated = repository.getDefectById("BUG-001").first()
        assertNotNull(updated)
        // Rule: Sharia defects must remain at least CRITICAL and P1_HIGH / P0_IMMEDIATE
        assertTrue(
            "Sharia defect must be CRITICAL or BLOCKER",
            updated!!.classification == DefectClassification.CRITICAL || updated.classification == DefectClassification.BLOCKER
        )
        assertTrue(
            "Sharia defect priority cannot be LOW",
            updated.priority == DefectPriority.P0_IMMEDIATE || updated.priority == DefectPriority.P1_HIGH
        )
    }

    @Test
    fun `closing a defect without a valid closure reason fails`() = runTest {
        val result = repository.updateDefectStatus(
            id = "BUG-004", // Minor defect
            newStatus = DefectStatus.CLOSED,
            resolutionNote = null,
            closureReason = "", // Empty reason
            verificationTest = null
        )

        assertTrue("Closing without reason must fail", result.isFailure)
        assertEquals("لا يمكن إغلاق العيب دون تقديم سبب الإغلاق والتبرير الفني.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `closing a defect with a valid closure reason succeeds`() = runTest {
        val result = repository.updateDefectStatus(
            id = "BUG-004",
            newStatus = DefectStatus.CLOSED,
            resolutionNote = null,
            closureReason = "تم التحقق وتصحيح التباين اللوني وفق معايير WCAG 2.1 AA",
            verificationTest = "ColorContrastVerificationTest"
        )

        assertTrue("Closing with valid reason must succeed", result.isSuccess)
        val updated = repository.getDefectById("BUG-004").first()
        assertNotNull(updated)
        assertEquals(DefectStatus.CLOSED, updated!!.status)
        assertEquals("تم التحقق وتصحيح التباين اللوني وفق معايير WCAG 2.1 AA", updated.closureReason)
    }

    @Test
    fun `resolving critical defect without resolution note fails`() = runTest {
        val result = repository.updateDefectStatus(
            id = "BUG-001", // Critical Sharia bug
            newStatus = DefectStatus.RESOLVED,
            resolutionNote = null, // Missing resolution note
            closureReason = null,
            verificationTest = null
        )

        assertTrue("Resolving critical bug without resolution note must fail", result.isFailure)
        assertEquals("الأعطال الحرجة والمانعة للإطلاق (Blocker / Critical) تتطلب توثيق تفاصيل الحل الفني بدقة.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `resolving critical defect with detailed resolution note and verification test succeeds`() = runTest {
        val result = repository.updateDefectStatus(
            id = "BUG-001",
            newStatus = DefectStatus.RESOLVED,
            resolutionNote = "تم تحديث مصدر نص حفص المعتمد من مجمع الملك فهد ومطابقة علامة الضبط",
            closureReason = null,
            verificationTest = "QuranTashkeelSanityTest#verifyAyaDiacritics"
        )

        assertTrue("Resolving critical bug with resolution note must succeed", result.isSuccess)
        val updated = repository.getDefectById("BUG-001").first()
        assertNotNull(updated)
        assertEquals(DefectStatus.RESOLVED, updated!!.status)
        assertEquals("تم تحديث مصدر نص حفص المعتمد من مجمع الملك فهد ومطابقة علامة الضبط", updated.resolutionNote)
        assertEquals("QuranTashkeelSanityTest#verifyAyaDiacritics", updated.verificationTest)
    }

    @Test
    fun `deferring a defect requires a valid reason`() = runTest {
        val resultFail = repository.updateDefectStatus(
            id = "BUG-005",
            newStatus = DefectStatus.DEFERRED,
            resolutionNote = null,
            closureReason = "   ",
            verificationTest = null
        )
        assertTrue(resultFail.isFailure)

        val resultSuccess = repository.updateDefectStatus(
            id = "BUG-005",
            newStatus = DefectStatus.DEFERRED,
            resolutionNote = null,
            closureReason = "مؤجل للإصدار 1.1.0 بعد إكمال المحرك الأساسي للمونتاج",
            verificationTest = null
        )
        assertTrue(resultSuccess.isSuccess)
        val updated = repository.getDefectById("BUG-005").first()
        assertEquals(DefectStatus.DEFERRED, updated?.status)
    }

    @Test
    fun `prioritized fix list places blockers and criticals ahead of minor enhancements and excludes closed items`() = runTest {
        val prioritized = repository.getPrioritizedFixList().first()
        assertTrue(prioritized.isNotEmpty())

        // The first item should be a high priority unclosed item (P0 Blocker or P1 Critical)
        val firstItem = prioritized.first()
        assertTrue("First item should be P0 or P1", firstItem.priority == DefectPriority.P0_IMMEDIATE || firstItem.priority == DefectPriority.P1_HIGH)
        assertTrue("First item should not be closed or deferred", firstItem.status != DefectStatus.CLOSED && firstItem.status != DefectStatus.DEFERRED)

        // Closed and deferred items must not be present in the active fix list
        assertTrue("Active fix list must not contain closed items", prioritized.none { it.status == DefectStatus.CLOSED })
        assertTrue("Active fix list must not contain deferred items", prioritized.none { it.status == DefectStatus.DEFERRED })
    }

    @Test
    fun `safe logs do not contain raw pii or user sensitive tokens`() = runTest {
        val defects = repository.getAllDefects().first()
        for (defect in defects) {
            val logs = defect.safeLogsOrBreadcrumbs
            assertFalse("Safe logs should not contain password", logs.contains("password", ignoreCase = true))
            assertFalse("Safe logs should not contain apiKey", logs.contains("apiKey", ignoreCase = true))
            assertFalse("Safe logs should not contain raw bearer token", logs.contains("Bearer ", ignoreCase = true))
            assertFalse("Safe logs should not contain user email address", logs.contains("@gmail.com", ignoreCase = true))
        }
    }

    @Test
    fun `registering new defect validates fields and assigns to repository`() = runTest {
        val newDefect = BetaDefectRecord(
            id = "BUG-009",
            title = "انقطاع صوت التلاوة عند ورود مكالمة هاتفية",
            description = "عند تشغيل تلاوة سورة مريم واستقبال مكالمة لا يتم استئناف الصوت بسلاسة بعد انتهاء المكالمة.",
            deviceModel = "Samsung Galaxy S24",
            osVersion = "Android 14",
            appVersion = "1.0.0-beta.1",
            stepsToReproduce = listOf("تشغيل تلاوة سورة مريم", "محاكاة اتصال هاتفي", "إنهاء المكالمة وملاحظة حالة المشغل"),
            expectedResult = "استئناف التشغيل التلقائي بعد انتهاء التركيز الصوتي (AudioFocus)",
            actualResult = "يتوقف الصوت نهائياً ويبقى زر التشغيل في حالة إيقاف مؤقت",
            safeLogsOrBreadcrumbs = "AUDIO_FOCUS_LOSS_TRANSIENT -> ON_AUDIO_FOCUS_GAIN not triggered",
            priority = DefectPriority.P2_MEDIUM,
            assignedRole = "مهندس الصوتيات والوسائط",
            status = DefectStatus.REPORTED,
            classification = DefectClassification.MAJOR,
            domain = DefectDomain.MEDIA_STUDIO,
            targetRelease = "1.0.0-beta.2"
        )

        val result = repository.registerDefect(newDefect)
        assertTrue(result.isSuccess)

        val retrieved = repository.getDefectById("BUG-009").first()
        assertNotNull(retrieved)
        assertEquals("انقطاع صوت التلاوة عند ورود مكالمة هاتفية", retrieved?.title)
        assertEquals(DefectClassification.MAJOR, retrieved?.classification)
    }
}
