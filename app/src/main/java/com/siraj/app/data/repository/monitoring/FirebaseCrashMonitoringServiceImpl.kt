package com.siraj.app.data.repository.monitoring

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.siraj.app.core.monitoring.CrashlyticsSanitizer
import com.siraj.app.domain.monitoring.BreadcrumbType
import com.siraj.app.domain.monitoring.CrashMonitoringService
import com.siraj.app.domain.monitoring.ErrorCategory
import com.siraj.app.domain.monitoring.ErrorSeverity

/**
 * Concrete implementation of CrashMonitoringService using Firebase Crashlytics on Android.
 * Designed with defensive exception handling so that missing Firebase configs or test environments
 * never crash the application.
 */
class FirebaseCrashMonitoringServiceImpl : CrashMonitoringService {
    private val tag = "SirajCrashlytics"
    private var isEnabled: Boolean = true

    private fun getCrashlyticsInstance(): FirebaseCrashlytics? =
        try {
            FirebaseCrashlytics.getInstance()
        } catch (e: Exception) {
            // Firebase not initialized (e.g. in local unit tests or isolated mode)
            null
        }

    override fun initialize(
        environment: String,
        appVersion: String,
        buildNumber: String,
    ) {
        try {
            val crashlytics = getCrashlyticsInstance()
            if (crashlytics != null) {
                crashlytics.setCustomKey("environment", environment)
                crashlytics.setCustomKey("app_version", appVersion)
                crashlytics.setCustomKey("build_number", buildNumber)
                crashlytics.setCustomKey("platform", "Android")
                Log.i(tag, "Crashlytics initialized successfully: Env=$environment, Version=$appVersion ($buildNumber)")
            } else {
                Log.d(tag, "Crashlytics instance not available at initialization.")
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to initialize Crashlytics custom keys: ${e.message}")
        }
    }

    override fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        this.isEnabled = enabled
        try {
            getCrashlyticsInstance()?.setCrashlyticsCollectionEnabled(enabled)
            Log.i(tag, "Crashlytics collection enabled set to: $enabled")
        } catch (e: Exception) {
            Log.w(tag, "Failed to toggle Crashlytics collection: ${e.message}")
        }
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

        try {
            val crashlytics = getCrashlyticsInstance()
            if (crashlytics != null) {
                // Set categorized diagnostic keys
                crashlytics.setCustomKey("error_category", category.key)
                crashlytics.setCustomKey("error_severity", severity.level)

                if (!requestId.isNullOrBlank()) {
                    crashlytics.setCustomKey("request_id", requestId)
                }

                // Attach safe custom keys
                customKeys.forEach { (key, value) ->
                    if (CrashlyticsSanitizer.isKeyAllowed(key)) {
                        when (value) {
                            is String -> crashlytics.setCustomKey(key, CrashlyticsSanitizer.sanitizeMessage(value))
                            is Boolean -> crashlytics.setCustomKey(key, value)
                            is Int -> crashlytics.setCustomKey(key, value)
                            is Long -> crashlytics.setCustomKey(key, value)
                            is Double -> crashlytics.setCustomKey(key, value)
                            is Float -> crashlytics.setCustomKey(key, value.toDouble())
                            else -> crashlytics.setCustomKey(key, CrashlyticsSanitizer.sanitizeMessage(value.toString()))
                        }
                    }
                }

                crashlytics.recordException(throwable)
                Log.d(tag, "Recorded non-fatal exception to Crashlytics: [Category=${category.key}] ${throwable.message}")
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to record exception to Crashlytics: ${e.message}")
        }
    }

    override fun logBreadcrumb(
        message: String,
        type: BreadcrumbType,
        attributes: Map<String, String>,
    ) {
        if (!isEnabled) return

        try {
            val formatted = "[${type.category.uppercase()}] ${CrashlyticsSanitizer.formatSafeBreadcrumb(message, attributes)}"
            getCrashlyticsInstance()?.log(formatted)
            Log.d(tag, "Breadcrumb: $formatted")
        } catch (e: Exception) {
            Log.w(tag, "Failed to log breadcrumb: ${e.message}")
        }
    }

    override fun setCustomKey(
        key: String,
        value: String,
    ) {
        if (!isEnabled || !CrashlyticsSanitizer.isKeyAllowed(key)) return
        try {
            getCrashlyticsInstance()?.setCustomKey(key, CrashlyticsSanitizer.sanitizeMessage(value))
        } catch (e: Exception) {
            Log.w(tag, "Failed to set custom string key: ${e.message}")
        }
    }

    override fun setCustomKey(
        key: String,
        value: Boolean,
    ) {
        if (!isEnabled || !CrashlyticsSanitizer.isKeyAllowed(key)) return
        try {
            getCrashlyticsInstance()?.setCustomKey(key, value)
        } catch (e: Exception) {
            Log.w(tag, "Failed to set custom boolean key: ${e.message}")
        }
    }

    override fun setCustomKey(
        key: String,
        value: Int,
    ) {
        if (!isEnabled || !CrashlyticsSanitizer.isKeyAllowed(key)) return
        try {
            getCrashlyticsInstance()?.setCustomKey(key, value)
        } catch (e: Exception) {
            Log.w(tag, "Failed to set custom int key: ${e.message}")
        }
    }

    override fun setCustomKey(
        key: String,
        value: Long,
    ) {
        if (!isEnabled || !CrashlyticsSanitizer.isKeyAllowed(key)) return
        try {
            getCrashlyticsInstance()?.setCustomKey(key, value)
        } catch (e: Exception) {
            Log.w(tag, "Failed to set custom long key: ${e.message}")
        }
    }

    override fun setCustomKey(
        key: String,
        value: Double,
    ) {
        if (!isEnabled || !CrashlyticsSanitizer.isKeyAllowed(key)) return
        try {
            getCrashlyticsInstance()?.setCustomKey(key, value)
        } catch (e: Exception) {
            Log.w(tag, "Failed to set custom double key: ${e.message}")
        }
    }

    override fun setUserId(userId: String?) {
        if (!isEnabled) return
        try {
            val anonymized = CrashlyticsSanitizer.anonymizeUserId(userId)
            if (anonymized != null) {
                getCrashlyticsInstance()?.setUserId(anonymized)
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to set anonymized userId: ${e.message}")
        }
    }

    override fun setEnvironment(environment: String) {
        setCustomKey("environment", environment)
    }

    override fun setRequestId(requestId: String) {
        setCustomKey("request_id", requestId)
    }

    override fun triggerTestNonFatalError(reason: String) {
        val testException = IllegalStateException("Siraj Diagnostics Non-Fatal Test: $reason")
        recordException(
            throwable = testException,
            category = ErrorCategory.SYSTEM,
            severity = ErrorSeverity.WARNING,
            customKeys = mapOf("test_trigger" to true, "reason" to reason),
        )
    }

    override fun triggerTestCrash() {
        Log.e(tag, "Triggering intentional test crash for Crashlytics pipeline verification...")
        throw RuntimeException("Siraj Test Crash - Verified for Crashlytics Monitoring Pipeline")
    }
}
