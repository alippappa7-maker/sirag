package com.siraj.app.domain.models.admin

data class AuditLogEntry(
    val id: String,
    val entityId: String,
    val entityType: String,
    val action: String,
    val performedByUserId: String,
    val performedByRole: String,
    val timestamp: Long,
    val previousState: String?,
    val newState: String?,
)

enum class AdminContentStatus {
    PENDING_REVIEW,
    APPROVED,
    SUSPENDED,
    ARCHIVED,
    REJECTED,
}

data class AdminContentItem(
    val id: String,
    val title: String,
    val type: String, // "FLASH", "AUDIO", "TEMPLATE", "SOURCE"
    val status: AdminContentStatus,
    val ownerId: String,
    val createdAt: Long,
    val isReligiousText: Boolean = false,
    val isPrivate: Boolean = false,
)

data class ContentManagementFilter(
    val query: String = "",
    val type: String? = null,
    val status: AdminContentStatus? = null,
)
