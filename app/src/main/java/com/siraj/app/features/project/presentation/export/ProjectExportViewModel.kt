package com.siraj.app.features.project.presentation.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.siraj.app.domain.models.Project
import com.siraj.app.domain.models.ReviewState
import com.siraj.app.domain.models.Scene
import com.siraj.app.features.project.data.repositories.FirebaseProductionJobRepositoryImpl
import com.siraj.app.features.project.data.services.FirebaseVideoCompositionServiceImpl
import com.siraj.app.features.project.domain.models.*
import com.siraj.app.features.project.domain.repositories.ProductionJobRepository
import com.siraj.app.features.project.domain.services.VideoCompositionService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ProjectExportViewModel(
    val projectId: String,
    private val jobRepository: ProductionJobRepository = FirebaseProductionJobRepositoryImpl(),
    private val compositionService: VideoCompositionService = FirebaseVideoCompositionServiceImpl(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _project = MutableStateFlow<Project?>(null)
    val project: StateFlow<Project?> = _project.asStateFlow()

    private val _scenes = MutableStateFlow<List<Scene>>(emptyList())
    val scenes: StateFlow<List<Scene>> = _scenes.asStateFlow()

    private val _subtitles = MutableStateFlow<List<SubtitleItem>>(emptyList())
    val subtitles: StateFlow<List<SubtitleItem>> = _subtitles.asStateFlow()

    private val _validationReport = MutableStateFlow(PreExportReport())
    val validationReport: StateFlow<PreExportReport> = _validationReport.asStateFlow()

    private val _selectedQuality = MutableStateFlow(ProductionQuality.FHD_1080P)
    val selectedQuality: StateFlow<ProductionQuality> = _selectedQuality.asStateFlow()

    private val _selectedAspectRatio = MutableStateFlow("9:16")
    val selectedAspectRatio: StateFlow<String> = _selectedAspectRatio.asStateFlow()

    private val _selectedFps = MutableStateFlow(30)
    val selectedFps: StateFlow<Int> = _selectedFps.asStateFlow()

    private val _burnSubtitles = MutableStateFlow(true)
    val burnSubtitles: StateFlow<Boolean> = _burnSubtitles.asStateFlow()

    private val _includeSourceCitation = MutableStateFlow(true)
    val includeSourceCitation: StateFlow<Boolean> = _includeSourceCitation.asStateFlow()

    private val _includeWatermark = MutableStateFlow(true)
    val includeWatermark: StateFlow<Boolean> = _includeWatermark.asStateFlow()

    private val _isPreviewMode = MutableStateFlow(false)
    val isPreviewMode: StateFlow<Boolean> = _isPreviewMode.asStateFlow()

    private val _availableCredits = MutableStateFlow(250L)
    val availableCredits: StateFlow<Long> = _availableCredits.asStateFlow()

    private val _storageUsedMb = MutableStateFlow(185.0)
    val storageUsedMb: StateFlow<Double> = _storageUsedMb.asStateFlow()

    private val _storageLimitMb = MutableStateFlow(2000.0)
    val storageLimitMb: StateFlow<Double> = _storageLimitMb.asStateFlow()

    private val _showWarningDialog = MutableStateFlow(false)
    val showWarningDialog: StateFlow<Boolean> = _showWarningDialog.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val _projectJobs = MutableStateFlow<List<ProductionJob>>(emptyList())
    val projectJobs: StateFlow<List<ProductionJob>> = _projectJobs.asStateFlow()

    private val _activeJob = MutableStateFlow<ProductionJob?>(null)
    val activeJob: StateFlow<ProductionJob?> = _activeJob.asStateFlow()

    val calculatedCost: StateFlow<Long> = combine(
        _scenes,
        _selectedQuality,
        _selectedFps,
        _isPreviewMode
    ) { sceneList, quality, fps, isPreview ->
        val totalSec = (sceneList.sumOf { it.durationMs } / 1000).coerceAtLeast(10)
        val baseUnits = (totalSec / 5).coerceAtLeast(5)
        val fpsMultiplier = when (fps) {
            60 -> 1.4
            24 -> 0.9
            else -> 1.0
        }
        val previewMultiplier = if (isPreview) 0.4 else 1.0
        (baseUnits * quality.costMultiplier * fpsMultiplier * previewMultiplier).toLong().coerceAtLeast(2L)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 15L)

    init {
        loadProjectData()
        observeJobs()
    }

    private fun loadProjectData() {
        viewModelScope.launch {
            try {
                // Fetch Project
                val projectDoc = firestore.collection("projects").document(projectId).get().await()
                if (projectDoc.exists()) {
                    val p = projectDoc.toObject(Project::class.java)
                    _project.value = p
                    if (p != null && p.aspectRatio.isNotBlank()) {
                        _selectedAspectRatio.value = p.aspectRatio
                    }
                }

                // Fetch Scenes
                val scenesSnap = firestore.collection("projects").document(projectId)
                    .collection("scenes").orderBy("orderIndex").get().await()
                val sceneList = scenesSnap.documents.mapNotNull { it.toObject(Scene::class.java) }
                _scenes.value = sceneList

                // Fetch Subtitles
                val subsSnap = firestore.collection("projects").document(projectId)
                    .collection("subtitles").orderBy("startMs").get().await()
                val subList = subsSnap.documents.mapNotNull { it.toObject(SubtitleItem::class.java) }
                _subtitles.value = subList

                // Run Validation
                runPreExportValidation(sceneList, subList)
            } catch (_: Exception) {}
        }
    }

    private fun observeJobs() {
        viewModelScope.launch {
            jobRepository.getJobsForProject(projectId).collect { jobs ->
                _projectJobs.value = jobs
                _activeJob.value = jobs.firstOrNull { !it.isTerminal } ?: jobs.firstOrNull()
            }
        }
    }

    private fun runPreExportValidation(scenes: List<Scene>, subtitles: List<SubtitleItem>) {
        val issues = mutableListOf<PreExportValidationIssue>()

        if (scenes.isEmpty()) {
            issues.add(
                PreExportValidationIssue(
                    id = "empty_scenes",
                    issueType = ValidationIssueType.INVALID_DURATION,
                    severity = ValidationSeverity.BLOCKER,
                    message = "المشروع خالي من المشاهد. يجب إضافة مشهد واحد على الأقل قبل التصدير.",
                    fixRecommendation = "انتقل لمحرر المشاهد وأضف نصاً أو صورة."
                )
            )
        }

        val emptyTextScenes = scenes.filter { it.narrationText.isBlank() }
        if (emptyTextScenes.isNotEmpty()) {
            issues.add(
                PreExportValidationIssue(
                    id = "empty_text_scenes",
                    issueType = ValidationIssueType.SCENE_WITHOUT_MEDIA,
                    severity = ValidationSeverity.WARNING,
                    message = "توجد ${emptyTextScenes.size} مشاهد بدون نص مسموع أو معروض.",
                    fixRecommendation = "أضف نصاً شرعياً أو توضيحياً للمشهد."
                )
            )
        }

        if (subtitles.isEmpty()) {
            issues.add(
                PreExportValidationIssue(
                    id = "no_subtitles",
                    issueType = ValidationIssueType.TEXT_OVERFLOW,
                    severity = ValidationSeverity.WARNING,
                    message = "لا توجد ترجمة نصية معروضة. سيتم التصدير بدون شريط ترجمة أسفل الشاشة.",
                    fixRecommendation = "يمكنك إنشاء ترجمة تلقائية من تبويب الترجمة."
                )
            )
        }

        _validationReport.value = PreExportReport(issues = issues)
    }

    fun setQuality(quality: ProductionQuality) {
        _selectedQuality.value = quality
    }

    fun setAspectRatio(ratio: String) {
        _selectedAspectRatio.value = ratio
    }

    fun setFps(fps: Int) {
        _selectedFps.value = fps
    }

    fun setBurnSubtitles(enabled: Boolean) {
        _burnSubtitles.value = enabled
    }

    fun setIncludeSourceCitation(enabled: Boolean) {
        _includeSourceCitation.value = enabled
    }

    fun setIncludeWatermark(enabled: Boolean) {
        _includeWatermark.value = enabled
    }

    fun setIsPreviewMode(isPreview: Boolean) {
        _isPreviewMode.value = isPreview
    }

    fun toggleWarningDialog(show: Boolean) {
        _showWarningDialog.value = show
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    fun requestExport() {
        val report = _validationReport.value
        if (!report.isExportAllowed) {
            _userMessage.value = "لا يمكن التصدير لوجود موانع حرجة في المشروع."
            return
        }

        if (report.hasWarnings) {
            _showWarningDialog.value = true
        } else {
            executeExport()
        }
    }

    fun executeExport() {
        _showWarningDialog.value = false
        val cost = calculatedCost.value
        if (_availableCredits.value < cost) {
            _userMessage.value = "رصيدك الحالي غير كافٍ. المطلوب: $cost نقطة، المتاح: ${_availableCredits.value} نقطة."
            return
        }

        viewModelScope.launch {
            val idempotencyKey = UUID.randomUUID().toString()
            val isPreview = _isPreviewMode.value

            val jobRes = jobRepository.createJob(
                projectId = projectId,
                workspaceId = _project.value?.workspaceId ?: "default_workspace",
                quality = _selectedQuality.value,
                burnSubtitles = _burnSubtitles.value,
                aspectRatio = _selectedAspectRatio.value,
                idempotencyKey = idempotencyKey,
                fps = _selectedFps.value,
                includeSourceCitation = _includeSourceCitation.value,
                includeWatermark = _includeWatermark.value,
                isPreviewOnly = isPreview
            )

            jobRes.onSuccess { createdJob ->
                _availableCredits.value -= cost
                _userMessage.value = "تم بدء عملية التصدير بنجاح! جاري التجميع والتصيير..."

                val manifestRes = compositionService.buildManifest(
                    projectId = projectId,
                    quality = _selectedQuality.value,
                    aspectRatio = _selectedAspectRatio.value,
                    burnSubtitles = _burnSubtitles.value,
                    fps = _selectedFps.value,
                    includeSourceCitation = _includeSourceCitation.value,
                    includeWatermark = _includeWatermark.value,
                    isPreview = isPreview
                )

                manifestRes.onSuccess { manifest ->
                    viewModelScope.launch {
                        compositionService.executeComposition(createdJob, manifest).collect { updatedJob ->
                            _activeJob.value = updatedJob
                        }
                    }
                }.onFailure { err ->
                    _userMessage.value = "فشل بناء مخطط التركيب: ${err.message}"
                }
            }.onFailure { err ->
                _userMessage.value = "تعذر إنشاء مهمة التصدير: ${err.message}"
            }
        }
    }

    fun cancelJob(jobId: String) {
        viewModelScope.launch {
            val res = jobRepository.cancelJob(jobId)
            res.onSuccess {
                _userMessage.value = "تم إلغاء عملية التصدير واسترداد الرصيد المحجوز."
            }.onFailure { err ->
                _userMessage.value = err.message ?: "فشل إلغاء المهمة"
            }
        }
    }

    fun retryJob(jobId: String) {
        viewModelScope.launch {
            val res = jobRepository.retryJob(jobId)
            res.onSuccess { retriedJob ->
                _userMessage.value = "تمت إعادة محاولة التصدير."
                _activeJob.value = retriedJob
            }.onFailure { err ->
                _userMessage.value = err.message ?: "فشل إعادة المحاولة"
            }
        }
    }

    fun deleteExportedFile(jobId: String) {
        viewModelScope.launch {
            val res = jobRepository.deleteExportedFile(jobId)
            res.onSuccess {
                _userMessage.value = "تم حذف الملف الناتج وإخلاء مساحة التخزين."
                _storageUsedMb.value = (_storageUsedMb.value - 15.0).coerceAtLeast(0.0)
            }.onFailure { err ->
                _userMessage.value = err.message ?: "فشل حذف الملف"
            }
        }
    }
}

class ProjectExportViewModelFactory(
    private val projectId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProjectExportViewModel(projectId) as T
    }
}
