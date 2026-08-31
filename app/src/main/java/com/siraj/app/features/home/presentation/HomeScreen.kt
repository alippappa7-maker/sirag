package com.siraj.app.features.home.presentation

import android.app.Application
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.core.ui.components.BetaBadgeBanner
import com.siraj.app.core.ui.components.SirajGlowContainer
import com.siraj.app.core.ui.components.SirajSectionHeader
import com.siraj.app.core.ui.components.SirajTechCard
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.Project
import com.siraj.app.features.notification.presentation.NotificationViewModel
import com.siraj.app.features.notification.presentation.NotificationViewModelFactory
import com.siraj.app.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    toggleTheme: () -> Unit,
    onNavigateToProject: (String) -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToShariaReview: () -> Unit = {},
    onNavigateToTesterHub: () -> Unit = {},
    viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory()),
    notificationViewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val notifState by notificationViewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var newProjectTitle by remember { mutableStateOf("") }
    val spacing = LocalSpacing.current

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("مشروع جديد") },
            text = {
                OutlinedTextField(
                    value = newProjectTitle,
                    onValueChange = { newProjectTitle = it },
                    label = { Text("اسم المشروع") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProjectTitle.isNotBlank()) {
                            viewModel.createProject(newProjectTitle) { projectId ->
                                onNavigateToProject(projectId)
                            }
                            showCreateDialog = false
                            newProjectTitle = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("إنشاء")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.cancel))
                }
            },
            shape = MaterialTheme.shapes.large
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(spacing.medium),
        verticalArrangement = Arrangement.spacedBy(spacing.large)
    ) {
        // 1. Header Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "مرحباً، ${uiState.userProfile?.name ?: "مستخدم"}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "سجل النشاط",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onNavigateToNotifications) {
                        BadgedBox(
                            badge = {
                                if (notifState.unreadCount > 0) {
                                    Badge {
                                        Text(if (notifState.unreadCount > 99) "+99" else "${notifState.unreadCount}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (notifState.unreadCount > 0) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                contentDescription = "الإشعارات",
                                tint = if (notifState.unreadCount > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = toggleTheme) {
                        Text("🌓")
                    }
                }
            }
        }

        // 2. Beta Banner
        item {
            BetaBadgeBanner(
                currentRoute = "home",
                onOpenTesterHub = onNavigateToTesterHub
            )
        }

        // 3. The Central Technical Hero Container
        item {
            SirajGlowContainer(
                isActive = true,
                glowColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.large),
                    verticalArrangement = Arrangement.spacedBy(spacing.medium)
                ) {
                    Text(
                        text = "جاهز لإنتاج محتوى جديد اليوم؟",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Search (Cyan touch)
                    Surface(
                        onClick = onNavigateToSearch,
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth().testTag("home_search_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.medium, vertical = spacing.medium),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "البحث",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(spacing.medium))
                            Text(
                                text = "ابحث في القرآن، الصوتيات، الومضات...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Primary Action (Gold touch)
                    Button(
                        onClick = { showCreateDialog = true },
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(spacing.small))
                        Text(
                            text = "إنشاء فيديو جديد",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 4. Recent Projects
        item {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
                SirajSectionHeader(title = "المشاريع الأخيرة")
                
                Crossfade(targetState = uiState.recentProjects, label = "projects_crossfade") { projectsRes ->
                    when (projectsRes) {
                        is Resource.Loading -> {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                        is Resource.Error -> {
                            Text(text = "خطأ: ${projectsRes.message}", color = MaterialTheme.colorScheme.error)
                        }
                        is Resource.Success -> {
                            if (projectsRes.data.isEmpty()) {
                                SirajTechCard(isActive = false) {
                                    Box(modifier = Modifier.fillMaxWidth().padding(spacing.medium), contentAlignment = Alignment.Center) {
                                        Text("لا توجد مشاريع سابقة. ابدأ بإنشاء مشروع جديد.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            } else {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                                    contentPadding = PaddingValues(horizontal = 4.dp) // for shadows
                                ) {
                                    items(projectsRes.data, key = { it.id }) { project ->
                                        ProjectCard(project = project, onClick = { onNavigateToProject(project.id) })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Shortcuts
        item {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
                SirajSectionHeader(title = "اختصارات سريعة")
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ShortcutCard("المحراب", Modifier.weight(1f), onClick = { /* TODO Navigate */ })
                    ShortcutCard("ومضات", Modifier.weight(1f), onClick = { /* TODO Navigate */ })
                    
                    // Sharia Review - specific highlight
                    SirajTechCard(
                        isActive = true, // specific highlight
                        modifier = Modifier
                            .weight(1.2f)
                            .height(90.dp)
                            .testTag("shortcut_sharia_review"),
                        onClick = { onNavigateToShariaReview() }
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "المراجعة الشرعية",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "تدقيق وتوثيق",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }
            }
            // Bottom Spacing for navigation bar
            Spacer(modifier = Modifier.height(spacing.huge))
        }
    }
}

@Composable
fun ProjectCard(project: Project, onClick: () -> Unit) {
    val spacing = LocalSpacing.current
    SirajTechCard(
        isActive = false,
        onClick = onClick,
        modifier = Modifier.width(220.dp).height(110.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = project.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = "آخر تعديل: ${project.updatedAt}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
fun ShortcutCard(title: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    SirajTechCard(
        isActive = false,
        onClick = onClick,
        modifier = modifier.height(90.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
