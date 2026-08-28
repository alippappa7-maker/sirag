package com.siraj.app.domain.models.audio

enum class AudioVerificationStatus {
    APPROVED, PENDING_REVIEW, REJECTED
}

data class AudioRights(
    val licenseType: String,
    val sourceUrl: String,
    val commercialUseAllowed: Boolean = false
)

data class AudioTrack(
    val id: String,
    val title: String,
    val speaker: String, // Reciter, lecturer, etc.
    val category: String, // "recitation", "lesson", "lecture", "podcast"
    val coverUrl: String? = null,
    val durationSeconds: Int,
    val source: String,
    val rights: AudioRights,
    val verificationStatus: AudioVerificationStatus = AudioVerificationStatus.PENDING_REVIEW,
    val listenProgressSeconds: Int = 0,
    val playCount: Int = 0,
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false
)

data class AudioFilter(
    val query: String = "",
    val categoryId: String? = null,
    val sortOption: AudioSortOption = AudioSortOption.NEWEST
)

enum class AudioSortOption {
    NEWEST, MOST_LISTENED, ALPHABETICAL
}
