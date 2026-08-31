package com.siraj.app.data.repository.flash

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.flash.*
import com.siraj.app.domain.repository.flash.FlashPublishingRepository
import kotlinx.coroutines.delay
import java.io.File
import java.util.UUID

class FirebaseFlashPublishingRepositoryImpl : FlashPublishingRepository {
    private val flashesStore = mutableMapOf<String, Flash>()
    private val auditLogsStore = mutableListOf<FlashAuditLog>()

    private fun logTransition(
        flashId: String,
        from: FlashPublishingState?,
        to: FlashPublishingState,
        actionBy: String,
        reason: String? = null,
    ) {
        val log =
            FlashAuditLog(
                flashId = flashId,
                fromState = from,
                toState = to,
                actionBy = actionBy,
                reason = reason,
            )
        auditLogsStore.add(log)
    }

    override suspend fun createDraft(
        creatorId: String,
        creatorName: String,
        workspaceId: String,
        videoFile: File?,
        videoAssetId: String?,
        durationMs: Long,
    ): Resource<Flash> {
        delay(800) // Simulate upload
        val flash =
            Flash(
                creatorId = creatorId,
                creatorName = creatorName,
                workspaceId = workspaceId,
                videoUrl = videoFile?.absolutePath ?: "mock_url",
                videoAssetId = videoAssetId ?: UUID.randomUUID().toString(),
                title = "ومضة جديدة",
                description = "",
                category = "عام",
                sourceInfo = null,
                publishingState = FlashPublishingState.DRAFT,
                durationMs = durationMs,
                thumbnailUrl = null,
            )
        flashesStore[flash.id] = flash
        logTransition(flash.id, null, FlashPublishingState.DRAFT, creatorId)
        return Resource.Success(flash)
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
        delay(400)
        val flash = flashesStore[flashId] ?: return Resource.Error("الومضة غير موجودة")

        // Rules: Editing text/audio resets to pending_review if it was approved,
        // but since we are updating details, if it was approved, we should probably revert it to pending_review or draft.
        // For simplicity, let's revert it to pending_review if they try to edit an approved flash.
        val newState =
            if (flash.publishingState == FlashPublishingState.APPROVED) {
                FlashPublishingState.PENDING_REVIEW
            } else {
                flash.publishingState
            }

        val updated =
            flash.copy(
                title = title,
                description = description,
                category = category,
                tags = tags,
                visibility = FlashVisibility.values().find { it.name == visibility } ?: FlashVisibility.PUBLIC,
                showCreatorInfo = showCreatorInfo,
                publishingState = newState,
            )

        if (newState != flash.publishingState) {
            logTransition(flashId, flash.publishingState, newState, "System", "تعديل تفاصيل الومضة")
        }

        flashesStore[flashId] = updated
        return Resource.Success(updated)
    }

    override suspend fun runAutomatedChecks(flashId: String): Resource<Boolean> {
        delay(1500)
        val flash = flashesStore[flashId] ?: return Resource.Error("الومضة غير موجودة")

        // Mock checks (duration, format, etc.)
        if (flash.durationMs > 60000) {
            return Resource.Error("مدة الومضة تتجاوز الدقيقة.")
        }
        return Resource.Success(true)
    }

    override suspend fun submitForReview(
        flashId: String,
        userId: String,
    ): Resource<Flash> {
        delay(500)
        val flash = flashesStore[flashId] ?: return Resource.Error("الومضة غير موجودة")

        // Cannot publish unowned/unlicensed assets or without sources for claims (simplified logic: check if title is empty)
        if (flash.title.isBlank()) {
            return Resource.Error("يجب كتابة العنوان أولاً.")
        }

        val updated = flash.copy(publishingState = FlashPublishingState.PENDING_REVIEW, rejectionReason = null)
        flashesStore[flashId] = updated
        logTransition(flashId, flash.publishingState, FlashPublishingState.PENDING_REVIEW, userId)
        return Resource.Success(updated)
    }

    override suspend fun publishFlash(
        flashId: String,
        userId: String,
    ): Resource<Flash> {
        delay(500)
        val flash = flashesStore[flashId] ?: return Resource.Error("الومضة غير موجودة")

        if (flash.publishingState != FlashPublishingState.APPROVED) {
            return Resource.Error("لا يمكن النشر إلا بعد اعتماد المراجع.")
        }

        val updated = flash.copy(publishingState = FlashPublishingState.PUBLISHED, publishedAt = System.currentTimeMillis())
        flashesStore[flashId] = updated
        logTransition(flashId, flash.publishingState, FlashPublishingState.PUBLISHED, userId)
        return Resource.Success(updated)
    }

    override suspend fun schedulePublish(
        flashId: String,
        userId: String,
        timestamp: Long,
    ): Resource<Flash> {
        delay(500)
        val flash = flashesStore[flashId] ?: return Resource.Error("الومضة غير موجودة")

        if (flash.publishingState != FlashPublishingState.APPROVED) {
            return Resource.Error("لا يمكن الجدولة إلا بعد اعتماد المراجع.")
        }

        val updated = flash.copy(publishingState = FlashPublishingState.SCHEDULED, scheduledAt = timestamp)
        flashesStore[flashId] = updated
        logTransition(flashId, flash.publishingState, FlashPublishingState.SCHEDULED, userId)
        return Resource.Success(updated)
    }

    override suspend fun suspendFlash(
        flashId: String,
        userId: String,
        reason: String,
    ): Resource<Flash> {
        delay(500)
        val flash = flashesStore[flashId] ?: return Resource.Error("الومضة غير موجودة")

        val updated = flash.copy(publishingState = FlashPublishingState.SUSPENDED, rejectionReason = reason)
        flashesStore[flashId] = updated
        logTransition(flashId, flash.publishingState, FlashPublishingState.SUSPENDED, userId, reason)
        return Resource.Success(updated)
    }

    override suspend fun revertToDraft(
        flashId: String,
        userId: String,
    ): Resource<Flash> {
        delay(400)
        val flash = flashesStore[flashId] ?: return Resource.Error("الومضة غير موجودة")

        val updated = flash.copy(publishingState = FlashPublishingState.DRAFT, rejectionReason = null)
        flashesStore[flashId] = updated
        logTransition(flashId, flash.publishingState, FlashPublishingState.DRAFT, userId)
        return Resource.Success(updated)
    }

    override suspend fun approveFlash(
        flashId: String,
        reviewerId: String,
    ): Resource<Flash> {
        delay(500)
        val flash = flashesStore[flashId] ?: return Resource.Error("الومضة غير موجودة")

        if (flash.creatorId == reviewerId) {
            return Resource.Error("لا يمكن لمنشئ المحتوى اعتماد ومضته الخاصة.")
        }

        val updated = flash.copy(publishingState = FlashPublishingState.APPROVED, rejectionReason = null)
        flashesStore[flashId] = updated
        logTransition(flashId, flash.publishingState, FlashPublishingState.APPROVED, reviewerId)
        return Resource.Success(updated)
    }

    override suspend fun rejectFlash(
        flashId: String,
        reviewerId: String,
        reason: String,
    ): Resource<Flash> {
        delay(500)
        val flash = flashesStore[flashId] ?: return Resource.Error("الومضة غير موجودة")

        val updated = flash.copy(publishingState = FlashPublishingState.REJECTED, rejectionReason = reason)
        flashesStore[flashId] = updated
        logTransition(flashId, flash.publishingState, FlashPublishingState.REJECTED, reviewerId, reason)
        return Resource.Success(updated)
    }

    override suspend fun getAuditLogs(flashId: String): Resource<List<FlashAuditLog>> {
        delay(200)
        val logs = auditLogsStore.filter { it.flashId == flashId }.sortedByDescending { it.timestamp }
        return Resource.Success(logs)
    }

    override suspend fun getMyFlashes(creatorId: String): Resource<List<Flash>> {
        delay(300)
        val list = flashesStore.values.filter { it.creatorId == creatorId }.toList()
        return Resource.Success(list)
    }
}
