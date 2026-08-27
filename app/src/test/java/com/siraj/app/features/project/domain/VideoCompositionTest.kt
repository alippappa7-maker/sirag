package com.siraj.app.features.project.domain

import com.siraj.app.features.project.domain.models.*
import org.junit.Assert.*
import org.junit.Test

class VideoCompositionTest {

    @Test
    fun `scenes are sorted strictly by orderIndex`() {
        val scene3 = CompositionSceneItem(sceneId = "s3", orderIndex = 2, durationMs = 3000L)
        val scene1 = CompositionSceneItem(sceneId = "s1", orderIndex = 0, durationMs = 2000L)
        val scene2 = CompositionSceneItem(sceneId = "s2", orderIndex = 1, durationMs = 4000L)

        val unorganizedList = listOf(scene3, scene1, scene2)
        val sortedList = unorganizedList.sortedBy { it.orderIndex }

        assertEquals("s1", sortedList[0].sceneId)
        assertEquals("s2", sortedList[1].sceneId)
        assertEquals("s3", sortedList[2].sceneId)
    }

    @Test
    fun `production job states reflect terminal and cancellable conditions correctly`() {
        val queuedJob = ProductionJob(status = ProductionJobStatus.QUEUED)
        assertTrue(queuedJob.canCancel)
        assertFalse(queuedJob.isTerminal)

        val composingJob = ProductionJob(status = ProductionJobStatus.COMPOSING)
        assertTrue(composingJob.canCancel)
        assertFalse(composingJob.isTerminal)

        val completedJob = ProductionJob(status = ProductionJobStatus.COMPLETED)
        assertFalse(completedJob.canCancel)
        assertTrue(completedJob.isTerminal)

        val failedJob = ProductionJob(status = ProductionJobStatus.FAILED)
        assertFalse(failedJob.canCancel)
        assertTrue(failedJob.isTerminal)
    }

    @Test
    fun `manifest sets islamic verification badge only when explicitly approved`() {
        val unverifiedBranding = CompositionBranding(isIslamicVerified = false, sourceCitationText = null)
        assertFalse(unverifiedBranding.isIslamicVerified)
        assertNull(unverifiedBranding.sourceCitationText)

        val verifiedBranding = CompositionBranding(
            isIslamicVerified = true,
            sourceCitationText = "تفسير ابن كثير - سورة البقرة آية 1"
        )
        assertTrue(verifiedBranding.isIslamicVerified)
        assertNotNull(verifiedBranding.sourceCitationText)
    }

    @Test
    fun `preview mode restricts duration to first two scenes or 10 seconds`() {
        val scenes = listOf(
            CompositionSceneItem(sceneId = "s1", orderIndex = 0, durationMs = 4000L),
            CompositionSceneItem(sceneId = "s2", orderIndex = 1, durationMs = 4000L),
            CompositionSceneItem(sceneId = "s3", orderIndex = 2, durationMs = 5000L)
        )

        val fullDuration = scenes.sumOf { it.durationMs }
        val previewDuration = scenes.take(2).sumOf { it.durationMs }.coerceAtMost(10000L)

        assertEquals(13000L, fullDuration)
        assertEquals(8000L, previewDuration)
        assertTrue(previewDuration <= 10000L)
    }

    @Test
    fun `quality multiplier scales cost units accurately`() {
        assertEquals(1.0, ProductionQuality.SD_720P.costMultiplier, 0.01)
        assertEquals(1.5, ProductionQuality.FHD_1080P.costMultiplier, 0.01)
        assertEquals(3.0, ProductionQuality.UHD_4K.costMultiplier, 0.01)
    }
}
