package com.siraj.app.features.audio.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.siraj.app.core.audio.AudioController
import com.siraj.app.core.ui.components.SirajGlowContainer
import com.siraj.app.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerScreen(onNavigateBack: () -> Unit) {
    val playbackState by AudioController.playbackState.collectAsState()
    val track = playbackState.currentTrack
    val spacing = LocalSpacing.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(track?.speaker ?: "المشغل", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.minimumInteractiveComponentSize()) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription =
                                androidx.compose.ui.res
                                    .stringResource(com.siraj.app.R.string.back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Menu */ }, modifier = Modifier.minimumInteractiveComponentSize()) {
                        Icon(Icons.Default.MoreVert, contentDescription = "المزيد")
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                    ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (track == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا يوجد مقطع قيد التشغيل", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Artwork / Visualizer placeholder
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .aspectRatio(1f),
                contentAlignment = Alignment.Center,
            ) {
                SirajGlowContainer(
                    modifier = Modifier.fillMaxSize(),
                    isActive = playbackState.isPlaying,
                    glowColor =
                        if (playbackState.isPlaying) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.tertiary
                                .copy(
                                    alpha = 0.5f,
                                )
                        },
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        // Tech-spiritual icon
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = if (playbackState.isPlaying) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.huge))

            // Title & Speaker
            Text(
                text = track.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(spacing.small))
            Text(
                text = track.speaker,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.tertiary,
            )

            Spacer(modifier = Modifier.height(spacing.large))

            // Progress Bar
            var sliderPosition by remember { mutableFloatStateOf(0f) }
            var isDragging by remember { mutableStateOf(false) }

            val currentPos = if (isDragging) sliderPosition.toLong() else playbackState.currentPosition

            Slider(
                value = currentPos.toFloat(),
                onValueChange = {
                    isDragging = true
                    sliderPosition = it
                },
                onValueChangeFinished = {
                    isDragging = false
                    AudioController.seekTo(sliderPosition.toLong())
                },
                valueRange = 0f..(playbackState.duration.coerceAtLeast(1L).toFloat()),
                modifier = Modifier.fillMaxWidth(),
                colors =
                    SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondary,
                        activeTrackColor = MaterialTheme.colorScheme.secondary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    formatDuration(currentPos),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    formatDuration(playbackState.duration),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(spacing.large))

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { /* Previous Track */ }, modifier = Modifier.size(48.dp)) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "السابق",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                IconButton(onClick = {
                    AudioController.seekTo((playbackState.currentPosition - 10000).coerceAtLeast(0))
                }, modifier = Modifier.size(48.dp)) {
                    Icon(
                        Icons.Default.FastRewind,
                        contentDescription = "إرجاع 10 ثواني",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Main Play/Pause Button
                SirajGlowContainer(
                    modifier = Modifier.size(80.dp),
                    isActive = true,
                    glowColor = MaterialTheme.colorScheme.secondary,
                ) {
                    FilledIconButton(
                        onClick = { AudioController.togglePlayPause() },
                        modifier = Modifier.fillMaxSize(),
                        shape = CircleShape,
                        colors =
                            IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary,
                            ),
                    ) {
                        if (playbackState.isBuffering) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(32.dp))
                        } else {
                            Icon(
                                imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "تشغيل/إيقاف",
                                modifier = Modifier.size(40.dp),
                            )
                        }
                    }
                }

                IconButton(onClick = {
                    AudioController.seekTo((playbackState.currentPosition + 10000).coerceAtMost(playbackState.duration))
                }, modifier = Modifier.size(48.dp)) {
                    Icon(
                        Icons.Default.FastForward,
                        contentDescription = "تقديم 10 ثواني",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                IconButton(onClick = { /* Next Track */ }, modifier = Modifier.size(48.dp)) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "التالي",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.large))

            // Additional controls (Speed, etc.)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                TextButton(
                    onClick = {
                        val newSpeed = if (playbackState.playbackSpeed >= 2f) 1f else playbackState.playbackSpeed + 0.25f
                        AudioController.setPlaybackSpeed(newSpeed)
                    },
                    modifier = Modifier.minimumInteractiveComponentSize(),
                ) {
                    Text(
                        "${playbackState.playbackSpeed}x",
                        color = MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.large))
        }
    }
}

private fun formatDuration(ms: Long): String {
    val seconds = (ms / 1000) % 60
    val minutes = (ms / (1000 * 60)) % 60
    val hours = ms / (1000 * 60 * 60)

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
