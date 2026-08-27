package com.siraj.app.features.project.presentation.assets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.Asset
import com.siraj.app.domain.models.AssetType
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetLibraryScreen(
    projectId: String,
    onNavigateBack: () -> Unit,
    onNavigateToSearch: () -> Unit,
    viewModel: AssetLibraryViewModel = viewModel(factory = AssetLibraryViewModelFactory(projectId))
) {
    val assetsState by viewModel.assetsState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showUploadDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("مدير الوسائط") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "عودة") }
                },
                actions = {
                    IconButton(onClick = onNavigateToSearch) { Icon(Icons.Default.Search, "بحث عن وسائط خارجية") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showUploadDialog = true }) {
                Icon(Icons.Default.Add, "إضافة أصل")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = assetsState) {
                is Resource.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is Resource.Error -> Text(state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                is Resource.Success -> {
                    val assets = state.data
                    if (assets.isEmpty()) {
                        Text("مكتبة الوسائط فارغة", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(150.dp),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(assets) { asset ->
                                AssetCard(
                                    asset = asset,
                                    onDelete = { viewModel.deleteAsset(asset) },
                                    onEdit = { /* Placeholder for edit metadata */ }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showUploadDialog) {
            UploadAssetDialog(
                onDismiss = { showUploadDialog = false },
                onUpload = { name, type, source, license, attr ->
                    viewModel.uploadMockAsset(name, type, source, license, attr)
                    showUploadDialog = false
                }
            )
        }
    }
}

@Composable
fun AssetCard(
    asset: Asset,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth().background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                val icon = when (asset.type) {
                    AssetType.IMAGE -> Icons.Default.Image
                    AssetType.VIDEO -> Icons.Default.VideoFile
                    AssetType.AUDIO -> Icons.Default.AudioFile
                    else -> Icons.Default.Image
                }
                Icon(icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.White)
            }
            Row(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("نوع: ${asset.type.name}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Text(if (asset.sizeBytes > 0) "${asset.sizeBytes / 1024} KB" else "", style = MaterialTheme.typography.labelSmall)
                }
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Edit, "تعديل", modifier = Modifier.size(16.dp)) }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Delete, "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun UploadAssetDialog(
    onDismiss: () -> Unit,
    onUpload: (name: String, type: AssetType, source: String, license: String, attribution: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(AssetType.IMAGE) }
    var source by remember { mutableStateOf("") }
    var license by remember { mutableStateOf("") }
    var attribution by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("رفع وسائط جديدة") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("اسم الملف (للمحاكاة)") })
                
                // Type selection (Simple row of chips for brevity)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = type == AssetType.IMAGE, onClick = { type = AssetType.IMAGE }, label = { Text("صورة") })
                    FilterChip(selected = type == AssetType.VIDEO, onClick = { type = AssetType.VIDEO }, label = { Text("فيديو") })
                    FilterChip(selected = type == AssetType.AUDIO, onClick = { type = AssetType.AUDIO }, label = { Text("صوت") })
                }
                
                OutlinedTextField(value = source, onValueChange = { source = it }, label = { Text("المصدر (رابط/اسم)") })
                OutlinedTextField(value = license, onValueChange = { license = it }, label = { Text("الترخيص (مثال: CC-BY, مرخص)") })
                OutlinedTextField(value = attribution, onValueChange = { attribution = it }, label = { Text("نص النسبة (Attribution)") })
                
                Text("سيتم التحقق من الحجم والنوع برمجياً", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            Button(onClick = { onUpload(name.ifEmpty { "ملف" }, type, source, license, attribution) }) {
                Text("رفع")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
