package com.siraj.app.features.project.presentation.soundtrack

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.siraj.app.features.project.data.repositories.FirebaseSoundtrackRepositoryImpl
import com.siraj.app.features.project.domain.models.SceneAudioTrackConfig
import com.siraj.app.features.project.domain.models.SoundtrackCategory
import com.siraj.app.features.project.domain.models.SoundtrackItem
import com.siraj.app.features.project.domain.repositories.SoundtrackRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SoundtrackLibraryViewModel(
    private val projectId: String,
    private val sceneId: String? = null,
    private val soundtrackRepository: SoundtrackRepository = FirebaseSoundtrackRepositoryImpl(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    // Filters
    var searchQuery = MutableStateFlow("")
        private set
    var selectedCategory = MutableStateFlow<SoundtrackCategory?>(null)
        private set
    var hideMusicOnly = MutableStateFlow(false)
        private set

    // Soundtracks list
    private val _soundtracks = MutableStateFlow<List<SoundtrackItem>>(emptyList())
    val soundtracks: StateFlow<List<SoundtrackItem>> = _soundtracks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Playback state
    private var mediaPlayer: MediaPlayer? = null
    private val _currentlyPlayingId = MutableStateFlow<String?>(null)
    val currentlyPlayingId: StateFlow<String?> = _currentlyPlayingId.asStateFlow()
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // Scene Attachment Customization Dialog State
    private val _selectedTrackForConfig = MutableStateFlow<SoundtrackItem?>(null)
    val selectedTrackForConfig: StateFlow<SoundtrackItem?> = _selectedTrackForConfig.asStateFlow()

    // Config options
    var trackVolume = MutableStateFlow(0.4f)
        private set
    var isLooping = MutableStateFlow(true)
        private set
    var isFadeIn = MutableStateFlow(true)
        private set
    var isFadeOut = MutableStateFlow(true)
        private set
    var trimRange = MutableStateFlow(0f..30000f)
        private set

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Existing track in scene if any
    private val _currentSceneTrackConfig = MutableStateFlow<SceneAudioTrackConfig?>(null)
    val currentSceneTrackConfig: StateFlow<SceneAudioTrackConfig?> = _currentSceneTrackConfig.asStateFlow()

    init {
        loadSoundtracks()
        loadCurrentSceneAudio()
    }

    private fun loadSoundtracks() {
        viewModelScope.launch {
            _isLoading.value = true
            combine(
                searchQuery,
                selectedCategory,
                hideMusicOnly
            ) { query, cat, hideMusic ->
                Triple(query, cat, hideMusic)
            }.flatMapLatest { (query, cat, hideMusic) ->
                soundtrackRepository.getSoundtracks(cat, query, hideMusic)
            }.collect { list ->
                _soundtracks.value = list
                _isLoading.value = false
            }
        }
    }

    private fun loadCurrentSceneAudio() {
        if (sceneId.isNullOrBlank()) return
        firestore.collection("projects").document(projectId)
            .collection("scenes").document(sceneId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val map = snapshot.get("soundtrackTrack") as? Map<*, *>
                    if (map != null) {
                        try {
                            val cfg = SceneAudioTrackConfig(
                                audioId = map["audioId"] as? String ?: "",
                                soundTitle = map["soundTitle"] as? String ?: "",
                                soundUrl = map["soundUrl"] as? String ?: "",
                                volume = (map["volume"] as? Number)?.toFloat() ?: 0.5f,
                                loop = map["loop"] as? Boolean ?: true,
                                fadeIn = map["fadeIn"] as? Boolean ?: true,
                                fadeOut = map["fadeOut"] as? Boolean ?: true,
                                startTrimMs = (map["startTrimMs"] as? Number)?.toLong() ?: 0L,
                                endTrimMs = (map["endTrimMs"] as? Number)?.toLong() ?: 0L,
                                effectiveDurationMs = (map["effectiveDurationMs"] as? Number)?.toLong() ?: 0L,
                                attributionRequired = map["attributionRequired"] as? Boolean ?: false,
                                attributionText = map["attributionText"] as? String ?: "",
                                licenseDisplayName = map["licenseDisplayName"] as? String ?: ""
                            )
                            _currentSceneTrackConfig.value = cfg
                        } catch (_: Exception) {
                            _currentSceneTrackConfig.value = null
                        }
                    } else {
                        _currentSceneTrackConfig.value = null
                    }
                }
            }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun onCategorySelect(category: SoundtrackCategory?) {
        selectedCategory.value = if (selectedCategory.value == category) null else category
    }

    fun onToggleHideMusic(hide: Boolean) {
        hideMusicOnly.value = hide
    }

    fun togglePlaySoundtrack(item: SoundtrackItem) {
        if (_currentlyPlayingId.value == item.id && _isPlaying.value) {
            mediaPlayer?.pause()
            _isPlaying.value = false
            return
        }

        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(item.audioUrl)
                prepareAsync()
                setOnPreparedListener { mp ->
                    mp.start()
                    _isPlaying.value = true
                    _currentlyPlayingId.value = item.id
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

    fun openConfigureDialog(item: SoundtrackItem) {
        _selectedTrackForConfig.value = item
        trackVolume.value = item.defaultVolume
        isLooping.value = true
        isFadeIn.value = true
        isFadeOut.value = true
        trimRange.value = 0f..item.durationMs.toFloat()
    }

    fun closeConfigureDialog() {
        _selectedTrackForConfig.value = null
    }

    fun onVolumeChange(vol: Float) {
        trackVolume.value = (vol * 100).toInt() / 100f
    }

    fun onLoopToggle(loop: Boolean) {
        isLooping.value = loop
    }

    fun onFadeInToggle(fade: Boolean) {
        isFadeIn.value = fade
    }

    fun onFadeOutToggle(fade: Boolean) {
        isFadeOut.value = fade
    }

    fun onTrimRangeChange(range: ClosedFloatingPointRange<Float>) {
        trimRange.value = range
    }

    fun applyTrackToScene() {
        val item = _selectedTrackForConfig.value ?: return
        if (sceneId.isNullOrBlank()) {
            _userMessage.value = "لا يوجد مشهد محدد لتطبيق الصوت عليه"
            return
        }

        val range = trimRange.value
        val startMs = range.start.toLong()
        val endMs = range.endInclusive.toLong()
        val effectiveDuration = (endMs - startMs).coerceAtLeast(1000L)

        val config = SceneAudioTrackConfig(
            audioId = item.id,
            soundTitle = item.title,
            soundUrl = item.audioUrl,
            category = item.category,
            isMusic = item.isMusic,
            volume = trackVolume.value,
            loop = isLooping.value,
            fadeIn = isFadeIn.value,
            fadeOut = isFadeOut.value,
            fadeInDurationMs = 1000L,
            fadeOutDurationMs = 1000L,
            startTrimMs = startMs,
            endTrimMs = endMs,
            effectiveDurationMs = effectiveDuration,
            attributionRequired = item.licenseType.requiresAttribution,
            attributionText = item.attributionText,
            licenseDisplayName = item.licenseType.displayName
        )

        viewModelScope.launch {
            val result = soundtrackRepository.attachTrackToScene(projectId, sceneId, config)
            result.onSuccess {
                _userMessage.value = "تم تعيين الصوت الخلفي للمشهد وتوثيق الرخصة"
                closeConfigureDialog()
            }.onFailure { err ->
                _userMessage.value = "فشل في حفظ الصوت بالمشهد: ${err.message}"
            }
        }
    }

    fun removeTrackFromScene() {
        if (sceneId.isNullOrBlank()) return
        viewModelScope.launch {
            val result = soundtrackRepository.removeTrackFromScene(projectId, sceneId)
            result.onSuccess {
                _userMessage.value = "تمت إزالة الصوت الخلفي من المشهد"
            }.onFailure { err ->
                _userMessage.value = "فشل في إزالة الصوت: ${err.message}"
            }
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

class SoundtrackLibraryViewModelFactory(
    private val projectId: String,
    private val sceneId: String? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SoundtrackLibraryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SoundtrackLibraryViewModel(projectId, sceneId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
