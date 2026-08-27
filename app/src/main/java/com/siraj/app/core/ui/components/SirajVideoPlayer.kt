package com.siraj.app.core.ui.components

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import java.util.Locale

@OptIn(UnstableApi::class)
@Composable
fun SirajVideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    thumbnailUrl: String? = null,
    autoPlay: Boolean = false,
    onDownloadClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    onFullScreenToggle: (() -> Unit)? = null,
    aspectRatioMode: Int = AspectRatioFrameLayout.RESIZE_MODE_FIT
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(autoPlay) }
    var isBuffering by remember { mutableStateOf(false) }
    var duration by remember { mutableStateOf(0L) }
    var currentPosition by remember { mutableStateOf(0L) }
    var showControls by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var isMuted by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableStateOf(1.0f) }
    var showThumbnail by remember { mutableStateOf(true) }

    // Initialize player
    DisposableEffect(context, videoUrl) {
        val player = ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl)))
            playWhenReady = autoPlay
            prepare()
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                    isPlaying = isPlayingNow
                    if (isPlayingNow) {
                        showThumbnail = false
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    isBuffering = playbackState == Player.STATE_BUFFERING
                    if (playbackState == Player.STATE_READY) {
                        duration = this@apply.duration.coerceAtLeast(0L)
                    }
                }

                override fun onPlayerError(playbackException: PlaybackException) {
                    error = if (playbackException.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED) {
                        "Network Error. Please check your connection."
                    } else if (playbackException.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS) {
                        "Link expired or unauthorized. Please refresh."
                    } else {
                        "Error playing video: ${playbackException.message}"
                    }
                }
            })
        }
        exoPlayer = player

        onDispose {
            player.release()
            exoPlayer = null
        }
    }

    // Lifecycle handling (pause on background)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                exoPlayer?.pause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Progress updater
    LaunchedEffect(exoPlayer, isPlaying) {
        while (isPlaying) {
            exoPlayer?.let {
                currentPosition = it.currentPosition.coerceAtLeast(0L)
            }
            delay(500.milliseconds)
        }
    }

    // Auto-hide controls
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(3000.milliseconds)
            showControls = false
        }
    }

    Box(
        modifier = modifier
            .background(Color.Black)
            .clickable { showControls = !showControls }
    ) {
        // Video Surface
        exoPlayer?.let { player ->
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = player
                        useController = false
                        resizeMode = aspectRatioMode
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier.matchParentSize()
            )
        }

        // Thumbnail Overlay
        if (showThumbnail && thumbnailUrl != null && !isPlaying) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = "Video Thumbnail",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }

        // Loading Indicator
        if (isBuffering) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Error Message
        if (error != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.ErrorOutline, contentDescription = "Error", tint = Color.White, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error ?: "",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    error = null
                    exoPlayer?.prepare()
                    exoPlayer?.play()
                }) {
                    Text("Retry")
                }
            }
        } else {
            // Controls Overlay
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.matchParentSize()
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    // Top Bar (Download, Share)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .align(Alignment.TopEnd),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (onDownloadClick != null) {
                            IconButton(onClick = onDownloadClick) {
                                Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.White)
                            }
                        }
                        if (onShareClick != null) {
                            IconButton(onClick = onShareClick) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                            }
                        }
                    }

                    // Play/Pause Center Button
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable {
                                if (isPlaying) exoPlayer?.pause() else {
                                    showThumbnail = false
                                    exoPlayer?.play()
                                }
                            }
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    // Bottom Bar (Progress, Time, Speed, Fullscreen, Mute)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatTime(currentPosition),
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Slider(
                                value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                                onValueChange = { value ->
                                    val newPos = (value * duration).toLong()
                                    currentPosition = newPos
                                    exoPlayer?.seekTo(newPos)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.5f)
                                )
                            )
                            Text(
                                text = formatTime(duration),
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left actions
                            Row {
                                IconButton(onClick = {
                                    isMuted = !isMuted
                                    exoPlayer?.volume = if (isMuted) 0f else 1f
                                }) {
                                    Icon(
                                        imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = if (isMuted) "Unmute" else "Mute",
                                        tint = Color.White
                                    )
                                }
                                TextButton(onClick = {
                                    playbackSpeed = when (playbackSpeed) {
                                        1.0f -> 1.5f
                                        1.5f -> 2.0f
                                        2.0f -> 0.5f
                                        0.5f -> 1.0f
                                        else -> 1.0f
                                    }
                                    exoPlayer?.setPlaybackSpeed(playbackSpeed)
                                }) {
                                    Text(
                                        text = "${playbackSpeed}x",
                                        color = Color.White
                                    )
                                }
                            }
                            
                            // Right actions
                            Row {
                                if (onFullScreenToggle != null) {
                                    IconButton(onClick = onFullScreenToggle) {
                                        Icon(
                                            imageVector = Icons.Default.Fullscreen,
                                            contentDescription = "Full Screen",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds)
}
