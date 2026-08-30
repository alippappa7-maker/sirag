package com.siraj.app.domain.models.flash

import java.util.UUID

enum class FlashVisibility(val titleArabic: String) {
    PUBLIC("عام"),
    UNLISTED("غير مدرج"),
    PRIVATE("خاص")
}

enum class FlashPublishingState(val titleArabic: String) {
    DRAFT("مسودة"),
    UPLOADED("تم الرفع"),
    SCANNING("قيد الفحص الآلي"),
    PENDING_REVIEW("قيد المراجعة"),
    APPROVED("معتمد"),
    LIMITED("محدود الظهور"),
    SCHEDULED("مجدول"),
    PUBLISHED("منشور"),
    REJECTED("مرفوض"),
    SUSPENDED("موقوف"),
    REMOVED("محذوف"),
    APPEALED("قيد الاستئناف"),
    RESTORED("مستعاد"),
    CORRECTED("مصحح"),
    ARCHIVED("مؤرشف")
}

data class FlashMetrics(
    val views: Int = 0,
    val likes: Int = 0,
    val saves: Int = 0,
    val shares: Int = 0
)

data class FlashSourceInfo(
    val sourceId: String,
    val title: String,
    val verificationStatus: FlashPublishingState,
    val reviewedAt: Long? = null
)

data class FlashAuditLog(
    val id: String = UUID.randomUUID().toString(),
    val flashId: String,
    val fromState: FlashPublishingState?,
    val toState: FlashPublishingState,
    val actionBy: String,
    val reason: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class Flash(
    val id: String = UUID.randomUUID().toString(),
    val creatorId: String,
    val creatorName: String,
    val workspaceId: String,
    val videoUrl: String, 
    val videoAssetId: String,
    val title: String,
    val description: String,
    val category: String,
    val tags: List<String> = emptyList(),
    val sourceInfo: FlashSourceInfo?, 
    val publishingState: FlashPublishingState = FlashPublishingState.DRAFT,
    val rejectionReason: String? = null,
    val durationMs: Long,
    val thumbnailUrl: String?,
    val visibility: FlashVisibility = FlashVisibility.PUBLIC,
    val showCreatorInfo: Boolean = true,
    val scheduledAt: Long? = null,
    val publishedAt: Long? = null,
    val metrics: FlashMetrics = FlashMetrics(),
    val isLikedByMe: Boolean = false,
    val isSavedByMe: Boolean = false
)

data class FlashesFeedResult(
    val flashes: List<Flash>,
    val nextPageToken: String?,
    val hasMore: Boolean
)
