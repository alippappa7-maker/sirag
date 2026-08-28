package com.siraj.app.domain.models.review

import java.util.UUID

enum class RiskLevel(val arabicTitle: String, val levelPriority: Int) {
    LOW("منخفض", 1),
    MEDIUM("متوسط", 2),
    HIGH("عالي", 3),
    CRITICAL("حرج / فائق الخطورة", 4)
}

enum class CriticalTopic(val arabicTitle: String, val description: String) {
    TAKFIER("التكفير", "مسائل إخراج المسلم من الملة وأحكام الردة"),
    BLOOD_VIOLENCE("الدماء والقتال", "مسائل الجهاد، الدماء، القصاص، والعنف"),
    DIVORCE("الطلاق والفرقة", "أحكام الطلاق، الفسخ، والخلع والأيمان المتعلقة بها"),
    FAMILY("شؤون الأسرة والنسب", "أحكام النسب، الحضانة، المواريث، والأعراض"),
    FINANCIAL_TRANSACTIONS("المعاملات المالية الحساسة", "مسائل الربا، الصكوك، العقود المستحدثة، والعملات الرقمية"),
    FATWA("الفتاوى الملزمة", "إصدار أحكام فقهية قطعية أو فتاوى نازلة"),
    CREED_DISPUTES("الخلافات العقدية", "مسائل الأسماء والصفات والفرق الكلامية والمذاهب العقدية"),
    SCHOLAR_ATTRIBUTIONS("نسب الأقوال للعلماء", "عزو أقوال أو فتاوى لأكابر العلماء دون سند موثق"),
    NONE("عام / لا يوجد موضوع حرج", "موضوعات دعوية وتربوية عامة")
}

enum class ShariaReviewStatus(val arabicTitle: String) {
    PENDING("قيد الانتظار"),
    IN_REVIEW("قيد المراجعة الفاحصة"),
    CHANGES_REQUESTED("مطلوب تعديل شرعي"),
    ESCALATED_SECOND_REVIEW("محول لمراجع ثانٍ"),
    DUAL_APPROVAL_PENDING("بانتظار الاعتماد المشترك"),
    APPROVED("معتمد شرعياً"),
    REJECTED("مرفوض شرعياً")
}

data class SourceVariation(
    val id: String = UUID.randomUUID().toString(),
    val sourceName: String,
    val narratorOrScholar: String,
    val text: String,
    val grade: String,
    val notes: String = "",
    val url: String = ""
)

data class ShariaClaim(
    val id: String = UUID.randomUUID().toString(),
    val claimText: String,
    val positionContext: String, // e.g. "المشهد 1 (00:00 - 00:15) - نص الراوي الصوتي"
    val sourceType: String = "HADITH", // QURAN, HADITH, TAFSIR, FIQH, SCHOLAR_QUOTE
    val sourceTitle: String,
    val sourceReference: String,
    val sourceUrl: String = "",
    val originalSourceText: String,
    val hadithGrade: String? = null, // e.g. "صحيح - رواه البخاري في صحيحه برقم 1"
    val hadithNarrator: String? = null, // e.g. "عمر بن الخطاب رضي الله عنه"
    val sourceVariations: List<SourceVariation> = emptyList(),
    val isVerified: Boolean = false,
    val reviewerComment: String? = null
)

data class RevisionHistoryItem(
    val versionId: String = UUID.randomUUID().toString(),
    val versionNumber: Int,
    val editedBy: String,
    val editedAt: Long = System.currentTimeMillis(),
    val changeSummary: String,
    val fullTextSnapshot: String
)

data class InternalNote(
    val id: String = UUID.randomUUID().toString(),
    val authorId: String,
    val authorName: String,
    val noteText: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class ShariaAuditLog(
    val id: String = UUID.randomUUID().toString(),
    val itemId: String,
    val reviewerId: String,
    val reviewerName: String,
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ShariaReviewDecision(
    val primaryReviewerId: String,
    val primaryReviewerName: String,
    val primaryStatus: ShariaReviewStatus,
    val primaryNotes: String,
    val primaryTimestamp: Long = System.currentTimeMillis(),
    val secondReviewerId: String? = null,
    val secondReviewerName: String? = null,
    val secondStatus: ShariaReviewStatus? = null,
    val secondNotes: String? = null,
    val secondTimestamp: Long? = null,
    val isDualApprovalRequired: Boolean = false,
    val isDualApprovalCompleted: Boolean = false,
    val scheduledReReviewDate: Long? = null
)

data class ShariaReviewItem(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val contentTitle: String,
    val creatorId: String,
    val creatorName: String,
    val fullContentText: String,
    val category: String, // "القرآن وعلومه", "الحديث الشريف", "الفقه وأصوله", "العقيدة", "المعاملات المالية", "الأسرة"
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val criticalTopics: List<CriticalTopic> = emptyList(),
    val claims: List<ShariaClaim> = emptyList(),
    val revisions: List<RevisionHistoryItem> = emptyList(),
    val internalNotes: List<InternalNote> = emptyList(),
    val auditLogs: List<ShariaAuditLog> = emptyList(),
    val status: ShariaReviewStatus = ShariaReviewStatus.PENDING,
    val currentReviewerId: String? = null,
    val currentReviewerName: String? = null,
    val contentVersion: Int = 1,
    val isDualApprovalRequired: Boolean = false,
    val decision: ShariaReviewDecision? = null,
    val submittedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class ShariaReviewFilter(
    val riskLevel: RiskLevel? = null,
    val category: String? = null,
    val status: ShariaReviewStatus? = null,
    val criticalTopic: CriticalTopic? = null,
    val searchQuery: String = "",
    val sortByDateAscending: Boolean = false
)
