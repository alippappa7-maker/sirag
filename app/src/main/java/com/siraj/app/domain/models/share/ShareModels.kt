package com.siraj.app.domain.models.share

import java.util.UUID

enum class ShareType(
    val path: String,
) {
    PROJECT("project"),
    VIDEO("video"),
    FLASH("flash"),
    AUDIO("audio"),
    QURAN("quran"),
    SOURCE("source"),
    TEMPLATE("template"),
    ;

    companion object {
        fun fromPath(path: String?): ShareType? = values().find { it.path == path }
    }
}

enum class ShareStatus {
    ACTIVE,
    REVOKED,
    EXPIRED,
    UNAVAILABLE,
}

data class ShareLink(
    val id: String = UUID.randomUUID().toString(),
    val targetId: String,
    val type: ShareType,
    val token: String? = null,
    val createdBy: String,
    val isPrivate: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null,
    val status: ShareStatus = ShareStatus.ACTIVE,
    val accessCount: Int = 0,
) {
    val isExpired: Boolean
        get() = expiresAt != null && System.currentTimeMillis() > expiresAt

    val isActive: Boolean
        get() = status == ShareStatus.ACTIVE && !isExpired
}
