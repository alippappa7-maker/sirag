package com.siraj.app.features.project.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.siraj.app.domain.models.SceneAudio
import com.siraj.app.features.project.domain.models.SceneAudioTrackConfig
import com.siraj.app.features.project.domain.models.SoundLicenseType
import com.siraj.app.features.project.domain.models.SoundtrackCategory
import com.siraj.app.features.project.domain.models.SoundtrackItem
import com.siraj.app.features.project.domain.repositories.SoundtrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirebaseSoundtrackRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : SoundtrackRepository {
    private val staticSoundtracks: List<SoundtrackItem> = emptyList()
            // Sound Effects (SFX)
            // Documentary Atmosphere (No Instruments/No Musical Melodies)
            // Vocal/Nasheed (Acoustic Human Vocals Only)

    override fun getSoundtracks(
        category: SoundtrackCategory?,
        searchQuery: String,
        hideMusic: Boolean,
    ): Flow<List<SoundtrackItem>> =
        flow {
            var filtered = staticSoundtracks

            if (hideMusic) {
                filtered = filtered.filter { !it.isMusic }
            }

            if (category != null) {
                filtered = filtered.filter { it.category == category }
            }

            if (searchQuery.isNotBlank()) {
                val q = searchQuery.trim().lowercase()
                filtered =
                    filtered.filter { item ->
                        item.title.lowercase().contains(q) ||
                            item.description.lowercase().contains(q) ||
                            item.tags.any { it.lowercase().contains(q) } ||
                            item.category.displayName
                                .lowercase()
                                .contains(q)
                    }
            }

            emit(filtered)
        }

    override suspend fun getSoundtrackById(id: String): SoundtrackItem? = staticSoundtracks.find { it.id == id }

    override suspend fun attachTrackToScene(
        projectId: String,
        sceneId: String,
        config: SceneAudioTrackConfig,
    ): Result<Unit> {
        return try {
            val sceneRef =
                firestore
                    .collection("projects")
                    .document(projectId)
                    .collection("scenes")
                    .document(sceneId)

            val sceneDoc = sceneRef.get().await()
            if (!sceneDoc.exists()) return Result.failure(Exception("المشهد غير موجود"))

            val sceneAudio =
                SceneAudio(
                    id = config.audioId,
                    sceneId = sceneId,
                    url = config.soundUrl,
                    type = if (config.isMusic) "background_music" else "sfx",
                    startTimeMs = config.startTrimMs,
                    durationMs = if (config.effectiveDurationMs > 0) config.effectiveDurationMs else null,
                    volume = config.volume,
                )

            val trackConfigMap =
                hashMapOf<String, Any>(
                    "audioId" to config.audioId,
                    "soundTitle" to config.soundTitle,
                    "soundUrl" to config.soundUrl,
                    "category" to config.category.name,
                    "isMusic" to config.isMusic,
                    "volume" to config.volume,
                    "loop" to config.loop,
                    "fadeIn" to config.fadeIn,
                    "fadeOut" to config.fadeOut,
                    "fadeInDurationMs" to config.fadeInDurationMs,
                    "fadeOutDurationMs" to config.fadeOutDurationMs,
                    "startTrimMs" to config.startTrimMs,
                    "endTrimMs" to config.endTrimMs,
                    "effectiveDurationMs" to config.effectiveDurationMs,
                    "attributionRequired" to config.attributionRequired,
                    "attributionText" to config.attributionText,
                    "licenseDisplayName" to config.licenseDisplayName,
                )

            sceneRef
                .update(
                    mapOf(
                        "soundtrackTrack" to trackConfigMap,
                        "backgroundAudio" to sceneAudio,
                    ),
                ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeTrackFromScene(
        projectId: String,
        sceneId: String,
    ): Result<Unit> =
        try {
            val sceneRef =
                firestore
                    .collection("projects")
                    .document(projectId)
                    .collection("scenes")
                    .document(sceneId)

            sceneRef
                .update(
                    mapOf(
                        "soundtrackTrack" to
                            com.google.firebase.firestore.FieldValue
                                .delete(),
                        "backgroundAudio" to
                            com.google.firebase.firestore.FieldValue
                                .delete(),
                    ),
                ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
