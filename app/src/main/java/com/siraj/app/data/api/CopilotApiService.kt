package com.siraj.app.data.api

import com.siraj.app.domain.models.copilot.*
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * واجهة برمجة المساعد الإسلامي الذكي
 * تتصل بـ Firebase Cloud Function الذي يستدعي Gemini API
 */
interface CopilotApiService {

    /**
     * إرسال سؤال والحصول على رد بمصادر موثّقة
     */
    @POST("copilotAsk")
    suspend fun ask(@Body request: CopilotApiRequest): CopilotApiResponse

    companion object {
        // TODO: استبدل برابط Cloud Function الفعلي بعد النشر
        // يمكن استخدام Firebase Cloud Functions URL أو اسم الدالة المخصص
        const val BASE_URL = "https://us-central1-siraj-app.cloudfunctions.net/"
    }
}

/**
 * طلب الإرسال
 */
data class CopilotApiRequest(
    val question: String,
    val language: String = "ar",
    val includeQuran: Boolean = true,
    val includeHadith: Boolean = true,
    val includeTafsir: Boolean = true,
)

/**
 * الرد من الـ backend
 */
data class CopilotApiResponse(
    val answer: String,
    val sources: List<ApiSource>,
    val confidence: Float,
    val followUpQuestions: List<String>,
    val disclaimer: String? = null,
    val error: String? = null,
)

data class ApiSource(
    val type: String,      // quran, hadith, tafsir, dua, fiqh
    val title: String,
    val reference: String,
    val excerpt: String,
    val url: String? = null,
)
