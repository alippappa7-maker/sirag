package com.siraj.app.features.notification.presentation

import android.app.Application
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.domain.models.notification.NotificationFilter
import com.siraj.app.domain.models.notification.NotificationType
import com.siraj.app.domain.models.notification.SirajNotification

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProject: (String) -> Unit = {},
    onNavigateToReview: (String) -> Unit = {},
    onNavigateToAudio: (String) -> Unit = {},
    onNavigateToFlashes: () -> Unit = {},
    onNavigateToMihrab: () -> Unit = {},
    viewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("مسح جميع الإشعارات") },
            text = { Text("هل أنت متأكد من رغبتك في حذف كافة الإشعارات؟ لا يمكن التراجع عن هذه الخطوة.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllNotifications()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("مسح الكل")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "مركز الإشعارات",
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Text(
                                    text = "${uiState.unreadCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = androidx.compose.ui.res.stringResource(com.siraj.app.R.string.back))
                    }
                },
                actions = {
                    if (uiState.unreadCount > 0) {
                        IconButton(
                            onClick = { viewModel.markAllAsRead() }
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = "تحديد الكل كمقروء")
                        }
                    }
                    if (uiState.notifications.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearConfirmDialog = true }
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "مسح الكل")
                        }
                    }
                    IconButton(
                        onClick = onNavigateToSettings
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "إعدادات الإشعارات")
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
            // Category Filter Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(NotificationFilter.entries) { filter ->
                    FilterChip(
                        selected = uiState.selectedFilter == filter,
                        onClick = { viewModel.setFilter(filter) },
                        label = {
                            Text(
                                text = filter.titleAr,
                                fontWeight = if (uiState.selectedFilter == filter) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = if (uiState.selectedFilter == filter) {
                            {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Notifications List
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredNotifications.isEmpty()) {
                EmptyNotificationsView(selectedFilter = uiState.selectedFilter)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.filteredNotifications,
                        key = { it.id }
                    ) { notification ->
                        NotificationCard(
                            notification = notification,
                            onNotificationClick = {
                                viewModel.markAsRead(notification.id)
                                handleNotificationNavigation(
                                    notification = notification,
                                    onNavigateToProject = onNavigateToProject,
                                    onNavigateToReview = onNavigateToReview,
                                    onNavigateToAudio = onNavigateToAudio,
                                    onNavigateToFlashes = onNavigateToFlashes,
                                    onNavigateToMihrab = onNavigateToMihrab,
                                    onNavigateToSettings = onNavigateToSettings
                                )
                            },
                            onDeleteClick = {
                                viewModel.deleteNotification(notification.id)
                            },
                            onMarkAsReadClick = {
                                viewModel.markAsRead(notification.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: SirajNotification,
    onNotificationClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMarkAsReadClick: () -> Unit
) {
    val isUnread = !notification.isRead
    val icon = getNotificationIcon(notification.type)
    val iconColor = getNotificationColor(notification.type)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNotificationClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnread)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnread) 2.dp else 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Category Icon Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Main Text Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.type.categoryAr,
                        style = MaterialTheme.typography.labelSmall,
                        color = iconColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = formatRelativeTime(notification.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isUnread) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                if (notification.isSensitive) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "محتوى شرعي حساس (محمي في شاشة القفل)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            // Actions (Mark Read / Delete)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "حذف الإشعار",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isUnread) {
                    IconButton(
                        onClick = onMarkAsReadClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircleOutline,
                            contentDescription = "تحديد كمقروء",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyNotificationsView(selectedFilter: NotificationFilter) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(88.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (selectedFilter == NotificationFilter.ALL) "لا توجد إشعارات حالياً" else "لا توجد إشعارات في قسم ${selectedFilter.titleAr}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "ستصلك التنبيهات هنا فور اكتمال معالجة مشاريعك، مراجعة المحتوى، أو مواقيت الصلاة.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

private fun getNotificationIcon(type: NotificationType): ImageVector {
    return when (type) {
        NotificationType.VIDEO_GENERATION_COMPLETED -> Icons.Default.VideoLibrary
        NotificationType.EXPORT_FAILED -> Icons.Default.ErrorOutline
        NotificationType.REVIEW_REQUESTED -> Icons.Default.RateReview
        NotificationType.REVIEW_RESULT -> Icons.Default.Verified
        NotificationType.PROJECT_COMMENT_UPDATE -> Icons.Default.Comment
        NotificationType.NEW_AUDIO_CONTENT -> Icons.Default.Headphones
        NotificationType.NEW_FLASH -> Icons.Default.Bolt
        NotificationType.PRAYER_REMINDER -> Icons.Default.AccessTime
        NotificationType.MORNING_EVENING_ADHKAR -> Icons.Default.MenuBook
        NotificationType.SUBSCRIPTION_BILLING -> Icons.Default.CreditCard
        NotificationType.SYSTEM_MESSAGE -> Icons.Default.Info
    }
}

@Composable
private fun getNotificationColor(type: NotificationType): Color {
    return when (type) {
        NotificationType.VIDEO_GENERATION_COMPLETED -> MaterialTheme.colorScheme.primary
        NotificationType.EXPORT_FAILED -> MaterialTheme.colorScheme.error
        NotificationType.REVIEW_REQUESTED -> MaterialTheme.colorScheme.tertiary
        NotificationType.REVIEW_RESULT -> Color(0xFF2E7D32) // Islamic Emerald Green
        NotificationType.PROJECT_COMMENT_UPDATE -> MaterialTheme.colorScheme.secondary
        NotificationType.NEW_AUDIO_CONTENT -> Color(0xFF0288D1)
        NotificationType.NEW_FLASH -> Color(0xFFF57C00)
        NotificationType.PRAYER_REMINDER -> Color(0xFF00897B)
        NotificationType.MORNING_EVENING_ADHKAR -> Color(0xFF5E35B1)
        NotificationType.SUBSCRIPTION_BILLING -> Color(0xFF673AB7)
        NotificationType.SYSTEM_MESSAGE -> MaterialTheme.colorScheme.outline
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val diffMillis = System.currentTimeMillis() - timestamp
    val seconds = diffMillis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "الآن"
        minutes < 60 -> "منذ $minutes د"
        hours < 24 -> "منذ $hours س"
        days < 7 -> "منذ $days يوم"
        else -> {
            val date = java.util.Date(timestamp)
            val format = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            format.format(date)
        }
    }
}

private fun handleNotificationNavigation(
    notification: SirajNotification,
    onNavigateToProject: (String) -> Unit,
    onNavigateToReview: (String) -> Unit,
    onNavigateToAudio: (String) -> Unit,
    onNavigateToFlashes: () -> Unit,
    onNavigateToMihrab: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val entityId = notification.entityId ?: ""
    when (notification.type) {
        NotificationType.VIDEO_GENERATION_COMPLETED,
        NotificationType.EXPORT_FAILED,
        NotificationType.PROJECT_COMMENT_UPDATE -> {
            if (entityId.isNotBlank()) onNavigateToProject(entityId) else onNavigateToProject("sample_project_1")
        }
        NotificationType.REVIEW_REQUESTED,
        NotificationType.REVIEW_RESULT -> {
            if (entityId.isNotBlank()) onNavigateToReview(entityId) else onNavigateToReview("sample_review_1")
        }
        NotificationType.NEW_AUDIO_CONTENT -> {
            if (entityId.isNotBlank()) onNavigateToAudio(entityId) else onNavigateToAudio("surah_67")
        }
        NotificationType.NEW_FLASH -> {
            onNavigateToFlashes()
        }
        NotificationType.PRAYER_REMINDER,
        NotificationType.MORNING_EVENING_ADHKAR -> {
            onNavigateToMihrab()
        }
        NotificationType.SUBSCRIPTION_BILLING,
        NotificationType.SYSTEM_MESSAGE -> {
            onNavigateToSettings()
        }
    }
}
