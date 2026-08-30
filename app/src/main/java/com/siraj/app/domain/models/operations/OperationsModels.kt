package com.siraj.app.domain.models.operations

/**
 * نماذج إدارة العمليات والتشغيل وإصدارات سراج بعد الإطلاق
 */

enum class LaunchPhase(val arabicLabel: String, val timeSpanDescription: String) {
    FIRST_24_HOURS("أول 24 ساعة", "مرحلة غرفة الطوارئ والمراقبة اللحظية"),
    FIRST_WEEK("الأسبوع الأول (الأيام 1-7)", "مرحلة التثبيت ومعالجة المشاكل الحرجة الأولى"),
    FIRST_30_DAYS("الشهر الأول (الأيام 8-30)", "مرحلة انتظام التحديثات والمراجعة الشهرية الأولى"),
    FIRST_60_DAYS("الشهر الثاني (الأيام 31-60)", "مرحلة تحسين الأداء وإدارة التكاليف واستقرار الميزات"),
    FIRST_90_DAYS("الشهر الثالث (الأيام 61-90)", "مرحلة التقييم الفصلي، تدوير الأسرار، وتثبيت التوافقية")
}

enum class HotfixSeverity(val priority: String, val maxSlaHours: Int, val description: String) {
    P0_CRITICAL("حرجة جداً", 4, "عطل يعطل الدخول أو الدفع أو يسبب انهياراً جماعياً (>1%) أو خطأ شرعي جسيم"),
    P1_HIGH("عالية", 12, "عطل يعطل ميزة رئيسية كالتصدير أو الذكاء الاصطناعي بنسبة مؤثرة"),
    P2_MEDIUM("متوسطة", 48, "مشكلة مظهرية أو عطل جزئي يمكن تجاوزه، يدرج في التحديث الأسبوعي القادم"),
    P3_LOW("منخفضة", 168, "تحسين طفيف أو ملاحظة ثانوية تدرج في الباكلوج الدوري")
}

data class HotfixEvaluation(
    val isHotfixJustified: Boolean,
    val severity: HotfixSeverity,
    val recommendedAction: String,
    val requiredApprovals: List<String>,
    val targetSlaHours: Int
)

data class FeatureRequestEvaluation(
    val title: String,
    val totalRequestsCount: Int,
    val reachScore: Int, // 1 - 10
    val impactScore: Int, // 1 - 10
    val confidenceScore: Int, // 1 - 10
    val effortScore: Int, // 1 - 10
    val calculatedRiceScore: Double,
    val isApprovedForBacklog: Boolean,
    val rejectionReason: String? = null
)

data class SecretRotationRecord(
    val secretId: String,
    val secretNameArabic: String,
    val targetService: String,
    val lastRotatedTimestamp: Long,
    val rotationIntervalDays: Int = 90,
    val isEmergencyRotation: Boolean = false,
    val status: RotationStatus = RotationStatus.ACTIVE
) {
    val nextRotationDueTimestamp: Long
        get() = lastRotatedTimestamp + (rotationIntervalDays.toLong() * 24 * 60 * 60 * 1000)

    val isOverdue: Boolean
        get() = System.currentTimeMillis() > nextRotationDueTimestamp

    val remainingDays: Int
        get() {
            val diff = nextRotationDueTimestamp - System.currentTimeMillis()
            return if (diff > 0) (diff / (1000 * 60 * 60 * 24)).toInt() else 0
        }
}

enum class RotationStatus {
    ACTIVE,
    PENDING_ROTATION,
    ROTATED_SUCCESSFULLY,
    REVOKED_EMERGENCY
}

data class MonthlyFinOpsAudit(
    val monthYear: String,
    val allocatedBudgetUsd: Double,
    val actualSpentUsd: Double,
    val geminiApiCostUsd: Double,
    val cloudFunctionsCostUsd: Double,
    val firestoreCostUsd: Double,
    val cloudStorageCostUsd: Double,
    val videoTranscodeCostUsd: Double,
    val costPerActiveUserUsd: Double,
    val isBudgetExceeded: Boolean = actualSpentUsd > allocatedBudgetUsd,
    val triggeredAlertLevel: BudgetAlertLevel,
    val costReductionActions: List<String>
)

enum class BudgetAlertLevel(val percentThreshold: Int, val actionRequired: String) {
    NORMAL(0, "استمرار العمليات الطبيعية"),
    WARNING_50(50, "إشعار فريق الهندسة والمالية بالمعدل"),
    ALERT_80(80, "تفعيل الترشيد التلقائي لطلبات الذكاء الاصطناعي غير الحرجة"),
    CRITICAL_100(100, "إيقاف الميزات المكلفة غير الأساسية تلقائياً وحصر العمليات بالأساسيات")
}

data class ShariaContentMonthlyAudit(
    val monthYear: String,
    val totalPublishedIslamicItems: Int,
    val totalCorrectionsRequested: Int,
    val totalCorrectionsAppliedWithLog: Int,
    val shariaReviewAccuracyPercentage: Double,
    val unlicensedAssetsRevoked: Int,
    val isAuditCompliant: Boolean
)

data class VersionCompatibilityPolicy(
    val currentProductionVersion: String,
    val currentVersionCode: Int,
    val minSupportedVersion: String,
    val minSupportedVersionCode: Int,
    val forcedUpdateMessageArabic: String,
    val storeUpdateUrl: String,
    val supportedDatabaseSchemaVersion: Int
) {
    fun isVersionSupported(clientVersionCode: Int): Boolean {
        return clientVersionCode >= minSupportedVersionCode
    }

    fun isUpdateRequired(clientVersionCode: Int): Boolean {
        return clientVersionCode < minSupportedVersionCode
    }
}

data class FeatureFlagDefinition(
    val flagKey: String,
    val nameArabic: String,
    val defaultValue: Boolean,
    val isServerSideSecurityGated: Boolean, // Must never be client-modifiable if true
    val rolloutPercentage: Int, // 0 - 100
    val expirationDate: String,
    val description: String
)
