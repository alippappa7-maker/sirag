package com.siraj.app.domain.models.community

import java.util.UUID

enum class UgcState(val titleArabic: String) {
    UPLOADED("تم الرفع"),
    SCANNING("قيد الفحص الآلي"),
    PENDING_REVIEW("قيد المراجعة"),
    APPROVED("معتمد"),
    LIMITED("محدود الظهور"),
    REJECTED("مرفوض"),
    SUSPENDED("موقوف"),
    REMOVED("محذوف"),
    APPEALED("قيد الاستئناف"),
    RESTORED("مستعاد")
}

enum class ReportType(val titleArabic: String) {
    MISINFORMATION("معلومات مضللة"),
    RELIGIOUS_ERROR("خطأ شرعي أو تحريف"),
    COPYRIGHT("انتهاك حقوق النشر والتراخيص"),
    HARASSMENT("مضايقة أو إساءة أو تحريض"),
    SPAM("محتوى تكراري أو احتيالي (Spam)"),
    IMPERSONATION("انتحال شخصية أو جهة"),
    INAPPROPRIATE("محتوى غير لائق أخلاقياً"),
    PRIVACY("انتهاك الخصوصية وبيانات شخصية"),
    OTHER("أخرى")
}

enum class ReportStatus {
    PENDING,
    IN_REVIEW,
    RESOLVED,
    DISMISSED
}

enum class ReportTargetType {
    FLASH, PROJECT, AUDIO, SOURCE, USER
}

data class Report(
    val id: String = UUID.randomUUID().toString(),
    val reporterId: String, // Cannot be seen by the reported user
    val targetType: ReportTargetType,
    val targetId: String,
    val targetOwnerId: String, // The user being reported
    val reportType: ReportType,
    val description: String,
    val status: ReportStatus = ReportStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val slaDeadlineMs: Long = createdAt + (24 * 3600 * 1000L), // 24 hours SLA target
    val resolvedAt: Long? = null,
    val resolverId: String? = null,
    val resolutionNotes: String? = null
) {
    val isOverdue: Boolean
        get() = status == ReportStatus.PENDING && System.currentTimeMillis() > slaDeadlineMs

    val remainingHours: Long
        get() {
            val remaining = (slaDeadlineMs - System.currentTimeMillis()) / (1000 * 3600)
            return if (remaining < 0) 0 else remaining
        }
}

enum class AppealStatus(val titleArabic: String) {
    PENDING("قيد المراجعة"),
    APPROVED("تم قبول الاستئناف واستعادة المحتوى"),
    REJECTED("تم رفض الاستئناف وتأييد القرار")
}

data class UgcAppeal(
    val id: String = UUID.randomUUID().toString(),
    val ugcId: String,
    val ugcTitle: String,
    val userId: String,
    val originalReason: String,
    val appealJustification: String,
    val status: AppealStatus = AppealStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null,
    val resolverId: String? = null,
    val resolverNotes: String? = null
)

data class PreUploadScanResult(
    val isSpam: Boolean = false,
    val spamScore: Float = 0.0f,
    val hasHarmfulContent: Boolean = false,
    val harmfulDetails: String? = null,
    val hasCopyrightIssue: Boolean = false,
    val copyrightDetails: String? = null,
    val hasReligiousSensitivity: Boolean = false,
    val requiresHumanReview: Boolean = false,
    val isImpersonation: Boolean = false,
    val passedAutoFilter: Boolean = true,
    val detectedFlags: List<String> = emptyList(),
    val recommendedState: UgcState = UgcState.PENDING_REVIEW
)

data class TermsOfServiceConsent(
    val userId: String,
    val termsVersion: String = "1.2.0",
    val acceptedAt: Long = System.currentTimeMillis()
)

data class UgcItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val creatorId: String,
    val creatorName: String,
    val mediaType: String = "VIDEO", // VIDEO, AUDIO, TEXT, IMAGE
    val mediaUrl: String? = null,
    val state: UgcState = UgcState.UPLOADED,
    val scanResult: PreUploadScanResult? = null,
    val rejectionReason: String? = null,
    val reportCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val targetSlaDeadlineMs: Long = createdAt + (24 * 3600 * 1000L),
    val assignedReviewerRole: String = "REVIEWER" // REVIEWER for sharia, ADMIN for general safety/copyright
)

enum class ModeratorAction(val titleArabic: String) {
    APPROVE("اعتماد ونشر"),
    LIMIT("تقييد الظهور (محدود)"),
    REJECT("رفض المحتوى"),
    SUSPEND("تعليق المحتوى"),
    REMOVE("حذف نهائي"),
    RESTORE("استعادة المحتوى"),
    WARN_USER("توجيه إنذار للمستخدم"),
    SUSPEND_USER("إيقاف حساب المستخدم"),
    DISMISS_REPORT("حفظ البلاغ")
}

data class ModerationDecisionLog(
    val id: String = UUID.randomUUID().toString(),
    val targetId: String,
    val targetType: String = "UGC", // UGC, REPORT, APPEAL, USER
    val moderatorId: String,
    val action: String,
    val notes: String,
    val previousState: String? = null,
    val newState: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
