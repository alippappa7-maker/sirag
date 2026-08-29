package com.siraj.app.features.admin.presentation.incident

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siraj.app.core.incident.IncidentResponseEngine
import com.siraj.app.domain.models.incident.EmergencyActionRecord
import com.siraj.app.domain.models.incident.EmergencyActionType
import com.siraj.app.domain.models.incident.IncidentContact
import com.siraj.app.domain.models.incident.IncidentPhase
import com.siraj.app.domain.models.incident.IncidentPostMortemReport
import com.siraj.app.domain.models.incident.IncidentRole
import com.siraj.app.domain.models.incident.IncidentSeverity
import com.siraj.app.domain.models.incident.IncidentType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentResponseScreen(
    viewModel: IncidentResponseViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedSectionIndex by remember { mutableIntStateOf(0) }
    val sectionTabs = listOf("لوحة الطوارئ والتحكم", "خطط الحوادث (Playbooks)", "تقارير ما بعد الحادث", "مصفوفة التصعيد 24/7")

    // Dialog States
    var showKillSwitchDialog by remember { mutableStateOf(false) }
    var showSecretRotationDialog by remember { mutableStateOf(false) }
    var showShariaCorrectionDialog by remember { mutableStateOf(false) }
    var showRefundDialog by remember { mutableStateOf(false) }
    var showSuspendContentDialog by remember { mutableStateOf(false) }
    var showNewReportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "خطة الاستجابة للطوارئ والحوادث",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("incident_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (uiState.isGlobalPublishingHalted) 
                        MaterialTheme.colorScheme.errorContainer 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Notification Banner
            if (uiState.bannerMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.bannerMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearBanner() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "إغلاق", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }

            // Global Publishing State Alert
            if (uiState.isGlobalPublishingHalted) {
                Surface(
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "تحذير",
                            tint = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "🚨 النشر العام مجمد حالياً (EMERGENCY KILL-SWITCH ACTIVE)",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onError,
                                fontSize = 13.sp
                            )
                            Text(
                                "تم إيقاف بوابات تصدير ومشاركة المشاريع لحين انتهاء معالجة الخلل الطارئ.",
                                color = MaterialTheme.colorScheme.onError,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedSectionIndex,
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 12.dp
            ) {
                sectionTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedSectionIndex == index,
                        onClick = { selectedSectionIndex = index },
                        text = { Text(title, fontSize = 13.sp, fontWeight = if (selectedSectionIndex == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            // Body Content
            when (selectedSectionIndex) {
                0 -> EmergencyHubSection(
                    uiState = uiState,
                    onOpenKillSwitch = { showKillSwitchDialog = true },
                    onOpenSecretRotation = { showSecretRotationDialog = true },
                    onOpenShariaCorrection = { showShariaCorrectionDialog = true },
                    onOpenRefund = { showRefundDialog = true },
                    onOpenSuspendContent = { showSuspendContentDialog = true }
                )
                1 -> IncidentPlaybooksSection(
                    selectedType = uiState.selectedIncidentType,
                    onSelectType = { viewModel.selectIncidentType(it) }
                )
                2 -> PostMortemReportsSection(
                    reports = uiState.reports,
                    onOpenNewReport = { showNewReportDialog = true },
                    onSelectReport = { viewModel.selectReportForDetail(it) }
                )
                3 -> EscalationMatrixSection(contacts = uiState.contacts)
            }
        }
    }

    // Modal Dialogs
    if (showKillSwitchDialog) {
        KillSwitchConfirmDialog(
            isHalted = uiState.isGlobalPublishingHalted,
            onDismiss = { showKillSwitchDialog = false },
            onConfirm = { halt, reason ->
                viewModel.toggleGlobalPublishing(halt, reason)
                showKillSwitchDialog = false
            }
        )
    }

    if (showSecretRotationDialog) {
        SecretRotationDialog(
            onDismiss = { showSecretRotationDialog = false },
            onConfirm = { secretKey, reason ->
                viewModel.rotateSecretCredential(secretKey, reason)
                showSecretRotationDialog = false
            }
        )
    }

    if (showShariaCorrectionDialog) {
        ShariaCorrectionDialog(
            onDismiss = { showShariaCorrectionDialog = false },
            onConfirm = { incId, prjId, faulty, correct, src, rev1, n1, rev2, n2 ->
                viewModel.submitShariaCorrection(incId, prjId, faulty, correct, src, rev1, n1, rev2, n2)
                showShariaCorrectionDialog = false
            }
        )
    }

    if (showRefundDialog) {
        RefundBatchDialog(
            onDismiss = { showRefundDialog = false },
            onConfirm = { target, credits, reason ->
                viewModel.executeRefundBatch(target, credits, reason)
                showRefundDialog = false
            }
        )
    }

    if (showSuspendContentDialog) {
        SuspendContentDialog(
            onDismiss = { showSuspendContentDialog = false },
            onConfirm = { prjId, reason ->
                viewModel.suspendPublishedContent(prjId, reason)
                showSuspendContentDialog = false
            }
        )
    }

    if (showNewReportDialog) {
        NewPostMortemDialog(
            onDismiss = { showNewReportDialog = false },
            onConfirm = { incId, type, sev, title, cause, contain, correct, prevent ->
                viewModel.createPostMortemReport(incId, type, sev, title, cause, contain, correct, prevent)
                showNewReportDialog = false
            }
        )
    }

    if (uiState.selectedReportForDetail != null) {
        ReportDetailModal(
            report = uiState.selectedReportForDetail!!,
            onDismiss = { viewModel.selectReportForDetail(null) }
        )
    }
}

// -------------------------------------------------------------
// SECTIONS
// -------------------------------------------------------------

@Composable
fun EmergencyHubSection(
    uiState: IncidentResponseUiState,
    onOpenKillSwitch: () -> Unit,
    onOpenSecretRotation: () -> Unit,
    onOpenShariaCorrection: () -> Unit,
    onOpenRefund: () -> Unit,
    onOpenSuspendContent: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "مركز التدخل السريع والإجراءات الطارئة",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        "أزرار العزل الفوري لقائد الحادث والمهندس المسؤول مع تسجيل كامل في سجل التدقيق",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenKillSwitch,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isGlobalPublishingHalted) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.weight(1f).testTag("emergency_kill_switch_btn")
                        ) {
                            Icon(Icons.Filled.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (uiState.isGlobalPublishingHalted) "إلغاء تجميد النشر" else "إيقاف النشر العام",
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }

                        Button(
                            onClick = onOpenSecretRotation,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            modifier = Modifier.weight(1f).testTag("emergency_rotate_secret_btn")
                        ) {
                            Icon(Icons.Filled.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تدوير المفاتيح", fontSize = 11.sp, maxLines = 1)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenShariaCorrection,
                            modifier = Modifier.weight(1f).testTag("emergency_sharia_corr_btn")
                        ) {
                            Icon(Icons.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تصحيح شرعي", fontSize = 11.sp, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = onOpenRefund,
                            modifier = Modifier.weight(1f).testTag("emergency_refund_btn")
                        ) {
                            Icon(Icons.Filled.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("استرداد مالي", fontSize = 11.sp, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = onOpenSuspendContent,
                            modifier = Modifier.weight(1f).testTag("emergency_suspend_btn")
                        ) {
                            Icon(Icons.Filled.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("سحب مشروع", fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
            }
        }

        item {
            Text(
                "سجل الإجراءات الطارئة المنفذة مؤخراً",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        items(uiState.emergencyActions) { action ->
            EmergencyActionItemCard(action)
        }
    }
}

@Composable
fun EmergencyActionItemCard(action: EmergencyActionRecord) {
    val dateStr = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(action.executionTimestamp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    action.actionType.displayNameArabic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    dateStr,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "المنفذ: ${action.executedByRole.displayNameArabic} (${action.executedByUserId})",
                fontSize = 11.sp
            )
            Text(
                "الهدف: ${action.targetResource}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                "السبب: ${action.reasonArabic}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun IncidentPlaybooksSection(
    selectedType: IncidentType,
    onSelectType: (IncidentType) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "اختر نوع الحادث لعرض مسار الاستجابة المعياري (Standard SOP):",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(IncidentType.values()) { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { onSelectType(type) },
                        label = { Text(type.titleArabic, fontSize = 11.sp) }
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            selectedType.titleArabic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        SeverityBadge(selectedType.defaultSeverity)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        selectedType.descriptionArabic,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "المسؤول الرئيسي: ${selectedType.primaryRole.displayNameArabic} (${selectedType.primaryRole.contactChannel})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            Text(
                "خطوات مسار الاستجابة (Incident Workflow Lifecycle):",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        items(IncidentPhase.values()) { phase ->
            WorkflowPhaseCard(phase = phase, incidentType = selectedType)
        }

        item {
            // Public Communication Sanitization Box
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "صيغة الإشعار العام المعتمدة للمستخدمين (Sanitized Communication)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        IncidentResponseEngine.sanitizePublicIncidentNotice(selectedType, "Technical raw err"),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun WorkflowPhaseCard(phase: IncidentPhase, incidentType: IncidentType) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "${phase.stepOrder}",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    phase.displayNameArabic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    phase.descriptionArabic,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PostMortemReportsSection(
    reports: List<IncidentPostMortemReport>,
    onOpenNewReport: () -> Unit,
    onSelectReport: (IncidentPostMortemReport) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "تقارير الحوادث والمراجعة اللاحقة (${reports.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Button(
                    onClick = onOpenNewReport,
                    modifier = Modifier.testTag("create_post_mortem_btn")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تقرير جديد", fontSize = 12.sp)
                }
            }
        }

        items(reports) { report ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectReport(report) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            report.reportId,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        SeverityBadge(report.severity)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        report.titleArabic,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "السبب الجذري: ${report.rootCauseSummaryArabic}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("المدة: ${report.totalDowntimeMinutes} دقيقة", fontSize = 11.sp)
                        Text("المحقق: ${report.leadInvestigator}", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun EscalationMatrixSection(contacts: List<IncidentContact>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "مصفوفة جهات الاتصال وقنوات التصعيد (24/7 Escalation Roster)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                "قنوات مشفرة ومباشرة للاتصال بفرق الاستجابة للطوارئ",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(contacts) { contact ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "T${contact.escalationOrder}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 13.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            contact.nameArabic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            contact.role.displayNameArabic,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "الهاتف: ${contact.primaryPhone} | البريد: ${contact.secureEmail}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SeverityBadge(severity: IncidentSeverity) {
    val (color, text) = when (severity) {
        IncidentSeverity.P0_CRITICAL -> MaterialTheme.colorScheme.error to "P0 - حرج جداً"
        IncidentSeverity.P1_HIGH -> MaterialTheme.colorScheme.errorContainer to "P1 - عالي الخطورة"
        IncidentSeverity.P2_MEDIUM -> MaterialTheme.colorScheme.tertiaryContainer to "P2 - متوسط"
        IncidentSeverity.P3_LOW -> MaterialTheme.colorScheme.surfaceVariant to "P3 - منخفض"
    }
    Surface(
        color = color,
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// -------------------------------------------------------------
// MODALS & DIALOGS
// -------------------------------------------------------------

@Composable
fun KillSwitchConfirmDialog(
    isHalted: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Boolean, String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isHalted) "إلغاء تجميد النشر العام" else "🚨 تأكيد إيقاف النشر العام (Kill Switch)")
        },
        text = {
            Column {
                Text(
                    if (isHalted) 
                        "هل ترغب بإعادة تفعيل بوابات النشر وتصدير المشاريع للعامة؟" 
                    else 
                        "سيتم حظر كافة عمليات النشر ومشاركة المشاريع والتصدير فورياً على مستوى التطبيق بالكامل.",
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("سبب القرار وسجل التدقيق") },
                    modifier = Modifier.fillMaxWidth().testTag("kill_switch_reason_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(!isHalted, reason) },
                enabled = reason.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isHalted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.testTag("kill_switch_confirm_btn")
            ) {
                Text(if (isHalted) "تأكيد إعادة التفعيل" else "تأكيد الإيقاف الفوري")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
fun SecretRotationDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var secretKey by remember { mutableStateOf("GEMINI_API_KEY_SEC_01") }
    var reason by remember { mutableStateOf("تدوير دوري وقائي") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تدوير مفتاح الاعتماد السحابي (Secret Rotation)") },
        text = {
            Column {
                Text("سيتم إبطال المفتاح الحالي فوراً وإنشاء اعتماد جديد في Google Secret Manager.", fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = secretKey,
                    onValueChange = { secretKey = it },
                    label = { Text("معرف المفتاح السري") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("السبب") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(secretKey, reason) },
                enabled = secretKey.isNotBlank() && reason.isNotBlank(),
                modifier = Modifier.testTag("rotate_secret_confirm_btn")
            ) {
                Text("تنفيذ التدوير")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
fun ShariaCorrectionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, String, String, String) -> Unit
) {
    var projectId by remember { mutableStateOf("PRJ-HADITH-440") }
    var faultyText by remember { mutableStateOf("") }
    var correctText by remember { mutableStateOf("") }
    var sourceRef by remember { mutableStateOf("صحيح البخاري - كتاب العلم") }
    var rev1Id by remember { mutableStateOf("REV-LEAD-01") }
    var rev1Notes by remember { mutableStateOf("تمت مطابقة النص مع الأصل المسند") }
    var rev2Id by remember { mutableStateOf("REV-LEAD-02") }
    var rev2Notes by remember { mutableStateOf("أؤكد صحة النقل وتوافق التخريج") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إجراء تصحيح شرعي معتمد (Double Review)") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                item {
                    Text("يلزم مراجعين اثنين لاعتماد أي تصحيح في النصوص الشرعية أو القرآنية.", fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = projectId, onValueChange = { projectId = it }, label = { Text("معرف المشروع") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = faultyText, onValueChange = { faultyText = it }, label = { Text("النص الخاطئ المرصود") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = correctText, onValueChange = { correctText = it }, label = { Text("النص المصوب المعتمد") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = sourceRef, onValueChange = { sourceRef = it }, label = { Text("المصدر المعتمد ورقم الحديث/الآية") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = rev1Notes, onValueChange = { rev1Notes = it }, label = { Text("ملاحظات المراجع الأول") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = rev2Notes, onValueChange = { rev2Notes = it }, label = { Text("ملاحظات المراجع الثاني") }, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm("INC-SHARIA-AUTO", projectId, faultyText, correctText, sourceRef, rev1Id, rev1Notes, rev2Id, rev2Notes)
                },
                enabled = faultyText.isNotBlank() && correctText.isNotBlank() && sourceRef.isNotBlank(),
                modifier = Modifier.testTag("sharia_correct_confirm_btn")
            ) {
                Text("اعتماد ونشر الإصدار الجديد")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
fun RefundBatchDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String) -> Unit
) {
    var target by remember { mutableStateOf("BATCH-BILLING-2026-08") }
    var credits by remember { mutableStateOf("50") }
    var reason by remember { mutableStateOf("تعويض عن خصم رصيد مكرر في عملية التوليد") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("استرداد مالي / إعادة أرصدة") },
        text = {
            Column {
                OutlinedTextField(value = target, onValueChange = { target = it }, label = { Text("المستخدم أو دفعة الحسابات المتأثرة") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = credits, onValueChange = { credits = it }, label = { Text("قيمة الرصيد المسترد (Credits)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("سبب الاسترداد") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(target, credits.toIntOrNull() ?: 50, reason) },
                enabled = target.isNotBlank(),
                modifier = Modifier.testTag("refund_confirm_btn")
            ) {
                Text("تنفيذ الاسترداد")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
fun SuspendContentDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var projectId by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("سحب احترازي لوجود بلاغ تدقيق") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("سحب وتعليق مشروع منشور") },
        text = {
            Column {
                OutlinedTextField(value = projectId, onValueChange = { projectId = it }, label = { Text("معرف المشروع المنشور") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("سبب التعليق") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(projectId, reason) },
                enabled = projectId.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("suspend_confirm_btn")
            ) {
                Text("سحب فوري")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
fun NewPostMortemDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, IncidentType, IncidentSeverity, String, String, List<String>, List<String>, List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var rootCause by remember { mutableStateOf("") }
    var incidentType by remember { mutableStateOf(IncidentType.SERVICE_OUTAGE) }
    var severity by remember { mutableStateOf(IncidentSeverity.P1_HIGH) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إنشاء تقرير حادث ومراجعة لاحقة (Post-Mortem)") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                item {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان الحادث") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = rootCause, onValueChange = { rootCause = it }, label = { Text("ملخص السبب الجذري") }, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        "INC-" + System.currentTimeMillis().toString().takeLast(6),
                        incidentType,
                        severity,
                        title,
                        rootCause,
                        listOf("عزل الخدمة", "تطبيق المسار البديل"),
                        listOf("إصلاح الكود وتحديث السياسة"),
                        listOf("تحديث المراقبة الآلية لمنع التكرار")
                    )
                },
                enabled = title.isNotBlank() && rootCause.isNotBlank(),
                modifier = Modifier.testTag("save_report_confirm_btn")
            ) {
                Text("حفظ التقرير")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
fun ReportDetailModal(
    report: IncidentPostMortemReport,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(report.reportId, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                Text(report.titleArabic, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp)) {
                item {
                    SeverityBadge(report.severity)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("نوع الحادث: ${report.incidentType.titleArabic}", fontSize = 12.sp)
                    Text("المحقق المسؤول: ${report.leadInvestigator}", fontSize = 12.sp)
                    Text("فترة التوقف: ${report.totalDowntimeMinutes} دقيقة", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("السبب الجذري (Root Cause):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(report.rootCauseSummaryArabic, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("إجراءات العزل الفوري (Containment):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    report.containmentStepsArabic.forEach { step ->
                        Text("• $step", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("الإجراءات التصحيحية (Corrective Actions):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    report.correctiveActionsArabic.forEach { act ->
                        Text("• $act", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("المهام الوقائية لمنع التكرار (Preventive Tasks):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    report.preventiveTasksArabic.forEach { prev ->
                        Text("• $prev", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("إغلاق") }
        }
    )
}
