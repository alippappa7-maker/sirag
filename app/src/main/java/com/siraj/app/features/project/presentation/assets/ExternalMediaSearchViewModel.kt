package com.siraj.app.features.project.presentation.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.*
import com.siraj.app.domain.repository.AssetRepository
import com.siraj.app.domain.repository.MediaSearchProvider
import com.siraj.app.domain.repository.ProjectRepository
import com.siraj.app.data.repository.FirebaseAssetRepositoryImpl
import com.siraj.app.data.repository.FirebaseProjectRepositoryImpl
import com.siraj.app.data.repository.MockMediaSearchProviderImpl
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ExternalMediaSearchViewModel(
    private val projectId: String,
    private val searchProvider: MediaSearchProvider = MockMediaSearchProviderImpl(),
    private val assetRepository: AssetRepository = FirebaseAssetRepositoryImpl(),
    private val projectRepository: ProjectRepository = FirebaseProjectRepositoryImpl()
) : ViewModel() {

    private val _projectState = MutableStateFlow<Project?>(null)

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private val _filter = MutableStateFlow(MediaSearchFilter())
    val filter = _filter.asStateFlow()

    private val _searchState = MutableStateFlow<Resource<MediaSearchResult>>(Resource.Success(MediaSearchResult(emptyList())))
    val searchState = _searchState.asStateFlow()

    private val _items = MutableStateFlow<List<ExternalMediaItem>>(emptyList())
    val items = _items.asStateFlow()

    private var currentNextPageToken: String? = null
    private var isSearching = false

    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage = _uiMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            val res = projectRepository.getProject(projectId)
            if (res is Resource.Success) {
                _projectState.value = res.data
            }
        }
    }

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
    }

    fun updateFilter(newFilter: MediaSearchFilter) {
        _filter.value = newFilter
        performSearch(isLoadMore = false)
    }

    fun performSearch(isLoadMore: Boolean = false) {
        if (isSearching) return
        val currentQuery = _query.value
        if (currentQuery.isBlank() && !isLoadMore) {
            _items.value = emptyList()
            _searchState.value = Resource.Success(MediaSearchResult(emptyList()))
            return
        }

        isSearching = true
        val pageToken = if (isLoadMore) currentNextPageToken else null

        viewModelScope.launch {
            searchProvider.searchMedia(currentQuery, _filter.value, pageToken).collect { res ->
                _searchState.value = res
                if (res is Resource.Success) {
                    val result = res.data
                    currentNextPageToken = result.nextPageToken
                    if (isLoadMore) {
                        _items.value = _items.value + result.items
                    } else {
                        _items.value = result.items
                    }
                }
                if (res !is Resource.Loading) {
                    isSearching = false
                }
            }
        }
    }

    fun loadMore() {
        if (currentNextPageToken != null && !isSearching) {
            performSearch(isLoadMore = true)
        }
    }

    fun addAssetToProject(item: ExternalMediaItem) {
        val proj = _projectState.value
        if (proj == null) {
            viewModelScope.launch { _uiMessage.emit("لم يتم تحميل بيانات المشروع بعد") }
            return
        }
        
        viewModelScope.launch {
            val assetType = if (item.type == MediaType.VIDEO) AssetType.VIDEO else AssetType.IMAGE
            val newAsset = Asset(
                ownerId = proj.ownerId,
                workspaceId = proj.workspaceId,
                projectId = proj.id,
                type = assetType,
                storagePath = "", // External asset, no local storage path initially
                downloadUrl = item.downloadUrl,
                thumbnailUrl = item.previewUrl,
                mimeType = if (assetType == AssetType.VIDEO) "video/mp4" else "image/jpeg",
                sizeBytes = 0L, 
                durationMs = item.durationMs,
                sourceUrl = item.sourceUrl,
                license = item.licenseName,
                attribution = item.attributionText,
                status = AssetStatus.READY
            )
            
            val res = assetRepository.addAsset(newAsset)
            if (res is Resource.Success) {
                _uiMessage.emit("تمت إضافة '${item.title}' للمشروع بنجاح")
            } else if (res is Resource.Error) {
                _uiMessage.emit(res.message)
            }
        }
    }
}

class ExternalMediaSearchViewModelFactory(
    private val projectId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExternalMediaSearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExternalMediaSearchViewModel(projectId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
