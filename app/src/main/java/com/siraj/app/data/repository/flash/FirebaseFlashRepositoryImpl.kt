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
        return Resource.Success(true)
    }

    override suspend fun toggleSave(flashId: String): Resource<Boolean> {
        return Resource.Success(true)
    }

    override suspend fun logView(flashId: String) {
    }

    override suspend fun reportFlash(
        flashId: String,
        reason: String,
    ): Resource<Unit> {
        return Resource.Success(Unit)
    }

    override suspend fun followCreator(creatorId: String): Resource<Unit> {
        return Resource.Success(Unit)
    }
}

