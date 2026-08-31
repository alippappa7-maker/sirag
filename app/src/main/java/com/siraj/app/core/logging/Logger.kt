package com.siraj.app.core.logging

import android.util.Log
import com.siraj.app.core.config.EnvironmentConfig
import com.siraj.app.core.monitoring.CrashMonitoringManager
import com.siraj.app.domain.monitoring.BreadcrumbType
import com.siraj.app.domain.monitoring.ErrorCategory
import com.siraj.app.domain.monitoring.ErrorSeverity

interface Logger {
    fun d(
        tag: String,
        message: String,
    )

    fun i(
        tag: String,
        message: String,
    )

    fun e(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    )
}

class SirajLogger : Logger {
    override fun d(
        tag: String,
        message: String,
    ) {
        if (EnvironmentConfig.isDebugEnabled) {
            Log.d(tag, message)
        }
    }

    override fun i(
        tag: String,
        message: String,
    ) {
        Log.i(tag, message)
        CrashMonitoringManager.logBreadcrumb(
            message = "$tag: $message",
            type = BreadcrumbType.SYSTEM_EVENT,
        )
    }

    override fun e(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        Log.e(tag, message, throwable)
        val exceptionToRecord = throwable ?: Exception("Logged Error [$tag]: $message")
        CrashMonitoringManager.recordException(
            throwable = exceptionToRecord,
            category = ErrorCategory.SYSTEM,
            severity = ErrorSeverity.ERROR,
            customKeys = mapOf("tag" to tag, "log_message" to message),
        )
    }
}
