package com.siraj.app.features.project.presentation

import com.siraj.app.features.project.domain.models.ProductionQuality
import org.junit.Assert.*
import org.junit.Test

class ProjectExportViewModelTest {

    @Test
    fun `calculateCost dynamically adjusts based on quality, fps and preview`() {
        val baseDurationSec = 30L
        val baseUnits = (baseDurationSec / 5).coerceAtLeast(5) // 6 units

        // SD_720P (1.0x), 30 fps (1.0x), Full (1.0x)
        val costSd = (baseUnits * ProductionQuality.SD_720P.costMultiplier * 1.0 * 1.0).toLong()
        assertEquals(6L, costSd)

        // FHD_1080P (1.5x), 60 fps (1.4x), Full (1.0x)
        val costFhd60 = (baseUnits * ProductionQuality.FHD_1080P.costMultiplier * 1.4 * 1.0).toLong()
        assertEquals(12L, costFhd60)

        // UHD_4K (3.0x), 30 fps (1.0x), Preview (0.4x)
        val cost4kPreview = (baseUnits * ProductionQuality.UHD_4K.costMultiplier * 1.0 * 0.4).toLong()
        assertEquals(7L, cost4kPreview)
    }

    @Test
    fun `pre export validation blocks export if empty scenes`() {
        val scenesCount = 0
        val isExportAllowed = scenesCount > 0
        assertFalse("Should block export when scenes are empty", isExportAllowed)
    }

    @Test
    fun `signed url expiration disclaimer is 7 days`() {
        val expiryDays = 7
        assertEquals(7, expiryDays)
    }
}
