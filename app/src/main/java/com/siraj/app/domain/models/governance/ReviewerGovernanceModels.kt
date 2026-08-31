package com.siraj.app.domain.models.governance

import com.siraj.app.domain.models.review.CriticalTopic
import com.siraj.app.domain.models.review.RiskLevel
import java.util.UUID

/**
 * المجالات الشرعية للاختصاص
 */
enum class ReviewerDomain(val arabicTitle: String, val code: String) {
    QURAN("القرآن وعلومه", "Quran"),
    HADITH("الحديث الشريف وعلومه", "Hadith"),
    TAFSIR("التفسير وعلوم القرآن", "Tafsir"),
    FIQH("الفقه وأصوله", "Fiqh"),
    AQEEDAH("العقيدة والتوحيد", "Aqeedah"),
    SEERAH("السيرة النبوية والتاريخ", "Seerah"),
    EDUCATION("التربية والتعليم الإسلامي", "Education"),
    GENERAL("دعوي وتربوي عام", "General")
}

/**
 * حالة اعتماد المراجع في المنظومة
 */
enum class ReviewerStatus(val arabicTitle: String) {
    ACTIVE("نشط ومعتمد رسمياً"),
    SUSPENDED("موقوف مؤقتاً"),
    PENDING_VERIFICATION("قيد التحقق والاعتماد من المالك")
}

/**
 * المؤهل الأكاديمي والشرعي للمراجع
 */
data class ReviewerQualification(
    val id: String = UUID.randomUUID().toString(),
    val degreeTitle: String, // e.g. "دكتوراه في الفقه المقارن", "إجازة مسندة برواية حفص"
    val institution: String, // e.g. "جامعة الأزهر", "الجامعة الإسلامية بالمدينة المنورة"
    val graduationYear: Int,
    val documentUrl: String? = null,
    val verifiedByOwnerId: String? = null,
    val verifiedAt: Long? = null,
    val isVerified: Boolean = false,
    val isPubliclyVisible: Boolean = false // لا تنشر تفاصيل المؤهل للعامة دون موافقة صريحة
)

/**
 * نطاق صلاحيات ومجالات اختصاص المراجع
 */
data class ReviewerScope(
    val allowedDomains: Set<ReviewerDomain> = setOf(ReviewerDomain.GENERAL),
    val excludedTopics: Set<CriticalTopic> = emptySet(), // الموضوعات التي يعتذر أو يمنع من مراجعتها
    val maxRiskLevelAllowed: RiskLevel = RiskLevel.MEDIUM,
    val canBePrimaryReviewer: Boolean = true,
    val canBeSecondReviewer: Boolean = false,
    val dailyReviewQuota: Int = 10
)

/**
 * أنواع تعارض المصالح
 */
enum class ConflictType(val arabicTitle: String) {
    OWN_CONTENT("المحتوى من إنشاء المراجع نفسه (محظور قطعاً)"),
    ORGANIZATIONAL("انتماء لنفس المؤسسة أو الجهة المنتجة للمحتوى"),
    PERSONAL_AFFILIATION("صلة قرابة أو مصلحة مالية مع صانع المحتوى"),
    PREVIOUS_DISPUTE("خصومة فكرية أو نزاع شخصي مسجل"),
    NONE("لا يوجد تعارض مصالح")
}

/**
 * سجل تعارض المصالح
 */
data class ReviewerConflict(
    val id: String = UUID.randomUUID().toString(),
    val reviewerId: String,
    val creatorId: String,
    val projectId: String? = null,
    val conflictType: ConflictType,
    val reason: String,
    val isRestricted: Boolean = true,
    val reportedAt: Long = System.currentTimeMillis()
)

/**
 * نتيجة قرار المراجعة
 */
enum class DecisionOutcome(val arabicTitle: String) {
    APPROVED("اعتماد شرعي"),
    REJECTED("رفض شرعي لتعارض مع الأصول"),
    CHANGES_REQUESTED("طلب تصحيح أو توثيق إضافي"),
    ESCALATED_SECOND_REVIEW("إحالة لمراجع ثانٍ للاختصاص أو الخطورة")
}

/**
 * القرار الشرعي (سجل غير قابل للتعديل أو الحذف)
 */
data class ReviewerDecision(
    val decisionId: String = UUID.randomUUID().toString(),
    val assignmentId: String,
    val itemId: String,
    val contentVersion: Int, // رقم النسخة الدقيق الذي خضع للمراجعة
    val reviewerId: String,
    val reviewerName: String,
    val reviewerRole: String, // "PRIMARY" أو "SECOND"
    val outcome: DecisionOutcome,
    val notes: String,
    val shariaEvidencesUsed: List<String> = emptyList(),
    val correctionSummary: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isImmutable: Boolean = true, // سجل ثابت
    val supersedesDecisionId: String? = null // في حال التصحيح ينشأ سجل جديد يشير للقديم
)

/**
 * حالة التعيين
 */
enum class AssignmentStatus(val arabicTitle: String) {
    ASSIGNED("معين للمراجعة"),
    IN_PROGRESS("قيد التدقيق الفاحص"),
    COMPLETED("مكتمل بقرار شرعي"),
    EXPIRED("منتهي الصلاحية الزمنية"),
    REASSIGNED("معاد التعيين لمراجع آخر"),
    CANCELLED("ملغى")
}

/**
 * تعيين مراجع لمحتوى معين
 */
data class ReviewerAssignment(
    val id: String = UUID.randomUUID().toString(),
    val itemId: String,
    val contentTitle: String,
    val contentVersion: Int,
    val domain: ReviewerDomain,
    val riskLevel: RiskLevel,
    val criticalTopic: CriticalTopic = CriticalTopic.NONE,
    val primaryReviewerId: String,
    val primaryReviewerName: String,
    val secondReviewerId: String? = null,
    val secondReviewerName: String? = null,
    val assignedByOwnerId: String,
    val assignedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (48 * 3600 * 1000L), // مهلة 48 ساعة افتراضية
    val status: AssignmentStatus = AssignmentStatus.ASSIGNED,
    val isSecondReviewRequired: Boolean = false,
    val primaryDecisionId: String? = null,
    val secondDecisionId: String? = null
)

/**
 * الملف الشخصي للمراجع الشرعي
 */
data class ReviewerProfile(
    val id: String,
    val displayName: String,
    val email: String,
    val organization: String,
    val qualifications: List<ReviewerQualification> = emptyList(),
    val specialties: Set<ReviewerDomain> = emptySet(),
    val languages: List<String> = listOf("ar"),
    val scope: ReviewerScope = ReviewerScope(),
    val status: ReviewerStatus = ReviewerStatus.PENDING_VERIFICATION,
    val verifiedByOwnerId: String? = null,
    val verificationDate: Long? = null,
    val nextReverificationDue: Long? = null, // موعد إعادة التحقق الدوري (كل سنة مثلاً)
    val totalReviewsCompleted: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
