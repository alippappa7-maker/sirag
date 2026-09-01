package com.siraj.app.data.repository

import com.siraj.app.domain.models.tafsir.TafsirEdition
import com.siraj.app.domain.models.tafsir.TafsirVerse
import com.siraj.app.domain.models.tafsir.TafsirSurah
import com.siraj.app.domain.repository.tafsir.TafsirRepository

/**
 * تطبيق مستودع التفسير — يفصل نص القرآن عن التفسير تماماً
 * المصادر المعتمدة: مجمع الملك فهد لطباعة المصحف الشريف
 */
class TafsirRepositoryImpl : TafsirRepository {

    private val editions = listOf(
        TafsirEdition("ibn_kathir", "ابن كثير", "إسماعيل بن عمر بن كثير القرشي", "مجمع الملك فهد", true),
        TafsirEdition("saadi", "السعدي", "عبد الرحمن بن ناصر السعدي", "إسلام ويب", true),
        TafsirEdition("muyassar", "الميسر", "مجموعة من العلماء", "مجمع الملك فهد", true),
        TafsirEdition("tabari", "الطبري", "محمد بن جرير الطبري", "مكتبة ابن تيمية", true),
    )

    private val surahs = listOf(
        TafsirSurah(1, "الفاتحة", 7, "مكية"),
        TafsirSurah(2, "البقرة", 286, "مدنية"),
        TafsirSurah(3, "آل عمران", 200, "مدنية"),
        TafsirSurah(4, "النساء", 176, "مدنية"),
        TafsirSurah(5, "المائدة", 120, "مدنية"),
        TafsirSurah(6, "الأنعام", 165, "مكية"),
        TafsirSurah(7, "الأعراف", 206, "مكية"),
        TafsirSurah(8, "الأنفال", 75, "مدنية"),
        TafsirSurah(9, "التوبة", 129, "مدنية"),
        TafsirSurah(10, "يونس", 109, "مكية"),
        TafsirSurah(112, "الإخلاص", 4, "مكية"),
        TafsirSurah(113, "الفلق", 5, "مكية"),
        TafsirSurah(114, "الناس", 6, "مكية"),
    )

    // بيانات التفسير مختصرة لأغراض العرض — المصدر الكامل يجب رفعه من قاعدة بيانات موثقة
    private val tafsirCache = mutableMapOf<String, TafsirVerse>()

    override suspend fun getEditions(): List<TafsirEdition> = editions

    override suspend fun getSurahs(): List<TafsirSurah> = surahs

    override suspend fun getTafsir(surahNumber: Int, ayahNumber: Int, editionId: String): TafsirVerse? {
        val key = "${editionId}_${surahNumber}_${ayahNumber}"
        // TODO: استبدال بجلب البيانات من Firebase Firestore (collection: tafsir)
        // حالياً يعيد بيانات تجريبية موثقة من الفاتحة
        if (surahNumber == 1 && ayahNumber == 1) {
            return TafsirVerse(
                surahNumber = 1,
                ayahNumber = 1,
                ayahText = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                tafsirText = getTafsirText(editionId, 1, 1),
                editionId = editionId,
                editionName = editions.find { it.id == editionId }?.name ?: "",
                contextReason = null,
            )
        }
        return tafsirCache[key]
    }

    override suspend fun getTafsirBySurah(surahNumber: Int, editionId: String): List<TafsirVerse> {
        val surah = surahs.find { it.number == surahNumber } ?: return emptyList()
        return (1..surah.ayahCount.coerceAtMost(7)).mapNotNull { ayah ->
            getTafsir(surahNumber, ayah, editionId)
        }
    }

    override suspend fun searchTafsir(query: String, editionId: String?): List<TafsirVerse> {
        // TODO: استبدال ببحث Firestore كامل
        return emptyList()
    }

    private fun getTafsirText(editionId: String, surah: Int, ayah: Int): String {
        return when (editionId) {
            "ibn_kathir" -> "قال ابن كثير رحمه الله: يفتتح الله عز وجل كتابه الكريم بالبسملة، وهي قول: بسم الله الرحمن الرحيم. واختلف العلماء في البسملة هل هي آية من الفاتحة أم لا؟ والصحيح أنها آية منها."
            "saadi" -> "قال السعدي رحمه الله: أي: أبتدئ بكل اسم لله تعالى، وهو علم على الرب تبارك وتعالى، يقال فيه: الله، ولا يقال في غيره."
            "muyassar" -> "يبتدئ الله كلامه بـ (بسم الله الرحمن الرحيم)، أي: أستعين بالله وحده، الرحمن ذي الرحمة الواسعة، الرحيم بالمؤمنين."
            "tabari" -> "قال الطبري رحمه الله: إن معنى قوله: (بسم الله) أي: بسم الله الذي لا ينبغي أن يُسمى به غيره من خلقه."
            else -> ""
        }
    }
}
