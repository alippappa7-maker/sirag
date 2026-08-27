package com.siraj.app.domain.repository

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.*
import kotlinx.coroutines.flow.Flow

interface WorkspaceRepository {
    fun getUserWorkspaces(userId: String): Flow<Resource<List<Workspace>>>
    fun getWorkspaceMembers(workspaceId: String): Flow<Resource<List<WorkspaceMember>>>
    fun getWorkspaceInvitations(workspaceId: String): Flow<Resource<List<WorkspaceInvitation>>>
    fun getUserInvitations(email: String): Flow<Resource<List<WorkspaceInvitation>>>
    
    suspend fun createWorkspace(name: String, type: WorkspaceType, owner: UserProfile): Resource<String>
    suspend fun inviteMember(workspaceId: String, email: String, role: WorkspaceRole, inviterId: String): Resource<Unit>
    suspend fun respondToInvitation(invitationId: String, accept: Boolean, user: UserProfile): Resource<Unit>
    
    suspend fun updateMemberRole(workspaceId: String, userId: String, newRole: WorkspaceRole): Resource<Unit>
    suspend fun removeMember(workspaceId: String, userId: String): Resource<Unit>
    suspend fun leaveWorkspace(workspaceId: String, userId: String): Resource<Unit>
    
    suspend fun transferOwnership(workspaceId: String, newOwnerId: String, currentOwnerId: String): Resource<Unit>
    suspend fun archiveWorkspace(workspaceId: String): Resource<Unit>
}
