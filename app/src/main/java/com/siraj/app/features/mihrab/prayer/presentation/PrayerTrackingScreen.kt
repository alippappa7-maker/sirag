package com.siraj.app.features.mihrab.prayer.presentation

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.domain.models.prayer.PrayerName
import com.siraj.app.domain.models.prayer.PrayerStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTrackingScreen(
    onNavigateBack: () -> Unit,
    viewModel: PrayerTrackingViewModel = viewModel(
        factory = PrayerTrackingViewModel.factory(LocalContext.current.applicationContext as Application)
    ),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تتبع الصلوات") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ملخص اليوم
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("صلوات اليوم", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = "${state.todayRecord.completedCount}/5",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        if (state.todayRecord.completedCount == 5) {
                            Text(
                                "أتممت صلواتك اليوم، تقبل الله",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
            }

            // قائمة الصلوات
            items(PrayerName.entries.toList()) { prayer ->
                PrayerStatusCard(
                    prayerName = prayer,
                    status = state.todayRecord.prayers[prayer] ?: PrayerStatus.PENDING,
                    onMarkPrayed = { viewModel.markPrayer(prayer, PrayerStatus.PRAYED) },
                    onMarkMissed = { viewModel.markPrayer(prayer, PrayerStatus.MISSED) },
                )
            }
        }
    }
}

@Composable
private fun PrayerStatusCard(
    prayerName: PrayerName,
    status: PrayerStatus,
    onMarkPrayed: () -> Unit,
    onMarkMissed: () -> Unit,
) {
    val statusColor = when (status) {
        PrayerStatus.PRAYED -> MaterialTheme.colorScheme.primary
        PrayerStatus.MISSED -> MaterialTheme.colorScheme.error
        PrayerStatus.PENDING -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = prayerName.arabicName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = status.arabicName,
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (status != PrayerStatus.PRAYED) {
                    FilledTonalButton(
                        onClick = onMarkPrayed,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "صليت")
                        Text("صليت", modifier = Modifier.padding(start = 4.dp))
                    }
                }
                if (status == PrayerStatus.PENDING) {
                    OutlinedButton(
                        onClick = onMarkMissed,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "فائتة")
                        Text("فائتة", modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }
        }
    }
}
