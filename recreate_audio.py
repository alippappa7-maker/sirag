import os

os.makedirs("app/src/main/java/com/siraj/app/core/audio", exist_ok=True)

audio_service_kt = """package com.siraj.app.core.audio

import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class SirajAudioService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var exoPlayer: ExoPlayer? = null

    override fun onCreate() {
        super.onCreate()
        
        exoPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true // handleAudioFocus
            )
            .build()
            
        mediaSession = MediaSession.Builder(this, exoPlayer!!)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
"""

audio_controller_kt = """package com.siraj.app.core.audio

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
    val isError: Boolean = false
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
        
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playbackState.value = _playbackState.value.copy(isPlaying = isPlaying)
                if (isPlaying) startProgressTracking() else stopProgressTracking()
            }
            
            override fun onPlaybackStateChanged(playbackState: Int) {
                _playbackState.value = _playbackState.value.copy(
                    isBuffering = playbackState == Player.STATE_BUFFERING,
                    isError = false
                )
                if (playbackState == Player.STATE_READY) {
                    _playbackState.value = _playbackState.value.copy(
                        duration = player.duration.coerceAtLeast(0L)
                    )
                }
            }
            
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                _playbackState.value = _playbackState.value.copy(isError = true, isPlaying = false)
            }
            
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                // Keep the currentTrack synchronized based on some ID if needed
            }
            
            override fun onPlaybackParametersChanged(playbackParameters: androidx.media3.common.PlaybackParameters) {
                _playbackState.value = _playbackState.value.copy(playbackSpeed = playbackParameters.speed)
            }
        })
    }
    
    fun playTrack(track: AudioTrack) {
        val player = mediaController ?: return
        
        val mediaItem = MediaItem.Builder()
            .setMediaId(track.id)
            .setUri(track.source) // For local testing, usually URL or local path
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.title)
                    .setArtist(track.speaker)
                    .setArtworkUri(if (track.coverUrl != null) android.net.Uri.parse(track.coverUrl) else null)
                    .build()
            )
            .build()
            
        _playbackState.value = _playbackState.value.copy(currentTrack = track, isError = false)
        
        player.setMediaItem(mediaItem)
        player.seekTo(track.listenProgressSeconds.toLong() * 1000L)
        player.prepare()
        player.play()
    }
    
    fun togglePlayPause() {
        val player = mediaController ?: return
        if (player.isPlaying) player.pause() else player.play()
    }
    
    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
        _playbackState.value = _playbackState.value.copy(currentPosition = positionMs)
    }
    
    fun setPlaybackSpeed(speed: Float) {
        mediaController?.setPlaybackSpeed(speed)
    }
    
    fun stop() {
        mediaController?.stop()
        _playbackState.value = PlaybackState()
        stopProgressTracking()
    }
    
    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                mediaController?.let {
                    _playbackState.value = _playbackState.value.copy(
                        currentPosition = it.currentPosition.coerceAtLeast(0L),
                        duration = it.duration.coerceAtLeast(0L)
                    )
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
"""

with open("app/src/main/java/com/siraj/app/core/audio/SirajAudioService.kt", "w") as f:
    f.write(audio_service_kt)

with open("app/src/main/java/com/siraj/app/core/audio/AudioController.kt", "w") as f:
    f.write(audio_controller_kt)

