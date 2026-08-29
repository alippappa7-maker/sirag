package com.siraj.app.core.error

import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageException
import java.io.IOException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException
import com.siraj.app.core.logging.Logger
import com.siraj.app.core.logging.SirajLogger
import java.net.SocketTimeoutException

object ErrorHandler {

    private val logger: Logger = SirajLogger()

    fun handle(exception: Throwable): AppError {
        val error = when (exception) {
            is AppError -> exception
            is UnknownHostException, is IOException, is SocketTimeoutException, is TimeoutException, is FirebaseNetworkException -> {
                AppError.Network(details = exception.message)
            }
            is FirebaseAuthException -> {
                AppError.Auth(details = sanitizeTechnicalMessage(exception.message))
            }
            is FirebaseFirestoreException -> {
                when (exception.code) {
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> AppError.Permission(details = sanitizeTechnicalMessage(exception.message))
                    FirebaseFirestoreException.Code.UNAVAILABLE -> AppError.Network(details = sanitizeTechnicalMessage(exception.message))
                    else -> AppError.Database(details = sanitizeTechnicalMessage(exception.message))
                }
            }
            is StorageException -> {
                when (exception.errorCode) {
                    StorageException.ERROR_NOT_AUTHORIZED -> AppError.Permission(details = sanitizeTechnicalMessage(exception.message))
                    else -> AppError.Storage(details = sanitizeTechnicalMessage(exception.message))
                }
            }
            is FirebaseException -> {
                AppError.Database(details = sanitizeTechnicalMessage(exception.message))
            }
            else -> {
                AppError.Unknown(details = sanitizeTechnicalMessage(exception.message))
            }
        }
        
        logError(error, exception)
        return error
    }
    
    private fun sanitizeTechnicalMessage(message: String?): String? {
        if (message == null) return null
        
        // Remove known sensitive patterns (basic implementation)
        var sanitized = message
            .replace(Regex("key=[^&\\s]+"), "key=***")
            .replace(Regex("token=[^&\\s]+"), "token=***")
            .replace(Regex("Bearer\\s+[\\w\\-\\.]+"), "Bearer ***")
            .replace(Regex("password=[^&\\s]+"), "password=***")
            
        return sanitized
    }

    private fun logError(error: AppError, originalException: Throwable) {
        val logMessage = "ErrorRef: [${error.referenceId}] | Type: ${error.javaClass.simpleName} | Details: ${error.technicalDetails}"
        logger.e("ErrorHandler", logMessage, originalException)
    }
}
