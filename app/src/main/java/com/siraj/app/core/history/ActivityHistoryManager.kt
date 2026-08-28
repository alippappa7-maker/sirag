package com.siraj.app.core.history

import android.content.Context
import com.siraj.app.data.repository.FirebaseActivityHistoryRepositoryImpl
import com.siraj.app.domain.models.history.ActivityEntityType
import com.siraj.app.domain.models.history.UserActivityItem
import com.siraj.app.domain.repository.history.ActivityHistoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object ActivityHistoryManager {
    private var repository: ActivityHistoryRepository? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var currentUserId: String = "user_default"

    fun initialize(context: Context) {
        if (repository == null) {
            repository = FirebaseActivityHistoryRepositoryImpl(context.applicationContext)
        }
    }

    fun setCurrentUser(userId: String) {
        currentUserId = userId
    }

    fun getRepository(): ActivityHistoryRepository? = repository

    fun recordProgress(
        entityType: ActivityEntityType,
        entityId: String,
        title: String,
        subtitle: String? = null,
        mediaUrl: String? = null,
        thumbnailUrl: String? = null,
        positionMs: Long,
        durationMs: Long,
        userId: String = currentUserId
    ) {
        val repo = repository ?: return
        scope.launch {
            val item = UserActivityItem(
                id = "$userId-${entityType.name}-$entityId",
                userId = userId,
                entityType = entityType,
                entityId = entityId,
                title = title,
                subtitle = subtitle,
                mediaUrl = mediaUrl,
                thumbnailUrl = thumbnailUrl,
                positionMs = positionMs,
                durationMs = durationMs
            )
            repo.recordPlaybackPosition(item)
        }
    }

    suspend fun getSavedPosition(
        entityType: ActivityEntityType,
        entityId: String,
        userId: String = currentUserId
    ): Long? {
        val repo = repository ?: return null
        return repo.getLastPosition(userId, entityType, entityId)
    }

    fun markCompleted(
        entityType: ActivityEntityType,
        entityId: String,
        userId: String = currentUserId
    ) {
        val repo = repository ?: return
        scope.launch {
            repo.markAsCompleted(userId, entityType, entityId)
        }
    }

    fun toggleWatchLater(
        item: UserActivityItem,
        userId: String = currentUserId
    ) {
        val repo = repository ?: return
        scope.launch {
            repo.toggleWatchLater(userId, item)
        }
    }
}
