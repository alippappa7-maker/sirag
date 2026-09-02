package com.siraj.app.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * واجهة Gemini API المجانية
 * النموذج: gemini-2.0-flash (مجاني، 1,500 طلب يومياً)
 * لا يحتاج بطاقة ائتمانية
 */
interface GeminiApiService {

    /**
     * توليد محتوى من Gemini
     * النقطة: https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent
     */
    @POST("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest,
    ): GeminiResponse
}

// ─── نماذج الطلب ───

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null,
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>,
    @Json(name = "role") val role: String? = null,
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String,
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "temperature") val temperature: Double = 0.3,
    @Json(name = "topP") val topP: Double = 0.9,
    @Json(name = "topK") val topK: Int = 40,
    @Json(name = "maxOutputTokens") val maxOutputTokens: Int = 2048,
)

// ─── نماذج الرد ───

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null,
    @Json(name = "error") val error: GeminiError? = null,
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent? = null,
    @Json(name = "finishReason") val finishReason: String? = null,
)

@JsonClass(generateAdapter = true)
data class GeminiError(
    @Json(name = "code") val code: Int? = null,
    @Json(name = "message") val message: String? = null,
)
