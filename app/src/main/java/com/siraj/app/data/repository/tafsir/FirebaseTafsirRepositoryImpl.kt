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

    private val defaultEditions = emptyList<TafsirEdition>()

    private val defaultSurahs = emptyList<TafsirSurah>()

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
