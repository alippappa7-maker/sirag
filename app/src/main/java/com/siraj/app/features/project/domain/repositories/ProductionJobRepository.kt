package com.siraj.app.features.project.domain.repositories

import com.siraj.app.features.project.domain.models.ProductionJob
import com.siraj.app.features.project.domain.models.ProductionQuality
import kotlinx.coroutines.flow.Flow

interface ProductionJobRepository {
    suspend fun createJob(
        projectId: String,
        workspaceId: String,
        quality: ProductionQuality,
        burnSubtitles: Boolean,
        aspectRatio: String,
        idempotencyKey: String,
        fps: Int = 30,
        includeSourceCitation: Boolean = true,
        includeWatermark: Boolean = true,
        isPreviewOnly: Boolean = false
    ): Result<ProductionJob>

    fun observeJob(jobId: String): Flow<ProductionJob?>

    fun getJobsForProject(projectId: String): Flow<List<ProductionJob>>

    fun getJobsForUser(userId: String): Flow<List<ProductionJob>>

    suspend fun cancelJob(jobId: String, reason: String = "تم الإلغاء بواسطة المستخدم"): Result<Unit>

    suspend fun retryJob(jobId: String): Result<ProductionJob>

    suspend fun deleteExportedFile(jobId: String): Result<Unit>
}
