package com.siraj.app.core.audio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.siraj.app.core.accessibility.AccessibilitySemantics.sirajClickable
import com.siraj.app.core.accessibility.AccessibilitySemantics.sirajLiveRegion
import com.siraj.app.core.accessibility.AccessibilitySemantics.sirajTouchTarget
import com.siraj.app.core.accessibility.LocalAccessibilityConfig
import com.siraj.app.core.ui.components.SirajGlowContainer
import com.siraj.app.core.ui.components.SirajTechCard

@Composable
fun MiniPlayer(
    modifier: Modifier = Modifier,
    onExpand: () -> Unit = {}
) {
    val playbackState by AudioController.playbackState.collectAsState()
    val a11yConfig = LocalAccessibilityConfig.current
    var showTranscriptDialog by remember { mutableStateOf(false) }
    
    AnimatedVisibility(
        visible = playbackState.currentTrack != null,
        enter = if (a11yConfig.reduceMotion) EnterTransition.None else slideInVertically(initialOffsetY = { it }),
        exit = if (a11yConfig.reduceMotion) ExitTransition.None else slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        val track = playbackState.currentTrack ?: return@AnimatedVisibility
        val hasTranscript = !track.transcript.isNullOrBlank() || track.transcriptSegments.isNotEmpty()
        
        SirajGlowContainer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            isActive = playbackState.isPlaying,
            glowColor = MaterialTheme.colorScheme.tertiary
        ) {
            SirajTechCard(
                isActive = playbackState.isPlaying,
                onClick = onExpand,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Artwork / Placeholder
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (playbackState.isPlaying) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            if (playbackState.isBuffering) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .semantics { contentDescription = "جاري تحميل المقطع الصوتي مؤقتاً" },
                                    color = MaterialTheme.colorScheme.onTertiary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (playbackState.isPlaying) "المشغل في حالة تشغيل" else "المشغل متوقف مؤقتاً",
                                    tint = if (playbackState.isPlaying) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Track Info
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .semantics(mergeDescendants = true) {}
                        ) {
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = track.speaker,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        // Transcript Button if available or enabled
                        if (hasTranscript || a11yConfig.showTranscripts) {
                            IconButton(
                                onClick = { showTranscriptDialog = true },
                                modifier = Modifier.sirajTouchTarget()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = "عرض التفريغ النصي للصوت",
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }

                        // Controls
                        IconButton(
                            onClick = { AudioController.togglePlayPause() },
                            modifier = Modifier.sirajTouchTarget()
                        ) {
                            Icon(
                                imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (playbackState.isPlaying) "إيقاف الصوت مؤقتاً" else "استئناف تشغيل الصوت",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }

                        IconButton(
                            onClick = { AudioController.stop() },
                            modifier = Modifier.sirajTouchTarget()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق وإيقاف المشغل الصوتي",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Mini Progress Bar
                    if (playbackState.duration > 0) {
                        val progress = playbackState.currentPosition.toFloat() / playbackState.duration.toFloat()
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .semantics {
                                    contentDescription = "شريط التقدم الصوتي: ${(progress * 100).toInt()} بالمائة"
                                },
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = Color.Transparent
                        )
                    }
                }
            }
        }
    }

    // Audio Transcript Dialog
    if (showTranscriptDialog && playbackState.currentTrack != null) {
        val currentTrack = playbackState.currentTrack ?: return
        AlertDialog(
            onDismissRequest = { showTranscriptDialog = false },
            title = {
                Text(
                    text = "التفريغ النصي: ${currentTrack.title}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.semantics { heading() }
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                        .sirajLiveRegion(isAssertive = false)
                ) {
                    if (currentTrack.transcriptSegments.isNotEmpty()) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(currentTrack.transcriptSegments) { segment ->
                                val isCurrent = playbackState.currentPosition in segment.startMs..segment.endMs
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            AudioController.seekTo(segment.startMs)
                                        }
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = segment.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    } else if (!currentTrack.transcript.isNullOrBlank()) {
                        Text(
                            text = currentTrack.transcript ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        Text(
                            text = "جاري إعداد وتوثيق التفريغ النصي لهذا المقطع.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showTranscriptDialog = false },
                    modifier = Modifier.sirajTouchTarget()
                ) {
                    Text("إغلاق")
                }
            }
        )
    }
}
