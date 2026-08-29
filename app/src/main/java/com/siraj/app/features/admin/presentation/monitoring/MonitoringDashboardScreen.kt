package com.siraj.app.features.admin.presentation.monitoring

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siraj.app.domain.models.monitoring.IncidentSeverity
import com.siraj.app.domain.models.monitoring.IncidentState
import com.siraj.app.domain.models.monitoring.MonitoredService
import com.siraj.app.domain.models.monitoring.MonitoringAlert
import com.siraj.app.domain.models.monitoring.ServiceCategory
import com.siraj.app.domain.models.monitoring.ServiceHealthCheck
import com.siraj.app.domain.models.monitoring.ServiceHealthStatus
import com.siraj.app.domain.models.monitoring.ServiceIncident
import com.siraj.app.domain.models.monitoring.SystemTelemetryOverview
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitoringDashboardScreen(
    viewModel: MonitoringDashboardViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("حالة الخدمات والواجهات", "البلاغات والأعطال (Incidents)", "سجل التنبيهات والأداء")

    var showCreateIncidentDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("مراقبة الخدمات وسرعة الاستجابة", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            "صحة المنظومة وسلامة المزودين ومسارات التحويل التلقائي",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("monitoring_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.runAllHealthProbes() },
                        enabled = !uiState.isProbing,
                        modifier = Modifier.testTag("refresh_all_probes_button")
                    ) {
                        if (uiState.isProbing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "فحص الكل الآن")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Banner notification
            AnimatedVisibility(visible = uiState.bannerMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = uiState.bannerMessage ?: "",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.clearBanner() }) {
                            Text("إغلاق", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Top Telemetry Metrics Summary Card
            TelemetryOverviewCards(telemetry = uiState.telemetryOverview)

            // Main Tab Row
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontSize = 13.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            when (selectedTab) {
                0 -> ServicesHealthListTab(
                    uiState = uiState,
                    onCategorySelected = { viewModel.filterByCategory(it) },
                    onRunSingleProbe = { viewModel.runSingleProbe(it) },
                    onToggleCircuitBreaker = { service, disable, reason ->
                        viewModel.toggleCircuitBreaker(service, disable, reason)
                    },
                    onOpenRunbook = { viewModel.selectServiceForRunbook(it) }
                )
                1 -> IncidentsManagementTab(
                    uiState = uiState,
                    onCreateIncidentClick = { showCreateIncidentDialog = true },
                    onSelectIncidentDetail = { viewModel.selectIncidentForDetail(it) },
                    onUpdateState = { id, state, notes -> viewModel.updateIncidentState(id, state, notes) }
                )
                2 -> AlertsAndPerformanceTab(
                    uiState = uiState,
                    onAcknowledgeAlert = { viewModel.acknowledgeAlert(it) },
                    onDismissAlert = { viewModel.dismissAlert(it) }
                )
            }
        }
    }

    // Create Incident Dialog
    if (showCreateIncidentDialog) {
        CreateIncidentDialog(
            onDismiss = { showCreateIncidentDialog = false },
            onConfirm = { service, title, desc, sev ->
                viewModel.createIncident(service, title, desc, sev)
                showCreateIncidentDialog = false
            }
        )
    }

    // Incident Detail Dialog
    uiState.selectedIncidentForDetail?.let { incident ->
        IncidentDetailDialog(
            incident = incident,
            onDismiss = { viewModel.selectIncidentForDetail(null) },
            onUpdateState = { newState, notes, rootCause, mitigation ->
                viewModel.updateIncidentState(incident.incidentId, newState, notes, rootCause, mitigation)
            }
        )
    }

    // Service Runbook Modal
    uiState.selectedServiceForRunbook?.let { service ->
        ServiceRunbookModal(
            service = service,
            onDismiss = { viewModel.selectServiceForRunbook(null) },
            onToggleCircuitBreaker = { disable, reason ->
                viewModel.toggleCircuitBreaker(service, disable, reason)
            }
        )
    }
}

@Composable
fun TelemetryOverviewCards(telemetry: SystemTelemetryOverview) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
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
                            .background(
                                when (telemetry.overallSystemStatus) {
                                    ServiceHealthStatus.HEALTHY -> Color(0xFF2E7D32)
                                    ServiceHealthStatus.DEGRADED -> Color(0xFFE65100)
                                    else -> Color(0xFFC62828)
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "الحالة العامة: ${telemetry.overallSystemStatus.displayNameArabic}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Text(
                    text = "${telemetry.healthyServicesCount} / ${telemetry.totalServicesCount} خدمة تعمل",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TelemetryMetricItem("متوسط الاستجابة", "${telemetry.avgSystemLatencyMs} ms")
                TelemetryMetricItem("نسبة الأعطال", "${telemetry.globalErrorRatePercent}%")
                TelemetryMetricItem("طابور الفيديو", "${telemetry.totalQueueDepth} مهام")
                TelemetryMetricItem("التخزين السحابي", "${telemetry.totalStorageUsageTb} TB")
            }
        }
    }
}

@Composable
fun TelemetryMetricItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun ServicesHealthListTab(
    uiState: MonitoringDashboardUiState,
    onCategorySelected: (ServiceCategory?) -> Unit,
    onRunSingleProbe: (MonitoredService) -> Unit,
    onToggleCircuitBreaker: (MonitoredService, Boolean, String) -> Unit,
    onOpenRunbook: (MonitoredService) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Category filters
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedCategory == null,
                        onClick = { onCategorySelected(null) },
                        label = { Text("الكل (${uiState.servicesHealthList.size})") }
                    )
                }
                items(ServiceCategory.values()) { category ->
                    val count = uiState.servicesHealthList.count { it.service.category == category }
                    FilterChip(
                        selected = uiState.selectedCategory == category,
                        onClick = { onCategorySelected(category) },
                        label = { Text("${category.displayNameArabic} ($count)") }
                    )
                }
            }
        }

        // Service items
        items(uiState.filteredServicesList, key = { it.service.name }) { serviceCheck ->
            ServiceHealthCheckCard(
                check = serviceCheck,
                onRunProbe = { onRunSingleProbe(serviceCheck.service) },
                onToggleCircuitBreaker = { disable ->
                    onToggleCircuitBreaker(
                        serviceCheck.service,
                        disable,
                        "تعطيل يدوي من مدير النظام لحماية جودة الخدمة"
                    )
                },
                onOpenRunbook = { onOpenRunbook(serviceCheck.service) }
            )
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
fun ServiceHealthCheckCard(
    check: ServiceHealthCheck,
    onRunProbe: () -> Unit,
    onToggleCircuitBreaker: (Boolean) -> Unit,
    onOpenRunbook: () -> Unit
) {
    val statusColor = when (check.status) {
        ServiceHealthStatus.HEALTHY -> Color(0xFF2E7D32)
        ServiceHealthStatus.DEGRADED -> Color(0xFFE65100)
        ServiceHealthStatus.UNAVAILABLE -> Color(0xFFC62828)
        ServiceHealthStatus.CIRCUIT_BROKEN_DISABLED -> Color(0xFF757575)
        ServiceHealthStatus.MAINTENANCE -> Color(0xFF0277BD)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (check.isCircuitBroken) 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = check.service.displayNameArabic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = check.service.category.displayNameArabic,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = check.status.displayNameArabic,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = check.statusMessageArabic,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Dynamic Fallback notice if present
            check.fallbackService?.let { fallback ->
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "المسار البديل النشط: ${fallback.displayNameArabic}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("الاستجابة: ${check.latencyMs}ms", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("الأخطاء: ${check.errorRatePercent}%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (check.queueDepth > 0) {
                        Text("الطابور: ${check.queueDepth}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = onOpenRunbook) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("دليل التشغيل (Runbook)", fontSize = 11.sp)
                    }
                    IconButton(onClick = onRunProbe, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Speed, contentDescription = "فحص الاستجابة", modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = { onToggleCircuitBreaker(!check.isCircuitBroken) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (check.isCircuitBroken) Icons.Default.PowerSettingsNew else Icons.Default.CloudOff,
                            contentDescription = if (check.isCircuitBroken) "إعادة التفعيل" else "تعطيل احترازي",
                            tint = if (check.isCircuitBroken) Color(0xFF2E7D32) else Color(0xFFC62828),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IncidentsManagementTab(
    uiState: MonitoringDashboardUiState,
    onCreateIncidentClick: () -> Unit,
    onSelectIncidentDetail: (ServiceIncident) -> Unit,
    onUpdateState: (String, IncidentState, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("الأعطال النشطة والبلاغات التشغيلية", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Button(
                    onClick = onCreateIncidentClick,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.AddAlert, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("فتح بلاغ عطل جديد", fontSize = 12.sp)
                }
            }
        }

        if (uiState.activeIncidents.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("لا توجد أعطال أو بلاغات تشغيلية نشطة حالياً", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("كافة الخدمات تعمل ضمن الكفاءة المحددة ودون أي انقطاع", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(uiState.activeIncidents, key = { it.incidentId }) { incident ->
                IncidentCardItem(
                    incident = incident,
                    onClick = { onSelectIncidentDetail(incident) },
                    onQuickResolve = {
                        onUpdateState(incident.incidentId, IncidentState.RESOLVED, "تم حل العطل وتأكيد استقرار الخدمة.")
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text("سجل الأعطال المحلولة سابقاً (${uiState.incidentHistory.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        items(uiState.incidentHistory.filter { it.state == IncidentState.RESOLVED }, key = { it.incidentId }) { resolvedIncident ->
            IncidentCardItem(
                incident = resolvedIncident,
                onClick = { onSelectIncidentDetail(resolvedIncident) },
                onQuickResolve = {}
            )
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
fun IncidentCardItem(
    incident: ServiceIncident,
    onClick: () -> Unit,
    onQuickResolve: () -> Unit
) {
    val severityColor = when (incident.severity) {
        IncidentSeverity.P0_CRITICAL -> Color(0xFFC62828)
        IncidentSeverity.P1_HIGH -> Color(0xFFE65100)
        IncidentSeverity.P2_MEDIUM -> Color(0xFFF57F17)
        IncidentSeverity.P3_LOW -> Color(0xFF1565C0)
    }

    val stateColor = when (incident.state) {
        IncidentState.INVESTIGATING -> Color(0xFFC62828)
        IncidentState.IDENTIFIED -> Color(0xFFE65100)
        IncidentState.MITIGATING -> Color(0xFF0277BD)
        IncidentState.RESOLVED -> Color(0xFF2E7D32)
        IncidentState.MONITORING -> Color(0xFF00897B)
    }

    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = severityColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = incident.severity.name.replace("_", " "),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = severityColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = incident.incidentId,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Surface(
                    color = stateColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = incident.state.displayNameArabic,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = stateColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(incident.titleArabic, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(incident.descriptionArabic, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "البدء: ${dateFormat.format(Date(incident.startTimestamp))}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (incident.state != IncidentState.RESOLVED) {
                    TextButton(onClick = onQuickResolve) {
                        Text("إغلاق العطل وحله", fontSize = 11.sp, color = Color(0xFF2E7D32))
                    }
                } else {
                    Text(
                        text = "تم الحل: ${dateFormat.format(Date(incident.resolvedTimestamp ?: incident.startTimestamp))}",
                        fontSize = 11.sp,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}

@Composable
fun AlertsAndPerformanceTab(
    uiState: MonitoringDashboardUiState,
    onAcknowledgeAlert: (String) -> Unit,
    onDismissAlert: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("التنبيهات الفورية غير المكررة (Deduplicated Alerts)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        if (uiState.activeAlerts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("لا توجد تنبيهات عاجلة غير مقروءة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(uiState.activeAlerts, key = { it.alertId }) { alert ->
                AlertItemCard(
                    alert = alert,
                    onAcknowledge = { onAcknowledgeAlert(alert.alertId) },
                    onDismiss = { onDismissAlert(alert.alertId) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("قواعد العزل البيئي والتنبيه الآمن", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("• منع تكرار التنبيهات: يتم تجميع التنبيهات المتطابقة بناءً على بصمة التجزئة (Deduplication Hash).", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• حظر اختبار المحتوى الديني: الفحص الآلي يستخدم رموزاً اصطناعية محايدة تماماً دون مساس بآيات القرآن.", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• أمان الرسائل الموجهة للمستخدم: إخفاء عناوين IP، ومفاتيح API، وبنية الـ Cloud Functions عن العميل.", fontSize = 12.sp)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
fun AlertItemCard(
    alert: MonitoringAlert,
    onAcknowledge: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(alert.titleArabic, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                Text(alert.service.displayNameArabic, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(alert.messageArabic, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("تجاهل", fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = onAcknowledge,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("تأكيد الاستلام", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun CreateIncidentDialog(
    onDismiss: () -> Unit,
    onConfirm: (MonitoredService, String, String, IncidentSeverity) -> Unit
) {
    var selectedService by remember { mutableStateOf(MonitoredService.GEMINI_AI_PROVIDER) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf(IncidentSeverity.P1_HIGH) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("فتح بلاغ عطل تشغيلي (Create Incident)", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("اختر الخدمة المتأثرة:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(MonitoredService.values()) { service ->
                        FilterChip(
                            selected = selectedService == service,
                            onClick = { selectedService = service },
                            label = { Text(service.displayNameArabic, fontSize = 11.sp) }
                        )
                    }
                }

                Text("مستوى الخطورة (Severity):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IncidentSeverity.values().forEach { sev ->
                        FilterChip(
                            selected = severity == sev,
                            onClick = { severity = sev },
                            label = { Text(sev.name.take(2), fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان العطل المختصر") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("التفاصيل والملاحظات الأولية") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedService, title, description, severity) },
                enabled = title.isNotBlank() && description.isNotBlank()
            ) {
                Text("فتح البلاغ وإشعار الفريق")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
fun IncidentDetailDialog(
    incident: ServiceIncident,
    onDismiss: () -> Unit,
    onUpdateState: (IncidentState, String, String?, String?) -> Unit
) {
    var newState by remember { mutableStateOf(incident.state) }
    var notes by remember { mutableStateOf("") }
    var rootCause by remember { mutableStateOf(incident.rootCauseSummaryArabic ?: "") }
    var mitigation by remember { mutableStateOf(incident.mitigationActionArabic ?: "") }

    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("تفاصيل البلاغ ${incident.incidentId}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(incident.severity.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(incident.titleArabic, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(incident.descriptionArabic, fontSize = 12.sp)

                HorizontalDivider()

                Text("تحديث حالة البلاغ:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(IncidentState.values()) { st ->
                        FilterChip(
                            selected = newState == st,
                            onClick = { newState = st },
                            label = { Text(st.displayNameArabic, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = rootCause,
                    onValueChange = { rootCause = it },
                    label = { Text("السبب الجذري (Root Cause)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = mitigation,
                    onValueChange = { mitigation = it },
                    label = { Text("إجراء التخفيف / المسار البديل (Mitigation)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات التحديث الزمني") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("السجل الزمني للأحداث (Timeline):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                incident.timelineEvents.forEach { event ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(event.state.displayNameArabic, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(dateFormat.format(Date(event.timestamp)), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(event.notesArabic, fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdateState(newState, notes.ifBlank { "تحديث حالة البلاغ إلى ${newState.displayNameArabic}" }, rootCause, mitigation)
                    onDismiss()
                }
            ) {
                Text("حفظ وتطبيق التحديث")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إغلاق") }
        }
    )
}

@Composable
fun ServiceRunbookModal(
    service: MonitoredService,
    onDismiss: () -> Unit,
    onToggleCircuitBreaker: (Boolean, String) -> Unit
) {
    val runbookSteps = when (service) {
        MonitoredService.AUTHENTICATION -> listOf(
            "1. فحص حالة Firebase Auth و Google Identity Services عبر Google Cloud Status.",
            "2. التحقق من صحة شهادات SSL ونطاقات OAuth المعتمدة في Console.",
            "3. إذا كان العطل من مزود جهة خارجية، تفعيل وضع تسجيل الدخول بالبريد ورسالة التنبيه اللطيفة."
        )
        MonitoredService.FIRESTORE -> listOf(
            "1. مراجعة حصص العمليات (Read/Write Quotas) في مشروع Firebase.",
            "2. التحقق من اكتمال الفهارس المركبة (Composite Indexes) لضمان عدم توقف الاستعلامات.",
            "3. تفعيل وضع القراءة فقط للكاش المحلي (Offline Persistence) على أجهزة العملاء."
        )
        MonitoredService.STORAGE -> listOf(
            "1. فحص حصص النطاق الترددي ومساحة الأصول والتسجيلات الصوتية.",
            "2. التحقق من صلاحيات مفاتيح التشفير السحابية CMEK في Cloud KMS.",
            "3. تفعيل التخزين الاحتياطي المؤقت (Temporary Storage Bucket)."
        )
        MonitoredService.GEMINI_AI_PROVIDER -> listOf(
            "1. فحص حصص الـ Rate Limits ومستوى استهلاك الـ Tokens عبر Cloud Functions Backend.",
            "2. تفعيل قاطع الدائرة (Circuit Breaker) لتحويل العملاء فوراً إلى القوالب اليدوية.",
            "3. عدم إرسال أي نصوص دينية حساسة للفحص، واستخدام نصوص محايدة اصطناعية فقط."
        )
        MonitoredService.QURAN_API_PROVIDER -> listOf(
            "1. التحقق من استجابة واجهة مجمع الملك فهد ومصحف المدينة الرقمي.",
            "2. التحويل التلقائي والفوري لقاعدة البيانات المحلية المشفرة المعتمدة.",
            "3. إظهار شارة الاعتماد للمستخدم مع تأكيد صحة نص الآية ورقمها."
        )
        MonitoredService.IMAGE_GENERATION_PROVIDER -> listOf(
            "1. فحص زمن استجابة مزود الصور في الـ Backend.",
            "2. في حال تجاوز المهلة (Timeout > 5s)، تفعيل المسار البديل إلى مكتبة الأصول الإسلامية الجاهزة.",
            "3. إعادة رصيد العملية للمستخدم آلياً في حال فشل التوليد."
        )
        MonitoredService.VIDEO_RENDERING_QUEUE -> listOf(
            "1. فحص عمق طابور المهام (Queue Depth) وحاويات Cloud Run المخصصة للرندرة.",
            "2. زيادة التوسع الأفقي التلقائي للحاويات (Auto-scaling Cloud Run instances).",
            "3. إبلاغ المستخدم بوضعه في طابور التصدير وإشعاره عبر FCM عند اكتمال الفيديو."
        )
        MonitoredService.GOOGLE_PLAY_BILLING, MonitoredService.APPLE_APP_STORE_BILLING -> listOf(
            "1. فحص خادم التحقق من الرموز (Purchase Token Server-to-Server Verifier).",
            "2. التأكد من حفظ رمز الشراء في جدول المزامنة الآمنة وعدم خصم أي ميزة دون إثبات رسمي.",
            "3. إذا كان المتجر تحت الصيانة، إبقاء حالة الشراء Pending وإعادة المحاولة مع Exponential Backoff."
        )
        else -> listOf(
            "1. فحص سجلات الخدمة في Google Cloud Logging.",
            "2. التأكد من عدم وجود اختناقات في شبكة الاتصال.",
            "3. التحويل للمسار الاحتياطي عند استمرار العطل أكثر من دقيقتين."
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("دليل تشغيل العطل (Runbook) - ${service.displayNameArabic}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("إجراءات الاستجابة والتعامل القياسية (Standard Operating Procedures):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                runbookSteps.forEach { step ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(step, fontSize = 12.sp, modifier = Modifier.padding(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(4.dp))

                Text("إجراءات الطوارئ الفورية:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                Text("في حال تعطل المزود الخارجي أو حدوث تكدس حاد، يمكنك تفعيل قاطع الدائرة لتحويل الحركة آلياً للمسار البديل.", fontSize = 11.sp)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onToggleCircuitBreaker(true, "تفعيل قاطع الدائرة بناءً على دليل التشغيل المعتمد")
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("تفعيل قاطع الدائرة احترازياً")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إغلاق") }
        }
    )
}
