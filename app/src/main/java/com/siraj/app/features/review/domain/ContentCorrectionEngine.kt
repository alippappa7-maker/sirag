package com.siraj.app.features.review.domain

import com.siraj.app.domain.models.Source
import com.siraj.app.domain.models.correction.*
import com.siraj.app.domain.models.review.ShariaClaim
import com.siraj.app.domain.models.review.ShariaReviewStatus
import java.security.MessageDigest
import java.util.UUID

/**
 * محرك الحوكمة والتحقق لإصدارات وتصحيحات المحتوى الشرعي والفني
 */
object ContentCorrectionEngine {

    data class CorrectionDraftResult(
        val newVersion: ContentVersion,
        val correctionNotice: CorrectionNotice,
        val sourceRevisions: List<SourceRevision>,
        val affectedAssets: List<AffectedAsset>,
        val updatedPreviousVersion: ContentVersion
    )

    data class CorrectionApprovalResult(
        val newPublishedVersion: ContentVersion,
        val supersededPreviousVersion: ContentVersion,
        val approvedNotice: CorrectionNotice,
        val updatedAssets: List<AffectedAsset>
    )

    data class CorrectionRejectionResult(
        val rejectedDraftVersion: ContentVersion,
        val rejectedNotice: CorrectionNotice
    )

    /**
     * إنشاء مسودة تصحيح وإصدار جديد دون المساس بالنسخة القديمة إطلاقاً
     */
    fun createCorrectionDraft(
        currentVersion: ContentVersion,
        correctionType: CorrectionType,
        reason: String,
        detailedExplanation: String,
        discoveredBy: String,
        discoveredByType: DiscoveredByType,
        correctedTitle: String,
        correctedFullContentText: String,
        correctedClaims: List<ShariaClaim>,
        correctedSources: List<Source>,
        sourceRevisions: List<SourceRevision> = emptyList(),
        affectedAssets: List<AffectedAsset> = emptyList(),
        createdBy: String,
        createdByName: String,
        changeSummary: String,
        publicNoticeText: String = "",
        forceImmediateSuspension: Boolean = false
    ): CorrectionDraftResult {
        require(reason.isNotBlank()) { "يجب تدوين سبب التصحيح بوضوح" }
        require(detailedExplanation.isNotBlank()) { "يجب تقديم شرح تفصيلي وموثق لمسوغات التصحيح" }
        require(discoveredBy.isNotBlank()) { "يجب تسجيل هوية الشخص أو الجهة المكتشفة للخطأ" }

        val noticeId = UUID.randomUUID().toString()
        val nextVersionNumber = currentVersion.versionNumber + 1

        val shouldImmediatelySuspend = forceImmediateSuspension ||
                correctionType == CorrectionType.RIGHTS_ISSUE ||
                correctionType == CorrectionType.SAFETY_ISSUE

        // تحديث النسخة الحالية لتكون مقيدة فوراً في حال قضايا الحقوق أو السلامة
        val updatedPreviousVersion = if (shouldImmediatelySuspend) {
            currentVersion.copy(
                status = VersionStatus.RESTRICTED_SUSPENDED,
                isRestricted = true,
                restrictionReason = "تم تعليق النشر فورياً بسبب: ${correctionType.arabicTitle} - $reason"
            )
        } else {
            currentVersion
        }

        // إنشاء إشعار التصحيح
        val notice = CorrectionNotice(
            id = noticeId,
            contentId = currentVersion.contentId,
            fromVersionNumber = currentVersion.versionNumber,
            toVersionNumber = nextVersionNumber,
            correctionType = correctionType,
            reason = reason,
            detailedExplanation = detailedExplanation,
            discoveredBy = discoveredBy,
            discoveredByType = discoveredByType,
            reportedAt = System.currentTimeMillis(),
            requiresPublicNotice = true,
            isPubliclyVisible = true,
            publicNoticeText = if (publicNoticeText.isNotBlank()) publicNoticeText else "تنبيه تصحيحي: $reason",
            status = if (correctionType.requiresReviewer) ShariaReviewStatus.PENDING else ShariaReviewStatus.APPROVED,
            isImmediateSuspensionApplied = shouldImmediatelySuspend,
            notificationSent = false
        )

        // إرفاق معرف الإشعار بالمصادر المعدلة
        val boundSourceRevisions = sourceRevisions.map {
            it.copy(correctionNoticeId = noticeId)
        }

        // إرفاق معرف الإشعار بالأصول المتأثرة
        val boundAffectedAssets = affectedAssets.map {
            it.copy(
                contentId = currentVersion.contentId,
                correctionNoticeId = noticeId,
                status = if (shouldImmediatelySuspend) AssetImpactStatus.SUSPENDED else AssetImpactStatus.REQUIRES_RE_RENDER
            )
        }

        // إنشاء النسخة الجديدة كمسودة
        val draftVersion = ContentVersion(
            id = UUID.randomUUID().toString(),
            contentId = currentVersion.contentId,
            versionNumber = nextVersionNumber,
            title = correctedTitle,
            fullContentText = correctedFullContentText,
            claims = correctedClaims,
            sources = correctedSources,
            status = if (correctionType.requiresReviewer) VersionStatus.IN_REVIEW else VersionStatus.DRAFT,
            correctionNoticeId = noticeId,
            createdBy = createdBy,
            createdByName = createdByName,
            createdAt = System.currentTimeMillis(),
            publishedAt = null,
            supersededAt = null,
            supersededByVersion = null,
            immutableHash = computeHash(
                contentId = currentVersion.contentId,
                version = nextVersionNumber,
                text = correctedFullContentText,
                timestamp = System.currentTimeMillis()
            ),
            changeSummary = changeSummary,
            isRestricted = false,
            restrictionReason = null
        )

        return CorrectionDraftResult(
            newVersion = draftVersion,
            correctionNotice = notice,
            sourceRevisions = boundSourceRevisions,
            affectedAssets = boundAffectedAssets,
            updatedPreviousVersion = updatedPreviousVersion
        )
    }

    /**
     * اعتماد ونشر التصحيح بعد المراجعة الشرعية الفاحصة
     */
    fun approveAndPublishCorrection(
        notice: CorrectionNotice,
        draftVersion: ContentVersion,
        previousVersion: ContentVersion,
        review: CorrectionReview,
        affectedAssets: List<AffectedAsset>
    ): CorrectionApprovalResult {
        require(review.isApproved) { "لا يمكن نشر التصحيح دون اعتماد صريح من المراجع الشرعي" }
        require(review.reviewerId.isNotBlank()) { "يجب تسجيل معرف المراجع الشرعي المعتمد" }

        val now = System.currentTimeMillis()

        // 1. ترقية النسخة الجديدة إلى نسخة منشورة ومعتمدة
        val publishedVersion = draftVersion.copy(
            status = VersionStatus.ACTIVE_PUBLISHED,
            publishedAt = now,
            immutableHash = computeHash(
                contentId = draftVersion.contentId,
                version = draftVersion.versionNumber,
                text = draftVersion.fullContentText,
                timestamp = now
            )
        )

        // 2. تحديث النسخة السابقة لتصبح مستبدلة في السجل التاريخي الثابت
        val supersededVersion = previousVersion.copy(
            status = VersionStatus.SUPERSEDED,
            supersededAt = now,
            supersededByVersion = draftVersion.versionNumber,
            isRestricted = true,
            restrictionReason = "تم استبدال هذه النسخة بالنسخة رقم (${draftVersion.versionNumber}) بناءً على إشعار التصحيح: ${notice.reason}"
        )

        // 3. تحديث الإشعار
        val approvedNotice = notice.copy(
            status = ShariaReviewStatus.APPROVED,
            reviewerId = review.reviewerId,
            reviewerName = review.reviewerName,
            reviewedAt = now,
            notificationSent = notice.requiresPublicNotice,
            notificationSentAt = if (notice.requiresPublicNotice) now else null
        )

        // 4. تحديث الأصول المتأثرة
        val updatedAssets = affectedAssets.map { asset ->
            asset.copy(
                status = AssetImpactStatus.UPDATED,
                remediationAction = "تم تحديث الأصل ليتوافق مع الإصدار المصحح رقم (${draftVersion.versionNumber})",
                updatedAt = now
            )
        }

        return CorrectionApprovalResult(
            newPublishedVersion = publishedVersion,
            supersededPreviousVersion = supersededVersion,
            approvedNotice = approvedNotice,
            updatedAssets = updatedAssets
        )
    }

    /**
     * رفض التصحيح أو طلب استدراكات إضافية
     */
    fun rejectCorrection(
        notice: CorrectionNotice,
        draftVersion: ContentVersion,
        review: CorrectionReview
    ): CorrectionRejectionResult {
        require(!review.isApproved) { "حالة المراجعة تفيد بالاعتماد" }

        val rejectedNotice = notice.copy(
            status = review.status,
            reviewerId = review.reviewerId,
            reviewerName = review.reviewerName,
            reviewedAt = System.currentTimeMillis()
        )

        val rejectedDraft = draftVersion.copy(
            status = VersionStatus.DRAFT,
            changeSummary = "مرفوض شرعياً: ${review.reviewerNotes}"
        )

        return CorrectionRejectionResult(
            rejectedDraftVersion = rejectedDraft,
            rejectedNotice = rejectedNotice
        )
    }

    /**
     * توليد تقرير الأثر للمحتوى المتأثر بالتصحيح
     */
    fun generateImpactReport(
        notice: CorrectionNotice,
        affectedAssets: List<AffectedAsset>
    ): ImpactReport {
        val projects = affectedAssets.filter { it.assetType == AffectedAssetType.PROJECT }.distinctBy { it.projectId }
        val scenes = affectedAssets.filter { it.assetType == AffectedAssetType.SCENE }
        val renders = affectedAssets.filter { it.assetType == AffectedAssetType.VIDEO_RENDER }
        val flashes = affectedAssets.filter { it.assetType == AffectedAssetType.PUBLISHED_FLASH }

        val summary = "تقرير حصر الأثر لإشعار التصحيح (${notice.reason}): يشمل ${projects.size} مشاريع، و ${scenes.size} مشاهد سيناريو، و ${renders.size} ملفات فيديو، و ${flashes.size} ومضات منشورة."

        return ImpactReport(
            contentId = notice.contentId,
            correctionNoticeId = notice.id,
            fromVersion = notice.fromVersionNumber,
            toVersion = notice.toVersionNumber,
            totalAffectedAssetsCount = affectedAssets.size,
            affectedProjectsCount = projects.size,
            affectedScenesCount = scenes.size,
            affectedVideoRendersCount = renders.size,
            affectedPublishedFlashesCount = flashes.size,
            affectedAssets = affectedAssets,
            generatedAt = System.currentTimeMillis(),
            summary = summary
        )
    }

    /**
     * حساب بصمة النزاهة الرقمية الثابتة (SHA-256 Hash) لمنع التعديل السري
     */
    fun computeHash(contentId: String, version: Int, text: String, timestamp: Long): String {
        val raw = "$contentId:$version:$text:$timestamp"
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(raw.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
