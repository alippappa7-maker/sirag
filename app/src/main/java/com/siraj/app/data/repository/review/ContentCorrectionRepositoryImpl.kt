package com.siraj.app.data.repository.review

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.Source
import com.siraj.app.domain.models.SourceType
import com.siraj.app.domain.models.SourceVerificationStatus
import com.siraj.app.domain.models.correction.*
import com.siraj.app.domain.models.review.ShariaClaim
import com.siraj.app.domain.repository.review.ContentCorrectionRepository
import com.siraj.app.features.review.domain.ContentCorrectionEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentHashMap

class ContentCorrectionRepositoryImpl : ContentCorrectionRepository {
    private val versionsMap = ConcurrentHashMap<String, MutableStateFlow<List<ContentVersion>>>()
    private val noticesMap = ConcurrentHashMap<String, MutableStateFlow<List<CorrectionNotice>>>()
    private val sourceRevisionsMap = ConcurrentHashMap<String, MutableStateFlow<List<SourceRevision>>>()
    private val affectedAssetsMap = ConcurrentHashMap<String, MutableStateFlow<List<AffectedAsset>>>()
    private val reviewsMap = ConcurrentHashMap<String, MutableStateFlow<List<CorrectionReview>>>()

    init {
        seedSampleData()
    }

    private fun seedSampleData() {
        val sampleContentId = "proj_siraj_101"

        // Initial Version 1
        val v1 =
            ContentVersion(
                id = "ver_101_v1",
                contentId = sampleContentId,
                versionNumber = 1,
                title = "فضل طلب العلم وسلوك طريقه إلى الجنة",
                fullContentText = "من سلك طريقاً يلتمس فيه علماً سهّل الله له به طريقاً إلى الجنة، وإن الملائكة لتضع أجنحتها رضاً لطالب العلم.",
                claims =
                    listOf(
                        ShariaClaim(
                            id = "claim_1",
                            claimText = "من سلك طريقاً يلتمس فيه علماً سهّل الله له به طريقاً إلى الجنة",
                            positionContext = "المشهد 1 (00:00 - 00:20)",
                            sourceType = "HADITH",
                            sourceTitle = "صحيح مسلم",
                            sourceReference = "كتاب الذكر والدعاء والتوبة، باب فضل الاجتماع على تلاوة القرآن، رقم 2699",
                            originalSourceText = "من سلك طريقا يلتمس فيه علما سهل الله له به طريقا إلى الجنة",
                            hadithGrade = "صحيح",
                            hadithNarrator = "أبو هريرة رضي الله عنه",
                            isVerified = true,
                        ),
                    ),
                sources =
                    listOf(
                        Source(
                            id = "src_1",
                            type = SourceType.HADITH,
                            title = "صحيح مسلم",
                            authorOrNarrator = "أبو هريرة رضي الله عنه",
                            originalText = "من سلك طريقا يلتمس فيه علما سهل الله له به طريقا إلى الجنة",
                            reference = "رقم 2699",
                            reviewStatus = SourceVerificationStatus.VERIFIED,
                        ),
                    ),
                status = VersionStatus.ACTIVE_PUBLISHED,
                createdBy = "creator_ahmed",
                createdByName = "أحمد المنصور",
                createdAt = System.currentTimeMillis() - 7 * 24 * 3600 * 1000L,
                publishedAt = System.currentTimeMillis() - 6 * 24 * 3600 * 1000L,
                immutableHash =
                    ContentCorrectionEngine.computeHash(
                        contentId = sampleContentId,
                        version = 1,
                        text = "من سلك طريقاً يلتمس فيه علماً سهّل الله له به طريقاً إلى الجنة",
                        timestamp = System.currentTimeMillis() - 7 * 24 * 3600 * 1000L,
                    ),
                changeSummary = "الإصدار التأسيسي الأول المعتمد",
            )

        val versionsList = MutableStateFlow(listOf(v1))
        versionsMap[sampleContentId] = versionsList
        noticesMap[sampleContentId] = MutableStateFlow(emptyList())
    }

    override fun getContentVersions(contentId: String): Flow<List<ContentVersion>> {
        val flow = versionsMap.getOrPut(contentId) { MutableStateFlow(emptyList()) }
        return flow.map { list -> list.sortedByDescending { it.versionNumber } }
    }

    override fun getVersionByNumber(
        contentId: String,
        versionNumber: Int,
    ): Flow<ContentVersion?> {
        val flow = versionsMap.getOrPut(contentId) { MutableStateFlow(emptyList()) }
        return flow.map { list -> list.find { it.versionNumber == versionNumber } }
    }

    override fun getCorrectionNotices(contentId: String): Flow<List<CorrectionNotice>> {
        val flow = noticesMap.getOrPut(contentId) { MutableStateFlow(emptyList()) }
        return flow.map { list -> list.sortedByDescending { it.reportedAt } }
    }

    override fun getCorrectionNoticeById(noticeId: String): Flow<CorrectionNotice?> {
        // Search all notices
        val allNoticesFlow = MutableStateFlow<CorrectionNotice?>(null)
        for ((_, flow) in noticesMap) {
            val found = flow.value.find { it.id == noticeId }
            if (found != null) {
                allNoticesFlow.value = found
                break
            }
        }
        return allNoticesFlow
    }

    override fun getSourceRevisions(noticeId: String): Flow<List<SourceRevision>> =
        sourceRevisionsMap.getOrPut(noticeId) {
            MutableStateFlow(emptyList())
        }

    override fun getAffectedAssets(noticeId: String): Flow<List<AffectedAsset>> =
        affectedAssetsMap.getOrPut(noticeId) {
            MutableStateFlow(emptyList())
        }

    override fun getCorrectionReviews(noticeId: String): Flow<List<CorrectionReview>> =
        reviewsMap.getOrPut(noticeId) {
            MutableStateFlow(emptyList())
        }

    override suspend fun createCorrection(draftResult: ContentCorrectionEngine.CorrectionDraftResult): Resource<String> =
        try {
            val contentId = draftResult.newVersion.contentId
            val noticeId = draftResult.correctionNotice.id

            // 1. Save or update versions list
            val versionsFlow = versionsMap.getOrPut(contentId) { MutableStateFlow(emptyList()) }
            val currentList = versionsFlow.value.toMutableList()

            // Update previous version if it was modified (e.g. suspended)
            val prevIndex = currentList.indexOfFirst { it.versionNumber == draftResult.updatedPreviousVersion.versionNumber }
            if (prevIndex >= 0) {
                currentList[prevIndex] = draftResult.updatedPreviousVersion
            }
            // Add draft new version
            currentList.add(draftResult.newVersion)
            versionsFlow.value = currentList

            // 2. Save Notice
            val noticesFlow = noticesMap.getOrPut(contentId) { MutableStateFlow(emptyList()) }
            noticesFlow.value = noticesFlow.value + draftResult.correctionNotice

            // 3. Save Source Revisions
            if (draftResult.sourceRevisions.isNotEmpty()) {
                val srcFlow = sourceRevisionsMap.getOrPut(noticeId) { MutableStateFlow(emptyList()) }
                srcFlow.value = draftResult.sourceRevisions
            }

            // 4. Save Affected Assets
            if (draftResult.affectedAssets.isNotEmpty()) {
                val assetFlow = affectedAssetsMap.getOrPut(noticeId) { MutableStateFlow(emptyList()) }
                assetFlow.value = draftResult.affectedAssets
            }

            Resource.Success(noticeId)
        } catch (e: Exception) {
            Resource.Error("فشل في إنشاء مسودة التصحيح: ${e.localizedMessage}")
        }

    override suspend fun submitReview(
        noticeId: String,
        review: CorrectionReview,
    ): Resource<Unit> {
        return try {
            // Find notice
            var foundContentId: String? = null
            var foundNotice: CorrectionNotice? = null
            for ((cId, flow) in noticesMap) {
                val item = flow.value.find { it.id == noticeId }
                if (item != null) {
                    foundContentId = cId
                    foundNotice = item
                    break
                }
            }

            if (foundContentId == null || foundNotice == null) {
                return Resource.Error("لم يتم العثور على إشعار التصحيح المحدد")
            }

            // Save review record
            val reviewsFlow = reviewsMap.getOrPut(noticeId) { MutableStateFlow(emptyList()) }
            reviewsFlow.value = reviewsFlow.value + review

            val versionsFlow = versionsMap.getOrPut(foundContentId) { MutableStateFlow(emptyList()) }
            val currentVersions = versionsFlow.value
            val draftVersion = currentVersions.find { it.versionNumber == foundNotice.toVersionNumber }
            val prevVersion = currentVersions.find { it.versionNumber == foundNotice.fromVersionNumber }
            val assetsFlow = affectedAssetsMap.getOrPut(noticeId) { MutableStateFlow(emptyList()) }

            if (draftVersion == null || prevVersion == null) {
                return Resource.Error("تعذر العثور على إصدارات المحتوى المرتبطة بالإشعار")
            }

            if (review.isApproved) {
                val approvalResult =
                    ContentCorrectionEngine.approveAndPublishCorrection(
                        notice = foundNotice,
                        draftVersion = draftVersion,
                        previousVersion = prevVersion,
                        review = review,
                        affectedAssets = assetsFlow.value,
                    )

                // Update versions
                val updatedVersionsList =
                    currentVersions.map { ver ->
                        when (ver.versionNumber) {
                            approvalResult.newPublishedVersion.versionNumber -> approvalResult.newPublishedVersion
                            approvalResult.supersededPreviousVersion.versionNumber -> approvalResult.supersededPreviousVersion
                            else -> ver
                        }
                    }
                versionsFlow.value = updatedVersionsList

                // Update notice
                val noticesFlow = noticesMap.getOrPut(foundContentId) { MutableStateFlow(emptyList()) }
                noticesFlow.value =
                    noticesFlow.value.map {
                        if (it.id == noticeId) approvalResult.approvedNotice else it
                    }

                // Update assets
                assetsFlow.value = approvalResult.updatedAssets
            } else {
                val rejectionResult =
                    ContentCorrectionEngine.rejectCorrection(
                        notice = foundNotice,
                        draftVersion = draftVersion,
                        review = review,
                    )

                // Update versions
                val updatedVersionsList =
                    currentVersions.map { ver ->
                        if (ver.versionNumber == rejectionResult.rejectedDraftVersion.versionNumber) {
                            rejectionResult.rejectedDraftVersion
                        } else {
                            ver
                        }
                    }
                versionsFlow.value = updatedVersionsList

                // Update notice
                val noticesFlow = noticesMap.getOrPut(foundContentId) { MutableStateFlow(emptyList()) }
                noticesFlow.value =
                    noticesFlow.value.map {
                        if (it.id == noticeId) rejectionResult.rejectedNotice else it
                    }
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("فشل في اعتماد أو رفض التصحيح: ${e.localizedMessage}")
        }
    }

    override suspend fun updateAssetStatus(
        assetId: String,
        newStatus: AssetImpactStatus,
    ): Resource<Unit> {
        return try {
            for ((_, flow) in affectedAssetsMap) {
                val list = flow.value
                val item = list.find { it.id == assetId }
                if (item != null) {
                    flow.value =
                        list.map {
                            if (it.id == assetId) it.copy(status = newStatus, updatedAt = System.currentTimeMillis()) else it
                        }
                    return Resource.Success(Unit)
                }
            }
            Resource.Error("لم يتم العثور على الأصل المطلوب")
        } catch (e: Exception) {
            Resource.Error("فشل في تحديث حالة الأصل: ${e.localizedMessage}")
        }
    }

    override suspend fun generateImpactReport(noticeId: String): Resource<ImpactReport> {
        return try {
            var foundNotice: CorrectionNotice? = null
            for ((_, flow) in noticesMap) {
                val item = flow.value.find { it.id == noticeId }
                if (item != null) {
                    foundNotice = item
                    break
                }
            }

            if (foundNotice == null) {
                return Resource.Error("لم يتم العثور على إشعار التصحيح")
            }

            val assetsFlow = affectedAssetsMap.getOrPut(noticeId) { MutableStateFlow(emptyList()) }
            val report = ContentCorrectionEngine.generateImpactReport(foundNotice, assetsFlow.value)
            Resource.Success(report)
        } catch (e: Exception) {
            Resource.Error("فشل في توليد تقرير الأثر: ${e.localizedMessage}")
        }
    }

    override suspend fun initializeDefaultVersion(version: ContentVersion): Resource<Unit> =
        try {
            val flow = versionsMap.getOrPut(version.contentId) { MutableStateFlow(emptyList()) }
            if (flow.value.none { it.versionNumber == version.versionNumber }) {
                flow.value = flow.value + version
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("فشل في تهيئة الإصدار: ${e.localizedMessage}")
        }
}
