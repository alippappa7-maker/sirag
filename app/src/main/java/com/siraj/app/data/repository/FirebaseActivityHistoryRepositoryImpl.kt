package com.siraj.app.data.repository

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.siraj.app.data.local.ActivityHistoryDao
import com.siraj.app.data.local.ActivityHistoryDatabase
import com.siraj.app.data.local.ActivityHistoryEntity
import com.siraj.app.data.local.ActivityHistoryPreferencesEntity
import com.siraj.app.domain.models.history.ActivityEntityType
import com.siraj.app.domain.models.history.ActivityHistoryPreferences
import com.siraj.app.domain.models.history.ActivityTab
import com.siraj.app.domain.models.history.RetentionPolicy
import com.siraj.app.domain.models.history.SyncStatus
import com.siraj.app.domain.models.history.UserActivityItem
import com.siraj.app.domain.repository.history.ActivityHistoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import com.siraj.app.core.error.GlobalErrorHandler

class FirebaseActivityHistoryRepositoryImpl(
    private val context: Context
) : ActivityHistoryRepository {

    private val tag = "ActivityHistoryRepo"
    private val database = ActivityHistoryDatabase.getDatabase(context)
    private val dao: ActivityHistoryDao = database.activityHistoryDao()

    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else {
                null
            }
        } catch (e: Exception) {
            GlobalErrorHandler.handle(e, tag)
            Log.w(tag, "Firestore not available: ${e.message}")
            null
        }
    }

    private val deviceId: String by lazy {
        try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                ?: UUID.randomUUID().toString()
        } catch (e: Exception) {
            GlobalErrorHandler.handle(e)
            UUID.randomUUID().toString()
        }
    }

    override fun observeHistory(
        userId: String,
        tab: ActivityTab,
        limit: Int,
        offset: Int
    ): Flow<List<UserActivityItem>> {
        val flow = when (tab) {
            ActivityTab.ALL -> dao.getAllHistory(userId, limit, offset)
            ActivityTab.VIDEO -> dao.getVideoHistory(userId, limit, offset)
            ActivityTab.AUDIO -> dao.getAudioHistory(userId, limit, offset)
            ActivityTab.WATCH_LATER -> dao.getWatchLater(userId, limit, offset)
            ActivityTab.DOWNLOADED -> dao.getDownloads(userId, limit, offset)
            ActivityTab.COMPLETED -> dao.getCompleted(userId, limit, offset)
        }

        return flow.map { list -> list.map { it.toDomain() } }.flowOn(Dispatchers.IO)
    }

    override fun getRecentResumeItem(userId: String): Flow<UserActivityItem?> {
        return dao.getMostRecentItem(userId)
            .map { it?.toDomain() }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun getLastPosition(
        userId: String,
        entityType: ActivityEntityType,
        entityId: String
    ): Long? = withContext(Dispatchers.IO) {
        val entity = dao.getItem(userId, entityType.name, entityId)
        entity?.positionMs
    }

    override suspend fun recordPlaybackPosition(item: UserActivityItem): Unit = withContext(Dispatchers.IO) {
        // First check if history recording is enabled
        val prefs = observePreferences(item.userId).firstOrNull() ?: ActivityHistoryPreferences()
        if (!prefs.isHistoryEnabled) {
            Log.d(tag, "History recording is disabled by user. Skipping record for ${item.entityId}")
            return@withContext
        }

        val duration = item.durationMs.coerceAtLeast(0L)
        val position = item.positionMs.coerceIn(0L, if (duration > 0) duration else Long.MAX_VALUE)
        val progress = if (duration > 0) {
            ((position.toFloat() / duration.toFloat()) * 100f).coerceIn(0f, 100f)
        } else {
            0f
        }
        val isCompleted = item.completed || progress >= 90f

        // Retrieve existing item to preserve watchLater and download flags
        val existing = dao.getItem(item.userId, item.entityType.name, item.entityId)
        val updatedItem = item.copy(
            id = existing?.id ?: item.id.ifEmpty { "${item.userId}-${item.entityType.name}-${item.entityId}" },
            positionMs = position,
            durationMs = duration,
            progressPercent = progress,
            completed = isCompleted,
            lastPlayedAt = System.currentTimeMillis(),
            deviceId = deviceId,
            isWatchLater = existing?.isWatchLater ?: item.isWatchLater,
            isDownloaded = existing?.isDownloaded ?: item.isDownloaded,
            syncStatus = if (prefs.isSyncEnabled) SyncStatus.PENDING_SYNC else SyncStatus.SYNCED
        )

        dao.insertOrUpdate(ActivityHistoryEntity.fromDomain(updatedItem))

        // Cloud sync if enabled
        if (prefs.isSyncEnabled) {
            syncSingleItemToCloud(updatedItem)
        }
    }

    override suspend fun markAsCompleted(
        userId: String,
        entityType: ActivityEntityType,
        entityId: String
    ): Unit = withContext(Dispatchers.IO) {
        val existing = dao.getItem(userId, entityType.name, entityId)
        if (existing != null) {
            val updated = existing.copy(
                completed = true,
                progressPercent = 100f,
                positionMs = existing.durationMs,
                lastPlayedAt = System.currentTimeMillis(),
                syncStatus = SyncStatus.PENDING_SYNC.name
            )
            dao.insertOrUpdate(updated)
            syncSingleItemToCloud(updated.toDomain())
        }
    }

    override suspend fun toggleWatchLater(
        userId: String,
        item: UserActivityItem
    ): Unit = withContext(Dispatchers.IO) {
        val existing = dao.getItem(userId, item.entityType.name, item.entityId)
        val newWatchLaterState = !(existing?.isWatchLater ?: item.isWatchLater)
        val updatedItem = item.copy(
            id = existing?.id ?: item.id.ifEmpty { "${userId}-${item.entityType.name}-${item.entityId}" },
            userId = userId,
            isWatchLater = newWatchLaterState,
            lastPlayedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING_SYNC
        )
        dao.insertOrUpdate(ActivityHistoryEntity.fromDomain(updatedItem))
        syncSingleItemToCloud(updatedItem)
    }

    override suspend fun toggleDownloaded(
        userId: String,
        item: UserActivityItem
    ): Unit = withContext(Dispatchers.IO) {
        val existing = dao.getItem(userId, item.entityType.name, item.entityId)
        val newDownloadState = !(existing?.isDownloaded ?: item.isDownloaded)
        val updatedItem = item.copy(
            id = existing?.id ?: item.id.ifEmpty { "${userId}-${item.entityType.name}-${item.entityId}" },
            userId = userId,
            isDownloaded = newDownloadState,
            lastPlayedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING_SYNC
        )
        dao.insertOrUpdate(ActivityHistoryEntity.fromDomain(updatedItem))
        syncSingleItemToCloud(updatedItem)
    }

    override suspend fun deleteItem(
        userId: String,
        id: String
    ): Unit = withContext(Dispatchers.IO) {
        dao.deleteById(userId, id)
        try {
            firestore?.collection("users")
                ?.document(userId)
                ?.collection("activity_history")
                ?.document(id)
                ?.delete()
                ?.await()
        } catch (e: Exception) {
            GlobalErrorHandler.handle(e, tag)
        }
    }

    override suspend fun clearAllHistory(
        userId: String
    ): Unit = withContext(Dispatchers.IO) {
        dao.clearAll(userId)
        try {
            val snapshot = firestore?.collection("users")
                ?.document(userId)
                ?.collection("activity_history")
                ?.get()
                ?.await()

            snapshot?.documents?.forEach { doc ->
                doc.reference.delete()
            }
        } catch (e: Exception) {
            GlobalErrorHandler.handle(e, tag)
        }
    }

    override suspend fun clearCompleted(
        userId: String
    ): Unit = withContext(Dispatchers.IO) {
        dao.clearCompleted(userId)
    }

    override suspend fun clearDownloads(
        userId: String
    ): Unit = withContext(Dispatchers.IO) {
        dao.clearDownloads(userId)
    }

    override fun observePreferences(
        userId: String
    ): Flow<ActivityHistoryPreferences> {
        return dao.getPreferences(userId).map { entity ->
            entity?.toDomain() ?: ActivityHistoryPreferences()
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun updatePreferences(
        userId: String,
        preferences: ActivityHistoryPreferences
    ): Unit = withContext(Dispatchers.IO) {
        val entity = ActivityHistoryPreferencesEntity.fromDomain(userId, preferences)
        dao.savePreferences(entity)

        try {
            firestore?.collection("users")
                ?.document(userId)
                ?.collection("settings")
                ?.document("activity_preferences")
                ?.set(
                    mapOf(
                        "isHistoryEnabled" to preferences.isHistoryEnabled,
                        "isSyncEnabled" to preferences.isSyncEnabled,
                        "retentionPolicy" to preferences.retentionPolicy.name,
                        "saveWatchHistory" to preferences.saveWatchHistory,
                        "saveListenHistory" to preferences.saveListenHistory,
                        "saveDownloadsHistory" to preferences.saveDownloadsHistory,
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )?.await()
        } catch (e: Exception) {
            GlobalErrorHandler.handle(e, tag)
        }

        // Apply retention pruning immediately
        applyRetentionPolicy(userId)
    }

    override suspend fun syncPending(
        userId: String
    ): Unit = withContext(Dispatchers.IO) {
        val prefs = observePreferences(userId).firstOrNull() ?: ActivityHistoryPreferences()
        if (!prefs.isSyncEnabled) return@withContext

        val pending = dao.getPendingSyncItems(userId)
        pending.forEach { item ->
            syncSingleItemToCloud(item.toDomain())
        }

        // Fetch updates from cloud
        fetchCloudUpdates(userId)
    }

    override suspend fun applyRetentionPolicy(
        userId: String
    ): Unit = withContext(Dispatchers.IO) {
        val prefs = observePreferences(userId).firstOrNull() ?: ActivityHistoryPreferences()
        if (prefs.retentionPolicy == RetentionPolicy.FOREVER) {
            return@withContext
        }

        val days = prefs.retentionPolicy.days
        val cutoffTimestamp = System.currentTimeMillis() - (days.toLong() * 24 * 60 * 60 * 1000L)
        dao.deleteOlderThan(userId, cutoffTimestamp)
    }

    override suspend fun purgeUserAllData(
        userId: String
    ): Unit = withContext(Dispatchers.IO) {
        clearAllHistory(userId)
        try {
            firestore?.collection("users")
                ?.document(userId)
                ?.collection("settings")
                ?.document("activity_preferences")
                ?.delete()
                ?.await()
        } catch (e: Exception) {
            GlobalErrorHandler.handle(e, tag)
        }
    }

    private suspend fun syncSingleItemToCloud(item: UserActivityItem) {
        val fs = firestore ?: return
        try {
            val docData = mapOf(
                "id" to item.id,
                "userId" to item.userId,
                "entityType" to item.entityType.name,
                "entityId" to item.entityId,
                "title" to item.title,
                "subtitle" to item.subtitle,
                "mediaUrl" to item.mediaUrl,
                "thumbnailUrl" to item.thumbnailUrl,
                "positionMs" to item.positionMs,
                "durationMs" to item.durationMs,
                "progressPercent" to item.progressPercent,
                "completed" to item.completed,
                "lastPlayedAt" to item.lastPlayedAt,
                "deviceId" to item.deviceId,
                "isWatchLater" to item.isWatchLater,
                "isDownloaded" to item.isDownloaded
            )

            fs.collection("users")
                .document(item.userId)
                .collection("activity_history")
                .document(item.id)
                .set(docData, SetOptions.merge())
                .await()

            // Update local item status to SYNCED
            dao.insertOrUpdate(ActivityHistoryEntity.fromDomain(item.copy(syncStatus = SyncStatus.SYNCED)))
        } catch (e: Exception) {
            GlobalErrorHandler.handle(e, tag)
        }
    }

    private suspend fun fetchCloudUpdates(userId: String) {
        val fs = firestore ?: return
        try {
            val snapshot = fs.collection("users")
                .document(userId)
                .collection("activity_history")
                .get()
                .await()

            val cloudEntities = snapshot.documents.mapNotNull { doc ->
                val entityTypeStr = doc.getString("entityType") ?: return@mapNotNull null
                val entityId = doc.getString("entityId") ?: return@mapNotNull null
                val title = doc.getString("title") ?: ""
                val subtitle = doc.getString("subtitle")
                val mediaUrl = doc.getString("mediaUrl")
                val thumbnailUrl = doc.getString("thumbnailUrl")
                val positionMs = doc.getLong("positionMs") ?: 0L
                val durationMs = doc.getLong("durationMs") ?: 0L
                val progressPercent = (doc.getDouble("progressPercent") ?: 0.0).toFloat()
                val completed = doc.getBoolean("completed") ?: false
                val lastPlayedAt = doc.getLong("lastPlayedAt") ?: System.currentTimeMillis()
                val itemDeviceId = doc.getString("deviceId") ?: ""
                val isWatchLater = doc.getBoolean("isWatchLater") ?: false
                val isDownloaded = doc.getBoolean("isDownloaded") ?: false

                ActivityHistoryEntity(
                    id = doc.id,
                    userId = userId,
                    entityType = entityTypeStr,
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
                    deviceId = itemDeviceId,
                    isWatchLater = isWatchLater,
                    isDownloaded = isDownloaded,
                    syncStatus = SyncStatus.SYNCED.name
                )
            }

            if (cloudEntities.isNotEmpty()) {
                dao.insertAll(cloudEntities)
            }
        } catch (e: Exception) {
            GlobalErrorHandler.handle(e, tag)
        }
    }
}
