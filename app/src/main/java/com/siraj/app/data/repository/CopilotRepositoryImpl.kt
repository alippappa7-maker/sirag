package com.siraj.app.data.repository

import com.siraj.app.data.api.*
import com.siraj.app.data.api.GeminiContent
import com.siraj.app.data.api.GeminiGenerationConfig
import com.siraj.app.data.api.GeminiPart
import com.siraj.app.data.api.GeminiRequest
import com.siraj.app.data.api.GeminiResponse
import com.siraj.app.domain.models.copilot.*
import com.siraj.app.domain.repository.copilot.CopilotRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * المساعد الإسلامي الذكي — مجاني بالكامل
 *
 * المعمارية:
 * 1. يبحث في Quran.com API عن الآيات القرآنية
 * 2. يبحث في UmmahAPI عن الأحاديث النبوية
 * 3. يجلب تفسير ابن كثير من Quran.com
 * 4. يرسل المصادر كلها لـ Gemini API (مجاني) مع تعليمات صارمة
 * 5. يرجع الإجابة الذكية + المصادر الموثّقة
 */
class CopilotRepositoryImpl : CopilotRepository {

    private val quranApi: QuranApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.quran.com/api/v4/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(QuranApiService::class.java)
    }

    private val hadithApi: HadithApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://ummahapi.com/api/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(HadithApiService::class.java)
    }

    private val geminiApi: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }

    // مفتاح Gemini API — يُدار عبر لوحة الأسرار أو .env
    var geminiApiKey: String = ""

    private val surahNames = mapOf(
        1 to "الفاتحة", 2 to "البقرة", 3 to "آل عمران", 4 to "النساء",
        5 to "المائدة", 6 to "الأنعام", 7 to "الأعراف", 8 to "الأنفال",
        9 to "التوبة", 10 to "يونس", 11 to "هود", 12 to "يوسف",
        13 to "الرعد", 14 to "إبراهيم", 15 to "الحجر", 16 to "النحل",
        17 to "الإسراء", 18 to "الكهف", 19 to "مريم", 20 to "طه",
        21 to "الأنبياء", 22 to "الحج", 23 to "المؤمنون", 24 to "النور",
        25 to "الفرقان", 26 to "الشعراء", 27 to "النمل", 28 to "القصص",
        29 to "العنكبوت", 30 to "الروم", 31 to "لقمان", 32 to "السجدة",
        33 to "الأحزاب", 34 to "سبأ", 35 to "فاطر", 36 to "يس",
        37 to "الصافات", 38 to "ص", 39 to "الزمر", 40 to "غافر",
        41 to "فصلت", 42 to "الشورى", 43 to "الزخرف", 44 to "الدخان",
        45 to "الجاثية", 46 to "الأحقاف", 47 to "محمد", 48 to "الفتح",
        49 to "الحجرات", 50 to "ق", 51 to "الذاريات", 52 to "الطور",
        53 to "النجم", 54 to "القمر", 55 to "الرحمن", 56 to "الواقعة",
        57 to "الحديد", 58 to "المجادلة", 59 to "الحشر", 60 to "الممتحنة",
        61 to "الصف", 62 to "الجمعة", 63 to "المنافقون", 64 to "التغابن",
        65 to "الطلاق", 66 to "التحريم", 67 to "الملك", 68 to "القلم",
        69 to "الحاقة", 70 to "المعارج", 71 to "نوح", 72 to "الجن",
        73 to "المزمل", 74 to "المدثر", 75 to "القيامة", 76 to "الإنسان",
        77 to "المرسلات", 78 to "النبأ", 79 to "النازعات", 80 to "عبس",
        81 to "التكوير", 82 to "الانفطار", 83 to "المطففين", 84 to "الانشقاق",
        85 to "البروج", 86 to "الطارق", 87 to "الأعلى", 88 to "الغاشية",
        89 to "الفجر", 90 to "البلد", 91 to "الشمس", 92 to "الليل",
        93 to "الضحى", 94 to "الشرح", 95 to "التين", 96 to "العلق",
        97 to "القدر", 98 to "البينة", 99 to "الزلزلة", 100 to "العاديات",
        101 to "القارعة", 102 to "التكاثر", 103 to "العصر", 104 to "الهمزة",
        105 to "الفيل", 106 to "قريش", 107 to "الماعون", 108 to "الكوثر",
        109 to "الكافرون", 110 to "النصر", 111 to "المسد", 112 to "الإخلاص",
        113 to "الفلق", 114 to "الناس",
    )

    override suspend fun ask(query: CopilotQuery): Flow<CopilotResponse> = flow {
        val sources = mutableListOf<CopilotSource>()

        // 1. ابحث في القرآن الكريم عبر Quran.com API
        if (query.includeQuran) {
            try {
                val searchQuery = extractSearchTerms(query.text)
                val quranResults = quranApi.searchQuran(searchQuery, query.language, 5)
                quranResults.search?.results?.forEach { result ->
                    val surahNum = result.verseKey.split(":").firstOrNull()?.toIntOrNull()
                    val surahName = surahNames[surahNum ?: 0] ?: "سورة"
                    val translation = result.translations?.firstOrNull()?.text ?: ""

                    sources.add(
                        CopilotSource(
                            type = CopilotSourceType.QURAN,
                            title = surahName,
                            reference = result.verseKey,
                            excerpt = result.text,
                            url = "https://quran.com/${result.verseKey}",
                        ),
                    )
                }
            } catch (e: Exception) {
                // تجاهل الأخطاء، استمر للمصادر الأخرى
            }
        }

        // 2. ابحث في الحديث النبوي عبر UmmahAPI
        if (query.includeHadith) {
            try {
                val hadithQuery = extractSearchTerms(query.text)
                val hadithResults = hadithApi.searchHadith(hadithQuery, 5)
                hadithResults.data?.hadiths?.forEach { hadith ->
                    sources.add(
                        CopilotSource(
                            type = CopilotSourceType.HADITH,
                            title = hadith.collectionName ?: hadith.collection ?: "حديث",
                            reference = "رقم ${hadith.hadithNumber ?: ""}",
                            excerpt = if (query.language == "ar") {
                                hadith.arabic ?: hadith.english ?: ""
                            } else {
                                hadith.english ?: hadith.arabic ?: ""
                            },
                            url = "https://sunnah.com/${hadith.collection}/${hadith.hadithNumber}",
                        ),
                    )
                }
            } catch (e: Exception) {
                // تجاهل الأخطاء
            }
        }

        // 3. اجلب التفسير لأول آية
        if (query.includeTafsir && sources.any { it.type == CopilotSourceType.QURAN }) {
            try {
                val firstQuran = sources.first { it.type == CopilotSourceType.QURAN }
                val tafsirResult = quranApi.getTafsir(firstQuran.reference, "en")
                tafsirResult.tafsir?.let { tafsir ->
                    sources.add(
                        CopilotSource(
                            type = CopilotSourceType.TAFSIR,
                            title = tafsir.resourceName ?: "تفسير ابن كثير",
                            reference = firstQuran.reference,
                            excerpt = tafsir.text.take(500),
                            url = "https://quran.com/${firstQuran.reference}/tafsirs",
                        ),
                    )
                }
            } catch (e: Exception) {
                // تجاهل
            }
        }

        // 4. أرسل المصادر لـ Gemini لتوليد إجابة ذكية
        val answer = if (geminiApiKey.isNotBlank() && sources.isNotEmpty()) {
            generateGeminiAnswer(query.text, sources, query.language)
        } else {
            buildLocalAnswer(query.text, sources)
        }

        val followUps = generateFollowUps(query.text)

        emit(
            CopilotResponse(
                answer = answer,
                sources = sources,
                confidence = if (sources.isNotEmpty()) 0.9f else 0.3f,
                followUpQuestions = followUps,
            ),
        )
    }

    /**
     * إرسال المصادر لـ Gemini مع تعليمات صارمة
     */
    private suspend fun generateGeminiAnswer(
        question: String,
        sources: List<CopilotSource>,
        language: String,
    ): String {
        val systemPrompt = if (language == "ar") {
            """أنت مساعد إسلامي معرفي موثّق. مهمتك:
1. أجب فقط من المصادر المرفقة. لا تخترع آيات أو أحاديث.
2. إذا لم تكفِ المصادر للإجابة، قل ذلك بوضوح.
3. اذكر المصدر مع كل معلومة (الآية، الحديث، التفسير).
4. لا تقدم فتاوى. للأسئلة الفقهية، وجّه المستخدم لأهل العلم.
5. أجب بالعربية الفصحى المبسّطة.
6. كن موجزاً وواضحاً.
7. في النهاية أضف: "هذا رد معرفي موثّق وليس فتوى. للاستفسارات الفقهية يُرجى الرجوع لأهل العلم." """.trimIndent()
        } else {
            """You are a verified Islamic knowledge assistant. Your task:
1. Answer ONLY from the provided sources. Do not invent verses or hadiths.
2. If sources are insufficient, say so clearly.
3. Cite the source with each piece of information.
4. Do not issue fatwas. For fiqh questions, direct users to scholars.
5. Be concise and clear.
6. End with: "This is a documented knowledge response, not a fatwa." """.trimIndent()
        }

        val sourcesContext = sources.mapIndexed { i, s ->
            val typeLabel = when (s.type) {
                CopilotSourceType.QURAN -> "آية قرآنية"
                CopilotSourceType.HADITH -> "حديث نبوي"
                CopilotSourceType.TAFSIR -> "تفسير"
                CopilotSourceType.DUA -> "دعاء"
                CopilotSourceType.FIQH -> "فقه"
            }
            "[${i + 1}] $typeLabel — ${s.title} (${s.reference})\nالنص: ${s.excerpt}"
        }.joinToString("\n\n")

        val userPrompt = if (language == "ar") {
            """سؤال المستخدم: $question

المصادر الإسلامية الموثّقة المتاحة:
$sourcesContext

أجب من هذه المصادر فقط."""
        } else {
            """User question: $question

Available verified Islamic sources:
$sourcesContext

Answer from these sources only."""
        }

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = userPrompt))),
            ),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.3,
                topP = 0.9,
                topK = 40,
                maxOutputTokens = 2048,
            ),
        )

        return try {
            val response: GeminiResponse = geminiApi.generateContent(geminiApiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: buildLocalAnswer(question, sources)
        } catch (e: Exception) {
            buildLocalAnswer(question, sources)
        }
    }

    private fun extractSearchTerms(text: String): String {
        val stopWords = setOf(
            "ما", "ماذا", "كيف", "هل", "من", "أين", "متى", "لماذا",
            "كم", "أي", "في", "عن", "على", "مع", "هو", "هي", "أن",
            "إن", "كان", "التي", "الذي", "هذا", "هذه", "ذلك", "تلك",
            "و", "ال", "أو", "ف", "ثم", "ب", "ل", "ك",
            "what", "how", "is", "the", "in", "on", "about", "are",
            "when", "where", "why", "who", "which",
        )
        return text.split(" ", "،", ",", "؟", "?", "\n")
            .map { it.trim() }
            .filter { it.isNotBlank() && it !in stopWords && it.length > 1 }
            .joinToString(" ")
            .ifBlank { text }
    }

    private fun buildLocalAnswer(query: String, sources: List<CopilotSource>): String {
        if (sources.isEmpty()) {
            return "لم أجد مصادر مرتبطة بهذا السؤال.\n\n" +
                   "حاول إعادة صياغة السؤال بكلمات من نص القرآن أو الحديث.\n" +
                   "مثلاً: «الصبر»، «الصدقة»، «الصلاة»، «الرحمة»."
        }

        val builder = StringBuilder()
        builder.append("بناءً على المصادر الإسلامية الموثّقة:\n\n")

        sources.filter { it.type == CopilotSourceType.QURAN }.let { quran ->
            if (quran.isNotEmpty()) {
                builder.append("📖 من القرآن الكريم:\n\n")
                quran.forEachIndexed { i, s ->
                    builder.append("${i + 1}. [${s.reference}] ${s.title}\n\"${s.excerpt}\"\n\n")
                }
            }
        }

        sources.filter { it.type == CopilotSourceType.HADITH }.let { hadith ->
            if (hadith.isNotEmpty()) {
                builder.append("🕌 من الحديث النبوي:\n\n")
                hadith.forEachIndexed { i, s ->
                    builder.append("${i + 1}. ${s.title} — ${s.reference}\n\"${s.excerpt}\"\n\n")
                }
            }
        }

        sources.filter { it.type == CopilotSourceType.TAFSIR }.let { tafsir ->
            if (tafsir.isNotEmpty()) {
                builder.append("📚 التفسير:\n\n")
                tafsir.forEachIndexed { i, s ->
                    builder.append("${i + 1}. ${s.title} (${s.reference})\n${s.excerpt}...\n\n")
                }
            }
        }

        builder.append("━ ━ ━ ━ ━\n")
        builder.append("هذا رد معرفي موثّق وليس فتوى. للاستفسارات الفقهية يُرجى الرجوع لأهل العلم.")
        return builder.toString()
    }

    private fun generateFollowUps(query: String): List<String> {
        val lower = query.lowercase()
        return when {
            lower.contains("صبر") -> listOf("آيات عن الصبر", "ما هو أجر الصابرين؟", "أحاديث عن الصبر")
            lower.contains("قلق") || lower.contains("طمأنين") -> listOf("كيف أحقق الطمأنينة؟", "أذكار الصباح", "آيات عن السكينة")
            lower.contains("رزق") -> listOf("ما هي أسباب الرزق؟", "دعاء الاستخارة", "آيات عن البركة")
            lower.contains("مغفرة") || lower.contains("توبة") -> listOf("كيف أتوب؟", "شروط التوبة", "أحاديث عن التوبة")
            lower.contains("صلاة") || lower.contains("pray") -> listOf("مواقيت الصلاة", "كيف أتطهر؟", "أحاديث عن الصلاة")
            lower.contains("زكاة") || lower.contains("صدقة") -> listOf("ما هو نصاب الزكاة؟", "أحاديث عن الصدقة", "حاسبة الزكاة")
            lower.contains("دعاء") -> listOf("دعاء الاستخارة", "أذكار الصباح والمساء", "أدعية من القرآن")
            else -> listOf("آيات عن الصبر", "أحاديث عن الرحمة", "أذكار الصباح")
        }
    }

    override suspend fun semanticSearch(query: String, language: String): List<CopilotSource> {
        val sources = mutableListOf<CopilotSource>()
        try {
            val quranResults = quranApi.searchQuran(query, language, 5)
            quranResults.search?.results?.forEach { result ->
                val surahNum = result.verseKey.split(":").firstOrNull()?.toIntOrNull()
                sources.add(
                    CopilotSource(
                        type = CopilotSourceType.QURAN,
                        title = surahNames[surahNum ?: 0] ?: "سورة",
                        reference = result.verseKey,
                        excerpt = result.text,
                        url = "https://quran.com/${result.verseKey}",
                    ),
                )
            }
        } catch (e: Exception) {
            // تجاهل
        }
        return sources
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

    override suspend fun getSourcesByType(type: CopilotSourceType): List<CopilotSource> = emptyList()
}
