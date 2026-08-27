package com.siraj.app.features.project.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.Project

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectEditorScreen(
    projectId: String,
    onNavigateBack: () -> Unit,
    viewModel: ProjectEditorViewModel = viewModel(factory = ProjectEditorViewModelFactory(projectId))
) {
    val projectState by viewModel.projectState.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = when(projectState) {
                            is Resource.Success -> (projectState as Resource.Success<Project>).data.title
                            else -> "جاري التحميل..."
                        }
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "عودة")
                    }
                },
                actions = {
                    when(saveState) {
                        is SaveState.Saving -> Text("جاري الحفظ...", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(end = 16.dp))
                        is SaveState.Saved -> Text("تم الحفظ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 16.dp))
                        is SaveState.Error -> Text("خطأ في الحفظ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(end = 16.dp))
                        is SaveState.Idle -> {}
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف المشروع", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("حذف المشروع") },
                text = { Text("هل أنت متأكد من حذف هذا المشروع؟ سيتم نقله إلى سلة المهملات.") },
                confirmButton = {
                    Button(
                        onClick = { 
                            showDeleteDialog = false
                            viewModel.deleteProject { onNavigateBack() }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("حذف")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("إلغاء")
                    }
                }
            )
        }

        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = projectState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is Resource.Error -> {
                    Text(
                        text = state.message, 
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is Resource.Success -> {
                    val project = state.data
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        OutlinedTextField(
                            value = project.title,
                            onValueChange = { viewModel.updateTitle(it) },
                            label = { Text("اسم المشروع") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = project.description,
                            onValueChange = { viewModel.updateDescription(it) },
                            label = { Text("الوصف") },
                            modifier = Modifier.fillMaxWidth().height(150.dp)
                        )
                        
                        // Placeholder for scenes and assets
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("مشاهد المشروع", style = MaterialTheme.typography.titleMedium)
                        Card(modifier = Modifier.fillMaxWidth().height(200.dp).padding(top = 8.dp)) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("لا توجد مشاهد بعد.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
