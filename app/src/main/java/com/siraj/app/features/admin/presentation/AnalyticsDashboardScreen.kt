package com.siraj.app.features.admin.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.core.analytics.AnalyticsManager
import com.siraj.app.domain.models.analytics.AnalyticsLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AnalyticsDashboardViewModel : ViewModel() {
    private val _logs = MutableStateFlow<List<AnalyticsLog>>(emptyList())
    val logs: StateFlow<List<AnalyticsLog>> = _logs.asStateFlow()

    init {
        viewModelScope.launch {
            AnalyticsManager.getRepository().getAggregatedEvents().collect { logsList ->
                _logs.value = logsList
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsDashboardScreen(
    onNavigateBack: () -> Unit,
    viewModel: AnalyticsDashboardViewModel = viewModel(),
) {
    val logs by viewModel.logs.collectAsState()
    val eventCounts = logs.groupingBy { it.event }.eachCount()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("لوحة تحليلات الاستخدام (مجمعة)") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription =
                                androidx.compose.ui.res
                                    .stringResource(com.siraj.app.R.string.back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {
            item {
                Text(
                    text = "ملخص الأحداث",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }

            items(eventCounts.entries.toList()) { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(entry.key, style = MaterialTheme.typography.bodyLarge)
                        Text(entry.value.toString(), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "أحدث السجلات (بدون معلومات حساسة)",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }

            items(logs.take(50)) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("الحدث: ${log.event}", style = MaterialTheme.typography.titleMedium)
                        Text("المستخدم: ${log.hashedUserId ?: "مجهول"}", style = MaterialTheme.typography.bodySmall)

                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                        Text("الوقت: ${sdf.format(Date(log.timestamp))}", style = MaterialTheme.typography.bodySmall)

                        if (log.properties.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("الخصائص:", style = MaterialTheme.typography.labelMedium)
                            log.properties.forEach { (k, v) ->
                                Text("- $k: $v", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
