package com.siraj.app.domain.models

enum class ProjectStatus {
    DRAFT,
    PROCESSING,
    READY,
    EXPORTING,
    COMPLETED,
    FAILED,
    ARCHIVED,
    DELETED,
}

enum class ProjectVisibility {
    PRIVATE,
    SHARED,
    PUBLIC,
}

data class Project(
    val id: String = "",
    val ownerId: String = "",
    val workspaceId: String = "",
    val title: String = "",
    val description: String = "",
    val status: ProjectStatus = ProjectStatus.DRAFT,
    val reviewState: ReviewState = ReviewState.DRAFT,
    val reviewLogs: List<ReviewLog> = emptyList(),
    val thumbnailUrl: String? = null,
    val sceneCount: Int = 0,
    val durationMs: Long = 0L,
    val aspectRatio: String = "16:9",
    val visibility: ProjectVisibility = ProjectVisibility.PRIVATE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val archivedAt: Long? = null,
    val currentVersionId: String? = null,
    val brief: ContentBrief = ContentBrief(),
    val contentPlan: ContentPlan? = null,
    val scenes: List<Scene> = emptyList(),
)

enum class MemberRole {
    VIEWER,
    EDITOR,
    ADMIN,
}

data class ProjectMember(
    val id: String = "",
    val projectId: String = "",
    val userId: String = "",
    val role: MemberRole = MemberRole.VIEWER,
    val addedAt: Long = System.currentTimeMillis(),
)

data class ProjectVersion(
    val id: String = "",
    val projectId: String = "",
    val createdBy: String = "",
    val description: String = "",
    val sceneSnapshotIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
)

enum class ActivityType {
    CREATED,
    EDITED,
    STATUS_CHANGED,
    MEMBER_ADDED,
    ASSET_UPLOADED,
    VERSION_CREATED,
    ARCHIVED,
    RESTORED,
    DELETED,
}

data class ProjectActivity(
    val id: String = "",
    val projectId: String = "",
    val userId: String = "",
    val type: ActivityType = ActivityType.EDITED,
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis(),
)

data class ContentBrief(
    val idea: String = "",
    val contentType: String = "فيديو",
    val targetAudience: String = "عام",
    val language: String = "العربية الفصحى",
    val duration: String = "قصير (أقل من دقيقة)",
    val platform: String = "TikTok / Reels (9:16)",
    val visualStyle: String = "موشن جرافيك",
    val voiceType: String = "صوت رجالي رخيم",
    val template: String = "فارغ",
    val hasQuran: Boolean = false,
    val hasHadith: Boolean = false,
    val hasFatwa: Boolean = false,
)

enum class SceneStatus {
    DRAFT,
    GENERATED,
    EDITED,
    APPROVED,
    FAILED,
}

enum class BackgroundType {
    IMAGE,
    VIDEO,
    SOLID_COLOR,
    GRADIENT,
    BLUR,
}

enum class TransitionType {
    NONE,
    FADE,
    SLIDE,
    WIPE,
    ZOOM,
    DISSOLVE,
}

data class Scene(
    val id: String =
        java.util.UUID
            .randomUUID()
            .toString(),
    val projectId: String,
    val versionId: String = "1",
    val orderIndex: Int = 0,
    val title: String = "",
    val narrationText: String = "",
    val durationMs: Long = 5000L,
    val transition: TransitionType = TransitionType.FADE,
    val backgroundType: BackgroundType = BackgroundType.IMAGE,
    val status: SceneStatus = SceneStatus.DRAFT,
    val claimIds: List<String> = emptyList(),
    val assetIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

data class SceneAsset(
    val id: String =
        java.util.UUID
            .randomUUID()
            .toString(),
    val sceneId: String = "",
    val projectId: String = "",
    val type: AssetType = AssetType.IMAGE,
    val url: String = "",
    val durationMs: Long? = null,
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)

data class SceneText(
    val id: String =
        java.util.UUID
            .randomUUID()
            .toString(),
    val sceneId: String = "",
    val text: String = "",
    val type: String = "caption", // narration, caption, overlay
    val startTimeMs: Long = 0L,
    val durationMs: Long = 5000L,
    val isIslamicText: Boolean = false,
    val sourceTextId: String? = null,
    val fontFamily: String = "Default",
    val fontSize: Float = 16f,
    val fontColor: String = "#FFFFFF",
    val alignment: String = "Center",
    val position: String = "Bottom",
    val showSource: Boolean = false,
)

data class SceneAudio(
    val id: String =
        java.util.UUID
            .randomUUID()
            .toString(),
    val sceneId: String = "",
    val url: String = "",
    val type: String = "voiceover", // voiceover, background_music, sfx
    val startTimeMs: Long = 0L,
    val durationMs: Long? = null,
    val volume: Float = 1.0f,
)
