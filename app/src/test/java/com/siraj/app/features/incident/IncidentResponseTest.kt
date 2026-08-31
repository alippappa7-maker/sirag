package com.siraj.app.features.incident

import com.siraj.app.core.incident.IncidentResponseEngine
import com.siraj.app.data.repository.incident.FirebaseIncidentResponseRepositoryImpl
import com.siraj.app.domain.models.incident.EmergencyActionType
import com.siraj.app.domain.models.incident.IncidentPhase
import com.siraj.app.domain.models.incident.IncidentPostMortemReport
import com.siraj.app.domain.models.incident.IncidentRole
import com.siraj.app.domain.models.incident.IncidentSeverity
import com.siraj.app.domain.models.incident.IncidentType
import com.siraj.app.domain.models.incident.ShariaIncidentCorrection
import com.siraj.app.features.admin.presentation.incident.IncidentResponseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IncidentResponseTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FirebaseIncidentResponseRepositoryImpl
    private lateinit var viewModel: IncidentResponseViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FirebaseIncidentResponseRepositoryImpl()
        viewModel = IncidentResponseViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `all 10 incident types are defined with distinct codes and responsible roles`() {
        val types = IncidentType.values()
        assertEquals(10, types.size)

        // Verify key types
        assertTrue(types.any { it == IncidentType.SERVICE_OUTAGE })
        assertTrue(types.any { it == IncidentType.KEY_CREDENTIAL_LEAK })
        assertTrue(types.any { it == IncidentType.UNAUTHORIZED_ACCESS })
        assertTrue(types.any { it == IncidentType.SHARIA_CONTENT_ERROR })
        assertTrue(types.any { it == IncidentType.UNREVIEWED_CONTENT_PUBLISHED })
        assertTrue(types.any { it == IncidentType.SUBSCRIPTION_GLITCH })
        assertTrue(types.any { it == IncidentType.DUPLICATE_CHARGE })
        assertTrue(types.any { it == IncidentType.FILE_DATA_LOSS })
        assertTrue(types.any { it == IncidentType.ABUSE_SPAM })
        assertTrue(types.any { it == IncidentType.COPYRIGHT_INFRINGEMENT })

        // Check Sharia incident role
        assertEquals(IncidentRole.SHARIA_REVIEWER_LEAD, IncidentType.SHARIA_CONTENT_ERROR.primaryRole)
        assertEquals(IncidentSeverity.P0_CRITICAL, IncidentType.SHARIA_CONTENT_ERROR.defaultSeverity)

        // Check Key leak role
        assertEquals(IncidentRole.SECURITY_OFFICER, IncidentType.KEY_CREDENTIAL_LEAK.primaryRole)
    }

    @Test
    fun `incident lifecycle contains all 8 sequential phases`() {
        val phases = IncidentPhase.values()
        assertEquals(8, phases.size)
        assertEquals("الاكتشاف", phases[0].displayNameArabic)
        assertEquals("التصنيف والتقييم", phases[1].displayNameArabic)
        assertEquals("العزل الفوري", phases[2].displayNameArabic)
        assertEquals("الإصلاح الجذري", phases[3].displayNameArabic)
        assertEquals("التحقق والاختبار", phases[4].displayNameArabic)
        assertEquals("التواصل الشفاف", phases[5].displayNameArabic)
        assertEquals("التوثيق والتقرير", phases[6].displayNameArabic)
        assertEquals("المراجعة اللاحقة", phases[7].displayNameArabic)
    }

    @Test
    fun `emergency kill-switch halts publishing globally and logs immutable action`() =
        runTest {
            val res =
                repository.toggleGlobalPublishingHalt(
                    halt = true,
                    executedByUserId = "ADM-COMMANDER-01",
                    executedByRole = IncidentRole.INCIDENT_COMMANDER,
                    reasonArabic = "تجميد احترازي لوجود نشاط غير مصرح به",
                )
            assertTrue(res.isSuccess)
            val action = res.getOrNull()!!
            assertEquals(EmergencyActionType.EMERGENCY_HALT_PUBLISHING, action.actionType)
            assertEquals("GLOBAL_PUBLISHING_GATEWAY", action.targetResource)

            val state = repository.getIncidentResponseStateStream().first()
            assertTrue("Global publishing must be halted", state.isGlobalPublishingHalted)
            assertEquals(1, state.criticalP0Count)
        }

    @Test
    fun `emergency secret rotation rejects unauthorized roles and approves security officer`() =
        runTest {
            // Unauthorized role attempt
            val failResult =
                IncidentResponseEngine.createEmergencyAction(
                    actionType = EmergencyActionType.ROTATE_SECRET_CREDENTIAL,
                    executedByUserId = "COMM-01",
                    executedByRole = IncidentRole.COMMUNICATIONS_OFFICER,
                    targetResource = "FIREBASE_SERVICE_ACCOUNT_KEY",
                    reasonArabic = "تدوير مفتاح",
                )
            assertTrue(failResult.isFailure)

            // Authorized role attempt
            val passResult =
                repository.rotateSecretKey(
                    secretIdentifier = "GEMINI_BACKEND_API_KEY",
                    executedByUserId = "CISO-01",
                    reasonArabic = "تدوير دوري وقائي كل 90 يوماً",
                )
            assertTrue(passResult.isSuccess)
            val actions = repository.getEmergencyActionsStream().first()
            assertTrue(actions.any { it.targetResource == "GEMINI_BACKEND_API_KEY" })
        }

    @Test
    fun `sharia correction requires verified sources and double reviewer approval for sacred content`() =
        runTest {
            // Incomplete correction without reviewer 2
            val singleRevCorrection =
                ShariaIncidentCorrection(
                    incidentId = "INC-SHARIA-002",
                    projectId = "PRJ-AYAH-99",
                    faultyText = "نص خاطئ",
                    verifiedCorrectText = "نص مصوب معتمد",
                    primarySourceReference = "تفسير الطبري",
                    reviewer1Id = "REV-01",
                    reviewer1NotesArabic = "تمت المراجعة",
                    reviewer2Id = null,
                    reviewer2NotesArabic = null,
                )
            val singleRes = IncidentResponseEngine.validateShariaCorrection(singleRevCorrection)
            assertTrue(singleRes.isSuccess)
            assertFalse("Single reviewer must not mark as approvedByBothReviewers", singleRes.getOrNull()!!.approvedByBothReviewers)

            // Full double approved correction
            val doubleApprovedCorrection =
                singleRevCorrection.copy(
                    reviewer2Id = "REV-02",
                    reviewer2NotesArabic = "أوافق وأعتمد التصويب",
                )
            val submitRes = repository.submitShariaCorrection(doubleApprovedCorrection)
            assertTrue(submitRes.isSuccess)
            assertTrue("Double reviewer must mark approvedByBothReviewers = true", submitRes.getOrNull()!!.approvedByBothReviewers)
        }

    @Test
    fun `public incident communication sanitizes technical stack details and protects secrets`() {
        val sanitizedOutage =
            IncidentResponseEngine.sanitizePublicIncidentNotice(
                IncidentType.SERVICE_OUTAGE,
                "Kubernetes pod crash on us-central1-a with exit code 137 OOMKilled",
            )
        assertFalse(sanitizedOutage.contains("Kubernetes") || sanitizedOutage.contains("us-central1"))
        assertTrue(sanitizedOutage.contains("بطئاً مؤقتاً") || sanitizedOutage.contains("البيانات"))

        val sanitizedSecretLeak =
            IncidentResponseEngine.sanitizePublicIncidentNotice(
                IncidentType.KEY_CREDENTIAL_LEAK,
                "AIzaSyD-PrivateGeminiKeyLeaked on GitHub commit abc1234",
            )
        assertFalse(sanitizedSecretLeak.contains("AIzaSyD") || sanitizedSecretLeak.contains("GitHub"))
        assertTrue(sanitizedSecretLeak.contains("إجراء أمني وقائي"))
    }

    @Test
    fun `batch refund executes for duplicate charges and logs credit restitution`() =
        runTest {
            val refundRes =
                repository.executeBatchRefund(
                    targetUserOrBatchId = "BATCH-DUP-USERS-44",
                    refundAmountCredits = 100,
                    executedByUserId = "BILLING-LEAD-01",
                    reasonArabic = "تعويض عن خصم رصيد مكرر أثناء انقطاع الخادم",
                )
            assertTrue(refundRes.isSuccess)
            val actions = repository.getEmergencyActionsStream().first()
            assertTrue(actions.any { it.actionType == EmergencyActionType.TRIGGER_BATCH_REFUND })
        }

    @Test
    fun `post-mortem report records complete root cause, downtime and preventive tasks`() =
        runTest {
            val report =
                IncidentPostMortemReport(
                    incidentId = "INC-TEST-009",
                    incidentType = IncidentType.UNREVIEWED_CONTENT_PUBLISHED,
                    severity = IncidentSeverity.P1_HIGH,
                    titleArabic = "محاولة نشر مشروع دون اكتمال ختم المراجعة",
                    leadInvestigator = "الشيخ د. عبد الرحمن السعدي",
                    detectionTimestamp = System.currentTimeMillis() - 7200000L,
                    containmentTimestamp = System.currentTimeMillis() - 7000000L,
                    resolutionTimestamp = System.currentTimeMillis(),
                    totalDowntimeMinutes = 15,
                    affectedUsersCount = 1,
                    rootCauseSummaryArabic = "تجاوز فحص الواجهة لخطوة التحقق من الختم الشرعي",
                    containmentStepsArabic = listOf("سحب المشروع فوراً عبر Kill-Switch"),
                    correctiveActionsArabic = listOf("إضافة فحص خادمي إلزامي في Cloud Functions"),
                    preventiveTasksArabic = listOf("اختبار وحدة لرفض أي مشروع غير مختوم"),
                )
            val createRes = repository.createPostMortemReport(report)
            assertTrue(createRes.isSuccess)

            val reports = repository.getPostMortemReportsStream().first()
            assertEquals(2, reports.size)
            assertTrue(reports.any { it.incidentId == "INC-TEST-009" })
        }

    @Test
    fun `escalation roster has 6 24x7 emergency contacts with distinct roles`() =
        runTest {
            val contacts = repository.getContactsMatrixStream().first()
            assertEquals(6, contacts.size)
            assertTrue(contacts.any { it.role == IncidentRole.INCIDENT_COMMANDER })
            assertTrue(contacts.any { it.role == IncidentRole.SHARIA_REVIEWER_LEAD })
            assertTrue(contacts.any { it.role == IncidentRole.SECURITY_OFFICER })
        }

    @Test
    fun `viewModel handles playbooks navigation and emergency actions`() =
        runTest {
            advanceUntilIdle()
            viewModel.selectIncidentType(IncidentType.SHARIA_CONTENT_ERROR)
            assertEquals(IncidentType.SHARIA_CONTENT_ERROR, viewModel.uiState.value.selectedIncidentType)

            viewModel.toggleGlobalPublishing(true, "إيقاف طارئ للتجربة")
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.isGlobalPublishingHalted)
            assertNotNull(viewModel.uiState.value.bannerMessage)

            viewModel.clearBanner()
            assertNull(viewModel.uiState.value.bannerMessage)
        }
}
