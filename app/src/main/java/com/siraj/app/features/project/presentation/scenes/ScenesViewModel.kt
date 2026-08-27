package com.siraj.app.features.project.presentation.scenes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.data.repository.FirebaseProjectRepositoryImpl
import com.siraj.app.domain.models.*
import com.siraj.app.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ScenesViewModel(
    private val projectId: String,
    private val projectRepository: ProjectRepository = FirebaseProjectRepositoryImpl()
) : ViewModel() {

    private val _projectState = MutableStateFlow<Resource<Project>>(Resource.Loading)
    val projectState: StateFlow<Resource<Project>> = _projectState.asStateFlow()

    private val _undoStack = MutableStateFlow<List<Project>>(emptyList())
    val canUndo: StateFlow<Boolean> = _undoStack.map { it.isNotEmpty() }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage = _uiMessage.asSharedFlow()

    init {
        loadProject()
    }

    private fun loadProject() {
        viewModelScope.launch {
            _projectState.value = Resource.Loading
            val projResult = projectRepository.getProject(projectId)
            _projectState.value = projResult
            
            // Auto-calculate duration on load
            if (projResult is Resource.Success) {
                calculateAndUpdateTotalDuration(projResult.data)
            }
        }
    }

    fun addScene() {
        val current = _projectState.value
        if (current is Resource.Success) {
            val project = current.data
            val newOrder = if (project.scenes.isEmpty()) 0 else project.scenes.maxOf { it.orderIndex } + 1
            val newScene = Scene(projectId = projectId, orderIndex = newOrder, title = "مشهد جديد")
            val updatedScenes = project.scenes + newScene
            val updatedProject = project.copy(scenes = updatedScenes)
            saveProject(updatedProject)
        }
    }

    fun duplicateScene(scene: Scene) {
        val current = _projectState.value
        if (current is Resource.Success) {
            val project = current.data
            val duplicated = scene.copy(
                id = java.util.UUID.randomUUID().toString(),
                orderIndex = project.scenes.maxOf { it.orderIndex } + 1,
                title = "${scene.title} (نسخة)",
                status = SceneStatus.DRAFT,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val updatedScenes = project.scenes + duplicated
            saveProject(project.copy(scenes = updatedScenes))
        }
    }

    fun deleteScene(sceneId: String) {
        val current = _projectState.value
        if (current is Resource.Success) {
            val project = current.data
            val scene = project.scenes.find { it.id == sceneId }
            
            if (scene?.status == SceneStatus.APPROVED) {
                viewModelScope.launch {
                    _uiMessage.emit("لا يمكن حذف مشهد معتمد. الرجاء إعادة مراجعة المشروع أولاً.")
                }
                return
            }
            
            val updatedScenes = project.scenes.filter { it.id != sceneId }
            saveProject(project.copy(scenes = updatedScenes))
        }
    }

    fun updateScene(updatedScene: Scene) {
        val current = _projectState.value
        if (current is Resource.Success) {
            val project = current.data
            // If the scene was approved and they modified it, reset its status to EDITED
            val sceneToUpdate = if (updatedScene.status == SceneStatus.APPROVED && isSceneModified(updatedScene, project)) {
                updatedScene.copy(status = SceneStatus.EDITED, updatedAt = System.currentTimeMillis())
            } else {
                updatedScene.copy(updatedAt = System.currentTimeMillis())
            }
            
            val updatedScenes = project.scenes.map { if (it.id == updatedScene.id) sceneToUpdate else it }
            saveProject(project.copy(scenes = updatedScenes))
        }
    }

    private fun isSceneModified(updatedScene: Scene, project: Project): Boolean {
        val oldScene = project.scenes.find { it.id == updatedScene.id } ?: return true
        return oldScene.narrationText != updatedScene.narrationText ||
               oldScene.durationMs != updatedScene.durationMs ||
               oldScene.transition != updatedScene.transition ||
               oldScene.backgroundType != updatedScene.backgroundType ||
               oldScene.claimIds != updatedScene.claimIds ||
               oldScene.assetIds != updatedScene.assetIds
    }

    fun reorderScenes(fromIndex: Int, toIndex: Int) {
        val current = _projectState.value
        if (current is Resource.Success) {
            val project = current.data
            val mutableScenes = project.scenes.sortedBy { it.orderIndex }.toMutableList()
            if (fromIndex in mutableScenes.indices && toIndex in mutableScenes.indices) {
                val item = mutableScenes.removeAt(fromIndex)
                mutableScenes.add(toIndex, item)
                val reordered = mutableScenes.mapIndexed { index, scene -> scene.copy(orderIndex = index, updatedAt = System.currentTimeMillis()) }
                saveProject(project.copy(scenes = reordered))
            }
        }
    }

    fun generateScenesFromPlan() {
        val current = _projectState.value
        if (current is Resource.Success) {
            val project = current.data
            if (project.scenes.isNotEmpty()) return // Already generated
            
            val plan = project.contentPlan ?: return
            val newScenes = plan.claims.mapIndexed { index, claim ->
                Scene(
                    projectId = projectId,
                    orderIndex = index,
                    title = "مشهد ${index + 1}",
                    narrationText = claim.text,
                    claimIds = listOf(claim.id),
                    durationMs = 5000L, // Estimate
                    status = SceneStatus.GENERATED
                )
            }
            saveProject(project.copy(scenes = newScenes))
        }
    }

    fun undoLastChange() {
        val stack = _undoStack.value.toMutableList()
        if (stack.isNotEmpty()) {
            val previousProject = stack.removeLast()
            _undoStack.value = stack
            
            // Note: Recalculate duration before restoring just in case
            val totalMs = previousProject.scenes.sumOf { it.durationMs }
            val projectToRestore = previousProject.copy(durationMs = totalMs)
            
            _projectState.value = Resource.Success(projectToRestore)
            viewModelScope.launch {
                projectRepository.updateProject(projectToRestore)
            }
        }
    }
    
    private fun calculateAndUpdateTotalDuration(project: Project) {
        val totalMs = project.scenes.sumOf { it.durationMs }
        if (totalMs != project.durationMs) {
            val updated = project.copy(durationMs = totalMs)
            _projectState.value = Resource.Success(updated)
            viewModelScope.launch {
                projectRepository.updateProject(updated)
            }
        }
    }

    private fun saveProject(project: Project) {
        val current = _projectState.value
        if (current is Resource.Success) {
            // Only push to undo stack if scenes actually changed to avoid massive stacks
            if (current.data.scenes != project.scenes) {
                _undoStack.value = _undoStack.value + current.data
            }
        }
        
        val totalMs = project.scenes.sumOf { it.durationMs }
        val updatedProject = project.copy(durationMs = totalMs)
        
        _projectState.value = Resource.Success(updatedProject)
        viewModelScope.launch {
            projectRepository.updateProject(updatedProject)
        }
    }
}

class ScenesViewModelFactory(private val projectId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScenesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScenesViewModel(projectId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
