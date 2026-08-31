package com.siraj.app.data.repository

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.ExternalMediaItem
import com.siraj.app.domain.models.MediaSearchFilter
import com.siraj.app.domain.models.MediaSearchResult
import com.siraj.app.domain.models.MediaType
import com.siraj.app.domain.repository.MediaSearchProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MockMediaSearchProviderImpl : MediaSearchProvider {
    
    // Simple cache for MVP
    private val cache = mutableMapOf<String, MediaSearchResult>()

    override fun searchMedia(query: String, filter: MediaSearchFilter, pageToken: String?, pageSize: Int): Flow<Resource<MediaSearchResult>> = flow {
        emit(Resource.Loading)
        
        val cacheKey = "$query-${filter.type}-${filter.orientation}-$pageToken"
        if (cache.containsKey(cacheKey)) {
            cache[cacheKey]?.let { emit(Resource.Success(it)) }
            return@flow
        }
        
        delay(1000) // simulate network
        
        if (query.isBlank()) {
            emit(Resource.Success(MediaSearchResult(emptyList())))
            return@flow
        }

        if (query.lowercase() == "error") {
            emit(Resource.Error("فشل الاتصال بمزود الوسائط"))
            return@flow
        }
        
        val items = mutableListOf<ExternalMediaItem>()
        val pageNum = pageToken?.toIntOrNull() ?: 1
        val startIndex = (pageNum - 1) * pageSize
        
        // Generate mock results
        for (i in 1..pageSize) {
            val idx = startIndex + i
            items.add(
                ExternalMediaItem(
                    id = "mock_${filter.type.name.lowercase()}_$idx",
                    type = filter.type,
                    previewUrl = "https://picsum.photos/seed/$query$idx/300/200",
                    downloadUrl = "https://picsum.photos/seed/$query$idx/1920/1080",
                    title = "${if (filter.type == MediaType.VIDEO) "فيديو" else "صورة"} $query - $idx",
                    creatorName = "المصور $idx",
                    sourceUrl = "https://example.com/photo/$idx",
                    licenseName = "CC BY 4.0",
                    commercialUseAllowed = true,
                    attributionRequired = true,
                    attributionText = "عمل بواسطة المصور $idx عبر Example",
                    durationMs = if (filter.type == MediaType.VIDEO) 15000L else null
                )
            )
        }
        
        val result = MediaSearchResult(
            items = items,
            nextPageToken = if (pageNum < 5) (pageNum + 1).toString() else null, // Mock 5 pages max
            totalResults = 100
        )
        
        cache[cacheKey] = result
        emit(Resource.Success(result))
    }
}
