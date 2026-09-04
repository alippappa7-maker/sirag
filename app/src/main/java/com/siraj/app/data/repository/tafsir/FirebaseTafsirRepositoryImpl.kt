package com.siraj.app.data.repository.tafsir

import com.google.firebase.firestore.FirebaseFirestore
import com.siraj.app.domain.models.tafsir.TafsirEdition
import com.siraj.app.domain.models.tafsir.TafsirSurah
import com.siraj.app.domain.models.tafsir.TafsirVerse
import com.siraj.app.domain.repository.tafsir.TafsirRepository
import kotlinx.coroutines.tasks.await

class FirebaseTafsirRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : TafsirRepository {

    private val defaultEditions = listOf(
        TafsirEdition("saadi", "تفسير السعدي (تيسير الكريم الرحمن)", "الشيخ عبد الرحمن بن ناصر السعدي", "مجمع الملك فهد لطباعة المصحف الشريف", true),
        TafsirEdition("ibnkathir", "تفسير ابن كثير", "الإمام الحافظ ابن كثير الدمشقي", "دار طيبة للنشر والتوزيع", true),
        TafsirEdition("muyassar", "التفسير الميسر", "نخبة من العلماء", "مجمع الملك فهد لطباعة المصحف الشريف", true)
    )

    private val defaultSurahs = listOf(
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
        TafsirSurah(11, "هود", 123, "مكية"),
        TafsirSurah(12, "يوسف", 111, "مكية"),
        TafsirSurah(18, "الكهف", 110, "مكية"),
        TafsirSurah(36, "يس", 83, "مكية"),
        TafsirSurah(67, "الملك", 30, "مكية"),
        TafsirSurah(112, "الإخلاص", 4, "مكية"),
        TafsirSurah(113, "الفلق", 5, "مكية"),
        TafsirSurah(114, "الناس", 6, "مكية")
    )

    override suspend fun getEditions(): List<TafsirEdition> {
        return try {
            val snapshot = firestore.collection("tafsir_editions").get().await()
            if (!snapshot.isEmpty) {
                snapshot.documents.mapNotNull { doc ->
                    val id = doc.id
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val author = doc.getString("author") ?: ""
                    val source = doc.getString("source") ?: ""
                    val isVerified = doc.getBoolean("isVerified") ?: true
                    TafsirEdition(id, name, author, source, isVerified)
                }
            } else {
                defaultEditions
            }
        } catch (e: Exception) {
            defaultEditions
        }
    }

    override suspend fun getSurahs(): List<TafsirSurah> {
        return try {
            val snapshot = firestore.collection("quran_surahs").get().await()
            if (!snapshot.isEmpty) {
                snapshot.documents.mapNotNull { doc ->
                    val number = doc.getLong("number")?.toInt() ?: return@mapNotNull null
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val ayahCount = doc.getLong("ayahCount")?.toInt() ?: 0
                    val revelationType = doc.getString("revelationType") ?: "مكية"
                    TafsirSurah(number, name, ayahCount, revelationType)
                }.sortedBy { it.number }
            } else {
                defaultSurahs
            }
        } catch (e: Exception) {
            defaultSurahs
        }
    }

    override suspend fun getTafsir(surahNumber: Int, ayahNumber: Int, editionId: String): TafsirVerse? {
        return try {
            val docId = "${surahNumber}_${ayahNumber}_$editionId"
            val doc = firestore.collection("tafsir_verses").document(docId).get().await()
            if (doc.exists()) {
                val ayahText = doc.getString("ayahText") ?: ""
                val tafsirText = doc.getString("tafsirText") ?: ""
                val editionName = doc.getString("editionName") ?: defaultEditions.find { it.id == editionId }?.name ?: ""
                val contextReason = doc.getString("contextReason")
                TafsirVerse(surahNumber, ayahNumber, ayahText, tafsirText, editionId, editionName, contextReason)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getTafsirBySurah(surahNumber: Int, editionId: String): List<TafsirVerse> {
        return try {
            val snapshot = firestore.collection("tafsir_verses")
                .whereEqualTo("surahNumber", surahNumber)
                .whereEqualTo("editionId", editionId)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                val ayahNumber = doc.getLong("ayahNumber")?.toInt() ?: return@mapNotNull null
                val ayahText = doc.getString("ayahText") ?: ""
                val tafsirText = doc.getString("tafsirText") ?: ""
                val editionName = doc.getString("editionName") ?: ""
                val contextReason = doc.getString("contextReason")
                TafsirVerse(surahNumber, ayahNumber, ayahText, tafsirText, editionId, editionName, contextReason)
            }.sortedBy { it.ayahNumber }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun searchTafsir(query: String, editionId: String?): List<TafsirVerse> {
        return try {
            var ref = firestore.collection("tafsir_verses").limit(30)
            if (!editionId.isNullOrEmpty()) {
                ref = ref.whereEqualTo("editionId", editionId)
            }
            val snapshot = ref.get().await()
            snapshot.documents.mapNotNull { doc ->
                val surahNumber = doc.getLong("surahNumber")?.toInt() ?: return@mapNotNull null
                val ayahNumber = doc.getLong("ayahNumber")?.toInt() ?: return@mapNotNull null
                val ayahText = doc.getString("ayahText") ?: ""
                val tafsirText = doc.getString("tafsirText") ?: ""
                val edition = doc.getString("editionId") ?: "saadi"
                val editionName = doc.getString("editionName") ?: ""
                val contextReason = doc.getString("contextReason")
                TafsirVerse(surahNumber, ayahNumber, ayahText, tafsirText, edition, editionName, contextReason)
            }.filter {
                it.ayahText.contains(query, ignoreCase = true) || it.tafsirText.contains(query, ignoreCase = true)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
