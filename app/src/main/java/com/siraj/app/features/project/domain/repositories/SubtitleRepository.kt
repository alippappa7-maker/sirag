package com.siraj.app.features.project.domain.repositories

import com.siraj.app.features.project.domain.models.SubtitleItem
import com.siraj.app.features.project.domain.models.SubtitleStyle
import kotlinx.coroutines.flow.Flow

interface SubtitleRepository {
    fun getSubtitles(
        projectId: String,
        sceneId: String? = null,
    ): Flow<List<SubtitleItem>>

    suspend fun saveSubtitle(subtitle: SubtitleItem): Result<Unit>

    suspend fun deleteSubtitle(
        projectId: String,
        sceneId: String,
        subtitleId: String,
    ): Result<Unit>

    suspend fun generateSubtitlesFromScene(
        projectId: String,
        sceneId: String,
        sceneText: String,
        sceneDurationMs: Long,
    ): Result<List<SubtitleItem>>

    suspend fun autoTranslateToEnglish(
        projectId: String,
        sceneId: String,
        arabicSubtitles: List<SubtitleItem>,
    ): Result<List<SubtitleItem>>

    suspend fun updateSubtitleStyleForScene(
        projectId: String,
        sceneId: String,
        style: SubtitleStyle,
    ): Result<Unit>

    fun exportToSrt(subtitles: List<SubtitleItem>): String

    fun exportToVtt(subtitles: List<SubtitleItem>): String
}
