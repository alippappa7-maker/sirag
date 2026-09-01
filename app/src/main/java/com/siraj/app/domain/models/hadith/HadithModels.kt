package com.siraj.app.domain.models.hadith

/**
 * نماذج الحديث النبوي (Hadith Models)
 * كل حديث يجب أن يكون من مصدر محقق ودرجته معتمدة
 */

enum class HadithGrade(val arabicName: String, val color: Long) {
    SAHIH("صحيح", 0xFF0A7C66),
    HASAN("حسن", 0xFFC9A227),
    DAIF("ضعيف", 0xFFB3261E),
    UNKNOWN("غير محدد", 0xFF6A7A8A),
}

data class HadithCollection(
    val id: String,
    val name: String,           // صحيح البخاري، صحيح مسلم...
    val author: String,
    val totalHadiths: Int,
    val isVerified: Boolean = true,
)

data class HadithNarrator(
    val name: String,
    val rank: String?,         // تابعي، صحابي، إمام...
    val reliability: String?, // ثقة، صدوق، ضعيف...
)

data class Hadith(
    val id: String,
    val text: String,            // متن الحديث
    val narrator: String,        // الراوي
    val chain: List<String>,     // سند الإسناد
    val grade: HadithGrade,      // درجة الحديث
    val collectionId: String,    // مصدر الحديث (بخاري، مسلم...)
    val collectionName: String,
    val hadithNumber: Int,       // رقم الحديث في المصدر
    val chapter: String?,       // كتاب/باب الحديث
    val topicTags: List<String>, // تصنيف موضوعي
)

data class HadithSearchResult(
    val query: String,
    val results: List<Hadith>,
    val totalCount: Int,
)
