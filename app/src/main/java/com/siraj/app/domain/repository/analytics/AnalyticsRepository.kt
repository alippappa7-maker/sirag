package com.siraj.app.domain.repository.analytics

import com.siraj.app.domain.models.analytics.AnalyticsEvent
import com.siraj.app.domain.models.analytics.AnalyticsLog
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {
    suspend fun logEvent(event: AnalyticsEvent, properties: Map<String, String> = emptyMap())
    suspend fun setAnalyticsEnabled(enabled: Boolean)
    suspend fun clearUserData()
    fun getAggregatedEvents(): Flow<List<AnalyticsLog>>
}
