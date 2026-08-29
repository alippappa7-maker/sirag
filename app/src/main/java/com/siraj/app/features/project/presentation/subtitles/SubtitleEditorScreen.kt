package com.siraj.app.features.project.presentation.subtitles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.features.project.domain.models.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitleEditorScreen(
    projectId: String,
    sceneId: String,
    initialSceneText: String = "",
    onNavigateBack: () -> Unit,
    viewModel: SubtitleEditorViewModel = viewModel(
        factory = SubtitleEditorViewModelFactory(projectId, sceneId, initialSceneText)
    )
) {
    val subtitles by viewModel.subtitles.collectAsState()
    val selectedLang by viewModel.selectedLanguage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val exportedContent by viewModel.exportedContent.collectAsState()
    val exportFormat by viewModel.exportFormat.collectAsState()
    val editingSubtitle by viewModel.editingSubtitle.collectAsState()
    val currentStyle by viewModel.currentStyle.collectAsState()
    val sceneDurationMs by viewModel.sceneDurationMs.collectAsState()
    val previewTimeMs by viewModel.previewCurrentTimeMs.collectAsState()

    val filteredSubtitles = remember(subtitles, selectedLang) {
        subtitles.filter { it.language == selectedLang }
    }

    // Active subtitle at preview playhead
    val activePreviewSub = remember(filteredSubtitles, previewTimeMs) {
        filteredSubtitles.find { previewTimeMs in it.startMs..it.endMs }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    var showStyleSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("محرر الترجمة والشارات (Subtitles)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "مزامنة التوقيت • دعم RTL • قفل النصوص الشرعية",
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
                    IconButton(onClick = { showStyleSheet = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "تخصيص المظهر")
                    }
                    IconButton(onClick = { viewModel.exportSrt() }) {
                        Icon(Icons.Default.Share, contentDescription = "تصدير SRT")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.onAddNewSubtitle() },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("إضافة سطر") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Video Subtitle Preview Frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFF121212))
                    .padding(12.dp)
            ) {
                // Mock Video Backdrop
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E1E24)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "معاينة الفيديو • التوقيت: ${(previewTimeMs / 1000f)} ث / ${(sceneDurationMs / 1000f)} ث",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }

                // Subtitle Overlay rendered according to style
                if (activePreviewSub != null) {
                    val sub = activePreviewSub!!
                    val alignment = when (currentStyle.position) {
                        SubtitlePosition.TOP -> Alignment.TopCenter
                        SubtitlePosition.MIDDLE -> Alignment.Center
                        SubtitlePosition.BOTTOM -> Alignment.BottomCenter
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = alignment
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x99000000)
                        ) {
                            Text(
                                text = sub.text,
                                color = Color.White,
                                fontSize = currentStyle.fontSizeSp.sp,
                                fontWeight = if (currentStyle.isBold) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Preview Playhead Scrubber
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0:00", style = MaterialTheme.typography.labelSmall)
                    Text("مسطرة توقيت المشهد (Scrubber)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text("${(sceneDurationMs / 1000f)}s", style = MaterialTheme.typography.labelSmall)
                }
                Slider(
                    value = previewTimeMs.toFloat(),
                    onValueChange = { viewModel.setPreviewTime(it.toLong()) },
                    valueRange = 0f..sceneDurationMs.toFloat().coerceAtLeast(1000f),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Language Switcher & Generator Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedLang == "ar",
                    onClick = { viewModel.onLanguageChange("ar") },
                    label = { Text("العربية (الأصل)") }
                )
                FilterChip(
                    selected = selectedLang == "en",
                    onClick = { viewModel.onLanguageChange("en") },
                    label = { Text("English (Draft)") }
                )

                Spacer(modifier = Modifier.weight(1f))

                FilledTonalButton(
                    onClick = { viewModel.generateSubtitlesFromNarration() },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("توليد من النص", style = MaterialTheme.typography.labelSmall)
                }

                if (selectedLang == "ar") {
                    OutlinedButton(
                        onClick = { viewModel.autoTranslateToArabicOrEnglish() },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("ترجمة إلى EN", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Subtitle Lines List
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (filteredSubtitles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد أسطر ترجمة مضافة لهذه اللغة",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.generateSubtitlesFromNarration() }) {
                            Text("توليد الترجمة تلقائياً من نص المشهد")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredSubtitles, key = { it.id }) { item ->
                        SubtitleLineCard(
                            item = item,
                            isActive = previewTimeMs in item.startMs..item.endMs,
                            onEdit = { viewModel.onSelectSubtitleForEdit(item) },
                            onDelete = { viewModel.onDeleteSubtitle(item) },
                            onJumpTo = { viewModel.setPreviewTime(item.startMs) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Modal Sheet for Style Customization
    if (showStyleSheet) {
        ModalBottomSheet(onDismissRequest = { showStyleSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text("تخصيص نمط الترجمة (Subtitle Style)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                // Position Selector
                Text("موضع الترجمة على الشاشة:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SubtitlePosition.values().forEach { pos ->
                        FilterChip(
                            selected = currentStyle.position == pos,
                            onClick = { viewModel.updateStyle(position = pos) },
                            label = { Text(pos.displayName) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Font Family
                Text("نوع الخط العربي:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SubtitleFontFamily.values().forEach { font ->
                        FilterChip(
                            selected = currentStyle.fontFamily == font,
                            onClick = { viewModel.updateStyle(fontFamily = font) },
                            label = { Text(font.displayName) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Font Size Slider
                Text("حجم الخط (${currentStyle.fontSizeSp} sp):", style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = currentStyle.fontSizeSp.toFloat(),
                    onValueChange = { viewModel.updateStyle(fontSizeSp = it.toInt()) },
                    valueRange = 14f..32f,
                    steps = 9
                )

                // Burn-in Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.updateStyle(burnIntoVideo = !currentStyle.burnIntoVideo) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = currentStyle.burnIntoVideo,
                        onCheckedChange = { viewModel.updateStyle(burnIntoVideo = it) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("حرق الترجمة داخل الفيديو (Hardsub)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text("تثبيت الترجمة مدمجة في الإطارات المرئية عند التصدير", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showStyleSheet = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("حفظ وتطبيق")
                }
            }
        }
    }

    // Edit Subtitle Line Dialog
    if (editingSubtitle != null) {
        val sub = editingSubtitle!!
        var textValue by remember(sub) { mutableStateOf(sub.text) }
        var startSec by remember(sub) { mutableStateOf((sub.startMs / 1000f).toString()) }
        var endSec by remember(sub) { mutableStateOf((sub.endMs / 1000f).toString()) }

        AlertDialog(
            onDismissRequest = { viewModel.onDismissEdit() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (sub.locked) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تعديل التوقيت (النص مقفل شرعياً)")
                    } else {
                        Text("تعديل سطر الترجمة")
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (sub.locked) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "🔒 هذا النص موثق ومقفل شرعياً كآية قرآنية أو حديث شريف، يُسمح بضبط توقيت الظهور فقط.",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = textValue,
                        onValueChange = { textValue = it },
                        label = { Text("نص الترجمة") },
                        enabled = !sub.locked,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = startSec,
                            onValueChange = { startSec = it },
                            label = { Text("البداية (ثانية)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = endSec,
                            onValueChange = { endSec = it },
                            label = { Text("النهاية (ثانية)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sMs = ((startSec.toFloatOrNull() ?: 0f) * 1000).toLong()
                        val eMs = ((endSec.toFloatOrNull() ?: 3f) * 1000).toLong()
                        viewModel.onUpdateSubtitle(textValue, sMs, eMs)
                    }
                ) {
                    Text("حفظ التعديل")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissEdit() }) {
                    Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.cancel))
                }
            }
        )
    }

    // Export Dialog (SRT / VTT)
    if (exportedContent != null) {
        val format = exportFormat ?: "SRT"
        val content = exportedContent!!

        AlertDialog(
            onDismissRequest = { viewModel.closeExportDialog() },
            title = { Text("تصدير ملف الترجمة ($format)") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("تم توليد محتوى ملف الترجمة بنجاح وفق المعايير القياسية:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        Text(
                            text = content,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(content))
                        viewModel.closeExportDialog()
                    }
                ) {
                    Text("نسخ إلى الحافظة")
                }
            },
            dismissButton = {
                Row {
                    if (format == "SRT") {
                        TextButton(onClick = { viewModel.exportVtt() }) {
                            Text("تصدير كـ VTT")
                        }
                    }
                    TextButton(onClick = { viewModel.closeExportDialog() }) {
                        Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.close))
                    }
                }
            }
        )
    }
}

@Composable
fun SubtitleLineCard(
    item: SubtitleItem,
    isActive: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onJumpTo: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onJumpTo() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time tag
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${(item.startMs / 1000f)}s ➔ ${(item.endMs / 1000f)}s",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Source / Lock badge
                if (item.locked) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            text = "🔒 نص موثق مقفل",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else if (item.reviewStatus == SubtitleReviewStatus.PENDING_REVIEW) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = "بانتظار المراجعة",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = androidx.compose.ui.res.stringResource(com.siraj.app.R.string.edit), modifier = Modifier.size(16.dp))
                }

                if (!item.locked) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = androidx.compose.ui.res.stringResource(com.siraj.app.R.string.delete), modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
