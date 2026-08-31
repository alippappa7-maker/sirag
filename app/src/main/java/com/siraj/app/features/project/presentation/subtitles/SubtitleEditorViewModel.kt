package com.siraj.app.features.project.presentation.subtitles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.siraj.app.features.project.data.repositories.FirebaseSubtitleRepositoryImpl
import com.siraj.app.features.project.domain.models.*
import com.siraj.app.features.project.domain.repositories.SubtitleRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.siraj.app.core.error.GlobalErrorHandler

class SubtitleEditorViewModel(
    private val projectId: String,
    private val sceneId: String,
    private val initialSceneText: String = "",
    private val subtitleRepository: SubtitleRepository = FirebaseSubtitleRepositoryImpl(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _subtitles = MutableStateFlow<List<SubtitleItem>>(emptyList())
    val subtitles: StateFlow<List<SubtitleItem>> = _subtitles.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("ar") // "ar", "en"
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Export output
    private val _exportedContent = MutableStateFlow<String?>(null)
    val exportedContent: StateFlow<String?> = _exportedContent.asStateFlow()
    private val _exportFormat = MutableStateFlow<String?>(null)
    val exportFormat: StateFlow<String?> = _exportFormat.asStateFlow()

    // Current Scene Details
    private val _sceneNarration = MutableStateFlow(initialSceneText)
    val sceneNarration: StateFlow<String> = _sceneNarration.asStateFlow()
    private val _sceneDurationMs = MutableStateFlow(5000L)
    val sceneDurationMs: StateFlow<Long> = _sceneDurationMs.asStateFlow()

    // Current Active Subtitle Editing
    private val _editingSubtitle = MutableStateFlow<SubtitleItem?>(null)
    val editingSubtitle: StateFlow<SubtitleItem?> = _editingSubtitle.asStateFlow()

    // Style Customizer
    private val _currentStyle = MutableStateFlow(SubtitleStyle())
    val currentStyle: StateFlow<SubtitleStyle> = _currentStyle.asStateFlow()

    // Preview Simulation Playhead (ms)
    private val _previewCurrentTimeMs = MutableStateFlow(0L)
    val previewCurrentTimeMs: StateFlow<Long> = _previewCurrentTimeMs.asStateFlow()
    private val _isPreviewPlaying = MutableStateFlow(false)
    val isPreviewPlaying: StateFlow<Boolean> = _isPreviewPlaying.asStateFlow()

    init {
        loadSceneMetadata()
        loadSubtitles()
    }

    private fun loadSceneMetadata() {
        viewModelScope.launch {
            try {
                val doc = firestore.collection("projects").document(projectId)
                    .collection("scenes").document(sceneId).get().await()
                if (doc.exists()) {
                    val text = doc.getString("narrationText") ?: initialSceneText
                    val dur = doc.getLong("durationMs") ?: 5000L
                    _sceneNarration.value = text
                    _sceneDurationMs.value = dur
                }
            } catch (e: Exception) {
            GlobalErrorHandler.handle(e)}
        }
    }

    private fun loadSubtitles() {
        viewModelScope.launch {
            _isLoading.value = true
            subtitleRepository.getSubtitles(projectId, sceneId).collect { list ->
                _subtitles.value = list
                if (list.isNotEmpty()) {
                    _currentStyle.value = list.first().style
                }
                _isLoading.value = false
            }
        }
    }

    fun onLanguageChange(lang: String) {
        _selectedLanguage.value = lang
    }

    fun generateSubtitlesFromNarration() {
        viewModelScope.launch {
            _isLoading.value = true
            val text = _sceneNarration.value.ifBlank { initialSceneText }
            if (text.isBlank()) {
                _userMessage.value = "لا يوجد نص للمشهد لإنشاء الترجمة منه"
                _isLoading.value = false
                return@launch
            }

            val result = subtitleRepository.generateSubtitlesFromScene(
                projectId = projectId,
                sceneId = sceneId,
                sceneText = text,
                sceneDurationMs = _sceneDurationMs.value
            )
            result.onSuccess {
                _userMessage.value = "تم إنشاء الترجمة ومزامنة التوقيت بنجاح"
            }.onFailure {
                _userMessage.value = "فشل في إنشاء الترجمة: ${it.message}"
            }
            _isLoading.value = false
        }
    }

    fun autoTranslateToArabicOrEnglish() {
        viewModelScope.launch {
            _isLoading.value = true
            val arabicSubs = _subtitles.value.filter { it.language == "ar" }
            if (arabicSubs.isEmpty()) {
                _userMessage.value = "يجب إنشاء ترجمة عربية أولاً قبل توليد الترجمة الإنجليزية"
                _isLoading.value = false
                return@launch
            }

            val result = subtitleRepository.autoTranslateToEnglish(projectId, sceneId, arabicSubs)
            result.onSuccess {
                _selectedLanguage.value = "en"
                _userMessage.value = "تم إنشاء مسودة الترجمة الإنجليزية للمراجعة"
            }.onFailure {
                _userMessage.value = "فشل في الترجمة: ${it.message}"
            }
            _isLoading.value = false
        }
    }

    fun onSelectSubtitleForEdit(sub: SubtitleItem) {
        _editingSubtitle.value = sub
    }

    fun onDismissEdit() {
        _editingSubtitle.value = null
    }

    fun onUpdateSubtitle(
        newText: String,
        newStartMs: Long,
        newEndMs: Long
    ) {
        val current = _editingSubtitle.value ?: return
        
        // Strict Islamic Rule Check: Prevent editing locked Quranic texts casually
        if (current.locked) {
            _userMessage.value = "تنبيه: هذا النص القرآني/الحديث مقفل شرعياً وموثق بالمصدر، لا يمكن تعديل صياغته."
            return
        }

        // Validate timing constraints: start < end, and end <= scene duration
        val maxDuration = _sceneDurationMs.value
        val safeStart = newStartMs.coerceAtLeast(0L)
        val safeEnd = newEndMs.coerceAtMost(maxDuration).coerceAtLeast(safeStart + 500L)

        val updated = current.copy(
            text = newText,
            startMs = safeStart,
            endMs = safeEnd,
            style = _currentStyle.value
        )

        viewModelScope.launch {
            subtitleRepository.saveSubtitle(updated)
            _editingSubtitle.value = null
            _userMessage.value = "تم تحديث سطر الترجمة"
        }
    }

    fun onDeleteSubtitle(sub: SubtitleItem) {
        if (sub.locked) {
            _userMessage.value = "النصوص الشرعية المقفلة تتطلب فك القفل أولاً"
            return
        }
        viewModelScope.launch {
            subtitleRepository.deleteSubtitle(projectId, sceneId, sub.id)
            _userMessage.value = "تم حذف السطر"
        }
    }

    fun onAddNewSubtitle() {
        val nextStart = (_subtitles.value.maxOfOrNull { it.endMs } ?: 0L).coerceAtMost(_sceneDurationMs.value)
        val nextEnd = (nextStart + 2000L).coerceAtMost(_sceneDurationMs.value)

        val newSub = SubtitleItem(
            id = "sub_${System.currentTimeMillis()}",
            projectId = projectId,
            sceneId = sceneId,
            language = _selectedLanguage.value,
            text = "سطر ترجمة جديد",
            startMs = nextStart,
            endMs = nextEnd,
            style = _currentStyle.value,
            sourceType = SubtitleSourceType.MANUAL_USER,
            locked = false,
            reviewStatus = SubtitleReviewStatus.NOT_REQUIRED
        )

        viewModelScope.launch {
            subtitleRepository.saveSubtitle(newSub)
            _editingSubtitle.value = newSub
        }
    }

    fun updateStyle(
        fontFamily: SubtitleFontFamily? = null,
        fontSizeSp: Int? = null,
        textColorHex: String? = null,
        backgroundColorHex: String? = null,
        position: SubtitlePosition? = null,
        burnIntoVideo: Boolean? = null
    ) {
        val s = _currentStyle.value
        val newStyle = s.copy(
            fontFamily = fontFamily ?: s.fontFamily,
            fontSizeSp = fontSizeSp ?: s.fontSizeSp,
            textColorHex = textColorHex ?: s.textColorHex,
            backgroundColorHex = backgroundColorHex ?: s.backgroundColorHex,
            position = position ?: s.position,
            burnIntoVideo = burnIntoVideo ?: s.burnIntoVideo
        )
        _currentStyle.value = newStyle

        viewModelScope.launch {
            subtitleRepository.updateSubtitleStyleForScene(projectId, sceneId, newStyle)
        }
    }

    fun exportSrt() {
        val list = _subtitles.value.filter { it.language == _selectedLanguage.value }
        val srt = subtitleRepository.exportToSrt(list)
        _exportedContent.value = srt
        _exportFormat.value = "SRT"
    }

    fun exportVtt() {
        val list = _subtitles.value.filter { it.language == _selectedLanguage.value }
        val vtt = subtitleRepository.exportToVtt(list)
        _exportedContent.value = vtt
        _exportFormat.value = "VTT"
    }

    fun closeExportDialog() {
        _exportedContent.value = null
        _exportFormat.value = null
    }

    fun setPreviewTime(timeMs: Long) {
        _previewCurrentTimeMs.value = timeMs.coerceIn(0L, _sceneDurationMs.value)
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}

class SubtitleEditorViewModelFactory(
    private val projectId: String,
    private val sceneId: String,
    private val initialSceneText: String = ""
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubtitleEditorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SubtitleEditorViewModel(projectId, sceneId, initialSceneText) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
