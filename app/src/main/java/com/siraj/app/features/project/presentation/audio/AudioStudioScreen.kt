package com.siraj.app.features.project.presentation.audio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.features.project.domain.models.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioStudioScreen(
    projectId: String,
    sceneId: String? = null,
    initialNarrationText: String = "",
    onNavigateBack: () -> Unit,
    viewModel: AudioStudioViewModel = viewModel(
        factory = AudioStudioViewModelFactory(projectId, sceneId, initialNarrationText)
    )
) {
    val narrationText by viewModel.narrationText.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val selectedVoiceId by viewModel.selectedVoiceId.collectAsState()
    val speed by viewModel.speed.collectAsState()
    val pitch by viewModel.pitch.collectAsState()
    val syncDuration by viewModel.syncDurationWithScene.collectAsState()

    val generationState by viewModel.generationState.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()
    val userCredits by viewModel.userCredits.collectAsState()
    val projectAudios by viewModel.projectAudios.collectAsState()

    val isPlaying by viewModel.isPlaying.collectAsState()
    val playingAudioId by viewModel.currentlyPlayingId.collectAsState()
    val selectedAudioForTrim by viewModel.selectedAudioForTrim.collectAsState()
    val trimRange by viewModel.trimRange.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) } // 0: توليد صوتي AI, 1: رفع تسجيل, 2: مكتبة أصوات المشروع
    val snackbarHostState = remember { SnackbarHostState() }

    // State for local audio upload dialog/inputs
    var uploadTitle by remember { mutableStateOf("") }
    var speakerName by remember { mutableStateOf("") }
    var isRecitationChecked by remember { mutableStateOf(false) }

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
                            text = if (sceneId != null) "الاستوديو الصوتي للمشهد" else "الاستوديو الصوتي للمشروع",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "تعليق فصيح • تسجيلات • تمييز شرعي موثق",
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
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$userCredits رصيد",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Selector
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("توليد الذكاء الاصطناعي") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("رفع تسجيل / تلاوة") }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("أصوات المشروع (${projectAudios.size})") }
                )
            }

            when (selectedTabIndex) {
                0 -> AiVoiceGeneratorTab(
                    narrationText = narrationText,
                    onTextChange = { viewModel.onTextChange(it) },
                    selectedLanguage = selectedLanguage,
                    onLanguageChange = { viewModel.onLanguageChange(it) },
                    selectedVoiceId = selectedVoiceId,
                    onVoiceChange = { viewModel.onVoiceChange(it) },
                    availableVoices = viewModel.availableVoices,
                    speed = speed,
                    onSpeedChange = { viewModel.onSpeedChange(it) },
                    pitch = pitch,
                    onPitchChange = { viewModel.onPitchChange(it) },
                    syncDuration = syncDuration,
                    onSyncDurationChange = { viewModel.onSyncDurationChange(it) },
                    isSceneContext = sceneId != null,
                    generationState = generationState,
                    onGenerate = { viewModel.generateVoiceover() }
                )
                1 -> AudioUploadTab(
                    uploadTitle = uploadTitle,
                    onTitleChange = { uploadTitle = it },
                    speakerName = speakerName,
                    onSpeakerNameChange = { speakerName = it },
                    isRecitation = isRecitationChecked,
                    onRecitationChange = { isRecitationChecked = it },
                    uploadState = uploadState,
                    onSimulateUpload = {
                        val mockBytes = ByteArray(1024 * 50) // Mock audio bytes
                        viewModel.uploadUserRecording(
                            title = uploadTitle.ifBlank { "تسجيل صوتي خاص" },
                            fileName = if (isRecitationChecked) "recitation.mp3" else "voice_record.mp3",
                            fileBytes = mockBytes,
                            mimeType = "audio/mpeg",
                            durationMs = 8000L,
                            speakerName = speakerName.ifBlank { null },
                            isRecitation = isRecitationChecked
                        )
                    }
                )
                2 -> ProjectAudioListTab(
                    audios = projectAudios,
                    isPlaying = isPlaying,
                    playingAudioId = playingAudioId,
                    onPlayPause = { viewModel.playAudio(it) },
                    onTrim = { viewModel.openTrimDialog(it) },
                    onAttachToScene = { viewModel.attachToScene(it) },
                    onDelete = { viewModel.deleteAudio(it) },
                    isSceneContext = sceneId != null
                )
            }
        }
    }

    // Audio Trim Modal Dialog
    if (selectedAudioForTrim != null) {
        val audio = selectedAudioForTrim!!
        AlertDialog(
            onDismissRequest = { viewModel.closeTrimDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("قص وضبط مدة الصوت", style = MaterialTheme.typography.titleMedium)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = audio.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "المدة الكلية الأصلية: ${(audio.originalDurationMs / 1000f)} ثانية",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "تحديد البداية والنهاية:",
                        style = MaterialTheme.typography.labelMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    RangeSlider(
                        value = trimRange,
                        onValueChange = { viewModel.onTrimRangeChanged(it) },
                        valueRange = 0f..audio.originalDurationMs.toFloat().coerceAtLeast(1000f),
                        steps = 20,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "البداية: ${String.format("%.1f", trimRange.start / 1000f)} ث",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = "المدة بعد القص: ${String.format("%.1f", (trimRange.endInclusive - trimRange.start) / 1000f)} ث",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "النهاية: ${String.format("%.1f", trimRange.endInclusive / 1000f)} ث",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "سيتم تلقائيًا مزامنة وتحديث زمن عرض المشهد ليطابق المدة المقصوصة.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.applyTrim() }) {
                    Text("حفظ القص والتطبيق")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeTrimDialog() }) {
                    Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun AiVoiceGeneratorTab(
    narrationText: String,
    onTextChange: (String) -> Unit,
    selectedLanguage: AudioLanguage,
    onLanguageChange: (AudioLanguage) -> Unit,
    selectedVoiceId: String,
    onVoiceChange: (String) -> Unit,
    availableVoices: List<VoiceOption>,
    speed: Float,
    onSpeedChange: (Float) -> Unit,
    pitch: Float,
    onPitchChange: (Float) -> Unit,
    syncDuration: Boolean,
    onSyncDurationChange: (Boolean) -> Unit,
    isSceneContext: Boolean,
    generationState: AudioGenerationUiState,
    onGenerate: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Islamic Safeguard Warning Banner
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "الضابط الشرعي: الصوت الاصطناعي مخصص للتعليق السردي والشرح والتوضيح فقط، ويُمنع استخدامه كتلاوة للقرآن الكريم أو انتحال أصوات العلماء والقراء دون إذن.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Narration Text Input
        item {
            Text(
                text = "نص التعليق الصوتي المراد توليده:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = narrationText,
                onValueChange = onTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 110.dp),
                placeholder = { Text("اكتب النص باللغة العربية مع التشكيل لضمان أعلى دقة في النطق الفصيح...") },
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Voice Language & Dialect
        item {
            Text(
                text = "اللهجة والنمط اللغوي:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AudioLanguage.values().forEach { lang ->
                    FilterChip(
                        selected = selectedLanguage == lang,
                        onClick = { onLanguageChange(lang) },
                        label = { Text(lang.displayName) },
                        leadingIcon = if (selectedLanguage == lang) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
        }

        // Voice Personality Selection
        item {
            Text(
                text = "اختيار المعلق الصوتي (Voices):",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                availableVoices.forEach { voice ->
                    val isSelected = selectedVoiceId == voice.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onVoiceChange(voice.id) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (voice.gender == AudioVoiceGender.MALE) Icons.Default.Face else Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = voice.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = voice.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = { onVoiceChange(voice.id) }
                            )
                        }
                    }
                }
            }
        }

        // Speed & Pitch Sliders
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "سرعة الإلقاء: ${speed}x",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = speed,
                        onValueChange = onSpeedChange,
                        valueRange = 0.7f..1.4f,
                        steps = 6,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "طبقة النبرة والوقار (Pitch): ${pitch}x",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = pitch,
                        onValueChange = onPitchChange,
                        valueRange = 0.8f..1.2f,
                        steps = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Sync with Scene Duration Checkbox (if in scene context)
        if (isSceneContext) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSyncDurationChange(!syncDuration) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = syncDuration,
                        onCheckedChange = { onSyncDurationChange(it) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "مزامنة مدة عرض المشهد تلقائيًا لتطابق طول التسجيل الصوتي المولد",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Generate Action Button
        item {
            val isGenerating = generationState is AudioGenerationUiState.Generating

            Button(
                onClick = onGenerate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isGenerating && narrationText.isNotBlank()
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("جارٍ توليد الصوت الفصيح...")
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("توليد التعليق الصوتي (1 رصيد)", fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun AudioUploadTab(
    uploadTitle: String,
    onTitleChange: (String) -> Unit,
    speakerName: String,
    onSpeakerNameChange: (String) -> Unit,
    isRecitation: Boolean,
    onRecitationChange: (Boolean) -> Unit,
    uploadState: AudioUploadUiState,
    onSimulateUpload: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "يمكنك رفع تسجيل صوتي مباشر أو تلاوة قرآنية موثقة. الصيغ المدعومة: MP3, WAV, M4A, AAC بحجم يصل حتى 50 ميغابايت.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        OutlinedTextField(
            value = uploadTitle,
            onValueChange = onTitleChange,
            label = { Text("عنوان التسجيل الصوتي") },
            placeholder = { Text("مثال: تسجيل المقطع الأول بصوت فلان") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = speakerName,
            onValueChange = onSpeakerNameChange,
            label = { Text("اسم المتحدث أو القارئ (للتوثيق والنسب)") },
            placeholder = { Text("مثال: الشيخ مشاري العفاسي / اسم المستخدم") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRecitationChange(!isRecitation) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isRecitation,
                    onCheckedChange = { onRecitationChange(it) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "تصنيف كـ (تلاوة قرآنية موثقة)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "يتم تمييز التلاوات القرآنية وتوثيق القارئ ورواية التلاوة لمنع الخلط مع الأصوات المولدة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        val isUploading = uploadState is AudioUploadUiState.Uploading

        Button(
            onClick = onSimulateUpload,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !isUploading
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("جارٍ رفع وتوثيق الملف الصوتي...")
            } else {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("اختيار ملف ورفعه إلى الاستوديو", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProjectAudioListTab(
    audios: List<AudioItem>,
    isPlaying: Boolean,
    playingAudioId: String?,
    onPlayPause: (AudioItem) -> Unit,
    onTrim: (AudioItem) -> Unit,
    onAttachToScene: (AudioItem) -> Unit,
    onDelete: (AudioItem) -> Unit,
    isSceneContext: Boolean
) {
    if (audios.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "لا توجد ملفات صوتية مولدة أو مرفوعة للمشروع بعد",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(audios, key = { it.id }) { audio ->
                AudioItemCard(
                    audio = audio,
                    isPlaying = isPlaying && playingAudioId == audio.id,
                    onPlayPause = { onPlayPause(audio) },
                    onTrim = { onTrim(audio) },
                    onAttachToScene = { onAttachToScene(audio) },
                    onDelete = { onDelete(audio) },
                    isSceneContext = isSceneContext
                )
            }
        }
    }
}

@Composable
fun AudioItemCard(
    audio: AudioItem,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onTrim: () -> Unit,
    onAttachToScene: () -> Unit,
    onDelete: () -> Unit,
    isSceneContext: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause Button
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "إيقاف" else "تشغيل",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = audio.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${String.format("%.1f", audio.trimmedDurationMs / 1000f)} ثانية",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• ${audio.voiceName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Type Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (audio.isAiGenerated) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = if (audio.isAiGenerated) "AI-VOICE" else if (audio.sourceType == AudioSourceType.QURAN_RECITATION) "تلاوة" else "تسجيل",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (audio.isAiGenerated) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (audio.textContent.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = audio.textContent,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Legal & Disclaimer text
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = audio.licenseNotice,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(6.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onTrim, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "قص البداية والنهاية", modifier = Modifier.size(18.dp))
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = androidx.compose.ui.res.stringResource(com.siraj.app.R.string.delete), tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }

                if (isSceneContext) {
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledTonalButton(
                        onClick = onAttachToScene,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ربط بالمشهد", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
