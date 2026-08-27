package com.siraj.app.domain.models

enum class ProjectStatus {
    DRAFT, PROCESSING, READY, EXPORTING, COMPLETED, FAILED, ARCHIVED, DELETED
}

enum class ProjectVisibility {
    PRIVATE, SHARED, PUBLIC
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
    val contentPlan: ContentPlan? = null
)

enum class MemberRole {
    VIEWER, EDITOR, ADMIN
}

data class ProjectMember(
    val id: String = "",
    val projectId: String = "",
    val userId: String = "",
    val role: MemberRole = MemberRole.VIEWER,
    val addedAt: Long = System.currentTimeMillis()
)

data class ProjectVersion(
    val id: String = "",
    val projectId: String = "",
    val createdBy: String = "",
    val description: String = "",
    val sceneSnapshotIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

enum class AssetType {
    IMAGE, VIDEO, AUDIO, DOCUMENT
}

data class ProjectAsset(
    val id: String = "",
    val projectId: String = "",
    val uploaderId: String = "",
    val type: AssetType = AssetType.IMAGE,
    val storagePath: String = "",
    val url: String = "",
    val filename: String = "",
    val sizeBytes: Long = 0L,
    val durationMs: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ActivityType {
    CREATED, EDITED, STATUS_CHANGED, MEMBER_ADDED, ASSET_UPLOADED, VERSION_CREATED, ARCHIVED, RESTORED, DELETED
}

data class ProjectActivity(
    val id: String = "",
    val projectId: String = "",
    val userId: String = "",
    val type: ActivityType = ActivityType.EDITED,
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis()
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
    val hasFatwa: Boolean = false
)
