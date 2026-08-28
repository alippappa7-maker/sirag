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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.siraj.app.core.audio.AudioController
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerScreen(
    onNavigateBack: () -> Unit
) {
    val playbackState by AudioController.playbackState.collectAsState()
    val track = playbackState.currentTrack

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(track?.speaker ?: "المشغل") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "عودة")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Menu */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "المزيد")
                    }
                }
            )
        }
    ) { padding ->
        if (track == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا يوجد مقطع قيد التشغيل")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Artwork
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow, // Replace with actual image later
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title & Speaker
            Text(
                text = track.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = track.speaker,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

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
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatDuration(currentPos), style = MaterialTheme.typography.labelMedium)
                Text(formatDuration(playbackState.duration), style = MaterialTheme.typography.labelMedium)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* Previous Track */ }) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "السابق", modifier = Modifier.size(32.dp))
                }
                
                IconButton(onClick = { AudioController.seekTo((playbackState.currentPosition - 10000).coerceAtLeast(0)) }) {
                    Icon(Icons.Default.FastRewind, contentDescription = "إرجاع 10 ثواني", modifier = Modifier.size(32.dp))
                }

                FilledIconButton(
                    onClick = { AudioController.togglePlayPause() },
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape
                ) {
                    if (playbackState.isBuffering) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(
                            imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "تشغيل/إيقاف",
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                
                IconButton(onClick = { AudioController.seekTo((playbackState.currentPosition + 10000).coerceAtMost(playbackState.duration)) }) {
                    Icon(Icons.Default.FastForward, contentDescription = "تقديم 10 ثواني", modifier = Modifier.size(32.dp))
                }

                IconButton(onClick = { /* Next Track */ }) {
                    Icon(Icons.Default.SkipNext, contentDescription = "التالي", modifier = Modifier.size(32.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Additional controls (Speed, Sleep Timer, etc.)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = { 
                    val newSpeed = if (playbackState.playbackSpeed >= 2f) 1f else playbackState.playbackSpeed + 0.25f
                    AudioController.setPlaybackSpeed(newSpeed)
                }) {
                    Text("${playbackState.playbackSpeed}x")
                }
                
                // You can add sleep timer icon here
            }
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
