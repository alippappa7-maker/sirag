package com.siraj.app.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.siraj.app.data.local.NotificationDatabase
import com.siraj.app.data.local.NotificationEntity
import com.siraj.app.data.local.NotificationPreferencesEntity
import com.siraj.app.domain.models.notification.*
import com.siraj.app.domain.repository.notification.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirebaseNotificationRepositoryImpl(
    private val context: Context,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : NotificationRepository {

    private val db = NotificationDatabase.getDatabase(context)
    private val dao = db.notificationDao()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var firestoreListener: ListenerRegistration? = null

    init {
        // Initial clean up of expired local notifications
        coroutineScope.launch {
            try {
                dao.deleteExpiredNotifications()
            } catch (e: Exception) {
                Log.e("NotificationRepo", "Error cleaning expired notifications", e)
            }
        }
    }

    override fun getNotificationsFlow(userId: String): Flow<List<SirajNotification>> {
        startFirestoreSync(userId)
        return dao.getNotificationsForUser(userId).map { list ->
            if (list.isEmpty()) {
                // Generate initial welcoming system & sample notifications if fresh
                val sampleList = getInitialNotifications(userId)
                sampleList
            } else {
                list.map { it.toDomain() }
            }
        }
    }

    override fun getUnreadCountFlow(userId: String): Flow<Int> {
        return dao.getUnreadCount(userId)
    }

    override suspend fun markAsRead(userId: String, notificationId: String): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            dao.markAsRead(userId, notificationId, now)
            
            // Sync to Firestore asynchronously
            try {
                firestore.collection("users")
                    .document(userId)
                    .collection("notifications")
                    .document(notificationId)
                    .update(
                        mapOf(
                            "readAt" to now,
                            "deliveryStatus" to DeliveryStatus.READ.name
                        )
                    ).await()
            } catch (e: Exception) {
                Log.w("NotificationRepo", "Firestore update offline or failed", e)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAllAsRead(userId: String): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            dao.markAllAsRead(userId, now)
            
            try {
                val snapshot = firestore.collection("users")
                    .document(userId)
                    .collection("notifications")
                    .whereEqualTo("readAt", null)
                    .get()
                    .await()

                val batch = firestore.batch()
                snapshot.documents.forEach { doc ->
                    batch.update(doc.reference, mapOf("readAt" to now, "deliveryStatus" to DeliveryStatus.READ.name))
                }
                batch.commit().await()
            } catch (e: Exception) {
                Log.w("NotificationRepo", "Firestore markAllAsRead batch failed or offline", e)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNotification(userId: String, notificationId: String): Result<Unit> {
        return try {
            dao.deleteNotification(userId, notificationId)
            try {
                firestore.collection("users")
                    .document(userId)
                    .collection("notifications")
                    .document(notificationId)
                    .delete()
                    .await()
            } catch (e: Exception) {
                Log.w("NotificationRepo", "Firestore delete failed or offline", e)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearAllNotifications(userId: String): Result<Unit> {
        return try {
            dao.clearAllNotifications(userId)
            try {
                val snapshot = firestore.collection("users")
                    .document(userId)
                    .collection("notifications")
                    .get()
                    .await()

                val batch = firestore.batch()
                snapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit().await()
            } catch (e: Exception) {
                Log.w("NotificationRepo", "Firestore clearAll batch failed or offline", e)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveNotification(notification: SirajNotification): Result<Unit> {
        return try {
            // Deduplication check: local entity insertion with REPLACE handles existing ID
            dao.insertNotification(NotificationEntity.fromDomain(notification))

            // Save to Firestore if connected
            try {
                val notifData = hashMapOf(
                    "id" to notification.id,
                    "userId" to notification.userId,
                    "type" to notification.type.name,
                    "title" to notification.title,
                    "body" to notification.body,
                    "entityType" to notification.entityType,
                    "entityId" to notification.entityId,
                    "readAt" to notification.readAt,
                    "createdAt" to notification.createdAt,
                    "expiresAt" to notification.expiresAt,
                    "deliveryStatus" to notification.deliveryStatus.name,
                    "isSensitive" to notification.isSensitive,
                    "actionUrl" to notification.actionUrl,
                    "metadata" to notification.metadata
                )

                firestore.collection("users")
                    .document(notification.userId)
                    .collection("notifications")
                    .document(notification.id)
                    .set(notifData, SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                Log.w("NotificationRepo", "Firestore saveNotification offline", e)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getPreferencesFlow(userId: String): Flow<NotificationPreferences> {
        return dao.getPreferences(userId).map { entity ->
            entity?.toDomain() ?: NotificationPreferences()
        }
    }

    override suspend fun updatePreferences(userId: String, preferences: NotificationPreferences): Result<Unit> {
        return try {
            dao.savePreferences(NotificationPreferencesEntity.fromDomain(userId, preferences))

            try {
                val prefsMap = hashMapOf(
                    "videoGeneration" to preferences.videoGeneration,
                    "exportStatus" to preferences.exportStatus,
                    "reviewRequests" to preferences.reviewRequests,
                    "reviewResults" to preferences.reviewResults,
                    "projectComments" to preferences.projectComments,
                    "newAudio" to preferences.newAudio,
                    "newFlashes" to preferences.newFlashes,
                    "prayerReminders" to preferences.prayerReminders,
                    "adhkarReminders" to preferences.adhkarReminders,
                    "subscriptionBilling" to preferences.subscriptionBilling,
                    "systemMessages" to preferences.systemMessages,
                    "marketingAllowed" to preferences.marketingAllowed,
                    "quietHoursEnabled" to preferences.quietHoursEnabled,
                    "quietHoursStartHour" to preferences.quietHoursStartHour,
                    "quietHoursStartMinute" to preferences.quietHoursStartMinute,
                    "quietHoursEndHour" to preferences.quietHoursEndHour,
                    "quietHoursEndMinute" to preferences.quietHoursEndMinute,
                    "hideSensitiveOnLockScreen" to preferences.hideSensitiveOnLockScreen,
                    "updatedAt" to System.currentTimeMillis()
                )

                firestore.collection("users")
                    .document(userId)
                    .collection("settings")
                    .document("notifications")
                    .set(prefsMap, SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                Log.w("NotificationRepo", "Firestore updatePreferences offline", e)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registerDeviceToken(userId: String, tokenInfo: DeviceTokenInfo): Result<Unit> {
        return try {
            val tokenData = hashMapOf(
                "token" to tokenInfo.token,
                "deviceModel" to tokenInfo.deviceModel,
                "platform" to tokenInfo.platform,
                "lastUpdated" to tokenInfo.lastUpdated,
                "isActive" to tokenInfo.isActive,
                "appVersion" to tokenInfo.appVersion
            )

            firestore.collection("users")
                .document(userId)
                .collection("deviceTokens")
                .document(tokenInfo.token)
                .set(tokenData, SetOptions.merge())
                .await()

            Log.d("NotificationRepo", "FCM Device Token registered successfully for user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Failed to register FCM device token", e)
            Result.failure(e)
        }
    }

    override suspend fun unregisterDeviceToken(userId: String, token: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("deviceTokens")
                .document(token)
                .update("isActive", false)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cleanStaleTokensAndExpired(userId: String): Result<Unit> {
        return try {
            // Delete expired local notifications
            dao.deleteExpiredNotifications()

            // 60 days threshold for stale FCM tokens
            val staleThreshold = System.currentTimeMillis() - (60L * 24 * 60 * 60 * 1000)
            val staleTokensSnapshot = firestore.collection("users")
                .document(userId)
                .collection("deviceTokens")
                .whereLessThan("lastUpdated", staleThreshold)
                .get()
                .await()

            val batch = firestore.batch()
            staleTokensSnapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun startFirestoreSync(userId: String) {
        if (firestoreListener != null) return
        try {
            firestoreListener = firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("NotificationRepo", "Firestore snapshot error", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        coroutineScope.launch {
                            val entities = snapshot.documents.mapNotNull { doc ->
                                try {
                                    val id = doc.getString("id") ?: doc.id
                                    val type = doc.getString("type") ?: NotificationType.SYSTEM_MESSAGE.name
                                    val title = doc.getString("title") ?: ""
                                    val body = doc.getString("body") ?: ""
                                    val entityType = doc.getString("entityType")
                                    val entityId = doc.getString("entityId")
                                    val readAt = doc.getLong("readAt")
                                    val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                                    val expiresAt = doc.getLong("expiresAt")
                                    val deliveryStatus = doc.getString("deliveryStatus") ?: DeliveryStatus.DELIVERED.name
                                    val isSensitive = doc.getBoolean("isSensitive") ?: false
                                    val actionUrl = doc.getString("actionUrl")

                                    NotificationEntity(
                                        id = id,
                                        userId = userId,
                                        type = type,
                                        title = title,
                                        body = body,
                                        entityType = entityType,
                                        entityId = entityId,
                                        readAt = readAt,
                                        createdAt = createdAt,
                                        expiresAt = expiresAt,
                                        deliveryStatus = deliveryStatus,
                                        isSensitive = isSensitive,
                                        actionUrl = actionUrl
                                    )
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            if (entities.isNotEmpty()) {
                                dao.insertNotifications(entities)
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Failed to start Firestore listener", e)
        }
    }

    private fun getInitialNotifications(userId: String): List<SirajNotification> {
        val now = System.currentTimeMillis()
        val sampleList = listOf(
            SirajNotification(
                id = "notif_welcome_1",
                userId = userId,
                type = NotificationType.SYSTEM_MESSAGE,
                title = "مرحباً بك في منصة سراج",
                body = "تم إعداد مساحة العمل الخاصة بك بنجاح. يمكنك الآن البدء بإنتاج المحتوى الهادف والموثق.",
                entityType = "SYSTEM",
                entityId = "welcome",
                readAt = null,
                createdAt = now - 1000 * 60 * 15 // 15 mins ago
            ),
            SirajNotification(
                id = "notif_adhkar_2",
                userId = userId,
                type = NotificationType.MORNING_EVENING_ADHKAR,
                title = "أذكار المساء",
                body = "حان وقت أذكار المساء، حصّن نفسك بذكر الله.",
                entityType = "MIHRAB",
                entityId = "evening_adhkar",
                readAt = null,
                createdAt = now - 1000 * 60 * 60 * 2 // 2 hours ago
            ),
            SirajNotification(
                id = "notif_audio_3",
                userId = userId,
                type = NotificationType.NEW_AUDIO_CONTENT,
                title = "تلاوة جديدة في المكتبة الصوتية",
                body = "تمت إضافة تلاوة خاشعة بصوت الشيخ مشاري العفاسي لسورة الملك.",
                entityType = "AUDIO",
                entityId = "surah_67",
                readAt = now - 1000 * 60 * 60 * 5,
                createdAt = now - 1000 * 60 * 60 * 5 // 5 hours ago
            ),
            SirajNotification(
                id = "notif_video_4",
                userId = userId,
                type = NotificationType.VIDEO_GENERATION_COMPLETED,
                title = "اكتمل تصيير الفيديو",
                body = "تم الانتهاء من تركيب وتصدير مشروع 'فضائل سورة الكهف' بجودة 1080p.",
                entityType = "PROJECT",
                entityId = "sample_project_1",
                readAt = null,
                createdAt = now - 1000 * 60 * 60 * 24 // 1 day ago
            )
        )

        // Seed into Room DB asynchronously
        coroutineScope.launch {
            try {
                dao.insertNotifications(sampleList.map { NotificationEntity.fromDomain(it) })
            } catch (e: Exception) {
                Log.e("NotificationRepo", "Seeding initial notifications error", e)
            }
        }
        return sampleList
    }
}
