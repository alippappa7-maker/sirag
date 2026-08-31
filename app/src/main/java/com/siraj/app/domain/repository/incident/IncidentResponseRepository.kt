package com.siraj.app.domain.repository.incident

import com.siraj.app.domain.models.incident.EmergencyActionRecord
import com.siraj.app.domain.models.incident.IncidentContact
import com.siraj.app.domain.models.incident.IncidentPostMortemReport
import com.siraj.app.domain.models.incident.IncidentResponseState
import com.siraj.app.domain.models.incident.IncidentRole
import com.siraj.app.domain.models.incident.ShariaIncidentCorrection
import kotlinx.coroutines.flow.Flow

interface IncidentResponseRepository {
    /**
     * تدفق حالة الاستجابة للحوادث
     */
    fun getIncidentResponseStateStream(): Flow<IncidentResponseState>

    /**
     * تدفق سجلات تقارير الحوادث (Post-Mortems)
     */
    fun getPostMortemReportsStream(): Flow<List<IncidentPostMortemReport>>

    /**
     * تدفق سجل الإجراءات الطارئة المنفذة
     */
    fun getEmergencyActionsStream(): Flow<List<EmergencyActionRecord>>

    /**
     * تدفق مصفوفة جهات الاتصال وفرق الطوارئ
     */
    fun getContactsMatrixStream(): Flow<List<IncidentContact>>

    /**
     * إيقاف أو إعادة تفعيل النشر العام (Kill Switch)
     */
    suspend fun toggleGlobalPublishingHalt(
        halt: Boolean,
        executedByUserId: String,
        executedByRole: IncidentRole,
        reasonArabic: String,
    ): Result<EmergencyActionRecord>

    /**
     * تدوير مفتاح أو رمز اعتماد مسرب
     */
    suspend fun rotateSecretKey(
        secretIdentifier: String,
        executedByUserId: String,
        reasonArabic: String,
    ): Result<EmergencyActionRecord>

    /**
     * تعليق وسحب محتوى أو مشروع منشور
     */
    suspend fun suspendPublishedProject(
        projectId: String,
        executedByUserId: String,
        reasonArabic: String,
    ): Result<EmergencyActionRecord>

    /**
     * تنفيذ استرداد مالي أو إعادة أرصدة لعملية مكررة
     */
    suspend fun executeBatchRefund(
        targetUserOrBatchId: String,
        refundAmountCredits: Int,
        executedByUserId: String,
        reasonArabic: String,
    ): Result<EmergencyActionRecord>

    /**
     * إجراء وتوثيق تصحيح شرعي معتمد من مراجعين
     */
    suspend fun submitShariaCorrection(correction: ShariaIncidentCorrection): Result<ShariaIncidentCorrection>

    /**
     * إنشاء تقرير حادث شامل (Incident Post-Mortem Report)
     */
    suspend fun createPostMortemReport(report: IncidentPostMortemReport): Result<IncidentPostMortemReport>
}
