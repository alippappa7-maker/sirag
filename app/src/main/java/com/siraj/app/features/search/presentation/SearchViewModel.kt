package com.siraj.app.features.search.presentation

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.search.*
import com.siraj.app.domain.repository.search.SearchRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val filter: SearchFilter = SearchFilter(),
    val searchResult: GlobalSearchResult = GlobalSearchResult(),
    val suggestions: List<SearchSuggestion> = emptyList(),
    val history: List<SearchHistoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val isFilterSheetOpen: Boolean = false,
    val hasSearched: Boolean = false,
    val currentPage: Int = 1
) {
    val activeFilterCount: Int
        get() {
            var count = 0
            if (filter.category != SearchCategory.ALL) count++
            if (filter.language != SearchLanguage.ALL) count++
            if (filter.contentType != SearchContentType.ALL) count++
            if (filter.verificationFilter != SearchVerificationFilter.ALL_APPROVED) count++
            if (filter.sortOption != SearchSortOption.RELEVANCE) count++
            if (filter.onlyPrivateProjects) count++
            return count
        }
}

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val searchRepository: SearchRepository,
    private val currentUserId: String? = null,
    private val currentWorkspaceId: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _queryFlow = MutableStateFlow("")
    private var searchJob: Job? = null

    init {
        // Observe search history
        viewModelScope.launch {
            searchRepository.getSearchHistory(currentUserId).collect { historyList ->
                _uiState.update { it.copy(history = historyList) }
            }
        }

        // Load initial suggestions
        loadSuggestions("")

        // Debounce query typing
        viewModelScope.launch {
            _queryFlow
                .debounce(350)
                .distinctUntilChanged()
                .collect { debouncedQuery ->
                    if (debouncedQuery.isNotBlank()) {
                        performSearch(debouncedQuery, _uiState.value.filter, page = 1)
                    } else {
                        _uiState.update {
                            it.copy(
                                searchResult = GlobalSearchResult(),
                                hasSearched = false,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                        loadSuggestions("")
                    }
                }
        }
    }

    fun onQueryChanged(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        _queryFlow.value = newQuery
        loadSuggestions(newQuery)
    }

    fun onSearchSubmitted(queryToSearch: String? = null) {
        val q = queryToSearch ?: _uiState.value.query
        if (q.isNotBlank()) {
            _uiState.update { it.copy(query = q) }
            _queryFlow.value = q
            performSearch(q, _uiState.value.filter, page = 1)
        }
    }

    fun onCategorySelected(category: SearchCategory) {
        val updatedFilter = _uiState.value.filter.copy(category = category)
        _uiState.update { it.copy(filter = updatedFilter) }
        if (_uiState.value.query.isNotBlank()) {
            performSearch(_uiState.value.query, updatedFilter, page = 1)
        }
    }

    fun onFilterUpdated(newFilter: SearchFilter) {
        _uiState.update { it.copy(filter = newFilter, isFilterSheetOpen = false) }
        if (_uiState.value.query.isNotBlank()) {
            performSearch(_uiState.value.query, newFilter, page = 1)
        }
    }

    fun resetFilters() {
        val defaultFilter = SearchFilter(category = _uiState.value.filter.category)
        _uiState.update { it.copy(filter = defaultFilter) }
        if (_uiState.value.query.isNotBlank()) {
            performSearch(_uiState.value.query, defaultFilter, page = 1)
        }
    }

    fun setFilterSheetOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isFilterSheetOpen = isOpen) }
    }

    fun loadNextPage() {
        val currentState = _uiState.value
        if (currentState.isLoadingMore || !currentState.searchResult.hasMore || currentState.isLoading) return

        val nextPage = currentState.currentPage + 1
        _uiState.update { it.copy(isLoadingMore = true) }

        viewModelScope.launch {
            val result = searchRepository.search(
                query = currentState.query,
                filter = currentState.filter,
                page = nextPage,
                pageSize = 20,
                userId = currentUserId,
                workspaceId = currentWorkspaceId
            )

            when (result) {
                is Resource.Success -> {
                    val combinedItems = currentState.searchResult.items + result.data.items
                    _uiState.update {
                        it.copy(
                            searchResult = result.data.copy(items = combinedItems),
                            currentPage = nextPage,
                            isLoadingMore = false
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoadingMore = false) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun performSearch(query: String, filter: SearchFilter, page: Int) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, hasSearched = true, currentPage = page) }

            val result = searchRepository.search(
                query = query,
                filter = filter,
                page = page,
                pageSize = 20,
                userId = currentUserId,
                workspaceId = currentWorkspaceId
            )

            when (result) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            searchResult = result.data,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "فشل في إتمام عملية البحث"
                        )
                    }
                }
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    private fun loadSuggestions(query: String) {
        viewModelScope.launch {
            val suggestions = searchRepository.getSuggestions(query)
            _uiState.update { it.copy(suggestions = suggestions) }
        }
    }

    fun deleteHistoryItem(id: String) {
        viewModelScope.launch {
            searchRepository.deleteHistoryItem(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            searchRepository.clearAllHistory(currentUserId)
        }
    }
}

class SearchViewModelFactory(
    private val application: Application,
    private val currentUserId: String? = "user_default",
    private val currentWorkspaceId: String? = "workspace_default"
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            val quranRepo = com.siraj.app.data.repository.QuranRepositoryImpl(application)
            val audioRepo = com.siraj.app.data.repository.audio.AudioRepositoryImpl()
            val templateRepo = com.siraj.app.data.repository.FirebaseTemplateRepositoryImpl()
            val projectRepo = com.siraj.app.data.repository.FirebaseProjectRepositoryImpl()
            val historyDao = com.siraj.app.data.local.SearchHistoryDatabase.getInstance(application).searchHistoryDao()
            
            val searchRepository = com.siraj.app.data.repository.search.UnifiedSearchRepositoryImpl(
                quranRepository = quranRepo,
                audioRepository = audioRepo,
                templateRepository = templateRepo,
                projectRepository = projectRepo,
                historyDao = historyDao
            )
            return SearchViewModel(searchRepository, currentUserId, currentWorkspaceId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
