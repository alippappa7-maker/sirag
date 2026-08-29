package com.siraj.app.domain.models.monitoring

import java.util.UUID

/**
 * Service identification for health checking and telemetry.
 */
enum class MonitoredService(val displayNameArabic: String, val category: ServiceCategory) {
    AUTHENTICATION("المصادقة (Auth)", ServiceCategory.CORE_FIREBASE),
    FIRESTORE("قاعدة البيانات (Firestore)", ServiceCategory.CORE_FIREBASE),
    STORAGE("التخزين السحابي (Cloud Storage)", ServiceCategory.CORE_FIREBASE),
    CLOUD_FUNCTIONS("دوال السحابة (Cloud Functions)", ServiceCategory.BACKEND_COMPUTE),
    CLOUD_RUN("خدمات المعالجة (Cloud Run)", ServiceCategory.BACKEND_COMPUTE),
    GEMINI_AI_PROVIDER("مزود الذكاء الاصطناعي (Gemini)", ServiceCategory.AI_PROVIDERS),
    QURAN_API_PROVIDER("مزود واجهة القرآن ومصحف المدينة", ServiceCategory.EXTERNAL_APIS),
    IMAGE_GENERATION_PROVIDER("مزود توليد الصور والخلفيات", ServiceCategory.AI_PROVIDERS),
    AUDIO_SYNTH_PROVIDER("مزود معالجة الصوت والتلاوات", ServiceCategory.MEDIA_SERVICES),
    VIDEO_RENDERING_QUEUE("طابور معالجة وتصيير الفيديو", ServiceCategory.MEDIA_SERVICES),
    FCM_NOTIFICATIONS("الإشعارات السحابية (FCM)", ServiceCategory.MESSAGING),
    GOOGLE_PLAY_BILLING("التحقق من مشتريات Google Play", ServiceCategory.STORE_VERIFICATION),
    APPLE_APP_STORE_BILLING("التحقق من مشتريات Apple StoreKit", ServiceCategory.STORE_VERIFICATION)
}

enum class ServiceCategory(val displayNameArabic: String) {
    CORE_FIREBASE("خدمات Firebase الأساسية"),
    BACKEND_COMPUTE("خدمات المعالجة والـ Backend"),
    AI_PROVIDERS("مزودو الذكاء الاصطناعي"),
    EXTERNAL_APIS("الواجهات الخارجية والقرآنية"),
    MEDIA_SERVICES("خدمات الصوت والوسائط والفيديو"),
    MESSAGING("المراسلة والإشعارات"),
    STORE_VERIFICATION("التحقق من اشتراكات المتاجر")
}

enum class ServiceHealthStatus(val displayNameArabic: String, val severityLevel: Int) {
    HEALTHY("سليمة وتعمل بكفاءة", 0),
    DEGRADED("أداء منخفض / تأخير استجابة", 1),
    UNAVAILABLE("معطلة / انقطاع في الخدمة", 2),
    MAINTENANCE("تحت الصيانة المجدولة", 3),
    CIRCUIT_BROKEN_DISABLED("معطلة احترازياً (Circuit Broken)", 4)
}

enum class IncidentSeverity(val displayNameArabic: String, val level: Int) {
    P0_CRITICAL("حرجة جداً (P0) - انقطاع شامل", 0),
    P1_HIGH("عالية (P1) - تأثر وظيفة محورية", 1),
    P2_MEDIUM("متوسطة (P2) - انخفاض أداء أو مزود بديل متاح", 2),
    P3_LOW("منخفضة (P3) - عطل طفيف أو غير مؤثر على المستخدمين", 3)
}

enum class IncidentState(val displayNameArabic: String) {
    INVESTIGATING("قيد التحقيق والفحص"),
    IDENTIFIED("تم تحديد سبب العطل"),
    MITIGATING("جارٍ التخفيف والتحويل للمزود البديل"),
    RESOLVED("تم حل العطل واكتمال الفحص"),
    MONITORING("مرحلة المراقبة بعد الإصلاح")
}

data class ServiceHealthCheck(
    val service: MonitoredService,
    val status: ServiceHealthStatus,
    val latencyMs: Long,
    val errorRatePercent: Double,
    val timeoutMs: Long = 5000L,
    val lastCheckedTimestamp: Long = System.currentTimeMillis(),
    val isCircuitBroken: Boolean = false,
    val activeIncidentId: String? = null,
    val fallbackService: MonitoredService? = null,
    val queueDepth: Int = 0,
    val crashRatePercent: Double = 0.0,
    val storageUsageGb: Double = 0.0,
    val failedPaymentsCountLastHour: Int = 0,
    val statusMessageArabic: String = "الخدمة تعمل ضمن الحدود التشغيلية الطبيعية",
    val runbookKey: String = service.name.lowercase()
)

data class ServiceIncident(
    val incidentId: String = "INC-${UUID.randomUUID().toString().take(8).uppercase()}",
    val service: MonitoredService,
    val titleArabic: String,
    val descriptionArabic: String,
    val severity: IncidentSeverity,
    val state: IncidentState,
    val startTimestamp: Long = System.currentTimeMillis(),
    val resolvedTimestamp: Long? = null,
    val rootCauseSummaryArabic: String? = null,
    val mitigationActionArabic: String? = null,
    val runbookUrl: String = "INCIDENT_RUNBOOK.md#${service.name.lowercase()}",
    val affectedUsersEstimate: Int = 0,
    val timelineEvents: List<IncidentTimelineEvent> = emptyList()
)

data class IncidentTimelineEvent(
    val eventId: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val state: IncidentState,
    val notesArabic: String,
    val updatedBy: String = "Automated Health Probe"
)

data class MonitoringAlert(
    val alertId: String = "ALT-${UUID.randomUUID().toString().take(8).uppercase()}",
    val service: MonitoredService,
    val severity: IncidentSeverity,
    val titleArabic: String,
    val messageArabic: String,
    val timestamp: Long = System.currentTimeMillis(),
    val incidentId: String? = null,
    val isAcknowledged: Boolean = false,
    val deduplicationKey: String = "${service.name}_${severity.name}"
)

data class SystemTelemetryOverview(
    val totalServicesCount: Int = 13,
    val healthyServicesCount: Int = 13,
    val degradedServicesCount: Int = 0,
    val unavailableServicesCount: Int = 0,
    val overallSystemStatus: ServiceHealthStatus = ServiceHealthStatus.HEALTHY,
    val globalCrashRatePercent: Double = 0.02,
    val avgSystemLatencyMs: Long = 142,
    val globalErrorRatePercent: Double = 0.08,
    val totalQueueDepth: Int = 12,
    val totalStorageUsageTb: Double = 1.48,
    val failedPurchasesLast24h: Int = 0,
    val activeIncidentsCount: Int = 0,
    val lastProbeTimestamp: Long = System.currentTimeMillis()
)
