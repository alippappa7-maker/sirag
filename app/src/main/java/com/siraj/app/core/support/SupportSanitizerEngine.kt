package com.siraj.app.core.support

import com.siraj.app.domain.models.support.*
import java.security.SecureRandom
import java.util.Calendar

/**
 * Support Sanitizer and Routing Engine for Siraj Platform
 * Ensures tickets route to proper restricted teams, strips secrets from logs,
 * and maintains strict privacy & Sharia boundaries.
 */
object SupportSanitizerEngine {

    private val random = SecureRandom()

    private val SECRET_PATTERNS = listOf(
        Regex("(?i)(password|passwd|pwd|secret|token|api[_-]?key|bearer|auth[_-]?key)\\s*[:=]\\s*['\"]?([^'\"\\s]+)['\"]?"),
        Regex("(?i)AIza[0-9A-Za-z-_]{35}"), // Google / Firebase API Key pattern
        Regex("(?i)ya29\\.[0-9A-Za-z-_]+"), // Google OAuth Access Token pattern
        Regex("(?i)sk-[a-zA-Z0-9]{32,}")   // General secret key pattern
    )

    const val SHARIA_DISCLAIMER_TEXT = "تنبيه هام: مركز الدعم والمساعدة الفني مخصص لتقديم الحلول التقنية والإدارية فقط، ولا يُعد جهة إفتاء أو إصدار أحكام شرعية. كافة البلاغات الشرعية تُحال مباشرة إلى هيئة التدقيق والمراجعة الشرعية المختصة."
    const val PASSWORD_WARNING_TEXT = "تحذير أمني: موظفو سراج لن يطلبوا منك كلمة المرور أو مفاتيحك الخاصة إطلاقاً. لا تشارك أي أسرار في تذاكر الدعم."

    /**
     * Generates an enterprise-compliant, easily readable ticket identifier.
     * Format: SRJ-TKT-2026-XXXX
     */
    fun generateTicketNumber(): String {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val randomNum = 1000 + random.nextInt(9000)
        return "SRJ-TKT-$year-$randomNum"
    }

    /**
     * Determines the strictly authorized target team for a ticket.
     */
    fun determineTargetTeam(category: TicketCategory): TicketTargetTeam {
        return category.defaultTeam
    }

    /**
     * Sanitizes raw logs by redacting any sensitive data, passwords, or tokens.
     */
    fun sanitizeLogLines(rawLogs: List<String>): List<String> {
        return rawLogs.map { line ->
            var sanitized = line
            SECRET_PATTERNS.forEach { regex ->
                sanitized = regex.replace(sanitized) { matchResult ->
                    val fullMatch = matchResult.value
                    if (fullMatch.contains("=")) {
                        val key = fullMatch.substringBefore("=")
                        "$key=[REDACTED_SECRET]"
                    } else if (fullMatch.contains(":")) {
                        val key = fullMatch.substringBefore(":")
                        "$key:[REDACTED_SECRET]"
                    } else {
                        "[REDACTED_CREDENTIAL]"
                    }
                }
            }
            sanitized
        }
    }

    /**
     * Validates that ticket submission adheres to safety rules.
     */
    fun validateTicketInput(subject: String, description: String): Result<Unit> {
        if (subject.trim().length < 4) {
            return Result.failure(IllegalArgumentException("عنوان التذكرة يجب أن لا يقل عن 4 أحرف"))
        }
        if (description.trim().length < 10) {
            return Result.failure(IllegalArgumentException("يرجى كتابة تفاصيل واضحة للمشكلة (10 أحرف على الأقل)"))
        }
        return Result.success(Unit)
    }

    /**
     * Validates rating input.
     */
    fun validateRating(stars: Int): Result<Unit> {
        if (stars !in 1..5) {
            return Result.failure(IllegalArgumentException("التقييم يجب أن يكون بين 1 و 5 نجوم"))
        }
        return Result.success(Unit)
    }
}
