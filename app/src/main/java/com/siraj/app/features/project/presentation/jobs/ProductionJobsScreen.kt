package com.siraj.app.features.project.presentation.jobs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.features.project.domain.models.ProductionJob
import com.siraj.app.features.project.domain.models.ProductionJobStatus
import com.siraj.app.features.project.domain.models.ProductionQuality
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionJobsScreen(
    projectId: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: ProductionJobsViewModel = viewModel(factory = ProductionJobsViewModelFactory(projectId))
) {
    val jobs by viewModel.jobs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val showCreateDialog by viewModel.showCreateJobDialog.collectAsState()
    val selectedQuality by viewModel.selectedQuality.collectAsState()
    val burnSubtitles by viewModel.burnSubtitles.collectAsState()
    val selectedAspectRatio by viewModel.selectedAspectRatio.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current
    var selectedJobForLogs by remember { mutableStateOf<ProductionJob?>(null) }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            if (projectId != null) "مهام إنتاج المشروع" else "طابور مهام الإنتاج (Cloud Tasks)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "معالجة سحابية غير متزامنة • تتبع مباشر",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "عودة")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (!projectId.isNullOrBlank()) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.openCreateJobDialog() },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("بدء تصدير جديد") },
                    containerColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (jobs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CloudQueue,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("لا توجد مهام إنتاج حالية", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "يمكنك بدء تصدير الفيديو من خلال شاشة معاينة المشروع أو الزر أدناه.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(jobs, key = { it.jobId }) { job ->
                        ProductionJobCard(
                            job = job,
                            onCancel = { viewModel.cancelJob(job.jobId) },
                            onRetry = { viewModel.retryJob(job.jobId) },
                            onViewLogs = { selectedJobForLogs = job },
                            onCopyUrl = { url ->
                                clipboardManager.setText(AnnotatedString(url))
                            }
                        )
                    }
                }
            }
        }
    }

    // Create Job Dialog
    if (showCreateDialog && projectId != null) {
        AlertDialog(
            onDismissRequest = { viewModel.closeCreateJobDialog() },
            title = { Text("بدء مهمة إنتاج وتصدير الفيديو", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "سيتم إدراج المهمة في طابور المعالجة السحابية وحجز الرصيد المطلوب تلقائياً.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Output Type Selection (Preview vs Full)
                    Text("نوع الإخراج المطلوب:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !viewModel.isPreviewMode.collectAsState().value,
                            onClick = { viewModel.setIsPreviewMode(false) },
                            label = { Text("إنتاج كامل نهائي", fontSize = 12.sp) }
                        )
                        FilterChip(
                            selected = viewModel.isPreviewMode.collectAsState().value,
                            onClick = { viewModel.setIsPreviewMode(true) },
                            label = { Text("معاينة سريعة (10 ثوانٍ)", fontSize = 12.sp) }
                        )
                    }

                    // Quality Selection
                    Text("جودة الإخراج:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProductionQuality.values().forEach { quality ->
                            FilterChip(
                                selected = selectedQuality == quality,
                                onClick = { viewModel.setQuality(quality) },
                                label = { Text(quality.label, fontSize = 12.sp) }
                            )
                        }
                    }


                    // Aspect Ratio
                    Text("أبعاد الإطار:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("9:16", "1:1", "16:9").forEach { ratio ->
                            FilterChip(
                                selected = selectedAspectRatio == ratio,
                                onClick = { viewModel.setAspectRatio(ratio) },
                                label = { Text(ratio) }
                            )
                        }
                    }

                    // Burn Subtitles Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("حرق الترجمة داخل الفيديو", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("دمج الشارات والترجمة بوضوح دائم", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = burnSubtitles,
                            onCheckedChange = { viewModel.setBurnSubtitles(it) }
                        )
                    }

                    // Cost estimation card
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("التكلفة المقدرة:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "${(10 * selectedQuality.costMultiplier).toInt()} وحدة رصيد",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.submitProductionJob(projectId) },
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("تأكيد وإرسال للطابور")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeCreateJobDialog() }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // View Logs Bottom Sheet
    selectedJobForLogs?.let { job ->
        ModalBottomSheet(onDismissRequest = { selectedJobForLogs = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text("سجل أحداث المهمة (Job Logs)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("معرّف المهمة: ${job.jobId.take(12)}...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F172A),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp, max = 350.dp)
                ) {
                    LazyColumn(modifier = Modifier.padding(12.dp)) {
                        if (job.logs.isEmpty()) {
                            item {
                                Text("لا توجد سجلات بعد.", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                            }
                        } else {
                            items(job.logs) { log ->
                                Text("• $log", color = Color(0xFF38BDF8), fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { selectedJobForLogs = null },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إغلاق السجل")
                }
            }
        }
    }
}

@Composable
private fun ProductionJobCard(
    job: ProductionJob,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    onViewLogs: () -> Unit,
    onCopyUrl: (String) -> Unit
) {
    val statusColor = when (job.status) {
        ProductionJobStatus.QUEUED -> MaterialTheme.colorScheme.tertiary
        ProductionJobStatus.PROCESSING -> MaterialTheme.colorScheme.secondary
        ProductionJobStatus.COMPOSING -> Color(0xFF8B5CF6)
        ProductionJobStatus.ENCODING -> Color(0xFFF59E0B)
        ProductionJobStatus.RENDERING -> MaterialTheme.colorScheme.primary
        ProductionJobStatus.UPLOADING -> Color(0xFF06B6D4)
        ProductionJobStatus.COMPLETED -> Color(0xFF10B981)
        ProductionJobStatus.FAILED -> MaterialTheme.colorScheme.error
        ProductionJobStatus.CANCELLED -> MaterialTheme.colorScheme.outline
    }

    val statusText = "${job.status.labelArabic} ${if (job.status == ProductionJobStatus.COMPLETED) "✅" else if (job.status == ProductionJobStatus.FAILED) "❌" else ""}"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (job.isPreviewOnly) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "معاينة سريعة",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = formatTimestamp(job.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(job.projectTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            Text(
                text = "الجودة: ${job.quality.label} • الأبعاد: ${job.aspectRatio} • التكلفة: ${job.costUnits} وحدة",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Metadata summary if completed or processing
            if (job.videoDurationMs > 0 || job.fileSizeBytes > 0) {
                val durationSec = job.videoDurationMs / 1000
                val sizeMb = if (job.fileSizeBytes > 0) String.format(Locale.US, "%.1f MB", job.fileSizeBytes / (1024.0 * 1024.0)) else ""
                Text(
                    text = "المدة: ${durationSec} ثانية ${if (sizeMb.isNotBlank()) "• الحجم: $sizeMb" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            // Progress Bar if in progress
            if (!job.isTerminal) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { job.progress / 100f },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("${job.progress}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            // Error info if failed
            if (job.status == ProductionJobStatus.FAILED && !job.errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "سبب الفشل: ${job.errorMessage}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        if (job.creditRefunded) {
                            Text(
                                text = "✅ تم استرداد رصيد المهمة (${job.costUnits} وحدة) تلقائياً.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Output / Download section if completed
            if (job.status == ProductionJobStatus.COMPLETED && !job.outputVideoUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("الفيديو جاهز للتحميل والمشاركة", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            Text("رابط موقع ومؤقت صالح لمدة 7 أيام", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { onCopyUrl(job.outputVideoUrl) }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "نسخ رابط التحميل")
                        }
                    }
                }
            }


            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onViewLogs) {
                    Icon(Icons.Default.Notes, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("السجلات", fontSize = 12.sp)
                }

                if (job.canCancel) {
                    Spacer(modifier = Modifier.width(4.dp))
                    OutlinedButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إلغاء", fontSize = 12.sp)
                    }
                }

                if (job.status == ProductionJobStatus.FAILED || job.status == ProductionJobStatus.CANCELLED) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(onClick = onRetry) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إعادة المحاولة", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timeMs: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timeMs))
}
