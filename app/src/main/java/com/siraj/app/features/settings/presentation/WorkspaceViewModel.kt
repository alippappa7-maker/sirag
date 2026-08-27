package com.siraj.app.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.data.repository.FirebaseAuthRepositoryImpl
import com.siraj.app.data.repository.FirebaseWorkspaceRepositoryImpl
import com.siraj.app.domain.models.*
import com.siraj.app.domain.repository.AuthRepository
import com.siraj.app.domain.repository.WorkspaceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class WorkspaceUiState(
    val currentUser: UserProfile? = null,
    val workspaces: List<Workspace> = emptyList(),
    val activeWorkspace: Workspace? = null,
    val currentUserRole: WorkspaceRole? = null,
    val members: Resource<List<WorkspaceMember>> = Resource.Loading,
    val invitations: List<WorkspaceInvitation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
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
                    // Fetch user workspaces
                    workspaceRepository.getUserWorkspaces(user.id).collect { workspacesRes ->
                        if (workspacesRes is Resource.Success && workspacesRes.data.isNotEmpty()) {
                            val activeId = user.preferences.activeWorkspaceId ?: workspacesRes.data.first().id
                            val activeWs = workspacesRes.data.find { it.id == activeId } ?: workspacesRes.data.first()
                            
                            _uiState.update { it.copy(
                                workspaces = workspacesRes.data,
                                activeWorkspace = activeWs
                            ) }
                            
                            // Load members for active workspace
                            workspaceRepository.getWorkspaceMembers(activeWs.id).collect { membersRes ->
                                val currentUserRole = if (membersRes is Resource.Success) {
                                    membersRes.data.find { it.userId == user.id }?.role
                                } else null
                                
                                _uiState.update { it.copy(
                                    members = membersRes,
                                    currentUserRole = currentUserRole
                                ) }
                            }
                        }
                    }
                    
                    // Fetch user invitations
                    workspaceRepository.getUserInvitations(user.email).collect { invRes ->
                        if (invRes is Resource.Success) {
                            _uiState.update { it.copy(invitations = invRes.data) }
                        }
                    }
                }
            }
        }
    }

    fun setActiveWorkspace(workspaceId: String) {
        viewModelScope.launch {
            val user = _uiState.value.currentUser ?: return@launch
            authRepository.updatePreferences(user.preferences.copy(activeWorkspaceId = workspaceId))
            // The flow in loadData will react and update activeWorkspace and members
        }
    }

    fun createWorkspace(name: String, type: WorkspaceType) {
        viewModelScope.launch {
            val user = _uiState.value.currentUser ?: return@launch
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val res = workspaceRepository.createWorkspace(name, type, user)) {
                is Resource.Success -> {
                    setActiveWorkspace(res.data)
                }
                is Resource.Error -> _uiState.update { it.copy(error = res.message) }
                else -> {}
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun inviteMember(email: String, role: WorkspaceRole) {
        viewModelScope.launch {
            val user = _uiState.value.currentUser ?: return@launch
            val workspace = _uiState.value.activeWorkspace ?: return@launch
            val currentUserRole = _uiState.value.currentUserRole ?: return@launch
            
            if (currentUserRole == WorkspaceRole.EDITOR || currentUserRole == WorkspaceRole.REVIEWER || currentUserRole == WorkspaceRole.VIEWER) {
                _uiState.update { it.copy(error = "ليس لديك صلاحية لدعوة أعضاء") }
                return@launch
            }
            
            _uiState.update { it.copy(isLoading = true, error = null) }
            val res = workspaceRepository.inviteMember(workspace.id, email, role, user.id)
            if (res is Resource.Error) {
                _uiState.update { it.copy(error = res.message) }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun respondToInvitation(invitationId: String, accept: Boolean) {
        viewModelScope.launch {
            val user = _uiState.value.currentUser ?: return@launch
            _uiState.update { it.copy(isLoading = true, error = null) }
            val res = workspaceRepository.respondToInvitation(invitationId, accept, user)
            if (res is Resource.Error) {
                _uiState.update { it.copy(error = res.message) }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun updateMemberRole(userId: String, newRole: WorkspaceRole) {
        viewModelScope.launch {
            val workspace = _uiState.value.activeWorkspace ?: return@launch
            val currentUserRole = _uiState.value.currentUserRole ?: return@launch
            
            if (currentUserRole != WorkspaceRole.OWNER && currentUserRole != WorkspaceRole.MANAGER) {
                _uiState.update { it.copy(error = "ليس لديك صلاحية لتعديل الأدوار") }
                return@launch
            }
            
            if (newRole == WorkspaceRole.OWNER && currentUserRole != WorkspaceRole.OWNER) {
                _uiState.update { it.copy(error = "فقط المالك يمكنه تعيين مالك جديد") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }
            val res = workspaceRepository.updateMemberRole(workspace.id, userId, newRole)
            if (res is Resource.Error) {
                _uiState.update { it.copy(error = res.message) }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun removeMember(userId: String) {
        viewModelScope.launch { 
             val workspace = _uiState.value.activeWorkspace ?: return@launch
             val currentUserRole = _uiState.value.currentUserRole ?: return@launch
             
             if (currentUserRole != WorkspaceRole.OWNER && currentUserRole != WorkspaceRole.MANAGER) {
                 _uiState.update { it.copy(error = "ليس لديك صلاحية لإزالة الأعضاء") }
                 return@launch
             }
             
             _uiState.update { it.copy(isLoading = true, error = null) }
             val res = workspaceRepository.removeMember(workspace.id, userId)
             if (res is Resource.Error) {
                 _uiState.update { it.copy(error = res.message) }
             }
             _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun leaveWorkspace() {
        viewModelScope.launch {
            val user = _uiState.value.currentUser ?: return@launch
            val workspace = _uiState.value.activeWorkspace ?: return@launch
            
            if (workspace.ownerId == user.id) {
                _uiState.update { it.copy(error = "لا يمكن للمالك مغادرة مساحة العمل، يرجى نقل الملكية أولاً") }
                return@launch
            }
            
            _uiState.update { it.copy(isLoading = true, error = null) }
            val res = workspaceRepository.leaveWorkspace(workspace.id, user.id)
            if (res is Resource.Success) {
                // Find another workspace to switch to
                val otherWs = _uiState.value.workspaces.find { it.id != workspace.id }
                if (otherWs != null) {
                    setActiveWorkspace(otherWs.id)
                }
            } else if (res is Resource.Error) {
                _uiState.update { it.copy(error = res.message) }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    
    fun transferOwnership(newOwnerId: String) {
        viewModelScope.launch {
            val user = _uiState.value.currentUser ?: return@launch
            val workspace = _uiState.value.activeWorkspace ?: return@launch
            val currentUserRole = _uiState.value.currentUserRole ?: return@launch
            
            if (currentUserRole != WorkspaceRole.OWNER) {
                 _uiState.update { it.copy(error = "فقط المالك يمكنه نقل الملكية") }
                 return@launch
            }
            
            _uiState.update { it.copy(isLoading = true, error = null) }
            val res = workspaceRepository.transferOwnership(workspace.id, newOwnerId, user.id)
            if (res is Resource.Error) {
                _uiState.update { it.copy(error = res.message) }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    
    fun archiveWorkspace() {
        viewModelScope.launch {
            val workspace = _uiState.value.activeWorkspace ?: return@launch
            val currentUserRole = _uiState.value.currentUserRole ?: return@launch
            
            if (currentUserRole != WorkspaceRole.OWNER) {
                 _uiState.update { it.copy(error = "فقط المالك يمكنه أرشفة مساحة العمل") }
                 return@launch
            }
            
            _uiState.update { it.copy(isLoading = true, error = null) }
            val res = workspaceRepository.archiveWorkspace(workspace.id)
            if (res is Resource.Error) {
                _uiState.update { it.copy(error = res.message) }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
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
