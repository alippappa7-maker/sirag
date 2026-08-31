package com.siraj.app.domain.models.correction

import com.siraj.app.domain.models.Source
import com.siraj.app.domain.models.review.ShariaClaim
import com.siraj.app.domain.models.review.ShariaReviewStatus
import java.util.UUID

/**
 * أنواع أسباب التصحيح الشرعي والفني
 */
enum class CorrectionType(
    val arabicTitle: String,
    val description: String,
    val requiresReviewer: Boolean,
) {
    SOURCE_ERROR(
        arabicTitle = "خطأ في المصدر أو التخريج",
        description = "خطأ في عزو الحديث، رقم الصفحة، اسم الكتاب، أو درجة صحة الرواية",
        requiresReviewer = true,
    ),
    WORDING_ERROR(
        arabicTitle = "خطأ في الصياغة أو اللفظ",
        description = "لحن، خطأ إملائي، أو سقط في كلمة تؤثر على المعنى الشرعي",
        requiresReviewer = true,
    ),
    ATTRIBUTION_ERROR(
        arabicTitle = "خطأ في نسبة القول أو العالم",
        description = "نسبة قول أو فتوى لغير قائلها من العلماء المعتبرين",
        requiresReviewer = true,
    ),
    RIGHTS_ISSUE(
        arabicTitle = "مسألة حقوق ملكية أو ترخيص",
        description = "انتهاء ترخيص صورة/صوت أو مطالبة من صاحب الحق الأصلي",
        requiresReviewer = false,
    ),
    TECHNICAL_ERROR(
        arabicTitle = "خطأ تقني أو مونتاجي",
        description = "خلل في تزامن الصوت مع النص، أو تقطيع في الفيديو",
        requiresReviewer = false,
    ),
    SAFETY_ISSUE(
        arabicTitle = "مسألة سلامة أو محتوى غير لائق",
        description = "محتوى يمس الضوابط الأخلاقية أو معايير السلامة العامة",
        requiresReviewer = false,
    ),
    OTHER(
        arabicTitle = "تصحيح آخر",
        description = "تحسينات أو استدراكات عامة أخرى",
        requiresReviewer = false,
    ),
}

/**
 * جهة اكتشاف المشكلة
 */
enum class DiscoveredByType(
    val arabicTitle: String,
) {
    USER_REPORT("بلاغ من مستخدم"),
    REVIEWER_AUDIT("تدقيق مراجع شرعي"),
    CREATOR_SELF_DISCOVERY("اكتشاف ذاتي من الصانع"),
    SYSTEM_SCAN("فحص آلي للنظام"),
}

/**
 * حالة الإصدار
 */
enum class VersionStatus(
    val arabicTitle: String,
) {
    DRAFT("مسودة تصحيح"),
    IN_REVIEW("قيد مراجعة التصحيح"),
    ACTIVE_PUBLISHED("منشور ومعتمد حالياً"),
    SUPERSEDED("مستبدل بنسخة أحدث"),
    RESTRICTED_SUSPENDED("موقوف ومقيد مؤقتاً"),
    ARCHIVED("مؤرشف في السجل الثابت"),
}

/**
 * حالة المواد والمقاطع المتأثرة
 */
enum class AssetImpactStatus(
    val arabicTitle: String,
) {
    REQUIRES_RE_RENDER("يتطلب إعادة رندرة"),
    SUSPENDED("موقوف مؤقتاً عن النشر"),
    UPDATED("تم التحديث بنجاح"),
    DEPRECATED("أصبح قديماً ومستبعداً"),
}

/**
 * نوع المادة المتأثرة
 */
enum class AffectedAssetType(
    val arabicTitle: String,
) {
    PROJECT("مشروع إنتاجي"),
    SCENE("مشهد سيناريو"),
    VIDEO_RENDER("فيديو مرندر"),
    AUDIO_TRACK("تسجيل صوتي"),
    SUBTITLE("نصوص وترجمة المشهد"),
    PUBLISHED_FLASH("ومضة منشورة في الموجز"),
}

/**
 * نموذج إصدار المحتوى (ContentVersion)
 * يمثل نسخة تاريخية ثابتة للمحتوى لا يتم تعديلها بصمت
 */
data class ContentVersion(
    val id: String = UUID.randomUUID().toString(),
    val contentId: String,
    val versionNumber: Int,
    val title: String,
    val fullContentText: String,
    val claims: List<ShariaClaim> = emptyList(),
    val sources: List<Source> = emptyList(),
    val status: VersionStatus = VersionStatus.ACTIVE_PUBLISHED,
    val correctionNoticeId: String? = null,
    val createdBy: String,
    val createdByName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val publishedAt: Long? = null,
    val supersededAt: Long? = null,
    val supersededByVersion: Int? = null,
    val immutableHash: String = "",
    val changeSummary: String = "",
    val isRestricted: Boolean = false,
    val restrictionReason: String? = null,
)

/**
 * إشعار التصحيح الشرعي والفني (CorrectionNotice)
 * يسجل السبب المفصل والجهة المكتشفة والإفصاح العام
 */
data class CorrectionNotice(
    val id: String = UUID.randomUUID().toString(),
    val contentId: String,
    val fromVersionNumber: Int,
    val toVersionNumber: Int,
    val correctionType: CorrectionType,
    val reason: String,
    val detailedExplanation: String,
    val discoveredBy: String,
    val discoveredByType: DiscoveredByType,
    val reportedAt: Long = System.currentTimeMillis(),
    val requiresPublicNotice: Boolean = true,
    val isPubliclyVisible: Boolean = true,
    val publicNoticeText: String = "",
    val status: ShariaReviewStatus = ShariaReviewStatus.PENDING,
    val reviewerId: String? = null,
    val reviewerName: String? = null,
    val reviewedAt: Long? = null,
    val isImmediateSuspensionApplied: Boolean = false,
    val notificationSent: Boolean = false,
    val notificationSentAt: Long? = null,
)

/**
 * تنقيح ومقارنة المصدر (SourceRevision)
 * مقارنة دقيقة بين المصدر الأصلي الخاطئ والمصدر المصحح
 */
data class SourceRevision(
    val id: String = UUID.randomUUID().toString(),
    val correctionNoticeId: String,
    val originalSourceId: String,
    val originalSourceTitle: String,
    val originalReference: String,
    val originalText: String,
    val originalGrade: String? = null,
    val originalScholar: String? = null,
    val correctedSourceTitle: String,
    val correctedReference: String,
    val correctedText: String,
    val correctedGrade: String? = null,
    val correctedScholar: String? = null,
    val correctionReason: String,
    val sourceUrl: String = "",
    val isVerified: Boolean = false,
    val verifiedBy: String? = null,
)

/**
 * الأصول والمشاريع المتأثرة بالتصحيح (AffectedAsset)
 */
data class AffectedAsset(
    val id: String = UUID.randomUUID().toString(),
    val contentId: String,
    val correctionNoticeId: String,
    val projectId: String,
    val projectTitle: String,
    val sceneId: String? = null,
    val sceneIndex: Int? = null,
    val assetType: AffectedAssetType,
    val assetName: String,
    val status: AssetImpactStatus = AssetImpactStatus.REQUIRES_RE_RENDER,
    val impactDescription: String,
    val remediationAction: String,
    val updatedAt: Long = System.currentTimeMillis(),
)

/**
 * مراجعة واعتماد التصحيح الشرعي (CorrectionReview)
 */
data class CorrectionReview(
    val id: String = UUID.randomUUID().toString(),
    val correctionNoticeId: String,
    val fromVersionNumber: Int,
    val toVersionNumber: Int,
    val reviewerId: String,
    val reviewerName: String,
    val reviewerSpecialty: String,
    val status: ShariaReviewStatus,
    val reviewerNotes: String,
    val shariaEvidences: List<String> = emptyList(),
    val reviewedAt: Long = System.currentTimeMillis(),
    val isApproved: Boolean = false,
)

/**
 * تقرير الأثر للمحتوى المتأثر بالتصحيح (ImpactReport)
 */
data class ImpactReport(
    val contentId: String,
    val correctionNoticeId: String,
    val fromVersion: Int,
    val toVersion: Int,
    val totalAffectedAssetsCount: Int,
    val affectedProjectsCount: Int,
    val affectedScenesCount: Int,
    val affectedVideoRendersCount: Int,
    val affectedPublishedFlashesCount: Int,
    val affectedAssets: List<AffectedAsset> = emptyList(),
    val generatedAt: Long = System.currentTimeMillis(),
    val summary: String,
)
