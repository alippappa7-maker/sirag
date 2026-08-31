package com.siraj.app.features.project.domain.models

enum class AudioSourceType(
    val displayName: String,
    val isAiGenerated: Boolean,
) {
    GENERATED_VOICE("صوت مولد آلياً (AI)", true),
    USER_RECORDING("تسجيل صوتي للمستخدم", false),
    LICENSED_AUDIO("مؤثر أو صوت خارجي مرخص", false),
    QURAN_RECITATION("تلاوة قرآنية موثقة", false),
}

enum class AudioVoiceGender(
    val displayName: String,
) {
    MALE("صوت رجالي"),
    FEMALE("صوت نسائي"),
}

data class VoiceOption(
    val id: String,
    val name: String,
    val gender: AudioVoiceGender,
    val dialect: String,
    val description: String,
    val previewSampleUrl: String = "",
)

enum class AudioLanguage(
    val code: String,
    val displayName: String,
) {
    ARABIC_MODERN_STANDARD("ar-SA", "العربية الفصحى (تشكيل متوازن)"),
    ARABIC_CLASSICAL("ar-XA", "العربية الفصحى التراثية"),
    ARABIC_EGYPTIAN("ar-EG", "العربية (لهجة مصرية وثائقية)"),
    ARABIC_GULF("ar-AE", "العربية (لهجة خليجية وثائقية)"),
    ARABIC_LEVANTINE("ar-SY", "العربية (لهجة شامية وثائقية)"),
}

data class AudioItem(
    val id: String =
        java.util.UUID
            .randomUUID()
            .toString(),
    val projectId: String = "",
    val sceneId: String? = null,
    val title: String = "",
    val textContent: String = "",
    val audioUrl: String = "",
    val storagePath: String = "",
    val sourceType: AudioSourceType = AudioSourceType.GENERATED_VOICE,
    val voiceId: String = "ar-male-1",
    val voiceName: String = "راشد (فصيح وقور)",
    val languageCode: String = "ar-SA",
    val speed: Float = 1.0f,
    val pitch: Float = 1.0f,
    val startTrimMs: Long = 0L,
    val endTrimMs: Long = 0L,
    val originalDurationMs: Long = 0L,
    val trimmedDurationMs: Long = 0L,
    val fileSize: Long = 0L,
    val mimeType: String = "audio/mpeg",
    val isAiGenerated: Boolean = true,
    val licenseNotice: String = "مولد بالذكاء الاصطناعي - لا يعتبر تلاوة أو فتوى شرعية",
    val licenseProofUrl: String? = null,
    val reciterName: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

data class GenerateAudioRequest(
    val requestId: String =
        java.util.UUID
            .randomUUID()
            .toString(),
    val projectId: String,
    val sceneId: String? = null,
    val text: String,
    val language: String = "ar-SA",
    val voiceId: String = "ar-male-1",
    val speed: Float = 1.0f,
    val pitch: Float = 1.0f,
    val costUnits: Int = 1,
)

sealed class AudioGenerationUiState {
    object Idle : AudioGenerationUiState()

    data class Generating(
        val progressMessage: String,
    ) : AudioGenerationUiState()

    data class Success(
        val audioItem: AudioItem,
    ) : AudioGenerationUiState()

    data class Error(
        val message: String,
    ) : AudioGenerationUiState()
}

sealed class AudioUploadUiState {
    object Idle : AudioUploadUiState()

    data class Uploading(
        val progress: Float,
    ) : AudioUploadUiState()

    data class Success(
        val audioItem: AudioItem,
    ) : AudioUploadUiState()

    data class Error(
        val message: String,
    ) : AudioUploadUiState()
}
