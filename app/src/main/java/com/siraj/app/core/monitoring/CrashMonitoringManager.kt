package com.siraj.app.core.monitoring

import android.util.Log
import com.siraj.app.data.repository.monitoring.FirebaseCrashMonitoringServiceImpl
import com.siraj.app.domain.monitoring.BreadcrumbType
import com.siraj.app.domain.monitoring.CrashMonitoringService
import com.siraj.app.domain.monitoring.ErrorCategory
import com.siraj.app.domain.monitoring.ErrorSeverity

/**
 * Global entry point for crash and error monitoring across the Siraj platform.
 * Provides unified, thread-safe access to crash diagnostics with full privacy protection.
 */
object CrashMonitoringManager {

    private const val TAG = "CrashMonitoringMgr"
    private var service: CrashMonitoringService = FirebaseCrashMonitoringServiceImpl()
    private var isInitialized: Boolean = false

    /**
     * Swaps or sets the underlying CrashMonitoringService (e.g. for testing or platform targets).
     */
    fun initializeService(customService: CrashMonitoringService) {
        this.service = customService
    }

    /**
     * Bootstraps monitoring with application version and environment metadata.
     */
    fun initialize(environment: String, appVersion: String, buildNumber: String) {
        service.initialize(environment, appVersion, buildNumber)
        installUncaughtExceptionHandler()
        isInitialized = true
        Log.i(TAG, "CrashMonitoringManager initialized: Env=$environment, Version=$appVersion")
    }

    /**
     * Installs a defensive uncaught exception handler that adds context before passing to default handler.
     */
    private fun installUncaughtExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                service.logBreadcrumb(
                    message = "Uncaught exception on thread: ${thread.name}",
                    type = BreadcrumbType.SYSTEM_EVENT
                )
                service.recordException(
                    throwable = throwable,
                    category = ErrorCategory.SYSTEM,
                    severity = ErrorSeverity.FATAL,
                    customKeys = mapOf("fatal" to true, "thread_name" to thread.name)
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error in uncaught exception handler: ${e.message}")
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        service.setCrashlyticsCollectionEnabled(enabled)
    }

    fun isCollectionEnabled(): Boolean = service.isCollectionEnabled()

    fun recordException(
        throwable: Throwable,
        category: ErrorCategory = ErrorCategory.UNKNOWN,
        severity: ErrorSeverity = ErrorSeverity.ERROR,
        requestId: String? = null,
        customKeys: Map<String, Any> = emptyMap()
    ) {
        service.recordException(throwable, category, severity, requestId, customKeys)
    }

    fun logBreadcrumb(
        message: String,
        type: BreadcrumbType = BreadcrumbType.SYSTEM_EVENT,
        attributes: Map<String, String> = emptyMap()
    ) {
        service.logBreadcrumb(message, type, attributes)
    }

    /**
     * Convenience method for recording screen navigation breadcrumbs safely.
     */
    fun logNavigation(destination: String, from: String? = null) {
        val attrs = mutableMapOf("destination" to destination)
        if (from != null) attrs["from"] = from
        logBreadcrumb(
            message = "Navigated to $destination",
            type = BreadcrumbType.NAVIGATION,
            attributes = attrs
        )
    }

    /**
     * Convenience method for recording user action breadcrumbs without sensitive payloads.
     */
    fun logUserAction(actionName: String, entityType: String? = null) {
        val attrs = mutableMapOf("action" to actionName)
        if (entityType != null) attrs["entity_type"] = entityType
        logBreadcrumb(
            message = "Action: $actionName",
            type = BreadcrumbType.USER_ACTION,
            attributes = attrs
        )
    }

    fun setUserId(userId: String?) {
        service.setUserId(userId)
    }

    fun setEnvironment(environment: String) {
        service.setEnvironment(environment)
    }

    fun setRequestId(requestId: String) {
        service.setRequestId(requestId)
    }

    fun setCustomKey(key: String, value: String) {
        service.setCustomKey(key, value)
    }

    fun setCustomKey(key: String, value: Boolean) {
        service.setCustomKey(key, value)
    }

    fun setCustomKey(key: String, value: Int) {
        service.setCustomKey(key, value)
    }

    fun triggerTestNonFatalError(reason: String = "Test non-fatal diagnostics") {
        service.triggerTestNonFatalError(reason)
    }

    fun triggerTestCrash() {
        service.triggerTestCrash()
    }

    fun getService(): CrashMonitoringService = service
}
