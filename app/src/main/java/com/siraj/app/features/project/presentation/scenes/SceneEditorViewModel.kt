package com.siraj.app.features.project.presentation.scenes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.*
import com.siraj.app.domain.repository.ProjectRepository
import com.siraj.app.data.repository.FirebaseProjectRepositoryImpl
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SceneEditorViewModel(
    private val projectId: String,
    private val sceneId: String,
    private val projectRepository: ProjectRepository = FirebaseProjectRepositoryImpl()
) : ViewModel() {

    private val _projectState = MutableStateFlow<Resource<Project>>(Resource.Loading)
    val projectState: StateFlow<Resource<Project>> = _projectState.asStateFlow()

    private val _sceneState = MutableStateFlow<Scene?>(null)
    val sceneState: StateFlow<Scene?> = _sceneState.asStateFlow()

    // Default text formatting properties mapping to a new SceneText object since we just need it for UI
    private val _sceneTextState = MutableStateFlow(SceneText(sceneId = sceneId))
    val sceneTextState: StateFlow<SceneText> = _sceneTextState.asStateFlow()

    private val _undoStack = MutableStateFlow<List<Pair<Scene, SceneText>>>(emptyList())
    val canUndo: StateFlow<Boolean> = _undoStack.map { it.isNotEmpty() }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val _redoStack = MutableStateFlow<List<Pair<Scene, SceneText>>>(emptyList())
    val canRedo: StateFlow<Boolean> = _redoStack.map { it.isNotEmpty() }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage = _uiMessage.asSharedFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val resource = projectRepository.getProject(projectId)
            _projectState.value = resource
            if (resource is Resource.Success) {
                val scene = resource.data.scenes.find { it.id == sceneId }
                if (_sceneState.value == null && scene != null) {
                    _sceneState.value = scene
                }
            }
        }
    }

    fun updateSceneAndText(updatedScene: Scene, updatedText: SceneText) {
        val currentScene = _sceneState.value ?: return
        val currentText = _sceneTextState.value

        _undoStack.value = _undoStack.value + Pair(currentScene, currentText)
        _redoStack.value = emptyList()

        _sceneState.value = updatedScene
        _sceneTextState.value = updatedText

        saveToProject(updatedScene)
    }

    fun undo() {
        val stack = _undoStack.value.toMutableList()
        if (stack.isNotEmpty()) {
            val previous = stack.removeLast()
            val currentScene = _sceneState.value
            val currentText = _sceneTextState.value
            if (currentScene != null) {
                _redoStack.value = _redoStack.value + Pair(currentScene, currentText)
            }
            _undoStack.value = stack
            _sceneState.value = previous.first
            _sceneTextState.value = previous.second
            saveToProject(previous.first)
        }
    }

    fun redo() {
        val stack = _redoStack.value.toMutableList()
        if (stack.isNotEmpty()) {
            val next = stack.removeLast()
            val currentScene = _sceneState.value
            val currentText = _sceneTextState.value
            if (currentScene != null) {
                _undoStack.value = _undoStack.value + Pair(currentScene, currentText)
            }
            _redoStack.value = stack
            _sceneState.value = next.first
            _sceneTextState.value = next.second
            saveToProject(next.first)
        }
    }

    fun duplicateScene(onNavigateBack: () -> Unit) {
        val currentProjResource = _projectState.value
        val currentScene = _sceneState.value
        if (currentProjResource is Resource.Success && currentScene != null) {
            val project = currentProjResource.data
            val newScene = currentScene.copy(
                id = java.util.UUID.randomUUID().toString(),
                title = "${currentScene.title} (نسخة)",
                orderIndex = currentScene.orderIndex + 1,
                status = SceneStatus.DRAFT,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            // Adjust order index of subsequent scenes
            val updatedScenes = project.scenes.map {
                if (it.orderIndex > currentScene.orderIndex) {
                    it.copy(orderIndex = it.orderIndex + 1)
                } else it
            }.toMutableList()
            updatedScenes.add(newScene)

            val totalMs = updatedScenes.sumOf { it.durationMs }
            val updatedProject = project.copy(scenes = updatedScenes, durationMs = totalMs)

            viewModelScope.launch {
                projectRepository.updateProject(updatedProject)
                _uiMessage.emit("تم نسخ المشهد بنجاح.")
                onNavigateBack() // Go back to the scene list so the user sees the new scene
            }
        }
    }

    private fun saveToProject(sceneToSave: Scene) {
        val currentProjResource = _projectState.value
        if (currentProjResource is Resource.Success) {
            val project = currentProjResource.data

            val oldScene = project.scenes.find { it.id == sceneToSave.id }
            var finalStatus = sceneToSave.status
            var finalReviewState = project.reviewState
            var logs = project.reviewLogs

            // Warning and reset if modifying documented claim
            if (oldScene != null && oldScene.narrationText != sceneToSave.narrationText && oldScene.claimIds.isNotEmpty()) {
                viewModelScope.launch {
                    _uiMessage.emit("تم تعديل نص مرتبط بمصدر موثق. سيتم إعادة المشروع للمراجعة.")
                }
                finalStatus = SceneStatus.EDITED
                if (project.reviewState == ReviewState.APPROVED) {
                    finalReviewState = ReviewState.CHANGES_REQUESTED
                    logs = logs + ReviewLog(
                        projectId = project.id,
                        previousState = ReviewState.APPROVED,
                        newState = ReviewState.CHANGES_REQUESTED,
                        comments = "تم تعديل نص مشهد موثق (${sceneToSave.title})."
                    )
                }
            }

            val finalScene = sceneToSave.copy(status = finalStatus, updatedAt = System.currentTimeMillis())
            val updatedScenes = project.scenes.map { if (it.id == finalScene.id) finalScene else it }

            val totalMs = updatedScenes.sumOf { it.durationMs }
            val updatedProject = project.copy(
                scenes = updatedScenes,
                durationMs = totalMs,
                reviewState = finalReviewState,
                reviewLogs = logs
            )

            viewModelScope.launch {
                projectRepository.updateProject(updatedProject)
            }
        }
    }
}

class SceneEditorViewModelFactory(
    private val projectId: String,
    private val sceneId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SceneEditorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SceneEditorViewModel(projectId, sceneId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
