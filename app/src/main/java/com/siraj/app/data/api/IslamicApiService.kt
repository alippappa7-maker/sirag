package com.siraj.app.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * واجهة Quran.com API v4 — مجانية بالكامل، بدون مفتاح
 * توفر: نص القرآن، التفسير، البحث، الترجمات
 */
interface QuranApiService {

    /**
     * البحث في القرآن الكريم
     */
    @GET("https://api.quran.com/api/v4/search")
    suspend fun searchQuran(
        @Query("q") query: String,
        @Query("language") language: String = "ar",
        @Query("size") size: Int = 5,
    ): QuranSearchResponse

    /**
     * جلب تفسير آية معينة (ابن كثير)
     */
    @GET("https://api.qurancdn.com/api/v4/tafsirs/169/by_ayah/{verseKey}")
    suspend fun getTafsir(
        @Path("verseKey") verseKey: String,
        @Query("locale") locale: String = "ar",
    ): TafsirResponse
}

/**
 * واجهة UmmahAPI — مجانية بالكامل، بدون مفتاح
 * توفر: 36,000+ حديث من 10 مصادر
 */
interface HadithApiService {

    @GET("https://ummahapi.com/api/hadith/search")
    suspend fun searchHadith(
        @Query("q") query: String,
        @Query("limit") limit: Int = 5,
    ): HadithSearchResponse

    @GET("https://ummahapi.com/api/hadith/collections")
    suspend fun getCollections(): HadithCollectionsResponse
}

// ─── نماذج البيانات الجديدة (غير موجودة في QuranApi.kt) ───

@JsonClass(generateAdapter = true)
data class QuranSearchResponse(
    @Json(name = "results") val results: List<QuranSearchResult>? = null,
)

@JsonClass(generateAdapter = true)
data class QuranSearchResult(
    @Json(name = "verse_key") val verseKey: String,
    @Json(name = "text") val text: String,
    @Json(name = "translations") val translations: List<QuranTranslation>? = null,
)

@JsonClass(generateAdapter = true)
data class QuranTranslation(
    @Json(name = "text") val text: String,
    @Json(name = "resource_name") val resourceName: String,
)

@JsonClass(generateAdapter = true)
data class TafsirResponse(
    @Json(name = "tafsir") val tafsir: TafsirText? = null,
)

@JsonClass(generateAdapter = true)
data class TafsirText(
    @Json(name = "text") val text: String,
    @Json(name = "resource_name") val resourceName: String? = null,
)

@JsonClass(generateAdapter = true)
data class HadithSearchResponse(
    @Json(name = "results") val results: List<HadithResult>? = null,
)

@JsonClass(generateAdapter = true)
data class HadithResult(
    @Json(name = "collection") val collection: String? = null,
    @Json(name = "book") val book: String? = null,
    @Json(name = "number") val number: Int? = null,
    @Json(name = "arabicText") val arabicText: String? = null,
    @Json(name = "englishText") val englishText: String? = null,
    @Json(name = "grade") val grade: String? = null,
)

@JsonClass(generateAdapter = true)
data class HadithCollectionsResponse(
    @Json(name = "collections") val collections: List<HadithCollection>? = null,
)

@JsonClass(generateAdapter = true)
data class HadithCollection(
    @Json(name = "name") val name: String,
    @Json(name = "count") val count: Int,
)
