package com.siraj.app.domain.repository.notification

import com.siraj.app.domain.models.notification.DeviceTokenInfo
import com.siraj.app.domain.models.notification.NotificationPreferences
import com.siraj.app.domain.models.notification.SirajNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    /**
     * Observes real-time list of notifications for the given user.
     */
    fun getNotificationsFlow(userId: String): Flow<List<SirajNotification>>

    /**
     * Observes unread count for badge indicators.
     */
    fun getUnreadCountFlow(userId: String): Flow<Int>

    /**
     * Marks a specific notification as read.
     */
    suspend fun markAsRead(
        userId: String,
        notificationId: String,
    ): Result<Unit>

    /**
     * Marks all notifications for a user as read.
     */
    suspend fun markAllAsRead(userId: String): Result<Unit>

    /**
     * Deletes a specific notification.
     */
    suspend fun deleteNotification(
        userId: String,
        notificationId: String,
    ): Result<Unit>

    /**
     * Clears all notifications for the user.
     */
    suspend fun clearAllNotifications(userId: String): Result<Unit>

    /**
     * Saves or receives an in-app/push notification locally and in Firestore.
     */
    suspend fun saveNotification(notification: SirajNotification): Result<Unit>

    /**
     * Observes user notification preferences.
     */
    fun getPreferencesFlow(userId: String): Flow<NotificationPreferences>

    /**
     * Updates user notification preferences.
     */
    suspend fun updatePreferences(
        userId: String,
        preferences: NotificationPreferences,
    ): Result<Unit>

    /**
     * Registers or updates device FCM token for push notifications.
     */
    suspend fun registerDeviceToken(
        userId: String,
        tokenInfo: DeviceTokenInfo,
    ): Result<Unit>

    /**
     * Unregisters/deactivates device token (e.g. on logout).
     */
    suspend fun unregisterDeviceToken(
        userId: String,
        token: String,
    ): Result<Unit>

    /**
     * Cleans up expired notifications and stale device tokens.
     */
    suspend fun cleanStaleTokensAndExpired(userId: String): Result<Unit>
}
