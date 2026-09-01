package com.siraj.app.domain.repository.tafsir

import com.siraj.app.domain.models.tafsir.TafsirEdition
import com.siraj.app.domain.models.tafsir.TafsirVerse
import com.siraj.app.domain.models.tafsir.TafsirSurah

interface TafsirRepository {
    suspend fun getEditions(): List<TafsirEdition>
    suspend fun getSurahs(): List<TafsirSurah>
    suspend fun getTafsir(surahNumber: Int, ayahNumber: Int, editionId: String): TafsirVerse?
    suspend fun getTafsirBySurah(surahNumber: Int, editionId: String): List<TafsirVerse>
    suspend fun searchTafsir(query: String, editionId: String? = null): List<TafsirVerse>
}
