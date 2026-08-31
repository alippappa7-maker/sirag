package com.siraj.app.features.project.presentation.soundtrack

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.features.project.domain.models.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundtrackLibraryScreen(
    projectId: String,
    sceneId: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: SoundtrackLibraryViewModel = viewModel(
        factory = SoundtrackLibraryViewModelFactory(projectId, sceneId)
    )
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val hideMusicOnly by viewModel.hideMusicOnly.collectAsState()
    val soundtracks by viewModel.soundtracks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val isPlaying by viewModel.isPlaying.collectAsState()
    val playingId by viewModel.currentlyPlayingId.collectAsState()
    val selectedTrackForConfig by viewModel.selectedTrackForConfig.collectAsState()
    val currentSceneTrack by viewModel.currentSceneTrackConfig.collectAsState()

    val volume by viewModel.trackVolume.collectAsState()
    val isLooping by viewModel.isLooping.collectAsState()
    val isFadeIn by viewModel.isFadeIn.collectAsState()
    val isFadeOut by viewModel.isFadeOut.collectAsState()
    val trimRange by viewModel.trimRange.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

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
                            text = if (sceneId != null) "مكتبة المؤثرات والخلفيات الصوتية" else "مكتبة الصوتيات والمؤثرات",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "مرخصة • طبيعية وفوكالز • تحكم كامل بالطبقات",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = androidx.compose.ui.res.stringResource(com.siraj.app.R.string.back))
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
            // Search Input Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("ابحث عن مؤثر، صوت طبيعة، صوت كتابة، مطر، نشيد...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = androidx.compose.ui.res.stringResource(com.siraj.app.R.string.clear))
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Preferences & Islamic Filter Bar (Hide Music Toggle)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (hideMusicOnly) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { viewModel.onToggleHideMusic(!hideMusicOnly) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (hideMusicOnly) Icons.Default.Check else Icons.Default.Clear,
                            contentDescription = null,
                            tint = if (hideMusicOnly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "إخفاء الموسيقى (أصوات طبيعية وفوكالز فقط)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (hideMusicOnly) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Categories Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { viewModel.onCategorySelect(null) },
                        label = { Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.all)) }
                    )
                }
                items(SoundtrackCategory.values()) { cat ->
                    if (!hideMusicOnly || cat != SoundtrackCategory.BACKGROUND_MUSIC) {
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { viewModel.onCategorySelect(cat) },
                            label = { Text(cat.displayName) }
                        )
                    }
                }
            }

            // Current Scene Active Track Card (if any attached)
            if (sceneId != null && currentSceneTrack != null) {
                val track = currentSceneTrack!!
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "الصوت النشط بالمشهد: ${track.soundTitle}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "مستوى الصوت: ${(track.volume * 100).toInt()}% • تكرار: ${if (track.loop) "نعم" else "لا"} • تلاشي: ${if (track.fadeIn) "نعم" else "لا"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { viewModel.removeTrackFromScene() }) {
                            Icon(Icons.Default.Delete, contentDescription = "إزالة الصوت", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // Soundtrack List
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (soundtracks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لم يتم العثور على مؤثرات صوتية تطابق الفلتر الحالي",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(soundtracks, key = { it.id }) { item ->
                        SoundtrackItemCard(
                            item = item,
                            isPlaying = isPlaying && playingId == item.id,
                            onPlayToggle = { viewModel.togglePlaySoundtrack(item) },
                            onSelectForConfig = { viewModel.openConfigureDialog(item) },
                            isSceneContext = sceneId != null
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    // Modal Sheet / Dialog for Audio Layer Configuration & Trimming
    if (selectedTrackForConfig != null) {
        val track = selectedTrackForConfig ?: return
        AlertDialog(
            onDismissRequest = { viewModel.closeConfigureDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ضبط طبقة الصوت للمشهد", style = MaterialTheme.typography.titleMedium)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "التصنيف: ${track.category.displayName} • المدة: ${(track.durationMs / 1000f)} ث",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // Volume Slider
                    Text(
                        text = "مستوى صوت الخلفية: ${(volume * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = volume,
                        onValueChange = { viewModel.onVolumeChange(it) },
                        valueRange = 0.05f..1.0f,
                        steps = 19,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Looping & Fade toggles
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onLoopToggle(!isLooping) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = isLooping, onCheckedChange = { viewModel.onLoopToggle(it) })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تكرار الصوت طوال المشهد (Loop)", style = MaterialTheme.typography.bodySmall)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onFadeInToggle(!isFadeIn) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = isFadeIn, onCheckedChange = { viewModel.onFadeInToggle(it) })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تلاشي الدخول التدريجي (Fade In)", style = MaterialTheme.typography.bodySmall)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onFadeOutToggle(!isFadeOut) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = isFadeOut, onCheckedChange = { viewModel.onFadeOutToggle(it) })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تلاشي الخروج عند نهاية المشهد (Fade Out)", style = MaterialTheme.typography.bodySmall)
                    }

                    // License Card
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "الترخيص: ${track.licenseType.displayName}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (track.attributionText.isNotBlank()) {
                                Text(
                                    text = "نص النسبة: ${track.attributionText}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "القيود: ${track.usageRestrictions}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.applyTrackToScene() }) {
                    Text("ربط بالمشهد")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeConfigureDialog() }) {
                    Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun SoundtrackItemCard(
    item: SoundtrackItem,
    isPlaying: Boolean,
    onPlayToggle: () -> Unit,
    onSelectForConfig: () -> Unit,
    isSceneContext: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause Icon Button
                IconButton(
                    onClick = onPlayToggle,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "إيقاف" else "معاينة",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.category.displayName} • ${(item.durationMs / 1000f)} ثانية",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Nature / SFX / Music Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (item.isMusic) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = if (item.isMusic) "موسيقى مرخصة" else if (item.category == SoundtrackCategory.NATURE_AMBIENCE) "طبيعة" else if (item.category == SoundtrackCategory.NASHEED_VOCAL) "فوكال بشري" else "مؤثر SFX",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isMusic) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الرخصة: ${item.licenseType.displayName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )

                if (isSceneContext) {
                    FilledTonalButton(
                        onClick = onSelectForConfig,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة للمشهد", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
