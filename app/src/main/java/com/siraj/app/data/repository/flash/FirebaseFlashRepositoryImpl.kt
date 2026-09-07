package com.siraj.app.data.repository.flash

import com.google.firebase.firestore.FirebaseFirestore
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.flash.*
import com.siraj.app.domain.repository.flash.FlashRepository
import kotlinx.coroutines.tasks.await

class FirebaseFlashRepositoryImpl(
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (_: Throwable) { null }
) : FlashRepository {

    override suspend fun getFlashesFeed(
        pageToken: String?,
        limit: Int,
    ): Resource<FlashesFeedResult> {
        val fs = firestore ?: return Resource.Success(FlashesFeedResult(emptyList(), null, false))
        return try {
            val snapshot = fs.collection("flashes")
                .whereEqualTo("publishingState", FlashPublishingState.APPROVED.name)
                .limit(limit.toLong())
                .get()
                .await()

            val flashes = snapshot.documents.mapNotNull { doc ->
                try {
                    Flash(
                        id = doc.id,
                        creatorId = doc.getString("creatorId") ?: "",
                        creatorName = doc.getString("creatorName") ?: "",
                        workspaceId = doc.getString("workspaceId") ?: "",
                        videoAssetId = doc.getString("videoAssetId") ?: "",
                        videoUrl = doc.getString("videoUrl") ?: "",
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        category = doc.getString("category") ?: "عام",
                        sourceInfo = null,
                        publishingState = FlashPublishingState.APPROVED,
                        durationMs = doc.getLong("durationMs") ?: 0L,
                        thumbnailUrl = doc.getString("thumbnailUrl"),
                        visibility = FlashVisibility.valueOf(doc.getString("visibility") ?: "PUBLIC")
                    )
                } catch (_: Exception) {
                    null
                }
            }
            Resource.Success(FlashesFeedResult(flashes, null, false))
        } catch (e: Exception) {
            Resource.Success(FlashesFeedResult(emptyList(), null, false))
        }
    }

    override suspend fun toggleLike(flashId: String): Resource<Boolean> {
        val fs = firestore ?: return Resource.Error("Firestore غير مهيأ")
        val uid =
            com.google.firebase.auth.FirebaseAuth
                .getInstance()
                .currentUser
                ?.uid ?: return Resource.Error("يجب تسجيل الدخول")
        return com.siraj.app.data.repository.community
            .FirebaseInteractionRepositoryImpl(fs)
            .toggleLike(uid, flashId)
    }

    override suspend fun toggleSave(flashId: String): Resource<Boolean> {
        val fs = firestore ?: return Resource.Error("Firestore غير مهيأ")
        val uid =
            com.google.firebase.auth.FirebaseAuth
                .getInstance()
                .currentUser
                ?.uid ?: return Resource.Error("يجب تسجيل الدخول")
        return com.siraj.app.data.repository.community
            .FirebaseInteractionRepositoryImpl(fs)
            .toggleSave(uid, flashId)
    }

    override suspend fun logView(flashId: String) {
        val fs = firestore ?: return
        try {
            fs
                .collection("interaction_counters")
                .document(flashId)
                .set(
                    mapOf(
                        "viewCount" to com.google.firebase.firestore.FieldValue.increment(1),
                        "targetId" to flashId,
                    ),
                    com.google.firebase.firestore.SetOptions.merge(),
                ).await()
        } catch (_: Exception) {
        }
    }

    override suspend fun reportFlash(
        flashId: String,
        reason: String,
    ): Resource<Unit> {
        val fs = firestore ?: return Resource.Error("Firestore غير مهيأ")
        val uid =
            com.google.firebase.auth.FirebaseAuth
                .getInstance()
                .currentUser
                ?.uid ?: return Resource.Error("يجب تسجيل الدخول")
        return try {
            val id =
                java.util.UUID
                    .randomUUID()
                    .toString()
            fs
                .collection("reports")
                .document(id)
                .set(
                    mapOf(
                        "id" to id,
                        "reporterId" to uid,
                        "targetType" to "FLASH",
                        "targetId" to flashId,
                        "targetOwnerId" to "",
                        "reportType" to "OTHER",
                        "description" to reason,
                        "status" to "PENDING",
                        "createdAt" to System.currentTimeMillis(),
                    ),
                ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "تعذر إرسال البلاغ")
        }
    }

    override suspend fun followCreator(creatorId: String): Resource<Unit> {
        val fs = firestore ?: return Resource.Error("Firestore غير مهيأ")
        val uid =
            com.google.firebase.auth.FirebaseAuth
                .getInstance()
                .currentUser
                ?.uid ?: return Resource.Error("يجب تسجيل الدخول")
        return when (
            val result =
                com.siraj.app.data.repository.community
                    .FirebaseInteractionRepositoryImpl(fs)
                    .toggleFollow(uid, creatorId)
        ) {
            is Resource.Success -> Resource.Success(Unit)
            is Resource.Error -> Resource.Error(result.message.ifBlank { "تعذر المتابعة" }, result.error)
            else -> Resource.Error("تعذر المتابعة")
        }
    }
}

