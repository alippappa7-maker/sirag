package com.siraj.app.domain.models

enum class RiskLevel { LOW, MEDIUM, HIGH }

enum class SceneType { NARRATION, VISUAL, QURAN, TEXT, TRANSITION }

data class IdeaGenerationRequest(
    val subject: String = "",
    val audience: String = "عام",
    val platform: String = "TikTok / Reels (9:16)",
    val duration: String = "قصير (أقل من دقيقة)",
    val tone: String = "تحفيزي",
    val goal: String = "توعية",
    val hasReligiousElement: Boolean = false,
)

data class GeneratedIdea(
    val id: String,
    val title: String,
    val hook: String,
    val summary: String,
    val audience: String,
    val suggestedScenes: List<SuggestedScene> = emptyList(),
    val requiredSources: List<String>,
    val riskLevel: RiskLevel,
    val needsReview: Boolean,
    val disclaimer: String?,
    val platform: String = "",
    val tone: String = "",
    val estimatedDuration: String = "",
    val tags: List<String> = emptyList(),
)

data class SuggestedScene(
    val order: Int = 0,
    val title: String = "",
    val description: String = "",
    val durationSeconds: Int = 0,
    val type: SceneType = SceneType.NARRATION,
)

data class ScenarioPlan(
    val ideaId: String = "",
    val title: String = "",
    val overview: String = "",
    val scenes: List<ScenarioScene> = emptyList(),
    val totalDurationSeconds: Int = 0,
    val openingHook: String = "",
    val closingCTA: String = "",
    val visualStyle: String = "",
    val musicMood: String = "",
)

data class ScenarioScene(
    val id: String = "",
    val order: Int = 0,
    val title: String = "",
    val narration: String = "",
    val visualDescription: String = "",
    val durationSeconds: Int = 0,
    val type: SceneType = SceneType.NARRATION,
    val transitions: String = "",
    val overlayText: String? = null,
    val arabicText: String? = null,
    val arabicSource: String? = null,
)
