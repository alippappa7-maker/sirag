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
     * الرد: { search: { results: [...] } }
     */
    @GET("https://api.quran.com/api/v4/search")
    suspend fun searchQuran(
        @Query("q") query: String,
        @Query("language") language: String = "ar",
        @Query("size") size: Int = 5,
    ): QuranSearchResponse

    /**
     * جلب تفسير آية معينة
     * resource 169 = Ibn Kathir (English), 171 = Tafsir al-Jalalayn
     */
    @GET("https://api.qurancdn.com/api/v4/tafsirs/169/by_ayah/{verseKey}")
    suspend fun getTafsir(
        @Path("verseKey") verseKey: String,
        @Query("locale") locale: String = "en",
    ): TafsirResponse
}

/**
 * واجهة UmmahAPI — مجانية بالكامل، بدون مفتاح
 * توفر: 36,000+ حديث من 10 مصادر
 */
interface HadithApiService {

    /**
     * البحث في الأحاديث
     * الرد: { success: true, data: { hadiths: [...] } }
     */
    @GET("https://ummahapi.com/api/hadith/search")
    suspend fun searchHadith(
        @Query("q") query: String,
        @Query("limit") limit: Int = 5,
    ): HadithSearchResponse

    @GET("https://ummahapi.com/api/hadith/collections")
    suspend fun getCollections(): HadithCollectionsResponse
}

// ─── نماذج Quran.com ───

@JsonClass(generateAdapter = true)
data class QuranSearchResponse(
    @Json(name = "search") val search: QuranSearchData? = null,
)

@JsonClass(generateAdapter = true)
data class QuranSearchData(
    @Json(name = "query") val query: String? = null,
    @Json(name = "total_results") val totalResults: Int? = null,
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
    @Json(name = "name") val name: String? = null,
    @Json(name = "resource_id") val resourceId: Int? = null,
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

// ─── نماذج UmmahAPI ───

@JsonClass(generateAdapter = true)
data class HadithSearchResponse(
    @Json(name = "success") val success: Boolean? = null,
    @Json(name = "data") val data: HadithSearchData? = null,
)

@JsonClass(generateAdapter = true)
data class HadithSearchData(
    @Json(name = "query") val query: String? = null,
    @Json(name = "total_found") val totalFound: Int? = null,
    @Json(name = "hadiths") val hadiths: List<HadithResult>? = null,
)

@JsonClass(generateAdapter = true)
data class HadithResult(
    @Json(name = "id") val id: String? = null,
    @Json(name = "collection") val collection: String? = null,
    @Json(name = "collection_name") val collectionName: String? = null,
    @Json(name = "hadithnumber") val hadithNumber: Int? = null,
    @Json(name = "arabic") val arabic: String? = null,
    @Json(name = "english") val english: String? = null,
    @Json(name = "grade") val grade: String? = null,
)

@JsonClass(generateAdapter = true)
data class HadithCollectionsResponse(
    @Json(name = "data") val data: List<HadithCollection>? = null,
)

@JsonClass(generateAdapter = true)
data class HadithCollection(
    @Json(name = "name") val name: String,
    @Json(name = "count") val count: Int,
)
