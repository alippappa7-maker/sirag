package com.siraj.app.features.quran.presentation

import android.app.Application
import android.net.Uri
import androidx.compose.animation.Crossfade
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.siraj.app.core.ui.components.SirajGlowContainer
import com.siraj.app.core.ui.components.SirajTechCard
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.quran.Ayah
import com.siraj.app.domain.models.quran.QuranReaderSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    onNavigateToSurah: (Int, String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: QuranViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return QuranViewModel(context.applicationContext as Application) as T
            }
        }
    )
    val surahsState by viewModel.surahs.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("سور القرآن الكريم") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = androidx.compose.ui.res.stringResource(com.siraj.app.R.string.back)) }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("بحث عن سورة...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = androidx.compose.ui.res.stringResource(com.siraj.app.R.string.search)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Crossfade(targetState = surahsState, label = "SurahsStateCrossfade") { state ->
                when (state) {
                    is Resource.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    is Resource.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.message, color = MaterialTheme.colorScheme.error) }
                    is Resource.Success -> {
                        val filteredSurahs = state.data.filter { it.nameArabic.contains(searchQuery) }
                        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filteredSurahs) { surah ->
                                SirajTechCard(
                                    isActive = false,
                                    onClick = { onNavigateToSurah(surah.chapterNumber, surah.nameArabic) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(surah.nameArabic, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                            Text("${surah.revelationPlace} • ${surah.versesCount} آيات • ${surah.nameTranslated}", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Text(
                                            surah.chapterNumber.toString(),
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "المصدر: Quran Foundation - البيانات موثقة",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahScreen(
    surahId: Int,
    surahName: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: QuranReaderViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return QuranReaderViewModel(context.applicationContext as Application, surahId) as T
            }
        }
    )
    val ayahsState by viewModel.ayahsWithBookmarks.collectAsState(initial = Resource.Loading)
    val settings by viewModel.settings.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }

    // Audio Player setup
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var currentlyPlayingAyah by remember { mutableStateOf<Ayah?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var repeatMode by remember { mutableStateOf(Player.REPEAT_MODE_OFF) }

    DisposableEffect(context) {
        val player = ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                    isPlaying = isPlayingNow
                }
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        if (repeatMode == Player.REPEAT_MODE_OFF) {
                            currentlyPlayingAyah = null
                        }
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

    LaunchedEffect(settings.playbackSpeed) {
        exoPlayer?.setPlaybackSpeed(settings.playbackSpeed)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("سورة $surahName") },
                navigationIcon = {
                    IconButton(onClick = {
                        exoPlayer?.stop()
                        onNavigateBack()
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = androidx.compose.ui.res.stringResource(com.siraj.app.R.string.back)) }
                },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "إعدادات القراءة")
                    }
                }
            )
        },
        bottomBar = {
            if (currentlyPlayingAyah != null) {
                val ayah = currentlyPlayingAyah!!
                QuranAudioPlayerBottomBar(
                    ayah = ayah,
                    isPlaying = isPlaying,
                    repeatMode = repeatMode,
                    speed = settings.playbackSpeed,
                    onPlayPause = {
                        if (isPlaying) exoPlayer?.pause() else exoPlayer?.play()
                    },
                    onStop = {
                        exoPlayer?.stop()
                        currentlyPlayingAyah = null
                    },
                    onToggleRepeat = {
                        repeatMode = if (repeatMode == Player.REPEAT_MODE_OFF) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
                        exoPlayer?.repeatMode = repeatMode
                    },
                    onChangeSpeed = {
                        val nextSpeed = when (settings.playbackSpeed) {
                            1.0f -> 1.5f
                            1.5f -> 2.0f
                            2.0f -> 0.5f
                            else -> 1.0f
                        }
                        viewModel.updatePlaybackSpeed(nextSpeed)
                    }
                )
            }
        },
        containerColor = if (settings.isNightMode) Color(0xFF121212) else MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Crossfade(targetState = ayahsState, label = "AyahsCrossfade") { state ->
                when (state) {
                    is Resource.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    is Resource.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.message, color = MaterialTheme.colorScheme.error) }
                    is Resource.Success -> {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.data) { ayah ->
                                AyahCard(
                                    ayah = ayah,
                                    settings = settings,
                                    isPlaying = currentlyPlayingAyah?.verseKey == ayah.verseKey && isPlaying,
                                    onPlayClick = {
                                        if (currentlyPlayingAyah?.verseKey == ayah.verseKey) {
                                            if (isPlaying) exoPlayer?.pause() else exoPlayer?.play()
                                        } else {
                                            currentlyPlayingAyah = ayah
                                            ayah.audio?.let { audioData ->
                                                exoPlayer?.setMediaItem(MediaItem.fromUri(Uri.parse(audioData.url)))
                                                exoPlayer?.prepare()
                                                exoPlayer?.play()
                                            }
                                        }
                                    },
                                    onToggleBookmark = { viewModel.toggleBookmark(ayah.verseKey, ayah.verseNumber, ayah.isBookmarked) },
                                    onSaveNote = { note -> viewModel.saveNote(ayah.verseKey, ayah.verseNumber, note) },
                                    onShare = { /* TODO System Share */ },
                                    onDownload = { /* TODO Download Logic */ }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSettingsDialog) {
        QuranSettingsDialog(
            settings = settings,
            onDismiss = { showSettingsDialog = false },
            onUpdateFontSize = viewModel::updateFontSize,
            onToggleNightMode = viewModel::toggleNightMode,
            onChangeReciter = viewModel::changeReciter
        )
    }
}

@Composable
fun AyahCard(
    ayah: Ayah,
    settings: QuranReaderSettings,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    onSaveNote: (String) -> Unit,
    onShare: () -> Unit,
    onDownload: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showNoteDialog by remember { mutableStateOf(false) }

    val textColor = if (settings.isNightMode) Color.White else MaterialTheme.colorScheme.onSurface
    val containerColor = if (settings.isNightMode) Color(0xFF1E1E1E) else MaterialTheme.colorScheme.surface

    SirajTechCard(
        isActive = isPlaying,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text("الآية ${ayah.verseNumber}") },
                    colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onShare) {
                        Icon(Icons.Default.Share, contentDescription = androidx.compose.ui.res.stringResource(com.siraj.app.R.string.share), tint = textColor)
                    }
                    if (ayah.audio != null) {
                        IconButton(onClick = onDownload) {
                            Icon(Icons.Default.Download, contentDescription = "تنزيل التلاوة", tint = textColor)
                        }
                        IconButton(onClick = onPlayClick) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "تلاوة",
                                tint = if (isPlaying) MaterialTheme.colorScheme.primary else textColor
                            )
                        }
                    }
                    IconButton(onClick = { showNoteDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "ملاحظات", tint = textColor)
                    }
                    IconButton(onClick = onToggleBookmark) {
                        Icon(
                            if (ayah.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "علامة مرجعية",
                            tint = if (ayah.isBookmarked) MaterialTheme.colorScheme.primary else textColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Arabic Text (Cannot be modified, from source)
            Text(
                text = ayah.textUthmani,
                style = MaterialTheme.typography.headlineMedium.copy(
                    lineHeight = (settings.fontSize * 1.8f).sp,
                    fontSize = settings.fontSize.sp
                ),
                textAlign = TextAlign.End,
                color = textColor,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tabs for Translation, Tafsir, Note
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("الترجمة") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("التفسير") })
                if (ayah.note != null) {
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("ملاحظاتي") })
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        Column {
                            Text(
                                text = ayah.translation?.text ?: "لا توجد ترجمة متاحة.",
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = (settings.fontSize * 0.7f).sp),
                                color = textColor
                            )
                            if (ayah.translation != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "المصدر: ${ayah.translation.resourceName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textColor.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                    1 -> {
                        Column {
                            Text(
                                text = ayah.tafsir?.text ?: "لا يوجد تفسير متاح.",
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = (settings.fontSize * 0.7f).sp),
                                color = textColor
                            )
                            if (ayah.tafsir != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "المصدر: ${ayah.tafsir.resourceName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textColor.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                    2 -> {
                        Text(
                            text = ayah.note ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    if (showNoteDialog) {
        var noteText by remember { mutableStateOf(ayah.note ?: "") }
        Dialog(onDismissRequest = { showNoteDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("إضافة ملاحظة للآية ${ayah.verseKey}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("اكتب ملاحظاتك أو خواطرك هنا...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { showNoteDialog = false }) { Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.cancel)) }
                        Button(onClick = {
                            onSaveNote(noteText)
                            showNoteDialog = false
                        }) {
                            Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.save))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuranSettingsDialog(
    settings: QuranReaderSettings,
    onDismiss: () -> Unit,
    onUpdateFontSize: (Float) -> Unit,
    onToggleNightMode: (Boolean) -> Unit,
    onChangeReciter: (Int) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("إعدادات القراءة", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("حجم الخط (${settings.fontSize.toInt()})")
                    Slider(
                        value = settings.fontSize,
                        onValueChange = onUpdateFontSize,
                        valueRange = 16f..48f,
                        modifier = Modifier.weight(1f).padding(start = 16.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("الوضع الليلي")
                    Switch(checked = settings.isNightMode, onCheckedChange = onToggleNightMode)
                }

                Text("القارئ (للتلاوة)", fontWeight = FontWeight.Medium)
                // Mock reciter selection for UI
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = settings.selectedReciterId == 1,
                        onClick = { onChangeReciter(1) },
                        label = { Text("العفاسي") }
                    )
                    FilterChip(
                        selected = settings.selectedReciterId == 2,
                        onClick = { onChangeReciter(2) },
                        label = { Text("الحصري") }
                    )
                }

                Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.close))
                }
            }
        }
    }
}

@Composable
fun QuranAudioPlayerBottomBar(
    ayah: Ayah,
    isPlaying: Boolean,
    repeatMode: Int,
    speed: Float,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onToggleRepeat: () -> Unit,
    onChangeSpeed: () -> Unit
) {
    SirajGlowContainer(
        isActive = isPlaying,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("جاري التلاوة - الآية ${ayah.verseNumber}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("القارئ: ${ayah.audio?.reciterName ?: "غير معروف"}", style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = onStop) {
                        Icon(Icons.Default.Close, contentDescription = "إيقاف والتسكير")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onChangeSpeed) {
                        Text("${speed}x")
                    }
                    IconButton(onClick = onToggleRepeat) {
                        Icon(
                            if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                            contentDescription = "تكرار",
                            tint = if (repeatMode == Player.REPEAT_MODE_ONE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FloatingActionButton(
                        onClick = onPlayPause,
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play"
                        )
                    }
                }
            }
        }
    }
}
