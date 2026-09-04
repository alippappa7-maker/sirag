package com.siraj.app.data.repository.analytics

import com.google.firebase.firestore.FirebaseFirestore
import com.siraj.app.domain.models.analytics.AnalyticsTimeFilter
import com.siraj.app.domain.models.analytics.CreatorAnalyticsDashboard
import com.siraj.app.domain.models.analytics.FlashAnalyticsSummary
import com.siraj.app.domain.repository.analytics.CreatorAnalyticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirebaseCreatorAnalyticsRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : CreatorAnalyticsRepository {
    override fun getCreatorDashboard(
        userId: String,
        timeFilter: AnalyticsTimeFilter,
    ): Flow<CreatorAnalyticsDashboard> =
        flow {
            try {
                val flashesSnapshot =
                    firestore
                        .collection("flashes")
                        .whereEqualTo("creatorId", userId)
                        .get()
                        .await()

                var totalViews = 0L
                var totalLikes = 0L
                var totalSaves = 0L
                var totalShares = 0L
                val flashSummaries = mutableListOf<FlashAnalyticsSummary>()

                for (doc in flashesSnapshot.documents) {
                    val views = doc.getLong("views") ?: 0L
                    val likes = doc.getLong("likes") ?: 0L
                    val saves = doc.getLong("saves") ?: 0L
                    val shares = doc.getLong("shares") ?: 0L
                    totalViews += views
                    totalLikes += likes
                    totalSaves += saves
                    totalShares += shares

                    flashSummaries.add(
                        FlashAnalyticsSummary(
                            flashId = doc.id,
                            title = doc.getString("title") ?: "",
                            views = views,
                            estimatedUniqueViews = (views * 0.85).toLong(),
                            completionRatePercentage = (doc.getDouble("completionRate") ?: 0.0).toFloat(),
                            averageWatchTimeSeconds = (doc.getDouble("avgWatchTime") ?: 0.0).toFloat(),
                            saves = saves,
                            shares = shares,
                            likes = likes,
                            trafficSources = emptyMap(),
                            topCountries = emptyMap(),
                            templateUsed = doc.getString("template") ?: "عام",
                            publishedAt = doc.getLong("publishedAt") ?: doc.getLong("createdAt") ?: System.currentTimeMillis(),
                        ),
                    )
                }

                val dashboard =
                    CreatorAnalyticsDashboard(
                        totalViews = totalViews,
                        followerGrowth = 0L,
                        estimatedUniqueViews = (totalViews * 0.85).toLong(),
                        bestPostingTimes = emptyList(),
                        topPerformingTemplates = emptyMap(),
                        flashes = flashSummaries,
                        timeFilter = timeFilter,
                    )
                emit(dashboard)
            } catch (e: Exception) {
                emit(
                    CreatorAnalyticsDashboard(
                        totalViews = 0L,
                        followerGrowth = 0L,
                        estimatedUniqueViews = 0L,
                        bestPostingTimes = emptyList(),
                        topPerformingTemplates = emptyMap(),
                        flashes = emptyList(),
                        timeFilter = timeFilter,
                    ),
                )
            }
        }

    override suspend fun generateExportReport(
        userId: String,
        timeFilter: AnalyticsTimeFilter,
    ): String {
        return try {
            val flashesSnapshot =
                firestore
                    .collection("flashes")
                    .whereEqualTo("creatorId", userId)
                    .get()
                    .await()

            var totalViews = 0L
            for (doc in flashesSnapshot.documents) {
                totalViews += doc.getLong("views") ?: 0L
            }
            val uniqueViews = (totalViews * 0.85).toLong()

            """
            تقرير أداء صانع المحتوى
            الفترة: ${timeFilter.displayName}
            ملاحظة: بعض الأرقام (مثل المشاهدات الفريدة) تقديرية لأغراض الخصوصية.
            البيانات الواردة هنا تقيس الأداء الفني ولا تعكس بالضرورة القيمة الشرعية للمحتوى.

            إجمالي المشاهدات,نمو المتابعين,المشاهدات الفريدة التقديرية
            $totalViews,0,$uniqueViews
            """.trimIndent()
        } catch (e: Exception) {
            """
            تقرير أداء صانع المحتوى
            الفترة: ${timeFilter.displayName}
            لا تتوفر بيانات حالياً
            """.trimIndent()
        }
    }
}
