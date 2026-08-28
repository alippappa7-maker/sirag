package com.siraj.app.features.studio.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.Project
import com.siraj.app.domain.models.ProjectStatus

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
    Scaffold(
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToAnalytics,
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "التحليلات") },
                    text = { Text("التحليلات") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                ExtendedFloatingActionButton(
                    onClick = onNavigateToFlashPublishing,
                    icon = { Icon(Icons.Default.Add, contentDescription = "نشر ومضة") },
                    text = { Text("نشر ومضة") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                ExtendedFloatingActionButton(
                    onClick = onNavigateToIdeation,
                    icon = { Icon(Icons.Default.Add, contentDescription = "فكرة جديدة") },
                    text = { Text("فكرة جديدة") }
                )
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            Text(text = "إدارة المشاريع", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text("بحث في المشاريع") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                FilterDropdown("تصفية: ${uiState.filterOption}", listOf("نشط", "مؤرشف", "محذوف")) {
                    viewModel.updateFilterOption(it)
                }
                FilterDropdown("ترتيب: ${uiState.sortOption}", listOf("الأحدث", "الأقدم", "الاسم (أ-ي)")) {
                    viewModel.updateSortOption(it)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            when (val projectsRes = uiState.projects) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Error -> {
                    Text(text = projectsRes.message, color = MaterialTheme.colorScheme.error)
                }
                is Resource.Success -> {
                    if (projectsRes.data.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("لا توجد مشاريع.")
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(projectsRes.data) { project ->
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

@Composable
fun FilterDropdown(label: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { expanded = true }) {
            Text(label)
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
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = project.title, style = MaterialTheme.typography.titleMedium)
                Text(text = "الحالة: ${project.status.name}", style = MaterialTheme.typography.bodySmall)
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "خيارات")
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text("فتح") }, onClick = { showMenu = false; onClick() })
                    DropdownMenuItem(text = { Text("نسخ") }, onClick = { showMenu = false; onCopy() })
                    if (project.status != ProjectStatus.ARCHIVED && project.status != ProjectStatus.DELETED) {
                        DropdownMenuItem(text = { Text("أرشفة") }, onClick = { showMenu = false; onArchive() })
                        DropdownMenuItem(text = { Text("حذف") }, onClick = { showMenu = false; onDelete() })
                    } else if (project.status == ProjectStatus.ARCHIVED || project.status == ProjectStatus.DELETED) {
                        DropdownMenuItem(text = { Text("استعادة") }, onClick = { showMenu = false; onRestore() })
                    }
                }
            }
        }
    }
}
