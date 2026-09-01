package com.siraj.app.data.repository

import com.siraj.app.domain.models.copilot.*
import com.siraj.app.domain.repository.copilot.CopilotRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

/**
 * تنفيذ المساعد الإسلامي الذكي
 * MVP: يحلل السؤال محلياً ويرد بمصادر موثّقة
 * TODO: ربط مع Vertex AI / OpenAI مع grounding على المصادر الإسلامية
 */
class CopilotRepositoryImpl : CopilotRepository {

    // قاعدة معرفة محلية للمصادر
    private val quranSources = listOf(
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
            title = "الطلاق",
            reference = "65:2-3",
            excerpt = "وَمَن يَتَّقِ اللَّهَ يَجْعَل لَّهُ مَخْرَجًا ۖ وَيَرْزُقْهُ مِنْ حَيْثُ لَا يَحْتَسِبُ",
        ),
        CopilotSource(
            type = CopilotSourceType.QURAN,
            title = "الشعراء",
            reference = "26:78-82",
            excerpt = "الَّذِي خَلَقَنِي فَهُوَ يَهْدِينِ ۖ وَالَّذِي هُوَ يُطْعِمُنِي وَيَسْقِينِ",
        ),
        CopilotSource(
            type = CopilotSourceType.QURAN,
            title = "الزمر",
            reference = "39:53",
            excerpt = "قُلْ يَا عِبَادِيَ الَّذِينَ أَسْرَفُوا عَلَىٰ أَنفُسِهِمْ لَا تَقْنَطُوا مِن رَّحْمَةِ اللَّهِ ۚ إِنَّ اللَّهَ يَغْفِرُ الذُّنُوبَ جَمِيعًا",
        ),
    )

    private val hadithSources = listOf(
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
        CopilotSource(
            type = CopilotSourceType.HADITH,
            title = "صحيح مسلم",
            reference = "2674",
            excerpt = "«الطُّهُورُ شَطْرُ الْإِيمَانِ»",
        ),
        CopilotSource(
            type = CopilotSourceType.HADITH,
            title = "صحيح البخاري",
            reference = "6018",
            excerpt = "«دَعْ ما يَرِيبُكَ إلَى ما لا يَرِيبُكَ»",
        ),
    )

    private val tafsirSources = listOf(
        CopilotSource(
            type = CopilotSourceType.TAFSIR,
            title = "تفسير ابن كثير",
            reference = "البقرة:153",
            excerpt = "يأمر تعالى عباده المؤمنين بالصبر على الطاعة وعن المعصية، والصبر على الأقدار، وأن يستعينوا على ذلك بالصلاة، فإن الصلاة معونة على جلب الخير ودفع الشر.",
        ),
        CopilotSource(
            type = CopilotSourceType.TAFSIR,
            title = "تفسير السعدي",
            reference = "البقرة:153",
            excerpt = "الصبر هو حبس النفس على طاعة الله، وعن معصيته، وعلى أقداره المؤلمة، والصلاة فيها معونة على الصبر والثبات.",
        ),
    )

    private val duaSources = listOf(
        CopilotSource(
            type = CopilotSourceType.DUA,
            title = "دعاء الصباح",
            reference = "سنن أبي داود",
            excerpt = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
        ),
        CopilotSource(
            type = CopilotSourceType.DUA,
            title = "دعاء الاستخارة",
            reference = "صحيح البخاري",
            excerpt = "اللَّهُمَّ إِنِّي أَسْتَخِيرُكَ بِعِلْمِكَ، وَأَسْتَقْدِرُكَ بِقُدْرَتِكَ، وَأَسْأَلُكَ مِنْ فَضْلِكَ الْعَظِيمِ",
        ),
    )

    override suspend fun ask(query: CopilotQuery): Flow<CopilotResponse> = flow {
        // محاكاة تفكير المساعد
        delay(1200)

        val sources = mutableListOf<CopilotSource>()

        // تحليل بسيط للسؤال
        val lowerText = query.text.lowercase()

        when {
            // أسئلة عن الصبر
            lowerText.contains("صبر") || lowerText.contains("patience") -> {
                sources.addAll(quranSources.filter { it.reference == "2:153" })
                sources.addAll(tafsirSources.filter { it.reference == "البقرة:153" })
            }
            // أسئلة عن القلق والطمأنينة
            lowerText.contains("قلق") || lowerText.contains("طمأنين") || lowerText.contains("anxiety") || lowerText.contains("peace") -> {
                sources.add(quranSources.find { it.reference == "13:28" }!!)
            }
            // أسئلة عن الرزق
            lowerText.contains("رزق") || lowerText.contains("provision") -> {
                sources.add(quranSources.find { it.reference == "65:2-3" }!!)
                sources.add(quranSources.find { it.reference == "26:78-82" }!!)
            }
            // أسئلة عن المغفرة والتوبة
            lowerText.contains("مغفرة") || lowerText.contains("توبة") || lowerText.contains("forgiveness") -> {
                sources.add(quranSources.find { it.reference == "39:53" }!!)
            }
            // أسئلة عن النية
            lowerText.contains("نية") || lowerText.contains("intention") -> {
                sources.add(hadithSources.find { it.reference == "1" }!!)
            }
            // أسئلة عن الطهارة
            lowerText.contains("طهارة") || lowerText.contains("purity") -> {
                sources.add(hadithSources.find { it.reference == "2674" }!!)
            }
            // أسئلة عن الظن بالله
            lowerText.contains("ظن") || lowerText.contains("رجاء") || lowerText.contains("hope") -> {
                sources.add(hadithSources.find { it.reference == "2675" }!!)
            }
            // أسئلة عن الدعاء
            lowerText.contains("دعاء") || lowerText.contains("supplication") || lowerText.contains("dua") -> {
                sources.addAll(duaSources)
            }
            // أسئلة عن الاستخارة
            lowerText.contains("استخارة") || lowerText.contains("istikhara") -> {
                sources.add(duaSources.find { it.title == "دعاء الاستخارة" }!!)
            }
            // default: أضف مصادر متنوعة
            else -> {
                sources.add(quranSources.first())
                sources.add(hadithSources.first())
            }
        }

        val answer = buildAnswer(query.text, sources)
        val followUps = generateFollowUps(query.text)

        emit(
            CopilotResponse(
                answer = answer,
                sources = sources,
                confidence = 0.9f,
                followUpQuestions = followUps,
            ),
        )
    }

    private fun buildAnswer(query: String, sources: List<CopilotSource>): String {
        if (sources.isEmpty()) return "لم أجد مصادر مناسبة لهذا السؤال. حاول إعادة صياغته."

        val builder = StringBuilder()
        builder.append("بناءً على المصادر الإسلامية الموثّقة:\n\n")

        sources.forEachIndexed { index, source ->
            builder.append("${index + 1}. ")
            when (source.type) {
                CopilotSourceType.QURAN -> {
                    builder.append("[${source.reference}] ${source.title}\n")
                    builder.append("\"${source.excerpt}\"\n\n")
                }
                CopilotSourceType.HADITH -> {
                    builder.append("${source.title} — ${source.reference}\n")
                    builder.append("\"${source.excerpt}\"\n\n")
                }
                CopilotSourceType.TAFSIR -> {
                    builder.append("${source.title} (${source.reference})\n")
                    builder.append("${source.excerpt}\n\n")
                }
                CopilotSourceType.DUA -> {
                    builder.append("${source.title} — ${source.reference}\n")
                    builder.append("\"${source.excerpt}\"\n\n")
                }
                CopilotSourceType.FIQH -> {
                    builder.append("${source.title}\n${source.excerpt}\n\n")
                }
            }
        }

        builder.append("هذا رد معرفي موثّق وليس فتوى. للاستفسارات الفقهية يُرجى الرجوع لأهل العلم.")
        return builder.toString()
    }

    private fun generateFollowUps(query: String): List<String> {
        return when {
            query.contains("صبر") -> listOf("كيف أصبر على البلاء؟", "ما هو أجر الصابرين؟")
            query.contains("قلق") -> listOf("كيف أحقق الطمأنينة؟", "أذكار الصباح والمساء")
            query.contains("رزق") -> listOf("ما هي أسباب الرزق؟", "دعاء الاستخارة")
            query.contains("مغفرة") || query.contains("توبة") -> listOf("كيف أتوب؟", "شروط التوبة")
            else -> listOf("آيات عن الصبر", "أحاديث عن الرحمة", "أذكار الصباح")
        }
    }

    override suspend fun semanticSearch(query: String, language: String): List<CopilotSource> {
        val allSources = quranSources + hadithSources + tafsirSources + duaSources
        return allSources.filter { source ->
            query.lowercase().split(" ").any { word ->
                source.excerpt.contains(word, ignoreCase = true) ||
                source.title.contains(word, ignoreCase = true) ||
                source.reference.contains(word, ignoreCase = true)
            }
        }.ifEmpty { allSources.take(3) }
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
            CopilotSourceType.QURAN -> quranSources
            CopilotSourceType.HADITH -> hadithSources
            CopilotSourceType.TAFSIR -> tafsirSources
            CopilotSourceType.DUA -> duaSources
            CopilotSourceType.FIQH -> emptyList()
        }
    }
}
