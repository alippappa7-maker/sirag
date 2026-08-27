package com.siraj.app.features.project.domain.repositories

import com.siraj.app.features.project.domain.models.AudioItem
import com.siraj.app.features.project.domain.models.GenerateAudioRequest
import com.siraj.app.features.project.domain.models.VoiceOption
import kotlinx.coroutines.flow.Flow

interface AudioRepository {
    fun getAvailableVoices(): List<VoiceOption>
    
    suspend fun generateVoiceover(request: GenerateAudioRequest): Result<AudioItem>
    
    suspend fun uploadUserAudio(
        projectId: String,
        sceneId: String?,
        title: String,
        fileName: String,
        fileBytes: ByteArray,
        mimeType: String,
        durationMs: Long,
        reciterOrSpeakerName: String?,
        isRecitation: Boolean
    ): Result<AudioItem>
    
    suspend fun trimAudio(
        audioId: String,
        startTrimMs: Long,
        endTrimMs: Long
    ): Result<AudioItem>
    
    suspend fun attachAudioToScene(
        projectId: String,
        sceneId: String,
        audioItem: AudioItem,
        syncSceneDuration: Boolean
    ): Result<Unit>
    
    suspend fun getProjectAudios(projectId: String): Flow<List<AudioItem>>
    
    suspend fun deleteAudio(audioId: String): Result<Unit>
}
