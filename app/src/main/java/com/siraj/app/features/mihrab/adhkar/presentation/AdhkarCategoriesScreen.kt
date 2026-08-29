package com.siraj.app.features.mihrab.adhkar.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.core.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdhkarCategoriesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCategory: (String, String) -> Unit
) {
    val viewModel: AdhkarCategoriesViewModel = viewModel()
    val categoriesState by viewModel.categories.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الأذكار المعتمدة") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = androidx.compose.ui.res.stringResource(com.siraj.app.R.string.back)) }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = categoriesState) {
                is Resource.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is Resource.Error -> Text(state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                is Resource.Success -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.data) { category ->
                            CategoryCard(
                                title = category.name,
                                icon = getIconForName(category.iconName),
                                onClick = { onNavigateToCategory(category.id, category.name) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryCard(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

fun getIconForName(name: String): ImageVector {
    return when (name) {
        "wb_sunny" -> Icons.Default.WbSunny
        "nights_stay" -> Icons.Default.NightsStay
        "mosque" -> Icons.Default.Home // Fallback icon
        "bedtime" -> Icons.Default.Bedtime
        "alarm" -> Icons.Default.Alarm
        "flight" -> Icons.Default.Flight
        "restaurant" -> Icons.Default.Restaurant
        else -> Icons.Default.List
    }
}
