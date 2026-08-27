package com.siraj.app.features.project.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.data.repository.FirebaseProjectRepositoryImpl
import com.siraj.app.domain.models.*
import com.siraj.app.domain.repository.TemplateRepository
import com.siraj.app.domain.models.ContentTemplate
import com.siraj.app.domain.repository.AuthRepository
import com.siraj.app.domain.repository.ProjectRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class SaveState {
    object Idle : SaveState()
    object Saving : SaveState()
    object Saved : SaveState()
    data class Error(val message: String) : SaveState()
}

@OptIn(FlowPreview::class)
class ProjectEditorViewModel(
    private val projectId: String,
    private val projectRepository: ProjectRepository = FirebaseProjectRepositoryImpl(),
    private val templateRepository: TemplateRepository = com.siraj.app.data.repository.FirebaseTemplateRepositoryImpl(),
    private val authRepository: com.siraj.app.domain.repository.AuthRepository = com.siraj.app.data.repository.FirebaseAuthRepositoryImpl()
) : ViewModel() {

    private val _projectState = MutableStateFlow<Resource<Project>>(Resource.Loading)
    val projectState: StateFlow<Resource<Project>> = _projectState.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    
    private val _templates = MutableStateFlow<Resource<List<ContentTemplate>>>(Resource.Loading)
    val templates: StateFlow<Resource<List<ContentTemplate>>> = _templates.asStateFlow()

    private val _favorites = MutableStateFlow<List<String>>(emptyList())
    val favorites: StateFlow<List<String>> = _favorites.asStateFlow()

    private val pendingUpdates = MutableSharedFlow<Project>(replay = 1)

    init {
        loadProject()
        loadTemplates()
        
        viewModelScope.launch {
            pendingUpdates
                .debounce(1500L) // Auto-save after 1.5s
                .collect { projectToSave ->
                    _saveState.value = SaveState.Saving
                    val result = projectRepository.updateProject(projectToSave)
                    if (result is Resource.Success) {
                        _saveState.value = SaveState.Saved
                    } else if (result is Resource.Error) {
                        _saveState.value = SaveState.Error(result.message)
                    }
                }
        }
    }

    private fun loadProject() {
        viewModelScope.launch {
            _projectState.value = Resource.Loading
            _projectState.value = projectRepository.getProject(projectId)
        }
    }

    fun updateTitle(newTitle: String) {
        val current = _projectState.value
        if (current is Resource.Success) {
            val updated = current.data.copy(title = newTitle)
            _projectState.value = Resource.Success(updated)
            viewModelScope.launch { pendingUpdates.emit(updated) }
        }
    }
    
    fun updateDescription(newDescription: String) {
        val current = _projectState.value
        if (current is Resource.Success) {
            val updated = current.data.copy(description = newDescription)
            _projectState.value = Resource.Success(updated)
            viewModelScope.launch { pendingUpdates.emit(updated) }
        }
    }

    fun updateBrief(update: (ContentBrief) -> ContentBrief) {
        val current = _projectState.value
        if (current is Resource.Success) {
            val updatedBrief = update(current.data.brief)
            val updatedProject = current.data.copy(brief = updatedBrief)
            _projectState.value = Resource.Success(updatedProject)
            viewModelScope.launch { pendingUpdates.emit(updatedProject) }
        }
    }
    
    fun generatePlan(onGenerated: () -> Unit) {
        // Will connect to Gemini later, for now we just change project status to PROCESSING
        val current = _projectState.value
        if (current is Resource.Success) {
            val updated = current.data.copy(status = ProjectStatus.PROCESSING)
            _projectState.value = Resource.Success(updated)
            viewModelScope.launch { 
                projectRepository.updateProject(updated) 
                onGenerated()
            }
        }
    }

    
    private fun loadTemplates() {
        viewModelScope.launch {
            templateRepository.getActiveTemplates().collect { res ->
                _templates.value = res
                if (res is Resource.Success && res.data.isEmpty()) {
                    // Seed if empty for demo purposes (usually done by admin)
                    templateRepository.seedDefaultTemplates()
                }
            }
        }
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                if (user != null) {
                    templateRepository.getFavoriteTemplates(user.id).collect { res ->
                        if (res is Resource.Success) {
                            _favorites.value = res.data
                        }
                    }
                }
            }
        }
    }

    fun applyTemplate(template: ContentTemplate) {
        val current = _projectState.value
        if (current is Resource.Success) {
            val updatedBrief = current.data.brief.copy(
                template = template.name,
                targetAudience = template.targetAudience,
                platform = template.recommendedPlatform,
                duration = template.recommendedDuration,
                visualStyle = template.sceneStyle,
                hasQuran = template.hasQuran,
                hasHadith = template.hasHadith,
                hasFatwa = template.hasFatwa
            )
            val updatedProject = current.data.copy(brief = updatedBrief)
            _projectState.value = Resource.Success(updatedProject)
            viewModelScope.launch { pendingUpdates.emit(updatedProject) }
        }
    }

    fun toggleFavorite(templateId: String) {
        viewModelScope.launch {
            authRepository.currentUser.first()?.let { user ->
                val isFav = _favorites.value.contains(templateId)
                templateRepository.toggleFavorite(user.id, templateId, !isFav)
            }
        }
    }

    fun deleteProject(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = projectRepository.deleteProject(projectId)
            if (result is Resource.Success) {
                onSuccess()
            }
        }
    }
}

class ProjectEditorViewModelFactory(private val projectId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProjectEditorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProjectEditorViewModel(projectId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}