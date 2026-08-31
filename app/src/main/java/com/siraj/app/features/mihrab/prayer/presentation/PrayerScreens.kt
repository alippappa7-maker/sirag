package com.siraj.app.features.mihrab.prayer.presentation

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.core.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: PrayerViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PrayerViewModel(context.applicationContext as Application) as T
            }
        }
    )

    val timesState by viewModel.prayerTimes.collectAsState()
    val nextPrayer by viewModel.nextPrayer.collectAsState()
    val locationError by viewModel.locationError.collectAsState()

    var showLocationRationale by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مواقيت الصلاة") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = androidx.compose.ui.res.stringResource(com.siraj.app.R.string.back)) }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "إعدادات الصلاة")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = timesState) {
                is Resource.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is Resource.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.updateSettings(viewModel.settings.value) }) {
                            Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.retry))
                        }
                    }
                }
                is Resource.Success -> {
                    val times = state.data
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header info
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(times.dateHijri, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(times.dateGregorian, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${times.meta.city} (طريقة: ${times.meta.method})", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        }

                        // Next Prayer Countdown
                        item {
                            if (nextPrayer != null) {
                                val prayer = nextPrayer!!
                                val remainingSecs = prayer.timeRemainingMs / 1000
                                val hours = remainingSecs / 3600
                                val minutes = (remainingSecs % 3600) / 60
                                val secs = remainingSecs % 60
                                val countdownStr = String.format("%02d:%02d:%02d", hours, minutes, secs)

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("الصلاة القادمة: ${prayer.name}", style = MaterialTheme.typography.titleLarge)
                                        Text(countdownStr, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Text("على الساعة ${prayer.timeStr}", style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            }
                        }
                        
                        // Location Warning
                        item {
                            if (locationError != null) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(locationError ?: "", color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                                        TextButton(onClick = { showLocationRationale = true }) {
                                            Text("تحديد الموقع")
                                        }
                                    }
                                }
                            }
                        }

                        // Prayer List
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val prayers = listOf(
                                    "الفجر" to times.fajr,
                                    "الشروق" to times.sunrise,
                                    "الظهر" to times.dhuhr,
                                    "العصر" to times.asr,
                                    "المغرب" to times.maghrib,
                                    "العشاء" to times.isha
                                )
                                prayers.forEach { (name, time) ->
                                    val isNext = nextPrayer?.name == name
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isNext) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        ),
                                        border = if (isNext) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal)
                                            Text(time, style = MaterialTheme.typography.titleMedium, fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (showLocationRationale) {
            AlertDialog(
                onDismissRequest = { showLocationRationale = false },
                title = { Text("إذن الموقع") },
                text = { Text("يطلب تطبيق سراج الوصول إلى موقعك الحالي (بشكل تقريبي) لتحديد المنطقة الزمنية وخطوط الطول والعرض بدقة، مما يضمن حساب مواقيت الصلاة الصحيحة الخاصة بمدينتك. لا يتم تخزين موقعك الدقيق في خوادمنا.") },
                confirmButton = {
                    Button(onClick = {
                        showLocationRationale = false
                        viewModel.requestLocationUpdate()
                    }) { Text("موافق") }
                },
                dismissButton = {
                    TextButton(onClick = { showLocationRationale = false }) { Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.cancel)) }
                }
            )
        }
    }
}
