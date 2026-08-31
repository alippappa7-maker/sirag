package com.siraj.app.core.audio

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.siraj.app.domain.models.audio.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlaybackState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val currentTrack: AudioTrack? = null,
    val isError: Boolean = false,
)

object AudioController {
    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState = _playbackState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null

    fun initialize(context: Context) {
        if (mediaControllerFuture != null) return

        val sessionToken = SessionToken(context.applicationContext, ComponentName(context, SirajAudioService::class.java))
        mediaControllerFuture = MediaController.Builder(context.applicationContext, sessionToken).buildAsync()
        mediaControllerFuture?.addListener({
            mediaController = mediaControllerFuture?.get()
            setupPlayerListener()
        }, MoreExecutors.directExecutor())
    }

    private fun setupPlayerListener() {
        val player = mediaController ?: return

        player.addListener(
            object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _playbackState.value = _playbackState.value.copy(isPlaying = isPlaying)
                    if (isPlaying) startProgressTracking() else stopProgressTracking()
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    _playbackState.value =
                        _playbackState.value.copy(
                            isBuffering = playbackState == Player.STATE_BUFFERING,
                            isError = false,
                        )
                    if (playbackState == Player.STATE_READY) {
                        _playbackState.value =
                            _playbackState.value.copy(
                                duration = player.duration.coerceAtLeast(0L),
                            )
                    }
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    _playbackState.value = _playbackState.value.copy(isError = true, isPlaying = false)
                }

                override fun onMediaItemTransition(
                    mediaItem: MediaItem?,
                    reason: Int,
                ) {
                    // Keep the currentTrack synchronized based on some ID if needed
                }

                override fun onPlaybackParametersChanged(playbackParameters: androidx.media3.common.PlaybackParameters) {
                    _playbackState.value = _playbackState.value.copy(playbackSpeed = playbackParameters.speed)
                }
            },
        )
    }

    fun playTrack(
        track: AudioTrack,
        initialPositionMs: Long? = null,
    ) {
        val player = mediaController ?: return

        val mediaItem =
            MediaItem
                .Builder()
                .setMediaId(track.id)
                .setUri(track.source) // For local testing, usually URL or local path
                .setMediaMetadata(
                    MediaMetadata
                        .Builder()
                        .setTitle(track.title)
                        .setArtist(track.speaker)
                        .setArtworkUri(if (track.coverUrl != null) android.net.Uri.parse(track.coverUrl) else null)
                        .build(),
                ).build()

        _playbackState.value = _playbackState.value.copy(currentTrack = track, isError = false)

        player.setMediaItem(mediaItem)
        val resumePos = initialPositionMs ?: (track.listenProgressSeconds.toLong() * 1000L)
        if (resumePos > 0) {
            player.seekTo(resumePos)
        }
        player.prepare()
        player.play()
    }

    fun togglePlayPause() {
        val player = mediaController ?: return
        if (player.isPlaying) {
            player.pause()
            saveCurrentTrackProgress()
        } else {
            player.play()
        }
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
        _playbackState.value = _playbackState.value.copy(currentPosition = positionMs)
        saveCurrentTrackProgress()
    }

    fun setPlaybackSpeed(speed: Float) {
        mediaController?.setPlaybackSpeed(speed)
    }

    fun stop() {
        saveCurrentTrackProgress()
        mediaController?.stop()
        _playbackState.value = PlaybackState()
        stopProgressTracking()
    }

    private fun saveCurrentTrackProgress() {
        val track = _playbackState.value.currentTrack ?: return
        val pos = _playbackState.value.currentPosition
        val dur = _playbackState.value.duration
        if (dur > 0) {
            com.siraj.app.core.history.ActivityHistoryManager.recordProgress(
                entityType =
                    if (track.category.equals("recitation", ignoreCase = true)) {
                        com.siraj.app.domain.models.history.ActivityEntityType.QURAN_RECITATION
                    } else {
                        com.siraj.app.domain.models.history.ActivityEntityType.AUDIO
                    },
                entityId = track.id,
                title = track.title,
                subtitle = track.speaker,
                mediaUrl = track.source,
                thumbnailUrl = track.coverUrl,
                positionMs = pos,
                durationMs = dur,
            )
        }
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob =
            scope.launch {
                var counter = 0
                while (true) {
                    mediaController?.let {
                        val pos = it.currentPosition.coerceAtLeast(0L)
                        val dur = it.duration.coerceAtLeast(0L)
                        _playbackState.value =
                            _playbackState.value.copy(
                                currentPosition = pos,
                                duration = dur,
                            )
                        counter++
                        if (counter % 5 == 0) { // save every 5 seconds
                            saveCurrentTrackProgress()
                        }
                    }
                    delay(1000)
                }
            }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
    }

    fun release() {
        mediaControllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
        mediaControllerFuture = null
    }
}
