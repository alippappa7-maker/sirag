package com.siraj.app.features.project.presentation.audio

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.siraj.app.features.project.data.repositories.FirebaseAudioRepositoryImpl
import com.siraj.app.features.project.domain.models.*
import com.siraj.app.features.project.domain.repositories.AudioRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AudioStudioViewModel(
    private val projectId: String,
    private val sceneId: String? = null,
    private val initialText: String = "",
    private val audioRepository: AudioRepository = FirebaseAudioRepositoryImpl(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : ViewModel() {
    // Inputs
    var narrationText = MutableStateFlow(initialText)
        private set
    var selectedLanguage = MutableStateFlow(AudioLanguage.ARABIC_MODERN_STANDARD)
        private set
    var selectedVoiceId = MutableStateFlow("ar-male-faseeh-1")
        private set
    var speed = MutableStateFlow(1.0f)
        private set
    var pitch = MutableStateFlow(1.0f)
        private set
    var syncDurationWithScene = MutableStateFlow(true)
        private set

    // Available Voices
    val availableVoices: List<VoiceOption> = audioRepository.getAvailableVoices()

    // Generation and Upload State
    private val _generationState = MutableStateFlow<AudioGenerationUiState>(AudioGenerationUiState.Idle)
    val generationState: StateFlow<AudioGenerationUiState> = _generationState.asStateFlow()

    private val _uploadState = MutableStateFlow<AudioUploadUiState>(AudioUploadUiState.Idle)
    val uploadState: StateFlow<AudioUploadUiState> = _uploadState.asStateFlow()

    private val _userCredits = MutableStateFlow(25)
    val userCredits: StateFlow<Int> = _userCredits.asStateFlow()

    private val _projectAudios = MutableStateFlow<List<AudioItem>>(emptyList())
    val projectAudios: StateFlow<List<AudioItem>> = _projectAudios.asStateFlow()

    // Audio Playback Player
    private var mediaPlayer: MediaPlayer? = null
    private val _currentlyPlayingId = MutableStateFlow<String?>(null)
    val currentlyPlayingId: StateFlow<String?> = _currentlyPlayingId.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    // Trimming State
    private val _selectedAudioForTrim = MutableStateFlow<AudioItem?>(null)
    val selectedAudioForTrim: StateFlow<AudioItem?> = _selectedAudioForTrim.asStateFlow()

    private val _trimRange = MutableStateFlow(0f..100f) // percentages or milliseconds
    val trimRange: StateFlow<ClosedFloatingPointRange<Float>> = _trimRange.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        loadUserBalance()
        loadProjectAudios()
    }

    private fun loadUserBalance() {
        val uid = auth.currentUser?.uid ?: return
        firestore
            .collection("users")
            .document(uid)
            .addSnapshotListener { snap, _ ->
                if (snap != null && snap.exists()) {
                    val bal = snap.getLong("balance")?.toInt() ?: 25
                    _userCredits.value = bal
                }
            }
    }

    private fun loadProjectAudios() {
        viewModelScope.launch {
            audioRepository.getProjectAudios(projectId).collect { list ->
                _projectAudios.value = list
            }
        }
    }

    fun onTextChange(newText: String) {
        narrationText.value = newText
    }

    fun onLanguageChange(lang: AudioLanguage) {
        selectedLanguage.value = lang
    }

    fun onVoiceChange(voiceId: String) {
        selectedVoiceId.value = voiceId
    }

    fun onSpeedChange(newSpeed: Float) {
        speed.value = (newSpeed * 10).toInt() / 10f
    }

    fun onPitchChange(newPitch: Float) {
        pitch.value = (newPitch * 10).toInt() / 10f
    }

    fun onSyncDurationChange(sync: Boolean) {
        syncDurationWithScene.value = sync
    }

    fun generateVoiceover() {
        val text = narrationText.value.trim()
        if (text.isBlank()) {
            _userMessage.value = "يرجى كتابة نص للتعليق الصوتي أولاً"
            return
        }

        // Islamic Safety Check: Warning if attempting to make AI voice pretend to be Quran Recitation
        val textLower = text.lowercase()
        if (textLower.contains("سورة ") || textLower.contains("أعوذ بالله من الشيطان الرجيم")) {
            // Note: Allowed for narration, but strict disclaimer applies
        }

        if (_userCredits.value < 1) {
            _userMessage.value = "رصيدك غير كافٍ لتوليد الصوت (التكلفة: 1 رصيد)"
            return
        }

        viewModelScope.launch {
            _generationState.value = AudioGenerationUiState.Generating("جارٍ توليد الصوت العربي الفصيح عبر خادم سراج...")

            val request =
                GenerateAudioRequest(
                    projectId = projectId,
                    sceneId = sceneId,
                    text = text,
                    language = selectedLanguage.value.code,
                    voiceId = selectedVoiceId.value,
                    speed = speed.value,
                    pitch = pitch.value,
                    costUnits = 1,
                )

            val result = audioRepository.generateVoiceover(request)
            result
                .onSuccess { item ->
                    _generationState.value = AudioGenerationUiState.Success(item)
                    _userMessage.value = "تم توليد التعليق الصوتي بنجاح"
                    // Auto attach if sceneId is provided
                    if (!sceneId.isNullOrBlank()) {
                        audioRepository.attachAudioToScene(projectId, sceneId, item, syncDurationWithScene.value)
                    }
                }.onFailure { err ->
                    _generationState.value = AudioGenerationUiState.Error(err.message ?: "فشل في توليد الصوت")
                }
        }
    }

    fun uploadUserRecording(
        title: String,
        fileName: String,
        fileBytes: ByteArray,
        mimeType: String,
        durationMs: Long,
        speakerName: String?,
        isRecitation: Boolean,
    ) {
        viewModelScope.launch {
            _uploadState.value = AudioUploadUiState.Uploading(0.3f)

            val result =
                audioRepository.uploadUserAudio(
                    projectId = projectId,
                    sceneId = sceneId,
                    title = title,
                    fileName = fileName,
                    fileBytes = fileBytes,
                    mimeType = mimeType,
                    durationMs = durationMs,
                    reciterOrSpeakerName = speakerName,
                    isRecitation = isRecitation,
                )

            result
                .onSuccess { item ->
                    _uploadState.value = AudioUploadUiState.Success(item)
                    _userMessage.value = "تم رفع الملف الصوتي وربط بيانات الترخيص بنجاح"
                    if (!sceneId.isNullOrBlank()) {
                        audioRepository.attachAudioToScene(projectId, sceneId, item, syncDurationWithScene.value)
                    }
                }.onFailure { err ->
                    _uploadState.value = AudioUploadUiState.Error(err.message ?: "فشل في رفع الملف الصوتي")
                }
        }
    }

    fun playAudio(audioItem: AudioItem) {
        if (_currentlyPlayingId.value == audioItem.id && _isPlaying.value) {
            mediaPlayer?.pause()
            _isPlaying.value = false
            return
        }

        try {
            mediaPlayer?.release()
            mediaPlayer =
                MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes
                            .Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build(),
                    )
                    setDataSource(audioItem.audioUrl)
                    prepareAsync()
                    setOnPreparedListener { mp ->
                        if (audioItem.startTrimMs > 0) {
                            mp.seekTo(audioItem.startTrimMs.toInt())
                        }
                        mp.start()
                        _isPlaying.value = true
                        _currentlyPlayingId.value = audioItem.id
                    }
                    setOnCompletionListener {
                        _isPlaying.value = false
                        _currentlyPlayingId.value = null
                    }
                    setOnErrorListener { _, _, _ ->
                        _isPlaying.value = false
                        _currentlyPlayingId.value = null
                        true
                    }
                }
        } catch (e: Exception) {
            _userMessage.value = "تعذر تشغيل الصوت: ${e.message}"
        }
    }

    fun stopAudio() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        _isPlaying.value = false
        _currentlyPlayingId.value = null
    }

    fun openTrimDialog(audioItem: AudioItem) {
        _selectedAudioForTrim.value = audioItem
        _trimRange.value = audioItem.startTrimMs.toFloat()..audioItem.endTrimMs.toFloat()
    }

    fun closeTrimDialog() {
        _selectedAudioForTrim.value = null
    }

    fun onTrimRangeChanged(newRange: ClosedFloatingPointRange<Float>) {
        _trimRange.value = newRange
    }

    fun applyTrim() {
        val audio = _selectedAudioForTrim.value ?: return
        val range = _trimRange.value

        viewModelScope.launch {
            val result =
                audioRepository.trimAudio(
                    audioId = audio.id,
                    startTrimMs = range.start.toLong(),
                    endTrimMs = range.endInclusive.toLong(),
                )

            result
                .onSuccess {
                    _userMessage.value = "تم قص الملف الصوتي وتحديث مدة المشهد"
                    closeTrimDialog()
                }.onFailure { err ->
                    _userMessage.value = "فشل القص: ${err.message}"
                }
        }
    }

    fun attachToScene(audioItem: AudioItem) {
        if (sceneId.isNullOrBlank()) {
            _userMessage.value = "لا يوجد مشهد محدد لربطه"
            return
        }

        viewModelScope.launch {
            val result =
                audioRepository.attachAudioToScene(
                    projectId = projectId,
                    sceneId = sceneId,
                    audioItem = audioItem,
                    syncSceneDuration = syncDurationWithScene.value,
                )

            result
                .onSuccess {
                    _userMessage.value = "تم ربط الصوت بالمشهد وتحديث مدة العرض"
                }.onFailure { err ->
                    _userMessage.value = "فشل في ربط الصوت بالمشهد: ${err.message}"
                }
        }
    }

    fun deleteAudio(audioItem: AudioItem) {
        viewModelScope.launch {
            audioRepository.deleteAudio(audioItem.id)
            _userMessage.value = "تم حذف الملف الصوتي"
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

class AudioStudioViewModelFactory(
    private val projectId: String,
    private val sceneId: String? = null,
    private val initialText: String = "",
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AudioStudioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AudioStudioViewModel(projectId, sceneId, initialText) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
