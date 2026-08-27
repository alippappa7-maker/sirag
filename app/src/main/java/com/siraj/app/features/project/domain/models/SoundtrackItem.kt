package com.siraj.app.features.project.domain.models

enum class SoundtrackCategory(val displayName: String, val description: String) {
    NATURE_AMBIENCE("أصوات طبيعية وبيئية", "رياح، أمطار، خرير ماء، طيور، صحراء، غابة"),
    SOUND_EFFECTS("مؤثرات صوتية (SFX)", "نقر، حركة، انتقال، كتابة، ورق، بيب، رنين"),
    DOCUMENTARY_ATMOSPHERE("أجواء وثائقية هادئة (بدون معازف)", "همهمات هادئة، دقات إيقاعية خفيفة، طبقات صوتية تأملية"),
    SPIRITUAL_ACOUSTIC("أجواء إيمانية وتأملية", "مؤثرات صوتية وقورة ملائمة للمحتوى الهادف والقصص"),
    NASHEED_VOCAL("أناشيد صوتية بشرية (فوكالز)", "أصوات بشرية خالية من الآلات الموسيقية"),
    BACKGROUND_MUSIC("موسيقى خلفية مرخصة", "ألحان وموسيقى تصويرية مرخصة ومفتوحة المصدر")
}

enum class SoundLicenseType(val displayName: String, val requiresAttribution: Boolean, val commercialAllowed: Boolean) {
    CC0_PUBLIC_DOMAIN("CC0 - ملكية عامة", false, true),
    CC_BY_4_0("CC-BY 4.0 - تتطلب نسبة المؤلف", true, true),
    CC_BY_NC("CC-BY-NC - استخدام غير تجاري", true, false),
    PIXABAY_AUDIO_LICENSE("رخصة Pixabay للمؤثرات", false, true),
    FREE_SOUND_LICENSE("رخصة Freesound المفتوحة", true, true),
    CUSTOM_SIRAJ_LICENSED("مرخص ومملوك لمنصة سراج", false, true)
}

data class SoundtrackItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val category: SoundtrackCategory = SoundtrackCategory.NATURE_AMBIENCE,
    val audioUrl: String = "",
    val durationMs: Long = 0L,
    val isMusic: Boolean = false, // false for Nature/SFX/Vocals, true for actual music
    val tags: List<String> = emptyList(),
    val authorOrCreator: String = "",
    val provider: String = "Siraj Library",
    val licenseType: SoundLicenseType = SoundLicenseType.CC0_PUBLIC_DOMAIN,
    val attributionText: String = "",
    val sourceUrl: String = "",
    val usageRestrictions: String = "مسموح للاستخدام في مشاريع سراج المصدرة",
    val defaultVolume: Float = 0.5f
)

data class SceneAudioTrackConfig(
    val audioId: String = "",
    val soundTitle: String = "",
    val soundUrl: String = "",
    val category: SoundtrackCategory = SoundtrackCategory.NATURE_AMBIENCE,
    val isMusic: Boolean = false,
    val volume: Float = 0.5f,
    val loop: Boolean = false,
    val fadeIn: Boolean = true,
    val fadeOut: Boolean = true,
    val fadeInDurationMs: Long = 1000L,
    val fadeOutDurationMs: Long = 1000L,
    val startTrimMs: Long = 0L,
    val endTrimMs: Long = 0L,
    val effectiveDurationMs: Long = 0L,
    val attributionRequired: Boolean = false,
    val attributionText: String = "",
    val licenseDisplayName: String = ""
)
