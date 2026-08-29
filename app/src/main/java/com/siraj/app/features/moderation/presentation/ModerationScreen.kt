package com.siraj.app.features.moderation.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.siraj.app.domain.models.community.Report
import com.siraj.app.core.ui.components.SirajButton
import androidx.compose.material3.OutlinedTextField

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
        viewModel.loadReports(currentUserRole)
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
                title = { Text("إدارة المحتوى والبلاغات") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = androidx.compose.ui.res.stringResource(com.siraj.app.R.string.back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (state.isLoading && state.reports.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.reports.isEmpty()) {
                Text(
                    "لا توجد بلاغات معلقة لمراجعتها.",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.reports) { report ->
                        ReportCard(
                            report = report,
                            onResolve = { resolution, notes ->
                                viewModel.resolveReport(report.id, currentUserId, resolution, notes, currentUserRole)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReportCard(
    report: Report,
    onResolve: (String, String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var selectedResolution by remember { mutableStateOf("DISMISS") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("نوع البلاغ: ${report.reportType.titleArabic}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(4.dp))
            Text("الهدف: ${report.targetType.name} - ${report.targetId}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text("التفاصيل: ${report.description}", style = MaterialTheme.typography.bodyMedium)
            
            Spacer(modifier = Modifier.height(16.dp))
            SirajButton(
                text = "اتخاذ إجراء",
                onClick = { showDialog = true },
                modifier = Modifier.align(Alignment.End)
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("القرار الإداري") },
            text = {
                Column {
                    Text("حدد الإجراء المناسب:")
                    val options = listOf("DISMISS" to "تجاهل (بلاغ غير صحيح)", "TAKE_DOWN" to "حذف المحتوى", "WARN_USER" to "توجيه إنذار", "SUSPEND_USER" to "إيقاف الحساب")
                    options.forEach { (value, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedResolution == value,
                                onClick = { selectedResolution = value }
                            )
                            Text(label)
                        }
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
                    onResolve(selectedResolution, notes)
                    showDialog = false
                }) {
                    Text("تأكيد")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.cancel))
                }
            }
        )
    }
}
