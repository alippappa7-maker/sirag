package com.siraj.app.features.audio.presentation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.core.ui.components.SirajTechCard
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.audio.AudioSortOption
import com.siraj.app.domain.models.audio.AudioTrack
import com.siraj.app.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioScreen() {
    val viewModel: AudioViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val spacing = LocalSpacing.current

    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("المكتبة الصوتية", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                actions = {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "فرز")
                    }
                    DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("أحدث الإضافات") },
                            onClick = {
                                viewModel.onSortOptionChanged(AudioSortOption.NEWEST)
                                showSortMenu = false
                            },
                            trailingIcon = { if (state.sortOption == AudioSortOption.NEWEST) Icon(Icons.Default.Check, null) },
                        )
                        DropdownMenuItem(
                            text = { Text("الأكثر استماعًا") },
                            onClick = {
                                viewModel.onSortOptionChanged(AudioSortOption.MOST_LISTENED)
                                showSortMenu = false
                            },
                            trailingIcon = { if (state.sortOption == AudioSortOption.MOST_LISTENED) Icon(Icons.Default.Check, null) },
                        )
                        DropdownMenuItem(
                            text = { Text("أبجديًا") },
                            onClick = {
                                viewModel.onSortOptionChanged(AudioSortOption.ALPHABETICAL)
                                showSortMenu = false
                            },
                            trailingIcon = { if (state.sortOption == AudioSortOption.ALPHABETICAL) Icon(Icons.Default.Check, null) },
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.medium, vertical = spacing.small),
                placeholder = { Text("ابحث عن تلاوة، درس، أو محاضرة...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription =
                                    androidx.compose.ui.res
                                        .stringResource(com.siraj.app.R.string.clear),
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    ),
            )

            // Categories
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = spacing.medium, vertical = spacing.small),
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
            ) {
                items(viewModel.categories) { (id, name) ->
                    FilterChip(
                        selected = state.selectedCategory == id,
                        onClick = { viewModel.onCategorySelected(id) },
                        label = { Text(name) },
                        leadingIcon = {
                            if (id == "favorites") Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp))
                            if (id == "downloads") Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors =
                            FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                                selectedLabelColor = MaterialTheme.colorScheme.onTertiary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onTertiary,
                            ),
                    )
                }
            }

            // Track List
            Box(modifier = Modifier.weight(1f)) {
                Crossfade(targetState = state.tracksResource, label = "tracks_crossfade") { res ->
                    when (res) {
                        is Resource.Loading ->
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.tertiary,
                            )
                        is Resource.Error ->
                            Text(
                                res.message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.align(Alignment.Center),
                            )
                        is Resource.Success -> {
                            val tracks = res.data
                            if (tracks.isEmpty()) {
                                Column(
                                    modifier = Modifier.align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Icon(
                                        Icons.Default.LibraryMusic,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.surfaceVariant,
                                    )
                                    Spacer(modifier = Modifier.height(spacing.medium))
                                    Text(
                                        "لا توجد ملفات صوتية مطابقة.",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            } else {
                                LazyColumn(
                                    contentPadding =
                                        PaddingValues(
                                            start = spacing.medium,
                                            end = spacing.medium,
                                            top = spacing.medium,
                                            bottom = 120.dp,
                                        ),
                                    verticalArrangement = Arrangement.spacedBy(spacing.small),
                                    modifier = Modifier.fillMaxSize(),
                                ) {
                                    items(tracks, key = { it.id }) { track ->
                                        AudioTrackCard(
                                            track = track,
                                            isActive = track.listenProgressSeconds > 0, // Visual feedback if started
                                            onPlay = { viewModel.playTrack(track) },
                                            onToggleFavorite = { viewModel.toggleFavorite(track.id) },
                                            onReport = { viewModel.reportTrack(track.id) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AudioTrackCard(
    track: AudioTrack,
    isActive: Boolean,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onReport: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    SirajTechCard(
        isActive = isActive,
        onClick = onPlay,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Play Icon or Cover Placeholder
                Box(
                    modifier =
                        Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isActive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isActive) Icons.Default.PlayArrow else Icons.Default.PlayArrow,
                        contentDescription =
                            androidx.compose.ui.res
                                .stringResource(com.siraj.app.R.string.play),
                        tint = if (isActive) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = track.speaker,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            formatDuration(track.durationSeconds),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Icon(
                            Icons.Default.Headphones,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${track.playCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Actions
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "المزيد", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(if (track.isFavorite) "إزالة من المفضلة" else "إضافة للمفضلة") },
                        onClick = {
                            onToggleFavorite()
                            showMenu = false
                        },
                        leadingIcon = { Icon(if (track.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder, null) },
                    )
                    DropdownMenuItem(
                        text = { Text("إضافة إلى قائمة التشغيل") },
                        onClick = { showMenu = false },
                        leadingIcon = { Icon(Icons.Default.PlaylistAdd, null) },
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                androidx.compose.ui.res
                                    .stringResource(com.siraj.app.R.string.share),
                            )
                        },
                        onClick = { showMenu = false },
                        leadingIcon = { Icon(Icons.Default.Share, null) },
                    )
                    DropdownMenuItem(
                        text = { Text("إبلاغ عن المحتوى") },
                        onClick = {
                            onReport()
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Report, null) },
                    )
                }
            }

            // Progress Bar if started
            if (track.listenProgressSeconds > 0 && track.durationSeconds > 0) {
                val progress = track.listenProgressSeconds.toFloat() / track.durationSeconds.toFloat()
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = MaterialTheme.colorScheme.tertiary,
                    trackColor = Color.Transparent,
                )
            }

            // Source & Verification Footer
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Verified,
                        contentDescription = "موثق",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "المصدر: ${track.source}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    track.rights.licenseType,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun formatDuration(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) {
        String.format("%d:%02d:%02d", h, m, s)
    } else {
        String.format("%02d:%02d", m, s)
    }
}
