package com.siraj.app.features.project.domain.repositories

import com.siraj.app.features.project.domain.models.SceneAudioTrackConfig
import com.siraj.app.features.project.domain.models.SoundtrackCategory
import com.siraj.app.features.project.domain.models.SoundtrackItem
import kotlinx.coroutines.flow.Flow

interface SoundtrackRepository {
    fun getSoundtracks(
        category: SoundtrackCategory? = null,
        searchQuery: String = "",
        hideMusic: Boolean = false
    ): Flow<List<SoundtrackItem>>

    suspend fun getSoundtrackById(id: String): SoundtrackItem?

    suspend fun attachTrackToScene(
        projectId: String,
        sceneId: String,
        config: SceneAudioTrackConfig
    ): Result<Unit>

    suspend fun removeTrackFromScene(
        projectId: String,
        sceneId: String
    ): Result<Unit>
}
