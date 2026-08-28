package com.siraj.app.data.local

import android.content.Context
import androidx.room.*
import com.siraj.app.domain.models.history.ActivityEntityType
import com.siraj.app.domain.models.history.ActivityHistoryPreferences
import com.siraj.app.domain.models.history.RetentionPolicy
import com.siraj.app.domain.models.history.SyncStatus
import com.siraj.app.domain.models.history.UserActivityItem
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "activity_history",
    indices = [
        Index(value = ["userId", "entityType", "entityId"], unique = true),
        Index(value = ["userId", "lastPlayedAt"]),
        Index(value = ["userId", "isWatchLater"]),
        Index(value = ["userId", "isDownloaded"])
    ]
)
data class ActivityHistoryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val entityType: String,
    val entityId: String,
    val title: String,
    val subtitle: String?,
    val mediaUrl: String?,
    val thumbnailUrl: String?,
    val positionMs: Long,
    val durationMs: Long,
    val progressPercent: Float,
    val completed: Boolean,
    val lastPlayedAt: Long,
    val deviceId: String,
    val isWatchLater: Boolean,
    val isDownloaded: Boolean,
    val syncStatus: String
) {
    fun toDomain(): UserActivityItem {
        val parsedEntityType = try {
            ActivityEntityType.valueOf(entityType)
        } catch (_: Exception) {
            ActivityEntityType.VIDEO
        }

        val parsedSyncStatus = try {
            SyncStatus.valueOf(syncStatus)
        } catch (_: Exception) {
            SyncStatus.SYNCED
        }

        return UserActivityItem(
            id = id,
            userId = userId,
            entityType = parsedEntityType,
            entityId = entityId,
            title = title,
            subtitle = subtitle,
            mediaUrl = mediaUrl,
            thumbnailUrl = thumbnailUrl,
            positionMs = positionMs,
            durationMs = durationMs,
            progressPercent = progressPercent,
            completed = completed,
            lastPlayedAt = lastPlayedAt,
            deviceId = deviceId,
            isWatchLater = isWatchLater,
            isDownloaded = isDownloaded,
            syncStatus = parsedSyncStatus
        )
    }

    companion object {
        fun fromDomain(item: UserActivityItem): ActivityHistoryEntity {
            return ActivityHistoryEntity(
                id = item.id,
                userId = item.userId,
                entityType = item.entityType.name,
                entityId = item.entityId,
                title = item.title,
                subtitle = item.subtitle,
                mediaUrl = item.mediaUrl,
                thumbnailUrl = item.thumbnailUrl,
                positionMs = item.positionMs,
                durationMs = item.durationMs,
                progressPercent = item.progressPercent,
                completed = item.completed,
                lastPlayedAt = item.lastPlayedAt,
                deviceId = item.deviceId,
                isWatchLater = item.isWatchLater,
                isDownloaded = item.isDownloaded,
                syncStatus = item.syncStatus.name
            )
        }
    }
}

@Entity(tableName = "activity_history_preferences")
data class ActivityHistoryPreferencesEntity(
    @PrimaryKey val userId: String,
    val isHistoryEnabled: Boolean = true,
    val isSyncEnabled: Boolean = true,
    val retentionPolicy: String = RetentionPolicy.DAYS_90.name,
    val saveWatchHistory: Boolean = true,
    val saveListenHistory: Boolean = true,
    val saveDownloadsHistory: Boolean = true
) {
    fun toDomain(): ActivityHistoryPreferences {
        val policy = try {
            RetentionPolicy.valueOf(retentionPolicy)
        } catch (_: Exception) {
            RetentionPolicy.DAYS_90
        }
        return ActivityHistoryPreferences(
            isHistoryEnabled = isHistoryEnabled,
            isSyncEnabled = isSyncEnabled,
            retentionPolicy = policy,
            saveWatchHistory = saveWatchHistory,
            saveListenHistory = saveListenHistory,
            saveDownloadsHistory = saveDownloadsHistory
        )
    }

    companion object {
        fun fromDomain(userId: String, prefs: ActivityHistoryPreferences): ActivityHistoryPreferencesEntity {
            return ActivityHistoryPreferencesEntity(
                userId = userId,
                isHistoryEnabled = prefs.isHistoryEnabled,
                isSyncEnabled = prefs.isSyncEnabled,
                retentionPolicy = prefs.retentionPolicy.name,
                saveWatchHistory = prefs.saveWatchHistory,
                saveListenHistory = prefs.saveListenHistory,
                saveDownloadsHistory = prefs.saveDownloadsHistory
            )
        }
    }
}

@Dao
interface ActivityHistoryDao {
    @Query("SELECT * FROM activity_history WHERE userId = :userId ORDER BY lastPlayedAt DESC LIMIT :limit OFFSET :offset")
    fun getAllHistory(userId: String, limit: Int, offset: Int): Flow<List<ActivityHistoryEntity>>

    @Query("SELECT * FROM activity_history WHERE userId = :userId AND (entityType = 'VIDEO' OR entityType = 'FLASH') ORDER BY lastPlayedAt DESC LIMIT :limit OFFSET :offset")
    fun getVideoHistory(userId: String, limit: Int, offset: Int): Flow<List<ActivityHistoryEntity>>

    @Query("SELECT * FROM activity_history WHERE userId = :userId AND (entityType = 'AUDIO' OR entityType = 'QURAN_RECITATION') ORDER BY lastPlayedAt DESC LIMIT :limit OFFSET :offset")
    fun getAudioHistory(userId: String, limit: Int, offset: Int): Flow<List<ActivityHistoryEntity>>

    @Query("SELECT * FROM activity_history WHERE userId = :userId AND isWatchLater = 1 ORDER BY lastPlayedAt DESC LIMIT :limit OFFSET :offset")
    fun getWatchLater(userId: String, limit: Int, offset: Int): Flow<List<ActivityHistoryEntity>>

    @Query("SELECT * FROM activity_history WHERE userId = :userId AND isDownloaded = 1 ORDER BY lastPlayedAt DESC LIMIT :limit OFFSET :offset")
    fun getDownloads(userId: String, limit: Int, offset: Int): Flow<List<ActivityHistoryEntity>>

    @Query("SELECT * FROM activity_history WHERE userId = :userId AND completed = 1 ORDER BY lastPlayedAt DESC LIMIT :limit OFFSET :offset")
    fun getCompleted(userId: String, limit: Int, offset: Int): Flow<List<ActivityHistoryEntity>>

    @Query("SELECT * FROM activity_history WHERE userId = :userId ORDER BY lastPlayedAt DESC LIMIT 1")
    fun getMostRecentItem(userId: String): Flow<ActivityHistoryEntity?>

    @Query("SELECT * FROM activity_history WHERE userId = :userId AND entityType = :entityType AND entityId = :entityId LIMIT 1")
    suspend fun getItem(userId: String, entityType: String, entityId: String): ActivityHistoryEntity?

    @Query("SELECT * FROM activity_history WHERE userId = :userId AND syncStatus != 'SYNCED'")
    suspend fun getPendingSyncItems(userId: String): List<ActivityHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: ActivityHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ActivityHistoryEntity>)

    @Query("DELETE FROM activity_history WHERE userId = :userId AND id = :id")
    suspend fun deleteById(userId: String, id: String)

    @Query("DELETE FROM activity_history WHERE userId = :userId AND entityType = :entityType AND entityId = :entityId")
    suspend fun deleteByEntity(userId: String, entityType: String, entityId: String)

    @Query("DELETE FROM activity_history WHERE userId = :userId")
    suspend fun clearAll(userId: String)

    @Query("DELETE FROM activity_history WHERE userId = :userId AND completed = 1")
    suspend fun clearCompleted(userId: String)

    @Query("UPDATE activity_history SET isDownloaded = 0 WHERE userId = :userId")
    suspend fun clearDownloads(userId: String)

    @Query("DELETE FROM activity_history WHERE userId = :userId AND lastPlayedAt < :cutoffTimestamp")
    suspend fun deleteOlderThan(userId: String, cutoffTimestamp: Long)

    @Query("SELECT * FROM activity_history_preferences WHERE userId = :userId")
    fun getPreferences(userId: String): Flow<ActivityHistoryPreferencesEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePreferences(preferences: ActivityHistoryPreferencesEntity)
}

@Database(
    entities = [ActivityHistoryEntity::class, ActivityHistoryPreferencesEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ActivityHistoryDatabase : RoomDatabase() {
    abstract fun activityHistoryDao(): ActivityHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: ActivityHistoryDatabase? = null

        fun getDatabase(context: Context): ActivityHistoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ActivityHistoryDatabase::class.java,
                    "siraj_activity_history.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
