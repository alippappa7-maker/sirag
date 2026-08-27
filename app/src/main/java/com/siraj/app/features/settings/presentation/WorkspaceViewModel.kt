package com.siraj.app.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.data.repository.FirebaseAuthRepositoryImpl
import com.siraj.app.data.repository.FirebaseWorkspaceRepositoryImpl
import com.siraj.app.domain.models.UserProfile
import com.siraj.app.domain.models.Workspace
import com.siraj.app.domain.models.WorkspaceMember
import com.siraj.app.domain.models.WorkspaceRole
import com.siraj.app.domain.repository.AuthRepository
import com.siraj.app.domain.repository.WorkspaceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class WorkspaceUiState(
    val currentUser: UserProfile? = null,
    val activeWorkspace: Workspace? = null,
    val members: Resource<List<WorkspaceMember>> = Resource.Loading
)

class WorkspaceViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepositoryImpl(),
    private val workspaceRepository: WorkspaceRepository = FirebaseWorkspaceRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkspaceUiState())
    val uiState: StateFlow<WorkspaceUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update { it.copy(currentUser = user) }
                if (user != null) {
                    workspaceRepository.getUserWorkspaces(user.id).collect { workspacesRes ->
                        if (workspacesRes is Resource.Success && workspacesRes.data.isNotEmpty()) {
                            val activeId = user.preferences.activeWorkspaceId ?: workspacesRes.data.first().id
                            val activeWs = workspacesRes.data.find { it.id == activeId } ?: workspacesRes.data.first()
                            
                            _uiState.update { it.copy(activeWorkspace = activeWs) }
                            
                            workspaceRepository.getWorkspaceMembers(activeWs.id).collect { membersRes ->
                                _uiState.update { it.copy(members = membersRes) }
                            }
                        }
                    }
                }
            }
        }
    }

    fun inviteMember(email: String) {
        viewModelScope.launch {
            val user = _uiState.value.currentUser ?: return@launch
            val workspace = _uiState.value.activeWorkspace ?: return@launch
            workspaceRepository.inviteMember(workspace.id, email, WorkspaceRole.VIEWER, user.id)
        }
    }

    fun removeMember(userId: String) {
        viewModelScope.launch {
             val workspace = _uiState.value.activeWorkspace ?: return@launch
             workspaceRepository.removeMember(workspace.id, userId)
        }
    }
}

class WorkspaceViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkspaceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkspaceViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
