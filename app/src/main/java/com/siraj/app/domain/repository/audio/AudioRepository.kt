package com.siraj.app.domain.repository.audio

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.audio.AudioFilter
import com.siraj.app.domain.models.audio.AudioTrack

interface AudioRepository {
    suspend fun getTracks(
        filter: AudioFilter,
        page: Int = 1,
        pageSize: Int = 20,
    ): Resource<List<AudioTrack>>

    suspend fun toggleFavorite(trackId: String): Resource<Boolean>

    suspend fun updateProgress(
        trackId: String,
        progressSeconds: Int,
    ): Resource<Boolean>

    suspend fun reportTrack(
        trackId: String,
        reason: String,
    ): Resource<Boolean>
}
