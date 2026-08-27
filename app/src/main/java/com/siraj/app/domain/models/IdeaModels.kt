package com.siraj.app.domain.models

enum class RiskLevel { LOW, MEDIUM, HIGH }

data class IdeaGenerationRequest(
    val subject: String = "",
    val audience: String = "عام",
    val platform: String = "TikTok / Reels (9:16)",
    val duration: String = "قصير (أقل من دقيقة)",
    val tone: String = "تحفيزي",
    val goal: String = "توعية",
    val hasReligiousElement: Boolean = false
)

data class GeneratedIdea(
    val id: String,
    val title: String,
    val hook: String,
    val summary: String,
    val audience: String,
    val suggestedScenes: Int,
    val requiredSources: List<String>,
    val riskLevel: RiskLevel,
    val needsReview: Boolean,
    val disclaimer: String?
)
