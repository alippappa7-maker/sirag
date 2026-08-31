package com.siraj.app.domain.models

data class ProjectPreview(
    val id: String,
    val title: String,
    val description: String,
    val lastModified: String,
)

data class VideoPreview(
    val id: String,
    val title: String,
    val duration: String,
    val thumbnailUrl: String? = null,
)

data class AudioItem(
    val id: String,
    val title: String,
    val reciter: String,
    val duration: String,
)

data class FlashItem(
    val id: String,
    val content: String,
    val author: String,
    val timestamp: String,
)

data class SourcePreview(
    val id: String,
    val title: String,
    val author: String,
    val verificationStatus: VerificationStatus,
)

enum class VerificationStatus(
    val label: String,
) {
    VERIFIED("موثق"),
    PENDING("قيد المراجعة"),
    REJECTED("مرفوض"),
}

data class NotificationPreview(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val isRead: Boolean,
)

data class SubscriptionPreview(
    val id: String,
    val planName: String,
    val status: String,
    val expiryDate: String,
)
