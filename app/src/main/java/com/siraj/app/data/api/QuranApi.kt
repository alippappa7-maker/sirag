package com.siraj.app.data.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChaptersResponse(
    @Json(name = "chapters") val chapters: List<ApiChapter>
)

@JsonClass(generateAdapter = true)
data class ApiChapter(
    @Json(name = "id") val id: Int,
    @Json(name = "revelation_place") val revelationPlace: String,
    @Json(name = "revelation_order") val revelationOrder: Int,
    @Json(name = "bismillah_pre") val bismillahPre: Boolean,
    @Json(name = "name_simple") val nameSimple: String,
    @Json(name = "name_arabic") val nameArabic: String,
    @Json(name = "verses_count") val versesCount: Int
)

@JsonClass(generateAdapter = true)
data class VersesResponse(
    @Json(name = "verses") val verses: List<ApiVerse>
)

@JsonClass(generateAdapter = true)
data class ApiVerse(
    @Json(name = "id") val id: Int,
    @Json(name = "verse_key") val verseKey: String,
    @Json(name = "text_uthmani") val textUthmani: String?,
    @Json(name = "translations") val translations: List<ApiTranslation>?,
    @Json(name = "tafsirs") val tafsirs: List<ApiTafsir>?
)

@JsonClass(generateAdapter = true)
data class ApiTranslation(
    @Json(name = "id") val id: Int,
    @Json(name = "resource_id") val resourceId: Int,
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class ApiTafsir(
    @Json(name = "id") val id: Int,
    @Json(name = "resource_id") val resourceId: Int,
    @Json(name = "text") val text: String
)

interface QuranApi {
    @GET("chapters?language=ar")
    suspend fun getChapters(): ChaptersResponse
    
    @GET("verses/by_chapter/{chapter_id}?language=ar&fields=text_uthmani")
    suspend fun getVersesByChapter(
        @Path("chapter_id") chapterId: Int,
        @Query("translations") translations: String? = null,
        @Query("tafsirs") tafsirs: String? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 300 // Get all verses for the surah in one go for most surahs
    ): VersesResponse
}
