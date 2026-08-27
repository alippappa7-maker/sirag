package com.siraj.app.features.project.presentation.scenes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.domain.models.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SceneEditorScreen(
    projectId: String,
    sceneId: String,
    onNavigateBack: () -> Unit,
    viewModel: SceneEditorViewModel = viewModel(factory = SceneEditorViewModelFactory(projectId, sceneId))
) {
    val scene by viewModel.sceneState.collectAsState()
    val sceneText by viewModel.sceneTextState.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    if (scene == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("محرر المشهد") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "عودة") }
                },
                actions = {
                    IconButton(onClick = { viewModel.undo() }, enabled = canUndo) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "تراجع")
                    }
                    IconButton(onClick = { viewModel.redo() }, enabled = canRedo) {
                        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "إعادة")
                    }
                    IconButton(onClick = { viewModel.duplicateScene(onNavigateBack) }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "نسخ المشهد")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preview Box
            ScenePreviewBox(scene = scene!!, sceneText = sceneText)
            
            Divider()

            if (scene!!.status == SceneStatus.APPROVED) {
                Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "هذا المشهد معتمد. تعديل النص الشرعي سيعيد المشروع للمراجعة ولن يتم تغيير المصدر الأصلي.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            // Title
            OutlinedTextField(
                value = scene!!.title,
                onValueChange = { 
                    viewModel.updateSceneAndText(scene!!.copy(title = it), sceneText) 
                },
                label = { Text("عنوان المشهد") },
                modifier = Modifier.fillMaxWidth()
            )

            // Narration Text
            OutlinedTextField(
                value = scene!!.narrationText,
                onValueChange = { 
                    viewModel.updateSceneAndText(scene!!.copy(narrationText = it), sceneText) 
                },
                label = { Text("نص التعليق الصوتي") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 5
            )
            
            // On-Screen Text (Caption)
            OutlinedTextField(
                value = sceneText.text,
                onValueChange = { 
                    viewModel.updateSceneAndText(scene!!, sceneText.copy(text = it)) 
                },
                label = { Text("النص الظاهر على الشاشة (Caption)") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                maxLines = 4
            )
            
            // Formatting Controls for On-Screen Text
            Text("تنسيق النص على الشاشة", style = MaterialTheme.typography.titleMedium)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Font Size
                var fontSizeStr by remember { mutableStateOf(sceneText.fontSize.toString()) }
                OutlinedTextField(
                    value = fontSizeStr,
                    onValueChange = { 
                        fontSizeStr = it
                        it.toFloatOrNull()?.let { size ->
                            viewModel.updateSceneAndText(scene!!, sceneText.copy(fontSize = size)) 
                        }
                    },
                    label = { Text("حجم الخط") },
                    modifier = Modifier.weight(1f)
                )
                
                // Alignment
                var expandedAlign by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedAlign, 
                    onExpandedChange = { expandedAlign = !expandedAlign },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = sceneText.alignment,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("المحاذاة") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAlign) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expandedAlign, onDismissRequest = { expandedAlign = false }) {
                        listOf("Start", "Center", "End").forEach { a ->
                            DropdownMenuItem(text = { Text(a) }, onClick = { 
                                viewModel.updateSceneAndText(scene!!, sceneText.copy(alignment = a))
                                expandedAlign = false 
                            })
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Position
                var expandedPos by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedPos, 
                    onExpandedChange = { expandedPos = !expandedPos },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = sceneText.position,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("الموضع") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPos) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expandedPos, onDismissRequest = { expandedPos = false }) {
                        listOf("Top", "Center", "Bottom").forEach { p ->
                            DropdownMenuItem(text = { Text(p) }, onClick = { 
                                viewModel.updateSceneAndText(scene!!, sceneText.copy(position = p))
                                expandedPos = false 
                            })
                        }
                    }
                }
                
                // Show Source Toggle
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = sceneText.showSource,
                        onCheckedChange = { 
                            viewModel.updateSceneAndText(scene!!, sceneText.copy(showSource = it)) 
                        }
                    )
                    Text("إظهار المصدر")
                }
            }

            // Duration and Settings
            Text("إعدادات المشهد", style = MaterialTheme.typography.titleMedium)
            
            var durationStr by remember { mutableStateOf((scene!!.durationMs / 1000).toString()) }
            OutlinedTextField(
                value = durationStr,
                onValueChange = { 
                    durationStr = it.filter { char -> char.isDigit() }
                    durationStr.toLongOrNull()?.let { sec ->
                         viewModel.updateSceneAndText(scene!!.copy(durationMs = sec * 1000), sceneText)
                    }
                },
                label = { Text("المدة (بالثواني)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            var expandedTransition by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expandedTransition, onExpandedChange = { expandedTransition = !expandedTransition }) {
                OutlinedTextField(
                    value = scene!!.transition.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("نوع الانتقال") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTransition) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expandedTransition, onDismissRequest = { expandedTransition = false }) {
                    TransitionType.values().forEach { t ->
                        DropdownMenuItem(text = { Text(t.name) }, onClick = { 
                            viewModel.updateSceneAndText(scene!!.copy(transition = t), sceneText)
                            expandedTransition = false 
                        })
                    }
                }
            }
            
            var expandedBg by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expandedBg, onExpandedChange = { expandedBg = !expandedBg }) {
                OutlinedTextField(
                    value = scene!!.backgroundType.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("نوع الخلفية") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBg) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expandedBg, onDismissRequest = { expandedBg = false }) {
                    BackgroundType.values().forEach { b ->
                        DropdownMenuItem(text = { Text(b.name) }, onClick = { 
                            viewModel.updateSceneAndText(scene!!.copy(backgroundType = b), sceneText)
                            expandedBg = false 
                        })
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ScenePreviewBox(scene: Scene, sceneText: SceneText) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.DarkGray)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
    ) {
        // Simulated Background
        Text(
            text = "معاينة: ${scene.backgroundType.name}",
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.Center)
        )
        
        // On Screen Text
        val align = when (sceneText.alignment) {
            "Start" -> Alignment.CenterStart
            "End" -> Alignment.CenterEnd
            else -> Alignment.Center
        }
        
        val posAlign = when (sceneText.position) {
            "Top" -> Alignment.TopCenter
            "Bottom" -> Alignment.BottomCenter
            else -> Alignment.Center
        }
        
        // Combine alignments roughly for preview
        val boxAlign = when {
            sceneText.position == "Top" && sceneText.alignment == "Start" -> Alignment.TopStart
            sceneText.position == "Top" && sceneText.alignment == "End" -> Alignment.TopEnd
            sceneText.position == "Top" -> Alignment.TopCenter
            sceneText.position == "Bottom" && sceneText.alignment == "Start" -> Alignment.BottomStart
            sceneText.position == "Bottom" && sceneText.alignment == "End" -> Alignment.BottomEnd
            sceneText.position == "Bottom" -> Alignment.BottomCenter
            sceneText.alignment == "Start" -> Alignment.CenterStart
            sceneText.alignment == "End" -> Alignment.CenterEnd
            else -> Alignment.Center
        }
        
        Column(
            modifier = Modifier.align(boxAlign).padding(16.dp),
            horizontalAlignment = when(sceneText.alignment) {
                "Start" -> Alignment.Start
                "End" -> Alignment.End
                else -> Alignment.CenterHorizontally
            }
        ) {
            if (sceneText.text.isNotEmpty()) {
                Text(
                    text = sceneText.text,
                    color = Color.White,
                    textAlign = when(sceneText.alignment) {
                        "Start" -> TextAlign.Start
                        "End" -> TextAlign.End
                        else -> TextAlign.Center
                    },
                    fontWeight = FontWeight.Bold
                )
            }
            if (sceneText.showSource) {
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text("المصدر: موثق", color = Color.Yellow, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(4.dp))
                }
            }
        }
    }
}
