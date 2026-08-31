package com.siraj.app.domain.monitoring

/**
 * Interface defining crash monitoring and diagnostics capabilities.
 * Designed to be Kotlin Multiplatform ready (expect/actual or shared interface).
 */
interface CrashMonitoringService {
    /**
     * Initializes crash monitoring with environment configuration and metadata.
     */
    fun initialize(
        environment: String,
        appVersion: String,
        buildNumber: String,
    )

    /**
     * Toggles Crashlytics data collection based on environment and user privacy opt-in.
     */
    fun setCrashlyticsCollectionEnabled(enabled: Boolean)

    /**
     * Checks if Crashlytics data collection is currently active.
     */
    fun isCollectionEnabled(): Boolean

    /**
     * Records a non-fatal caught exception with categorized metadata and safe keys.
     */
    fun recordException(
        throwable: Throwable,
        category: ErrorCategory = ErrorCategory.UNKNOWN,
        severity: ErrorSeverity = ErrorSeverity.ERROR,
        requestId: String? = null,
        customKeys: Map<String, Any> = emptyMap(),
    )

    /**
     * Adds a safe breadcrumb for diagnostic context.
     * Guaranteed to be sanitized of PII, secrets, and religious texts.
     */
    fun logBreadcrumb(
        message: String,
        type: BreadcrumbType = BreadcrumbType.SYSTEM_EVENT,
        attributes: Map<String, String> = emptyMap(),
    )

    /**
     * Sets a custom string key-value pair in Crashlytics.
     */
    fun setCustomKey(
        key: String,
        value: String,
    )

    /**
     * Sets a custom boolean key-value pair in Crashlytics.
     */
    fun setCustomKey(
        key: String,
        value: Boolean,
    )

    /**
     * Sets a custom integer key-value pair in Crashlytics.
     */
    fun setCustomKey(
        key: String,
        value: Int,
    )

    /**
     * Sets a custom long key-value pair in Crashlytics.
     */
    fun setCustomKey(
        key: String,
        value: Long,
    )

    /**
     * Sets a custom double key-value pair in Crashlytics.
     */
    fun setCustomKey(
        key: String,
        value: Double,
    )

    /**
     * Sets an anonymized / hashed identifier for the user session.
     * NEVER sets real email, phone, or raw name.
     */
    fun setUserId(userId: String?)

    /**
     * Sets the active environment tag (e.g. DEVELOPMENT, STAGING, PRODUCTION).
     */
    fun setEnvironment(environment: String)

    /**
     * Sets a specific request or correlation ID for end-to-end tracing with Backend.
     */
    fun setRequestId(requestId: String)

    /**
     * Simulates a test non-fatal exception for QA and verification.
     */
    fun triggerTestNonFatalError(reason: String = "Test non-fatal error triggered from Siraj Diagnostics")

    /**
     * Simulates a test crash for Crashlytics pipeline verification.
     */
    fun triggerTestCrash()
}
