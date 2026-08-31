package com.siraj.app.domain.repository

import com.siraj.app.domain.models.beta.BetaDefectRecord
import com.siraj.app.domain.models.beta.DefectClassification
import com.siraj.app.domain.models.beta.DefectPriority
import com.siraj.app.domain.models.beta.DefectStatus
import com.siraj.app.domain.models.beta.DefectTriageSummary
import kotlinx.coroutines.flow.Flow

interface BetaDefectManagementRepository {
    /**
     * جلب كافة الملاحظات والعيوب المصنفة
     */
    fun getAllDefects(): Flow<List<BetaDefectRecord>>

    /**
     * جلب تفاصيل عيب محدد بمعرفه
     */
    fun getDefectById(id: String): Flow<BetaDefectRecord?>

    /**
     * تصنيف وفرز العيب وتعيين المسؤول والإصدار المستهدف
     */
    suspend fun triageDefect(
        id: String,
        classification: DefectClassification,
        priority: DefectPriority,
        assignedRole: String,
        targetRelease: String,
    ): Result<Unit>

    /**
     * تحديث حالة العيب مع فرض ذكر سبب الإغلاق أو تفاصيل التحقق عند الإنهاء
     */
    suspend fun updateDefectStatus(
        id: String,
        newStatus: DefectStatus,
        resolutionNote: String? = null,
        closureReason: String? = null,
        verificationTest: String? = null,
    ): Result<Unit>

    /**
     * جلب قائمة الإصلاح المرتبة حسب الأولوية والخطورة (P0 Blockers -> P1 Critical -> P2 Major -> P3 Minor)
     */
    fun getPrioritizedFixList(): Flow<List<BetaDefectRecord>>

    /**
     * جلب قائمة العيوب المؤجلة للإصدارات القادمة مع توثيق الأسباب
     */
    fun getDeferredDefectsList(): Flow<List<BetaDefectRecord>>

    /**
     * استخراج ملخص إحصائي شامل لفرز وتصنيف عيوب البيتا
     */
    fun getTriageSummary(): Flow<DefectTriageSummary>

    /**
     * تسجيل عيب جديد ناتج عن ملاحظات المختبرين
     */
    suspend fun registerDefect(defect: BetaDefectRecord): Result<Unit>
}
