package com.siraj.app.features.project.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.Project
import com.siraj.app.domain.models.ContentBrief

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import com.siraj.app.domain.models.ContentTemplate


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectEditorScreen(
    projectId: String,
    onNavigateBack: () -> Unit,
    onNavigateToPlan: (String) -> Unit,
    onNavigateToAssetLibrary: (String) -> Unit,
    viewModel: ProjectEditorViewModel = viewModel(factory = ProjectEditorViewModelFactory(projectId))
) {
    val projectState by viewModel.projectState.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showTemplateBrowser by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = when(projectState) {
                            is Resource.Success -> "استوديو المحتوى"
                            else -> "جاري التحميل..."
                        }
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "عودة")
                    }
                },
                actions = {
                    when(saveState) {
                        is SaveState.Saving -> Text("جاري الحفظ...", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(end = 16.dp))
                        is SaveState.Saved -> Text("مسودة محفوظة", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 16.dp))
                        is SaveState.Error -> Text("خطأ في الحفظ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(end = 16.dp))
                        is SaveState.Idle -> {}
                    }
                    IconButton(onClick = { onNavigateToAssetLibrary(projectId) }) {
                        Icon(Icons.Default.Search, contentDescription = "مدير الوسائط")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "حذف المشروع", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        
        if (showTemplateBrowser) {
            TemplateBrowserDialog(
                viewModel = viewModel,
                onDismiss = { showTemplateBrowser = false },
                onSelect = { template ->
                    viewModel.applyTemplate(template)
                    showTemplateBrowser = false
                }
            )
        }

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
                    TextButton(onClick = { showDeleteDialog = false }) { Text("إلغاء") }
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
                    StudioForm(
                        project = project,
                        onTitleChange = viewModel::updateTitle,
                        onBriefChange = viewModel::updateBrief,
                        onGeneratePlan = { 
                            viewModel.generatePlan { onNavigateToPlan(projectId) } 
                        },
                        onBrowseTemplates = { showTemplateBrowser = true }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioForm(
    project: Project,
    onTitleChange: (String) -> Unit,
    onBriefChange: ((ContentBrief) -> ContentBrief) -> Unit,
    onGeneratePlan: () -> Unit,
    onBrowseTemplates: () -> Unit
) {
    val scrollState = rememberScrollState()
    val brief = project.brief
    
    val wordCount = brief.idea.split("\\s+".toRegex()).count { it.isNotBlank() }
    val charCount = brief.idea.length
    
    // Check for religious keywords for the warning
    val religiousKeywords = listOf("حلال", "حرام", "فتوى", "حكم", "قال رسول الله", "يجوز", "لا يجوز", "بدعة", "سنة")
    val hasReligiousClaim = religiousKeywords.any { brief.idea.contains(it) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 0. Template Selection
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("القالب المختار: ${brief.template}", fontWeight = FontWeight.Bold)
            OutlinedButton(onClick = onBrowseTemplates) {
                Text("تصفح القوالب")
            }
        }
        Divider()

        // 1. Basic Info
        OutlinedTextField(
            value = project.title,
            onValueChange = onTitleChange,
            label = { Text("عنوان المشروع") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        OutlinedTextField(
            value = brief.idea,
            onValueChange = { newIdea -> onBriefChange { it.copy(idea = newIdea) } },
            label = { Text("فكرة المحتوى أو النص المبدئي") },
            modifier = Modifier.fillMaxWidth().height(150.dp),
            supportingText = { Text("الكلمات: $wordCount | الأحرف: $charCount") }
        )
        
        if (hasReligiousClaim && !brief.hasFatwa && !brief.hasHadith) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = "تحذير", tint = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "يبدو أن المحتوى يتضمن ادعاءات دينية أو فتاوى. يُرجى توفير المصدر أو تحديد العلامات المناسبة أدناه.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Divider()
        
        // 2. Selection Settings
        Text("إعدادات الإنتاج", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DropdownSelector(
                label = "نوع المحتوى",
                options = listOf("فيديو", "صوت", "نص", "صورة"),
                selected = brief.contentType,
                onSelected = { sel -> onBriefChange { it.copy(contentType = sel) } },
                modifier = Modifier.weight(1f)
            )
            DropdownSelector(
                label = "المنصة / المقاس",
                options = listOf("TikTok / Reels (9:16)", "YouTube (16:9)", "Instagram Post (1:1)", "قصير (صوتي)"),
                selected = brief.platform,
                onSelected = { sel -> onBriefChange { it.copy(platform = sel) } },
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DropdownSelector(
                label = "الجمهور المستهدف",
                options = listOf("عام", "أطفال", "شباب", "أكاديمي/متخصص"),
                selected = brief.targetAudience,
                onSelected = { sel -> onBriefChange { it.copy(targetAudience = sel) } },
                modifier = Modifier.weight(1f)
            )
            DropdownSelector(
                label = "اللغة واللهجة",
                options = listOf("العربية الفصحى", "لهجة خليجية", "لهجة مصرية", "لهجة شامية"),
                selected = brief.language,
                onSelected = { sel -> onBriefChange { it.copy(language = sel) } },
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DropdownSelector(
                label = "الأسلوب البصري",
                options = listOf("موشن جرافيك", "تصوير حي", "وايت بورد", "نصي فقط"),
                selected = brief.visualStyle,
                onSelected = { sel -> onBriefChange { it.copy(visualStyle = sel) } },
                modifier = Modifier.weight(1f)
            )
            DropdownSelector(
                label = "المدة التقديرية",
                options = listOf("قصير (أقل من دقيقة)", "متوسط (1-3 دقائق)", "طويل (أكثر من 3 دقائق)"),
                selected = brief.duration,
                onSelected = { sel -> onBriefChange { it.copy(duration = sel) } },
                modifier = Modifier.weight(1f)
            )
        }
        
        Divider()
        
        // 3. Religious Context Toggles
        Text("تصنيفات المحتوى الإسلامي (تتطلب مصادر للاعتماد)", style = MaterialTheme.typography.titleSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = brief.hasQuran, onCheckedChange = { chk -> onBriefChange { it.copy(hasQuran = chk) } })
            Text("يتضمن آيات قرآنية")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = brief.hasHadith, onCheckedChange = { chk -> onBriefChange { it.copy(hasHadith = chk) } })
            Text("يتضمن أحاديث نبوية")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = brief.hasFatwa, onCheckedChange = { chk -> onBriefChange { it.copy(hasFatwa = chk) } })
            Text("يتضمن فتوى أو حكم شرعي")
        }
        
        Divider()
        
        // 4. Preview & Estimate
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("التكلفة التقديرية للإنتاج (بالذكاء الاصطناعي)", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                val estimatedCost = if (brief.duration.contains("قصير")) "0.05$" else "0.15$"
                Text("حوالي $estimatedCost (تقديري وغير ملزم)", fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = onGeneratePlan,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = project.title.isNotBlank() && brief.idea.isNotBlank()
        ) {
            Text("إنشاء خطة المحتوى")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateBrowserDialog(
    viewModel: ProjectEditorViewModel,
    onDismiss: () -> Unit,
    onSelect: (ContentTemplate) -> Unit
) {
    val templatesState by viewModel.templates.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyFavorites by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text("مكتبة القوالب") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "إغلاق") }
                    }
                )
                
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("بحث في القوالب") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = showOnlyFavorites, onCheckedChange = { showOnlyFavorites = it })
                        Text("عرض المفضلة فقط")
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    when (templatesState) {
                        is Resource.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        is Resource.Error -> Text((templatesState as Resource.Error).message, color = MaterialTheme.colorScheme.error)
                        is Resource.Success -> {
                            val templates = (templatesState as Resource.Success).data
                            val filtered = templates.filter { 
                                it.name.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true)
                            }.filter {
                                if (showOnlyFavorites) favorites.contains(it.id) else true
                            }
                            
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(filtered) { tpl ->
                                    TemplateCard(
                                        template = tpl,
                                        isFavorite = favorites.contains(tpl.id),
                                        onToggleFavorite = { viewModel.toggleFavorite(tpl.id) },
                                        onSelect = { onSelect(tpl) }
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
fun TemplateCard(
    template: ContentTemplate,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onSelect: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(template.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "مفضلة",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(template.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(template.targetAudience) })
                AssistChip(onClick = {}, label = { Text(template.recommendedPlatform.split(" ")[0]) })
                AssistChip(onClick = {}, label = { Text(template.sceneStyle) })
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onSelect, modifier = Modifier.align(Alignment.End)) {
                Text("استخدام القالب")
            }
        }
    }
}
