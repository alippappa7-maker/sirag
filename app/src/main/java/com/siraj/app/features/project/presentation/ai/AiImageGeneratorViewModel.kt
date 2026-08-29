package com.siraj.app.features.project.presentation.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.*
import com.siraj.app.domain.repository.AssetRepository
import com.siraj.app.domain.repository.ProjectRepository
import com.siraj.app.domain.services.AiImageGeneratorService
import com.siraj.app.data.repository.FirebaseAssetRepositoryImpl
import com.siraj.app.data.repository.FirebaseProjectRepositoryImpl
import com.siraj.app.data.services.FirebaseAiImageGeneratorServiceImpl
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AiImageGeneratorViewModel(
    val projectId: String,
    val initialSceneId: String? = null,
    private val aiService: AiImageGeneratorService = FirebaseAiImageGeneratorServiceImpl(),
    private val projectRepository: ProjectRepository = FirebaseProjectRepositoryImpl(),
    private val assetRepository: AssetRepository = FirebaseAssetRepositoryImpl()
) : ViewModel() {

    private val _projectState = MutableStateFlow<Project?>(null)
    val projectState = _projectState.asStateFlow()

    private val _selectedSceneId = MutableStateFlow<String?>(initialSceneId)
    val selectedSceneId = _selectedSceneId.asStateFlow()

    private val _prompt = MutableStateFlow("")
    val prompt = _prompt.asStateFlow()

    private val _negativePrompt = MutableStateFlow("")
    val negativePrompt = _negativePrompt.asStateFlow()

    private val _style = MutableStateFlow(AiImageStyle.ISLAMIC_ART)
    val style = _style.asStateFlow()

    private val _aspectRatio = MutableStateFlow(AiImageAspectRatio.RATIO_16_9)
    val aspectRatio = _aspectRatio.asStateFlow()

    private val _count = MutableStateFlow(1)
    val count = _count.asStateFlow()

    private val _status = MutableStateFlow(AiImageStatus.IDLE)
    val status = _status.asStateFlow()

    private val _generatedImages = MutableStateFlow<List<GeneratedImageItem>>(emptyList())
    val generatedImages = _generatedImages.asStateFlow()

    private val _userCredits = MutableStateFlow(50) // User balance in credits
    val userCredits = _userCredits.asStateFlow()

    private val _currentRequestId = MutableStateFlow<String?>(null)

    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage = _uiMessage.asSharedFlow()

    init {
        loadProject()
    }

    private fun loadProject() {
        viewModelScope.launch {
            val res = projectRepository.getProject(projectId)
            if (res is Resource.Success) {
                _projectState.value = res.data
                if (_selectedSceneId.value == null && res.data.scenes.isNotEmpty()) {
                    _selectedSceneId.value = res.data.scenes.first().id
                }
            }
        }
    }

    fun updatePrompt(text: String) {
        _prompt.value = text
    }

    fun updateNegativePrompt(text: String) {
        _negativePrompt.value = text
    }

    fun updateStyle(newStyle: AiImageStyle) {
        _style.value = newStyle
    }

    fun updateAspectRatio(newRatio: AiImageAspectRatio) {
        _aspectRatio.value = newRatio
    }

    fun updateCount(newCount: Int) {
        _count.value = newCount.coerceIn(1, 4)
    }

    fun selectScene(sceneId: String?) {
        _selectedSceneId.value = sceneId
    }

    fun generateImages(isRegenerate: Boolean = false) {
        val currentPrompt = _prompt.value.trim()
        
        // 0. Check Feature Flag
        if (!com.siraj.app.core.config.FeatureFlagManager.isFeatureEnabled(com.siraj.app.core.config.FeatureFlagManager.FEATURE_AI_GENERATION)) {
            viewModelScope.launch { _uiMessage.emit("عذراً، ميزة التوليد بالذكاء الاصطناعي معطلة حالياً للصيانة.") }
            return
        }

        // 1. Validate prompt
        val validation = IslamicPromptSafetyValidator.validatePrompt(currentPrompt)
        if (!validation.isAllowed) {
            viewModelScope.launch { _uiMessage.emit(validation.reason ?: "الوصف غير صالح.") }
            return
        }

        // 2. Validate user credit balance
        val totalCost = _count.value * 2
        if (_userCredits.value < totalCost) {
            viewModelScope.launch { _uiMessage.emit("رصيدك الحالي لا يكفي (${_userCredits.value} رصيد). التكلفة المطلوبة: $totalCost رصيد.") }
            return
        }

        val request = AiImageGenerationRequest(
            projectId = projectId,
            sceneId = _selectedSceneId.value,
            promptText = currentPrompt,
            negativePrompt = _negativePrompt.value.ifBlank { null },
            style = _style.value,
            aspectRatio = _aspectRatio.value,
            count = _count.value,
            seed = if (isRegenerate) System.currentTimeMillis() else null,
            costUnits = totalCost
        )

        _currentRequestId.value = request.requestId
        _status.value = AiImageStatus.QUEUED

        viewModelScope.launch {
            // Deduct tentative credits
            _userCredits.value -= totalCost

            _status.value = AiImageStatus.PROCESSING
            aiService.generateImage(request).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _status.value = AiImageStatus.PROCESSING
                    }
                    is Resource.Success -> {
                        _status.value = AiImageStatus.COMPLETED
                        if (isRegenerate) {
                            _generatedImages.value = result.data + _generatedImages.value
                        } else {
                            _generatedImages.value = result.data
                        }
                        _uiMessage.emit("تم توليد ${result.data.size} صورة بنجاح وخصم $totalCost رصيد.")
                    }
                    is Resource.Error -> {
                        _status.value = AiImageStatus.FAILED
                        // Refund credits on failure policy
                        _userCredits.value += totalCost
                        _uiMessage.emit("${result.message} (تمت استعادة $totalCost رصيد إلى حسابك)")
                    }
                }
            }
        }
    }

    fun cancelCurrentGeneration() {
        val reqId = _currentRequestId.value ?: return
        viewModelScope.launch {
            aiService.cancelGeneration(reqId)
            _status.value = AiImageStatus.CANCELLED
            // Refund credits
            val refunded = _count.value * 2
            _userCredits.value += refunded
            _uiMessage.emit("تم إلغاء عملية التوليد واستعادة الرصيد.")
        }
    }

    fun attachToScene(image: GeneratedImageItem, asBackground: Boolean) {
        val sceneId = _selectedSceneId.value
        if (sceneId == null) {
            viewModelScope.launch { _uiMessage.emit("يرجى اختيار مشهد لربط الصورة به.") }
            return
        }

        viewModelScope.launch {
            val res = aiService.attachImageToScene(projectId, sceneId, image, asBackground)
            if (res is Resource.Success) {
                _uiMessage.emit(if (asBackground) "تم تعيين الصورة كخلفية للمشهد بنجاح." else "تمت إضافة الصورة كأصل للمشهد.")
                loadProject()
            } else if (res is Resource.Error) {
                _uiMessage.emit(res.message)
            }
        }
    }

    fun deleteImage(image: GeneratedImageItem) {
        viewModelScope.launch {
            _generatedImages.value = _generatedImages.value.filter { it.id != image.id }
            aiService.deleteGeneratedImage(projectId, image.id)
            _uiMessage.emit("تم حذف الصورة.")
        }
    }

    fun saveToAssetLibrary(image: GeneratedImageItem) {
        viewModelScope.launch {
            val proj = _projectState.value
            val asset = Asset(
                ownerId = proj?.ownerId ?: "user",
                workspaceId = proj?.workspaceId ?: "workspace",
                projectId = projectId,
                type = AssetType.IMAGE,
                storagePath = "ai_images/${image.id}.jpg",
                downloadUrl = image.imageUrl,
                thumbnailUrl = image.thumbnailUrl,
                mimeType = "image/jpeg",
                sizeBytes = 2 * 1024 * 1024L,
                sourceUrl = "AI Model: ${image.model}",
                license = "AI Generated (Safe)",
                attribution = "مولد بالذكاء الاصطناعي بواسطة ${image.provider}",
                status = AssetStatus.READY
            )
            val res = assetRepository.addAsset(asset)
            if (res is Resource.Success) {
                _uiMessage.emit("تم حفظ الصورة في مكتبة الوسائط للمشروع.")
            } else if (res is Resource.Error) {
                _uiMessage.emit(res.message)
            }
        }
    }
}

class AiImageGeneratorViewModelFactory(
    private val projectId: String,
    private val sceneId: String? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AiImageGeneratorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AiImageGeneratorViewModel(projectId, sceneId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
