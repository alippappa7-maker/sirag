package com.siraj.app.core.security

import android.util.Log
import java.util.regex.Pattern

/**
 * مسجل السجلات الآمن (Sanitized Logger)
 * يقوم بتطهير وحجب أي مفاتيح سرية أو رموز اعتماد (Tokens/Keys/Passwords)
 * تلقائياً لمنع ظهورها في Logcat أو Crashlytics أو شاشات الأخطاء.
 */
object SanitizedLogger {

    private const val REDACTED_MASK = "[REDACTED_SECRET]"

    // أنماط الأسرار والمفاتيح الحساسة لاكتشافها وحجبها
    private val SECRET_PATTERNS = listOf(
        // Google API Keys / Firebase Keys (AIzaSy...)
        Pattern.compile("AIza[0-9A-Za-z\\-_]{30,45}"),
        // OpenAI / Generic sk- keys
        Pattern.compile("sk-[0-9A-Za-z]{20,60}"),
        // Bearer Tokens
        Pattern.compile("(?i)Bearer\\s+[0-9A-Za-z\\-_.~+/]+=*"),
        // Authorization Headers
        Pattern.compile("(?i)(authorization|api[_-]?key|secret|token|password|passwd|private[_-]?key)[\"':\\s=]+[\"']?([^\"'\\s,;]+)[\"']?"),
        // Private Key blocks
        Pattern.compile("-----BEGIN (RSA |EC |DSA |OPENSSH |ENCRYPTED )?PRIVATE KEY-----[\\s\\S]*?-----END (RSA |EC |DSA |OPENSSH |ENCRYPTED )?PRIVATE KEY-----"),
        // Generic JWT tokens
        Pattern.compile("ey[A-Za-z0-9-_=]+\\.ey[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_.+/=]*"),
        // Database connection strings with credentials (postgres://user:pass@host)
        Pattern.compile("(?i)(mongodb|postgres|mysql|redis)://[^:]+:([^@]+)@")
    )

    /**
     * تطهير أي نص من المفاتيح والأسرار
     */
    fun sanitize(message: String?): String {
        if (message.isNullOrEmpty()) return ""
        var sanitized = message

        // 1. تطهير حقول authorization / token / secret / password
        val authMatcher = Pattern.compile("(?i)(authorization|api[_-]?key|secret|token|password|passwd|private[_-]?key)[\"':\\s=]+[\"']?([^\"'\\s,;]+)[\"']?").matcher(sanitized)
        val sb = StringBuffer()
        while (authMatcher.find()) {
            val keyName = authMatcher.group(1)
            val replacement = "$keyName: \"$REDACTED_MASK\""
            authMatcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(replacement))
        }
        authMatcher.appendTail(sb)
        sanitized = sb.toString()

        // 2. تطهير كافة الأنماط الثابتة
        for (pattern in SECRET_PATTERNS) {
            sanitized = pattern.matcher(sanitized).replaceAll(REDACTED_MASK)
        }

        return sanitized
    }

    fun d(tag: String, message: String) {
        val safeTag = sanitize(tag)
        val safeMessage = sanitize(message)
        try {
            Log.d(safeTag, safeMessage)
        } catch (_: Throwable) {
            // JVM test fallback
        }
    }

    fun i(tag: String, message: String) {
        val safeTag = sanitize(tag)
        val safeMessage = sanitize(message)
        try {
            Log.i(safeTag, safeMessage)
        } catch (_: Throwable) {
            // JVM test fallback
        }
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        val safeTag = sanitize(tag)
        val safeMessage = sanitize(message)
        try {
            if (throwable != null) {
                val safeException = sanitize(throwable.localizedMessage ?: throwable.message ?: throwable.javaClass.simpleName)
                Log.w(safeTag, "$safeMessage - Exception: $safeException")
            } else {
                Log.w(safeTag, safeMessage)
            }
        } catch (_: Throwable) {
            // JVM test fallback
        }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        val safeTag = sanitize(tag)
        val safeMessage = sanitize(message)
        try {
            if (throwable != null) {
                val safeException = sanitize(throwable.localizedMessage ?: throwable.message ?: throwable.javaClass.simpleName)
                Log.e(safeTag, "$safeMessage - Exception: $safeException")
            } else {
                Log.e(safeTag, safeMessage)
            }
        } catch (_: Throwable) {
            // JVM test fallback
        }
    }
}
