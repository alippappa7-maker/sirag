package com.siraj.app.domain.models

enum class AssetType {
    IMAGE, VIDEO, AUDIO, MUSIC, FONT, SUBTITLE
}

enum class AssetStatus {
    UPLOADING, READY, ERROR, DELETED
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
    val sourceUrl: String = "",
    val license: String = "",
    val attribution: String = "",
    val status: AssetStatus = AssetStatus.READY,
    val createdAt: Long = System.currentTimeMillis()
)
