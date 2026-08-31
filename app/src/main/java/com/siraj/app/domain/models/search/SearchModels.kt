package com.siraj.app.domain.models.search

import java.util.UUID

enum class SearchCategory(
    val titleArabic: String,
    val englishName: String,
) {
    ALL("الكل", "All"),
    QURAN("القرآن الكريم", "Quran"),
    AUDIO("المكتبة الصوتية", "Audio"),
    FLASH("الومضات", "Flashes"),
    PROJECT("مشاريعي", "Projects"),
    TEMPLATE("القوالب", "Templates"),
    SOURCE("المصادر والمراجع", "Sources"),
}

enum class SearchLanguage(
    val titleArabic: String,
    val code: String,
) {
    ALL("جميع اللغات", "all"),
    ARABIC("العربية", "ar"),
    ENGLISH("الإنجليزية", "en"),
    URDU("الأردية", "ur"),
    FRENCH("الفرنسية", "fr"),
    OTHER("أخرى", "other"),
}

enum class SearchContentType(
    val titleArabic: String,
) {
    ALL("جميع الأنواع"),
    TEXT("نصوص وتفاسير"),
    AUDIO("صوتيات وتلاوات"),
    VIDEO("فيديوهات ومرئيات"),
    PROJECT("مشاريع عمل"),
    TEMPLATE("قوالب إنتاج"),
    REFERENCE("مراجع وأحاديث"),
}

enum class SearchVerificationFilter(
    val titleArabic: String,
) {
    ALL_APPROVED("المعتمد والموثق"),
    VERIFIED_ONLY("المراجع الموثقة فقط (Verified)"),
}

enum class SearchSortOption(
    val titleArabic: String,
) {
    RELEVANCE("الأعلى صلة"),
    NEWEST("الأحدث أولاً"),
    POPULAR("الأكثر استماعاً وتداولاً"),
}

data class SearchFilter(
    val category: SearchCategory = SearchCategory.ALL,
    val language: SearchLanguage = SearchLanguage.ALL,
    val contentType: SearchContentType = SearchContentType.ALL,
    val verificationFilter: SearchVerificationFilter = SearchVerificationFilter.ALL_APPROVED,
    val sortOption: SearchSortOption = SearchSortOption.RELEVANCE,
    val onlyPrivateProjects: Boolean = false,
)

data class SearchResultItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val snippet: String = "",
    val category: SearchCategory,
    val sourceName: String? = null,
    val authorOrReciter: String? = null,
    val referenceUrl: String? = null,
    val verificationStatus: String? = null,
    val isVerified: Boolean = true,
    val isPrivate: Boolean = false,
    val language: String = "العربية",
    val timestamp: Long = System.currentTimeMillis(),
    val targetRoute: String = "",
    val durationText: String? = null,
    val extraMetadata: Map<String, String> = emptyMap(),
)

data class GlobalSearchResult(
    val query: String = "",
    val totalCount: Int = 0,
    val items: List<SearchResultItem> = emptyList(),
    val categoryCounts: Map<SearchCategory, Int> = emptyMap(),
    val page: Int = 1,
    val hasMore: Boolean = false,
)

data class SearchHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val query: String,
    val category: SearchCategory = SearchCategory.ALL,
    val timestamp: Long = System.currentTimeMillis(),
    val resultCount: Int = 0,
    val userId: String? = null,
)

data class SearchSuggestion(
    val text: String,
    val category: SearchCategory = SearchCategory.ALL,
    val isHistory: Boolean = false,
)
