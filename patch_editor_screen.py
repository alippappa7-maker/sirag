import re

with open('app/src/main/java/com/siraj/app/features/project/presentation/ProjectEditorScreen.kt', 'r') as f:
    content = f.read()

# Add imports
imports = """
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import com.siraj.app.domain.models.ContentTemplate
"""
content = content.replace('import com.siraj.app.domain.models.ContentBrief', 'import com.siraj.app.domain.models.ContentBrief\n' + imports)

# Add state to ProjectEditorScreen
content = content.replace('var showDeleteDialog by remember { mutableStateOf(false) }', 'var showDeleteDialog by remember { mutableStateOf(false) }\n    var showTemplateBrowser by remember { mutableStateOf(false) }')

# Add bottom sheet or dialog call
browser_call = """
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
"""
content = content.replace('if (showDeleteDialog) {', browser_call + '\n        if (showDeleteDialog) {')

# Add "Browse Templates" button in StudioForm
form_button = """
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("القالب المختار: ${brief.template}", fontWeight = FontWeight.Bold)
            OutlinedButton(onClick = onBrowseTemplates) {
                Text("تصفح القوالب")
            }
        }
        Divider()
"""

# Find where to put it in StudioForm
content = content.replace(
    'fun StudioForm(\n    project: Project,\n    onTitleChange: (String) -> Unit,\n    onBriefChange: ((ContentBrief) -> ContentBrief) -> Unit,\n    onGeneratePlan: () -> Unit\n) {',
    'fun StudioForm(\n    project: Project,\n    onTitleChange: (String) -> Unit,\n    onBriefChange: ((ContentBrief) -> ContentBrief) -> Unit,\n    onGeneratePlan: () -> Unit,\n    onBrowseTemplates: () -> Unit\n) {'
)

content = content.replace(
    'onGeneratePlan = { viewModel.generatePlan(onNavigateBack) }',
    'onGeneratePlan = { viewModel.generatePlan(onNavigateBack) },\n                        onBrowseTemplates = { showTemplateBrowser = true }'
)

content = content.replace(
    '// 1. Basic Info',
    '// 0. Template Selection\n        ' + form_button + '\n        // 1. Basic Info'
)


# Add TemplateBrowserDialog component at the end
browser_ui = """
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
"""
content += "\n" + browser_ui

# Add androidx.compose.ui.window.Dialog import
content = content.replace('import androidx.compose.ui.unit.dp', 'import androidx.compose.ui.unit.dp\nimport androidx.compose.ui.window.Dialog')

with open('app/src/main/java/com/siraj/app/features/project/presentation/ProjectEditorScreen.kt', 'w') as f:
    f.write(content)
