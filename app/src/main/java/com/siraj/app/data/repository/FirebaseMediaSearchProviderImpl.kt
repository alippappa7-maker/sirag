package com.siraj.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.ExternalMediaItem
import com.siraj.app.domain.models.MediaSearchFilter
import com.siraj.app.domain.models.MediaSearchResult
import com.siraj.app.domain.models.MediaType
import com.siraj.app.domain.repository.MediaSearchProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirebaseMediaSearchProviderImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : MediaSearchProvider {

    override fun searchMedia(
        query: String,
        filter: MediaSearchFilter,
        pageToken: String?,
        pageSize: Int
    ): Flow<Resource<MediaSearchResult>> = flow {
        emit(Resource.Loading)
        try {
            val ref = firestore.collection("external_media").limit(pageSize.toLong())
            val snapshot = ref.get().await()
            val items = snapshot.documents.mapNotNull { doc ->
                val id = doc.id
                val title = doc.getString("title") ?: return@mapNotNull null
                val previewUrl = doc.getString("previewUrl") ?: doc.getString("url") ?: ""
                val downloadUrl = doc.getString("downloadUrl") ?: previewUrl
                val typeStr = doc.getString("type") ?: "IMAGE"
                val type = if (typeStr.equals("VIDEO", ignoreCase = true)) MediaType.VIDEO else MediaType.IMAGE
                val creatorName = doc.getString("creatorName") ?: "مكتبة سراج"
                val sourceUrl = doc.getString("sourceUrl") ?: "https://siraj.app"
                val licenseName = doc.getString("licenseName") ?: "Creative Commons"
                val commercialUseAllowed = doc.getBoolean("commercialUseAllowed") ?: true
                val attributionRequired = doc.getBoolean("attributionRequired") ?: false
                val attributionText = doc.getString("attributionText") ?: ""
                val width = doc.getLong("width")?.toInt() ?: 1920
                val height = doc.getLong("height")?.toInt() ?: 1080
                val durationMs = doc.getLong("durationMs")

                ExternalMediaItem(
                    id = id,
                    type = type,
                    previewUrl = previewUrl,
                    downloadUrl = downloadUrl,
                    title = title,
                    creatorName = creatorName,
                    sourceUrl = sourceUrl,
                    licenseName = licenseName,
                    commercialUseAllowed = commercialUseAllowed,
                    attributionRequired = attributionRequired,
                    attributionText = attributionText,
                    width = width,
                    height = height,
                    durationMs = durationMs
                )
            }.filter {
                if (query.isBlank()) true else it.title.contains(query, ignoreCase = true)
            }
            emit(Resource.Success(MediaSearchResult(items = items, nextPageToken = null, totalResults = items.size)))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "فشل في جلب الوسائط من الخادم"))
        }
    }
}
