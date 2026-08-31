package com.siraj.app.features.history

import com.siraj.app.domain.models.history.*
import com.siraj.app.domain.repository.history.ActivityHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FakeActivityHistoryRepository : ActivityHistoryRepository {
    val items = mutableMapOf<String, UserActivityItem>()
    val preferences = MutableStateFlow(ActivityHistoryPreferences())

    override fun observeHistory(
        userId: String,
        tab: ActivityTab,
        limit: Int,
        offset: Int,
    ): Flow<List<UserActivityItem>> =
        flowOf(
            items.values
                .filter { it.userId == userId }
                .filter { item ->
                    when (tab) {
                        ActivityTab.ALL -> true
                        ActivityTab.VIDEO -> item.entityType == ActivityEntityType.VIDEO || item.entityType == ActivityEntityType.FLASH
                        ActivityTab.AUDIO ->
                            item.entityType == ActivityEntityType.AUDIO ||
                                item.entityType == ActivityEntityType.QURAN_RECITATION
                        ActivityTab.WATCH_LATER -> item.isWatchLater
                        ActivityTab.DOWNLOADED -> item.isDownloaded
                        ActivityTab.COMPLETED -> item.completed
                    }
                }.sortedByDescending { it.lastPlayedAt }
                .drop(offset)
                .take(limit),
        )

    override fun getRecentResumeItem(userId: String): Flow<UserActivityItem?> =
        preferences.map { prefs ->
            if (!prefs.isHistoryEnabled) {
                null
            } else {
                items.values
                    .filter { it.userId == userId && !it.completed && it.positionMs > 0 }
                    .maxByOrNull { it.lastPlayedAt }
            }
        }

    override suspend fun getLastPosition(
        userId: String,
        entityType: ActivityEntityType,
        entityId: String,
    ): Long? {
        val id = "$userId-${entityType.name}-$entityId"
        return items[id]?.takeIf { !it.completed }?.positionMs
    }

    override suspend fun recordPlaybackPosition(item: UserActivityItem) {
        if (!preferences.value.isHistoryEnabled) return
        val existing = items[item.id]
        val progress = if (item.durationMs > 0) (item.positionMs.toFloat() / item.durationMs.toFloat()) * 100f else 0f
        val isCompleted = progress >= 90f || item.completed

        val updated =
            item.copy(
                progressPercent = progress.coerceIn(0f, 100f),
                completed = isCompleted,
                isWatchLater = existing?.isWatchLater ?: item.isWatchLater,
                isDownloaded = existing?.isDownloaded ?: item.isDownloaded,
                lastPlayedAt = System.currentTimeMillis(),
            )
        items[item.id] = updated
    }

    override suspend fun markAsCompleted(
        userId: String,
        entityType: ActivityEntityType,
        entityId: String,
    ) {
        val id = "$userId-${entityType.name}-$entityId"
        items[id]?.let {
            items[id] = it.copy(completed = true, progressPercent = 100f, positionMs = it.durationMs)
        }
    }

    override suspend fun toggleWatchLater(
        userId: String,
        item: UserActivityItem,
    ) {
        val existing = items[item.id]
        if (existing != null) {
            items[item.id] = existing.copy(isWatchLater = !existing.isWatchLater)
        } else {
            items[item.id] = item.copy(isWatchLater = true)
        }
    }

    override suspend fun toggleDownloaded(
        userId: String,
        item: UserActivityItem,
    ) {
        val existing = items[item.id]
        if (existing != null) {
            items[item.id] = existing.copy(isDownloaded = !existing.isDownloaded)
        } else {
            items[item.id] = item.copy(isDownloaded = true)
        }
    }

    override suspend fun deleteItem(
        userId: String,
        id: String,
    ) {
        items.remove(id)
    }

    override suspend fun clearAllHistory(userId: String) {
        val toRemove = items.filter { it.value.userId == userId }.keys
        toRemove.forEach { items.remove(it) }
    }

    override suspend fun clearCompleted(userId: String) {
        val toRemove = items.filter { it.value.userId == userId && it.value.completed }.keys
        toRemove.forEach { items.remove(it) }
    }

    override suspend fun clearDownloads(userId: String) {
        val toRemove = items.filter { it.value.userId == userId && it.value.isDownloaded }.keys
        toRemove.forEach { items.remove(it) }
    }

    override fun observePreferences(userId: String): Flow<ActivityHistoryPreferences> = preferences

    override suspend fun updatePreferences(
        userId: String,
        preferences: ActivityHistoryPreferences,
    ) {
        this.preferences.value = preferences
    }

    override suspend fun syncPending(userId: String) {
        // No-op in fake
    }

    override suspend fun applyRetentionPolicy(userId: String) {
        val policy = preferences.value.retentionPolicy
        val cutoffDays = policy.days ?: return
        val cutoffTime = System.currentTimeMillis() - (cutoffDays * 24L * 60L * 60L * 1000L)
        val oldKeys = items.filter { it.value.userId == userId && it.value.lastPlayedAt < cutoffTime }.keys
        oldKeys.forEach { items.remove(it) }
    }

    override suspend fun purgeUserAllData(userId: String) {
        val keys = items.filter { it.value.userId == userId }.keys
        keys.forEach { items.remove(it) }
    }
}

class ActivityHistoryTest {
    private lateinit var repo: FakeActivityHistoryRepository
    private val testUser = "test_user_123"

    @Before
    fun setUp() {
        repo = FakeActivityHistoryRepository()
    }

    @Test
    fun testRecordPlaybackPosition_calculatesProgressAndCompletion() =
        runBlocking {
            val item =
                UserActivityItem(
                    id = "$testUser-VIDEO-vid_001",
                    userId = testUser,
                    entityType = ActivityEntityType.VIDEO,
                    entityId = "vid_001",
                    title = "قصة أصحاب الكهف",
                    positionMs = 50000L, // 50s
                    durationMs = 100000L, // 100s -> 50%
                )

            repo.recordPlaybackPosition(item)

            val saved = repo.items[item.id]
            assertNotNull(saved)
            assertEquals(50f, saved!!.progressPercent, 0.1f)
            assertFalse(saved.completed)

            // Near completion (>90%)
            val itemNearEnd = item.copy(positionMs = 95000L)
            repo.recordPlaybackPosition(itemNearEnd)
            val savedNearEnd = repo.items[item.id]
            assertNotNull(savedNearEnd)
            assertEquals(95f, savedNearEnd!!.progressPercent, 0.1f)
            assertTrue(savedNearEnd.completed)
        }

    @Test
    fun testDisabledHistory_doesNotSaveProgress() =
        runBlocking {
            repo.updatePreferences(testUser, ActivityHistoryPreferences(isHistoryEnabled = false))

            val item =
                UserActivityItem(
                    id = "$testUser-AUDIO-aud_001",
                    userId = testUser,
                    entityType = ActivityEntityType.AUDIO,
                    entityId = "aud_001",
                    title = "سورة الرحمن",
                    positionMs = 30000L,
                    durationMs = 60000L,
                )

            repo.recordPlaybackPosition(item)

            val saved = repo.items[item.id]
            assertNull(saved)
        }

    @Test
    fun testWatchLaterToggle() =
        runBlocking {
            val item =
                UserActivityItem(
                    id = "$testUser-VIDEO-vid_002",
                    userId = testUser,
                    entityType = ActivityEntityType.VIDEO,
                    entityId = "vid_002",
                    title = "تفسير سورة النور",
                    positionMs = 0L,
                    durationMs = 120000L,
                )

            repo.toggleWatchLater(testUser, item)
            assertTrue(repo.items[item.id]?.isWatchLater == true)

            repo.toggleWatchLater(testUser, repo.items[item.id]!!)
            assertFalse(repo.items[item.id]?.isWatchLater == true)
        }

    @Test
    fun testClearCompletedAndClearAll() =
        runBlocking {
            val item1 =
                UserActivityItem(
                    id = "$testUser-VIDEO-1",
                    userId = testUser,
                    entityType = ActivityEntityType.VIDEO,
                    entityId = "1",
                    title = "فيديو 1",
                    positionMs = 100000L,
                    durationMs = 100000L,
                    completed = true,
                )
            val item2 =
                UserActivityItem(
                    id = "$testUser-VIDEO-2",
                    userId = testUser,
                    entityType = ActivityEntityType.VIDEO,
                    entityId = "2",
                    title = "فيديو 2",
                    positionMs = 20000L,
                    durationMs = 100000L,
                    completed = false,
                )

            repo.items[item1.id] = item1
            repo.items[item2.id] = item2

            assertEquals(2, repo.items.size)

            repo.clearCompleted(testUser)
            assertEquals(1, repo.items.size)
            assertNull(repo.items[item1.id])
            assertNotNull(repo.items[item2.id])

            repo.clearAllHistory(testUser)
            assertEquals(0, repo.items.size)
        }

    @Test
    fun testFormattingHelpers() {
        val item =
            UserActivityItem(
                id = "test-1",
                userId = "user",
                entityType = ActivityEntityType.VIDEO,
                entityId = "1",
                title = "اختبار",
                positionMs = 125000L, // 2m 5s
                durationMs = 360000L, // 6m 0s
            )

        assertEquals("02:05", item.getFormattedPosition())
        assertEquals("06:00", item.getFormattedDuration())
        assertTrue(item.getRemainingTimeText().contains("تبقى"))
    }
}
