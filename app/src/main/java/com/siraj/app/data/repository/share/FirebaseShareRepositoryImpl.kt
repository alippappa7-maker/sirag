package com.siraj.app.data.repository.share

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.siraj.app.core.error.ErrorHandler
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.share.ShareLink
import com.siraj.app.domain.models.share.ShareStatus
import com.siraj.app.domain.models.share.ShareType
import com.siraj.app.domain.repository.share.ShareRepository
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseShareRepositoryImpl(
    private val firestore: FirebaseFirestore? =
        try {
            FirebaseFirestore.getInstance()
        } catch (_: Throwable) {
            null
        },
) : ShareRepository {
    private fun requireDb(): FirebaseFirestore =
        firestore ?: throw IllegalStateException("Firestore غير مهيأ")

    private fun mapDoc(id: String, data: Map<String, Any?>): ShareLink? {
        val targetId = data["targetId"] as? String ?: return null
        val typeName = data["type"] as? String ?: return null
        val type = runCatching { ShareType.valueOf(typeName) }.getOrNull() ?: return null
        val createdBy = data["createdBy"] as? String ?: return null
        val statusName = data["status"] as? String ?: ShareStatus.ACTIVE.name
        val status = runCatching { ShareStatus.valueOf(statusName) }.getOrDefault(ShareStatus.ACTIVE)
        return ShareLink(
            id = id,
            targetId = targetId,
            type = type,
            token = data["token"] as? String,
            createdBy = createdBy,
            isPrivate = data["isPrivate"] as? Boolean ?: false,
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            expiresAt = (data["expiresAt"] as? Number)?.toLong(),
            status = status,
            accessCount = (data["accessCount"] as? Number)?.toInt() ?: 0,
        )
    }

    override suspend fun createShareLink(
        targetId: String,
        type: ShareType,
        userId: String,
        isPrivate: Boolean,
        expiresAt: Long?,
    ): Resource<ShareLink> =
        try {
            val db = requireDb()
            val linkId = UUID.randomUUID().toString().take(8)
            val token = if (isPrivate) UUID.randomUUID().toString().replace("-", "") else null
            val shareLink =
                ShareLink(
                    id = linkId,
                    targetId = targetId,
                    type = type,
                    token = token,
                    createdBy = userId,
                    isPrivate = isPrivate,
                    expiresAt = expiresAt,
                )
            db
                .collection("share_links")
                .document(linkId)
                .set(
                    mapOf(
                        "targetId" to shareLink.targetId,
                        "type" to shareLink.type.name,
                        "token" to shareLink.token,
                        "createdBy" to shareLink.createdBy,
                        "isPrivate" to shareLink.isPrivate,
                        "createdAt" to shareLink.createdAt,
                        "expiresAt" to shareLink.expiresAt,
                        "status" to shareLink.status.name,
                        "accessCount" to shareLink.accessCount,
                    ),
                ).await()
            Resource.Success(shareLink)
        } catch (e: Exception) {
            val error = ErrorHandler.handle(e)
            Resource.Error(error.userMessage, error)
        }

    override suspend fun getAndValidateShareLink(
        linkId: String,
        token: String?,
    ): Resource<ShareLink> {
        return try {
            val db = requireDb()
            val snapshot = db.collection("share_links").document(linkId).get().await()
            if (!snapshot.exists()) {
                return Resource.Error("هذا الرابط غير موجود أو تم حذفه.")
            }
            val link =
                mapDoc(snapshot.id, snapshot.data ?: emptyMap())
                    ?: return Resource.Error("هذا الرابط غير موجود أو تم حذفه.")

            if (link.isExpired) {
                return Resource.Error("عذراً، انتهت صلاحية هذا الرابط.")
            }
            if (link.status == ShareStatus.REVOKED) {
                return Resource.Error("تم إبطال هذا الرابط من قبل صاحبه.")
            }
            if (link.status == ShareStatus.UNAVAILABLE) {
                return Resource.Error("المحتوى المرتبط بهذا الرابط غير متاح.")
            }
            if (link.isPrivate && link.token != token) {
                return Resource.Error("ليس لديك الصلاحية للوصول إلى هذا المحتوى.")
            }
            Resource.Success(link)
        } catch (e: Exception) {
            val error = ErrorHandler.handle(e)
            Resource.Error(error.userMessage, error)
        }
    }

    override suspend fun revokeShareLink(
        linkId: String,
        userId: String,
    ): Resource<Unit> {
        return try {
            val db = requireDb()
            val ref = db.collection("share_links").document(linkId)
            val snapshot = ref.get().await()
            if (!snapshot.exists()) {
                return Resource.Error("الرابط غير موجود.")
            }
            val createdBy = snapshot.getString("createdBy")
            if (createdBy != userId) {
                return Resource.Error("ليس لديك صلاحية لإبطال هذا الرابط.")
            }
            ref.update("status", ShareStatus.REVOKED.name).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            val error = ErrorHandler.handle(e)
            Resource.Error(error.userMessage, error)
        }
    }

    override suspend fun logAnonymousView(linkId: String) {
        try {
            val db = requireDb()
            db
                .collection("share_links")
                .document(linkId)
                .update("accessCount", FieldValue.increment(1))
                .await()
        } catch (_: Exception) {
            // Ignore logging errors
        }
    }
}
