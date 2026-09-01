package com.siraj.app.domain.repository.hadith

import com.siraj.app.domain.models.hadith.Hadith
import com.siraj.app.domain.models.hadith.HadithCollection

interface HadithRepository {
    suspend fun getCollections(): List<HadithCollection>
    suspend fun getHadithsByCollection(collectionId: String, limit: Int = 20): List<Hadith>
    suspend fun getHadithById(id: String): Hadith?
    suspend fun searchHadiths(query: String, collectionId: String? = null): List<Hadith>
    suspend fun getHadithsByTopic(topic: String, limit: Int = 20): List<Hadith>
}
