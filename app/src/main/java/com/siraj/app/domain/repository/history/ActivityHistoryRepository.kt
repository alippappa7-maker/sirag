package com.siraj.app.domain.repository.history

import com.siraj.app.domain.models.history.ActivityEntityType
import com.siraj.app.domain.models.history.ActivityHistoryPreferences
import com.siraj.app.domain.models.history.ActivityTab
import com.siraj.app.domain.models.history.UserActivityItem
import kotlinx.coroutines.flow.Flow

interface ActivityHistoryRepository {
    fun observeHistory(
        userId: String,
        tab: ActivityTab,
        limit: Int = 20,
        offset: Int = 0,
    ): Flow<List<UserActivityItem>>

    fun getRecentResumeItem(userId: String): Flow<UserActivityItem?>

    suspend fun getLastPosition(
        userId: String,
        entityType: ActivityEntityType,
        entityId: String,
    ): Long?

    suspend fun recordPlaybackPosition(item: UserActivityItem)

    suspend fun markAsCompleted(
        userId: String,
        entityType: ActivityEntityType,
        entityId: String,
    )

    suspend fun toggleWatchLater(
        userId: String,
        item: UserActivityItem,
    )

    suspend fun toggleDownloaded(
        userId: String,
        item: UserActivityItem,
    )

    suspend fun deleteItem(
        userId: String,
        id: String,
    )

    suspend fun clearAllHistory(userId: String)

    suspend fun clearCompleted(userId: String)

    suspend fun clearDownloads(userId: String)

    fun observePreferences(userId: String): Flow<ActivityHistoryPreferences>

    suspend fun updatePreferences(
        userId: String,
        preferences: ActivityHistoryPreferences,
    )

    suspend fun syncPending(userId: String)

    suspend fun applyRetentionPolicy(userId: String)

    suspend fun purgeUserAllData(userId: String)
}
