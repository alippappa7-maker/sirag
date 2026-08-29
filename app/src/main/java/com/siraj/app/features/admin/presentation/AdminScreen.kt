package com.siraj.app.features.admin.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.siraj.app.data.repository.admin.FirebaseContentManagementRepositoryImpl
import com.siraj.app.data.repository.backup.FirebaseBackupRepositoryImpl
import com.siraj.app.data.repository.monitoring.FirebaseMonitoringRepositoryImpl
import com.siraj.app.features.admin.presentation.backup.BackupRecoveryScreen
import com.siraj.app.features.admin.presentation.backup.BackupRecoveryViewModel
import com.siraj.app.features.admin.presentation.monitoring.MonitoringDashboardScreen
import com.siraj.app.features.admin.presentation.monitoring.MonitoringDashboardViewModel

import com.siraj.app.data.repository.incident.FirebaseIncidentResponseRepositoryImpl
import com.siraj.app.features.admin.presentation.incident.IncidentResponseScreen
import com.siraj.app.features.admin.presentation.incident.IncidentResponseViewModel

@Composable
fun AdminScreen(onNavigateBack: () -> Unit = {}) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("إدارة المحتوى", "تحليلات الاستخدام", "النسخ والاستعادة (DR)", "مراقبة الخدمات", "الاستجابة للحوادث")

    Column(modifier = Modifier.fillMaxWidth()) {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }
        
        when (selectedTabIndex) {
            0 -> {
                val repository = remember { FirebaseContentManagementRepositoryImpl() }
                val viewModel = remember { ContentManagementViewModel(repository) }
                ContentManagementScreen(
                    viewModel = viewModel,
                    onNavigateBack = onNavigateBack
                )
            }
            1 -> {
                AnalyticsDashboardScreen(
                    onNavigateBack = onNavigateBack
                )
            }
            2 -> {
                val backupRepo = remember { FirebaseBackupRepositoryImpl() }
                val backupViewModel = remember { BackupRecoveryViewModel(backupRepo) }
                BackupRecoveryScreen(
                    viewModel = backupViewModel,
                    onNavigateBack = onNavigateBack
                )
            }
            3 -> {
                val monitoringRepo = remember { FirebaseMonitoringRepositoryImpl() }
                val monitoringViewModel = remember { MonitoringDashboardViewModel(monitoringRepo) }
                MonitoringDashboardScreen(
                    viewModel = monitoringViewModel,
                    onNavigateBack = onNavigateBack
                )
            }
            4 -> {
                val incidentRepo = remember { FirebaseIncidentResponseRepositoryImpl() }
                val incidentViewModel = remember { IncidentResponseViewModel(incidentRepo) }
                IncidentResponseScreen(
                    viewModel = incidentViewModel,
                    onNavigateBack = onNavigateBack
                )
            }
        }
    }
}

