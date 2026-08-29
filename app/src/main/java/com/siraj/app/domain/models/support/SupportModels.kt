package com.siraj.app.domain.models.support

import java.util.UUID

/**
 * Help Center and Support Models for Siraj Platform
 * Covering Knowledge Base, FAQs, Safe Diagnostics, and Support Tickets.
 */

enum class HelpCategory(val id: String, val titleAr: String, val descriptionAr: String) {
    PROJECTS_CREATION("projects", "المشاريع وصناعة الفيديو", "إنشاء المشاهد، الخطط، وتوليد المحتوى والوسائط"),
    SUBSCRIPTIONS_BILLING("billing", "الاشتراكات والفوترة والأرصدة", "الباقات، الأرصدة، مشاكل الدفع، واسترداد المبالغ"),
    SHARIA_REVIEW("sharia", "المراجعة والتدقيق الشرعي", "مسارات الاعتماد، تدقيق المصادر، والإبلاغ عن الأخطاء الشرعية"),
    SOURCES_REFERENCES("sources", "المصادر والمراجع والتحقق", "المكتبة، توثيق الأحاديث، وتخريج الروايات المعتمدة"),
    MIHRAB_QURAN("mihrab", "المحراب والقرآن الكريم", "المصحف المرتل، أوقات الصلاة، اتجاه القبلة، والأذكار"),
    EXPORT_RENDERING("export", "تصدير الفيديو والإنتاج", "جودة الفيديو، الترميز، تنزيل المقاطع، والعلامة المائية"),
    ACCOUNT_PRIVACY("privacy", "الحساب والخصوصية وحذف البيانات", "إدارة الحساب، تصدير البيانات، وحذف الحساب نهائياً"),
    TECHNICAL_ISSUES("technical", "المشكلات التقنية والأعطال", "الأخطاء البرمجية، اتصال الشبكة، ومزامنة البيانات")
}

data class HelpArticle(
    val id: String,
    val category: HelpCategory,
    val title: String,
    val summary: String,
    val content: String,
    val tags: List<String>,
    val readTimeMinutes: Int = 2,
    val helpfulVotes: Int = 0,
    val unhelpfulVotes: Int = 0,
    val relatedArticleIds: List<String> = emptyList(),
    val isFaq: Boolean = true
)

enum class TicketCategory(val titleAr: String, val defaultTeam: TicketTargetTeam) {
    SHARIA_CONTENT_ERROR("الإبلاغ عن خطأ في نص شرعي أو قرآني", TicketTargetTeam.SHARIA_REVIEWERS),
    PAYMENT_AND_BILLING("مشكلة في الدفع أو تجديد الاشتراك أو الرصيد", TicketTargetTeam.BILLING_SPECIALISTS),
    EXPORT_AND_RENDERING("مشكلة في تصدير الفيديو أو جودة المعالجة", TicketTargetTeam.TECHNICAL_ENGINEERING),
    ACCOUNT_AND_PRIVACY("طلب حذف الحساب أو تصدير البيانات الشخصية", TicketTargetTeam.PRIVACY_OFFICER),
    PROJECTS_AND_STUDIO("استفسار أو عطل في محرر المشاريع والمشاهد", TicketTargetTeam.TECHNICAL_ENGINEERING),
    MIHRAB_AND_QURAN("ملاحظات على المحراب أو التلاوات أو القبلة", TicketTargetTeam.SHARIA_REVIEWERS),
    TECHNICAL_BUG("عطل برمجى أو توقف مفاجئ في التطبيق", TicketTargetTeam.TECHNICAL_ENGINEERING),
    GENERAL_INQUIRY("استفسار عام أو اقتراح لتحسين المنصة", TicketTargetTeam.GENERAL_SUPPORT),
    APPEAL_AND_POLICY("اعتراض على قرار أو الإبلاغ عن مخالفة", TicketTargetTeam.GENERAL_SUPPORT),
    SOURCE_CORRECTION("تصحيح وتخريج مصدر أو مرجع", TicketTargetTeam.SHARIA_REVIEWERS)
}

enum class TicketStatus(val titleAr: String) {
    OPEN("مفتوحة"),
    WAITING_USER("بانتظار رد المستخدم"),
    IN_PROGRESS("قيد المعالجة"),
    RESOLVED("تم الحل"),
    CLOSED("مغلقة"),
    ESCALATED("مُصعدة للفريق الأعلى")
}

enum class TicketPriority(val titleAr: String) {
    LOW("منخفضة"),
    NORMAL("عادية"),
    HIGH("عالية"),
    URGENT("حرجة طارئة")
}

enum class TicketTargetTeam(val titleAr: String) {
    SHARIA_REVIEWERS("هيئة المراجعة والتدقيق الشرعي"),
    BILLING_SPECIALISTS("فريق الفوترة والاشتراكات المعتمد"),
    TECHNICAL_ENGINEERING("فريق الدعم الفني والهندسي"),
    PRIVACY_OFFICER("مسؤول الخصوصية وحماية البيانات"),
    GENERAL_SUPPORT("فريق خدمة العملاء العام")
}

enum class ReplyAuthorRole(val titleAr: String) {
    USER("المستخدم"),
    SUPPORT_AGENT("أخصائي الدعم الفني"),
    SHARIA_REVIEWER("المراجع الشرعي"),
    BILLING_ADMIN("مسؤول الفوترة"),
    SYSTEM("النظام الآلي")
}

data class TicketReply(
    val id: String = UUID.randomUUID().toString(),
    val ticketId: String,
    val authorId: String,
    val authorName: String,
    val authorRole: ReplyAuthorRole,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isInternalNote: Boolean = false
)

data class TicketRating(
    val stars: Int, // 1 to 5
    val feedback: String? = null,
    val ratedAt: Long = System.currentTimeMillis()
)

data class SafeDiagnosticsLog(
    val appVersion: String,
    val buildNumber: String,
    val osVersion: String,
    val deviceModel: String,
    val networkState: String,
    val memoryAvailableMb: Long,
    val sanitizedLogs: List<String>,
    val timestamp: Long = System.currentTimeMillis(),
    val containsNoSecretsVerified: Boolean = true
)

data class SupportTicket(
    val id: String = UUID.randomUUID().toString(),
    val ticketNumber: String, // e.g. SRJ-TKT-2026-8491
    val userId: String,
    val userEmail: String,
    val userName: String,
    val category: TicketCategory,
    val priority: TicketPriority = TicketPriority.NORMAL,
    val status: TicketStatus = TicketStatus.OPEN,
    val subject: String,
    val description: String,
    val targetTeam: TicketTargetTeam,
    val relatedProjectId: String? = null,
    val shariaSurahOrHadithRef: String? = null,
    val billingTransactionId: String? = null,
    val safeLogs: SafeDiagnosticsLog? = null,
    val replies: List<TicketReply> = emptyList(),
    val rating: TicketRating? = null,
    val isShariaReport: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null,
    val closedAt: Long? = null,
    val assignedAgentName: String? = null
)
