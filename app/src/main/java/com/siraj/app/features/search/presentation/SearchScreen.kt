package com.siraj.app.features.search.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.siraj.app.core.ui.components.SirajTechCard
import com.siraj.app.domain.models.search.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToResult: (SearchResultItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    // Detect scrolling to bottom for pagination
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null &&
                    lastVisibleIndex >= uiState.searchResult.items.size - 3 &&
                    uiState.searchResult.hasMore &&
                    !uiState.isLoadingMore
                ) {
                    viewModel.loadNextPage()
                }
            }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // Main Search Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("search_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = androidx.compose.ui.res.stringResource(com.siraj.app.R.string.back)
                        )
                    }

                    // Search TextField
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = { viewModel.onQueryChanged(it) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_input_field"),
                        placeholder = {
                            Text(
                                text = "ابحث في القرآن، الصوتيات، الومضات، القوالب والمصادر...",
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = androidx.compose.ui.res.stringResource(com.siraj.app.R.string.search),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (uiState.query.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.onQueryChanged("") },
                                    modifier = Modifier.testTag("search_clear_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "مسح النص"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(28.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                focusManager.clearFocus()
                                viewModel.onSearchSubmitted()
                            }
                        )
                    )

                    Spacer(Modifier.width(6.dp))

                    // Filter Button with Badge
                    BadgedBox(
                        badge = {
                            if (uiState.activeFilterCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Text(uiState.activeFilterCount.toString())
                                }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = { viewModel.setFilterSheetOpen(true) },
                            modifier = Modifier.testTag("search_filter_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "تصفية وفرز",
                                tint = if (uiState.activeFilterCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Category Chips Row
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SearchCategory.values()) { category ->
                        val isSelected = uiState.filter.category == category
                        val count = uiState.searchResult.categoryCounts[category]

                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onCategorySelected(category) },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(category.titleArabic)
                                    if (count != null && count > 0 && uiState.hasSearched) {
                                        Spacer(Modifier.width(4.dp))
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = count.toString(),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("category_chip_${category.name}")
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        },
        modifier = modifier.testTag("search_screen")
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // 1. Loading State
                uiState.isLoading -> {
                    SearchLoadingSkeleton()
                }

                // 2. Error State
                uiState.errorMessage != null -> {
                    SearchErrorView(
                        errorMessage = uiState.errorMessage ?: "",
                        onRetry = { viewModel.onSearchSubmitted() }
                    )
                }

                // 3. Search Results State (when query is active and results found)
                uiState.hasSearched && uiState.searchResult.items.isNotEmpty() -> {
                    SearchResultsList(
                        results = uiState.searchResult,
                        isLoadingMore = uiState.isLoadingMore,
                        listState = listState,
                        onItemClick = { item ->
                            focusManager.clearFocus()
                            onNavigateToResult(item)
                        }
                    )
                }

                // 4. No Results Found State
                uiState.hasSearched && uiState.searchResult.items.isEmpty() && uiState.query.isNotBlank() -> {
                    SearchNoResultsView(
                        query = uiState.query,
                        onResetFilters = { viewModel.resetFilters() }
                    )
                }

                // 5. Suggestions State (while user is typing but hasn't searched or when query is short)
                uiState.query.isNotBlank() && uiState.suggestions.isNotEmpty() -> {
                    SearchSuggestionsList(
                        suggestions = uiState.suggestions,
                        onSuggestionClick = { suggestion ->
                            focusManager.clearFocus()
                            viewModel.onSearchSubmitted(suggestion.text)
                        }
                    )
                }

                // 6. Idle / Initial State (Search History & Popular Suggestions)
                else -> {
                    SearchIdleView(
                        history = uiState.history,
                        suggestions = uiState.suggestions,
                        onHistoryClick = { query ->
                            focusManager.clearFocus()
                            viewModel.onSearchSubmitted(query)
                        },
                        onDeleteHistoryItem = { id ->
                            viewModel.deleteHistoryItem(id)
                        },
                        onClearAllHistory = {
                            viewModel.clearAllHistory()
                        },
                        onSuggestionClick = { query ->
                            focusManager.clearFocus()
                            viewModel.onSearchSubmitted(query)
                        }
                    )
                }
            }
        }

        // Filter Bottom Sheet
        if (uiState.isFilterSheetOpen) {
            SearchFilterBottomSheet(
                currentFilter = uiState.filter,
                onApplyFilter = { newFilter -> viewModel.onFilterUpdated(newFilter) },
                onResetFilter = { viewModel.resetFilters() },
                onDismiss = { viewModel.setFilterSheetOpen(false) }
            )
        }
    }
}

@Composable
fun SearchResultsList(
    results: GlobalSearchResult,
    isLoadingMore: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onItemClick: (SearchResultItem) -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تم العثور على (${results.totalCount}) نتيجة",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        items(results.items, key = { it.id }) { item ->
            SearchResultCard(
                item = item,
                onClick = { onItemClick(item) }
            )
        }

        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

@Composable
fun SearchIdleView(
    history: List<SearchHistoryItem>,
    suggestions: List<SearchSuggestion>,
    onHistoryClick: (String) -> Unit,
    onDeleteHistoryItem: (String) -> Unit,
    onClearAllHistory: () -> Unit,
    onSuggestionClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Recent Search History
        if (history.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "عمليات البحث الأخيرة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(onClick = onClearAllHistory) {
                        Text(
                            text = "مسح السجل",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            items(history, key = { it.id }) { historyItem ->
                SirajTechCard(
                    isActive = false,
                    onClick = { onHistoryClick(historyItem.query) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = historyItem.query,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = { onDeleteHistoryItem(historyItem.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = androidx.compose.ui.res.stringResource(com.siraj.app.R.string.delete),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Popular & Trending Topics
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "مواضيع مقترحة وشائعة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            val popularTopics = listOf(
                "سورة الكهف",
                "أذكار الصباح",
                "آية الكرسي",
                "صحيح البخاري",
                "تفسير ابن كثير",
                "سورة الملك",
                "فضل الاستغفار",
                "شرح الأربعين النووية",
                "قوالب ريلز إسلامية",
                "أذكار المساء",
                "رياض الصالحين"
            )

            OptInFlowRow(popularTopics, onSuggestionClick)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OptInFlowRow(
    topics: List<String>,
    onTopicClick: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        topics.forEach { topic ->
            SuggestionChip(
                onClick = { onTopicClick(topic) },
                label = { Text(topic) },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun SearchSuggestionsList(
    suggestions: List<SearchSuggestion>,
    onSuggestionClick: (SearchSuggestion) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(suggestions) { suggestion ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSuggestionClick(suggestion) }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (suggestion.category) {
                        SearchCategory.QURAN -> Icons.Default.MenuBook
                        SearchCategory.AUDIO -> Icons.Default.Headphones
                        SearchCategory.FLASH -> Icons.Default.Bolt
                        SearchCategory.SOURCE -> Icons.Default.LocalLibrary
                        else -> Icons.Default.Search
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = suggestion.text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.NorthWest,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        }
    }
}

@Composable
fun SearchNoResultsView(
    query: String,
    onResetFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.SearchOff,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "لم يتم العثور على نتائج لـ \"$query\"",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "تأكد من كتابة الكلمات بشكل صحيح، أو جرّب البحث بكلمات أخرى أو إعادة ضبط الفلاتر.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            onClick = onResetFilters,
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("إعادة ضبط الفلاتر والبحث في الكل")
        }
    }
}

@Composable
fun SearchLoadingSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(4) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun SearchErrorView(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onRetry) {
            Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.retry))
        }
    }
}
