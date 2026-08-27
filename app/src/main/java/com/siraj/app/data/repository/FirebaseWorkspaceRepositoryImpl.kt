package com.siraj.app.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.*
import com.siraj.app.domain.repository.WorkspaceRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseWorkspaceRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : WorkspaceRepository {

    private val workspacesCol = firestore.collection("workspaces")
    private val membersCol = firestore.collection("workspace_members")
    private val invitationsCol = firestore.collection("workspace_invitations")
    private val auditCol = firestore.collection("audit_logs")

    override fun getUserWorkspaces(userId: String): Flow<Resource<List<Workspace>>> = callbackFlow {
        trySend(Resource.Loading)
        val registration = workspacesCol
            .whereArrayContains("memberIds", userId)
            .whereEqualTo("status", "ACTIVE")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "فشل جلب مساحات العمل"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val workspaces = snapshot.documents.mapNotNull { it.toObject(Workspace::class.java) }
                    trySend(Resource.Success(workspaces))
                }
            }
        awaitClose { registration.remove() }
    }

    override fun getWorkspaceMembers(workspaceId: String): Flow<Resource<List<WorkspaceMember>>> = callbackFlow {
        trySend(Resource.Loading)
        val registration = membersCol
            .whereEqualTo("workspaceId", workspaceId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "فشل جلب الأعضاء"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val members = snapshot.documents.mapNotNull { it.toObject(WorkspaceMember::class.java) }
                    trySend(Resource.Success(members))
                }
            }
        awaitClose { registration.remove() }
    }

    override fun getWorkspaceInvitations(workspaceId: String): Flow<Resource<List<WorkspaceInvitation>>> = callbackFlow {
        trySend(Resource.Loading)
        val registration = invitationsCol
            .whereEqualTo("workspaceId", workspaceId)
            .whereEqualTo("status", InvitationStatus.PENDING.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "فشل جلب الدعوات"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val invitations = snapshot.documents.mapNotNull { it.toObject(WorkspaceInvitation::class.java) }
                    trySend(Resource.Success(invitations))
                }
            }
        awaitClose { registration.remove() }
    }

    override fun getUserInvitations(email: String): Flow<Resource<List<WorkspaceInvitation>>> = callbackFlow {
        trySend(Resource.Loading)
        val registration = invitationsCol
            .whereEqualTo("email", email)
            .whereEqualTo("status", InvitationStatus.PENDING.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "فشل جلب الدعوات"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val invitations = snapshot.documents.mapNotNull { it.toObject(WorkspaceInvitation::class.java) }
                    trySend(Resource.Success(invitations))
                }
            }
        awaitClose { registration.remove() }
    }

    override suspend fun createWorkspace(name: String, type: WorkspaceType, owner: UserProfile): Resource<String> {
        return try {
            val workspaceId = UUID.randomUUID().toString()
            val workspace = Workspace(
                id = workspaceId,
                name = name,
                type = type,
                ownerId = owner.id,
                memberIds = listOf(owner.id)
            )
            
            val memberId = "${workspaceId}_${owner.id}"
            val member = WorkspaceMember(
                id = memberId,
                workspaceId = workspaceId,
                userId = owner.id,
                userEmail = owner.email,
                userName = owner.name,
                role = WorkspaceRole.OWNER
            )
            
            firestore.runBatch { batch ->
                batch.set(workspacesCol.document(workspaceId), workspace)
                batch.set(membersCol.document(memberId), member)
            }.await()
            
            Resource.Success(workspaceId)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "فشل إنشاء مساحة العمل")
        }
    }

    override suspend fun inviteMember(workspaceId: String, email: String, role: WorkspaceRole, inviterId: String): Resource<Unit> {
        return try {
            val invitationId = UUID.randomUUID().toString()
            val invitation = WorkspaceInvitation(
                id = invitationId,
                workspaceId = workspaceId,
                inviterId = inviterId,
                email = email,
                role = role
            )
            invitationsCol.document(invitationId).set(invitation).await()
            logAudit(workspaceId, inviterId, "INVITE_MEMBER", "Invited $email as $role")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "فشل إرسال الدعوة")
        }
    }

    override suspend fun respondToInvitation(invitationId: String, accept: Boolean, user: UserProfile): Resource<Unit> {
        return try {
            val snapshot = invitationsCol.document(invitationId).get().await()
            val invitation = snapshot.toObject(WorkspaceInvitation::class.java) ?: return Resource.Error("الدعوة غير موجودة")
            
            if (invitation.email != user.email) return Resource.Error("هذه الدعوة ليست لك")
            if (invitation.status != InvitationStatus.PENDING) return Resource.Error("الدعوة لم تعد صالحة")
            
            if (accept) {
                val memberId = "${invitation.workspaceId}_${user.id}"
                val member = WorkspaceMember(
                    id = memberId,
                    workspaceId = invitation.workspaceId,
                    userId = user.id,
                    userEmail = user.email,
                    userName = user.name,
                    role = invitation.role
                )
                
                firestore.runBatch { batch ->
                    batch.update(invitationsCol.document(invitationId), "status", InvitationStatus.ACCEPTED.name)
                    batch.set(membersCol.document(memberId), member)
                    batch.update(workspacesCol.document(invitation.workspaceId), "memberIds", FieldValue.arrayUnion(user.id))
                }.await()
                logAudit(invitation.workspaceId, user.id, "ACCEPT_INVITE", "Accepted invite")
            } else {
                invitationsCol.document(invitationId).update("status", InvitationStatus.REJECTED.name).await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "فشل الرد على الدعوة")
        }
    }

    override suspend fun updateMemberRole(workspaceId: String, userId: String, newRole: WorkspaceRole): Resource<Unit> {
        return try {
            val memberId = "${workspaceId}_${userId}"
            membersCol.document(memberId).update("role", newRole.name).await()
            logAudit(workspaceId, "ADMIN", "UPDATE_ROLE", "Updated user $userId to $newRole")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "فشل تحديث الصلاحية")
        }
    }

    override suspend fun removeMember(workspaceId: String, userId: String): Resource<Unit> {
         return try {
            val memberId = "${workspaceId}_${userId}"
            firestore.runBatch { batch ->
                batch.delete(membersCol.document(memberId))
                batch.update(workspacesCol.document(workspaceId), "memberIds", FieldValue.arrayRemove(userId))
            }.await()
            logAudit(workspaceId, "ADMIN", "REMOVE_MEMBER", "Removed user $userId")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "فشل إزالة العضو")
        }
    }

    override suspend fun leaveWorkspace(workspaceId: String, userId: String): Resource<Unit> {
        return removeMember(workspaceId, userId)
    }

    override suspend fun transferOwnership(workspaceId: String, newOwnerId: String, currentOwnerId: String): Resource<Unit> {
        return try {
            val currentMemberId = "${workspaceId}_${currentOwnerId}"
            val newMemberId = "${workspaceId}_${newOwnerId}"
            
            firestore.runBatch { batch ->
                batch.update(workspacesCol.document(workspaceId), "ownerId", newOwnerId)
                batch.update(membersCol.document(currentMemberId), "role", WorkspaceRole.MANAGER.name)
                batch.update(membersCol.document(newMemberId), "role", WorkspaceRole.OWNER.name)
            }.await()
            
            logAudit(workspaceId, currentOwnerId, "TRANSFER_OWNERSHIP", "Transferred to $newOwnerId")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "فشل نقل الملكية")
        }
    }

    override suspend fun archiveWorkspace(workspaceId: String): Resource<Unit> {
        return try {
            workspacesCol.document(workspaceId).update("status", "ARCHIVED").await()
            logAudit(workspaceId, "ADMIN", "ARCHIVE_WORKSPACE", "Archived workspace")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "فشل أرشفة مساحة العمل")
        }
    }
    
    private suspend fun logAudit(workspaceId: String, userId: String, action: String, details: String) {
        val id = UUID.randomUUID().toString()
        auditCol.document(id).set(mapOf(
            "id" to id,
            "workspaceId" to workspaceId,
            "userId" to userId,
            "action" to action,
            "details" to details,
            "timestamp" to System.currentTimeMillis()
        )).await()
    }
}
