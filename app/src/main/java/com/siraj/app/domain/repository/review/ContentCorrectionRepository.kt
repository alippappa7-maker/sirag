package com.siraj.app.domain.repository.review

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.correction.*
import com.siraj.app.features.review.domain.ContentCorrectionEngine
import kotlinx.coroutines.flow.Flow

interface ContentCorrectionRepository {
    fun getContentVersions(contentId: String): Flow<List<ContentVersion>>

    fun getVersionByNumber(
        contentId: String,
        versionNumber: Int,
    ): Flow<ContentVersion?>

    fun getCorrectionNotices(contentId: String): Flow<List<CorrectionNotice>>

    fun getCorrectionNoticeById(noticeId: String): Flow<CorrectionNotice?>

    fun getSourceRevisions(noticeId: String): Flow<List<SourceRevision>>

    fun getAffectedAssets(noticeId: String): Flow<List<AffectedAsset>>

    fun getCorrectionReviews(noticeId: String): Flow<List<CorrectionReview>>

    suspend fun createCorrection(draftResult: ContentCorrectionEngine.CorrectionDraftResult): Resource<String>

    suspend fun submitReview(
        noticeId: String,
        review: CorrectionReview,
    ): Resource<Unit>

    suspend fun updateAssetStatus(
        assetId: String,
        newStatus: AssetImpactStatus,
    ): Resource<Unit>

    suspend fun generateImpactReport(noticeId: String): Resource<ImpactReport>

    suspend fun initializeDefaultVersion(version: ContentVersion): Resource<Unit>
}
