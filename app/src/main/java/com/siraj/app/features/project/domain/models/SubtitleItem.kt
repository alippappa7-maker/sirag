package com.siraj.app.features.project.domain.models

enum class SubtitleSourceType(val displayName: String) {
    VOICEOVER_AUTO("توليد تلقائي من التعليق الصوتي"),
    SCENE_NARRATION("نص المشهد التلقائي"),
    MANUAL_USER("إدخال يدوي من المستخدم"),
    QURAN_SOURCE_LOCKED("نص قرآني موثق (مقفل)"),
    HADITH_SOURCE_LOCKED("نص حديث شريف موثق (مقفل)"),
    TRANSLATION_EN("ترجمة باللغة الإنجليزية (مراجعة مطلوبة)")
}

enum class SubtitlePosition(val displayName: String) {
    BOTTOM("أسفل الشاشة (افتراضي)"),
    MIDDLE("وسط الشاشة"),
    TOP("أعلى الشاشة")
}

enum class SubtitleFontFamily(val displayName: String) {
    SYSTEM_SANS("خط النظام الحديث (Sans)"),
    AMIRI_QURANIC("خط عثماني/أميري وقور"),
    KUFIC_MODERN("خط كوفي حديث"),
    NOTO_ARABIC("خط نسخ واضح ومريح")
}

data class SubtitleStyle(
    val fontFamily: SubtitleFontFamily = SubtitleFontFamily.SYSTEM_SANS,
    val fontSizeSp: Int = 18,
    val textColorHex: String = "#FFFFFF",
    val backgroundColorHex: String = "#80000000", // Semi-transparent black background box
    val position: SubtitlePosition = SubtitlePosition.BOTTOM,
    val isBold: Boolean = true,
    val hasOutline: Boolean = true,
    val outlineColorHex: String = "#000000",
    val maxWordsPerLine: Int = 8,
    val burnIntoVideo: Boolean = true // Burn-in (Hardsub) vs external (Softsub)
)

enum class SubtitleReviewStatus(val displayName: String) {
    NOT_REQUIRED("غير مطلوب (نص عادي)"),
    PENDING_REVIEW("بانتظار مراجعة المدقق اللغوي/الشرعي"),
    VERIFIED_LOCKED("معتمد ومقفل رسمياً"),
    REJECTED("مرفوض - يتطلب تعديل")
}

data class SubtitleItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val projectId: String = "",
    val sceneId: String = "",
    val language: String = "ar", // "ar", "en", etc.
    val text: String = "",
    val startMs: Long = 0L,
    val endMs: Long = 3000L,
    val style: SubtitleStyle = SubtitleStyle(),
    val sourceType: SubtitleSourceType = SubtitleSourceType.SCENE_NARRATION,
    val locked: Boolean = false, // Locked if it is verified Quran/Hadith source text
    val reviewStatus: SubtitleReviewStatus = SubtitleReviewStatus.NOT_REQUIRED,
    val reviewerNotes: String? = null,
    val sourceRefTitle: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
