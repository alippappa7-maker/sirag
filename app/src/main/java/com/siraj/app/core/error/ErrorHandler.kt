package com.siraj.app.core.error

import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageException
import com.siraj.app.core.logging.Logger
import com.siraj.app.core.logging.SirajLogger
import com.siraj.app.core.monitoring.CrashMonitoringManager
import com.siraj.app.core.monitoring.CrashlyticsSanitizer
import com.siraj.app.domain.monitoring.BreadcrumbType
import com.siraj.app.domain.monitoring.ErrorCategory
import com.siraj.app.domain.monitoring.ErrorSeverity
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

object ErrorHandler {

    private val logger: Logger = SirajLogger()

    fun handle(exception: Throwable, requestId: String? = null): AppError {
        val error = when (exception) {
            is AppError -> exception
            is UnknownHostException, is IOException, is SocketTimeoutException, is TimeoutException, is FirebaseNetworkException -> {
                AppError.Network(details = CrashlyticsSanitizer.sanitizeMessage(exception.message))
            }
            is FirebaseAuthException -> {
                AppError.Auth(details = CrashlyticsSanitizer.sanitizeMessage(exception.message))
            }
            is FirebaseFirestoreException -> {
                when (exception.code) {
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> AppError.Permission(details = CrashlyticsSanitizer.sanitizeMessage(exception.message))
                    FirebaseFirestoreException.Code.UNAVAILABLE -> AppError.Network(details = CrashlyticsSanitizer.sanitizeMessage(exception.message))
                    else -> AppError.Database(details = CrashlyticsSanitizer.sanitizeMessage(exception.message))
                }
            }
            is StorageException -> {
                when (exception.errorCode) {
                    StorageException.ERROR_NOT_AUTHORIZED -> AppError.Permission(details = CrashlyticsSanitizer.sanitizeMessage(exception.message))
                    else -> AppError.Storage(details = CrashlyticsSanitizer.sanitizeMessage(exception.message))
                }
            }
            is FirebaseException -> {
                AppError.Database(details = CrashlyticsSanitizer.sanitizeMessage(exception.message))
            }
            else -> {
                AppError.Unknown(details = CrashlyticsSanitizer.sanitizeMessage(exception.message))
            }
        }
        
        val category = mapToErrorCategory(error)
        logAndReportError(error, exception, category, requestId)
        return error
    }

    private fun mapToErrorCategory(error: AppError): ErrorCategory {
        return when (error) {
            is AppError.Network -> ErrorCategory.NETWORK
            is AppError.Auth -> ErrorCategory.AUTH
            is AppError.Permission -> ErrorCategory.SECURITY
            is AppError.Database -> ErrorCategory.DATABASE
            is AppError.Storage -> ErrorCategory.STORAGE
            is AppError.AiProvider -> ErrorCategory.AI_PROVIDER
            is AppError.Queue -> ErrorCategory.QUEUE
            is AppError.Payment -> ErrorCategory.PAYMENT
            is AppError.LocalExecution -> ErrorCategory.LOCAL_EXECUTION
            is AppError.Unknown -> ErrorCategory.UNKNOWN
        }
    }

    private fun logAndReportError(
        error: AppError,
        originalException: Throwable,
        category: ErrorCategory,
        requestId: String?
    ) {
        val sanitizedDetails = CrashlyticsSanitizer.sanitizeMessage(error.technicalDetails)
        val logMessage = "ErrorRef: [${error.referenceId}] | Type: ${error.javaClass.simpleName} | Details: $sanitizedDetails"
        
        logger.e("ErrorHandler", logMessage, originalException)

        // Log structured event to Crashlytics
        CrashMonitoringManager.logBreadcrumb(
            message = "AppError [${error.referenceId}]: ${error.javaClass.simpleName}",
            type = BreadcrumbType.SYSTEM_EVENT,
            attributes = mapOf(
                "ref_id" to error.referenceId,
                "is_retryable" to error.isRetryable.toString(),
                "category" to category.key
            )
        )

        CrashMonitoringManager.recordException(
            throwable = originalException,
            category = category,
            severity = if (error.isRetryable) ErrorSeverity.WARNING else ErrorSeverity.ERROR,
            requestId = requestId ?: error.referenceId,
            customKeys = mapOf(
                "reference_id" to error.referenceId,
                "is_retryable" to error.isRetryable,
                "error_class" to error.javaClass.simpleName
            )
        )
    }
}

