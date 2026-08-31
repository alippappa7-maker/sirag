package com.siraj.app.features.studio.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.data.repository.FirebaseAuthRepositoryImpl
import com.siraj.app.data.repository.FirebaseProjectRepositoryImpl
import com.siraj.app.domain.models.Project
import com.siraj.app.domain.repository.AuthRepository
import com.siraj.app.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class StudioUiState(
    val projects: Resource<List<Project>> = Resource.Loading,
    val searchQuery: String = "",
    val sortOption: String = "الأحدث",
    val filterOption: String = "نشط",
    val activeWorkspaceId: String? = null,
)

class StudioViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepositoryImpl(),
    private val projectRepository: ProjectRepository = FirebaseProjectRepositoryImpl(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    private var allProjects: List<Project> = emptyList()

    init {
        loadProjects()
    }

    private fun loadProjects() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                if (user != null) {
                    val activeId = user.preferences.activeWorkspaceId
                    if (activeId != null) {
                        _uiState.update { it.copy(activeWorkspaceId = activeId) }
                        projectRepository.getAllProjects(activeId).collect { res ->
                            if (res is Resource.Success) {
                                allProjects = res.data
                                applyFiltersAndSort()
                            } else if (res is Resource.Error) {
                                _uiState.update { it.copy(projects = res) }
                            } else {
                                _uiState.update { it.copy(projects = Resource.Loading) }
                            }
                        }
                    } else {
                        _uiState.update { it.copy(projects = Resource.Success(emptyList())) }
                    }
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFiltersAndSort()
    }

    fun updateSortOption(option: String) {
        _uiState.update { it.copy(sortOption = option) }
        applyFiltersAndSort()
    }

    fun updateFilterOption(option: String) {
        _uiState.update { it.copy(filterOption = option) }
        applyFiltersAndSort()
    }

    private fun applyFiltersAndSort() {
        val query = _uiState.value.searchQuery.lowercase()
        val filter = _uiState.value.filterOption
        val sort = _uiState.value.sortOption

        var filtered = allProjects

        // Filter by Status
        filtered =
            when (filter) {
                "نشط" ->
                    filtered.filter {
                        it.status.name == "DRAFT" ||
                            it.status.name == "READY" ||
                            it.status.name == "COMPLETED" ||
                            it.status.name == "PROCESSING"
                    }
                "مؤرشف" -> filtered.filter { it.status.name == "ARCHIVED" }
                "محذوف" -> filtered.filter { it.status.name == "DELETED" }
                else -> filtered
            }

        // Search
        if (query.isNotBlank()) {
            filtered = filtered.filter { it.title.lowercase().contains(query) || it.description.lowercase().contains(query) }
        }

        // Sort
        filtered =
            when (sort) {
                "الأحدث" -> filtered.sortedByDescending { it.updatedAt }
                "الأقدم" -> filtered.sortedBy { it.updatedAt }
                "الاسم (أ-ي)" -> filtered.sortedBy { it.title }
                else -> filtered
            }

        _uiState.update { it.copy(projects = Resource.Success(filtered)) }
    }

    fun copyProject(
        projectId: String,
        currentUserId: String,
    ) {
        viewModelScope.launch { projectRepository.copyProject(projectId, currentUserId) }
    }

    fun archiveProject(projectId: String) {
        viewModelScope.launch { projectRepository.archiveProject(projectId) }
    }

    fun restoreProject(projectId: String) {
        viewModelScope.launch { projectRepository.restoreProject(projectId) }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch { projectRepository.deleteProject(projectId) }
    }
}

class StudioViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudioViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
