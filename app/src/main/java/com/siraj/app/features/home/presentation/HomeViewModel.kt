package com.siraj.app.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.data.repository.FirebaseAuthRepositoryImpl
import com.siraj.app.data.repository.FirebaseProjectRepositoryImpl
import com.siraj.app.data.repository.FirebaseWorkspaceRepositoryImpl
import com.siraj.app.domain.models.Project
import com.siraj.app.domain.models.UserProfile
import com.siraj.app.domain.models.Workspace
import com.siraj.app.domain.models.WorkspaceType
import com.siraj.app.domain.repository.AuthRepository
import com.siraj.app.domain.repository.ProjectRepository
import com.siraj.app.domain.repository.WorkspaceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val userProfile: UserProfile? = null,
    val activeWorkspace: Workspace? = null,
    val recentProjects: Resource<List<Project>> = Resource.Loading,
    val isOffline: Boolean = false,
)

class HomeViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepositoryImpl(),
    private val projectRepository: ProjectRepository = FirebaseProjectRepositoryImpl(),
    private val workspaceRepository: WorkspaceRepository = FirebaseWorkspaceRepositoryImpl(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update { it.copy(userProfile = user) }
                if (user != null) {
                    workspaceRepository.getUserWorkspaces(user.id).collect { workspacesRes ->
                        if (workspacesRes is Resource.Success) {
                            val workspaces = workspacesRes.data
                            if (workspaces.isEmpty()) {
                                // Create personal workspace
                                val res = workspaceRepository.createWorkspace("مساحتي الشخصية", WorkspaceType.PERSONAL, user)
                                if (res is Resource.Success) {
                                    authRepository.updatePreferences(user.preferences.copy(activeWorkspaceId = res.data))
                                }
                            } else {
                                val activeId = user.preferences.activeWorkspaceId ?: workspaces.first().id
                                val activeWs = workspaces.find { it.id == activeId } ?: workspaces.first()

                                if (user.preferences.activeWorkspaceId != activeWs.id) {
                                    authRepository.updatePreferences(user.preferences.copy(activeWorkspaceId = activeWs.id))
                                }

                                _uiState.update { it.copy(activeWorkspace = activeWs) }

                                // Fetch projects for active workspace
                                projectRepository.getRecentProjects(activeWs.id, 5).collect { projectsRes ->
                                    _uiState.update { it.copy(recentProjects = projectsRes) }
                                }
                            }
                        }
                    }
                } else {
                    _uiState.update { it.copy(recentProjects = Resource.Success(emptyList()), activeWorkspace = null) }
                }
            }
        }
    }

    fun createProject(
        title: String,
        onSuccess: (String) -> Unit,
    ) {
        viewModelScope.launch {
            val user = _uiState.value.userProfile ?: return@launch
            val workspace = _uiState.value.activeWorkspace ?: return@launch
            val newProject =
                Project(
                    ownerId = user.id,
                    workspaceId = workspace.id,
                    title = title,
                    description = "",
                )
            val result = projectRepository.createProject(newProject)
            if (result is Resource.Success) {
                onSuccess(result.data)
            }
        }
    }
}

class HomeViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
