package com.siraj.app.features.ideation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.data.repository.FirebaseAuthRepositoryImpl
import com.siraj.app.data.repository.FirebaseProjectRepositoryImpl
import com.siraj.app.domain.models.*
import com.siraj.app.domain.repository.AuthRepository
import com.siraj.app.domain.repository.ProjectRepository
import com.siraj.app.domain.services.IdeaGeneratorService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class IdeationUiState(
    val request: IdeaGenerationRequest = IdeaGenerationRequest(),
    val isGenerating: Boolean = false,
    val generatedIdeas: List<GeneratedIdea> = emptyList(),
    val error: String? = null,
)

class IdeationViewModel(
    private val ideaGenerator: IdeaGeneratorService =
        com.siraj.app.data.services
            .FirebaseIdeaGeneratorServiceImpl(),
    private val authRepository: AuthRepository = FirebaseAuthRepositoryImpl(),
    private val projectRepository: ProjectRepository = FirebaseProjectRepositoryImpl(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(IdeationUiState())
    val uiState: StateFlow<IdeationUiState> = _uiState.asStateFlow()

    fun updateRequest(update: (IdeaGenerationRequest) -> IdeaGenerationRequest) {
        _uiState.update { it.copy(request = update(it.request)) }
    }

    fun generateIdeas() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, error = null, generatedIdeas = emptyList()) }
            val res = ideaGenerator.generateIdeas(_uiState.value.request)
            if (res is Resource.Success) {
                _uiState.update { it.copy(isGenerating = false, generatedIdeas = res.data) }
            } else if (res is Resource.Error) {
                _uiState.update { it.copy(isGenerating = false, error = res.message) }
            }
        }
    }

    fun clearIdeas() {
        _uiState.update { it.copy(generatedIdeas = emptyList()) }
    }

    fun dismissIdea(ideaId: String) {
        _uiState.update { state ->
            state.copy(generatedIdeas = state.generatedIdeas.filter { it.id != ideaId })
        }
    }

    fun reportIdea(
        ideaId: String,
        reason: String,
    ) {
        viewModelScope.launch {
            ideaGenerator.reportIdea(ideaId, reason)
            dismissIdea(ideaId)
        }
    }

    fun convertToProject(
        idea: GeneratedIdea,
        onProjectCreated: (String) -> Unit,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true) }
            val user = authRepository.currentUser.first()
            if (user != null && user.preferences.activeWorkspaceId != null) {
                val brief =
                    ContentBrief(
                        idea = idea.summary + "\n\nالخطاف: " + idea.hook,
                        targetAudience = idea.audience,
                        hasQuran = _uiState.value.request.hasReligiousElement,
                        hasHadith = _uiState.value.request.hasReligiousElement,
                        hasFatwa = _uiState.value.request.hasReligiousElement,
                    )
                val project =
                    Project(
                        ownerId = user.id,
                        workspaceId = user.preferences.activeWorkspaceId,
                        title = idea.title,
                        description = idea.summary,
                        brief = brief,
                    )
                val result = projectRepository.createProject(project)
                if (result is Resource.Success) {
                    onProjectCreated(result.data)
                } else if (result is Resource.Error) {
                    _uiState.update { it.copy(error = result.message) }
                }
            } else {
                _uiState.update { it.copy(error = "لا توجد مساحة عمل نشطة") }
            }
            _uiState.update { it.copy(isGenerating = false) }
        }
    }
}

class IdeationViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IdeationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IdeationViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
