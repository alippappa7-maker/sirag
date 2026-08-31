package com.siraj.app.data.repository.audio

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.audio.*
import com.siraj.app.domain.repository.audio.AudioRepository
import kotlinx.coroutines.delay

class AudioRepositoryImpl : AudioRepository {
    // Mock data in memory
    private val mockTracks =
        mutableListOf(
            AudioTrack(
                id = "a1",
                title = "تلاوة سورة الكهف",
                speaker = "القارئ محمد",
                category = "recitation",
                durationSeconds = 1800,
                source = "مؤسسة التلاوات الرسمية",
                rights = AudioRights("Creative Commons", "https://example.com/audio1"),
                verificationStatus = AudioVerificationStatus.APPROVED,
                playCount = 1500,
                listenProgressSeconds = 120,
            ),
            AudioTrack(
                id = "a2",
                title = "شرح كتاب التوحيد - الدرس الأول",
                speaker = "الشيخ أحمد",
                category = "lesson",
                durationSeconds = 3600,
                source = "الموقع الرسمي للشيخ",
                rights = AudioRights("Public Domain", "https://example.com/audio2"),
                verificationStatus = AudioVerificationStatus.APPROVED,
                playCount = 500,
                isFavorite = true,
            ),
            AudioTrack(
                id = "a3",
                title = "محاضرة: كيف نستقبل رمضان",
                speaker = "الشيخ إبراهيم",
                category = "lecture",
                durationSeconds = 2700,
                source = "إذاعة القرآن الكريم",
                rights = AudioRights("Copyrighted (Licensed)", "https://example.com/audio3"),
                verificationStatus = AudioVerificationStatus.APPROVED,
                playCount = 3000,
            ),
            AudioTrack(
                id = "a4",
                title = "بودكاست سراج - الحلقة 1",
                speaker = "عمر",
                category = "podcast",
                durationSeconds = 1200,
                source = "منصة سراج",
                rights = AudioRights("Exclusive", "https://siraj.app/podcast/1"),
                verificationStatus = AudioVerificationStatus.APPROVED,
                playCount = 800,
            ),
            AudioTrack(
                id = "a5",
                title = "تلاوة غير موثقة (للاختبار)",
                speaker = "مجهول",
                category = "recitation",
                durationSeconds = 600,
                source = "منتدى غير معروف",
                rights = AudioRights("Unknown", ""),
                verificationStatus = AudioVerificationStatus.PENDING_REVIEW,
                playCount = 10,
            ),
        )

    override suspend fun getTracks(
        filter: AudioFilter,
        page: Int,
        pageSize: Int,
    ): Resource<List<AudioTrack>> {
        delay(500) // Simulate network/cache

        // Always filter by APPROVED first
        var result = mockTracks.filter { it.verificationStatus == AudioVerificationStatus.APPROVED }

        // Category filter
        if (filter.categoryId != null && filter.categoryId != "all") {
            if (filter.categoryId == "favorites") {
                result = result.filter { it.isFavorite }
            } else if (filter.categoryId == "downloads") {
                result = result.filter { it.isDownloaded }
            } else {
                result = result.filter { it.category == filter.categoryId }
            }
        }

        // Query filter
        if (filter.query.isNotBlank()) {
            result =
                result.filter {
                    it.title.contains(filter.query, ignoreCase = true) ||
                        it.speaker.contains(filter.query, ignoreCase = true)
                }
        }

        // Sort
        result =
            when (filter.sortOption) {
                AudioSortOption.NEWEST -> result.sortedByDescending { it.id } // Mocking newest by ID
                AudioSortOption.MOST_LISTENED -> result.sortedByDescending { it.playCount }
                AudioSortOption.ALPHABETICAL -> result.sortedBy { it.title }
            }

        // Pagination
        val startIndex = (page - 1) * pageSize
        if (startIndex >= result.size) return Resource.Success(emptyList())
        val endIndex = minOf(startIndex + pageSize, result.size)

        return Resource.Success(result.subList(startIndex, endIndex))
    }

    override suspend fun toggleFavorite(trackId: String): Resource<Boolean> {
        val index = mockTracks.indexOfFirst { it.id == trackId }
        if (index != -1) {
            val track = mockTracks[index]
            mockTracks[index] = track.copy(isFavorite = !track.isFavorite)
            return Resource.Success(true)
        }
        return Resource.Error("Track not found")
    }

    override suspend fun updateProgress(
        trackId: String,
        progressSeconds: Int,
    ): Resource<Boolean> {
        val index = mockTracks.indexOfFirst { it.id == trackId }
        if (index != -1) {
            val track = mockTracks[index]
            mockTracks[index] = track.copy(listenProgressSeconds = progressSeconds)
            return Resource.Success(true)
        }
        return Resource.Error("Track not found")
    }

    override suspend fun reportTrack(
        trackId: String,
        reason: String,
    ): Resource<Boolean> {
        delay(300)
        return Resource.Success(true)
    }
}
