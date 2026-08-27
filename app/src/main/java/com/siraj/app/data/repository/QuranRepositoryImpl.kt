package com.siraj.app.data.repository

import com.siraj.app.core.utils.Resource
import com.siraj.app.data.api.QuranApi
import com.siraj.app.data.local.QuranBookmarkEntity
import com.siraj.app.data.local.QuranDao
import com.siraj.app.data.local.QuranNoteEntity
import com.siraj.app.domain.models.quran.Ayah
import com.siraj.app.domain.models.quran.Surah
import com.siraj.app.domain.repository.QuranRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class QuranRepositoryImpl(
    private val api: QuranApi,
    private val dao: QuranDao
) : QuranRepository {
    
    // In-memory cache for surahs to avoid repeated network calls
    private var cachedSurahs: List<Surah>? = null
    
    override suspend fun getSurahs(): Resource<List<Surah>> {
        return try {
            cachedSurahs?.let { return Resource.Success(it) }
            val response = api.getChapters()
            val surahs = response.chapters.map { 
                Surah(it.id, it.nameArabic, it.versesCount, if(it.revelationPlace == "makkah") "مكية" else "مدنية") 
            }
            cachedSurahs = surahs
            Resource.Success(surahs)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load Surahs")
        }
    }

    override suspend fun getAyahs(surahId: Int): Resource<List<Ayah>> {
        return try {
            // Using translation ID 131 (Saheeh International) and Tafsir ID 16 (Al-Muyassar) for demonstration
            val response = api.getVersesByChapter(surahId, translations = "131", tafsirs = "16")
            val ayahs = response.verses.map { apiVerse ->
                val localNote = dao.getNote(apiVerse.verseKey)
                Ayah(
                    id = apiVerse.id,
                    verseKey = apiVerse.verseKey,
                    textUthmani = apiVerse.textUthmani ?: "",
                    translation = apiVerse.translations?.firstOrNull()?.text,
                    tafsir = apiVerse.tafsirs?.firstOrNull()?.text,
                    note = localNote?.noteText
                )
            }
            Resource.Success(ayahs)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load Ayahs")
        }
    }

    override suspend fun toggleBookmark(verseKey: String, surahId: Int, verseNumber: Int, isBookmarked: Boolean) {
        val entity = QuranBookmarkEntity(verseKey, surahId, verseNumber)
        if (isBookmarked) {
            dao.insertBookmark(entity)
        } else {
            dao.deleteBookmark(entity)
        }
    }

    override suspend fun saveNote(verseKey: String, surahId: Int, verseNumber: Int, note: String) {
        dao.insertNote(QuranNoteEntity(verseKey, surahId, verseNumber, note))
    }

    override fun getBookmarkedVerseKeys(): Flow<List<String>> {
        return dao.getAllBookmarks().map { list -> list.map { it.verseKey } }
    }
}
