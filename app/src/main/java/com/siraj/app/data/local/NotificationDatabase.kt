package com.siraj.app.data.local

import android.content.Context
import androidx.room.*
import com.siraj.app.domain.models.notification.DeliveryStatus
import com.siraj.app.domain.models.notification.NotificationPreferences
import com.siraj.app.domain.models.notification.NotificationType
import com.siraj.app.domain.models.notification.SirajNotification
import kotlinx.coroutines.flow.Flow
import com.siraj.app.core.error.GlobalErrorHandler

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val type: String, // String representation of NotificationType
    val title: String,
    val body: String,
    val entityType: String?,
    val entityId: String?,
    val readAt: Long?,
    val createdAt: Long,
    val expiresAt: Long?,
    val deliveryStatus: String,
    val isSensitive: Boolean,
    val actionUrl: String?
) {
    fun toDomain(): SirajNotification {
        val parsedType = try {
            NotificationType.valueOf(type)
        } catch (e: Exception) {
            GlobalErrorHandler.handle(e)
            NotificationType.SYSTEM_MESSAGE
        }

        val parsedStatus = try {
            DeliveryStatus.valueOf(deliveryStatus)
        } catch (e: Exception) {
            GlobalErrorHandler.handle(e)
            DeliveryStatus.DELIVERED
        }

        return SirajNotification(
            id = id,
            userId = userId,
            type = parsedType,
            title = title,
            body = body,
            entityType = entityType,
            entityId = entityId,
            readAt = readAt,
            createdAt = createdAt,
            expiresAt = expiresAt,
            deliveryStatus = parsedStatus,
            isSensitive = isSensitive,
            actionUrl = actionUrl
        )
    }

    companion object {
        fun fromDomain(notification: SirajNotification): NotificationEntity {
            return NotificationEntity(
                id = notification.id,
                userId = notification.userId,
                type = notification.type.name,
                title = notification.title,
                body = notification.body,
                entityType = notification.entityType,
                entityId = notification.entityId,
                readAt = notification.readAt,
                createdAt = notification.createdAt,
                expiresAt = notification.expiresAt,
                deliveryStatus = notification.deliveryStatus.name,
                isSensitive = notification.isSensitive,
                actionUrl = notification.actionUrl
            )
        }
    }
}

@Entity(tableName = "notification_preferences")
data class NotificationPreferencesEntity(
    @PrimaryKey val userId: String,
    val videoGeneration: Boolean,
    val exportStatus: Boolean,
    val reviewRequests: Boolean,
    val reviewResults: Boolean,
    val projectComments: Boolean,
    val newAudio: Boolean,
    val newFlashes: Boolean,
    val prayerReminders: Boolean,
    val adhkarReminders: Boolean,
    val subscriptionBilling: Boolean,
    val systemMessages: Boolean,
    val marketingAllowed: Boolean,
    val quietHoursEnabled: Boolean,
    val quietHoursStartHour: Int,
    val quietHoursStartMinute: Int,
    val quietHoursEndHour: Int,
    val quietHoursEndMinute: Int,
    val hideSensitiveOnLockScreen: Boolean
) {
    fun toDomain(): NotificationPreferences {
        return NotificationPreferences(
            videoGeneration = videoGeneration,
            exportStatus = exportStatus,
            reviewRequests = reviewRequests,
            reviewResults = reviewResults,
            projectComments = projectComments,
            newAudio = newAudio,
            newFlashes = newFlashes,
            prayerReminders = prayerReminders,
            adhkarReminders = adhkarReminders,
            subscriptionBilling = subscriptionBilling,
            systemMessages = systemMessages,
            marketingAllowed = marketingAllowed,
            quietHoursEnabled = quietHoursEnabled,
            quietHoursStartHour = quietHoursStartHour,
            quietHoursStartMinute = quietHoursStartMinute,
            quietHoursEndHour = quietHoursEndHour,
            quietHoursEndMinute = quietHoursEndMinute,
            hideSensitiveOnLockScreen = hideSensitiveOnLockScreen
        )
    }

    companion object {
        fun fromDomain(userId: String, prefs: NotificationPreferences): NotificationPreferencesEntity {
            return NotificationPreferencesEntity(
                userId = userId,
                videoGeneration = prefs.videoGeneration,
                exportStatus = prefs.exportStatus,
                reviewRequests = prefs.reviewRequests,
                reviewResults = prefs.reviewResults,
                projectComments = prefs.projectComments,
                newAudio = prefs.newAudio,
                newFlashes = prefs.newFlashes,
                prayerReminders = prefs.prayerReminders,
                adhkarReminders = prefs.adhkarReminders,
                subscriptionBilling = prefs.subscriptionBilling,
                systemMessages = prefs.systemMessages,
                marketingAllowed = prefs.marketingAllowed,
                quietHoursEnabled = prefs.quietHoursEnabled,
                quietHoursStartHour = prefs.quietHoursStartHour,
                quietHoursStartMinute = prefs.quietHoursStartMinute,
                quietHoursEndHour = prefs.quietHoursEndHour,
                quietHoursEndMinute = prefs.quietHoursEndMinute,
                hideSensitiveOnLockScreen = prefs.hideSensitiveOnLockScreen
            )
        }
    }
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND readAt IS NULL")
    fun getUnreadCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET readAt = :timestamp, deliveryStatus = 'READ' WHERE id = :id AND userId = :userId")
    suspend fun markAsRead(userId: String, id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE notifications SET readAt = :timestamp, deliveryStatus = 'READ' WHERE userId = :userId AND readAt IS NULL")
    suspend fun markAllAsRead(userId: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM notifications WHERE id = :id AND userId = :userId")
    suspend fun deleteNotification(userId: String, id: String)

    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun clearAllNotifications(userId: String)

    @Query("DELETE FROM notifications WHERE expiresAt IS NOT NULL AND expiresAt < :currentTime")
    suspend fun deleteExpiredNotifications(currentTime: Long = System.currentTimeMillis())

    @Query("SELECT * FROM notification_preferences WHERE userId = :userId")
    fun getPreferences(userId: String): Flow<NotificationPreferencesEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePreferences(preferences: NotificationPreferencesEntity)
}

@Database(entities = [NotificationEntity::class, NotificationPreferencesEntity::class], version = 1, exportSchema = false)
abstract class NotificationDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: NotificationDatabase? = null

        fun getDatabase(context: Context): NotificationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotificationDatabase::class.java,
                    "siraj_notifications.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
