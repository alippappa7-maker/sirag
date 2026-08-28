package com.siraj.app.features.admin.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.siraj.app.domain.models.admin.AdminContentItem
import com.siraj.app.domain.models.admin.AdminContentStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentManagementScreen(
    viewModel: ContentManagementViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<String?>(null) }
    var selectedStatus by remember { mutableStateOf<AdminContentStatus?>(null) }
    var selectedItemForLogs by remember { mutableStateOf<AdminContentItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إدارة المحتوى") },
                actions = {
                    IconButton(onClick = { viewModel.exportReport() }) {
                        Icon(Icons.Default.Download, contentDescription = "تصدير تقرير")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Search & Filters
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it 
                    viewModel.updateFilter(query = it)
                },
                label = { Text("بحث...") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedStatus == null,
                    onClick = { 
                        selectedStatus = null
                        viewModel.updateFilter(status = null)
                    },
                    label = { Text("الكل") }
                )
                FilterChip(
                    selected = selectedStatus == AdminContentStatus.PENDING_REVIEW,
                    onClick = { 
                        selectedStatus = AdminContentStatus.PENDING_REVIEW
                        viewModel.updateFilter(status = AdminContentStatus.PENDING_REVIEW)
                    },
                    label = { Text("قيد المراجعة") }
                )
                FilterChip(
                    selected = selectedStatus == AdminContentStatus.APPROVED,
                    onClick = { 
                        selectedStatus = AdminContentStatus.APPROVED
                        viewModel.updateFilter(status = AdminContentStatus.APPROVED)
                    },
                    label = { Text("معتمد") }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (state.error != null) {
                Text(state.error ?: "خطأ", color = MaterialTheme.colorScheme.error)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(state.items) { item ->
                        AdminContentCard(
                            item = item,
                            onApprove = { viewModel.approveContent(item.id) },
                            onSuspend = { viewModel.suspendContent(item.id) },
                            onArchive = { viewModel.archiveContent(item.id) },
                            onRestore = { viewModel.restoreContent(item.id) },
                            onViewLogs = { 
                                selectedItemForLogs = item
                                viewModel.loadAuditLogs(item.id)
                            }
                        )
                    }
                }
            }
        }
    }

    if (selectedItemForLogs != null) {
        AlertDialog(
            onDismissRequest = { selectedItemForLogs = null },
            title = { Text("سجل التدقيق (Audit Logs)") },
            text = {
                LazyColumn {
                    items(state.selectedAuditLogs) { log ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text("الإجراء: ${log.action}", fontWeight = FontWeight.Bold)
                            Text("المستخدم: ${log.performedByUserId} (${log.performedByRole})")
                            val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(log.timestamp))
                            Text("التاريخ: $date", style = MaterialTheme.typography.bodySmall)
                            if (log.previousState != null) Text("السابق: ${log.previousState}", style = MaterialTheme.typography.bodySmall)
                            if (log.newState != null) Text("الجديد: ${log.newState}", style = MaterialTheme.typography.bodySmall)
                            Divider(modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                    if (state.selectedAuditLogs.isEmpty()) {
                        item { Text("لا يوجد سجل لهذا العنصر.") }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedItemForLogs = null }) {
                    Text("إغلاق")
                }
            }
        )
    }

    if (state.reportUrl != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearReportUrl() },
            title = { Text("اكتمل التصدير") },
            text = { Text("تم تصدير التقرير بنجاح. الرابط:\n${state.reportUrl}") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearReportUrl() }) {
                    Text("حسناً")
                }
            }
        )
    }
}

@Composable
fun AdminContentCard(
    item: AdminContentItem,
    onApprove: () -> Unit,
    onSuspend: () -> Unit,
    onArchive: () -> Unit,
    onRestore: () -> Unit,
    onViewLogs: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Badge(containerColor = getStatusColor(item.status)) {
                    Text(item.status.name)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("النوع: ${item.type} | نص شرعي: ${if (item.isReligiousText) "نعم" else "لا"}", style = MaterialTheme.typography.bodySmall)
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(item.createdAt))
            Text("تاريخ الإنشاء: $date | المالك: ${item.ownerId}", style = MaterialTheme.typography.bodySmall)
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (item.status != AdminContentStatus.APPROVED) {
                    IconButton(onClick = onApprove) { Icon(Icons.Default.CheckCircle, "اعتماد", tint = Color.Green) }
                }
                if (item.status != AdminContentStatus.SUSPENDED) {
                    IconButton(onClick = onSuspend) { Icon(Icons.Default.Block, "تعليق", tint = Color.Red) }
                }
                if (item.status != AdminContentStatus.ARCHIVED) {
                    IconButton(onClick = onArchive) { Icon(Icons.Default.Archive, "أرشفة", tint = Color.Gray) }
                } else {
                    IconButton(onClick = onRestore) { Icon(Icons.Default.Restore, "استعادة", tint = Color.Blue) }
                }
                IconButton(onClick = onViewLogs) { Icon(Icons.Default.History, "سجل التدقيق") }
            }
        }
    }
}

fun getStatusColor(status: AdminContentStatus): Color {
    return when(status) {
        AdminContentStatus.APPROVED -> Color(0xFF4CAF50)
        AdminContentStatus.PENDING_REVIEW -> Color(0xFFFF9800)
        AdminContentStatus.SUSPENDED -> Color(0xFFF44336)
        AdminContentStatus.ARCHIVED -> Color(0xFF9E9E9E)
        AdminContentStatus.REJECTED -> Color(0xFFE91E63)
    }
}
