package com.siraj.app.data.repository.audio

import com.google.firebase.firestore.FirebaseFirestore
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.audio.*
import com.siraj.app.domain.repository.audio.AudioRepository
import kotlinx.coroutines.tasks.await

class AudioRepositoryImpl(
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (_: Throwable) { null }
) : AudioRepository {

    private val localTracks = mutableListOf<AudioTrack>()

    override suspend fun getTracks(
        filter: AudioFilter,
        page: Int,
        pageSize: Int,
    ): Resource<List<AudioTrack>> {
        val fs = firestore
        if (fs != null) {
            try {
                val snapshot = fs.collection("audio_tracks")
                    .whereEqualTo("verificationStatus", AudioVerificationStatus.APPROVED.name)
                    .get()
                    .await()

                val remoteTracks = snapshot.documents.mapNotNull { doc ->
                    try {
                        AudioTrack(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            speaker = doc.getString("speaker") ?: "",
                            category = doc.getString("category") ?: "recitation",
                            durationSeconds = doc.getLong("durationSeconds")?.toInt() ?: 0,
                            source = doc.getString("source") ?: "",
                            rights = AudioRights(
                                licenseType = doc.getString("rightsLicense") ?: "All Rights Reserved",
                                sourceUrl = doc.getString("rightsUrl") ?: "",
                            ),
                            verificationStatus = AudioVerificationStatus.APPROVED,
                            playCount = doc.getLong("playCount")?.toInt() ?: 0,
                            listenProgressSeconds = 0,
                            isFavorite = false,
                            isDownloaded = false
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
                if (remoteTracks.isNotEmpty()) {
                    localTracks.clear()
                    localTracks.addAll(remoteTracks)
                }
            } catch (_: Exception) {
                // Return local tracks
            }
        }

        var result = localTracks.filter { it.verificationStatus == AudioVerificationStatus.APPROVED }

        if (filter.categoryId != null && filter.categoryId != "all") {
            if (filter.categoryId == "favorites") {
                result = result.filter { it.isFavorite }
            } else if (filter.categoryId == "downloads") {
                result = result.filter { it.isDownloaded }
            } else {
                result = result.filter { it.category == filter.categoryId }
            }
        }

        if (filter.query.isNotBlank()) {
            result = result.filter {
                it.title.contains(filter.query, ignoreCase = true) ||
                    it.speaker.contains(filter.query, ignoreCase = true)
            }
        }

        result = when (filter.sortOption) {
            AudioSortOption.NEWEST -> result.sortedByDescending { it.id }
            AudioSortOption.MOST_LISTENED -> result.sortedByDescending { it.playCount }
            AudioSortOption.ALPHABETICAL -> result.sortedBy { it.title }
        }

        val startIndex = (page - 1) * pageSize
        if (startIndex >= result.size) return Resource.Success(emptyList())
        val endIndex = minOf(startIndex + pageSize, result.size)

        return Resource.Success(result.subList(startIndex, endIndex))
    }

    override suspend fun toggleFavorite(trackId: String): Resource<Boolean> {
        val index = localTracks.indexOfFirst { it.id == trackId }
        if (index != -1) {
            val track = localTracks[index]
            localTracks[index] = track.copy(isFavorite = !track.isFavorite)
            return Resource.Success(true)
        }
        return Resource.Error("Track not found")
    }

    override suspend fun updateProgress(
        trackId: String,
        progressSeconds: Int,
    ): Resource<Boolean> {
        val index = localTracks.indexOfFirst { it.id == trackId }
        if (index != -1) {
            val track = localTracks[index]
            localTracks[index] = track.copy(listenProgressSeconds = progressSeconds)
            return Resource.Success(true)
        }
        return Resource.Error("Track not found")
    }

    override suspend fun reportTrack(
        trackId: String,
        reason: String,
    ): Resource<Boolean> {
        return Resource.Success(true)
    }
}
