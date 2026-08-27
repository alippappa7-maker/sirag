import re

with open('app/src/main/java/com/siraj/app/features/project/presentation/ProjectEditorViewModel.kt', 'r') as f:
    content = f.read()

# Add TemplateRepository and AuthRepository to constructor
content = content.replace(
    'private val projectRepository: ProjectRepository = FirebaseProjectRepositoryImpl()',
    'private val projectRepository: ProjectRepository = FirebaseProjectRepositoryImpl(),\n    private val templateRepository: TemplateRepository = com.siraj.app.data.repository.FirebaseTemplateRepositoryImpl(),\n    private val authRepository: com.siraj.app.domain.repository.AuthRepository = com.siraj.app.data.repository.FirebaseAuthRepositoryImpl()'
)

# Add state flows for templates
state_flows = """
    private val _templates = MutableStateFlow<Resource<List<ContentTemplate>>>(Resource.Loading)
    val templates: StateFlow<Resource<List<ContentTemplate>>> = _templates.asStateFlow()

    private val _favorites = MutableStateFlow<List<String>>(emptyList())
    val favorites: StateFlow<List<String>> = _favorites.asStateFlow()
"""
content = content.replace('private val pendingUpdates = MutableSharedFlow<Project>(replay = 1)', state_flows + '\n    private val pendingUpdates = MutableSharedFlow<Project>(replay = 1)')

# Add loadTemplates inside init
content = content.replace('loadProject()', 'loadProject()\n        loadTemplates()')

# Add functions
funcs = """
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
"""
content = content.replace('fun deleteProject(onSuccess: () -> Unit) {', funcs + '\n    fun deleteProject(onSuccess: () -> Unit) {')

with open('app/src/main/java/com/siraj/app/features/project/presentation/ProjectEditorViewModel.kt', 'w') as f:
    f.write(content)
