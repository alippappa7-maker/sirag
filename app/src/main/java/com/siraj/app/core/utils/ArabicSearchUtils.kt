package com.siraj.app.core.utils

object ArabicSearchUtils {

    // Regex for Arabic diacritics / Tashkeel
    private val TASHKEEL_REGEX = "[\\u0617-\\u061A\\u064B-\\u0652\\u0670]".toRegex()
    private val TATWEEL_REGEX = "\\u0640".toRegex()

    /**
     * تطبيع النص العربي للبحث المرن (إزالة التشكيل، توحيد الألفات والياء والتاء المربوطة)
     */
    fun normalizeArabic(input: String?): String {
        if (input.isNullOrBlank()) return ""
        return input
            .replace(TASHKEEL_REGEX, "")
            .replace(TATWEEL_REGEX, "")
            .replace('أ', 'ا')
            .replace('إ', 'ا')
            .replace('آ', 'ا')
            .replace('ٱ', 'ا')
            .replace('ى', 'ي')
            .replace('ة', 'ه')
            .replace('ؤ', 'و')
            .replace('ئ', 'ي')
            .trim()
            .lowercase()
    }

    /**
     * فحص مطابقة الاستعلام للنص بعد التطبيع
     */
    fun matches(text: String?, query: String): Boolean {
        if (text.isNullOrBlank() || query.isBlank()) return false
        val normalizedText = normalizeArabic(text)
        val normalizedQuery = normalizeArabic(query)

        // Exact phrase match
        if (normalizedText.contains(normalizedQuery)) return true

        // Multi-word token match (All tokens must match)
        val tokens = normalizedQuery.split("\\s+".toRegex()).filter { it.isNotBlank() }
        if (tokens.isEmpty()) return false
        return tokens.all { normalizedText.contains(it) }
    }

    /**
     * حساب درجة الصلة بالاستعلام (Relevance Score) لترتيب النتائج بدقة
     */
    fun calculateScore(title: String, body: String, query: String): Int {
        val normQuery = normalizeArabic(query)
        val normTitle = normalizeArabic(title)
        val normBody = normalizeArabic(body)

        if (normQuery.isBlank()) return 0
        var score = 0

        // Exact title match
        if (normTitle == normQuery) {
            score += 100
        } else if (normTitle.startsWith(normQuery)) {
            score += 70
        } else if (normTitle.contains(normQuery)) {
            score += 50
        }

        // Exact body match
        if (normBody.contains(normQuery)) {
            score += 20
        }

        // Token matches
        val tokens = normQuery.split("\\s+".toRegex()).filter { it.isNotBlank() }
        tokens.forEach { token ->
            if (normTitle.contains(token)) score += 15
            if (normBody.contains(token)) score += 5
        }

        return score
    }

    /**
     * استخراج مقتطف نصي حول موضع التطابق (Snippet with context)
     */
    fun extractSnippet(fullText: String, query: String, maxLen: Int = 120): String {
        if (fullText.isBlank()) return ""
        val normText = normalizeArabic(fullText)
        val normQuery = normalizeArabic(query)

        val idx = normText.indexOf(normQuery)
        if (idx == -1) {
            return if (fullText.length > maxLen) fullText.take(maxLen) + "..." else fullText
        }

        val start = (idx - 30).coerceAtLeast(0)
        val end = (idx + normQuery.length + 60).coerceAtMost(fullText.length)

        var snippet = fullText.substring(start, end).trim()
        if (start > 0) snippet = "...$snippet"
        if (end < fullText.length) snippet = "$snippet..."
        return snippet
    }
}
