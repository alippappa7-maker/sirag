package com.siraj.app.features.project.presentation.assets

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.ExternalMediaItem
import com.siraj.app.domain.models.MediaType
import com.siraj.app.domain.models.MediaOrientation
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExternalMediaSearchScreen(
    projectId: String,
    onNavigateBack: () -> Unit,
    viewModel: ExternalMediaSearchViewModel = viewModel(factory = ExternalMediaSearchViewModelFactory(projectId))
) {
    val query by viewModel.query.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val items by viewModel.items.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedItem by remember { mutableStateOf<ExternalMediaItem?>(null) }
    
    val listState = rememberLazyGridState()
    
    LaunchedEffect(Unit) {
        viewModel.uiMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }
    
    // Load more when scrolled to bottom
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && items.isNotEmpty() && lastIndex >= items.size - 4) {
                    viewModel.loadMore()
                }
            }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("البحث الخارجي") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "عودة") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Search Bar
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.updateQuery(it) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("بحث عن صور أو فيديو...") },
                trailingIcon = {
                    IconButton(onClick = { viewModel.performSearch() }) {
                        Icon(Icons.Default.Search, "بحث")
                    }
                },
                singleLine = true
            )
            
            // Filters
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filter.type == MediaType.IMAGE,
                    onClick = { viewModel.updateFilter(filter.copy(type = MediaType.IMAGE)) },
                    label = { Text("صور") }
                )
                FilterChip(
                    selected = filter.type == MediaType.VIDEO,
                    onClick = { viewModel.updateFilter(filter.copy(type = MediaType.VIDEO)) },
                    label = { Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.video)) }
                )
                FilterChip(
                    selected = filter.orientation != MediaOrientation.ALL,
                    onClick = { 
                        val newOri = if (filter.orientation == MediaOrientation.ALL) MediaOrientation.LANDSCAPE else MediaOrientation.ALL
                        viewModel.updateFilter(filter.copy(orientation = newOri)) 
                    },
                    label = { Text(if (filter.orientation == MediaOrientation.ALL) "الاتجاه" else "أفقي") }
                )
                if (filter.type == MediaType.IMAGE) {
                    FilterChip(
                        selected = filter.color != null,
                        onClick = { viewModel.updateFilter(filter.copy(color = if (filter.color == null) "أزرق" else null)) },
                        label = { Text(filter.color ?: "اللون") }
                    )
                } else {
                    FilterChip(
                        selected = filter.maxDurationMs != null,
                        onClick = { viewModel.updateFilter(filter.copy(maxDurationMs = if (filter.maxDurationMs == null) 60000L else null)) },
                        label = { Text(if (filter.maxDurationMs != null) "< 1m" else "المدة") }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Results
            Box(modifier = Modifier.fillMaxSize()) {
                if (items.isEmpty() && searchState is Resource.Success) {
                    Text(
                        "لا توجد نتائج، يرجى تجربة كلمات مختلفة.",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(150.dp),
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items) { item ->
                            MediaItemCard(item = item, onClick = { selectedItem = item })
                        }
                        
                        if (searchState is Resource.Loading) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
                
                if (searchState is Resource.Error && items.isEmpty()) {
                    Text(
                        (searchState as Resource.Error).message,
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        
        // Asset Details Dialog
        selectedItem?.let { item ->
            AssetDetailsDialog(
                item = item,
                onDismiss = { selectedItem = null },
                onAdd = { 
                    viewModel.addAssetToProject(item)
                    selectedItem = null
                }
            )
        }
    }
}

@Composable
fun MediaItemCard(item: ExternalMediaItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(1f).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Text(item.title, color = Color.White, style = MaterialTheme.typography.bodySmall)
            }
            
            // License badge
            Surface(
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = item.licenseName,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun AssetDetailsDialog(
    item: ExternalMediaItem,
    onDismiss: () -> Unit,
    onAdd: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تفاصيل الوسائط") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp).background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("معاينة (Preview)")
                }
                Text("الاسم: ${item.title}", fontWeight = FontWeight.Bold)
                Text("النوع: ${if (item.type == MediaType.VIDEO) "فيديو" else "صورة"}")
                Text("المصدر: ${item.sourceUrl}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Text("المالك: ${item.creatorName}")
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.medium) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("الترخيص والإسناد", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                        Text("الترخيص: ${item.licenseName}")
                        Text("الاستخدام التجاري: ${if (item.commercialUseAllowed) "مسموح" else "غير مسموح"}")
                        if (item.attributionRequired) {
                            Text("نص الإسناد المطلوب:", fontWeight = FontWeight.Bold)
                            Text(item.attributionText, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("إضافة للمشروع")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.cancel)) }
        }
    )
}
