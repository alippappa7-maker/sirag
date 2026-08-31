package com.siraj.app.core.monitoring

import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.siraj.app.core.error.AppError
import com.siraj.app.core.error.ErrorHandler
import com.siraj.app.domain.monitoring.BreadcrumbType
import com.siraj.app.domain.monitoring.CrashMonitoringService
import com.siraj.app.domain.monitoring.ErrorCategory
import com.siraj.app.domain.monitoring.ErrorSeverity
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

class FakeCrashMonitoringService : CrashMonitoringService {
    var isEnabled: Boolean = true
    var recordedEnvironment: String? = null
    var recordedAppVersion: String? = null
    var recordedBuildNumber: String? = null
    var recordedUserId: String? = null
    var lastRecordedException: Throwable? = null
    var lastCategory: ErrorCategory? = null
    var lastSeverity: ErrorSeverity? = null
    var lastRequestId: String? = null
    val customKeys = mutableMapOf<String, Any>()
    val breadcrumbs = mutableListOf<String>()
    var testCrashTriggered: Boolean = false

    override fun initialize(
        environment: String,
        appVersion: String,
        buildNumber: String,
    ) {
        this.recordedEnvironment = environment
        this.recordedAppVersion = appVersion
        this.recordedBuildNumber = buildNumber
        setCustomKey("environment", environment)
        setCustomKey("app_version", appVersion)
        setCustomKey("build_number", buildNumber)
    }

    override fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        this.isEnabled = enabled
    }

    override fun isCollectionEnabled(): Boolean = isEnabled

    override fun recordException(
        throwable: Throwable,
        category: ErrorCategory,
        severity: ErrorSeverity,
        requestId: String?,
        customKeys: Map<String, Any>,
    ) {
        if (!isEnabled) return
        this.lastRecordedException = throwable
        this.lastCategory = category
        this.lastSeverity = severity
        this.lastRequestId = requestId
        this.customKeys.putAll(customKeys)
    }

    override fun logBreadcrumb(
        message: String,
        type: BreadcrumbType,
        attributes: Map<String, String>,
    ) {
        if (!isEnabled) return
        val formatted = "[${type.category.uppercase()}] ${CrashlyticsSanitizer.formatSafeBreadcrumb(message, attributes)}"
        breadcrumbs.add(formatted)
    }

    override fun setCustomKey(
        key: String,
        value: String,
    ) {
        if (isEnabled && CrashlyticsSanitizer.isKeyAllowed(key)) {
            customKeys[key] = CrashlyticsSanitizer.sanitizeMessage(value)
        }
    }

    override fun setCustomKey(
        key: String,
        value: Boolean,
    ) {
        if (isEnabled && CrashlyticsSanitizer.isKeyAllowed(key)) {
            customKeys[key] = value
        }
    }

    override fun setCustomKey(
        key: String,
        value: Int,
    ) {
        if (isEnabled && CrashlyticsSanitizer.isKeyAllowed(key)) {
            customKeys[key] = value
        }
    }

    override fun setCustomKey(
        key: String,
        value: Long,
    ) {
        if (isEnabled && CrashlyticsSanitizer.isKeyAllowed(key)) {
            customKeys[key] = value
        }
    }

    override fun setCustomKey(
        key: String,
        value: Double,
    ) {
        if (isEnabled && CrashlyticsSanitizer.isKeyAllowed(key)) {
            customKeys[key] = value
        }
    }

    override fun setUserId(userId: String?) {
        if (!isEnabled) return
        this.recordedUserId = CrashlyticsSanitizer.anonymizeUserId(userId)
    }

    override fun setEnvironment(environment: String) {
        setCustomKey("environment", environment)
    }

    override fun setRequestId(requestId: String) {
        setCustomKey("request_id", requestId)
    }

    override fun triggerTestNonFatalError(reason: String) {
        val testEx = IllegalStateException("Test: $reason")
        recordException(
            throwable = testEx,
            category = ErrorCategory.SYSTEM,
            severity = ErrorSeverity.WARNING,
            customKeys = mapOf("test" to true, "reason" to reason),
        )
    }

    override fun triggerTestCrash() {
        testCrashTriggered = true
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CrashMonitoringTest {
    private lateinit var fakeService: FakeCrashMonitoringService

    @Before
    fun setUp() {
        fakeService = FakeCrashMonitoringService()
        CrashMonitoringManager.initializeService(fakeService)
    }

    @Test
    fun sanitizer_masksSensitiveApiKeysAndTokens() {
        val input = "Network error with url https://api.example.com?key=AIzaSyA123456789&token=secret_abc123"
        val sanitized = CrashlyticsSanitizer.sanitizeMessage(input)

        assertFalse("API key must be masked", sanitized.contains("AIzaSyA123456789"))
        assertFalse("Token must be masked", sanitized.contains("secret_abc123"))
        assertTrue("Contains masked key indicator", sanitized.contains("key=***"))
        assertTrue("Contains masked token indicator", sanitized.contains("token=***"))
    }

    @Test
    fun sanitizer_masksBearerTokensAndPasswords() {
        val input = "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9 and password=SuperSecretPass123"
        val sanitized = CrashlyticsSanitizer.sanitizeMessage(input)

        assertFalse("Bearer token must be masked", sanitized.contains("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"))
        assertFalse("Password must be masked", sanitized.contains("SuperSecretPass123"))
        assertTrue(sanitized.contains("Bearer ***"))
        assertTrue(sanitized.contains("password=***"))
    }

    @Test
    fun sanitizer_masksEmailsAndPurchaseTokens() {
        val input = "User account user.test@siraj.app failed purchase_token=gpay_token_xyz987"
        val sanitized = CrashlyticsSanitizer.sanitizeMessage(input)

        assertFalse("Email should be masked", sanitized.contains("user.test@siraj.app"))
        assertTrue("Masked email placeholder present", sanitized.contains("[MASKED_EMAIL]"))
        assertFalse("Purchase token should be masked", sanitized.contains("gpay_token_xyz987"))
        assertTrue("Masked purchase token indicator present", sanitized.contains("purchase_token=***"))
    }

    @Test
    fun sanitizer_blocksForbiddenKeys() {
        assertFalse("Password key is forbidden", CrashlyticsSanitizer.isKeyAllowed("password"))
        assertFalse("API key is forbidden", CrashlyticsSanitizer.isKeyAllowed("api_key"))
        assertFalse("Token key is forbidden", CrashlyticsSanitizer.isKeyAllowed("access_token"))
        assertFalse("Quran text key is forbidden", CrashlyticsSanitizer.isKeyAllowed("quran_text"))
        assertFalse("Hadith text key is forbidden", CrashlyticsSanitizer.isKeyAllowed("hadith_text"))
        assertFalse("Script content key is forbidden", CrashlyticsSanitizer.isKeyAllowed("script_content"))

        assertTrue("Safe keys are allowed", CrashlyticsSanitizer.isKeyAllowed("environment"))
        assertTrue("Safe keys are allowed", CrashlyticsSanitizer.isKeyAllowed("app_version"))
        assertTrue("Safe keys are allowed", CrashlyticsSanitizer.isKeyAllowed("screen_name"))
        assertTrue("Safe keys are allowed", CrashlyticsSanitizer.isKeyAllowed("error_category"))
    }

    @Test
    fun sanitizer_anonymizesUserIdWithStableHash() {
        val rawUserId = "user_12345_sensitive_id"
        val hashed1 = CrashlyticsSanitizer.anonymizeUserId(rawUserId)
        val hashed2 = CrashlyticsSanitizer.anonymizeUserId(rawUserId)

        assertNotNull("Hashed user id should not be null", hashed1)
        assertEquals("Hash must be deterministic", hashed1, hashed2)
        assertFalse("Hash must not contain the raw user ID", hashed1!!.contains(rawUserId))
        assertEquals("Hash length should be 16 chars", 16, hashed1.length)

        val nullHash = CrashlyticsSanitizer.anonymizeUserId(null)
        assertNull("Null user id returns null", nullHash)
    }

    @Test
    fun crashMonitoringManager_initializesMetadataAndSetsCustomKeys() {
        CrashMonitoringManager.initialize(
            environment = "PRODUCTION",
            appVersion = "1.0.0",
            buildNumber = "42",
        )

        assertEquals("PRODUCTION", fakeService.recordedEnvironment)
        assertEquals("1.0.0", fakeService.recordedAppVersion)
        assertEquals("42", fakeService.recordedBuildNumber)
        assertEquals("PRODUCTION", fakeService.customKeys["environment"])
        assertEquals("1.0.0", fakeService.customKeys["app_version"])
    }

    @Test
    fun crashMonitoringManager_recordsNavigationAndActionBreadcrumbs() {
        CrashMonitoringManager.logNavigation(destination = "ProjectEditorScreen", from = "HomeScreen")
        CrashMonitoringManager.logUserAction(actionName = "SaveContentPlan", entityType = "Project")

        assertEquals(2, fakeService.breadcrumbs.size)
        assertTrue(fakeService.breadcrumbs[0].contains("[NAVIGATION]"))
        assertTrue(fakeService.breadcrumbs[0].contains("destination=ProjectEditorScreen"))
        assertTrue(fakeService.breadcrumbs[1].contains("[USER_ACTION]"))
        assertTrue(fakeService.breadcrumbs[1].contains("action=SaveContentPlan"))
    }

    @Test
    fun crashMonitoringManager_handlesTestNonFatalError() {
        CrashMonitoringManager.triggerTestNonFatalError("QA Validation")

        assertNotNull(fakeService.lastRecordedException)
        assertEquals(ErrorCategory.SYSTEM, fakeService.lastCategory)
        assertEquals(ErrorSeverity.WARNING, fakeService.lastSeverity)
        assertEquals(true, fakeService.customKeys["test"])
    }

    @Test
    fun errorHandler_categorizesAndReportsNetworkErrors() {
        val ioException = IOException("Failed to connect to backend service key=abc123secret")
        val appError = ErrorHandler.handle(ioException, requestId = "REQ-999")

        assertTrue(appError is AppError.Network)
        assertTrue(appError.isRetryable)
        assertEquals(ErrorCategory.NETWORK, fakeService.lastCategory)
        assertEquals("REQ-999", fakeService.lastRequestId)

        // Assert technical details in breadcrumbs/customKeys are sanitized
        assertFalse(fakeService.customKeys.toString().contains("abc123secret"))
    }

    @Test
    fun errorHandler_categorizesAndReportsAuthAndDatabaseErrors() {
        val authException = FirebaseAuthException("ERROR_INVALID_CUSTOM_TOKEN", "Invalid token=tok_bad_value")
        val appAuthError = ErrorHandler.handle(authException)

        assertTrue(appAuthError is AppError.Auth)
        assertEquals(ErrorCategory.AUTH, fakeService.lastCategory)
        assertFalse(fakeService.customKeys.toString().contains("tok_bad_value"))

        val dbException = FirebaseFirestoreException("Permission denied", FirebaseFirestoreException.Code.PERMISSION_DENIED)
        val appPermError = ErrorHandler.handle(dbException)

        assertTrue(appPermError is AppError.Permission)
        assertEquals(ErrorCategory.SECURITY, fakeService.lastCategory)
    }

    @Test
    fun crashMonitoringManager_respectsOptOut() {
        CrashMonitoringManager.setCrashlyticsCollectionEnabled(false)
        assertFalse(CrashMonitoringManager.isCollectionEnabled())

        CrashMonitoringManager.logNavigation("AnyScreen")
        CrashMonitoringManager.recordException(RuntimeException("Should be ignored"))

        assertTrue("No breadcrumbs when disabled", fakeService.breadcrumbs.isEmpty())
        assertNull("No exception recorded when disabled", fakeService.lastRecordedException)
    }
}
