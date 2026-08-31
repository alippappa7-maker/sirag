package com.siraj.app.features.studio.presentation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.core.ui.components.SirajSectionHeader
import com.siraj.app.core.ui.components.SirajStatusBadge
import com.siraj.app.core.ui.components.SirajTechCard
import com.siraj.app.core.ui.components.StatusType
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.Project
import com.siraj.app.domain.models.ProjectStatus
import com.siraj.app.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioScreen(
    onNavigateToProject: (String) -> Unit = {},
    onNavigateToIdeation: () -> Unit = {},
    onNavigateToFlashPublishing: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    viewModel: StudioViewModel = viewModel(factory = StudioViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()
    val spacing = LocalSpacing.current

    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(spacing.medium)
            ) {
                SmallFloatingActionButton(
                    onClick = onNavigateToAnalytics,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Icon(Icons.Default.Assessment, contentDescription = "التحليلات")
                }
                SmallFloatingActionButton(
                    onClick = onNavigateToFlashPublishing,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = "نشر ومضة")
                }
                ExtendedFloatingActionButton(
                    onClick = onNavigateToIdeation,
                    icon = { Icon(Icons.Default.Add, contentDescription = "فكرة جديدة") },
                    text = { Text("فكرة إبداعية جديدة") },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.medium)
        ) {
            SirajSectionHeader(title = "استوديو الإنتاج التقني")

            // Search and Filters Area
            SirajTechCard(isActive = false) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("بحث في المشاريع...") },
                    leadingIcon = { 
                        Icon(
                            Icons.Default.Search, 
                            contentDescription = "بحث",
                            tint = MaterialTheme.colorScheme.tertiary 
                        ) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(spacing.medium))
                Row(
                    modifier = Modifier.fillMaxWidth(), 
                    horizontalArrangement = Arrangement.spacedBy(spacing.small)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        FilterDropdown("تصفية: ${uiState.filterOption}", listOf("نشط", "مؤرشف", "محذوف")) {
                            viewModel.updateFilterOption(it)
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        FilterDropdown("ترتيب: ${uiState.sortOption}", listOf("الأحدث", "الأقدم", "الاسم (أ-ي)")) {
                            viewModel.updateSortOption(it)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.small))
            Text(
                text = "مساحة العمل",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Crossfade(targetState = uiState.projects, label = "projects_crossfade") { projectsRes ->
                when (projectsRes) {
                    is Resource.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                    is Resource.Error -> {
                        Text(text = projectsRes.message, color = MaterialTheme.colorScheme.error)
                    }
                    is Resource.Success -> {
                        if (projectsRes.data.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("مساحة العمل فارغة. ابدأ فكرة جديدة.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(spacing.medium),
                                contentPadding = PaddingValues(bottom = 80.dp) // Space for FABs
                            ) {
                                items(projectsRes.data, key = { it.id }) { project ->
                                    StudioProjectCard(
                                        project = project,
                                        onClick = { onNavigateToProject(project.id) },
                                        onCopy = { viewModel.copyProject(project.id, project.ownerId) },
                                        onArchive = { viewModel.archiveProject(project.id) },
                                        onRestore = { viewModel.restoreProject(project.id) },
                                        onDelete = { viewModel.deleteProject(project.id) }
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

@Composable
fun FilterDropdown(label: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun StudioProjectCard(
    project: Project,
    onClick: () -> Unit,
    onCopy: () -> Unit,
    onArchive: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    val statusType = when (project.status) {
        ProjectStatus.READY, ProjectStatus.COMPLETED -> StatusType.SUCCESS
        ProjectStatus.DRAFT, ProjectStatus.ARCHIVED -> StatusType.WARNING
        ProjectStatus.FAILED, ProjectStatus.DELETED -> StatusType.ERROR
        ProjectStatus.PROCESSING, ProjectStatus.EXPORTING -> StatusType.INFO
    }

    val isActive = project.status == ProjectStatus.READY || project.status == ProjectStatus.COMPLETED || project.status == ProjectStatus.PROCESSING || project.status == ProjectStatus.EXPORTING

    SirajTechCard(
        isActive = isActive,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = project.title, 
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                SirajStatusBadge(
                    text = project.status.name,
                    statusType = statusType
                )
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert, 
                        contentDescription = androidx.compose.ui.res.stringResource(com.siraj.app.R.string.options),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text("فتح في المحرر") }, onClick = { showMenu = false; onClick() })
                    DropdownMenuItem(text = { Text("نسخ") }, onClick = { showMenu = false; onCopy() })
                    if (project.status != ProjectStatus.ARCHIVED && project.status != ProjectStatus.DELETED) {
                        DropdownMenuItem(text = { Text("أرشفة") }, onClick = { showMenu = false; onArchive() })
                        DropdownMenuItem(text = { Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.delete)) }, onClick = { showMenu = false; onDelete() })
                    } else if (project.status == ProjectStatus.ARCHIVED || project.status == ProjectStatus.DELETED) {
                        DropdownMenuItem(text = { Text("استعادة") }, onClick = { showMenu = false; onRestore() })
                    }
                }
            }
        }
    }
}

