package com.siraj.app.data.repository

import com.siraj.app.data.api.CopilotApiRequest
import com.siraj.app.data.api.CopilotApiResponse
import com.siraj.app.data.api.ApiSource
import com.siraj.app.domain.models.copilot.*
import com.siraj.app.domain.repository.copilot.CopilotRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.UUID

/**
 * تنفيذ المساعد الإسلامي الذكي
 * 
 * يتصل بـ Firebase Cloud Function الذي:
 * 1. يبحث في Quran.com API عن الآيات
 * 2. يبحث في UmmahAPI عن الأحاديث
 * 3. يجلب التفسير من Quran.com
 * 4. يرسل للمصادر لـ Gemini API مع تعليمات صارمة
 * 5. يرجع الإجابة + المصادر الموثّقة
 * 
 * في حالة عدم توفر الاتصال بالـ backend، يرجع لقاعدة المعرفة المحلية.
 */
class CopilotRepositoryImpl : CopilotRepository {

    // قاعدة معرفة محلية احتياطية (offline fallback)
    private val localQuranSources = listOf(
        CopilotSource(
            type = CopilotSourceType.QURAN,
            title = "البقرة",
            reference = "2:153",
            excerpt = "يَا أَيُّهَا الَّذِينَ آمَنُوا اسْتَعِينُوا بِالصَّبْرِ وَالصَّلَاةِ ۚ إِنَّ اللَّهَ مَعَ الصَّابِرِينَ",
        ),
        CopilotSource(
            type = CopilotSourceType.QURAN,
            title = "الرعد",
            reference = "13:28",
            excerpt = "الَّذِينَ آمَنُوا وَتَطْمَئِنُّ قُلُوبُهُم بِذِكْرِ اللَّهِ ۗ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
        ),
        CopilotSource(
            type = CopilotSourceType.QURAN,
            title = "الزمر",
            reference = "39:53",
            excerpt = "قُلْ يَا عِبَادِيَ الَّذِينَ أَسْرَفُوا عَلَىٰ أَنفُسِهِمْ لَا تَقْنَطُوا مِن رَّحْمَةِ اللَّهِ ۚ إِنَّ اللَّهَ يَغْفِرُ الذُّنُوبَ جَمِيعًا",
        ),
    )

    private val localHadithSources = listOf(
        CopilotSource(
            type = CopilotSourceType.HADITH,
            title = "صحيح مسلم",
            reference = "2675",
            excerpt = "قال رسول الله ﷺ: «إِنَّ اللَّهَ تَعَالَى قَالَ: أَنَا عِنْدَ ظَنِّ عَبْدِي بِي»",
        ),
        CopilotSource(
            type = CopilotSourceType.HADITH,
            title = "صحيح البخاري",
            reference = "1",
            excerpt = "إنَّمَا الْأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى",
        ),
    )

    private val localDuaSources = listOf(
        CopilotSource(
            type = CopilotSourceType.DUA,
            title = "دعاء الاستخارة",
            reference = "صحيح البخاري",
            excerpt = "اللَّهُمَّ إِنِّي أَسْتَخِيرُكَ بِعِلْمِكَ، وَأَسْتَقْدِرُكَ بِقُدْرَتِكَ، وَأَسْأَلُكَ مِنْ فَضْلِكَ الْعَظِيمِ",
        ),
    )

    override suspend fun ask(query: CopilotQuery): Flow<CopilotResponse> = flow {
        // محاولة الاتصال بالـ backend الحقيقي
        try {
            val apiService = createApiService()
            val apiResponse = apiService.ask(
                CopilotApiRequest(
                    question = query.text,
                    language = query.language,
                    includeQuran = query.includeQuran,
                    includeHadith = query.includeHadith,
                    includeTafsir = query.includeTafsir,
                ),
            )

            if (apiResponse.error != null) {
                throw Exception(apiResponse.error)
            }

            val sources = apiResponse.sources.map { it.toDomain() }
            val response = CopilotResponse(
                answer = apiResponse.answer,
                sources = sources,
                confidence = apiResponse.confidence,
                followUpQuestions = apiResponse.followUpQuestions,
            )
            emit(response)
        } catch (e: Exception) {
            // fallback: استخدم قاعدة المعرفة المحلية
            delay(800)
            val sources = localSearch(query.text)
            val answer = buildLocalAnswer(query.text, sources)
            val followUps = generateFollowUps(query.text)

            emit(
                CopilotResponse(
                    answer = answer,
                    sources = sources,
                    confidence = 0.6f,
                    followUpQuestions = followUps,
                ),
            )
        }
    }

    /**
     * إنشاء خدمة Retrofit للاتصال بالـ Cloud Function
     */
    private fun createApiService(): com.siraj.app.data.api.CopilotApiService {
        return Retrofit.Builder()
            .baseUrl(com.siraj.app.data.api.CopilotApiService.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(com.siraj.app.data.api.CopilotApiService::class.java)
    }

    private fun localSearch(text: String): List<CopilotSource> {
        val sources = mutableListOf<CopilotSource>()
        val lower = text.lowercase()

        when {
            lower.contains("صبر") || lower.contains("patience") -> {
                sources.add(localQuranSources[0])
            }
            lower.contains("قلق") || lower.contains("طمأنين") || lower.contains("peace") -> {
                sources.add(localQuranSources[1])
            }
            lower.contains("مغفرة") || lower.contains("توبة") || lower.contains("forgiveness") -> {
                sources.add(localQuranSources[2])
            }
            lower.contains("نية") || lower.contains("intention") -> {
                sources.add(localHadithSources[1])
            }
            lower.contains("ظن") || lower.contains("رجاء") || lower.contains("hope") -> {
                sources.add(localHadithSources[0])
            }
            lower.contains("دعاء") || lower.contains("استخارة") || lower.contains("dua") -> {
                sources.add(localDuaSources[0])
            }
            else -> {
                sources.add(localQuranSources.first())
                sources.add(localHadithSources.first())
            }
        }
        return sources
    }

    private fun buildLocalAnswer(query: String, sources: List<CopilotSource>): String {
        if (sources.isEmpty()) return "لم أجد مصادر مناسبة لهذا السؤال. حاول إعادة صياغته."
        val builder = StringBuilder()
        builder.append("بناءً على المصادر الإسلامية الموثّقة:\n\n")
        sources.forEachIndexed { index, source ->
            builder.append("${index + 1}. ")
            when (source.type) {
                CopilotSourceType.QURAN -> {
                    builder.append("[${source.reference}] ${source.title}\n\"${source.excerpt}\"\n\n")
                }
                CopilotSourceType.HADITH -> {
                    builder.append("${source.title} — ${source.reference}\n\"${source.excerpt}\"\n\n")
                }
                CopilotSourceType.DUA -> {
                    builder.append("${source.title} — ${source.reference}\n\"${source.excerpt}\"\n\n")
                }
                else -> {
                    builder.append("${source.title}\n${source.excerpt}\n\n")
                }
            }
        }
        builder.append("هذا رد معرفي موثّق وليس فتوى. للاستفسارات الفقهية يُرجى الرجوع لأهل العلم.")
        return builder.toString()
    }

    private fun generateFollowUps(query: String): List<String> = when {
        query.contains("صبر") -> listOf("كيف أصبر على البلاء؟", "ما هو أجر الصابرين؟")
        query.contains("قلق") -> listOf("كيف أحقق الطمأنينة؟", "أذكار الصباح والمساء")
        query.contains("مغفرة") || query.contains("توبة") -> listOf("كيف أتوب؟", "شروط التوبة")
        else -> listOf("آيات عن الصبر", "أحاديث عن الرحمة", "أذكار الصباح")
    }

    override suspend fun semanticSearch(query: String, language: String): List<CopilotSource> {
        // TODO: استدعاء endpoint البحث الدلالي في الـ backend
        return localSearch(query)
    }

    override suspend fun saveConversation(conversation: CopilotConversation) {
        // TODO: حفظ في Firestore
    }

    override suspend fun getConversations(): List<CopilotConversation> = emptyList()

    override suspend fun getConversation(id: String): CopilotConversation? = null

    override suspend fun getSuggestedQuestions(): List<String> = listOf(
        "آيات عن الصبر",
        "كيف أتطهر؟",
        "ما هو أجر الصدقة؟",
        "دعاء الاستخارة",
        "آيات عن الرزق",
        "أحاديث عن الأخلاق",
        "كيف أحقق الطمأنينة؟",
        "ما هي شروط التوبة؟",
    )

    override suspend fun getSourcesByType(type: CopilotSourceType): List<CopilotSource> {
        return when (type) {
            CopilotSourceType.QURAN -> localQuranSources
            CopilotSourceType.HADITH -> localHadithSources
            CopilotSourceType.DUA -> localDuaSources
            else -> emptyList()
        }
    }
}

/**
 * تحويل من نموذج API إلى نموذج Domain
 */
private fun ApiSource.toDomain(): CopilotSource {
    val sourceType = when (type.lowercase()) {
        "quran" -> CopilotSourceType.QURAN
        "hadith" -> CopilotSourceType.HADITH
        "tafsir" -> CopilotSourceType.TAFSIR
        "dua" -> CopilotSourceType.DUA
        "fiqh" -> CopilotSourceType.FIQH
        else -> CopilotSourceType.QURAN
    }
    return CopilotSource(
        type = sourceType,
        title = title,
        reference = reference,
        excerpt = excerpt,
        url = url,
    )
}
