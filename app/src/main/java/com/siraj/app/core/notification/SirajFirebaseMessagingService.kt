package com.siraj.app.core.notification

import android.os.Build
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.siraj.app.data.repository.FirebaseNotificationRepositoryImpl
import com.siraj.app.domain.models.notification.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class SirajFirebaseMessagingService : FirebaseMessagingService() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val notificationRepository by lazy { FirebaseNotificationRepositoryImpl(applicationContext) }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("SirajFCM", "New FCM Token received: $token")
        
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous_user"
        coroutineScope.launch {
            val tokenInfo = DeviceTokenInfo(
                token = token,
                deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
                platform = "ANDROID",
                lastUpdated = System.currentTimeMillis(),
                isActive = true
            )
            notificationRepository.registerDeviceToken(currentUserId, tokenInfo)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("SirajFCM", "FCM Message received from: ${remoteMessage.from}")

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous_user"
        val data = remoteMessage.data

        // Extract or derive notification fields
        val id = data["id"] ?: "fcm_${remoteMessage.messageId ?: System.currentTimeMillis()}"
        val typeStr = data["type"] ?: NotificationType.SYSTEM_MESSAGE.name
        val type = try {
            NotificationType.valueOf(typeStr)
        } catch (_: Exception) {
            NotificationType.SYSTEM_MESSAGE
        }

        val title = remoteMessage.notification?.title ?: data["title"] ?: type.titleAr
        val body = remoteMessage.notification?.body ?: data["body"] ?: ""
        val entityType = data["entityType"]
        val entityId = data["entityId"]
        val isSensitive = data["isSensitive"]?.toBooleanStrictOrNull() ?: false
        val actionUrl = data["actionUrl"]

        val sirajNotification = SirajNotification(
            id = id,
            userId = currentUserId,
            type = type,
            title = title,
            body = body,
            entityType = entityType,
            entityId = entityId,
            readAt = null,
            createdAt = System.currentTimeMillis(),
            deliveryStatus = DeliveryStatus.DELIVERED,
            isSensitive = isSensitive,
            actionUrl = actionUrl,
            metadata = data
        )

        coroutineScope.launch {
            // 1. Fetch user preferences
            val prefs = notificationRepository.getPreferencesFlow(currentUserId).firstOrNull() ?: NotificationPreferences()

            // 2. Persist locally and in Firestore
            notificationRepository.saveNotification(sirajNotification)

            // 3. Show System Android Notification if allowed
            NotificationHelper.showSystemNotification(
                context = applicationContext,
                notification = sirajNotification,
                preferences = prefs
            )
        }
    }
}
