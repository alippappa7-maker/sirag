package com.siraj.app.domain.repository.analytics

import com.siraj.app.domain.models.analytics.AnalyticsTimeFilter
import com.siraj.app.domain.models.analytics.CreatorAnalyticsDashboard
import kotlinx.coroutines.flow.Flow

interface CreatorAnalyticsRepository {
    fun getCreatorDashboard(
        userId: String,
        timeFilter: AnalyticsTimeFilter,
    ): Flow<CreatorAnalyticsDashboard>

    suspend fun generateExportReport(
        userId: String,
        timeFilter: AnalyticsTimeFilter,
    ): String
}
