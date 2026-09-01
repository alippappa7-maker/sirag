package com.siraj.app.domain.models.tafsir

/**
 * نماذج التفسير (Tafsir Models)
 * تفريق صارم بين نص القرآن والتفسير — لا يمكن خلطهما برمجياً
 */

data class TafsirEdition(
    val id: String,
    val name: String,          // اسم المفسر: ابن كثير، السعدي، الميسر
    val author: String,        // اسم المؤلف الكامل
    val source: String,        // المصدر: مجمع الملك فهد، إسلام ويب
    val isVerified: Boolean,   // معتمد من لجنة شرعية
    val language: String = "ar",
)

data class TafsirVerse(
    val surahNumber: Int,
    val ayahNumber: Int,
    val ayahText: String,      // نص الآية (قرآني — غير قابل للتعديل)
    val tafsirText: String,    // نص التفسير
    val editionId: String,    // معرف المفسر
    val editionName: String,   // اسم المفسر
    val contextReason: String? = null,  // سبب النزول إن وُجد
)

data class TafsirSurah(
    val number: Int,
    val name: String,
    val ayahCount: Int,
    val revelationType: String,  // "مكية" أو "مدنية"
)

data class TafsirSearchResult(
    val query: String,
    val results: List<TafsirVerse>,
    val totalCount: Int,
)
