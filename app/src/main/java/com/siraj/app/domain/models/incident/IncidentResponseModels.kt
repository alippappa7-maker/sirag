package com.siraj.app.domain.models.incident

import java.util.UUID

/**
 * أنواع الحوادث التشغيلية والأمنية والشرعية والمالية في منصة سراج
 */
enum class IncidentType(
    val code: String,
    val titleArabic: String,
    val defaultSeverity: IncidentSeverity,
    val primaryRole: IncidentRole,
    val descriptionArabic: String
) {
    SERVICE_OUTAGE(
        code = "INC_OUTAGE",
        titleArabic = "توقف الخدمة أو بطء شديد",
        defaultSeverity = IncidentSeverity.P0_CRITICAL,
        primaryRole = IncidentRole.TECH_LEAD_ENGINEER,
        descriptionArabic = "انقطاع كلي أو جزئي في الخدمات الأساسية أو الخوادم السحابية أو قواعد البيانات"
    ),
    KEY_CREDENTIAL_LEAK(
        code = "INC_KEY_LEAK",
        titleArabic = "تسرب مفتاح أو بيانات اعتماد",
        defaultSeverity = IncidentSeverity.P0_CRITICAL,
        primaryRole = IncidentRole.SECURITY_OFFICER,
        descriptionArabic = "رصد تسرب لمفتاح API أو رمز وصول أو اعتماد سحابي في السجلات أو الكود"
    ),
    UNAUTHORIZED_ACCESS(
        code = "INC_UNAUTH_ACCESS",
        titleArabic = "وصول غير مصرح به أو اختراق",
        defaultSeverity = IncidentSeverity.P0_CRITICAL,
        primaryRole = IncidentRole.SECURITY_OFFICER,
        descriptionArabic = "محاولة أو نجاح وصول لحسابات إدارية أو بيانات مستخدمين دون صلاحية رسمية"
    ),
    SHARIA_CONTENT_ERROR(
        code = "INC_SHARIA_ERROR",
        titleArabic = "خطأ في نص قرآني أو حديث أو فتوى",
        defaultSeverity = IncidentSeverity.P0_CRITICAL,
        primaryRole = IncidentRole.SHARIA_REVIEWER_LEAD,
        descriptionArabic = "اكتشاف خطأ في رسم آية أو نسبة حديث غير صحيحة أو حكم شرعي مغلوط"
    ),
    UNREVIEWED_CONTENT_PUBLISHED(
        code = "INC_UNREVIEWED_PUB",
        titleArabic = "نشر محتوى غير مراجع أو مسودة",
        defaultSeverity = IncidentSeverity.P1_HIGH,
        primaryRole = IncidentRole.SHARIA_REVIEWER_LEAD,
        descriptionArabic = "تجاوز بوابات المراجعة ونشر مشروع أو مرئية قبل اعتماد المراجعين الشرعيين"
    ),
    SUBSCRIPTION_GLITCH(
        code = "INC_SUB_GLITCH",
        titleArabic = "خلل في الاشتراكات أو الصلاحيات",
        defaultSeverity = IncidentSeverity.P1_HIGH,
        primaryRole = IncidentRole.FINANCIAL_BILLING_LEAD,
        descriptionArabic = "فشل تفعيل باقة مدفوعة أو عدم منح الحصص والأرصدة المستحقة للمشتركين"
    ),
    DUPLICATE_CHARGE(
        code = "INC_DUP_CHARGE",
        titleArabic = "تكرار خصم مالي أو أرصدة",
        defaultSeverity = IncidentSeverity.P1_HIGH,
        primaryRole = IncidentRole.FINANCIAL_BILLING_LEAD,
        descriptionArabic = "خصم مكرر من بطاقة المستخدم أو سحب أرصدة مضاعفة لعملية توليد واحدة"
    ),
    FILE_DATA_LOSS(
        code = "INC_DATA_LOSS",
        titleArabic = "فقد أو تلف ملفات أو أصول",
        defaultSeverity = IncidentSeverity.P1_HIGH,
        primaryRole = IncidentRole.TECH_LEAD_ENGINEER,
        descriptionArabic = "تعذر الوصول للملفات الصوتية أو مقاطع الفيديو المصدرة أو مشاريع المستخدمين"
    ),
    ABUSE_SPAM(
        code = "INC_ABUSE_SPAM",
        titleArabic = "إساءة استخدام أو هجمات سبام",
        defaultSeverity = IncidentSeverity.P2_MEDIUM,
        primaryRole = IncidentRole.SECURITY_OFFICER,
        descriptionArabic = "محاولات إغراق النظام بطلبات توليد مكثفة أو رفع محتوى غير لائق"
    ),
    COPYRIGHT_INFRINGEMENT(
        code = "INC_DMCA_COPYRIGHT",
        titleArabic = "انتهاك حقوق نشر أو بلاغ DMCA",
        defaultSeverity = IncidentSeverity.P2_MEDIUM,
        primaryRole = IncidentRole.COMMUNICATIONS_OFFICER,
        descriptionArabic = "استخدام تلاوة أو صورة أو خط مرخص دون إذن مع وصول بلاغ من صاحب الحقوق"
    )
}

enum class IncidentSeverity(
    val level: String,
    val displayNameArabic: String,
    val targetResponseMinutes: Int,
    val targetMitigationHours: Int
) {
    P0_CRITICAL("P0", "حرج جداً (فوري)", 15, 2),
    P1_HIGH("P1", "عالي الخطورة", 30, 4),
    P2_MEDIUM("P2", "متوسط الخطورة", 120, 12),
    P3_LOW("P3", "منخفض / استفسار", 480, 48)
}

enum class IncidentPhase(
    val displayNameArabic: String,
    val stepOrder: Int,
    val descriptionArabic: String
) {
    DETECTION("الاكتشاف", 1, "رصد الحدث عبر المراقبة الآلية أو بلاغ المستخدمين أو التدقيق"),
    CLASSIFICATION("التصنيف والتقييم", 2, "تحديد نوع الحادث ومستوى خطورته P0-P3 وتعيين الفريق المسؤول"),
    CONTAINMENT("العزل الفوري", 3, "إيقاف انتشار الضرر عبر تفعيل قواطع الدائرة أو تجميد النشر أو تدوير الأسرار"),
    REMEDIATION("الإصلاح الجذري", 4, "معالجة الخلل البرمجي أو تعديل المحتوى أو استرداد الأموال"),
    VERIFICATION("التحقق والاختبار", 5, "التأكد من سلامة الإصلاح في Staging ثم بيئة الإنتاج"),
    COMMUNICATION("التواصل الشفاف", 6, "إخطار المستخدمين أو الجهات المعنية دون كشف تفاصيل أمنية حساسة"),
    DOCUMENTATION("التوثيق والتقرير", 7, "إنشاء تقرير الحادث الشامل (Incident Post-Mortem)"),
    POST_MORTEM_REVIEW("المراجعة اللاحقة", 8, "اجتماع الدروس المستفادة وتحديث القواعد لمنع التكرار")
}

enum class IncidentRole(
    val displayNameArabic: String,
    val contactChannel: String
) {
    INCIDENT_COMMANDER("قائد الاستجابة للحوادث", "incident-commander@siraj.app"),
    TECH_LEAD_ENGINEER("المهندس الرئيسي للأنظمة", "tech-lead@siraj.app"),
    SHARIA_REVIEWER_LEAD("رئيس هيئة المراجعة الشرعية", "sharia-board@siraj.app"),
    SECURITY_OFFICER("مسؤول أمن المعلومات والخصوصية", "security@siraj.app"),
    COMMUNICATIONS_OFFICER("مسؤول التواصل والإعلام", "press-relations@siraj.app"),
    FINANCIAL_BILLING_LEAD("مسؤول الفوترة والاشتراكات", "billing-support@siraj.app")
}

/**
 * الإجراءات الطارئة المنفذة فوراً لعزل الحادث
 */
enum class EmergencyActionType(
    val displayNameArabic: String,
    val requiresApproval: Boolean
) {
    EMERGENCY_HALT_PUBLISHING("إيقاف النشر العام فورياً (Kill Switch)", true),
    SUSPEND_PROJECT_PUBLISHING("سحب وتعليق مشروع منشور", false),
    ROTATE_SECRET_CREDENTIAL("تدوير وإبطال مفاتيح الاعتماد المسربة", true),
    KILL_SWITCH_PROVIDER("فصل مزود خارجي فورياً والتحويل للبديل", false),
    SHARIA_CORRECTION_AND_VERSIONING("إجراء تصحيح شرعي وتثبيت إصدار جديد", true),
    TRIGGER_BATCH_REFUND("استرداد المدفوعات والأرصدة المكررة", true),
    REVOKE_USER_SESSIONS("إبطال جلسات مستخدمين مشبوهة", false),
    RESTORE_FROM_BACKUP("استعادة بيانات من نسخة احتياطية معتمدة", true)
}

data class EmergencyActionRecord(
    val actionId: String = UUID.randomUUID().toString(),
    val actionType: EmergencyActionType,
    val executedByUserId: String,
    val executedByRole: IncidentRole,
    val targetResource: String,
    val executionTimestamp: Long = System.currentTimeMillis(),
    val reasonArabic: String,
    val success: Boolean = true,
    val auditLogNotes: String = ""
)

data class IncidentContact(
    val contactId: String,
    val role: IncidentRole,
    val nameArabic: String,
    val primaryPhone: String,
    val secureEmail: String,
    val escalationOrder: Int,
    val available24x7: Boolean = true
)

data class IncidentPostMortemReport(
    val reportId: String = "REP-" + UUID.randomUUID().toString().take(8).uppercase(),
    val incidentId: String,
    val incidentType: IncidentType,
    val severity: IncidentSeverity,
    val titleArabic: String,
    val leadInvestigator: String,
    val detectionTimestamp: Long,
    val containmentTimestamp: Long?,
    val resolutionTimestamp: Long?,
    val totalDowntimeMinutes: Long = 0,
    val affectedUsersCount: Int = 0,
    val rootCauseSummaryArabic: String,
    val containmentStepsArabic: List<String> = emptyList(),
    val correctiveActionsArabic: List<String> = emptyList(),
    val preventiveTasksArabic: List<String> = emptyList(),
    val userNotificationIssued: Boolean = false,
    val userNoticeContentArabic: String? = null,
    val isSanitizedForPublic: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class ShariaIncidentCorrection(
    val correctionId: String = "SHARIA-CORR-" + UUID.randomUUID().toString().take(8).uppercase(),
    val incidentId: String,
    val projectId: String,
    val faultyText: String,
    val verifiedCorrectText: String,
    val primarySourceReference: String,
    val reviewer1Id: String,
    val reviewer1NotesArabic: String,
    val reviewer2Id: String?,
    val reviewer2NotesArabic: String?,
    val approvedByBothReviewers: Boolean = false,
    val publishedVersion: Int = 2,
    val correctedAt: Long = System.currentTimeMillis()
)

data class IncidentResponseState(
    val isGlobalPublishingHalted: Boolean = false,
    val activeIncidentsCount: Int = 0,
    val criticalP0Count: Int = 0,
    val reportsList: List<IncidentPostMortemReport> = emptyList(),
    val emergencyActionsHistory: List<EmergencyActionRecord> = emptyList(),
    val shariaCorrections: List<ShariaIncidentCorrection> = emptyList(),
    val contactsMatrix: List<IncidentContact> = emptyList()
)
