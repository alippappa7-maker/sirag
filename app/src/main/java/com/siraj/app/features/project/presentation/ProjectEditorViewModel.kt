package com.siraj.app.features.project.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.data.repository.FirebaseProjectRepositoryImpl
import com.siraj.app.domain.models.Project
import com.siraj.app.domain.models.ProjectActivity
import com.siraj.app.domain.models.ActivityType
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
    private val projectRepository: ProjectRepository = FirebaseProjectRepositoryImpl()
) : ViewModel() {

    private val _projectState = MutableStateFlow<Resource<Project>>(Resource.Loading)
    val projectState: StateFlow<Resource<Project>> = _projectState.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    private val pendingUpdates = MutableSharedFlow<Project>(replay = 1)

    init {
        loadProject()
        
        viewModelScope.launch {
            pendingUpdates
                .debounce(1500L) // Wait 1.5 seconds after last edit to save
                .collect { projectToSave ->
                    _saveState.value = SaveState.Saving
                    val result = projectRepository.updateProject(projectToSave)
                    if (result is Resource.Success) {
                        _saveState.value = SaveState.Saved
                        projectRepository.logActivity(
                            ProjectActivity(
                                projectId = projectToSave.id,
                                userId = projectToSave.ownerId,
                                type = ActivityType.EDITED,
                                details = "تم تحديث المشروع تلقائياً"
                            )
                        )
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
