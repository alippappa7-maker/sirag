package com.siraj.app.features.admin.presentation.backup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siraj.app.domain.models.backup.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRecoveryScreen(
    viewModel: BackupRecoveryViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormatter = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("النسخ الاحتياطي والتعافي من الكوارث", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Backup & Disaster Recovery (RPO < 1h, RTO < 4h)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("backup_back_button")) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.setDrRunbookModalVisible(true) },
                        modifier = Modifier.testTag("backup_dr_runbook_button")
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = "دليل التعافي من الكوارث")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.setCreateBackupDialogVisible(true) },
                icon = { Icon(Icons.Default.CloudUpload, contentDescription = null) },
                text = { Text("إنشاء نسخة احتياطية مشفرة") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("trigger_backup_fab")
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Banner Notification if any
            if (uiState.bannerMessage != null) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("backup_banner_message")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = uiState.bannerMessage ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearBannerMessage() }) {
                                Icon(Icons.Default.Close, contentDescription = "إغلاق")
                            }
                        }
                    }
                }
            }

            // 2. High Level SLA & Health KPI Dashboard
            item {
                BackupHealthKpiDashboard(
                    drPlan = uiState.drPlan,
                    retention = uiState.retentionPolicy,
                    tombstoneCount = uiState.deletedUsersTombstoneCount,
                    onOpenDryRun = {
                        val latestProd = uiState.snapshots.firstOrNull { it.environment == BackupEnvironment.PROD }
                        viewModel.setDryRunModalVisible(true, latestProd)
                    },
                    onOpenProjectRestore = {
                        viewModel.setProjectRestoreModalVisible(true)
                    }
                )
            }

            // 3. Environment Filters
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.selectedEnvironment == null,
                        onClick = { viewModel.setEnvironmentFilter(null) },
                        label = { Text("الكل (${uiState.snapshots.size})") }
                    )
                    FilterChip(
                        selected = uiState.selectedEnvironment == BackupEnvironment.PROD,
                        onClick = { viewModel.setEnvironmentFilter(BackupEnvironment.PROD) },
                        label = { Text("الإنتاج المعزول") }
                    )
                    FilterChip(
                        selected = uiState.selectedEnvironment == BackupEnvironment.STAGING,
                        onClick = { viewModel.setEnvironmentFilter(BackupEnvironment.STAGING) },
                        label = { Text("بيئة الاختبار") }
                    )
                    FilterChip(
                        selected = uiState.selectedEnvironment == BackupEnvironment.DEV,
                        onClick = { viewModel.setEnvironmentFilter(BackupEnvironment.DEV) },
                        label = { Text("التطوير") }
                    )
                }
            }

            // 4. Snapshots List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "النسخ الاحتياطية الموثقة (${uiState.filteredSnapshots.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "مشفرة بـ CMEK & WORM",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 5. Snapshot Cards
            if (uiState.filteredSnapshots.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CloudOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("لا توجد نسخ احتياطية مسجلة لهذا التصنيف", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            } else {
                items(uiState.filteredSnapshots) { snapshot ->
                    BackupSnapshotCard(
                        snapshot = snapshot,
                        dateFormatter = dateFormatter,
                        onTestDryRun = {
                            viewModel.setDryRunModalVisible(true, snapshot)
                        },
                        onSelect = {
                            viewModel.selectSnapshot(snapshot)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // Modal: Create Backup Dialog
    if (uiState.showCreateBackupDialog) {
        CreateBackupDialog(
            isTriggering = uiState.isTriggeringBackup,
            onDismiss = { viewModel.setCreateBackupDialogVisible(false) },
            onConfirm = { type, env, notes ->
                viewModel.triggerNewBackup(type, env, notes)
            }
        )
    }

    // Modal: Dry-Run Restore Test
    if (uiState.showDryRunModal && uiState.selectedSnapshot != null) {
        DryRunRestoreModal(
            snapshot = uiState.selectedSnapshot!!,
            activeJob = uiState.activeDryRunJob,
            isExecuting = uiState.isExecutingDryRun,
            tombstoneCount = uiState.deletedUsersTombstoneCount,
            onDismiss = { viewModel.setDryRunModalVisible(false) },
            onExecute = {
                viewModel.executeDryRunRestore(uiState.selectedSnapshot!!.id)
            }
        )
    }

    // Modal: Project Level Restore
    if (uiState.showProjectRestoreModal) {
        ProjectRestoreModal(
            snapshots = uiState.snapshots,
            onDismiss = { viewModel.setProjectRestoreModalVisible(false) },
            onRestore = { projectId, targetWorkspaceId, snapshotId ->
                viewModel.executeProjectRestore(projectId, targetWorkspaceId, snapshotId)
            }
        )
    }

    // Modal: Disaster Recovery Runbook
    if (uiState.showDrRunbookModal) {
        DisasterRecoveryRunbookModal(
            drPlan = uiState.drPlan,
            retention = uiState.retentionPolicy,
            onDismiss = { viewModel.setDrRunbookModalVisible(false) }
        )
    }
}

@Composable
private fun BackupHealthKpiDashboard(
    drPlan: DisasterRecoveryPlan,
    retention: BackupRetentionPolicy,
    tombstoneCount: Int,
    onOpenDryRun: () -> Unit,
    onOpenProjectRestore: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("backup_health_dashboard"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2E7D32))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("حالة النسخ الاحتياطي والطوارئ: متوافقة ونشطة", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "CMEK + WORM Locked",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                KpiMetricItem(title = "نافذة فقد البيانات (RPO)", value = "15 دقيقة", subtext = "الحد الأقصى: 60 دقيقة")
                KpiMetricItem(title = "زمن الاستعادة (RTO)", value = "< 4 ساعات", subtext = "المشروع: 15 دقيقة")
                KpiMetricItem(title = "حق النسيان (GDPR)", value = "$tombstoneCount مستبعداً", subtext = "تطهير فوري عند الاستعادة")
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenDryRun,
                    modifier = Modifier.weight(1f).testTag("dry_run_test_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) {
                    Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("اختبار استعادة تجريبي", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onOpenProjectRestore,
                    modifier = Modifier.weight(1f).testTag("project_restore_btn")
                ) {
                    Icon(Icons.Default.RestorePage, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("استعادة مشروع محدد", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun KpiMetricItem(title: String, value: String, subtext: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
        Text(subtext, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun BackupSnapshotCard(
    snapshot: BackupSnapshot,
    dateFormatter: SimpleDateFormat,
    onTestDryRun: () -> Unit,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("snapshot_card_${snapshot.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (snapshot.backupType) {
                            BackupType.FULL -> Icons.Default.FolderZip
                            BackupType.INCREMENTAL -> Icons.Default.Update
                            BackupType.METADATA_ONLY -> Icons.Default.Description
                            BackupType.DISASTER_RECOVERY_SNAPSHOT -> Icons.Default.Shield
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(snapshot.id, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            dateFormatter.format(Date(snapshot.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = when (snapshot.environment) {
                        BackupEnvironment.PROD -> Color(0xFF1B5E20).copy(alpha = 0.15f)
                        BackupEnvironment.STAGING -> Color(0xFFE65100).copy(alpha = 0.15f)
                        BackupEnvironment.DEV -> Color(0xFF0277BD).copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        snapshot.environment.labelArabic,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (snapshot.environment) {
                            BackupEnvironment.PROD -> Color(0xFF1B5E20)
                            BackupEnvironment.STAGING -> Color(0xFFE65100)
                            BackupEnvironment.DEV -> Color(0xFF0277BD)
                        }
                    )
                }
            }

            Text(
                snapshot.notes.ifBlank { "نسخة احتياطية مشفرة بـ CMEK ومحققة آلياً" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SuggestionChip(
                    onClick = {},
                    label = { Text("${snapshot.documentCount} مستنداً", fontSize = 11.sp) }
                )
                SuggestionChip(
                    onClick = {},
                    label = { Text("${snapshot.sizeBytes / (1024 * 1024)} MB", fontSize = 11.sp) }
                )
                SuggestionChip(
                    onClick = {},
                    label = { Text("${snapshot.purgedTombstonesCount} مستبعداً (حذف)", fontSize = 11.sp) }
                )
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "SHA-256: ${snapshot.checksumSha256.take(12)}...",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )

                TextButton(
                    onClick = onTestDryRun,
                    modifier = Modifier.testTag("card_dry_run_btn_${snapshot.id}")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("اختبار في بيئة معزولة", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun CreateBackupDialog(
    isTriggering: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (BackupType, BackupEnvironment, String) -> Unit
) {
    var selectedType by remember { mutableStateOf(BackupType.FULL) }
    var selectedEnv by remember { mutableStateOf(BackupEnvironment.PROD) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!isTriggering) onDismiss() },
        title = { Text("إنشاء نسخة احتياطية مشفرة جديدة", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("نوع النسخة الاحتياطية:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BackupType.values().forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(when(type){
                                BackupType.FULL -> "شاملة"
                                BackupType.INCREMENTAL -> "تزايدية"
                                BackupType.METADATA_ONLY -> "بيانات فقط"
                                BackupType.DISASTER_RECOVERY_SNAPSHOT -> "طوارئ DR"
                            }, fontSize = 11.sp) }
                        )
                    }
                }

                Text("البيئة المستهدفة:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BackupEnvironment.values().forEach { env ->
                        FilterChip(
                            selected = selectedEnv == env,
                            onClick = { selectedEnv = env },
                            label = { Text(env.name, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات النسخة (سبب الإنشاء)") },
                    placeholder = { Text("مثال: نسخة شهرية دورية أو قبل ترقية المخطط") },
                    modifier = Modifier.fillMaxWidth()
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("سيتم تشفير النسخة فوراً بواسطة مفاتيح CMEK وتخزينها في مستودع آمن منفصل عن بيئة التطوير.", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedType, selectedEnv, notes) },
                enabled = !isTriggering,
                modifier = Modifier.testTag("confirm_create_backup_btn")
            ) {
                if (isTriggering) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("بدء النسخ والتشفير")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isTriggering) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
private fun DryRunRestoreModal(
    snapshot: BackupSnapshot,
    activeJob: RestoreJob?,
    isExecuting: Boolean,
    tombstoneCount: Int,
    onDismiss: () -> Unit,
    onExecute: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isExecuting) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Science, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("اختبار استعادة تجريبي (Dry-Run)", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("النسخة المحددة: ${snapshot.id}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("موقع التخزين: ${snapshot.storageLocationUri}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("🛡️ الضمانات الأمنية للاختبار التجريبي:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1B5E20))
                        Text("• يتم الاختبار داخل Sandbox معزول دون أي تأثير على بيانات الإنتاج.", fontSize = 11.sp, color = Color(0xFF1B5E20))
                        Text("• يتم تطبيق استبعاد $tombstoneCount طلباً لحذف الحسابات تلقائياً (Right to be Forgotten).", fontSize = 11.sp, color = Color(0xFF1B5E20))
                        Text("• يتم فحص توافق الفهارس وتكامل التواقيع الرقمية SHA-256.", fontSize = 11.sp, color = Color(0xFF1B5E20))
                    }
                }

                if (isExecuting) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("جارٍ تنفيذ الاختبار وفحص السلامة في البيئة المعزولة...", fontSize = 12.sp)
                    }
                } else if (activeJob != null) {
                    Text("سجل أحداث الاختبار:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 160.dp)
                    ) {
                        LazyColumn(modifier = Modifier.padding(8.dp)) {
                            items(activeJob.logs) { log ->
                                Text(log, fontSize = 11.sp, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onExecute,
                enabled = !isExecuting,
                modifier = Modifier.testTag("execute_dry_run_btn")
            ) {
                Text(if (activeJob == null) "بدء الاختبار الآن" else "إعادة الاختبار")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isExecuting) {
                Text("إغلاق")
            }
        }
    )
}

@Composable
private fun ProjectRestoreModal(
    snapshots: List<BackupSnapshot>,
    onDismiss: () -> Unit,
    onRestore: (String, String, String) -> Unit
) {
    var projectId by remember { mutableStateOf("") }
    var targetWorkspaceId by remember { mutableStateOf("workspace_main_01") }
    var selectedSnapshotId by remember { mutableStateOf(snapshots.firstOrNull()?.id ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("استعادة مشروع محدد من نسخة سابقة", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("تتيح هذه الميزة استعادة مسودة أو مشاهد مشروع محدد تم حذفه عن طريق الخطأ دون استعادة كامل قاعدة البيانات.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                OutlinedTextField(
                    value = projectId,
                    onValueChange = { projectId = it },
                    label = { Text("معرف المشروع (Project ID)") },
                    placeholder = { Text("مثال: proj_1700000000_abc") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = targetWorkspaceId,
                    onValueChange = { targetWorkspaceId = it },
                    label = { Text("مساحة العمل المستهدفة (Target Workspace)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("النسخة الاحتياطية المصدر:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                snapshots.take(3).forEach { snapshot ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSnapshotId = snapshot.id }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSnapshotId == snapshot.id,
                            onClick = { selectedSnapshotId = snapshot.id }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${snapshot.id} (${snapshot.environment.name})", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onRestore(projectId, targetWorkspaceId, selectedSnapshotId) },
                enabled = projectId.isNotBlank(),
                modifier = Modifier.testTag("confirm_project_restore_btn")
            ) {
                Text("استعادة المشروع")
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
private fun DisasterRecoveryRunbookModal(
    drPlan: DisasterRecoveryPlan,
    retention: BackupRetentionPolicy,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("دليل التعافي من الكوارث والسياسات (DR Runbook)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("1. أهداف التعافي المعتمدة (SLAs):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("• RPO (Recovery Point Objective): ${drPlan.rpoTargetMinutes} دقيقة للنسخ التزايدي.", fontSize = 12.sp)
                    Text("• RTO (Recovery Time Objective): ${drPlan.rtoTargetMinutes} دقيقة لاستعادة كامل النظام، و${drPlan.projectRtoMinutes} دقيقة للمشروع الفردي.", fontSize = 12.sp)
                    Text("• المنطقة الأساسية: ${drPlan.primaryRegion}", fontSize = 12.sp)
                    Text("• منطقة الطوارئ البديلة (Failover): ${drPlan.failoverRegion}", fontSize = 12.sp)
                }

                item {
                    Divider()
                    Text("2. سياسة الاحتفاظ بالنسخ (Retention Policy):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("• النسخ اليومية: احتفاظ لمدة ${retention.dailyRetentionDays} يوماً.", fontSize = 12.sp)
                    Text("• النسخ الأسبوعية: احتفاظ لمدة ${retention.weeklyRetentionWeeks} أسبوعاً.", fontSize = 12.sp)
                    Text("• النسخ الشهرية: احتفاظ لمدة ${retention.monthlyRetentionMonths} شهراً.", fontSize = 12.sp)
                    Text("• الأرشيف البارد: احتفاظ لمدة ${retention.coldArchiveYears} سنوات مع قفل WORM غير القابل للتعديل.", fontSize = 12.sp)
                }

                item {
                    Divider()
                    Text("3. الامتثال للخصوصية وحق النسيان (GDPR):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("• يُمنع منعاً باتاً استعادة أي بيانات لمستخدم طلب حذف حسابه.", fontSize = 12.sp)
                    Text("• يتم تطبيق فحص Tombstone تلقائياً أثناء أي عملية استعادة لتطهير السجلات المحذوفة فورياً.", fontSize = 12.sp)
                }

                item {
                    Divider()
                    Text("4. صلاحيات الوصول المقيدة (Least Privilege):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("• مدير DR فقط (Admin) يملك صلاحية الاستعادة الكاملة وتعديل السياسات.", fontSize = 12.sp)
                    Text("• المشغل (Operator) يملك صلاحية إطلاق النسخ والاختبارات التجريبية فقط.", fontSize = 12.sp)
                    Text("• النسخ مشفرة بمفاتيح CMEK وتخزن في مستودع معزول تماماً يمنع الوصول المباشر.", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )
}
