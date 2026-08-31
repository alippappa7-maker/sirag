package com.siraj.app.core.error

/**
 * Centralized exception classification and error handling strategy.
 * Replaces broad catch(Exception) patterns with specific, actionable handling.
 */

/** Base class for all Siraj-specific exceptions */
sealed class SirajException(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {
    val severity: Severity get() = when (this) {
        is NetworkException -> if (statusCode in 500..599) Severity.HIGH else Severity.MEDIUM
        is AuthException -> Severity.HIGH
        is DataException -> Severity.MEDIUM
        is ContentException -> Severity.LOW
        is ShariaComplianceException -> Severity.CRITICAL
    }
}

class NetworkException(val statusCode: Int, message: String, cause: Throwable? = null) :
    SirajException(message, cause)

class AuthException(message: String, cause: Throwable? = null) :
    SirajException(message, cause)

class DataException(message: String, cause: Throwable? = null) :
    SirajException(message, cause)

class ContentException(message: String, cause: Throwable? = null) :
    SirajException(message, cause)

class ShariaComplianceException(message: String, cause: Throwable? = null) :
    SirajException(message, cause)

enum class Severity { LOW, MEDIUM, HIGH, CRITICAL }

/**
 * Centralized error handler — classifies caught exceptions
 * and routes them to appropriate channels (Crashlytics, logging, UI).
 */
object GlobalErrorHandler {

    fun classify(exception: Throwable): SirajException = when (exception) {
        is SirajException -> exception
        is java.net.UnknownHostException,
        is java.net.ConnectException,
        is java.net.SocketTimeoutException ->
            NetworkException(0, "فشل الاتصال بالشبكة", exception)
        is retrofit2.HttpException ->
            NetworkException(exception.code(), "خطأ في الخادم: ${exception.code()}", exception)
        is com.google.firebase.auth.FirebaseAuthException ->
            AuthException("خطأ في المصادقة: ${exception.message}", exception)
        is com.google.firebase.firestore.FirebaseFirestoreException ->
            DataException("خطأ في قاعدة البيانات: ${exception.message}", exception)
        is com.google.firebase.storage.StorageException ->
            DataException("خطأ في تخزين الملفات: ${exception.message}", exception)
        else -> DataException(exception.message ?: "خطأ غير معروف", exception)
    }

    fun handle(exception: Throwable, tag: String = "Siraj") {
        val classified = classify(exception)
        when (classified.severity) {
            Severity.CRITICAL -> {
                android.util.Log.e(tag, "CRITICAL: ${classified.message}", classified)
                reportToCrashlytics(classified)
            }
            Severity.HIGH -> {
                android.util.Log.e(tag, "HIGH: ${classified.message}", classified)
                reportToCrashlytics(classified)
            }
            Severity.MEDIUM -> {
                android.util.Log.w(tag, "MEDIUM: ${classified.message}", classified)
            }
            Severity.LOW -> {
                android.util.Log.i(tag, "LOW: ${classified.message}", classified)
            }
        }
    }

    private fun reportToCrashlytics(exception: SirajException) {
        runCatching {
            com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
                .recordException(exception)
        }
    }
}
