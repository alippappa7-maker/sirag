package com.siraj.app.domain.models

import java.util.UUID

enum class AiImageStyle(val displayName: String, val promptSuffix: String) {
    PHOTOREALISTIC("فوتوغرافي واقعي", "photorealistic, ultra high detail, 8k resolution, cinematic lighting"),
    ISLAMIC_ART("فن وزخرفة إسلامية", "islamic art style, arabesque patterns, intricate geometric details, golden illumination"),
    CINEMATIC("سينمائي درامي", "cinematic atmosphere, dramatic lighting, depth of field, masterpiece"),
    MINIMALIST("بسيط ومعاصر (Minimalist)", "minimalist design, clean background, soft lighting, modern aesthetic"),
    THREE_D_RENDER("تصيير ثلاثي الأبعاد (3D)", "3d render, octane render, smooth textures, volumetric light"),
    CALLIGRAPHY_BACKGROUND("خلفية خط عربي", "arabic calligraphy background, elegant typography textures, subtle ambient lighting")
}

enum class AiImageAspectRatio(val displayName: String, val ratioString: String, val width: Int, val height: Int) {
    RATIO_16_9("أفقي (16:9)", "16:9", 1920, 1080),
    RATIO_9_16("رأسي (9:16)", "9:16", 1080, 1920),
    RATIO_1_1("مربع (1:1)", "1:1", 1024, 1024),
    RATIO_4_5("منشورات (4:5)", "4:5", 1080, 1350)
}

enum class AiImageStatus {
    IDLE,
    QUEUED,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED
}

data class AiImageGenerationRequest(
    val requestId: String = "img_req_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}",
    val projectId: String,
    val sceneId: String? = null,
    val promptText: String,
    val negativePrompt: String? = null,
    val style: AiImageStyle = AiImageStyle.ISLAMIC_ART,
    val aspectRatio: AiImageAspectRatio = AiImageAspectRatio.RATIO_16_9,
    val count: Int = 1,
    val seed: Long? = null,
    val model: String = "imagen-3.0-generate-002",
    val provider: String = "Google Cloud Vertex AI",
    val costUnits: Int = 2
)

data class GeneratedImageItem(
    val id: String = UUID.randomUUID().toString(),
    val requestId: String,
    val projectId: String,
    val sceneId: String? = null,
    val imageUrl: String,
    val thumbnailUrl: String,
    val promptText: String,
    val negativePrompt: String? = null,
    val style: AiImageStyle = AiImageStyle.ISLAMIC_ART,
    val model: String = "imagen-3.0-generate-002",
    val provider: String = "Google Cloud Vertex AI",
    val width: Int,
    val height: Int,
    val seed: Long? = null,
    val status: AiImageStatus = AiImageStatus.COMPLETED,
    val costUnits: Int = 2,
    val sourceType: String = "ai_generated",
    val generatedAt: Long = System.currentTimeMillis(),
    val errorMessage: String? = null,
    val isAiGenerated: Boolean = true,
    val licenseNotice: String = "مولد بالذكاء الاصطناعي - لا يعتبر دليلاً شرعياً"
)

object IslamicPromptSafetyValidator {
    // Prohibited words and concepts strictly forbidden in image generation for Islamic compliance
    private val PROHIBITED_KEYWORDS = listOf(
        "نبي", "الرسول", "محمد صلى الله عليه وسلم", "عيسى", "موسى", "إبراهيم", "نوح",
        "صحابي", "أبو بكر", "عمر بن الخطاب", "عثمان بن عفان", "علي بن أبي طالب",
        "الله", "الرب", "الخالق", "تجسيد الذات الإلهية", "ملاك", "جبريل", "ميكائيل", "ملك الموت",
        "prophet", "muhammad", "jesus", "moses", "abraham", "angel gabriel", "god depiction"
    )

    data class ValidationResult(
        val isAllowed: Boolean,
        val reason: String? = null
    )

    fun validatePrompt(prompt: String): ValidationResult {
        val normalized = prompt.lowercase().trim()
        if (normalized.isBlank()) {
            return ValidationResult(false, "يرجى كتابة وصف للصورة.")
        }
        
        for (keyword in PROHIBITED_KEYWORDS) {
            if (normalized.contains(keyword.lowercase())) {
                return ValidationResult(
                    false,
                    "تنبيه شرعي صارم: يُمنع قطعيًا استخدام الذكاء الاصطناعي لتصوير الأنبياء أو الرسل أو الصحابة أو الملائكة أو الذات الإلهية."
                )
            }
        }
        return ValidationResult(true)
    }
}
