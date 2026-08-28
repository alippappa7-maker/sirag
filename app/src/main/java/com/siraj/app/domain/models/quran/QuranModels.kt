package com.siraj.app.domain.models.quran

data class Surah(
    val chapterNumber: Int,
    val nameArabic: String,
    val nameTranslated: String,
    val versesCount: Int,
    val revelationPlace: String
)

data class Ayah(
    val verseKey: String, // e.g., "1:1"
    val chapterNumber: Int,
    val verseNumber: Int,
    val textUthmani: String,
    val translation: AyahTranslation?,
    val tafsir: AyahTafsir?,
    val audio: AyahAudio?,
    val isBookmarked: Boolean = false,
    val note: String? = null
)

data class AyahTranslation(
    val text: String,
    val resourceName: String, // e.g., "Clear Quran", "Saheeh International"
    val language: String
)

data class AyahTafsir(
    val text: String,
    val resourceName: String, // e.g., "تفسير ابن كثير", "Tafsir Al-Jalalayn"
    val language: String
)

data class AyahAudio(
    val url: String,
    val reciterName: String,
    val reciterStyle: String? = null // e.g., "Murattal"
)

data class QuranReaderSettings(
    val fontSize: Float = 24f,
    val isNightMode: Boolean = false,
    val selectedReciterId: Int = 1,
    val selectedTranslationId: Int = 1,
    val selectedTafsirId: Int = 1,
    val playbackSpeed: Float = 1.0f
)
