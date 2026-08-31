package com.siraj.app.features.beta.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.domain.models.beta.*
import com.siraj.app.ui.theme.statusColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefectTriageScreen(
    onNavigateBack: () -> Unit,
    viewModel: DefectTriageViewModel = viewModel(factory = DefectTriageViewModelFactory()),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissUserMessage()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { err ->
            snackbarHostState.showSnackbar(err)
            viewModel.dismissUserMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "إدارة العيوب وفرز الملاحظات",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "تحليل ملاحظات النسخة التجريبية (Beta Defect Triage)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("triage_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. ملخص إحصائي لفرز العيوب
                item {
                    TriageSummaryCard(summary = uiState.summary)
                }

                // 2. تنبيه الضوابط الشرعية الصارمة
                item {
                    ShariaContentRuleAlert()
                }

                // 3. شريط البحث والفرز المرتب
                item {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("defect_search_input"),
                        placeholder = { Text("بحث بالرقم، الوصف، الجهاز، أو المسؤول...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "مسح")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // 4. فلاتر التصنيف الثمانية
                item {
                    ClassificationFilterChips(
                        selected = uiState.selectedClassification,
                        onSelect = { viewModel.onSelectClassificationFilter(it) }
                    )
                }

                // 5. فلاتر نطاق الاختصاص ومجال العمل
                item {
                    DomainFilterChips(
                        selected = uiState.selectedDomain,
                        onSelect = { viewModel.onSelectDomainFilter(it) }
                    )
                }

                // 6. مفتاح تبديل القائمة المرتبة بالأولويات
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ترتيب العمل حسب الأولوية (P0 → P3)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Switch(
                            checked = uiState.isPrioritizedView,
                            onCheckedChange = { viewModel.togglePrioritizedView(it) },
                            modifier = Modifier.testTag("prioritized_view_switch")
                        )
                    }
                }

                // 7. قائمة بطاقات العيوب
                if (uiState.filteredDefects.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircleOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "لا توجد ملاحظات أو عيوب مطابقة للمحددات الحالية",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.filteredDefects, key = { it.id }) { defect ->
                        DefectCardItem(
                            defect = defect,
                            onViewDetails = { viewModel.selectDefectForDetails(defect) },
                            onOpenTriage = { viewModel.openTriageDialog(defect) },
                            onOpenStatus = { viewModel.openStatusDialog(defect) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Modal Sheet لتفاصيل العيب الشاملة
    uiState.selectedDefect?.let { defect ->
        if (!uiState.isTriageDialogOpen && !uiState.isStatusDialogOpen) {
            DefectDetailDialog(
                defect = defect,
                onDismiss = { viewModel.selectDefectForDetails(null) },
                onOpenTriage = { viewModel.openTriageDialog(defect) },
                onOpenStatus = { viewModel.openStatusDialog(defect) }
            )
        }
    }

    // Dialog لتعديل وتصنيف الفرز (Triage Dialog)
    if (uiState.isTriageDialogOpen && uiState.selectedDefect != null) {
        val defect = uiState.selectedDefect
        TriageActionDialog(
            defect = defect,
            onDismiss = { viewModel.closeTriageDialog() },
            onConfirm = { classification, priority, role, targetRelease ->
                viewModel.applyTriage(
                    defectId = defect.id,
                    classification = classification,
                    priority = priority,
                    assignedRole = role,
                    targetRelease = targetRelease
                )
            }
        )
    }

    // Dialog لتحديث دورة حياة الحالة (Status Transition Dialog)
    if (uiState.isStatusDialogOpen && uiState.selectedDefect != null) {
        val defect = uiState.selectedDefect
        StatusTransitionDialog(
            defect = defect,
            onDismiss = { viewModel.closeStatusDialog() },
            onConfirm = { newStatus, resolutionNote, closureReason, verificationTest ->
                viewModel.updateStatus(
                    defectId = defect.id,
                    newStatus = newStatus,
                    resolutionNote = resolutionNote,
                    closureReason = closureReason,
                    verificationTest = verificationTest
                )
            }
        )
    }
}

@Composable
private fun TriageSummaryCard(summary: DefectTriageSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ملخص مؤشرات الفرز والتصنيف",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "الإجمالي: ${summary.totalCount}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // الصف الأول: التصنيفات الحرجة
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricChip(
                    title = "Blocker",
                    count = summary.blockerCount,
                    containerColor = MaterialTheme.statusColors.errorBg,
                    contentColor = MaterialTheme.statusColors.errorFg,
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    title = "Critical",
                    count = summary.criticalCount,
                    containerColor = MaterialTheme.statusColors.warningBg,
                    contentColor = MaterialTheme.statusColors.warningFg,
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    title = "Major",
                    count = summary.majorCount,
                    containerColor = Color(0xFFFFFDE7),
                    contentColor = MaterialTheme.statusColors.warningFg,
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    title = "Minor",
                    count = summary.minorCount,
                    containerColor = Color(0xFFE3F2FD),
                    contentColor = Color(0xFF1565C0),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // الصف الثاني: التحسينات والحالات الخاصة
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricChip(
                    title = "تحسينات",
                    count = summary.enhancementCount,
                    containerColor = Color(0xFFF3E5F5),
                    contentColor = Color(0xFF6A1B9A),
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    title = "مغلق / مبرر",
                    count = summary.closedCount,
                    containerColor = MaterialTheme.statusColors.successBg,
                    contentColor = MaterialTheme.statusColors.successFg,
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    title = "مؤجل",
                    count = summary.deferredCount,
                    containerColor = MaterialTheme.statusColors.neutralBg,
                    contentColor = Color(0xFF37474F),
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    title = "شرعي",
                    count = summary.shariaDomainCount,
                    containerColor = Color(0xFFE0F2F1),
                    contentColor = Color(0xFF004D40),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricChip(
    title: String,
    count: Int,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.85f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ShariaContentRuleAlert() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Gavel,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "ضابط سلامة المحتوى الشرعي",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "أي ملاحظة تخص نصاً قرآنياً أو حديثاً أو عزواً شرعياً تصنف تلقائياً كـ (Critical/Blocker) ولا يتم إغلاقها إلا بعد اعتماد هيئة المراجعة الشرعية المؤهلة.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun ClassificationFilterChips(
    selected: DefectClassification?,
    onSelect: (DefectClassification?) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            label = { Text("الكل") }
        )
        DefectClassification.values().forEach { classification ->
            FilterChip(
                selected = selected == classification,
                onClick = { onSelect(classification) },
                label = { Text(classification.titleAr) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = getClassificationColor(classification).copy(alpha = 0.2f),
                    selectedLabelColor = getClassificationColor(classification)
                )
            )
        }
    }
}

@Composable
private fun DomainFilterChips(
    selected: DefectDomain?,
    onSelect: (DefectDomain?) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        DefectDomain.values().forEach { domain ->
            FilterChip(
                selected = selected == domain,
                onClick = { onSelect(domain) },
                label = { Text(domain.titleAr) },
                leadingIcon = {
                    if (domain == DefectDomain.SHARIA_CONTENT) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = Color(0xFF00796B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun DefectCardItem(
    defect: BetaDefectRecord,
    onViewDetails: () -> Unit,
    onOpenTriage: () -> Unit,
    onOpenStatus: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() }
            .testTag("defect_card_${defect.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (defect.domain == DefectDomain.SHARIA_CONTENT) Color(0xFF00796B).copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // شريط الرأس: المعرف، الأولوية، والتصنيف
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = defect.id,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    PriorityBadge(priority = defect.priority)
                }

                ClassificationBadge(classification = defect.classification)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // العنوان
            Text(
                text = defect.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // الوصف المختصر
            Text(
                text = defect.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // تفاصيل الجهاز، المسؤول، والإصدار
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${defect.deviceModel} • ${defect.osVersion}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (defect.assignedRole.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = defect.assignedRole,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                StatusBadge(status = defect.status)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            // أزرار الإجراءات السريعة
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onOpenTriage,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("فرز وتصنيف", style = MaterialTheme.typography.labelMedium)
                }

                Spacer(modifier = Modifier.width(6.dp))

                FilledTonalButton(
                    onClick = onOpenStatus,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تحديث الحالة", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun PriorityBadge(priority: DefectPriority) {
    val (bgColor, txtColor) = when (priority) {
        DefectPriority.P0_IMMEDIATE -> Color(0xFFFFCDD2) to Color(0xFFB71C1C)
        DefectPriority.P1_HIGH -> Color(0xFFFFE0B2) to MaterialTheme.statusColors.warningFg
        DefectPriority.P2_MEDIUM -> Color(0xFFFFF9C4) to MaterialTheme.statusColors.warningFg
        DefectPriority.P3_LOW -> Color(0xFFE1F5FE) to Color(0xFF0277BD)
    }
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor
    ) {
        Text(
            text = priority.code,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = txtColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun ClassificationBadge(classification: DefectClassification) {
    val color = getClassificationColor(classification)
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Text(
            text = classification.titleAr,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun StatusBadge(status: DefectStatus) {
    val (bgColor, fgColor) = when (status) {
        DefectStatus.REPORTED -> Color(0xFFE0E0E0) to Color(0xFF424242)
        DefectStatus.TRIAGED -> Color(0xFFE1BEE7) to Color(0xFF4A148C)
        DefectStatus.IN_PROGRESS -> Color(0xFFBBDEFB) to Color(0xFF0D47A1)
        DefectStatus.RESOLVED -> Color(0xFFC8E6C9) to MaterialTheme.statusColors.successFg
        DefectStatus.VERIFIED -> Color(0xFFA5D6A7) to MaterialTheme.statusColors.successFg
        DefectStatus.DEFERRED -> Color(0xFFCFD8DC) to Color(0xFF37474F)
        DefectStatus.CLOSED -> MaterialTheme.statusColors.neutralBg to Color(0xFF616161)
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = status.titleAr,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = fgColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun DefectDetailDialog(
    defect: BetaDefectRecord,
    onDismiss: () -> Unit,
    onOpenTriage: () -> Unit,
    onOpenStatus: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = defect.id, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    ClassificationBadge(classification = defect.classification)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = defect.title, style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // النطاق والأولوية
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "المجال: ${defect.domain.titleAr}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    PriorityBadge(priority = defect.priority)
                }

                HorizontalDivider()

                // الوصف
                Text(text = "الوصف والملاحظة:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(text = defect.description, style = MaterialTheme.typography.bodyMedium)

                // خطوات إعادة الإنتاج
                if (defect.stepsToReproduce.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "خطوات إعادة الإنتاج:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    defect.stepsToReproduce.forEachIndexed { idx, step ->
                        Text(
                            text = "${idx + 1}. $step",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // النتائج
                if (defect.expectedResult.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "النتيجة المتوقعة:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text(text = defect.expectedResult, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.statusColors.successFg)
                }

                if (defect.actualResult.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "النتيجة الفعلية:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text(text = defect.actualResult, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.statusColors.errorFg)
                }

                // سجلات آمنة
                if (defect.safeLogsOrBreadcrumbs.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "التشخيص والسجلات الآمنة (بدون بيانات شخصية):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = defect.safeLogsOrBreadcrumbs,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // المسؤول والإصدار المستهدف
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "المسؤول: ${defect.assignedRole.ifBlank { "لم يعين" }}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "الإصدار: ${defect.targetRelease}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                // تفاصيل الحل أو سبب الإغلاق إن وجدت
                defect.resolutionNote?.let { note ->
                    Surface(
                        color = MaterialTheme.statusColors.successBg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(text = "ملاحظة الحل الفني:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.statusColors.successFg)
                            Text(text = note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.statusColors.successFg)
                            defect.verificationTest?.let { test ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "اختبار التحقق: $test", style = MaterialTheme.typography.labelSmall, color = Color(0xFF33691E))
                            }
                        }
                    }
                }

                defect.closureReason?.let { reason ->
                    Surface(
                        color = MaterialTheme.statusColors.neutralBg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(text = "سبب الإغلاق أو التأجيل:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = Color(0xFF37474F))
                            Text(text = reason, style = MaterialTheme.typography.bodySmall, color = Color(0xFF455A64))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("إغلاق")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = {
                    onDismiss()
                    onOpenTriage()
                }) {
                    Text("فرز وتصنيف")
                }
                TextButton(onClick = {
                    onDismiss()
                    onOpenStatus()
                }) {
                    Text("تحديث الحالة")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TriageActionDialog(
    defect: BetaDefectRecord,
    onDismiss: () -> Unit,
    onConfirm: (DefectClassification, DefectPriority, String, String) -> Unit
) {
    var selectedClassification by remember { mutableStateOf(defect.classification) }
    var selectedPriority by remember { mutableStateOf(defect.priority) }
    var assignedRole by remember { mutableStateOf(defect.assignedRole) }
    var targetRelease by remember { mutableStateOf(defect.targetRelease) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("فرز وتصنيف العيب: ${defect.id}") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (defect.domain == DefectDomain.SHARIA_CONTENT) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "تنبيه: محتوى شرعي، الحد الأدنى للتصنيف هو Critical وفق السياسة الإلزامية.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Text("التصنيف:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                DefectClassification.values().forEach { classification ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedClassification = classification }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedClassification == classification,
                            onClick = { selectedClassification = classification }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = classification.titleAr, style = MaterialTheme.typography.bodySmall)
                    }
                }

                HorizontalDivider()

                Text("الأولوية:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DefectPriority.values().forEach { priority ->
                        FilterChip(
                            selected = selectedPriority == priority,
                            onClick = { selectedPriority = priority },
                            label = { Text(priority.code) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = assignedRole,
                    onValueChange = { assignedRole = it },
                    label = { Text("المسؤول / الدور المكلف") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = targetRelease,
                    onValueChange = { targetRelease = it },
                    label = { Text("الإصدار المستهدف") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(selectedClassification, selectedPriority, assignedRole, targetRelease)
            }) {
                Text("حفظ الفرز")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
private fun StatusTransitionDialog(
    defect: BetaDefectRecord,
    onDismiss: () -> Unit,
    onConfirm: (DefectStatus, String?, String?, String?) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(defect.status) }
    var resolutionNote by remember { mutableStateOf(defect.resolutionNote ?: "") }
    var closureReason by remember { mutableStateOf(defect.closureReason ?: "") }
    var verificationTest by remember { mutableStateOf(defect.verificationTest ?: "") }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تحديث حالة العيب: ${defect.id}") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("اختر الحالة الجديدة:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                DefectStatus.values().forEach { status ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedStatus = status }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedStatus == status,
                            onClick = { selectedStatus = status }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = status.titleAr, style = MaterialTheme.typography.bodySmall)
                    }
                }

                if (selectedStatus == DefectStatus.RESOLVED || selectedStatus == DefectStatus.VERIFIED) {
                    OutlinedTextField(
                        value = resolutionNote,
                        onValueChange = { resolutionNote = it },
                        label = { Text("تفاصيل الإصلاح والحل الفني *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = verificationTest,
                        onValueChange = { verificationTest = it },
                        label = { Text("مرجع اختبار التحقق (Test Case)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (selectedStatus == DefectStatus.CLOSED || selectedStatus == DefectStatus.DEFERRED) {
                    OutlinedTextField(
                        value = closureReason,
                        onValueChange = { closureReason = it },
                        label = { Text("سبب الإغلاق أو التأجيل (إلزامي) *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                validationError?.let { err ->
                    Text(text = err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (selectedStatus == DefectStatus.CLOSED && closureReason.isBlank()) {
                    validationError = "لا يمكن إغلاق العيب دون ذكر سبب الإغلاق والتبرير الفني."
                    return@Button
                }
                if (selectedStatus == DefectStatus.DEFERRED && closureReason.isBlank()) {
                    validationError = "يجب توثيق سبب تأجيل العيب للإصدار القادم."
                    return@Button
                }
                if ((selectedStatus == DefectStatus.RESOLVED || selectedStatus == DefectStatus.VERIFIED) &&
                    (defect.classification == DefectClassification.BLOCKER || defect.classification == DefectClassification.CRITICAL) &&
                    resolutionNote.isBlank()
                ) {
                    validationError = "يجب كتابة تفاصيل الحل الفني للأعطال الحرجة."
                    return@Button
                }
                onConfirm(selectedStatus, resolutionNote.ifBlank { null }, closureReason.ifBlank { null }, verificationTest.ifBlank { null })
            }) {
                Text("تحديث الحالة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

private fun getClassificationColor(classification: DefectClassification): Color {
    return when (classification) {
        DefectClassification.BLOCKER -> MaterialTheme.statusColors.errorFg
        DefectClassification.CRITICAL -> Color(0xFFD84315)
        DefectClassification.MAJOR -> MaterialTheme.statusColors.warningFg
        DefectClassification.MINOR -> Color(0xFF1565C0)
        DefectClassification.ENHANCEMENT -> Color(0xFF6A1B9A)
        DefectClassification.DUPLICATE -> MaterialTheme.statusColors.neutralFg
        DefectClassification.NOT_REPRODUCIBLE -> Color(0xFF78909C)
        DefectClassification.EXPECTED_BEHAVIOR -> MaterialTheme.statusColors.successFg
    }
}
