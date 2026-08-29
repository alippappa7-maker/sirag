package com.siraj.app.domain.models

enum class AssetType {
    IMAGE, VIDEO, AUDIO, MUSIC, FONT, SUBTITLE, QURAN_RECITATION
}

enum class AssetStatus {
    UPLOADING, READY, ERROR, DELETED
}

enum class RightsStatus {
    UNKNOWN,
    PENDING_VERIFICATION,
    PERSONAL_USE,
    COMMERCIAL_ALLOWED,
    ATTRIBUTION_REQUIRED,
    EXPIRED,
    RESTRICTED,
    REJECTED
}

data class Asset(
    val id: String = java.util.UUID.randomUUID().toString(),
    val ownerId: String = "",
    val workspaceId: String = "",
    val projectId: String = "",
    val type: AssetType = AssetType.IMAGE,
    val storagePath: String = "",
    val downloadUrl: String = "",
    val thumbnailUrl: String? = null,
    val mimeType: String = "",
    val sizeBytes: Long = 0L,
    val durationMs: Long? = null,
    
    // Rights & License Management Fields
    val sourceUrl: String = "",
    val creatorName: String = "",
    val provider: String = "",
    val license: String = "",
    val commercialUseAllowed: Boolean = false,
    val modificationAllowed: Boolean = false,
    val attributionRequired: Boolean = false,
    val attribution: String = "",
    val proofUrl: String = "", 
    val acquiredAt: Long? = null,
    val expiresAt: Long? = null,
    val rightsStatus: RightsStatus = RightsStatus.UNKNOWN,
    val usageRestrictions: String = "",

    val status: AssetStatus = AssetStatus.READY,
    val createdAt: Long = System.currentTimeMillis()
)
