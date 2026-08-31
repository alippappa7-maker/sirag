package com.siraj.app.features.history.presentation

import android.app.Application
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.domain.models.history.*
import java.text.SimpleDateFormat
import java.util.*
import com.siraj.app.ui.theme.statusColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityHistoryScreen(
    onNavigateBack: () -> Unit,
    onResumeVideo: (String) -> Unit = {},
    onResumeAudio: (String) -> Unit = {},
    onResumeQuran: (String) -> Unit = {},
    onResumeFlash: (String) -> Unit = {},
    viewModel: ActivityHistoryViewModel =
        viewModel(
            factory = ActivityHistoryViewModelFactory(LocalContext.current.applicationContext as Application),
        ),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showClearAllConfirmDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<UserActivityItem?>(null) }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "سجل النشاط والمتابعة",
                        fontWeight = FontWeight.Bold,
                    )
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
                actions = {
                    IconButton(
                        onClick = { viewModel.syncNow() },
                        enabled = !uiState.isSyncing,
                    ) {
                        if (uiState.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(Icons.Default.Sync, contentDescription = "مزامنة")
                        }
                    }
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "إعدادات السجل والخصوصية")
                    }
                    IconButton(
                        onClick = { showClearAllConfirmDialog = true },
                        enabled = uiState.items.isNotEmpty(),
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "مسح السجل")
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            // Privacy Alert Banner if history is paused
            if (!uiState.preferences.isHistoryEnabled) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.PauseCircleFilled,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "حفظ السجل متوقف مؤقتاً",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                            Text(
                                text = "لن يتم تسجيل مواضع المشاهدة والاستماع الجديدة حتى تعيد التفعيل.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                        TextButton(
                            onClick = { viewModel.toggleHistoryRecording(true) },
                        ) {
                            Text("تفعيل", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Search Filter Box
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("بحث في السجل والمتابعة...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح البحث")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )

            // Tabs Selector
            LazyRow(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(ActivityTab.values()) { tab ->
                    FilterChip(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        label = { Text(tab.title) },
                        leadingIcon = {
                            when (tab) {
                                ActivityTab.ALL -> Icon(Icons.Default.History, null, modifier = Modifier.size(16.dp))
                                ActivityTab.VIDEO -> Icon(Icons.Default.VideoLibrary, null, modifier = Modifier.size(16.dp))
                                ActivityTab.AUDIO -> Icon(Icons.Default.Headphones, null, modifier = Modifier.size(16.dp))
                                ActivityTab.WATCH_LATER -> Icon(Icons.Default.WatchLater, null, modifier = Modifier.size(16.dp))
                                ActivityTab.DOWNLOADED -> Icon(Icons.Default.DownloadDone, null, modifier = Modifier.size(16.dp))
                                ActivityTab.COMPLETED -> Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                            }
                        },
                    )
                }
            }

            val filteredItems =
                remember(uiState.items, uiState.searchQuery) {
                    if (uiState.searchQuery.isBlank()) {
                        uiState.items
                    } else {
                        uiState.items.filter {
                            it.title.contains(uiState.searchQuery, ignoreCase = true) ||
                                (it.subtitle?.contains(uiState.searchQuery, ignoreCase = true) == true)
                        }
                    }
                }

            if (uiState.isLoading && uiState.items.isEmpty()) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (filteredItems.isEmpty()) {
                EmptyHistoryView(tab = uiState.selectedTab, isSearch = uiState.searchQuery.isNotBlank())
            } else {
                LazyColumn(
                    state = listState,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Quick Resume Banner (if available and tab is ALL or matching)
                    if (uiState.selectedTab == ActivityTab.ALL && uiState.recentResumeItem != null && uiState.searchQuery.isBlank()) {
                        item {
                            QuickResumeCard(
                                item = uiState.recentResumeItem!!,
                                onResume = {
                                    handleResume(
                                        item = uiState.recentResumeItem!!,
                                        onResumeVideo = onResumeVideo,
                                        onResumeAudio = onResumeAudio,
                                        onResumeQuran = onResumeQuran,
                                        onResumeFlash = onResumeFlash,
                                    )
                                },
                            )
                        }
                    }

                    items(filteredItems, key = { it.id }) { item ->
                        ActivityHistoryItemCard(
                            item = item,
                            onResume = {
                                handleResume(
                                    item = item,
                                    onResumeVideo = onResumeVideo,
                                    onResumeAudio = onResumeAudio,
                                    onResumeQuran = onResumeQuran,
                                    onResumeFlash = onResumeFlash,
                                )
                            },
                            onToggleWatchLater = { viewModel.toggleWatchLater(item) },
                            onToggleDownloaded = { viewModel.toggleDownloaded(item) },
                            onDelete = { itemToDelete = item },
                        )
                    }

                    // Load More trigger / button
                    if (uiState.hasMore) {
                        item {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else {
                                    OutlinedButton(
                                        onClick = { viewModel.loadMore() },
                                    ) {
                                        Text("تحميل المزيد من السجل")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Clear All Confirmation Dialog
    if (showClearAllConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirmDialog = false },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("مسح سجل النشاط كاملاً") },
            text = {
                Text("هل أنت متأكد من مسح جميع عناصر سجل المشاهدة والاستماع والتنزيلات؟ لا يمكن التراجع عن هذا الإجراء.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearAllConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) {
                    Text("نعم، مسح الكل")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirmDialog = false }) {
                    Text(
                        androidx.compose.ui.res
                            .stringResource(com.siraj.app.R.string.cancel),
                    )
                }
            },
        )
    }

    // Delete Single Item Confirmation Dialog
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("حذف من السجل") },
            text = { Text("هل تريد إزالة \"${item.title}\" من سجل النشاط؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteItem(item)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(
                        androidx.compose.ui.res
                            .stringResource(com.siraj.app.R.string.delete),
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(
                        androidx.compose.ui.res
                            .stringResource(com.siraj.app.R.string.cancel),
                    )
                }
            },
        )
    }

    // History Preferences Dialog
    if (showSettingsDialog) {
        HistoryPreferencesDialog(
            preferences = uiState.preferences,
            onDismiss = { showSettingsDialog = false },
            onToggleHistory = { viewModel.toggleHistoryRecording(it) },
            onToggleSync = { viewModel.toggleSync(it) },
            onUpdateRetention = { viewModel.updateRetentionPolicy(it) },
            onClearCompleted = { viewModel.clearCompleted() },
            onClearDownloads = { viewModel.clearDownloads() },
        )
    }
}

private fun handleResume(
    item: UserActivityItem,
    onResumeVideo: (String) -> Unit,
    onResumeAudio: (String) -> Unit,
    onResumeQuran: (String) -> Unit,
    onResumeFlash: (String) -> Unit,
) {
    when (item.entityType) {
        ActivityEntityType.VIDEO -> onResumeVideo(item.entityId)
        ActivityEntityType.AUDIO -> onResumeAudio(item.entityId)
        ActivityEntityType.QURAN_RECITATION -> onResumeQuran(item.entityId)
        ActivityEntityType.FLASH -> onResumeFlash(item.entityId)
    }
}

@Composable
fun QuickResumeCard(
    item: UserActivityItem,
    onResume: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onResume() },
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "متابعة من حيث توقفت",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (item.subtitle != null) {
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { (item.progressPercent / 100f).coerceIn(0f, 1f) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "${item.getFormattedPosition()} / ${item.getFormattedDuration()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    )
                    Text(
                        text = item.getRemainingTimeText(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityHistoryItemCard(
    item: UserActivityItem,
    onResume: () -> Unit,
    onToggleWatchLater: () -> Unit,
    onToggleDownloaded: () -> Unit,
    onDelete: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onResume() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Leading Icon/Badge
                Box(
                    modifier =
                        Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(getEntityTypeColor(item.entityType).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = getEntityTypeIcon(item.entityType),
                        contentDescription = null,
                        tint = getEntityTypeColor(item.entityType),
                        modifier = Modifier.size(24.dp),
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        if (item.completed) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(4.dp),
                            ) {
                                Text(
                                    text = "مكتمل",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }

                    if (item.subtitle != null) {
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Text(
                        text = formatRelativeTime(item.lastPlayedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }

                // Actions Menu Box
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription =
                                androidx.compose.ui.res
                                    .stringResource(com.siraj.app.R.string.options),
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("استئناف التشغيل") },
                            leadingIcon = { Icon(Icons.Default.PlayArrow, null) },
                            onClick = {
                                showMenu = false
                                onResume()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(if (item.isWatchLater) "إزالة من المتابعة لاحقاً" else "إضافة للمتابعة لاحقاً") },
                            leadingIcon = {
                                Icon(
                                    if (item.isWatchLater) Icons.Default.BookmarkRemove else Icons.Default.BookmarkAdd,
                                    null,
                                )
                            },
                            onClick = {
                                showMenu = false
                                onToggleWatchLater()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(if (item.isDownloaded) "إزالة من التنزيلات" else "إضافة للتنزيلات") },
                            leadingIcon = {
                                Icon(
                                    if (item.isDownloaded) Icons.Default.FileDownloadOff else Icons.Default.Download,
                                    null,
                                )
                            },
                            onClick = {
                                showMenu = false
                                onToggleDownloaded()
                            },
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("حذف من السجل", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                        )
                    }
                }
            }

            // Progress Bar & Duration
            if (item.durationMs > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { (item.progressPercent / 100f).coerceIn(0f, 1f) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                    color = getEntityTypeColor(item.entityType),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "${item.getFormattedPosition()} من ${item.getFormattedDuration()} (${item.progressPercent.toInt()}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    if (item.isWatchLater) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.WatchLater,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "متابعة لاحقاً",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyHistoryView(
    tab: ActivityTab,
    isSearch: Boolean,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector =
                    if (isSearch) {
                        Icons.Default.SearchOff
                    } else {
                        when (tab) {
                            ActivityTab.WATCH_LATER -> Icons.Default.BookmarkBorder
                            ActivityTab.DOWNLOADED -> Icons.Default.DownloadDone
                            ActivityTab.COMPLETED -> Icons.Default.CheckCircleOutline
                            else -> Icons.Default.History
                        }
                    },
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text =
                    if (isSearch) {
                        "لا توجد نتائج تطابق بحثك"
                    } else {
                        when (tab) {
                            ActivityTab.ALL -> "سجل النشاط فارغ حالياً"
                            ActivityTab.VIDEO -> "لم تشاهد أي فيديوهات أو ومضات بعد"
                            ActivityTab.AUDIO -> "لم تستمع إلى أي دروس أو تلاوات بعد"
                            ActivityTab.WATCH_LATER -> "قائمة المتابعة لاحقاً فارغة"
                            ActivityTab.DOWNLOADED -> "لا توجد عناصر في سجل التنزيلات"
                            ActivityTab.COMPLETED -> "لم تكتمل مشاهدة أي محتوى بعد"
                        }
                    },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isSearch) "جرب البحث بكلمات أخرى" else "المحتوى الذي تشاهده أو تستمع إليه سيظهر هنا لتتمكن من استئنافه بسهولة.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
fun HistoryPreferencesDialog(
    preferences: ActivityHistoryPreferences,
    onDismiss: () -> Unit,
    onToggleHistory: (Boolean) -> Unit,
    onToggleSync: (Boolean) -> Unit,
    onUpdateRetention: (RetentionPolicy) -> Unit,
    onClearCompleted: () -> Unit,
    onClearDownloads: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إعدادات السجل والخصوصية") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Switch: Enable History
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("حفظ سجل النشاط", fontWeight = FontWeight.Bold)
                        Text(
                            "حفظ مواضع التوقف في المقاطع لاستئنافها لاحقاً",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                    Switch(
                        checked = preferences.isHistoryEnabled,
                        onCheckedChange = onToggleHistory,
                    )
                }

                HorizontalDivider()

                // Switch: Enable Sync
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("المزامنة بين الأجهزة", fontWeight = FontWeight.Bold)
                        Text(
                            "مزامنة السجل وقائمة المتابعة عبر السحابة",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                    Switch(
                        checked = preferences.isSyncEnabled,
                        onCheckedChange = onToggleSync,
                    )
                }

                HorizontalDivider()

                // Retention Policy
                Text("مدة الاحتفاظ بالسجل تلقائياً:", fontWeight = FontWeight.Bold)
                RetentionPolicy.values().forEach { policy ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onUpdateRetention(policy) },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = preferences.retentionPolicy == policy,
                            onClick = { onUpdateRetention(policy) },
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(policy.titleArabic, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                HorizontalDivider()

                // Quick Clean Actions
                Text("تنظيف مخصص:", fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            onClearCompleted()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("مسح المكتملة", style = MaterialTheme.typography.bodySmall)
                    }
                    OutlinedButton(
                        onClick = {
                            onClearDownloads()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("مسح التنزيلات", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("تم")
            }
        },
    )
}

private fun getEntityTypeIcon(type: ActivityEntityType): ImageVector =
    when (type) {
        ActivityEntityType.VIDEO -> Icons.Default.PlayCircle
        ActivityEntityType.AUDIO -> Icons.Default.Audiotrack
        ActivityEntityType.FLASH -> Icons.Default.Bolt
        ActivityEntityType.QURAN_RECITATION -> Icons.AutoMirrored.Filled.MenuBook
    }

@Composable
private fun getEntityTypeColor(type: ActivityEntityType): Color =
    when (type) {
        ActivityEntityType.VIDEO -> MaterialTheme.colorScheme.primary
        ActivityEntityType.AUDIO -> MaterialTheme.colorScheme.secondary
        ActivityEntityType.FLASH -> MaterialTheme.statusColors.warningFg // Amber / Orange
        ActivityEntityType.QURAN_RECITATION -> MaterialTheme.statusColors.successFg // Islamic Forest Green
    }

private fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "الآن"
        minutes < 60 -> "منذ $minutes دقيقة"
        hours < 24 -> "منذ $hours ساعة"
        days == 1L -> "أمس"
        days < 7 -> "منذ $days أيام"
        else -> {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
