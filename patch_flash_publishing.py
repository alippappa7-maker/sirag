import re

file_path = "app/src/main/java/com/siraj/app/data/repository/flash/FirebaseFlashPublishingRepositoryImpl.kt"

content = """package com.siraj.app.data.repository.flash

import com.google.firebase.firestore.FirebaseFirestore
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.flash.*
import com.siraj.app.domain.repository.flash.FlashPublishingRepository
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID

class FirebaseFlashPublishingRepositoryImpl(
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (_: Throwable) { null }
) : FlashPublishingRepository {

    private suspend fun logTransition(
        flashId: String,
        from: FlashPublishingState?,
        to: FlashPublishingState,
        actionBy: String,
        reason: String? = null,
    ) {
        if (firestore == null) return
        try {
            val log = FlashAuditLog(
                id = UUID.randomUUID().toString(),
                flashId = flashId,
                fromState = from,
                toState = to,
                actionBy = actionBy,
                reason = reason,
                timestamp = System.currentTimeMillis()
            )
            firestore.collection("flash_audit_logs").document(log.id).set(log).await()
        } catch (_: Exception) { }
    }

    override suspend fun createDraft(
        creatorId: String,
        creatorName: String,
        workspaceId: String,
        videoFile: File?,
        videoAssetId: String?,
        durationMs: Long,
    ): Resource<Flash> {
        if (firestore == null) return Resource.Error("Firestore not initialized")
        return try {
            val flash = Flash(
                id = UUID.randomUUID().toString(),
                creatorId = creatorId,
                creatorName = creatorName,
                workspaceId = workspaceId,
                videoUrl = videoFile?.absolutePath ?: "",
                videoAssetId = videoAssetId ?: UUID.randomUUID().toString(),
                title = "ومضة جديدة",
                description = "",
                category = "عام",
                sourceInfo = null,
                publishingState = FlashPublishingState.DRAFT,
                durationMs = durationMs,
                thumbnailUrl = null,
            )
            firestore.collection("flashes").document(flash.id).set(flash).await()
            logTransition(flash.id, null, FlashPublishingState.DRAFT, creatorId)
            Resource.Success(flash)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to create draft")
        }
    }

    override suspend fun updateFlashDetails(
        flashId: String,
        title: String,
        description: String,
        category: String,
        tags: List<String>,
        visibility: String,
        showCreatorInfo: Boolean,
        sourceIds: List<String>,
    ): Resource<Flash> {
        if (firestore == null) return Resource.Error("Firestore not initialized")
        return try {
            val doc = firestore.collection("flashes").document(flashId).get().await()
            val flash = doc.toObject(Flash::class.java) ?: return Resource.Error("الومضة غير موجودة")
            
            val newState = if (flash.publishingState == FlashPublishingState.APPROVED) {
                FlashPublishingState.PENDING_REVIEW
            } else {
                flash.publishingState
            }
            
            val updated = flash.copy(
                title = title,
                description = description,
                category = category,
                tags = tags,
                visibility = FlashVisibility.values().find { it.name == visibility } ?: FlashVisibility.PUBLIC,
                showCreatorInfo = showCreatorInfo,
                publishingState = newState,
            )
            
            firestore.collection("flashes").document(flashId).set(updated).await()
            if (newState != flash.publishingState) {
                logTransition(flashId, flash.publishingState, newState, "System", "تعديل تفاصيل الومضة")
            }
            Resource.Success(updated)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error updating flash")
        }
    }

    override suspend fun runAutomatedChecks(flashId: String): Resource<Boolean> {
        if (firestore == null) return Resource.Error("Firestore not initialized")
        return try {
            val doc = firestore.collection("flashes").document(flashId).get().await()
            val flash = doc.toObject(Flash::class.java) ?: return Resource.Error("الومضة غير موجودة")
            if (flash.durationMs > 60000) {
                return Resource.Error("مدة الومضة تتجاوز الدقيقة.")
            }
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Check failed")
        }
    }

    override suspend fun submitForReview(
        flashId: String,
        userId: String,
    ): Resource<Flash> {
        return updateState(flashId, FlashPublishingState.PENDING_REVIEW, userId, null)
    }

    override suspend fun publishFlash(
        flashId: String,
        userId: String,
    ): Resource<Flash> {
        if (firestore == null) return Resource.Error("Firestore not initialized")
        return try {
            val doc = firestore.collection("flashes").document(flashId).get().await()
            val flash = doc.toObject(Flash::class.java) ?: return Resource.Error("الومضة غير موجودة")
            if (flash.publishingState != FlashPublishingState.APPROVED) {
                return Resource.Error("لا يمكن النشر إلا بعد اعتماد المراجع.")
            }
            val updated = flash.copy(publishingState = FlashPublishingState.PUBLISHED, publishedAt = System.currentTimeMillis())
            firestore.collection("flashes").document(flashId).set(updated).await()
            logTransition(flashId, flash.publishingState, FlashPublishingState.PUBLISHED, userId)
            Resource.Success(updated)
        } catch (e: Exception) {
            Resource.Error("Failed to publish")
        }
    }

    override suspend fun schedulePublish(
        flashId: String,
        userId: String,
        timestamp: Long,
    ): Resource<Flash> {
        if (firestore == null) return Resource.Error("Firestore not initialized")
        return try {
            val doc = firestore.collection("flashes").document(flashId).get().await()
            val flash = doc.toObject(Flash::class.java) ?: return Resource.Error("الومضة غير موجودة")
            if (flash.publishingState != FlashPublishingState.APPROVED) {
                return Resource.Error("لا يمكن الجدولة إلا بعد اعتماد المراجع.")
            }
            val updated = flash.copy(publishingState = FlashPublishingState.SCHEDULED, scheduledAt = timestamp)
            firestore.collection("flashes").document(flashId).set(updated).await()
            logTransition(flashId, flash.publishingState, FlashPublishingState.SCHEDULED, userId)
            Resource.Success(updated)
        } catch (e: Exception) {
            Resource.Error("Failed to schedule")
        }
    }

    override suspend fun suspendFlash(
        flashId: String,
        userId: String,
        reason: String,
    ): Resource<Flash> {
        return updateState(flashId, FlashPublishingState.SUSPENDED, userId, reason)
    }

    override suspend fun revertToDraft(
        flashId: String,
        userId: String,
    ): Resource<Flash> {
        return updateState(flashId, FlashPublishingState.DRAFT, userId, null)
    }

    override suspend fun approveFlash(
        flashId: String,
        reviewerId: String,
    ): Resource<Flash> {
        if (firestore == null) return Resource.Error("Firestore not initialized")
        return try {
            val doc = firestore.collection("flashes").document(flashId).get().await()
            val flash = doc.toObject(Flash::class.java) ?: return Resource.Error("الومضة غير موجودة")
            if (flash.creatorId == reviewerId) {
                return Resource.Error("لا يمكن لمنشئ المحتوى اعتماد ومضته الخاصة.")
            }
            val updated = flash.copy(publishingState = FlashPublishingState.APPROVED, rejectionReason = null)
            firestore.collection("flashes").document(flashId).set(updated).await()
            logTransition(flashId, flash.publishingState, FlashPublishingState.APPROVED, reviewerId)
            Resource.Success(updated)
        } catch (e: Exception) {
            Resource.Error("Failed to approve")
        }
    }

    override suspend fun rejectFlash(
        flashId: String,
        reviewerId: String,
        reason: String,
    ): Resource<Flash> {
        return updateState(flashId, FlashPublishingState.REJECTED, reviewerId, reason)
    }

    override suspend fun getAuditLogs(flashId: String): Resource<List<FlashAuditLog>> {
        if (firestore == null) return Resource.Success(emptyList())
        return try {
            val snapshot = firestore.collection("flash_audit_logs")
                .whereEqualTo("flashId", flashId)
                .get().await()
            val logs = snapshot.documents.mapNotNull { it.toObject(FlashAuditLog::class.java) }
                .sortedByDescending { it.timestamp }
            Resource.Success(logs)
        } catch (e: Exception) {
            Resource.Success(emptyList())
        }
    }

    override suspend fun getMyFlashes(creatorId: String): Resource<List<Flash>> {
        if (firestore == null) return Resource.Success(emptyList())
        return try {
            val snapshot = firestore.collection("flashes")
                .whereEqualTo("creatorId", creatorId)
                .get().await()
            val flashes = snapshot.documents.mapNotNull { it.toObject(Flash::class.java) }
            Resource.Success(flashes)
        } catch (e: Exception) {
            Resource.Success(emptyList())
        }
    }
    
    private suspend fun updateState(flashId: String, newState: FlashPublishingState, userId: String, reason: String?): Resource<Flash> {
        if (firestore == null) return Resource.Error("Firestore not initialized")
        return try {
            val doc = firestore.collection("flashes").document(flashId).get().await()
            val flash = doc.toObject(Flash::class.java) ?: return Resource.Error("الومضة غير موجودة")
            
            val updated = flash.copy(publishingState = newState, rejectionReason = reason)
            firestore.collection("flashes").document(flashId).set(updated).await()
            logTransition(flashId, flash.publishingState, newState, userId, reason)
            Resource.Success(updated)
        } catch(e: Exception) {
            Resource.Error("Error updating state")
        }
    }
}
"""

with open(file_path, "w") as f:
    f.write(content)

