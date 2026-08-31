package com.siraj.app.data.repository.flash

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.flash.*
import com.siraj.app.domain.repository.flash.FlashRepository
import kotlinx.coroutines.delay

class FirebaseFlashRepositoryImpl : FlashRepository {
    // Using memory list to mock data for MVP, showing only APPROVED flashes.
    private val mockFlashes =
        listOf(
            Flash(
                id = "flash_1",
                creatorId = "creator_1",
                creatorName = "الشيخ محمد",
                workspaceId = "workspace_1",
                videoAssetId = "vid_1",
                videoUrl = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
                title = "أهمية الصبر",
                description = "ومضة سريعة حول أهمية الصبر في الإسلام.",
                category = "موعظة",
                sourceInfo =
                    FlashSourceInfo(
                        "src_1",
                        "رياض الصالحين - باب الصبر",
                        FlashPublishingState.APPROVED,
                        System.currentTimeMillis() - 86400000,
                    ),
                publishingState = FlashPublishingState.APPROVED,
                durationMs = 15000,
                thumbnailUrl = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.png",
                visibility = FlashVisibility.PUBLIC,
            ),
            Flash(
                id = "flash_2",
                creatorId = "creator_2",
                creatorName = "قناة اقرأ",
                workspaceId = "workspace_2",
                videoAssetId = "vid_2",
                videoUrl = "https://storage.googleapis.com/exoplayer-test-media-0/Jazz_In_Paris.mp4",
                title = "أذكار الصباح",
                description = "فضل المحافظة على أذكار الصباح والمساء.",
                category = "أذكار",
                sourceInfo = FlashSourceInfo("src_2", "حصن المسلم", FlashPublishingState.APPROVED, System.currentTimeMillis() - 172800000),
                publishingState = FlashPublishingState.APPROVED,
                durationMs = 25000,
                thumbnailUrl = null,
                visibility = FlashVisibility.PUBLIC,
            ),
            Flash(
                id = "flash_3",
                creatorId = "creator_1",
                creatorName = "الشيخ محمد",
                workspaceId = "workspace_1",
                videoAssetId = "vid_3",
                videoUrl = "https://storage.googleapis.com/exoplayer-test-media-1/mkv/android-screens-lavf-56.36.100-aac-avc-main-1280x720.mkv",
                title = "الصدقة الجارية",
                description = "أبواب الصدقة الجارية وأثرها بعد الموت.",
                category = "أحكام",
                sourceInfo = FlashSourceInfo("src_3", "صحيح مسلم", FlashPublishingState.APPROVED, System.currentTimeMillis() - 10000000),
                publishingState = FlashPublishingState.APPROVED,
                durationMs = 30000,
                thumbnailUrl = null,
                visibility = FlashVisibility.PUBLIC,
            ),
        )

    override suspend fun getFlashesFeed(
        pageToken: String?,
        limit: Int,
    ): Resource<FlashesFeedResult> {
        delay(800) // Simulate network
        // Only return APPROVED flashes
        val approvedFlashes = mockFlashes.filter { it.publishingState == FlashPublishingState.APPROVED }

        // Simple pagination logic for mock data
        val startIndex = pageToken?.toIntOrNull() ?: 0
        val endIndex = minOf(startIndex + limit, approvedFlashes.size)

        if (startIndex >= approvedFlashes.size) {
            return Resource.Success(FlashesFeedResult(emptyList(), null, false))
        }

        val paginatedList = approvedFlashes.subList(startIndex, endIndex)
        val hasMore = endIndex < approvedFlashes.size
        val nextToken = if (hasMore) endIndex.toString() else null

        return Resource.Success(FlashesFeedResult(paginatedList, nextToken, hasMore))
    }

    override suspend fun toggleLike(flashId: String): Resource<Boolean> {
        delay(200)
        return Resource.Success(true)
    }

    override suspend fun toggleSave(flashId: String): Resource<Boolean> {
        delay(200)
        return Resource.Success(true)
    }

    override suspend fun logView(flashId: String) {
        // Mock logging
    }

    override suspend fun reportFlash(
        flashId: String,
        reason: String,
    ): Resource<Unit> {
        delay(500)
        return Resource.Success(Unit)
    }

    override suspend fun followCreator(creatorId: String): Resource<Unit> {
        delay(500)
        return Resource.Success(Unit)
    }
}
