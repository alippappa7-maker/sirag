package com.siraj.app.domain.models.quran

data class Surah(
    val id: Int,
    val nameArabic: String,
    val versesCount: Int,
    val revelationPlace: String
)

data class Ayah(
    val id: Int,
    val verseKey: String,
    val textUthmani: String,
    val translation: String?,
    val tafsir: String?,
    val isBookmarked: Boolean = false,
    val note: String? = null
)
