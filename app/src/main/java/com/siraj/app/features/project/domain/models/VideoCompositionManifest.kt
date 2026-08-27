package com.siraj.app.features.project.domain.models

enum class CompositionTransitionType(val labelArabic: String, val ffmpegFilter: String) {
    CUT("قطع مباشر", "none"),
    FADE("تلاشي تدريجي (Fade)", "fade"),
    DISSOLVE("تداخل سينمائي (Dissolve)", "dissolve"),
    SLIDE("انزلاق سلس (Slide)", "slideleft")
}

enum class CompositionVisualType {
    IMAGE,
    VIDEO_CLIP,
    COLOR_SOLID
}

data class CompositionSceneItem(
    val sceneId: String = "",
    val orderIndex: Int = 0,
    val durationMs: Long = 5000L,
    val visualUrl: String = "",
    val visualType: CompositionVisualType = CompositionVisualType.IMAGE,
    val transitionType: CompositionTransitionType = CompositionTransitionType.DISSOLVE,
    val transitionDurationMs: Long = 500L,
    val hasMotionEffect: Boolean = true,
    val zoomDirection: String = "in" // in, out, static
)

data class CompositionSfxItem(
    val sfxId: String = "",
    val name: String = "",
    val sfxUrl: String = "",
    val startOffsetMs: Long = 0L,
    val volume: Float = 0.8f
)

data class CompositionAudioTrack(
    val voiceoverUrl: String? = null,
    val recitationUrl: String? = null,
    val voiceVolume: Float = 1.0f,
    val soundtrackUrl: String? = null,
    val soundtrackVolume: Float = 0.25f,
    val soundtrackLoop: Boolean = true,
    val sfxTracks: List<CompositionSfxItem> = emptyList()
)

data class CompositionSubtitleItem(
    val subtitleId: String = "",
    val text: String = "",
    val startMs: Long = 0L,
    val endMs: Long = 0L,
    val isQuranic: Boolean = false,
    val positionBottomDp: Int = 48,
    val textColorHex: String = "#FFFFFF",
    val backgroundColorHex: String = "#80000000"
)

data class CompositionBranding(
    val isIslamicVerified: Boolean = false, // True فقط إذا كانت حالة المراجعة APPROVED
    val sourceCitationText: String? = null, // مثال: "سورة البقرة - تفسير ابن كثير"
    val showWatermark: Boolean = true,
    val attributionCredits: List<String> = emptyList()
)

data class VideoCompositionManifest(
    val manifestId: String = "",
    val jobId: String = "",
    val projectId: String = "",
    val projectTitle: String = "",
    val projectVersion: Int = 1,
    val aspectRatio: String = "9:16",
    val quality: ProductionQuality = ProductionQuality.FHD_1080P,
    val resolutionWidth: Int = 1080,
    val resolutionHeight: Int = 1920,
    val fps: Int = 30,
    val videoCodec: String = "libx264",
    val audioCodec: String = "aac",
    val scenes: List<CompositionSceneItem> = emptyList(),
    val audioMix: CompositionAudioTrack = CompositionAudioTrack(),
    val subtitles: List<CompositionSubtitleItem> = emptyList(),
    val burnSubtitles: Boolean = true,
    val branding: CompositionBranding = CompositionBranding(),
    val isPreviewOnly: Boolean = false,
    val totalDurationMs: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
