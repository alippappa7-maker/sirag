package com.siraj.app.domain.models

enum class TemplateStatus {
    ACTIVE,
    DRAFT,
    ARCHIVED,
}

data class ContentTemplate(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val targetAudience: String = "عام",
    val requiredFields: List<String> = emptyList(),
    val recommendedPlatform: String = "TikTok / Reels (9:16)",
    val recommendedDuration: String = "قصير (أقل من دقيقة)",
    val sceneStyle: String = "موشن جرافيك",
    val hasQuran: Boolean = false,
    val hasHadith: Boolean = false,
    val hasFatwa: Boolean = false,
    val status: TemplateStatus = TemplateStatus.ACTIVE,
    val version: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

data class TemplateFavorite(
    val id: String = "", // userId_templateId
    val userId: String = "",
    val templateId: String = "",
    val savedAt: Long = System.currentTimeMillis(),
)
