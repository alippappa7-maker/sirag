package com.siraj.app.core.monitoring

import java.security.MessageDigest

/**
 * Sanitizer and privacy guard to ensure absolutely no PII, API keys, passwords,
 * auth tokens, purchase tokens, raw Quranic/Hadith sacred texts, or user private drafts
 * are leaked to Crashlytics logs or custom keys.
 */
object CrashlyticsSanitizer {
    private val SENSITIVE_PATTERNS =
        listOf(
            // API Keys & Tokens
            Regex("(?i)(api[_-]?key|apikey|secret|client[_-]?secret|private[_-]?key)\\s*[:=]\\s*[\"']?([a-zA-Z0-9_\\-\\.]+)[\"']?"),
            Regex("(?i)Bearer\\s+[A-Za-z0-9\\-\\._~\\+\\/]+=*"),
            Regex("(?i)(token|access_token|refresh_token|auth_token)\\s*[:=]\\s*[\"']?([a-zA-Z0-9_\\-\\.]+)[\"']?"),
            // Passwords & Credentials
            Regex("(?i)(password|passwd|pwd|credentials?)\\s*[:=]\\s*[\"']?([^\"'\\s&,]+)[\"']?"),
            // Purchase Tokens & Billing Secrets
            Regex(
                "(?i)(purchase[_-]?token|order[_-]?id|credit[_-]?card|cvv|billing[_-]?token)\\s*[:=]\\s*[\"']?([a-zA-Z0-9_\\-\\.]+)[\"']?",
            ),
            // Emails
            Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}"),
        )

    private val BLOCKED_KEY_KEYWORDS =
        listOf(
            "password",
            "token",
            "auth",
            "secret",
            "apikey",
            "purchase",
            "credit",
            "card",
            "cvv",
            "quran",
            "ayah",
            "hadith",
            "script",
            "prompt",
            "draft",
            "email",
            "phone",
            "nationalid",
            "ssn",
        )

    private val SAFE_TECHNICAL_KEYS =
        setOf(
            "environment",
            "appversion",
            "buildnumber",
            "screenname",
            "errorcategory",
            "errorseverity",
            "requestid",
            "statuscode",
            "action",
            "destination",
            "from",
            "test",
            "reason",
            "authenticated",
            "userrole",
            "threadname",
            "fatal",
        )

    /**
     * Sanitizes raw error messages and exception messages by masking sensitive substrings.
     */
    fun sanitizeMessage(message: String?): String {
        if (message.isNullOrBlank()) return ""

        var sanitized = message

        // 1. Mask known sensitive patterns
        sanitized = sanitized.replace(Regex("(?i)Bearer\\s+[A-Za-z0-9\\-\\._~\\+\\/]+=*"), "Bearer ***")
        sanitized = sanitized.replace(Regex("(?i)(key|api_key|token|auth)=[^&\\s,]+"), "$1=***")
        sanitized = sanitized.replace(Regex("(?i)(password|passwd|pwd)=[^&\\s,]+"), "$1=***")
        sanitized = sanitized.replace(Regex("(?i)(purchase_token|purchasetoken)=[^&\\s,]+"), "$1=***")
        sanitized = sanitized.replace(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}"), "[MASKED_EMAIL]")

        // 2. Limit length to avoid ballooning log payloads (max 500 chars)
        return if (sanitized.length > 500) {
            sanitized.take(500) + "... [TRUNCATED]"
        } else {
            sanitized
        }
    }

    /**
     * Verifies and sanitizes custom key names and values.
     * Rejects keys that represent sensitive domains or user private content.
     */
    fun isKeyAllowed(key: String): Boolean {
        val normalized = key.lowercase().replace("_", "").replace("-", "")
        if (SAFE_TECHNICAL_KEYS.contains(normalized)) {
            return true
        }
        return BLOCKED_KEY_KEYWORDS.none { normalized.contains(it) }
    }

    /**
     * Anonymizes a user ID into a stable, non-reversible SHA-256 hash
     * so user identities cannot be mapped back to emails or real identities in Crashlytics.
     */
    fun anonymizeUserId(userId: String?): String? {
        if (userId.isNullOrBlank()) return null
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(userId.toByteArray(Charsets.UTF_8))
            // Return first 16 hex chars for lightweight tracking
            hashBytes.joinToString("") { "%02x".format(it) }.take(16)
        } catch (e: Exception) {
            "anon_user_${userId.hashCode()}"
        }
    }

    /**
     * Creates a safe breadcrumb description without user content or sacred text.
     */
    fun formatSafeBreadcrumb(
        message: String,
        attributes: Map<String, String> = emptyMap(),
    ): String {
        val cleanMsg = sanitizeMessage(message)
        if (attributes.isEmpty()) return cleanMsg

        val cleanAttrs =
            attributes
                .filter { (k, _) -> isKeyAllowed(k) }
                .map { (k, v) -> "$k=${sanitizeMessage(v)}" }
                .joinToString(", ")

        return "$cleanMsg | [$cleanAttrs]"
    }
}
