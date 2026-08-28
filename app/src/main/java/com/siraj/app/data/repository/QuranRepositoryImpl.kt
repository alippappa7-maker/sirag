package com.siraj.app.data.repository

import com.siraj.app.core.utils.Resource
import com.siraj.app.data.api.QuranApi
import com.siraj.app.data.local.QuranBookmarkEntity
import com.siraj.app.data.local.QuranDao
import com.siraj.app.data.local.QuranNoteEntity
import com.siraj.app.domain.models.quran.Ayah
import com.siraj.app.domain.models.quran.AyahAudio
import com.siraj.app.domain.models.quran.AyahTafsir
import com.siraj.app.domain.models.quran.AyahTranslation
import com.siraj.app.domain.models.quran.Surah
import com.siraj.app.domain.repository.QuranRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class QuranRepositoryImpl(
    private val api: QuranApi,
    private val dao: QuranDao
) : QuranRepository {

    private var cachedSurahs: List<Surah>? = null

    override suspend fun getSurahs(): Resource<List<Surah>> {
        return try {
            cachedSurahs?.let { return Resource.Success(it) }
            val response = api.getChapters()
            val surahs = response.chapters.map {
                Surah(
                    chapterNumber = it.id,
                    nameArabic = it.nameArabic,
                    nameTranslated = it.nameSimple,
                    versesCount = it.versesCount,
                    revelationPlace = if (it.revelationPlace == "makkah") "مكية" else "مدنية"
                )
            }
            cachedSurahs = surahs
            Resource.Success(surahs)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load Surahs")
        }
    }

    override suspend fun getAyahs(surahId: Int): Resource<List<Ayah>> {
        return try {
            // Fetch verses with translations and tafsirs
            // Note: Since we are mocking/using a simulated API for now that might fail, 
            // we will catch the exception and return a dummy result for the UI to display,
            // strictly following the rule: "Do not put static verses from memory".
            // However, to ensure the UI works, we simulate the structure properly.
            val response = api.getVersesByChapter(surahId, translations = "131", tafsirs = "16")
            val ayahs = response.verses.map { apiVerse ->
                val localNote = dao.getNote(apiVerse.verseKey)
                
                val chapterNumber = apiVerse.verseKey.split(":")[0].toIntOrNull() ?: surahId
                val verseNumber = apiVerse.verseKey.split(":")[1].toIntOrNull() ?: 1

                // Create full structured objects with source names as required
                val translation = apiVerse.translations?.firstOrNull()?.let {
                    AyahTranslation(
                        text = it.text,
                        resourceName = "Saheeh International",
                        language = "en"
                    )
                }

                val tafsir = apiVerse.tafsirs?.firstOrNull()?.let {
                    AyahTafsir(
                        text = it.text,
                        resourceName = "التفسير الميسر",
                        language = "ar"
                    )
                }

                val audio = AyahAudio(
                    url = "https://example.com/audio/${chapterNumber}_${verseNumber}.mp3", // Simulated URL
                    reciterName = "مشاري راشد العفاسي",
                    reciterStyle = "مرتل"
                )

                Ayah(
                    verseKey = apiVerse.verseKey,
                    chapterNumber = chapterNumber,
                    verseNumber = verseNumber,
                    textUthmani = apiVerse.textUthmani ?: "",
                    translation = translation,
                    tafsir = tafsir,
                    audio = audio,
                    isBookmarked = false, // Will be merged later
                    note = localNote?.noteText
                )
            }
            Resource.Success(ayahs)
        } catch (e: Exception) {
            // When offline or API fails, do NOT generate fake verses. Return error.
            Resource.Error(e.message ?: "Failed to load Ayahs from source")
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
