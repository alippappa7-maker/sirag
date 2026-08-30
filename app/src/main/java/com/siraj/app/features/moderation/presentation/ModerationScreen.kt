package com.siraj.app.features.moderation.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.siraj.app.core.ui.components.SirajButton
import com.siraj.app.domain.models.community.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentModerationScreen(
    viewModel: ModerationViewModel,
    currentUserRole: String,
    currentUserId: String,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(currentUserRole) {
        viewModel.loadAll(currentUserRole)
    }

    LaunchedEffect(state.error, state.successMessage) {
        state.error?.let {
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
                        Text("مركز إدارة ومراقبة المحتوى")
                        Text("الدور: $currentUserRole", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Dashboard Tabs
            TabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = state.selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    text = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("البلاغات")
                            if (state.totalPendingReports > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Badge { Text("${state.totalPendingReports}") }
                            }
                        }
                    }
                )
                Tab(
                    selected = state.selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    text = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("طابور الفحص")
                            if (state.pendingUgcReviewCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Badge { Text("${state.pendingUgcReviewCount}") }
                            }
                        }
                    }
                )
                Tab(
                    selected = state.selectedTab == 2,
                    onClick = { viewModel.setSelectedTab(2) },
                    text = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("الاستئنافات")
                            if (state.pendingAppealsCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Badge { Text("${state.pendingAppealsCount}") }
                            }
                        }
                    }
                )
                Tab(
                    selected = state.selectedTab == 3,
                    onClick = { viewModel.setSelectedTab(3) },
                    text = { Text("الأداء وSLA") }
                )
            }

            // Tab Contents
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                if (state.isLoading && state.reports.isEmpty() && state.ugcItems.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    when (state.selectedTab) {
                        0 -> ReportsTabContent(
                            reports = state.reports,
                            onResolve = { reportId, res, notes ->
                                viewModel.resolveReport(reportId, currentUserId, res, notes, currentUserRole)
                            }
                        )
                        1 -> UgcQueueTabContent(
                            items = state.ugcItems,
                            selectedFilter = state.filterUgcState,
                            onSelectFilter = { 
                                viewModel.setUgcFilter(it)
                                viewModel.loadUgcQueue(currentUserRole)
                            },
                            onAction = { ugcId, action, notes ->
                                viewModel.takeUgcAction(ugcId, currentUserId, action, notes, currentUserRole)
                            }
                        )
                        2 -> AppealsTabContent(
                            appeals = state.appeals,
                            onResolveAppeal = { appealId, isApproved, notes ->
                                viewModel.resolveAppeal(appealId, currentUserId, isApproved, notes, currentUserRole)
                            }
                        )
                        3 -> PerformanceTabContent(
                            reports = state.reports,
                            logs = state.logs
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportsTabContent(
    reports: List<Report>,
    onResolve: (String, String, String) -> Unit
) {
    if (reports.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا توجد بلاغات معلقة لمراجعتها حالياً.")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(reports) { report ->
                ReportCardItem(report = report, onResolve = onResolve)
            }
        }
    }
}

@Composable
private fun ReportCardItem(
    report: Report,
    onResolve: (String, String, String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var selectedResolution by remember { mutableStateOf("DISMISS") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = report.reportType.titleArabic,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )

                // SLA Badge
                val slaColor = if (report.isOverdue) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                Surface(
                    color = slaColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (report.isOverdue) "متأخر عن SLA" else "متبقي ${report.remainingHours} ساعة (SLA)",
                        style = MaterialTheme.typography.labelSmall,
                        color = slaColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "الهدف: ${report.targetType.name} #${report.targetId.take(8)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "تفاصيل البلاغ: ${report.description}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "حالة هوية المُبلّغ: سرية ومحمية (مبدأ حماية المبلغين)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))
            SirajButton(
                text = "اتخاذ إجراء على البلاغ",
                onClick = { showDialog = true },
                modifier = Modifier.align(Alignment.End)
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("القرار الإداري للبلاغ") },
            text = {
                Column {
                    Text("حدد الإجراء المناسب:", style = MaterialTheme.typography.labelMedium)
                    val options = listOf(
                        "DISMISS" to "حفظ (بلاغ غير صحيح)",
                        "TAKE_DOWN" to "حذف المحتوى نهائياً",
                        "SUSPEND" to "تعليق المحتوى مؤقتاً",
                        "WARN_USER" to "توجيه إنذار لصاحب المحتوى",
                        "SUSPEND_USER" to "إيقاف حساب المستخدم المخالف"
                    )
                    options.forEach { (value, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedResolution == value,
                                onClick = { selectedResolution = value }
                            )
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ملاحظات وتعليل القرار") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onResolve(report.id, selectedResolution, notes)
                    showDialog = false
                }) {
                    Text("تأكيد القرار")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun UgcQueueTabContent(
    items: List<UgcItem>,
    selectedFilter: UgcState?,
    onSelectFilter: (UgcState?) -> Unit,
    onAction: (String, ModeratorAction, String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Filters
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { onSelectFilter(null) },
                    label = { Text("الكل") }
                )
            }
            items(UgcState.values()) { state ->
                FilterChip(
                    selected = selectedFilter == state,
                    onClick = { onSelectFilter(state) },
                    label = { Text(state.titleArabic) }
                )
            }
        }

        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا يوجد محتوى يطابق الفلتر المحدد.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(items) { item ->
                    UgcItemCard(item = item, onAction = onAction)
                }
            }
        }
    }
}

@Composable
private fun UgcItemCard(
    item: UgcItem,
    onAction: (String, ModeratorAction, String) -> Unit
) {
    var showActionDialog by remember { mutableStateOf(false) }
    var selectedAction by remember { mutableStateOf(ModeratorAction.APPROVE) }
    var actionNotes by remember { mutableStateOf("") }

    val stateColor = when (item.state) {
        UgcState.APPROVED, UgcState.RESTORED -> Color(0xFF2E7D32)
        UgcState.LIMITED -> Color(0xFFF57F17)
        UgcState.REJECTED, UgcState.SUSPENDED, UgcState.REMOVED -> MaterialTheme.colorScheme.error
        UgcState.APPEALED -> Color(0xFF7B1FA2)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = stateColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = item.state.titleArabic,
                        style = MaterialTheme.typography.labelSmall,
                        color = stateColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("الناشر: @${item.creatorName} | النوع: ${item.mediaType}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = item.description, style = MaterialTheme.typography.bodyMedium)

            // Scan Results Display
            item.scanResult?.let { scan ->
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("نتائج الفحص الآلي الاستباقي:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        if (scan.detectedFlags.isNotEmpty()) {
                            scan.detectedFlags.forEach { flag ->
                                Text("• $flag", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            }
                        } else {
                            Text("• لم يتم رصد مخالفات آلية (نظيف)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32))
                        }
                    }
                }
            }

            item.rejectionReason?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text("سبب التقييد/الرفض: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                SirajButton(
                    text = "إجراء إشرافي",
                    onClick = { showActionDialog = true }
                )
            }
        }
    }

    if (showActionDialog) {
        AlertDialog(
            onDismissRequest = { showActionDialog = false },
            title = { Text("اتخاذ إجراء إشرافي على المحتوى") },
            text = {
                Column {
                    Text("اختر القرار:", style = MaterialTheme.typography.labelMedium)
                    ModeratorAction.values().filter { it != ModeratorAction.DISMISS_REPORT }.forEach { action ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedAction == action,
                                onClick = { selectedAction = action }
                            )
                            Text(action.titleArabic, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = actionNotes,
                        onValueChange = { actionNotes = it },
                        label = { Text("تعليل القرار والملاحظات") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onAction(item.id, selectedAction, actionNotes)
                    showActionDialog = false
                }) {
                    Text("تنفيذ القرار")
                }
            },
            dismissButton = {
                TextButton(onClick = { showActionDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun AppealsTabContent(
    appeals: List<UgcAppeal>,
    onResolveAppeal: (String, Boolean, String) -> Unit
) {
    if (appeals.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا توجد طلبات استئناف معلقة.")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(appeals) { appeal ->
                AppealCardItem(appeal = appeal, onResolveAppeal = onResolveAppeal)
            }
        }
    }
}

@Composable
private fun AppealCardItem(
    appeal: UgcAppeal,
    onResolveAppeal: (String, Boolean, String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var isApprove by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "استئناف: ${appeal.ugcTitle}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                val statusColor = when (appeal.status) {
                    AppealStatus.APPROVED -> Color(0xFF2E7D32)
                    AppealStatus.REJECTED -> MaterialTheme.colorScheme.error
                    AppealStatus.PENDING -> Color(0xFFF57F17)
                }
                Surface(color = statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)) {
                    Text(
                        text = appeal.status.titleArabic,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text("سبب الإجراء الأصلي: ${appeal.originalReason}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(4.dp))
            Text("مبررات المستخدم: ${appeal.appealJustification}", style = MaterialTheme.typography.bodyMedium)

            if (appeal.status == AppealStatus.PENDING) {
                Spacer(modifier = Modifier.height(16.dp))
                SirajButton(
                    text = "البت في الاستئناف",
                    onClick = { showDialog = true },
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("البت في طلب الاستئناف") },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = isApprove, onClick = { isApprove = true })
                        Text("قبول الاستئناف واستعادة المحتوى")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = !isApprove, onClick = { isApprove = false })
                        Text("رفض الاستئناف وتأييد القرار")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ملاحظات القرار") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onResolveAppeal(appeal.id, isApprove, notes)
                    showDialog = false
                }) {
                    Text("حفظ القرار")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun PerformanceTabContent(
    reports: List<Report>,
    logs: List<ModerationDecisionLog>
) {
    val total = reports.size
    val overdue = reports.count { it.isOverdue }
    val resolved = reports.count { it.status == ReportStatus.RESOLVED || it.status == ReportStatus.DISMISSED }
    val complianceRate = if (total > 0) ((total - overdue).toFloat() / total * 100).toInt() else 100

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "مؤشرات الأداء وزمن الاستجابة (SLA Dashboard)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("الالتزام بـ SLA (24h)", style = MaterialTheme.typography.labelSmall)
                            Text("$complianceRate%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                        Column {
                            Text("إجمالي البلاغات", style = MaterialTheme.typography.labelSmall)
                            Text("$total", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("متأخرة عن SLA", style = MaterialTheme.typography.labelSmall)
                            Text("$overdue", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "سجل القرارات الإدارية (Audit Logs)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (logs.isEmpty()) {
            item {
                Text("لا توجد سجلات قرارات سابقة.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            items(logs) { log ->
                val dateStr = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(log.timestamp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = log.action, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(text = dateStr, style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "المشرف: ${log.moderatorId} | الهدف: ${log.targetType} #${log.targetId.take(8)}", style = MaterialTheme.typography.bodySmall)
                        if (log.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "الملاحظات: ${log.notes}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

