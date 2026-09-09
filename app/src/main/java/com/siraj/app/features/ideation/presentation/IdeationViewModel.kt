package com.siraj.app.features.ideation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.data.repository.FirebaseAuthRepositoryImpl
import com.siraj.app.data.repository.FirebaseProjectRepositoryImpl
import com.siraj.app.data.services.GeminiIdeaGeneratorServiceImpl
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
    val selectedIdea: GeneratedIdea? = null,
    val isGeneratingScenario: Boolean = false,
    val scenario: ScenarioPlan? = null,
    val error: String? = null,
    val generationMode: GenerationMode = GenerationMode.IDEAS,
)

enum class GenerationMode { IDEAS, SCENARIO }

class IdeationViewModel(
    private val ideaGenerator: IdeaGeneratorService = GeminiIdeaGeneratorServiceImpl(),
    private val authRepository: AuthRepository = FirebaseAuthRepositoryImpl(),
    private val projectRepository: ProjectRepository = FirebaseProjectRepositoryImpl(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(IdeationUiState())
    val uiState: StateFlow<IdeationUiState> = _uiState.asStateFlow()

    fun updateRequest(update: (IdeaGenerationRequest) -> IdeaGenerationRequest) {
        _uiState.update { it.copy(request = update(it.request)) }
    }

    // ─── توليد الأفكار ───

    fun generateIdeas() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGenerating = true,
                    error = null,
                    generatedIdeas = emptyList(),
                    generationMode = GenerationMode.IDEAS,
                )
            }
            val res = ideaGenerator.generateIdeas(_uiState.value.request)
            when (res) {
                is Resource.Success ->
                    _uiState.update { it.copy(isGenerating = false, generatedIdeas = res.data) }
                is Resource.Error ->
                    _uiState.update { it.copy(isGenerating = false, error = res.message) }
                is Resource.Loading -> {}
            }
        }
    }

    // ─── تحويل فكرة لسيناريو ───

    fun generateScenario(idea: GeneratedIdea) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedIdea = idea,
                    isGeneratingScenario = true,
                    scenario = null,
                    error = null,
                    generationMode = GenerationMode.SCENARIO,
                )
            }
            val res = ideaGenerator.generateScenario(idea, _uiState.value.request)
            when (res) {
                is Resource.Success ->
                    _uiState.update { it.copy(isGeneratingScenario = false, scenario = res.data) }
                is Resource.Error ->
                    _uiState.update { it.copy(isGeneratingScenario = false, error = res.message) }
                is Resource.Loading -> {}
            }
        }
    }

    // ─── العودة لقائمة الأفكار ───

    fun backToIdeas() {
        _uiState.update {
            it.copy(
                generationMode = GenerationMode.IDEAS,
                selectedIdea = null,
                scenario = null,
                isGeneratingScenario = false,
            )
        }
    }

    // ─── إنشاء مشروع من السيناريو ───

    fun createProjectFromScenario(
        onProjectCreated: (String) -> Unit,
    ) {
        val scenario = _uiState.value.scenario ?: return
        val idea = _uiState.value.selectedIdea ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true) }
            val user = authRepository.currentUser.first()
            if (user != null && user.preferences.activeWorkspaceId != null) {
                val brief = ScenarioToProjectConverter.scenarioToContentBrief(
                    scenario = scenario,
                    audience = idea.audience,
                )
                val project =
                    Project(
                        ownerId = user.id,
                        workspaceId = user.preferences.activeWorkspaceId,
                        title = scenario.title,
                        description = scenario.overview,
                        brief = brief,
                    )
                val result = projectRepository.createProject(project)
                when (result) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        // إنشاء المشاهد من السيناريو
                        val projectId = result.data
                        val scenes = ScenarioToProjectConverter.convertScenarioToScenes(
                            projectId = projectId,
                            scenario = scenario,
                        )
                        // تحديث المشروع بالمشاهد
                        val projectWithScenes = project.copy(
                            id = projectId,
                            scenes = scenes,
                            sceneCount = scenes.size,
                            durationMs = scenes.sumOf { it.durationMs },
                        )
                        projectRepository.updateProject(projectWithScenes)
                        onProjectCreated(projectId)
                    }
                    is Resource.Error ->
                        _uiState.update { it.copy(error = result.message) }
                }
            } else {
                _uiState.update { it.copy(error = "لا توجد مساحة عمل نشطة") }
            }
            _uiState.update { it.copy(isGenerating = false) }
        }
    }

    // ─── حفظ السيناريو كمسودة ───

    fun saveScenarioDraft() {
        val scenario = _uiState.value.scenario ?: return
        val idea = _uiState.value.selectedIdea ?: return
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            if (user != null && user.preferences.activeWorkspaceId != null) {
                val brief = ScenarioToProjectConverter.scenarioToContentBrief(
                    scenario = scenario,
                    audience = idea.audience,
                )
                val project =
                    Project(
                        ownerId = user.id,
                        workspaceId = user.preferences.activeWorkspaceId,
                        title = scenario.title,
                        description = scenario.overview,
                        brief = brief,
                    )
                projectRepository.createProject(project)
            }
        }
    }

    // ─── إجراءات عامة ───

    fun clearIdeas() {
        _uiState.update { it.copy(generatedIdeas = emptyList()) }
    }

    fun dismissIdea(ideaId: String) {
        _uiState.update { state ->
            state.copy(generatedIdeas = state.generatedIdeas.filter { it.id != ideaId })
        }
    }

    fun reportIdea(ideaId: String, reason: String) {
        viewModelScope.launch {
            ideaGenerator.reportIdea(ideaId, reason)
            dismissIdea(ideaId)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // ─── تحويل مباشر لمشروع (بدون سيناريو) ───

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
                when (result) {
                    is Resource.Success -> onProjectCreated(result.data)
                    is Resource.Error ->
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
