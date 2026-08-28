package com.siraj.app.features.mihrab.calendar.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HijriCalendarScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: HijriCalendarViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    
    var showSettingsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("التقويم الإسلامي") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع") }
                },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "إعدادات التاريخ")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("اليوم", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(state.currentHijriDate, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text(state.currentGregorianDate, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            
            item {
                Text("المناسبات الإسلامية", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            
            items(state.events) { event ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (event.isUpcoming) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                    ),
                    border = if (event.isUpcoming) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(event.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(event.dateHijri, style = MaterialTheme.typography.bodyMedium)
                            Text("المصدر: ${event.source}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (event.isUpcoming) {
                            IconButton(onClick = { /* TODO: Notification */ }) {
                                Icon(Icons.Default.Notifications, contentDescription = "تذكير")
                            }
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "ملاحظة: تختلف بدايات الأشهر الهجرية حسب الرؤية الشرعية في كل بلد. لا نعتمد هذه التواريخ كأحكام شرعية قاطعة وإنما للحسابات التقريبية.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (showSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = { Text("تعديل اليوم الهجري") },
                text = { 
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("إذا كان التاريخ الهجري في منطقتك يختلف عن الحساب الفلكي، يمكنك تعديله هنا:")
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                            Button(onClick = { viewModel.setDayAdjustment(state.dayAdjustment - 1) }) { Text("-1") }
                            Text(state.dayAdjustment.toString(), style = MaterialTheme.typography.titleLarge)
                            Button(onClick = { viewModel.setDayAdjustment(state.dayAdjustment + 1) }) { Text("+1") }
                        }
                        Button(
                            onClick = { viewModel.setDayAdjustment(0) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) { Text("إعادة الضبط") }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSettingsDialog = false }) { Text("إغلاق") }
                }
            )
        }
    }
}
