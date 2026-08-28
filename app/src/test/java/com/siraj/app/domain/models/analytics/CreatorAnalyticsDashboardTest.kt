package com.siraj.app.domain.models.analytics

import org.junit.Assert.assertEquals
import org.junit.Test

class CreatorAnalyticsDashboardTest {

    @Test
    fun `default values are set correctly`() {
        val dashboard = CreatorAnalyticsDashboard()
        assertEquals(0L, dashboard.totalViews)
        assertEquals(0L, dashboard.followerGrowth)
        assertEquals(0L, dashboard.estimatedUniqueViews)
        assertEquals(emptyList<String>(), dashboard.bestPostingTimes)
        assertEquals(emptyMap<String, Long>(), dashboard.topPerformingTemplates)
        assertEquals(emptyList<FlashAnalyticsSummary>(), dashboard.flashes)
        assertEquals(AnalyticsTimeFilter.LAST_30_DAYS, dashboard.timeFilter)
    }

    @Test
    fun `custom values are assigned correctly`() {
        val dashboard = CreatorAnalyticsDashboard(
            totalViews = 1500,
            timeFilter = AnalyticsTimeFilter.LAST_7_DAYS
        )
        assertEquals(1500L, dashboard.totalViews)
        assertEquals(AnalyticsTimeFilter.LAST_7_DAYS, dashboard.timeFilter)
    }
}
