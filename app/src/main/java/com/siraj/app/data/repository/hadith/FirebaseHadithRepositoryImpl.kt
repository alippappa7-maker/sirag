package com.siraj.app.data.repository.hadith

import com.google.firebase.firestore.FirebaseFirestore
import com.siraj.app.domain.models.hadith.Hadith
import com.siraj.app.domain.models.hadith.HadithCollection
import com.siraj.app.domain.models.hadith.HadithGrade
import com.siraj.app.domain.repository.hadith.HadithRepository
import kotlinx.coroutines.tasks.await

class FirebaseHadithRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : HadithRepository {

    private val defaultCollections = listOf(
        HadithCollection("bukhari", "صحيح البخاري", "الإمام محمد بن إسماعيل البخاري", 7563, true),
        HadithCollection("muslim", "صحيح مسلم", "الإمام مسلم بن الحجاج النيسابوري", 7500, true),
        HadithCollection("abudawud", "سنن أبي داود", "الإمام أبو داود السجستاني", 5274, true),
        HadithCollection("tirmidhi", "جامع الترمذي", "الإمام أبو عيسى محمد الترمذي", 3956, true),
        HadithCollection("nasai", "سنن النسائي", "الإمام أحمد بن شعيب النسائي", 5758, true),
        HadithCollection("ibnmajah", "سنن ابن ماجه", "الإمام محمد بن ماجه القزويني", 4341, true)
    )

    override suspend fun getCollections(): List<HadithCollection> {
        return try {
            val snapshot = firestore.collection("hadith_collections").get().await()
            if (!snapshot.isEmpty) {
                snapshot.documents.mapNotNull { doc ->
                    val id = doc.id
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val author = doc.getString("author") ?: ""
                    val total = doc.getLong("totalHadiths")?.toInt() ?: 0
                    val isVerified = doc.getBoolean("isVerified") ?: true
                    HadithCollection(id, name, author, total, isVerified)
                }
            } else {
                defaultCollections
            }
        } catch (e: Exception) {
            defaultCollections
        }
    }

    override suspend fun getHadithsByCollection(collectionId: String, limit: Int): List<Hadith> {
        return try {
            val snapshot = firestore.collection("hadiths")
                .whereEqualTo("collectionId", collectionId)
                .limit(limit.toLong())
                .get()
                .await()
            snapshot.documents.mapNotNull { doc -> mapDocToHadith(doc) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getHadithById(id: String): Hadith? {
        return try {
            val doc = firestore.collection("hadiths").document(id).get().await()
            if (doc.exists()) mapDocToHadith(doc) else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun searchHadiths(query: String, collectionId: String?): List<Hadith> {
        return try {
            var ref = firestore.collection("hadiths").limit(20)
            if (!collectionId.isNullOrEmpty()) {
                ref = ref.whereEqualTo("collectionId", collectionId)
            }
            val snapshot = ref.get().await()
            snapshot.documents.mapNotNull { doc -> mapDocToHadith(doc) }
                .filter { it.text.contains(query, ignoreCase = true) || it.narrator.contains(query, ignoreCase = true) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getHadithsByTopic(topic: String, limit: Int): List<Hadith> {
        return try {
            val snapshot = firestore.collection("hadiths")
                .whereArrayContains("topicTags", topic)
                .limit(limit.toLong())
                .get()
                .await()
            snapshot.documents.mapNotNull { doc -> mapDocToHadith(doc) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun mapDocToHadith(doc: com.google.firebase.firestore.DocumentSnapshot): Hadith? {
        val id = doc.id
        val text = doc.getString("text") ?: return null
        val narrator = doc.getString("narrator") ?: ""
        @Suppress("UNCHECKED_CAST")
        val chain = (doc.get("chain") as? List<String>) ?: emptyList()
        val gradeStr = doc.getString("grade") ?: "SAHIH"
        val grade = try { HadithGrade.valueOf(gradeStr) } catch (e: Exception) { HadithGrade.SAHIH }
        val collectionId = doc.getString("collectionId") ?: "bukhari"
        val collectionName = doc.getString("collectionName") ?: "صحيح البخاري"
        val hadithNumber = doc.getLong("hadithNumber")?.toInt() ?: 1
        val chapter = doc.getString("chapter")
        @Suppress("UNCHECKED_CAST")
        val topicTags = (doc.get("topicTags") as? List<String>) ?: emptyList()

        return Hadith(
            id = id,
            text = text,
            narrator = narrator,
            chain = chain,
            grade = grade,
            collectionId = collectionId,
            collectionName = collectionName,
            hadithNumber = hadithNumber,
            chapter = chapter,
            topicTags = topicTags
        )
    }
}
