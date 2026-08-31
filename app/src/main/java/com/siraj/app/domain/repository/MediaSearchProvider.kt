package com.siraj.app.domain.repository

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.MediaSearchFilter
import com.siraj.app.domain.models.MediaSearchResult
import kotlinx.coroutines.flow.Flow

interface MediaSearchProvider {
    fun searchMedia(
        query: String,
        filter: MediaSearchFilter,
        pageToken: String? = null,
        pageSize: Int = 20,
    ): Flow<Resource<MediaSearchResult>>
}
