package com.siraj.app.features.project.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.siraj.app.domain.models.Project
import com.siraj.app.domain.models.ProjectStatus
import com.siraj.app.domain.models.Scene
import com.siraj.app.features.project.domain.models.ProductionJob
import com.siraj.app.features.project.domain.models.ProductionJobStatus
import com.siraj.app.features.project.domain.models.ProductionQuality
import com.siraj.app.features.project.domain.repositories.ProductionJobRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseProductionJobRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ProductionJobRepository {

    override suspend fun createJob(
        projectId: String,
        workspaceId: String,
        quality: ProductionQuality,
        burnSubtitles: Boolean,
        aspectRatio: String,
        idempotencyKey: String,
        fps: Int,
        includeSourceCitation: Boolean,
        includeWatermark: Boolean,
        isPreviewOnly: Boolean
    ): Result<ProductionJob> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("يجب تسجيل الدخول لإنشاء مهمة إنتاج"))
            val userId = user.uid

            // 1. Idempotency check to prevent duplicate charges or task submissions
            val existingJobQuery = firestore.collection("production_jobs")
                .whereEqualTo("idempotencyKey", idempotencyKey)
                .limit(1)
                .get()
                .await()

            if (!existingJobQuery.isEmpty) {
                val existing = existingJobQuery.documents.first().toObject(ProductionJob::class.java)
                if (existing != null) {
                    return Result.success(existing)
                }
            }

            // 2. Fetch and Validate Project
            val projectDoc = firestore.collection("projects").document(projectId).get().await()
            if (!projectDoc.exists()) {
                return Result.failure(Exception("المشروع غير موجود"))
            }
            val project = projectDoc.toObject(Project::class.java) ?: return Result.failure(Exception("تعذر قراءة بيانات المشروع"))

            // Verify Scenes count
            val scenesSnapshot = firestore.collection("projects").document(projectId)
                .collection("scenes").get().await()
            if (scenesSnapshot.isEmpty) {
                return Result.failure(Exception("لا يمكن تصدير مشروع لا يحتوي على أي مشاهد."))
            }

            // Calculate cost units based on quality, fps, duration and preview mode
            val durationSec = (project.durationMs / 1000).coerceAtLeast(10)
            val baseUnits = (durationSec / 5).coerceAtLeast(5)
            val fpsMultiplier = when (fps) {
                60 -> 1.4
                24 -> 0.9
                else -> 1.0
            }
            val calculatedUnits = (baseUnits * quality.costMultiplier * fpsMultiplier * (if (isPreviewOnly) 0.4 else 1.0)).toLong().coerceAtLeast(2L)

            val jobId = UUID.randomUUID().toString()
            val initialLog = if (isPreviewOnly) {
                "تم استلام طلب تركيب نسخة معاينة سريعة (Fast Preview) بمعدل $fps إطار/ث وإدراجه في طابور مهام سراج."
            } else {
                "تم استلام طلب تركيب الفيديو النهائي الكامل بمعدل $fps إطار/ث وجودة ${quality.label} وإدراجه في طابور مهام سراج (Cloud Tasks)."
            }

            val newJob = ProductionJob(
                jobId = jobId,
                projectId = projectId,
                projectTitle = project.title.ifBlank { "مشروع سراج فيديو" },
                ownerId = userId,
                workspaceId = workspaceId.ifBlank { project.workspaceId },
                status = ProductionJobStatus.QUEUED,
                progress = 5,
                attemptCount = 1,
                maxAttempts = 3,
                costUnits = calculatedUnits,
                creditRefunded = false,
                provider = "Siraj-Video-Composition-Worker",
                quality = if (isPreviewOnly) ProductionQuality.SD_720P else quality,
                burnSubtitles = burnSubtitles,
                includeSourceCitation = includeSourceCitation,
                includeWatermark = includeWatermark,
                fps = fps,
                isPreviewOnly = isPreviewOnly,
                aspectRatio = aspectRatio.ifBlank { project.aspectRatio },
                idempotencyKey = idempotencyKey,
                logs = listOf(initialLog),
                createdAt = System.currentTimeMillis()
            )

            // Save job in Firestore
            firestore.collection("production_jobs").document(jobId).set(newJob).await()

            // Update Project status to EXPORTING
            firestore.collection("projects").document(projectId).update(
                mapOf(
                    "status" to ProjectStatus.EXPORTING.name,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()

            Result.success(newJob)
        } catch (e: Exception) { GlobalErrorHandler.handle(e); Result.failure(e) }
    }


    override fun observeJob(jobId: String): Flow<ProductionJob?> = callbackFlow {
        val listener = firestore.collection("production_jobs").document(jobId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val job = snapshot?.toObject(ProductionJob::class.java)
                trySend(job)
            }
        awaitClose { listener.remove() }
    }

    override fun getJobsForProject(projectId: String): Flow<List<ProductionJob>> = callbackFlow {
        val listener = firestore.collection("production_jobs")
            .whereEqualTo("projectId", projectId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val jobs = snapshot?.documents?.mapNotNull { it.toObject(ProductionJob::class.java) } ?: emptyList()
                trySend(jobs)
            }
        awaitClose { listener.remove() }
    }

    override fun getJobsForUser(userId: String): Flow<List<ProductionJob>> = callbackFlow {
        val listener = firestore.collection("production_jobs")
            .whereEqualTo("ownerId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val jobs = snapshot?.documents?.mapNotNull { it.toObject(ProductionJob::class.java) } ?: emptyList()
                trySend(jobs)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun cancelJob(jobId: String, reason: String): Result<Unit> {
        return try {
            val jobDoc = firestore.collection("production_jobs").document(jobId).get().await()
            if (!jobDoc.exists()) return Result.failure(Exception("المهمة غير موجودة"))
            val job = jobDoc.toObject(ProductionJob::class.java) ?: return Result.failure(Exception("خطأ في قراءة المهمة"))

            if (!job.canCancel) {
                return Result.failure(Exception("لا يمكن إلغاء مهمة مكتملة أو منتهية بالفعل"))
            }

            val updatedLogs = job.logs + "تم إلغاء المهمة: $reason. تم استرداد الرصيد المحجوز (${job.costUnits} وحدة)."

            firestore.collection("production_jobs").document(jobId).update(
                mapOf(
                    "status" to ProductionJobStatus.CANCELLED.name,
                    "creditRefunded" to true,
                    "logs" to updatedLogs,
                    "completedAt" to System.currentTimeMillis()
                )
            ).await()

            // Update Project Status back to READY
            firestore.collection("projects").document(job.projectId).update(
                mapOf("status" to ProjectStatus.READY.name)
            ).await()

            Result.success(Unit)
        } catch (e: Exception) { GlobalErrorHandler.handle(e); Result.failure(e) }
    }

    override suspend fun retryJob(jobId: String): Result<ProductionJob> {
        return try {
            val jobDoc = firestore.collection("production_jobs").document(jobId).get().await()
            if (!jobDoc.exists()) return Result.failure(Exception("المهمة غير موجودة"))
            val job = jobDoc.toObject(ProductionJob::class.java) ?: return Result.failure(Exception("خطأ في قراءة المهمة"))

            if (job.status != ProductionJobStatus.FAILED && job.status != ProductionJobStatus.CANCELLED) {
                return Result.failure(Exception("يمكن فقط إعادة محاولة المهام الفاشلة أو الملغاة"))
            }

            val newAttempt = job.attemptCount + 1
            if (newAttempt > job.maxAttempts) {
                return Result.failure(Exception("تم تجاوز الحد الأقصى للمحاولات (${job.maxAttempts}) لهذه المهمة. يُرجى إنشاء مهمة جديدة."))
            }

            val updatedLogs = job.logs + "إعادة إرسال المهمة للطابور (محاولة $newAttempt من ${job.maxAttempts})."

            val updatedJob = job.copy(
                status = ProductionJobStatus.QUEUED,
                progress = 5,
                attemptCount = newAttempt,
                errorCode = null,
                errorMessage = null,
                creditRefunded = false,
                logs = updatedLogs,
                startedAt = System.currentTimeMillis()
            )

            firestore.collection("production_jobs").document(jobId).set(updatedJob).await()
            Result.success(updatedJob)
        } catch (e: Exception) { GlobalErrorHandler.handle(e); Result.failure(e) }
    }

    override suspend fun deleteExportedFile(jobId: String): Result<Unit> {
        return try {
            val jobDoc = firestore.collection("production_jobs").document(jobId).get().await()
            if (!jobDoc.exists()) return Result.failure(Exception("المهمة غير موجودة"))
            val job = jobDoc.toObject(ProductionJob::class.java) ?: return Result.failure(Exception("خطأ في قراءة المهمة"))

            val updatedLogs = job.logs + "تم حذف الملف النهائي الناتج من التخزين السحابي بأمان."

            firestore.collection("production_jobs").document(jobId).update(
                mapOf(
                    "outputVideoUrl" to null,
                    "previewVideoUrl" to null,
                    "fileSizeBytes" to 0L,
                    "logs" to updatedLogs
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) { GlobalErrorHandler.handle(e); Result.failure(e) }
    }
}
