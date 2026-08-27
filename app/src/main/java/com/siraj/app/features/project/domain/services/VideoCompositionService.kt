package com.siraj.app.features.project.domain.services

import com.siraj.app.features.project.domain.models.ProductionJob
import com.siraj.app.features.project.domain.models.ProductionQuality
import com.siraj.app.features.project.domain.models.VideoCompositionManifest
import kotlinx.coroutines.flow.Flow

interface VideoCompositionService {
    /**
     * بناء ملف تفويض التركيب (Manifest) بعد التحقق من المشاهد، الأصوات، الترجمة والتراخيص
     */
    suspend fun buildManifest(
        projectId: String,
        quality: ProductionQuality = ProductionQuality.FHD_1080P,
        aspectRatio: String = "9:16",
        burnSubtitles: Boolean = true,
        fps: Int = 30,
        includeSourceCitation: Boolean = true,
        includeWatermark: Boolean = true,
        isPreview: Boolean = false
    ): Result<VideoCompositionManifest>

    /**
     * تشغيل خط المعالجة الخادمي للتركيب والتصيير مع متابعة الحالات ونسبة التقدم
     */
    fun executeComposition(
        job: ProductionJob,
        manifest: VideoCompositionManifest
    ): Flow<ProductionJob>
}
