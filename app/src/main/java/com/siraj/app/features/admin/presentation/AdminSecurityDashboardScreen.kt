package com.siraj.app.features.admin.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSecurityDashboardScreen(
    viewModel: AdminSecurityViewModel,
    onNavigateBack: () -> Unit,
) {
    val config by viewModel.config.collectAsState()
    val activeSessions by viewModel.activeSessions.collectAsState()

    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("أمان الإدارة (Admin Security)") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
            )
        },
    ) { paddingValues ->
        if (config == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val securityConfig = config!!
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ملف الأمان الخاص بك", style = MaterialTheme.typography.titleLarge)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("الدور: ${securityConfig.role.name}")
                        Text("حالة المصادقة الثنائية (MFA): ${if (securityConfig.isMfaEnabled) "مفعل" else "معطل"}")
                        Text("إعادة التحقق للعمليات الحساسة: ${if (securityConfig.requireReAuthForSensitiveOps) "مطلوب" else "غير مطلوب"}")
                    }
                }
            }

            item {
                Text("الجلسات النشطة", style = MaterialTheme.typography.titleLarge)
            }

            items(activeSessions) { session ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("الجهاز: ${session.device.deviceName}", style = MaterialTheme.typography.titleMedium)
                        Text("IP: ${session.device.lastIpAddress ?: "غير معروف"}")
                        Text("تاريخ البداية: ${dateFormatter.format(Date(session.startedAt))}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.revokeSession(session.sessionId) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        ) {
                            Text("إنهاء الجلسة (Revoke)")
                        }
                    }
                }
            }
        }
    }
}
