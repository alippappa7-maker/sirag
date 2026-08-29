package com.siraj.app.features.mihrab.prayer.presentation

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerSettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: PrayerViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PrayerViewModel(context.applicationContext as Application) as T
            }
        }
    )

    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إعدادات مواقيت الصلاة") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = androidx.compose.ui.res.stringResource(com.siraj.app.R.string.back)) }
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
                Text("الموقع الجغرافي", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("استخدام موقعي الحالي")
                    Switch(
                        checked = settings.useLocation,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(useLocation = it)) }
                    )
                }
                if (!settings.useLocation) {
                    OutlinedTextField(
                        value = settings.city,
                        onValueChange = { viewModel.updateSettings(settings.copy(city = it)) },
                        label = { Text("المدينة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            
            item { Divider() }

            item {
                Text("إعدادات إضافية", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                
                var soundExpanded by remember { mutableStateOf(false) }
                val sounds = listOf("تلقائي (حسب النظام)", "أذان مكة", "أذان المدينة", "أذان الأقصى")
                
                ExposedDropdownMenuBox(
                    expanded = soundExpanded,
                    onExpandedChange = { soundExpanded = !soundExpanded }
                ) {
                    OutlinedTextField(
                        value = sounds[1], // Mock selected
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("صوت الأذان") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = soundExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = soundExpanded,
                        onDismissRequest = { soundExpanded = false }
                    ) {
                        sounds.forEach { name ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    soundExpanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = "0",
                    onValueChange = { },
                    label = { Text("تعديل الدقائق يدويًا (للأمان)") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Text("دقيقة") }
                )
                Text("مفيد إذا كان المسجد القريب منك يتأخر أو يتقدم بدقائق معدودة.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            item { Divider() }

            item {
                Text("طريقة الحساب", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                
                var expanded by remember { mutableStateOf(false) }
                val methods = mapOf(2 to "رابطة العالم الإسلامي", 4 to "جامعة أم القرى (مكة)")
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = methods[settings.methodId] ?: "أخرى",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("الهيئة المعتمدة") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        methods.forEach { (id, name) ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    viewModel.updateSettings(settings.copy(methodId = id))
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("المذهب الحنفي (لصلاة العصر)")
                    Switch(
                        checked = settings.isAsrHanafi,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(isAsrHanafi = it)) }
                    )
                }
            }

            item { Divider() }

            item {
                Text("تنبيهات الصلاة (الأذان)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("يجب تفعيل الإشعارات من إعدادات النظام وتحديد صوت الأذان المناسب لك.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                val prayers = listOf(
                    "الفجر" to settings.fajrNotificationEnabled,
                    "الظهر" to settings.dhuhrNotificationEnabled,
                    "العصر" to settings.asrNotificationEnabled,
                    "المغرب" to settings.maghribNotificationEnabled,
                    "العشاء" to settings.ishaNotificationEnabled
                )

                prayers.forEach { (name, enabled) ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("أذان $name")
                        Switch(
                            checked = enabled,
                            onCheckedChange = { isChecked ->
                                val updated = when (name) {
                                    "الفجر" -> settings.copy(fajrNotificationEnabled = isChecked)
                                    "الظهر" -> settings.copy(dhuhrNotificationEnabled = isChecked)
                                    "العصر" -> settings.copy(asrNotificationEnabled = isChecked)
                                    "المغرب" -> settings.copy(maghribNotificationEnabled = isChecked)
                                    "العشاء" -> settings.copy(ishaNotificationEnabled = isChecked)
                                    else -> settings
                                }
                                viewModel.updateSettings(updated)
                            }
                        )
                    }
                }
            }
            
            item { Divider() }
            
            item {
                Text("أوقات الهدوء (عدم الإزعاج)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("تفعيل وقت الهدوء")
                    Switch(
                        checked = settings.isQuietTimeEnabled,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(isQuietTimeEnabled = it)) }
                    )
                }
                if (settings.isQuietTimeEnabled) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = settings.quietTimeStart,
                            onValueChange = { viewModel.updateSettings(settings.copy(quietTimeStart = it)) },
                            label = { Text("من الساعة") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = settings.quietTimeEnd,
                            onValueChange = { viewModel.updateSettings(settings.copy(quietTimeEnd = it)) },
                            label = { Text("إلى الساعة") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
