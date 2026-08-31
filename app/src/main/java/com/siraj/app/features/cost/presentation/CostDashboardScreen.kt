package com.siraj.app.features.cost.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CostDashboardScreen(
    viewModel: CostDashboardViewModel,
    onNavigateBack: () -> Unit
) {
    val usageState by viewModel.usageState.collectAsState()
    val providerStatuses by viewModel.providerStatuses.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("لوحة التحكم بالتكاليف والحدود") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (usageState == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val usage = usageState ?: return

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("ملخص الاستخدام", style = MaterialTheme.typography.titleLarge)
                
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("الاستخدام الشهري: $${String.format("%.2f", usage.usage.currentMonthlyUsage)} / $${usage.limits.monthlyLimitUsd}")
                        LinearProgressIndicator(
                            progress = { (usage.usage.currentMonthlyUsage / usage.limits.monthlyLimitUsd).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        )
                        if (usage.alerts[80]?.isTriggered == true) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تنبيه: تم تجاوز 80% من الحد الشهري", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
            
            item {
                Text("حالة المزودين (مفاتيح الطوارئ)", style = MaterialTheme.typography.titleLarge)
            }
            
            items(providerStatuses) { status ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(status.provider.name, style = MaterialTheme.typography.titleMedium)
                            Text(if (status.isEnabled) "نشط" else "معطل", color = if (status.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                        }
                        Switch(
                            checked = status.isEnabled,
                            onCheckedChange = { viewModel.toggleProviderStatus(status.provider, it) }
                        )
                    }
                }
            }
        }
    }
}
