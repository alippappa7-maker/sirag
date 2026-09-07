package com.siraj.app.data.repository.community

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.repository.community.InteractionRepository
import kotlinx.coroutines.tasks.await

class FirebaseInteractionRepositoryImpl(
    private val firestore: FirebaseFirestore? =
        try {
            FirebaseFirestore.getInstance()
        } catch (_: Throwable) {
            null
        },
) : InteractionRepository {
    private fun requireDb(): FirebaseFirestore =
        firestore ?: throw IllegalStateException("Firestore غير مهيأ")

    private fun docId(
        kind: String,
        targetId: String,
        userId: String,
    ): String = "${kind}_${targetId}_$userId"

    private suspend fun toggleMembership(
        kind: String,
        userId: String,
        targetId: String,
        counterField: String? = null,
    ): Resource<Boolean> {
        return try {
            val db = requireDb()
            val id = docId(kind, targetId, userId)
            val ref = db.collection("interactions").document(id)
            val snapshot = ref.get().await()
            val nowActive =
                if (snapshot.exists()) {
                    ref.delete().await()
                    false
                } else {
                    ref
                        .set(
                            mapOf(
                                "userId" to userId,
                                "targetId" to targetId,
                                "kind" to kind,
                                "createdAt" to System.currentTimeMillis(),
                            ),
                        ).await()
                    true
                }

            if (counterField != null) {
                val delta = if (nowActive) 1L else -1L
                db
                    .collection("interaction_counters")
                    .document(targetId)
                    .set(
                        mapOf(counterField to FieldValue.increment(delta), "targetId" to targetId),
                        com.google.firebase.firestore.SetOptions.merge(),
                    ).await()
            }
            Resource.Success(nowActive)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "تعذر تحديث التفاعل")
        }
    }

    override suspend fun toggleLike(
        userId: String,
        targetId: String,
    ): Resource<Boolean> = toggleMembership("like", userId, targetId, "likeCount")

    override suspend fun getLikeCount(targetId: String): Resource<Int> {
        return try {
            val db = requireDb()
            val counter =
                db
                    .collection("interaction_counters")
                    .document(targetId)
                    .get()
                    .await()
            if (counter.exists()) {
                return Resource.Success((counter.getLong("likeCount") ?: 0L).toInt())
            }
            val snapshot =
                db
                    .collection("interactions")
                    .whereEqualTo("targetId", targetId)
                    .whereEqualTo("kind", "like")
                    .get()
                    .await()
            Resource.Success(snapshot.size())
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "تعذر جلب عدد الإعجابات")
        }
    }

    override suspend fun toggleSave(
        userId: String,
        targetId: String,
    ): Resource<Boolean> = toggleMembership("save", userId, targetId, "saveCount")

    override suspend fun getSavedItems(userId: String): Resource<List<String>> {
        return try {
            val db = requireDb()
            val snapshot =
                db
                    .collection("interactions")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("kind", "save")
                    .get()
                    .await()
            Resource.Success(snapshot.documents.mapNotNull { it.getString("targetId") })
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "تعذر جلب العناصر المحفوظة")
        }
    }

    override suspend fun toggleFollow(
        userId: String,
        targetUserId: String,
    ): Resource<Boolean> = toggleMembership("follow", userId, targetUserId, "followerCount")

    override suspend fun getFollowers(userId: String): Resource<List<String>> {
        return try {
            val db = requireDb()
            val snapshot =
                db
                    .collection("interactions")
                    .whereEqualTo("targetId", userId)
                    .whereEqualTo("kind", "follow")
                    .get()
                    .await()
            Resource.Success(snapshot.documents.mapNotNull { it.getString("userId") })
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "تعذر جلب المتابعين")
        }
    }

    override suspend fun getFollowing(userId: String): Resource<List<String>> {
        return try {
            val db = requireDb()
            val snapshot =
                db
                    .collection("interactions")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("kind", "follow")
                    .get()
                    .await()
            Resource.Success(snapshot.documents.mapNotNull { it.getString("targetId") })
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "تعذر جلب المتابَعين")
        }
    }

    override suspend fun blockUser(
        userId: String,
        blockedUserId: String,
    ): Resource<Unit> {
        return try {
            val db = requireDb()
            db
                .collection("interactions")
                .document(docId("block", blockedUserId, userId))
                .set(
                    mapOf(
                        "userId" to userId,
                        "targetId" to blockedUserId,
                        "kind" to "block",
                        "createdAt" to System.currentTimeMillis(),
                    ),
                ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "تعذر حظر المستخدم")
        }
    }

    override suspend fun unblockUser(
        userId: String,
        blockedUserId: String,
    ): Resource<Unit> {
        return try {
            val db = requireDb()
            db
                .collection("interactions")
                .document(docId("block", blockedUserId, userId))
                .delete()
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "تعذر إلغاء الحظر")
        }
    }

    override suspend fun getBlockedUsers(userId: String): Resource<List<String>> {
        return try {
            val db = requireDb()
            val snapshot =
                db
                    .collection("interactions")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("kind", "block")
                    .get()
                    .await()
            Resource.Success(snapshot.documents.mapNotNull { it.getString("targetId") })
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "تعذر جلب قائمة المحظورين")
        }
    }

    override suspend fun hideContent(
        userId: String,
        contentId: String,
    ): Resource<Unit> {
        return try {
            val db = requireDb()
            db
                .collection("interactions")
                .document(docId("hide", contentId, userId))
                .set(
                    mapOf(
                        "userId" to userId,
                        "targetId" to contentId,
                        "kind" to "hide",
                        "createdAt" to System.currentTimeMillis(),
                    ),
                ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "تعذر إخفاء المحتوى")
        }
    }

    override suspend fun getHiddenContent(userId: String): Resource<List<String>> {
        return try {
            val db = requireDb()
            val snapshot =
                db
                    .collection("interactions")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("kind", "hide")
                    .get()
                    .await()
            Resource.Success(snapshot.documents.mapNotNull { it.getString("targetId") })
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "تعذر جلب المحتوى المخفي")
        }
    }

    override suspend fun recordShare(
        userId: String,
        targetId: String,
    ): Resource<Unit> {
        return try {
            val db = requireDb()
            db
                .collection("interactions")
                .document()
                .set(
                    mapOf(
                        "userId" to userId,
                        "targetId" to targetId,
                        "kind" to "share",
                        "createdAt" to System.currentTimeMillis(),
                    ),
                ).await()
            db
                .collection("interaction_counters")
                .document(targetId)
                .set(
                    mapOf("shareCount" to FieldValue.increment(1), "targetId" to targetId),
                    com.google.firebase.firestore.SetOptions.merge(),
                ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "تعذر تسجيل المشاركة")
        }
    }
}
