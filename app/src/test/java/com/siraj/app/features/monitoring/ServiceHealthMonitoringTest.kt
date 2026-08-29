package com.siraj.app.features.monitoring

import com.siraj.app.core.monitoring.HealthMonitoringEngine
import com.siraj.app.data.repository.monitoring.FirebaseMonitoringRepositoryImpl
import com.siraj.app.domain.models.monitoring.IncidentSeverity
import com.siraj.app.domain.models.monitoring.IncidentState
import com.siraj.app.domain.models.monitoring.MonitoredService
import com.siraj.app.domain.models.monitoring.ServiceCategory
import com.siraj.app.domain.models.monitoring.ServiceHealthStatus
import com.siraj.app.features.admin.presentation.monitoring.MonitoringDashboardViewModel
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
class ServiceHealthMonitoringTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FirebaseMonitoringRepositoryImpl
    private lateinit var viewModel: MonitoringDashboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FirebaseMonitoringRepositoryImpl()
        viewModel = MonitoringDashboardViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `all 13 essential platform services are monitored and loaded with initial health status`() = runTest {
        val healthList = repository.getServicesHealthStream().first()
        assertEquals(13, healthList.size)

        val geminiCheck = healthList.first { it.service == MonitoredService.GEMINI_AI_PROVIDER }
        assertEquals(ServiceHealthStatus.HEALTHY, geminiCheck.status)
        assertEquals(ServiceCategory.AI_PROVIDERS, geminiCheck.service.category)
        assertEquals(MonitoredService.CLOUD_RUN, geminiCheck.fallbackService)

        val quranCheck = healthList.first { it.service == MonitoredService.QURAN_API_PROVIDER }
        assertEquals(ServiceHealthStatus.HEALTHY, quranCheck.status)
        assertEquals(MonitoredService.FIRESTORE, quranCheck.fallbackService)
    }

    @Test
    fun `health monitoring engine computes deduplication hash without leaking secrets`() {
        val hash1 = HealthMonitoringEngine.computeDeduplicationHash(MonitoredService.GEMINI_AI_PROVIDER, IncidentSeverity.P1_HIGH)
        val hash2 = HealthMonitoringEngine.computeDeduplicationHash(MonitoredService.GEMINI_AI_PROVIDER, IncidentSeverity.P1_HIGH)
        val hash3 = HealthMonitoringEngine.computeDeduplicationHash(MonitoredService.AUTHENTICATION, IncidentSeverity.P0_CRITICAL)

        assertEquals("Same service and severity must produce identical deduplication hash", hash1, hash2)
        assertNotEquals("Different services must produce distinct deduplication hashes", hash1, hash3)
        assertFalse("Hash must not contain raw strings", hash1.contains("GEMINI"))
    }

    @Test
    fun `public user messages are politely sanitized hiding cloud cluster details`() {
        val geminiError = HealthMonitoringEngine.sanitizeForUser(
            MonitoredService.GEMINI_AI_PROVIDER,
            "Internal 503 Backend Timeout at us-central1-docker.pkg.dev:8080/gemini-1.5-pro with Bearer secret"
        )
        assertFalse("User message must never leak hostnames or API key pointers", geminiError.contains("us-central1") || geminiError.contains("Bearer"))
        assertTrue("Must provide polite Arabic fallback advice", geminiError.contains("مساعد الإنتاج الذكي") || geminiError.contains("صيانة مؤقتة"))

        val quranError = HealthMonitoringEngine.sanitizeForUser(
            MonitoredService.QURAN_API_PROVIDER,
            "Postgres SQL connection pool exhausted at quran-db-primary.cloudsql"
        )
        assertFalse("User message must never leak DB details", quranError.contains("Postgres") || quranError.contains("cloudsql"))
        assertTrue("Must assure user that local verified Mushaf is used", quranError.contains("النسخة المحلية المعتمدة"))
    }

    @Test
    fun `circuit breaker evaluates trip conditions accurately on consecutive failures`() {
        assertFalse("1 failure within timeout should not trip breaker", HealthMonitoringEngine.shouldTripCircuitBreaker(1, 150, 5000, 0.0))
        assertTrue("3 consecutive failures must trip breaker", HealthMonitoringEngine.shouldTripCircuitBreaker(3, 150, 5000, 0.0))
        assertTrue("Latency exceeding 2x timeout must trip breaker", HealthMonitoringEngine.shouldTripCircuitBreaker(1, 11000, 5000, 0.0))
        assertTrue("Error rate above 50 percent must trip breaker", HealthMonitoringEngine.shouldTripCircuitBreaker(1, 200, 5000, 55.0))
    }

    @Test
    fun `tripping circuit breaker disables service and routes to fallback gracefully`() = runTest {
        val result = repository.toggleServiceCircuitBreaker(
            service = MonitoredService.GEMINI_AI_PROVIDER,
            disabled = true,
            reasonArabic = "تجاوز معدل الأخطاء 50%"
        )
        assertTrue("Toggle must succeed", result.isSuccess)

        val updatedList = repository.getServicesHealthStream().first()
        val geminiCheck = updatedList.first { it.service == MonitoredService.GEMINI_AI_PROVIDER }
        assertTrue(geminiCheck.isCircuitBroken)
        assertEquals(ServiceHealthStatus.CIRCUIT_BROKEN_DISABLED, geminiCheck.status)
        assertEquals(MonitoredService.CLOUD_RUN, geminiCheck.fallbackService)

        // Must generate an alert
        val alerts = repository.getActiveAlertsStream().first()
        assertTrue("An active alert must be raised for circuit trip", alerts.any { it.service == MonitoredService.GEMINI_AI_PROVIDER })
    }

    @Test
    fun `creating an incident logs timeline events and updates service status`() = runTest {
        val incidentResult = repository.createIncident(
            service = MonitoredService.VIDEO_RENDERING_QUEUE,
            titleArabic = "تكدس طابور المعالجة",
            descriptionArabic = "تجاوز عدد المهام 50 مهمة مع بطء في التصدير",
            severity = IncidentSeverity.P1_HIGH
        )
        assertTrue(incidentResult.isSuccess)
        val incident = incidentResult.getOrNull()!!
        assertEquals(IncidentState.INVESTIGATING, incident.state)
        assertEquals(1, incident.timelineEvents.size)

        // Update state to mitigating
        val updateRes = repository.updateIncidentState(
            incidentId = incident.incidentId,
            newState = IncidentState.MITIGATING,
            notesArabic = "تم رفع التوسع الأفقي لحاويات Cloud Run",
            mitigationAction = "Auto-scale from 5 to 20 instances"
        )
        assertTrue(updateRes.isSuccess)
        val updatedIncident = updateRes.getOrNull()!!
        assertEquals(IncidentState.MITIGATING, updatedIncident.state)
        assertEquals(2, updatedIncident.timelineEvents.size)

        // Resolve incident
        val resolveRes = repository.updateIncidentState(
            incidentId = incident.incidentId,
            newState = IncidentState.RESOLVED,
            notesArabic = "استقر الطابور واكتملت كافة المهام"
        )
        assertTrue(resolveRes.isSuccess)
        assertNotNull(resolveRes.getOrNull()?.resolvedTimestamp)
    }

    @Test
    fun `synthetic probe uses safe non-religious tokens`() {
        assertEquals("SIRAJ_SYSTEM_HEALTH_CHECK_SYNTHETIC_PING_2026", HealthMonitoringEngine.SYNTHETIC_HEALTH_PROBE_PAYLOAD)
    }

    @Test
    fun `viewModel coordinates probes, category filters and incident lifecycle`() = runTest {
        advanceUntilIdle()
        viewModel.filterByCategory(ServiceCategory.AI_PROVIDERS)
        val state = viewModel.uiState.value
        assertEquals(ServiceCategory.AI_PROVIDERS, state.selectedCategory)
        assertTrue("Filtered list should only contain AI providers", state.filteredServicesList.all { it.service.category == ServiceCategory.AI_PROVIDERS })

        viewModel.runAllHealthProbes()
        advanceUntilIdle()
        val postState = viewModel.uiState.value
        assertFalse(postState.isProbing)
        assertNotNull(postState.bannerMessage)
    }
}
