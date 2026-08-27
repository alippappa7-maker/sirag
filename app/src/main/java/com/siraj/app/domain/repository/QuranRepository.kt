package com.siraj.app.domain.repository

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.quran.Ayah
import com.siraj.app.domain.models.quran.Surah
import kotlinx.coroutines.flow.Flow

interface QuranRepository {
    suspend fun getSurahs(): Resource<List<Surah>>
    suspend fun getAyahs(surahId: Int): Resource<List<Ayah>>
    suspend fun toggleBookmark(verseKey: String, surahId: Int, verseNumber: Int, isBookmarked: Boolean)
    suspend fun saveNote(verseKey: String, surahId: Int, verseNumber: Int, note: String)
    fun getBookmarkedVerseKeys(): Flow<List<String>>
}
