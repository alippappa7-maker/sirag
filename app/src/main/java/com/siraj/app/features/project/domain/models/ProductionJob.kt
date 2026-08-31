package com.siraj.app.features.project.domain.models

enum class ProductionJobStatus(
    val labelArabic: String,
) {
    QUEUED("في الطابور"),         // في طابور Cloud Tasks
    PROCESSING("تجهيز الموارد"),   // قيد التحقق وتجهيز الموارد
    COMPOSING("تركيب المشاهد"),    // تركيب الصور والفيديو وطبقات الصوت
    ENCODING("ترميز الفيديو"),     // تصيير وترميز الفيديو النهائي
    RENDERING("معالجة الوسائط"),   // معالجة عامة للتوافق
    UPLOADING("رفع إلى التخزين"),  // رفع الفيديو إلى Cloud Storage
    COMPLETED("اكتمل بنجاح"),      // جاهز للتنزيل والمشاهدة
    FAILED("تعذر الإنتاج"),        // فشل مع تسجيل السبب وإعادة الرصيد
    CANCELLED("ملغاة"),            // تم الإلغاء
}

enum class ProductionQuality(
    val label: String,
    val resolution: String,
    val costMultiplier: Double,
) {
    SD_720P("720p HD", "1280x720", 1.0),
    FHD_1080P("1080p Full HD", "1920x1080", 1.5),
    UHD_4K("4K Ultra HD", "3840x2160", 3.0),
}

data class ProductionJob(
    val jobId: String = "",
    val projectId: String = "",
    val projectTitle: String = "",
    val ownerId: String = "",
    val workspaceId: String = "",
    val status: ProductionJobStatus = ProductionJobStatus.QUEUED,
    val progress: Int = 0, // 0 - 100%
    val attemptCount: Int = 0,
    val maxAttempts: Int = 3,
    val costUnits: Long = 10L,
    val creditRefunded: Boolean = false,
    val provider: String = "Siraj-Video-Composition-Worker",
    val quality: ProductionQuality = ProductionQuality.FHD_1080P,
    val burnSubtitles: Boolean = true,
    val includeSourceCitation: Boolean = true,
    val includeWatermark: Boolean = true,
    val fps: Int = 30,
    val isPreviewOnly: Boolean = false,
    val aspectRatio: String = "9:16",
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val outputAssetId: String? = null,
    val outputVideoUrl: String? = null,
    val previewVideoUrl: String? = null,
    val thumbnailUrl: String? = null,
    val fileSizeBytes: Long = 0L,
    val videoDurationMs: Long = 0L,
    val renderDurationMs: Long = 0L,
    val downloadUrlExpiry: Long? = null,
    val idempotencyKey: String = "",
    val logs: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val startedAt: Long? = null,
    val completedAt: Long? = null,
) {
    val isTerminal: Boolean
        get() =
            status == ProductionJobStatus.COMPLETED ||
                status == ProductionJobStatus.FAILED ||
                status == ProductionJobStatus.CANCELLED

    val canCancel: Boolean
        get() =
            status == ProductionJobStatus.QUEUED ||
                status == ProductionJobStatus.PROCESSING ||
                status == ProductionJobStatus.COMPOSING ||
                status == ProductionJobStatus.ENCODING ||
                status == ProductionJobStatus.RENDERING
}
