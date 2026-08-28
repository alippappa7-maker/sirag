package com.siraj.app.features.review.presentation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siraj.app.domain.models.review.*
import com.siraj.app.features.review.presentation.components.ReviewQueueItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShariaReviewQueueScreen(
    viewModel: ShariaReviewViewModel,
    currentUserRole: String,
    currentUserId: String,
    onNavigateToItemDetail: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var searchQuery by remember { mutableStateOf("") }
    var selectedRisk by remember { mutableStateOf<RiskLevel?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedStatus by remember { mutableStateOf<ShariaReviewStatus?>(null) }
    var selectedCriticalTopic by remember { mutableStateOf<CriticalTopic?>(null) }
    var sortByDateAsc by remember { mutableStateOf(false) }

    val categories = listOf("القرآن وعلومه", "الحديث الشريف", "الفقه وأصوله", "العقيدة", "المعاملات المالية", "الأسرة")

    LaunchedEffect(state.errorMessage, state.successMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "لوحة المراجعة الشرعية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "فحص وتوثيق النصوص والمصادر والادعاءات",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_back_review_queue")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            sortByDateAsc = !sortByDateAsc
                            viewModel.updateFilter(
                                state.activeFilter.copy(sortByDateAscending = sortByDateAsc)
                            )
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "ترتيب",
                            tint = if (sortByDateAsc) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.updateFilter(state.activeFilter.copy(searchQuery = it))
                },
                placeholder = { Text("ابحث في عناوين المحتوى، النصوص، أو المراجع...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            viewModel.updateFilter(state.activeFilter.copy(searchQuery = ""))
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_review_queue")
            )

            // Horizontal Filters Row 1: Risk Level
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "مستوى الخطر:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Bold
                )

                FilterChip(
                    selected = selectedRisk == null,
                    onClick = {
                        selectedRisk = null
                        viewModel.updateFilter(state.activeFilter.copy(riskLevel = null))
                    },
                    label = { Text("الكل") }
                )

                RiskLevel.values().forEach { risk ->
                    FilterChip(
                        selected = selectedRisk == risk,
                        onClick = {
                            selectedRisk = if (selectedRisk == risk) null else risk
                            viewModel.updateFilter(state.activeFilter.copy(riskLevel = selectedRisk))
                        },
                        label = { Text(risk.arabicTitle) }
                    )
                }
            }

            // Horizontal Filters Row 2: Categories & Critical Topics
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "القسم الشرعي:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Bold
                )

                FilterChip(
                    selected = selectedCategory == null,
                    onClick = {
                        selectedCategory = null
                        viewModel.updateFilter(state.activeFilter.copy(category = null))
                    },
                    label = { Text("جميع الأقسام") }
                )

                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = {
                            selectedCategory = if (selectedCategory == cat) null else cat
                            viewModel.updateFilter(state.activeFilter.copy(category = selectedCategory))
                        },
                        label = { Text(cat) }
                    )
                }
            }

            // Status Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الحالة:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Bold
                )

                FilterChip(
                    selected = selectedStatus == null,
                    onClick = {
                        selectedStatus = null
                        viewModel.updateFilter(state.activeFilter.copy(status = null))
                    },
                    label = { Text("الكل") }
                )

                ShariaReviewStatus.values().forEach { st ->
                    FilterChip(
                        selected = selectedStatus == st,
                        onClick = {
                            selectedStatus = if (selectedStatus == st) null else st
                            viewModel.updateFilter(state.activeFilter.copy(status = selectedStatus))
                        },
                        label = { Text(st.arabicTitle) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Queue Content List
            Box(modifier = Modifier.fillMaxSize()) {
                if (state.isLoading && state.queueItems.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (state.queueItems.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.FactCheck,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "لا توجد عناصر مراجعة مطابقة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "تم فحص كافة العناصر أو لا توجد مواد تطابق خيارات التصفية الحالية.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "قائمة الانتظار (${state.queueItems.size} عناصر جاهزة للمراجعة):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        items(state.queueItems, key = { it.id }) { item ->
                            ReviewQueueItemCard(
                                item = item,
                                onClick = { onNavigateToItemDetail(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
