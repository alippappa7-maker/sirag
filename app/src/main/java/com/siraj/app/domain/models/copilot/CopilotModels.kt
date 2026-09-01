package com.siraj.app.domain.models.copilot

/**
 * المساعد الإسلامي الذكي — يجيب من مصادر موثّقة فقط
 */
data class CopilotMessage(
    val id: String,
    val role: CopilotRole,
    val content: String,
    val sources: List<CopilotSource>,
    val timestamp: Long,
    val isLoading: Boolean = false,
)

enum class CopilotRole { USER, ASSISTANT }

data class CopilotSource(
    val type: CopilotSourceType,
    val title: String,
    val reference: String,
    val excerpt: String,
    val url: String? = null,
)

enum class CopilotSourceType {
    QURAN,      // آية قرآنية
    HADITH,     // حديث نبوي
    TAFSIR,     // تفسير
    FIQH,       // فقه
    DUA,        // دعاء
}

/**
 * جلسة محادثة
 */
data class CopilotConversation(
    val id: String,
    val title: String,
    val messages: List<CopilotMessage>,
    val createdAt: Long,
    val updatedAt: Long,
)

/**
 * نموذج الإدخال للسؤال
 */
data class CopilotQuery(
    val text: String,
    val language: String = "ar",
    val includeQuran: Boolean = true,
    val includeHadith: Boolean = true,
    val includeTafsir: Boolean = true,
)

/**
 * الرد المولّد
 */
data class CopilotResponse(
    val answer: String,
    val sources: List<CopilotSource>,
    val confidence: Float,
    val followUpQuestions: List<String>,
)
