package com.siraj.app.features.settings.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.siraj.app.domain.models.*

@Composable
fun AccountSettings(uiState: SettingsUiState, viewModel: SettingsViewModel, onLogout: () -> Unit) {
    val profile = uiState.profile
    Column(modifier = Modifier.padding(16.dp)) {
        if (profile != null) {
            Text("الاسم: ${profile.name}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("البريد الإلكتروني: ${profile.email}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("الدور: ${profile.role.name}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                Text("تسجيل الخروج")
            }
        } else {
            Text("غير مسجل الدخول")
        }
    }
}

@Composable
fun AppearanceSettings(uiState: SettingsUiState, viewModel: SettingsViewModel) {
    val prefs = uiState.profile?.preferences ?: return
    Column(modifier = Modifier.padding(16.dp)) {
        Text("السمة (Theme)", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = prefs.themeMode == ThemeMode.LIGHT, onClick = { viewModel.updatePreferences { it.copy(themeMode = ThemeMode.LIGHT) } })
            Text("فاتح")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(selected = prefs.themeMode == ThemeMode.DARK, onClick = { viewModel.updatePreferences { it.copy(themeMode = ThemeMode.DARK) } })
            Text("داكن")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(selected = prefs.themeMode == ThemeMode.SYSTEM, onClick = { viewModel.updatePreferences { it.copy(themeMode = ThemeMode.SYSTEM) } })
            Text("تلقائي (حسب النظام)")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("تقليل الحركة (Reduce Motion)")
            Switch(checked = prefs.reduceMotion, onCheckedChange = { viewModel.updatePreferences { p -> p.copy(reduceMotion = it) } })
        }
    }
}

@Composable
fun LanguageSettings(uiState: SettingsUiState, viewModel: SettingsViewModel) {
    val prefs = uiState.profile?.preferences ?: return
    Column(modifier = Modifier.padding(16.dp)) {
        Text("اللغة", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = prefs.language == "ar", onClick = { viewModel.updatePreferences { it.copy(language = "ar") } })
            Text("العربية")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(selected = prefs.language == "en", onClick = { viewModel.updatePreferences { it.copy(language = "en") } })
            Text("English (Coming soon)")
        }
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = prefs.city,
            onValueChange = { viewModel.updatePreferences { p -> p.copy(city = it) } },
            label = { Text("المدينة (لحساب المواقيت)") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun NotificationSettings(uiState: SettingsUiState, viewModel: SettingsViewModel) {
    com.siraj.app.features.notification.presentation.NotificationSettingsScreen(
        onNavigateBack = {}
    )
}

@Composable
fun MihrabSettings(uiState: SettingsUiState, viewModel: SettingsViewModel) {
    val prefs = uiState.profile?.preferences ?: return
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Text("طريقة الحساب", style = MaterialTheme.typography.titleMedium)
            CalculationMethod.values().forEach { method ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = prefs.calculationMethod == method, onClick = { viewModel.updatePreferences { p -> p.copy(calculationMethod = method) } })
                    Text(method.name)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("المذهب", style = MaterialTheme.typography.titleMedium)
            Madhab.values().forEach { madhab ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = prefs.madhab == madhab, onClick = { viewModel.updatePreferences { p -> p.copy(madhab = madhab) } })
                    Text(madhab.name)
                }
            }
        }
    }
}

@Composable
fun VideoSettings(uiState: SettingsUiState, viewModel: SettingsViewModel) {
    val prefs = uiState.profile?.preferences ?: return
    Column(modifier = Modifier.padding(16.dp)) {
        Text("جودة الفيديو", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = prefs.videoQuality == VideoQuality.HIGH, onClick = { viewModel.updatePreferences { p -> p.copy(videoQuality = VideoQuality.HIGH) } })
            Text("عالية")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(selected = prefs.videoQuality == VideoQuality.MEDIUM, onClick = { viewModel.updatePreferences { p -> p.copy(videoQuality = VideoQuality.MEDIUM) } })
            Text("متوسطة")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(selected = prefs.videoQuality == VideoQuality.LOW, onClick = { viewModel.updatePreferences { p -> p.copy(videoQuality = VideoQuality.LOW) } })
            Text("منخفضة (توفير البيانات)")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("التنزيل عبر Wi-Fi فقط")
            Switch(checked = prefs.downloadWifiOnly, onCheckedChange = { viewModel.updatePreferences { p -> p.copy(downloadWifiOnly = it) } })
        }
    }
}

@Composable
fun LibrarySettings(uiState: SettingsUiState, viewModel: SettingsViewModel) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("إعدادات المكتبة ستتوفر قريباً.", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun PrivacySettings(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel,
    onNavigateToActivityHistory: () -> Unit = {}
) {
    val prefs = uiState.profile?.preferences ?: return
    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("قفل التطبيق (App Lock)")
            Switch(checked = prefs.appLockEnabled, onCheckedChange = { viewModel.updatePreferences { p -> p.copy(appLockEnabled = it) } })
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("يمكن تفعيل البصمة أو الوجه في حال تم تفعيل القفل.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text("سجل النشاط والخصوصية", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("التحكم في حفظ مواضع المشاهدة والاستماع وسياسة الاحتفاظ بالبيانات.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onNavigateToActivityHistory,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("إدارة سجل النشاط والمشاهدة")
        }
    }
}

@Composable
fun IslamicSettings(uiState: SettingsUiState, viewModel: SettingsViewModel) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("سياسة المحتوى الشرعي", style = MaterialTheme.typography.titleMedium)
        Text("تطبيق سراج يعتمد على مراجعة بشرية للقرارات الحساسة.", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { /* TODO */ }) {
            Text("قراءة المنهجية الكاملة")
        }
    }
}

@Composable
fun StorageSettings(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel,
    onLogout: () -> Unit,
    onNavigateToActivityHistory: () -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = { /* TODO */ }, modifier = Modifier.fillMaxWidth()) {
            Text("مسح الذاكرة المؤقتة (Cache)")
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onNavigateToActivityHistory, modifier = Modifier.fillMaxWidth()) {
            Text("سجل التنزيلات والمشاهدة")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { /* TODO */ }, modifier = Modifier.fillMaxWidth()) {
            Text("تصدير بياناتي")
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("حذف الحساب نهائياً")
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("حذف الحساب") },
                text = { Text("هل أنت متأكد من رغبتك في حذف الحساب نهائياً؟ سيتم مسح كافة بياناتك ولا يمكن التراجع عن هذا الإجراء.") },
                confirmButton = {
                    Button(
                        onClick = { 
                            showDeleteDialog = false
                            viewModel.deleteAccount(onLogout) 
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("نعم، احذف حسابي")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("إلغاء")
                    }
                }
            )
        }
    }
}

@Composable
fun SupportSettings() {
    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = { /* TODO */ }, modifier = Modifier.fillMaxWidth()) {
            Text("الإبلاغ عن مشكلة تقنية")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { /* TODO */ }, modifier = Modifier.fillMaxWidth()) {
            Text("الإبلاغ عن خطأ شرعي")
        }
    }
}

@Composable
fun AboutSettings() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("تطبيق سراج", style = MaterialTheme.typography.headlineMedium)
        Text("الإصدار: 1.0.0", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(32.dp))
        TextButton(onClick = { /* TODO */ }, modifier = Modifier.fillMaxWidth()) {
            Text("سياسة الخصوصية")
        }
        TextButton(onClick = { /* TODO */ }, modifier = Modifier.fillMaxWidth()) {
            Text("شروط الاستخدام")
        }
    }
}
