package com.siraj.app.features.project.presentation.scenes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScenesScreen(
    projectId: String,
    onNavigateBack: () -> Unit,
    viewModel: ScenesViewModel = viewModel(factory = ScenesViewModelFactory(projectId))
) {
    val projectState by viewModel.projectState.collectAsState()
    var sceneToEdit by remember { mutableStateOf<Scene?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إنتاج المشاهد") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "عودة") }
                },
                actions = {
                    val currentProj = (projectState as? Resource.Success)?.data
                    if (currentProj != null) {
                        val durationSeconds = currentProj.durationMs / 1000
                        val min = durationSeconds / 60
                        val sec = durationSeconds % 60
                        Text("المدة: ${String.format("%02d:%02d", min, sec)}", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(end = 16.dp))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.addScene() }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة مشهد")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = projectState) {
                is Resource.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is Resource.Error -> Text(state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                is Resource.Success -> {
                    val project = state.data
                    if (project.scenes.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("لا توجد مشاهد بعد", style = MaterialTheme.typography.titleMedium)
                            if (project.contentPlan != null && project.contentPlan.claims.isNotEmpty()) {
                                Button(onClick = { viewModel.generateScenesFromPlan() }) {
                                    Text("توليد المشاهد من السيناريو")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(project.scenes.sortedBy { it.orderIndex }) { scene ->
                                SceneCard(
                                    scene = scene,
                                    onEdit = { sceneToEdit = scene },
                                    onDelete = { viewModel.deleteScene(scene.id) },
                                    onDuplicate = { viewModel.duplicateScene(scene) },
                                    onMoveUp = { 
                                        val index = project.scenes.indexOf(scene)
                                        if (index > 0) viewModel.reorderScenes(index, index - 1)
                                    },
                                    onMoveDown = {
                                        val index = project.scenes.indexOf(scene)
                                        if (index < project.scenes.size - 1) viewModel.reorderScenes(index, index + 1)
                                    },
                                    project = project
                                )
                            }
                        }
                    }
                }
            }
        }
        
        if (sceneToEdit != null) {
            val currentProj = (projectState as? Resource.Success)?.data
            if (currentProj != null) {
                SceneEditorDialog(
                    scene = sceneToEdit!!,
                    project = currentProj,
                    onDismiss = { sceneToEdit = null },
                    onSave = { updated -> 
                        viewModel.updateScene(updated)
                        sceneToEdit = null
                    }
                )
            }
        }
    }
}

@Composable
fun SceneCard(
    scene: Scene,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    project: Project
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.small),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${scene.orderIndex + 1}", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(scene.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                
                Row {
                    IconButton(onClick = onMoveUp) { Icon(Icons.Default.KeyboardArrowUp, contentDescription = "أعلى") }
                    IconButton(onClick = onMoveDown) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = "أسفل") }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(scene.narrationText.ifEmpty { "بدون نص تعليق" }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = {},
                        label = { Text("${scene.durationMs / 1000} ثانية") },
                        leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text(scene.transition.name) }
                    )
                }
                Row {
                    IconButton(onClick = onDuplicate) { Icon(Icons.Default.AddCircle, contentDescription = "نسخ") }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error) }
                }
            }
            
            if (scene.claimIds.isNotEmpty()) {
                val linkedClaims = project.contentPlan?.claims?.filter { it.id in scene.claimIds } ?: emptyList()
                if (linkedClaims.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("الادعاءات المرتبطة: ${linkedClaims.size}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SceneEditorDialog(
    scene: Scene,
    project: Project,
    onDismiss: () -> Unit,
    onSave: (Scene) -> Unit
) {
    var title by remember { mutableStateOf(scene.title) }
    var text by remember { mutableStateOf(scene.narrationText) }
    var durationSec by remember { mutableStateOf((scene.durationMs / 1000).toString()) }
    var transition by remember { mutableStateOf(scene.transition) }
    var backgroundType by remember { mutableStateOf(scene.backgroundType) }
    
    var expandedTransition by remember { mutableStateOf(false) }
    var expandedBackground by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize().padding(16.dp), shape = MaterialTheme.shapes.large) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("تعديل المشهد", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان المشهد") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("نص التعليق الصوتي") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    supportingText = { Text("ملاحظة: تعديل النصوص الشرعية يخضع للمراجعة") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = durationSec,
                    onValueChange = { durationSec = it.filter { char -> char.isDigit() } },
                    label = { Text("المدة (بالثواني)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Selectors
                ExposedDropdownMenuBox(expanded = expandedTransition, onExpandedChange = { expandedTransition = !expandedTransition }) {
                    OutlinedTextField(
                        value = transition.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("نوع الانتقال") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTransition) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expandedTransition, onDismissRequest = { expandedTransition = false }) {
                        TransitionType.values().forEach { t ->
                            DropdownMenuItem(text = { Text(t.name) }, onClick = { transition = t; expandedTransition = false })
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ExposedDropdownMenuBox(expanded = expandedBackground, onExpandedChange = { expandedBackground = !expandedBackground }) {
                    OutlinedTextField(
                        value = backgroundType.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("نوع الخلفية") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBackground) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expandedBackground, onDismissRequest = { expandedBackground = false }) {
                        BackgroundType.values().forEach { b ->
                            DropdownMenuItem(text = { Text(b.name) }, onClick = { backgroundType = b; expandedBackground = false })
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Button(onClick = {
                        val updated = scene.copy(
                            title = title,
                            narrationText = text,
                            durationMs = (durationSec.toLongOrNull() ?: 5L) * 1000,
                            transition = transition,
                            backgroundType = backgroundType,
                            status = SceneStatus.EDITED
                        )
                        onSave(updated)
                    }) {
                        Text("حفظ المشهد")
                    }
                }
            }
        }
    }
}
