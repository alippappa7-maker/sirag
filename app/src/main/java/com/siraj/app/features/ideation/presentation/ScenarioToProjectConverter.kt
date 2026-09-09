package com.siraj.app.features.ideation.presentation

import com.siraj.app.domain.models.*

/**
 * أداة تحويل سيناريو Gemini إلى مشاهد جاهزة للمشروع.
 */
object ScenarioToProjectConverter {

    /**
     * تحويل سيناريو كامل إلى مشاهد مشروع.
     */
    fun convertScenarioToScenes(
        projectId: String,
        scenario: ScenarioPlan,
    ): List<Scene> {
        return scenario.scenes.mapIndexed { index, scene ->
            Scene(
                projectId = projectId,
                orderIndex = index,
                title = scene.title,
                narrationText = scene.narration,
                durationMs = (scene.durationSeconds * 1000).toLong(),
                transition = parseTransition(scene.transitions),
                backgroundType = parseBackgroundType(scene.type),
                status = SceneStatus.DRAFT,
            )
        }
    }

    /**
     * تحويل سيناريو كامل إلى محتوى Brief غني.
     */
    fun scenarioToContentBrief(
        scenario: ScenarioPlan,
        audience: String,
    ): ContentBrief {
        val hasQuran = scenario.scenes.any { it.type == SceneType.QURAN }
        val hasHadith = scenario.scenes.any {
            it.arabicSource?.contains("حديث") == true ||
                it.arabicSource?.contains("بخاري") == true ||
                it.arabicSource?.contains("مسلم") == true
        }

        return ContentBrief(
            idea = scenario.overview + "\n\nالخطاف: " + scenario.openingHook,
            contentType = "فيديو",
            targetAudience = audience,
            language = "العربية الفصحى",
            duration = formatDuration(scenario.totalDurationSeconds),
            platform = "TikTok / Reels (9:16)",
            visualStyle = scenario.visualStyle.ifBlank { "موشن جرافيك" },
            voiceType = "صوت رجالي رخيم",
            template = "فارغ",
            hasQuran = hasQuran,
            hasHadith = hasHadith,
            hasFatwa = false,
        )
    }

    private fun formatDuration(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return when {
            minutes >= 10 -> "طويل جداً (أكثر من 10 دقائق)"
            minutes >= 3 -> "طويل (3-10 دقائق)"
            minutes >= 1 -> "متوسط (1-3 دقائق)"
            else -> "قصير (أقل من دقيقة)"
        }
    }

    private fun parseTransition(transition: String): TransitionType = when {
        transition.contains("قطع") || transition.contains("cut") -> TransitionType.CUT
        transition.contains("تلاشي") || transition.contains("fade") -> TransitionType.FADE
        transition.contains("انزلاق") || transition.contains("slide") -> TransitionType.SLIDE_LEFT
        transition.contains("تدوير") || transition.contains("spin") -> TransitionType.CROSS_DISSOLVE
        else -> TransitionType.FADE
    }

    private fun parseBackgroundType(type: SceneType): BackgroundType = when (type) {
        SceneType.VISUAL -> BackgroundType.IMAGE
        SceneType.QURAN -> BackgroundType.SOLID
        SceneType.TEXT -> BackgroundType.SOLID
        SceneType.NARRATION -> BackgroundType.IMAGE
        SceneType.TRANSITION -> BackgroundType.SOLID
    }
}
