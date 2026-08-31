package com.siraj.app.features.project.presentation.export

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.domain.models.ReviewState
import com.siraj.app.features.project.domain.models.*
import java.text.SimpleDateFormat
import java.util.*
import com.siraj.app.ui.theme.statusColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectExportScreen(
    projectId: String,
    onNavigateBack: () -> Unit,
    viewModel: ProjectExportViewModel = viewModel(factory = ProjectExportViewModelFactory(projectId)),
) {
    val context = LocalContext.current
    val project by viewModel.project.collectAsState()
    val scenes by viewModel.scenes.collectAsState()
    val validationReport by viewModel.validationReport.collectAsState()
    val selectedQuality by viewModel.selectedQuality.collectAsState()
    val selectedAspectRatio by viewModel.selectedAspectRatio.collectAsState()
    val selectedFps by viewModel.selectedFps.collectAsState()
    val burnSubtitles by viewModel.burnSubtitles.collectAsState()
    val includeSourceCitation by viewModel.includeSourceCitation.collectAsState()
    val includeWatermark by viewModel.includeWatermark.collectAsState()
    val isPreviewMode by viewModel.isPreviewMode.collectAsState()
    val availableCredits by viewModel.availableCredits.collectAsState()
    val calculatedCost by viewModel.calculatedCost.collectAsState()
    val storageUsedMb by viewModel.storageUsedMb.collectAsState()
    val storageLimitMb by viewModel.storageLimitMb.collectAsState()
    val activeJob by viewModel.activeJob.collectAsState()
    val projectJobs by viewModel.projectJobs.collectAsState()
    val showWarningDialog by viewModel.showWarningDialog.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    var showDeleteConfirmJobId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "تصدير الفيديو والإنتاج النهائي",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = project?.title ?: "مشروع بدون عنوان",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription =
                                androidx.compose.ui.res
                                    .stringResource(com.siraj.app.R.string.back),
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
        ) {
            // 1. Pre-Export Validation & Review Status Banner
            item {
                PreExportValidationBanner(
                    report = validationReport,
                    reviewState = project?.reviewState ?: ReviewState.DRAFT,
                )
            }

            // 2. Active Export Status Tracker (if job exists)
            activeJob?.let { job ->
                item {
                    Text(
                        text = "حالة عملية التصدير الحالية:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ActiveJobCard(
                        job = job,
                        onCancel = { viewModel.cancelJob(job.jobId) },
                        onRetry = { viewModel.retryJob(job.jobId) },
                        onDownload = { url ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        },
                        onShare = { url ->
                            val shareIntent =
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, project?.title ?: "فيديو منصة سراج")
                                    putExtra(Intent.EXTRA_TEXT, "شاهد فيديو سراج الموثق: $url")
                                }
                            context.startActivity(Intent.createChooser(shareIntent, "مشاركة الفيديو عبر"))
                        },
                        onCopyUrl = { url ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Siraj Video Link", url)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "تم نسخ الرابط المؤقت (صالح لمدة 7 أيام) 📋", Toast.LENGTH_SHORT).show()
                        },
                        onDelete = { showDeleteConfirmJobId = job.jobId },
                    )
                }
            }

            // 3. Export Settings Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إعدادات الدقة والإخراج", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }

                        // Output Type (Preview vs Full)
                        Column {
                            Text("نوع الإنتاج:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = !isPreviewMode,
                                    onClick = { viewModel.setIsPreviewMode(false) },
                                    label = { Text("إنتاج نهائي كامل") },
                                    leadingIcon = { Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                )
                                FilterChip(
                                    selected = isPreviewMode,
                                    onClick = { viewModel.setIsPreviewMode(true) },
                                    label = { Text("معاينة سريعة (10 ثوانٍ)") },
                                    leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                )
                            }
                        }

                        // Aspect Ratio
                        Column {
                            Text(
                                "أبعاد الإطار (Aspect Ratio):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(
                                    "9:16" to "رأسي (Reels/Shorts)",
                                    "1:1" to "مربع (Instagram)",
                                    "16:9" to "عريض (YouTube)",
                                ).forEach { (ratio, label) ->
                                    FilterChip(
                                        selected = selectedAspectRatio == ratio,
                                        onClick = { viewModel.setAspectRatio(ratio) },
                                        label = { Text(label, fontSize = 12.sp) },
                                    )
                                }
                            }
                        }

                        // Quality
                        Column {
                            Text("جودة الفيديو (Resolution):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ProductionQuality.entries.forEach { quality ->
                                    FilterChip(
                                        selected = selectedQuality == quality,
                                        onClick = { viewModel.setQuality(quality) },
                                        label = { Text(quality.label, fontSize = 12.sp) },
                                    )
                                }
                            }
                        }

                        // Frame Rate (FPS)
                        Column {
                            Text("معدل الإطارات (FPS):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(
                                    24 to "24 fps (سينمائي)",
                                    30 to "30 fps (قياسي)",
                                    60 to "60 fps (سلس جداً)",
                                ).forEach { (fps, label) ->
                                    FilterChip(
                                        selected = selectedFps == fps,
                                        onClick = { viewModel.setFps(fps) },
                                        label = { Text(label, fontSize = 12.sp) },
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Option Toggles
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ToggleOptionRow(
                                title = "تضمين الترجمة النصية المزامنة (Hardsub)",
                                description = "حرق أسطر الترجمة أسفل الشاشة مباشرة أثناء التصيير",
                                checked = burnSubtitles,
                                onCheckedChange = { viewModel.setBurnSubtitles(it) },
                                icon = Icons.Default.Subtitles,
                            )

                            ToggleOptionRow(
                                title = "إظهار التوثيق الشرعي واسم المصدر",
                                description = "تضمين شارة 'محتوى معتمد وموثق' والمصدر الشرعي للآيات والتفاسير",
                                checked = includeSourceCitation,
                                onCheckedChange = { viewModel.setIncludeSourceCitation(it) },
                                icon = Icons.Default.Verified,
                            )

                            ToggleOptionRow(
                                title = "إظهار العلامة المائية (منصة سراج)",
                                description = "وضع شعار سراج الهادئ في الزاوية علامة للإنتاج الإسلامي",
                                checked = includeWatermark,
                                onCheckedChange = { viewModel.setIncludeWatermark(it) },
                                icon = Icons.Default.BrandingWatermark,
                            )
                        }
                    }
                }
            }

            // 4. Credits & Storage Cost Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "تكلفة التصدير برصيد النقاط:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Text(
                                text = "$calculatedCost نقطة",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "رصيد الحساب المتاح:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text("$availableCredits نقطة", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Storage Usage Indicator
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    "مساحة التخزين المستهلكة في المساحة:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    "${storageUsedMb.toInt()} MB / ${storageLimitMb.toInt()} MB",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (storageUsedMb / storageLimitMb).toFloat().coerceIn(0f, 1f) },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                        }
                    }
                }
            }

            // 5. Start Export Action Button
            item {
                val isExportAllowed = validationReport.isExportAllowed
                val isJobRunning = activeJob?.isTerminal == false

                Button(
                    onClick = { viewModel.requestExport() },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                    enabled = isExportAllowed && !isJobRunning,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = if (validationReport.hasWarnings) Color(0xFFF59E0B) else MaterialTheme.colorScheme.primary,
                        ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    if (isJobRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("جاري معالجة وتصيير الفيديو...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (validationReport.hasWarnings) "بدء التصدير (يوجد تنبيهات)" else "بدء تصدير الفيديو الآن",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                    }
                }

                if (!isExportAllowed) {
                    Text(
                        text = "⚠️ لا يمكن التصدير لوجود موانع حرجة في المشروع. يرجى مراجعة التقرير أعلاه.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }

            // 6. History Export Jobs Section
            if (projectJobs.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "سجل عمليات الإنتاج والتصدير السابقة:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }

                items(projectJobs) { job ->
                    HistoricalJobRow(
                        job = job,
                        onDownload = { url -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) },
                        onDelete = { showDeleteConfirmJobId = job.jobId },
                    )
                }
            }
        }
    }

    // Warnings Confirmation Dialog
    if (showWarningDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleWarningDialog(false) },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF59E0B)) },
            title = { Text("تنبيهات قبل التصدير", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("المشروع يحتوي على التنبيهات التالية:")
                    validationReport.issues.filter { it.severity == ValidationSeverity.WARNING }.forEach { issue ->
                        Row(verticalAlignment = Alignment.Top) {
                            Text("• ", fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                            Column {
                                Text(issue.issueType.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                Text(
                                    issue.message,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "هل ترغب في متابعة التصدير وتجاهل التنبيهات؟",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.executeExport() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                ) {
                    Text("متابعة التصدير على أي حال")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleWarningDialog(false) }) {
                    Text("تراجع وتعديل")
                }
            },
        )
    }

    // Delete Confirmation Dialog
    showDeleteConfirmJobId?.let { jobId ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmJobId = null },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("تأكيد حذف الملف النهائي") },
            text = {
                Text(
                    "سيتم حذف ملف الفيديو والصورة المصغرة من التخزين السحابي نهائياً وإخلاء المساحة. لن تؤثر العملية على مشاهد أو نصوص المشروع الأصلي.",
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteExportedFile(jobId)
                        showDeleteConfirmJobId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) {
                    Text("حذف الملف النهائي")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmJobId = null }) {
                    Text(
                        androidx.compose.ui.res
                            .stringResource(com.siraj.app.R.string.cancel),
                    )
                }
            },
        )
    }
}

@Composable
private fun PreExportValidationBanner(
    report: PreExportReport,
    reviewState: ReviewState,
) {
    val isApproved = reviewState == ReviewState.APPROVED

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    when {
                        !report.isExportAllowed -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        report.hasWarnings -> MaterialTheme.statusColors.warningBg
                        else -> Color(0xFFD1FAE5)
                    },
            ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (report.isExportAllowed) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (report.isExportAllowed) Color(0xFF059669) else MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (report.isExportAllowed) "حالة جاهزية التصدير: مكتمل" else "حالة التصدير: محظور لوجود موانع",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                if (isApproved) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF10B981),
                    ) {
                        Text(
                            text = "معتمد شرعياً ✅",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            if (report.issues.isNotEmpty()) {
                report.issues.forEach { issue ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (issue.severity == ValidationSeverity.BLOCKER) "❌ " else "⚠️ ",
                            fontSize = 12.sp,
                        )
                        Column {
                            Text(issue.issueType.title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text(
                                issue.message,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveJobCard(
    job: ProductionJob,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    onDownload: (String) -> Unit,
    onShare: (String) -> Unit,
    onCopyUrl: (String) -> Unit,
    onDelete: () -> Unit,
) {
    val statusColor =
        when (job.status) {
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier =
                            Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(statusColor),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = job.status.labelArabic,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                    )
                }

                Text(
                    text = "${job.progress}%",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                )
            }

            // Live Progress Bar
            LinearProgressIndicator(
                progress = { job.progress / 100f },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            // Latest Log Message
            job.logs.lastOrNull()?.let { lastLog ->
                Text(
                    text = "📋 $lastLog",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Expiry Disclaimer
            if (job.status == ProductionJobStatus.COMPLETED) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFECFDF5),
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.LockClock, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "رابط موقع ومؤقت صالح لمدة 7 أيام للتحميل والمشاركة لحماية الخصوصية",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF065F46),
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (!job.isTerminal) {
                    OutlinedButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إلغاء التصدير")
                    }
                } else if (job.status == ProductionJobStatus.FAILED) {
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            androidx.compose.ui.res
                                .stringResource(com.siraj.app.R.string.retry),
                        )
                    }
                } else if (job.status == ProductionJobStatus.COMPLETED && !job.outputVideoUrl.isNullOrBlank()) {
                    val url = job.outputVideoUrl
                    Button(
                        onClick = { onDownload(url) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تنزيل")
                    }

                    OutlinedButton(
                        onClick = { onShare(url) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            androidx.compose.ui.res
                                .stringResource(com.siraj.app.R.string.share),
                        )
                    }

                    IconButton(onClick = { onCopyUrl(url) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "نسخ الرابط المؤقت")
                    }

                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف الملف", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoricalJobRow(
    job: ProductionJob,
    onDownload: (String) -> Unit,
    onDelete: () -> Unit,
) {
    val formatter = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }
    val dateStr = formatter.format(Date(job.createdAt))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(10.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${job.quality.label} (${job.aspectRatio})",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                    )
                    if (job.isPreviewOnly) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                            Text(
                                "معاينة",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                            )
                        }
                    }
                }
                Text(
                    text = "$dateStr • ${job.status.labelArabic}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (!job.outputVideoUrl.isNullOrBlank()) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { onDownload(job.outputVideoUrl) }) {
                        Icon(Icons.Default.Download, contentDescription = "تنزيل", modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription =
                                androidx.compose.ui.res
                                    .stringResource(com.siraj.app.R.string.delete),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleOptionRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                Text(description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}
