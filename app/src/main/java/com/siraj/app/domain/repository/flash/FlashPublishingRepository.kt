package com.siraj.app.domain.repository.flash

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.flash.Flash
import com.siraj.app.domain.models.flash.FlashPublishingState
import com.siraj.app.domain.models.flash.FlashAuditLog
import java.io.File

interface FlashPublishingRepository {
    // 1. Core Upload & Update
    suspend fun createDraft(
        creatorId: String,
        creatorName: String,
        workspaceId: String,
        videoFile: File?, // nullable for mock MVP
        videoAssetId: String?,
        durationMs: Long
    ): Resource<Flash>
    
    suspend fun updateFlashDetails(
        flashId: String,
        title: String,
        description: String,
        category: String,
        tags: List<String>,
        visibility: String,
        showCreatorInfo: Boolean,
        sourceIds: List<String>
    ): Resource<Flash>

    // 2. Automated Checks
    suspend fun runAutomatedChecks(flashId: String): Resource<Boolean>

    // 3. State Transitions (Publishing Flow)
    suspend fun submitForReview(flashId: String, userId: String): Resource<Flash>
    suspend fun publishFlash(flashId: String, userId: String): Resource<Flash>
    suspend fun schedulePublish(flashId: String, userId: String, timestamp: Long): Resource<Flash>
    suspend fun suspendFlash(flashId: String, userId: String, reason: String): Resource<Flash>
    suspend fun revertToDraft(flashId: String, userId: String): Resource<Flash>
    
    // For Reviewer/Admin ONLY
    suspend fun approveFlash(flashId: String, reviewerId: String): Resource<Flash>
    suspend fun rejectFlash(flashId: String, reviewerId: String, reason: String): Resource<Flash>

    // 4. History
    suspend fun getAuditLogs(flashId: String): Resource<List<FlashAuditLog>>
    suspend fun getMyFlashes(creatorId: String): Resource<List<Flash>>
}
