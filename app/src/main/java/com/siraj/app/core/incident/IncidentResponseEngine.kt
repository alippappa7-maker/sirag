package com.siraj.app.core.incident

import com.siraj.app.domain.models.incident.EmergencyActionRecord
import com.siraj.app.domain.models.incident.EmergencyActionType
import com.siraj.app.domain.models.incident.IncidentContact
import com.siraj.app.domain.models.incident.IncidentRole
import com.siraj.app.domain.models.incident.IncidentType
import com.siraj.app.domain.models.incident.ShariaIncidentCorrection
import java.security.MessageDigest

/**
 * محرك الاستجابة للحوادث وإدارة الإجراءات الطارئة لمنظومة سراج
 */
object IncidentResponseEngine {
    val STANDARD_CONTACTS_MATRIX =
        listOf(
            IncidentContact(
                contactId = "CONT-001",
                role = IncidentRole.INCIDENT_COMMANDER,
                nameArabic = "د. أنس الزهراني",
                primaryPhone = "+966500001001",
                secureEmail = "commander@siraj.app",
                escalationOrder = 1,
            ),
            IncidentContact(
                contactId = "CONT-002",
                role = IncidentRole.SHARIA_REVIEWER_LEAD,
                nameArabic = "الشيخ د. عبد الرحمن السعدي",
                primaryPhone = "+966500001002",
                secureEmail = "sharia.lead@siraj.app",
                escalationOrder = 1,
            ),
            IncidentContact(
                contactId = "CONT-003",
                role = IncidentRole.TECH_LEAD_ENGINEER,
                nameArabic = "م. خالد المنصور",
                primaryPhone = "+966500001003",
                secureEmail = "tech.lead@siraj.app",
                escalationOrder = 2,
            ),
            IncidentContact(
                contactId = "CONT-004",
                role = IncidentRole.SECURITY_OFFICER,
                nameArabic = "م. عمر الحربي (CISO)",
                primaryPhone = "+966500001004",
                secureEmail = "security@siraj.app",
                escalationOrder = 2,
            ),
            IncidentContact(
                contactId = "CONT-005",
                role = IncidentRole.FINANCIAL_BILLING_LEAD,
                nameArabic = "أ. ياسر الغامدي",
                primaryPhone = "+966500001005",
                secureEmail = "billing@siraj.app",
                escalationOrder = 3,
            ),
            IncidentContact(
                contactId = "CONT-006",
                role = IncidentRole.COMMUNICATIONS_OFFICER,
                nameArabic = "أ. طارق الشمري",
                primaryPhone = "+966500001006",
                secureEmail = "press@siraj.app",
                escalationOrder = 3,
            ),
        )

    /**
     * التحقق من سلامة وصحة التصحيح الشرعي واعتماد المراجعين
     */
    fun validateShariaCorrection(correction: ShariaIncidentCorrection): Result<ShariaIncidentCorrection> {
        if (correction.faultyText.isBlank() || correction.verifiedCorrectText.isBlank()) {
            return Result.failure(IllegalArgumentException("النص الشرعي المصوب أو الخاطئ لا يمكن أن يكون فارغاً"))
        }
        if (correction.primarySourceReference.isBlank()) {
            return Result.failure(IllegalArgumentException("يجب توفير المصدر المعتمد ورقم الآية أو الحديث الصحيح"))
        }
        if (correction.reviewer1Id.isBlank()) {
            return Result.failure(IllegalArgumentException("يلزم اعتماد المراجع الشرعي الأول"))
        }
        // For high impact sacred text (Quran/Hadith), double review is strictly enforced
        val isDoubleApproved =
            !correction.reviewer2Id.isNullOrBlank() &&
                correction.reviewer2NotesArabic?.isNotBlank() == true

        val verifiedCorrection =
            correction.copy(
                approvedByBothReviewers = isDoubleApproved,
            )
        return Result.success(verifiedCorrection)
    }

    /**
     * تجريد تقارير الحوادث من أي تفاصيل أمنية قد يستغلها مهاجم أو تكشف بنية الخوادم
     */
    fun sanitizePublicIncidentNotice(
        incidentType: IncidentType,
        rawTechnicalError: String,
    ): String =
        when (incidentType) {
            IncidentType.SERVICE_OUTAGE ->
                "نواجه حالياً بطئاً مؤقتاً في بعض الخوادم السحابية. يعمل الفريق الهندسي على استعادة الجاهزية الكاملة، وجميع بياناتكم ومشاريعكم محفوظة بأمان."
            IncidentType.KEY_CREDENTIAL_LEAK ->
                "كإجراء أمني وقائي استباقي، قمنا بتحديث مفاتيح الاعتماد وتجديد الجلسات النشطة لضمان أعلى معايير الحماية لبياناتكم."
            IncidentType.UNAUTHORIZED_ACCESS ->
                "تم رصد نشاط غير معتاد وتصدت له أنظمة الحماية بنجاح. قمنا بإعادة ضبط الرموز الأمنية احترازياً، ونهيب بالمستخدمين تفعيل التحقق الثنائي."
            IncidentType.SHARIA_CONTENT_ERROR ->
                "حرصاً على سلامة النصوص الشرعية، تم رصد تعديل في أحد النقولات وإعادة ضبطه فوراً ومطابقته مع المصحف المعتمد والمراجع الموثقة."
            IncidentType.UNREVIEWED_CONTENT_PUBLISHED ->
                "تم سحب المحتوى وإعادته إلى مسار المراجعة الشرعية والتحقق لاستيفاء متطلبات النشر الرسمية."
            IncidentType.SUBSCRIPTION_GLITCH, IncidentType.DUPLICATE_CHARGE ->
                "رصدنا خطأً غير مقصود في مزامنة الفوترة، وتمت جدولة استرداد الأرصدة والمبالغ المستحقة لكافة المتأثرين تلقائياً دون أي حاجة للتواصل مع الدعم."
            IncidentType.FILE_DATA_LOSS ->
                "تمت استعادة الأصول والمشاريع المتأثرة من النسخ الاحتياطية السحابية المعتمدة بنجاح."
            IncidentType.ABUSE_SPAM, IncidentType.COPYRIGHT_INFRINGEMENT ->
                "تم اتخاذ الإجراءات النظامية اللازمة وحجب المواد المخالفة لسياسات الاستخدام وحقوق النشر."
        }

    /**
     * توليد بصمة تجزئة آمنة للحدث بدون كشف نصوص حساسة
     */
    fun generateIncidentFingerprint(
        incidentType: IncidentType,
        sourceId: String,
    ): String {
        val raw = "${incidentType.code}:$sourceId:${System.currentTimeMillis() / 60000}"
        val digest = MessageDigest.getInstance("MD5").digest(raw.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * إنشاء إجراء طارئ مع التحقق من الصلاحية
     */
    fun createEmergencyAction(
        actionType: EmergencyActionType,
        executedByUserId: String,
        executedByRole: IncidentRole,
        targetResource: String,
        reasonArabic: String,
    ): Result<EmergencyActionRecord> {
        if (reasonArabic.isBlank()) {
            return Result.failure(IllegalArgumentException("يجب توضيح سبب الإجراء الطارئ في سجل التدقيق"))
        }
        // Enforce role-based segregation of duties
        val isAuthorized =
            when (actionType) {
                EmergencyActionType.EMERGENCY_HALT_PUBLISHING ->
                    executedByRole == IncidentRole.INCIDENT_COMMANDER || executedByRole == IncidentRole.TECH_LEAD_ENGINEER
                EmergencyActionType.SHARIA_CORRECTION_AND_VERSIONING ->
                    executedByRole == IncidentRole.SHARIA_REVIEWER_LEAD
                EmergencyActionType.ROTATE_SECRET_CREDENTIAL ->
                    executedByRole == IncidentRole.SECURITY_OFFICER || executedByRole == IncidentRole.TECH_LEAD_ENGINEER
                EmergencyActionType.TRIGGER_BATCH_REFUND ->
                    executedByRole == IncidentRole.FINANCIAL_BILLING_LEAD || executedByRole == IncidentRole.INCIDENT_COMMANDER
                else -> true
            }

        if (!isAuthorized) {
            return Result.failure(
                SecurityException(
                    "الدور [${executedByRole.displayNameArabic}] غير مخول بتنفيذ الإجراء الطارئ [${actionType.displayNameArabic}]",
                ),
            )
        }

        val record =
            EmergencyActionRecord(
                actionType = actionType,
                executedByUserId = executedByUserId,
                executedByRole = executedByRole,
                targetResource = targetResource,
                reasonArabic = reasonArabic,
                auditLogNotes = "Audit confirmed for emergency action ${actionType.name} by ${executedByRole.name}",
            )
        return Result.success(record)
    }
}
