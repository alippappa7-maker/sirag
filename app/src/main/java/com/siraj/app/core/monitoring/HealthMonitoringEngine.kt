package com.siraj.app.core.monitoring

import com.siraj.app.domain.models.monitoring.IncidentSeverity
import com.siraj.app.domain.models.monitoring.MonitoredService
import com.siraj.app.domain.models.monitoring.ServiceCategory
import com.siraj.app.domain.models.monitoring.ServiceHealthStatus
import java.security.MessageDigest

/**
 * Core health check executor, alert deduplicator, and architectural isolation engine.
 * Ensures:
 * 1. Safe health probes without using real sacred religious texts or producing offensive AI tests.
 * 2. Deduplication of alerts so operators aren't flooded.
 * 3. Client message sanitization preventing cloud infrastructure exposure.
 * 4. Circuit breaker heuristics and graceful degradation routing.
 */
object HealthMonitoringEngine {

    // Synthetic non-religious probe token
    const val SYNTHETIC_HEALTH_PROBE_PAYLOAD = "SIRAJ_SYSTEM_HEALTH_CHECK_SYNTHETIC_PING_2026"

    /**
     * Deduplicates alerts based on service and severity within a 15-minute sliding window.
     */
    fun computeDeduplicationHash(service: MonitoredService, severity: IncidentSeverity): String {
        val raw = "${service.name}_${severity.name}"
        val bytes = MessageDigest.getInstance("MD5").digest(raw.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Sanitizes errors and returns a polite Arabic user-facing message
     * hiding all cluster IPs, function ARNs, database table schemas, and API keys.
     */
    fun sanitizeForUser(service: MonitoredService, rawTechnicalError: String?): String {
        return when (service) {
            MonitoredService.AUTHENTICATION -> 
                "خدمة تسجيل الدخول تواجه تحديثاً مؤقتاً، يرجى المحاولة بعد لحظات."
            MonitoredService.FIRESTORE, MonitoredService.STORAGE -> 
                "جاري مزامنة البيانات، سيتم الحفظ تلقائياً فور استقرار الاتصال."
            MonitoredService.CLOUD_FUNCTIONS, MonitoredService.CLOUD_RUN -> 
                "الخدمة السحابية مشغولة حالياً، جاري معالجة طلبك عبر الخادم الاحتياطي."
            MonitoredService.GEMINI_AI_PROVIDER, MonitoredService.IMAGE_GENERATION_PROVIDER -> 
                "مساعد الإنتاج الذكي يمر بفترة صيانة مؤقتة، يمكنك مواصلة التحرير اليدوي بكل سهولة."
            MonitoredService.QURAN_API_PROVIDER -> 
                "جاري استخدام النسخة المحلية المعتمدة من المصحف الشريف لضمان دقة النص."
            MonitoredService.AUDIO_SYNTH_PROVIDER, MonitoredService.VIDEO_RENDERING_QUEUE -> 
                "تمت إضافة مشروعك إلى طابور المعالجة وسيتم إشعارك فور اكتمال التصدير."
            MonitoredService.FCM_NOTIFICATIONS -> 
                "تنبيهات المنصة قيد التحديث."
            MonitoredService.GOOGLE_PLAY_BILLING, MonitoredService.APPLE_APP_STORE_BILLING -> 
                "عملية التحقق من الاشتراك قيد التأكيد مع المتجر، لن يتم خصم أي رصيد دون تأكيد رسمي."
        }
    }

    /**
     * Determines whether a service failure warrants tripping the circuit breaker.
     */
    fun shouldTripCircuitBreaker(
        consecutiveFailures: Int,
        latencyMs: Long,
        timeoutThresholdMs: Long,
        errorRatePercent: Double
    ): Boolean {
        if (consecutiveFailures >= 3) return true
        if (latencyMs > timeoutThresholdMs * 2) return true
        if (errorRatePercent >= 50.0) return true
        return false
    }

    /**
     * Suggests a fallback route when a primary service experiences an outage.
     */
    fun resolveFallbackRoute(service: MonitoredService): MonitoredService? {
        return when (service) {
            MonitoredService.GEMINI_AI_PROVIDER -> MonitoredService.CLOUD_RUN // Local rule-based fallback
            MonitoredService.QURAN_API_PROVIDER -> MonitoredService.FIRESTORE // Cached offline Mushaf
            MonitoredService.IMAGE_GENERATION_PROVIDER -> MonitoredService.STORAGE // Fallback to curated asset library
            MonitoredService.CLOUD_FUNCTIONS -> MonitoredService.CLOUD_RUN
            else -> null
        }
    }

    /**
     * Analyzes latency and error rate to compute overall health status.
     */
    fun evaluateHealthStatus(
        latencyMs: Long,
        errorRatePercent: Double,
        timeoutMs: Long,
        isCircuitBroken: Boolean
    ): ServiceHealthStatus {
        if (isCircuitBroken) return ServiceHealthStatus.CIRCUIT_BROKEN_DISABLED
        if (latencyMs >= timeoutMs || errorRatePercent >= 40.0) return ServiceHealthStatus.UNAVAILABLE
        if (latencyMs >= timeoutMs / 2 || errorRatePercent >= 5.0) return ServiceHealthStatus.DEGRADED
        return ServiceHealthStatus.HEALTHY
    }
}
