package com.siraj.app.domain.models.analytics

enum class AnalyticsTimeFilter(val displayName: String) {
    LAST_7_DAYS("آخر 7 أيام"),
    LAST_30_DAYS("آخر 30 يوماً"),
    ALL_TIME("كل الوقت")
}

data class FlashAnalyticsSummary(
    val flashId: String = "",
    val title: String = "",
    val views: Long = 0,
    val estimatedUniqueViews: Long = 0,
    val completionRatePercentage: Float = 0f,
    val averageWatchTimeSeconds: Float = 0f,
    val saves: Long = 0,
    val shares: Long = 0,
    val likes: Long = 0,
    val trafficSources: Map<String, Float> = emptyMap(),
    val topCountries: Map<String, Float>? = null,
    val templateUsed: String? = null,
    val publishedAt: Long = 0
)

data class CreatorAnalyticsDashboard(
    val totalViews: Long = 0,
    val followerGrowth: Long = 0,
    val estimatedUniqueViews: Long = 0,
    val bestPostingTimes: List<String> = emptyList(),
    val topPerformingTemplates: Map<String, Long> = emptyMap(),
    val flashes: List<FlashAnalyticsSummary> = emptyList(),
    val timeFilter: AnalyticsTimeFilter = AnalyticsTimeFilter.LAST_30_DAYS
)
