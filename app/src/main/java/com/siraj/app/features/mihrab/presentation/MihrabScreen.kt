package com.siraj.app.features.mihrab.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.core.ui.components.SirajTextField
import com.siraj.app.core.ui.components.SirajTechCard
import com.siraj.app.core.ui.components.SirajGlowContainer
import com.siraj.app.ui.theme.LocalSpacing

@Composable
fun MihrabScreen(
    onNavigateToQuran: () -> Unit = {},
    onNavigateToPrayerTimes: () -> Unit = {},
    onNavigateToQibla: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToAdhkar: () -> Unit = {},

    viewModel: MihrabViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    // Using a composition local provider to ensure RTL is respected strictly for this section if needed, 
    // though the app wide should be RTL. We'll rely on the app's default RTL.

    Scaffold(
        topBar = {
            MihrabTopBar()
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                state.error != null -> {
                    ErrorState(
                        message = state.error ?: "",
                        onRetry = { viewModel.retry() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.isOffline -> {
                    ErrorState(
                        message = "أنت في وضع عدم الاتصال. يتم عرض المحتوى المحفوظ فقط.",
                        onRetry = { viewModel.retry() },
                        icon = Icons.Default.SignalWifiOff,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    MihrabContent(
                        state = state,
                        onSearchQueryChanged = viewModel::onSearchQueryChanged,
                        onNavigateToQuran = onNavigateToQuran,
                        onNavigateToPrayerTimes = onNavigateToPrayerTimes,
                        onNavigateToQibla = onNavigateToQibla,
                        onNavigateToCalendar = onNavigateToCalendar,
                        onNavigateToAdhkar = onNavigateToAdhkar
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MihrabTopBar() {
    TopAppBar(
        title = {
            Text(
                text = "المحراب",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        actions = {
            IconButton(onClick = { /* TODO: Navigate to Mihrab Settings */ }) {
                Icon(Icons.Default.Settings, contentDescription = "إعدادات المحراب")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun MihrabContent(
    state: MihrabState,
    onSearchQueryChanged: (String) -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateToPrayerTimes: () -> Unit,
    onNavigateToQibla: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToAdhkar: () -> Unit
) {
    val spacing = LocalSpacing.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = spacing.medium,
            top = spacing.medium,
            end = spacing.medium,
            bottom = 120.dp // Prevent MiniPlayer overlap
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.large)
    ) {
        // Search
        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = { Text("ابحث في السور، التفاسير، الأذكار...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = androidx.compose.ui.res.stringResource(com.siraj.app.R.string.search))
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                )
            )
        }

        // Last Read / Listened
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                state.lastRead?.let { lastRead ->
                    LastActionCard(
                        title = "متابعة القراءة",
                        subtitle = "${lastRead.title} - ${lastRead.subtitle}",
                        icon = Icons.Default.MenuBook,
                        modifier = Modifier.weight(1f),
                        onClick = { /* TODO */ }
                    )
                }
                state.lastListened?.let { lastListened ->
                    LastActionCard(
                        title = "متابعة الاستماع",
                        subtitle = "${lastListened.title}\n${lastListened.reciter}",
                        icon = Icons.Default.Headset,
                        modifier = Modifier.weight(1f),
                        onClick = { /* TODO */ }
                    )
                }
            }
        }

        // Shortcuts (Daily)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "وصول سريع",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.shortcuts) { shortcut ->
                        ShortcutCard(
                            shortcut = shortcut,
                            onClick = { 
                                when (shortcut.id) {
                                    "prayer_times" -> onNavigateToPrayerTimes()
                                    "qibla" -> onNavigateToQibla()
                                    "hijri_calendar" -> onNavigateToCalendar()
                                    "adhkar" -> onNavigateToAdhkar()
                                }
                            }
                        )
                    }
                }
            }
        }

        // Main Sections
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "الأقسام",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                // Grid representation using Rows for simplicity
                val chunkedSections = state.sections.chunked(2)
                chunkedSections.forEach { rowSections ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowSections.forEach { section ->
                            SectionCard(
                                section = section,
                                modifier = Modifier.weight(1f),
                                onClick = { 
                                    if (section.id == "quran") {
                                        onNavigateToQuran()
                                    } else {
                                        /* TODO */ 
                                    }
                                }
                            )
                        }
                        // Fill empty space if odd number
                        if (rowSections.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        
        item {
             Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun LastActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    SirajTechCard(
        isActive = true, // Highlight as active/last action
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ShortcutCard(
    shortcut: ShortcutItem,
    onClick: () -> Unit
) {
    val icon = when (shortcut.iconName) {
        "book" -> Icons.Default.Book
        "schedule" -> Icons.Default.Schedule
        "explore" -> Icons.Default.Explore
        "calendar_today" -> Icons.Default.CalendarToday
        else -> Icons.Default.Star
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SirajGlowContainer(
            modifier = Modifier.size(56.dp),
            isActive = true,
            glowColor = MaterialTheme.colorScheme.tertiary
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = shortcut.title,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Text(
            text = shortcut.title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun SectionCard(
    section: MihrabSection,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val icon = when (section.iconName) {
        "menu_book" -> Icons.Default.MenuBook
        "headset" -> Icons.Default.Headset
        "library_books" -> Icons.Default.LibraryBooks
        "bookmark" -> Icons.Default.Bookmark
        else -> Icons.Default.Folder
    }

    SirajTechCard(
        isActive = false,
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.ErrorOutline
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Button(onClick = onRetry) {
            Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.retry))
        }
    }
}
