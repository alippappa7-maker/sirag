package com.siraj.app.domain.models

enum class WorkspaceType {
    PERSONAL, TEAM
}

enum class WorkspaceRole {
    OWNER, MANAGER, EDITOR, REVIEWER, VIEWER
}

enum class InvitationStatus {
    PENDING, ACCEPTED, REJECTED, EXPIRED
}

data class Workspace(
    val id: String = "",
    val name: String = "",
    val type: WorkspaceType = WorkspaceType.PERSONAL,
    val ownerId: String = "",
    val memberIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "ACTIVE" // ACTIVE, ARCHIVED
)

data class WorkspaceMember(
    val id: String = "", // workspaceId_userId
    val workspaceId: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val userName: String = "",
    val role: WorkspaceRole = WorkspaceRole.VIEWER,
    val joinedAt: Long = System.currentTimeMillis()
)

data class WorkspaceInvitation(
    val id: String = "",
    val workspaceId: String = "",
    val inviterId: String = "",
    val email: String = "",
    val role: WorkspaceRole = WorkspaceRole.VIEWER,
    val status: InvitationStatus = InvitationStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L // 7 days
)
