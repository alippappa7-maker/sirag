package com.siraj.app.domain.models

enum class MediaType { IMAGE, VIDEO }
enum class MediaOrientation { ALL, LANDSCAPE, PORTRAIT, SQUARE }

data class MediaSearchFilter(
    val type: MediaType = MediaType.IMAGE,
    val orientation: MediaOrientation = MediaOrientation.ALL,
    val color: String? = null,
    val minDurationMs: Long? = null,
    val maxDurationMs: Long? = null
)

data class ExternalMediaItem(
    val id: String,
    val type: MediaType,
    val previewUrl: String,
    val downloadUrl: String, // Original or high quality
    val title: String,
    val creatorName: String,
    val sourceUrl: String,
    val licenseName: String,
    val commercialUseAllowed: Boolean,
    val attributionRequired: Boolean,
    val attributionText: String,
    val width: Int = 0,
    val height: Int = 0,
    val durationMs: Long? = null
)

data class MediaSearchResult(
    val items: List<ExternalMediaItem>,
    val nextPageToken: String? = null,
    val totalResults: Int = 0
)
