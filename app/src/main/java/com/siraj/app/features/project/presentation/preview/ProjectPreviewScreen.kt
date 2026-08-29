package com.siraj.app.features.project.presentation.preview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.domain.models.Scene
import com.siraj.app.features.project.domain.models.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectPreviewScreen(
    projectId: String,
    onNavigateBack: () -> Unit,
    onNavigateToSceneEdit: (sceneId: String) -> Unit,
    onNavigateToSubtitles: (sceneId: String) -> Unit,
    onNavigateToExportJob: () -> Unit = {},
    viewModel: ProjectPreviewViewModel = viewModel(factory = ProjectPreviewViewModelFactory(projectId))
) {
    val project by viewModel.project.collectAsState()
    val scenes by viewModel.scenes.collectAsState()
    val subtitles by viewModel.subtitles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val aspectRatio by viewModel.aspectRatio.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentTimeMs by viewModel.currentTimeMs.collectAsState()
    val totalDurationMs by viewModel.totalDurationMs.collectAsState()
    val currentSceneIndex by viewModel.currentSceneIndex.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val validationReport by viewModel.validationReport.collectAsState()
    val showValidationSheet by viewModel.showValidationSheet.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    val currentScene = remember(scenes, currentSceneIndex) {
        scenes.getOrNull(currentSceneIndex)
    }

    // Active Subtitle for the current playhead
    val activeSubtitle = remember(subtitles, currentScene, currentTimeMs, scenes) {
        if (currentScene != null) {
            // Calculate scene local time
            var accumulated = 0L
            for (i in 0 until currentSceneIndex) {
                accumulated += scenes[i].durationMs.coerceAtLeast(1000L)
            }
            val localSceneTime = currentTimeMs - accumulated
            subtitles.filter { it.sceneId == currentScene.id }
                .find { localSceneTime in it.startMs..it.endMs }
        } else null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(project?.title?.ifBlank { "معاينة المشروع" } ?: "معاينة المشروع", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${scenes.size} مشاهد • إجمالي المدة: ${formatDuration(totalDurationMs)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = androidx.compose.ui.res.stringResource(com.siraj.app.R.string.back))
                    }
                },
                actions = {
                    // Pre-export report badge button
                    IconButton(onClick = { viewModel.toggleValidationSheet() }) {
                        Badge(
                            containerColor = if (validationReport.blockerCount > 0) MaterialTheme.colorScheme.error
                            else if (validationReport.warningCount > 0) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.primary
                        ) {
                            Text("${validationReport.issues.size}")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (validationReport.isExportAllowed) "جاهز للتصدير والمراجعة" else "يوجد ${validationReport.blockerCount} موانع تصدير",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (validationReport.isExportAllowed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "${validationReport.warningCount} تنبيهات قابلة للمتابعة",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (validationReport.isExportAllowed) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.toggleValidationSheet() }
                            ) {
                                Text("التقرير")
                            }
                            Button(
                                onClick = onNavigateToExportJob,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تصدير الفيديو")
                            }
                        }
                    } else {
                        Button(
                            onClick = { viewModel.toggleValidationSheet() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("حل الموانع للتصدير")
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Aspect Ratio Selector (9:16 Reels / 16:9 Landscape / 1:1 Square)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("المقاس:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(10.dp))
                    listOf("9:16", "1:1", "16:9").forEach { ratio ->
                        FilterChip(
                            selected = aspectRatio == ratio,
                            onClick = { viewModel.setAspectRatio(ratio) },
                            label = { Text(ratio) },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }

                // Player Canvas Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFF0F0F14))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val frameModifier = when (aspectRatio) {
                        "9:16" -> Modifier
                            .fillMaxHeight()
                            .aspectRatio(9f / 16f)
                        "1:1" -> Modifier
                            .fillMaxHeight(0.85f)
                            .aspectRatio(1f)
                        else -> Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                    }

                    Box(
                        modifier = frameModifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    ) {
                        // Current Scene Graphic/Narration Backdrop Simulation
                        if (currentScene != null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    Text(
                                        text = "مشهد ${currentSceneIndex + 1}: ${currentScene.title.ifBlank { "بدون عنوان" }}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Text(
                                    text = currentScene.narrationText.ifBlank { "نص المشهد المرئي..." },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.85f),
                                    textAlign = TextAlign.Center,
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("لا توجد مشاهد للمعاينة", color = Color.White.copy(alpha = 0.5f))
                            }
                        }

                        // Subtitle Overlay at preview playhead
                        if (activeSubtitle != null) {
                            val align = when (activeSubtitle.style.position) {
                                SubtitlePosition.TOP -> Alignment.TopCenter
                                SubtitlePosition.MIDDLE -> Alignment.Center
                                SubtitlePosition.BOTTOM -> Alignment.BottomCenter
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = align
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xCC000000)
                                ) {
                                    Text(
                                        text = activeSubtitle.text,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        // Watermark / Mode Tag
                        Text(
                            text = "سراج • وضع المعاينة",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.35f),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        )
                    }
                }

                // Timeline Scrubber Slider & Timing Info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                ) {
                    Slider(
                        value = currentTimeMs.toFloat(),
                        onValueChange = { viewModel.seekTo(it.toLong()) },
                        valueRange = 0f..totalDurationMs.toFloat().coerceAtLeast(1000f),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatDuration(currentTimeMs),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "مشهد ${currentSceneIndex + 1} من ${scenes.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = formatDuration(totalDurationMs),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Media Player Controls Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute / Unmute
                    IconButton(onClick = { viewModel.toggleMute() }) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.Close else Icons.Default.PlayArrow,
                            contentDescription = "كتم الصوت",
                            tint = if (isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Previous Scene
                    IconButton(
                        onClick = { viewModel.previousScene() },
                        enabled = currentSceneIndex > 0
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "المشهد السابق")
                    }

                    // Play / Pause FAB
                    FilledIconButton(
                        onClick = { viewModel.togglePlayPause() },
                        modifier = Modifier.size(52.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Check else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    // Next Scene
                    IconButton(
                        onClick = { viewModel.nextScene() },
                        enabled = currentSceneIndex < scenes.lastIndex
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "المشهد التالي")
                    }

                    // Playback Speed Selector (0.5x, 1.0x, 1.5x, 2.0x)
                    TextButton(
                        onClick = {
                            val nextSpeed = when (playbackSpeed) {
                                0.5f -> 1.0f
                                1.0f -> 1.5f
                                1.5f -> 2.0f
                                else -> 0.5f
                            }
                            viewModel.setPlaybackSpeed(nextSpeed)
                        }
                    ) {
                        Text("${playbackSpeed}x", fontWeight = FontWeight.Bold)
                    }
                }

                // Horizontal Scenes Thumbnails Row
                Text(
                    text = "مشاهد المشروع (انقر للانتقال الفوري):",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(scenes) { idx, sc ->
                        val isSelected = idx == currentSceneIndex
                        Card(
                            modifier = Modifier
                                .width(110.dp)
                                .height(60.dp)
                                .clickable { viewModel.jumpToScene(idx) },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = if (isSelected) CardDefaults.outlinedCardBorder() else null
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${idx + 1}. ${sc.title.ifBlank { "مشهد" }}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${sc.durationMs / 1000f} ثانية",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Pre-Export Validation Sheet
    if (showValidationSheet) {
        ModalBottomSheet(onDismissRequest = { viewModel.toggleValidationSheet() }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تقرير فحص ما قبل التصدير", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (validationReport.isExportAllowed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = if (validationReport.isExportAllowed) "التصدير مسموح ✅" else "ممنوع لوجود موانع ⛔",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = if (validationReport.isExportAllowed) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (validationReport.issues.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("المشروع مكتمل وخالٍ من أي موانع أو تحذيرات!", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(validationReport.issues) { issue ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (issue.severity == ValidationSeverity.BLOCKER)
                                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                                    else MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (issue.severity == ValidationSeverity.BLOCKER) "⛔ مانع حرج" else "⚠️ تحذير",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (issue.severity == ValidationSeverity.BLOCKER) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(issue.issueType.title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.weight(1f))
                                        if (issue.sceneId != null) {
                                            TextButton(
                                                onClick = {
                                                    viewModel.toggleValidationSheet()
                                                    if (issue.issueType == ValidationIssueType.OVERLAPPING_SUBTITLES) {
                                                        onNavigateToSubtitles(issue.sceneId)
                                                    } else {
                                                        onNavigateToSceneEdit(issue.sceneId)
                                                    }
                                                },
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text("إصلاح الآن", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }

                                    Text(issue.message, style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "💡 التوصية: ${issue.fixRecommendation}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.toggleValidationSheet() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إغلاق التقرير")
                }
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    val fraction = (ms % 1000) / 100
    return String.format("%02d:%02d.%d", min, sec, fraction)
}
