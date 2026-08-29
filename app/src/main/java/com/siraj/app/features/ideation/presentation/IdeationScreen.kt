package com.siraj.app.features.ideation.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.domain.models.GeneratedIdea
import com.siraj.app.domain.models.RiskLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeationScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProject: (String) -> Unit,
    viewModel: IdeationViewModel = viewModel(factory = IdeationViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مولد الأفكار") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.generatedIdeas.isNotEmpty()) {
                            viewModel.clearIdeas()
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = androidx.compose.ui.res.stringResource(com.siraj.app.R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isGenerating) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("جاري العصف الذهني وتوليد الأفكار...")
                }
            } else if (uiState.generatedIdeas.isNotEmpty()) {
                // Results View
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text("الأفكار المقترحة", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("يمكنك تحويل أي فكرة لمشروع لبدء العمل عليها.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(uiState.generatedIdeas) { idea ->
                        IdeaCard(
                            idea = idea,
                            onConvert = { viewModel.convertToProject(idea, onNavigateToProject) },
                            onReject = { viewModel.dismissIdea(idea.id) },
                            onReport = { viewModel.reportIdea(idea.id, "محتوى غير مناسب") }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { viewModel.generateIdeas() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إعادة توليد أفكار جديدة")
                        }
                    }
                }
            } else {
                // Form View
                val req = uiState.request
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = req.subject,
                        onValueChange = { s -> viewModel.updateRequest { it.copy(subject = s) } },
                        label = { Text("عن ماذا تريد التحدث؟ (الموضوع الأساسي)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DropdownOption(
                            label = "الجمهور",
                            options = listOf("عام", "أطفال", "شباب", "متخصصون"),
                            selected = req.audience,
                            onSelected = { s -> viewModel.updateRequest { it.copy(audience = s) } },
                            modifier = Modifier.weight(1f)
                        )
                        DropdownOption(
                            label = "المنصة",
                            options = listOf("TikTok / Reels (9:16)", "YouTube (16:9)"),
                            selected = req.platform,
                            onSelected = { s -> viewModel.updateRequest { it.copy(platform = s) } },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DropdownOption(
                            label = "المدة",
                            options = listOf("قصير (أقل من دقيقة)", "متوسط (1-3 دقائق)"),
                            selected = req.duration,
                            onSelected = { s -> viewModel.updateRequest { it.copy(duration = s) } },
                            modifier = Modifier.weight(1f)
                        )
                        DropdownOption(
                            label = "النبرة",
                            options = listOf("تحفيزي", "قصصي", "أكاديمي", "وعظي"),
                            selected = req.tone,
                            onSelected = { s -> viewModel.updateRequest { it.copy(tone = s) } },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    DropdownOption(
                        label = "الهدف",
                        options = listOf("توعية", "تفاعل (إعجابات ومشاركات)", "تعليم"),
                        selected = req.goal,
                        onSelected = { s -> viewModel.updateRequest { it.copy(goal = s) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = req.hasReligiousElement,
                                onCheckedChange = { c -> viewModel.updateRequest { it.copy(hasReligiousElement = c) } }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("يحتوي على عنصر شرعي (آية، حديث، فتوى)", fontWeight = FontWeight.Bold)
                                Text("تفعيل هذا الخيار سيضيف متطلبات مراجعة للحفاظ على الموثوقية.", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    
                    if (uiState.error != null) {
                        Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Button(
                        onClick = { viewModel.generateIdeas() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = req.subject.isNotBlank()
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("توليد الأفكار")
                    }
                }
            }
        }
    }
}

@Composable
fun IdeaCard(
    idea: GeneratedIdea,
    onConvert: () -> Unit,
    onReject: () -> Unit,
    onReport: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(idea.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("الخطاف (Hook):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            Text(idea.hook, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            
            Text("الملخص:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            Text(idea.summary, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(idea.audience) })
                AssistChip(onClick = {}, label = { Text("${idea.suggestedScenes} مشاهد مقترحة") })
            }
            
            if (idea.needsReview || idea.riskLevel == RiskLevel.HIGH) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = "تنبيه", tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مخاطرة ${idea.riskLevel.name}: ${idea.disclaimer ?: "يحتاج مراجعة بشرية لمصادره"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row {
                    IconButton(onClick = onReject) {
                        Icon(Icons.Default.Delete, contentDescription = "رفض", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onReport) {
                        Icon(Icons.Default.Warning, contentDescription = "إبلاغ", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                Button(onClick = onConvert) {
                    Text("بدء المشروع من هذه الفكرة")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownOption(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = {
                        onSelected(opt)
                        expanded = false
                    }
                )
            }
        }
    }
}
