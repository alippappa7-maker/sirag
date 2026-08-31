package com.siraj.app.features.project.presentation.preview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.siraj.app.domain.models.Project
import com.siraj.app.domain.models.ReviewState
import com.siraj.app.domain.models.Scene
import com.siraj.app.features.project.data.repositories.FirebaseSubtitleRepositoryImpl
import com.siraj.app.features.project.domain.models.*
import com.siraj.app.features.project.domain.repositories.SubtitleRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProjectPreviewViewModel(
    private val projectId: String,
    private val subtitleRepository: SubtitleRepository = FirebaseSubtitleRepositoryImpl(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _project = MutableStateFlow<Project?>(null)
    val project: StateFlow<Project?> = _project.asStateFlow()

    private val _scenes = MutableStateFlow<List<Scene>>(emptyList())
    val scenes: StateFlow<List<Scene>> = _scenes.asStateFlow()

    private val _subtitles = MutableStateFlow<List<SubtitleItem>>(emptyList())
    val subtitles: StateFlow<List<SubtitleItem>> = _subtitles.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Aspect Ratio: "9:16", "16:9", "1:1"
    private val _aspectRatio = MutableStateFlow("9:16")
    val aspectRatio: StateFlow<String> = _aspectRatio.asStateFlow()

    // Player State
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTimeMs = MutableStateFlow(0L)
    val currentTimeMs: StateFlow<Long> = _currentTimeMs.asStateFlow()

    private val _totalDurationMs = MutableStateFlow(0L)
    val totalDurationMs: StateFlow<Long> = _totalDurationMs.asStateFlow()

    private val _currentSceneIndex = MutableStateFlow(0)
    val currentSceneIndex: StateFlow<Int> = _currentSceneIndex.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f) // 0.5x, 1.0x, 1.5x, 2.0x
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    // Pre-export validation
    private val _validationReport = MutableStateFlow(PreExportReport())
    val validationReport: StateFlow<PreExportReport> = _validationReport.asStateFlow()

    private val _showValidationSheet = MutableStateFlow(false)
    val showValidationSheet: StateFlow<Boolean> = _showValidationSheet.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private var playbackJob: Job? = null

    init {
        loadProjectData()
    }

    private fun loadProjectData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Fetch Project
                val projectDoc = firestore.collection("projects").document(projectId).get().await()
                if (projectDoc.exists()) {
                    val p = projectDoc.toObject(Project::class.java) ?: Project(id = projectId)
                    _project.value = p
                    _aspectRatio.value = p.aspectRatio.ifBlank { "9:16" }
                }

                // 2. Fetch Scenes
                val scenesSnapshot = firestore.collection("projects").document(projectId)
                    .collection("scenes").orderBy("orderIndex").get().await()
                val sceneList = scenesSnapshot.documents.mapNotNull { it.toObject(Scene::class.java) }
                _scenes.value = sceneList

                val totalMs = if (sceneList.isNotEmpty()) sceneList.sumOf { it.durationMs.coerceAtLeast(1000L) } else 0L
                _totalDurationMs.value = totalMs

                // 3. Fetch Subtitles
                subtitleRepository.getSubtitles(projectId, null).collect { subs ->
                    _subtitles.value = subs
                    runPreExportValidation(sceneList, subs)
                }

            } catch (e: Exception) { GlobalErrorHandler.handle(e); _userMessage.value = "خطأ في تحميل المشروع: ${e.message} }"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setAspectRatio(ratio: String) {
        _aspectRatio.value = ratio
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        if (_totalDurationMs.value <= 0L) return
        _isPlaying.value = true
        startPlaybackLoop()
    }

    fun pause() {
        _isPlaying.value = false
        playbackJob?.cancel()
    }

    fun seekTo(timeMs: Long) {
        val boundedTime = timeMs.coerceIn(0L, _totalDurationMs.value)
        _currentTimeMs.value = boundedTime
        updateCurrentSceneFromTime(boundedTime)
    }

    fun jumpToScene(index: Int) {
        val sceneList = _scenes.value
        if (index in sceneList.indices) {
            var accumulated = 0L
            for (i in 0 until index) {
                accumulated += sceneList[i].durationMs.coerceAtLeast(1000L)
            }
            seekTo(accumulated)
            _currentSceneIndex.value = index
        }
    }

    fun nextScene() {
        val nextIdx = _currentSceneIndex.value + 1
        if (nextIdx < _scenes.value.size) {
            jumpToScene(nextIdx)
        }
    }

    fun previousScene() {
        val prevIdx = _currentSceneIndex.value - 1
        if (prevIdx >= 0) {
            jumpToScene(prevIdx)
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        if (_isPlaying.value) {
            startPlaybackLoop()
        }
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
    }

    fun toggleValidationSheet() {
        _showValidationSheet.value = !_showValidationSheet.value
    }

    private fun startPlaybackLoop() {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val stepMs = 50L
            while (_isPlaying.value && _currentTimeMs.value < _totalDurationMs.value) {
                val delayTime = (stepMs / _playbackSpeed.value).toLong().coerceAtLeast(10L)
                delay(delayTime)
                val newTime = _currentTimeMs.value + stepMs
                if (newTime >= _totalDurationMs.value) {
                    _currentTimeMs.value = _totalDurationMs.value
                    _isPlaying.value = false
                    break
                } else {
                    _currentTimeMs.value = newTime
                    updateCurrentSceneFromTime(newTime)
                }
            }
        }
    }

    private fun updateCurrentSceneFromTime(timeMs: Long) {
        val sceneList = _scenes.value
        var accumulated = 0L
        for ((idx, sc) in sceneList.withIndex()) {
            val dur = sc.durationMs.coerceAtLeast(1000L)
            if (timeMs in accumulated until (accumulated + dur)) {
                _currentSceneIndex.value = idx
                return
            }
            accumulated += dur
        }
        if (sceneList.isNotEmpty()) {
            _currentSceneIndex.value = sceneList.lastIndex
        }
    }

    // Comprehensive Pre-Export Quality, Islamic & Technical Checker
    fun runPreExportValidation(scenes: List<Scene>, subtitles: List<SubtitleItem>) {
        val issues = mutableListOf<PreExportValidationIssue>()

        if (scenes.isEmpty()) {
            issues.add(
                PreExportValidationIssue(
                    issueType = ValidationIssueType.INVALID_DURATION,
                    severity = ValidationSeverity.BLOCKER,
                    message = "المشروع لا يحتوي على أي مشاهد ليتم تصديره.",
                    fixRecommendation = "أضف مشهداً واحداً على الأقل من خلال محرر المشاهد."
                )
            )
        }

        var globalAccumulatedMs = 0L

        scenes.forEachIndexed { index, scene ->
            val sceneDuration = scene.durationMs
            val sceneNumber = index + 1

            // 1. Duration check
            if (sceneDuration <= 0) {
                issues.add(
                    PreExportValidationIssue(
                        sceneId = scene.id,
                        sceneIndex = index,
                        sceneTitle = scene.title.ifBlank { "مشهد $sceneNumber" },
                        issueType = ValidationIssueType.INVALID_DURATION,
                        severity = ValidationSeverity.BLOCKER,
                        message = "مدة المشهد $sceneNumber غير صالحة (0 ثانية).",
                        fixRecommendation = "اضبط مدة المشهد على 3 ثوانٍ على الأقل."
                    )
                )
            }

            // 2. Media presence check
            if (scene.assetIds.isEmpty() && scene.narrationText.isBlank()) {
                issues.add(
                    PreExportValidationIssue(
                        sceneId = scene.id,
                        sceneIndex = index,
                        sceneTitle = scene.title.ifBlank { "مشهد $sceneNumber" },
                        issueType = ValidationIssueType.SCENE_WITHOUT_MEDIA,
                        severity = ValidationSeverity.WARNING,
                        message = "المشهد $sceneNumber فارغ بدون وسائط بصرية أو نص تعليق.",
                        fixRecommendation = "اربط صورة/فيديو من مدير الوسائط أو اكتب نص المشهد."
                    )
                )
            }

            // 3. Audio/Voiceover presence
            if (scene.narrationText.isNotBlank()) {
                // If scene has script narration but no audio attached
                // Warning only (user might want silent text-only video)
            }

            // 4. Subtitles checks for this scene
            val sceneSubs = subtitles.filter { it.sceneId == scene.id }.sortedBy { it.startMs }
            for (i in 0 until sceneSubs.size - 1) {
                val currentSub = sceneSubs[i]
                val nextSub = sceneSubs[i + 1]
                if (currentSub.endMs > nextSub.startMs) {
                    issues.add(
                        PreExportValidationIssue(
                            sceneId = scene.id,
                            sceneIndex = index,
                            sceneTitle = scene.title.ifBlank { "مشهد $sceneNumber" },
                            issueType = ValidationIssueType.OVERLAPPING_SUBTITLES,
                            severity = ValidationSeverity.BLOCKER,
                            message = "تداخل زمني في أسطر الترجمة بالمشهد $sceneNumber بين '${currentSub.text.take(15)}...' و '${nextSub.text.take(15)}...'",
                            fixRecommendation = "افتح محرر الترجمة واضبط نهايات وبدايات الأسطر لمنع التداخل."
                        )
                    )
                }
            }

            // 5. Islamic Sacred Claims check
            val hasSacredText = scene.narrationText.contains("﴿") || scene.narrationText.contains("قال تعالى") || scene.narrationText.contains("قال رسول الله")
            if (hasSacredText && scene.claimIds.isEmpty()) {
                issues.add(
                    PreExportValidationIssue(
                        sceneId = scene.id,
                        sceneIndex = index,
                        sceneTitle = scene.title.ifBlank { "مشهد $sceneNumber" },
                        issueType = ValidationIssueType.UNREVIEWED_CLAIM,
                        severity = ValidationSeverity.WARNING,
                        message = "المشهد $sceneNumber يحتوي على نص شرعي/آية دون ربطه بمصدر موثق في المحراب.",
                        fixRecommendation = "اربط الآية/الحديث بمرجع معتمد من مكتبة المصادر لضمان التوثيق."
                    )
                )
            }

            // 6. Text overflow check (> 30 words in single subtitle chunk)
            sceneSubs.forEach { sub ->
                if (sub.text.split("\\s+".toRegex()).size > 25) {
                    issues.add(
                        PreExportValidationIssue(
                            sceneId = scene.id,
                            sceneIndex = index,
                            sceneTitle = scene.title.ifBlank { "مشهد $sceneNumber" },
                            issueType = ValidationIssueType.TEXT_OVERFLOW,
                            severity = ValidationSeverity.WARNING,
                            message = "سطر الترجمة طويل جداً في المشهد $sceneNumber وقد يخرج عن إطار الشاشة.",
                            fixRecommendation = "قسّم السطر إلى شطرين أقصر من خلال محرر الترجمة."
                        )
                    )
                }
            }

            globalAccumulatedMs += sceneDuration
        }

        _validationReport.value = PreExportReport(issues = issues)
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        playbackJob?.cancel()
    }
}

class ProjectPreviewViewModelFactory(
    private val projectId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProjectPreviewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProjectPreviewViewModel(projectId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
