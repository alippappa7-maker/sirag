package com.siraj.app.features.settings.presentation

import com.siraj.app.core.analytics.AnalyticsManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
                Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.logout))
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
        Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.language), style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = prefs.language == "ar", onClick = { 
                viewModel.updatePreferences { it.copy(language = "ar") }
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ar"))
            })
            Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.language_arabic))
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(selected = prefs.language == "en", onClick = { 
                viewModel.updatePreferences { it.copy(language = "en") }
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
            })
            Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.language_english))
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
        
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("مشاركة بيانات الاستخدام")
            Switch(checked = prefs.analyticsOptIn, onCheckedChange = { 
                viewModel.updatePreferences { p -> p.copy(analyticsOptIn = it) } 
                AnalyticsManager.setAnalyticsEnabled(it)
            })
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("نجمع بيانات استخدام مجهولة (بدون IP أو نصوص شخصية) لتحسين جودة التطبيق فقط.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("مشاركة تقارير الأعطال (Crash Reporting)")
            Switch(checked = prefs.crashReportsOptIn, onCheckedChange = { 
                viewModel.updatePreferences { p -> p.copy(crashReportsOptIn = it) } 
                com.siraj.app.core.monitoring.CrashMonitoringManager.setCrashlyticsCollectionEnabled(it)
            })
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("إرسال تقارير الأعطال الفنية تلقائياً للمساعدة في حل المشكلات التقنية مع التزام تام بعدم جمع أي نصوص قرآنية أو أحاديث أو محتوى خاص أو أسرار.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

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
fun IslamicSettings(
    uiState: SettingsUiState, 
    viewModel: SettingsViewModel,
    onNavigateToContentPolicy: () -> Unit = {}
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("سياسة المحتوى والاستخدام", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("تطبيق سراج يعتمد على مراجعة بشرية للقرارات الحساسة ويطبق سياسة استخدام واضحة لضمان سلامة وموثوقية المحتوى.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onNavigateToContentPolicy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("قراءة المنهجية الكاملة للمحتوى")
        }
    }
}

@Composable
fun StorageSettings(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel,
    onLogout: () -> Unit,
    onNavigateToActivityHistory: () -> Unit = {},
    onNavigateToPrivacyCenter: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCacheDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("إدارة التخزين والبيانات", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("التحكم في المساحة التخزينية المحلية وتصدير أو حذف البيانات بأمان.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { showCacheDialog = true }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.CleaningServices, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("مسح الذاكرة المؤقتة (Cache)")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onNavigateToActivityHistory, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Download, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("إدارة التنزيلات والمشاهدة بدون إنترنت")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onNavigateToPrivacyCenter,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Icon(Icons.Default.Security, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("الانتقال إلى مركز الخصوصية وتصدير البيانات")
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onNavigateToPrivacyCenter,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.DeleteForever, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("حذف الحساب نهائياً (مركز الخصوصية)")
        }

        if (showCacheDialog) {
            AlertDialog(
                onDismissRequest = { showCacheDialog = false },
                title = { Text("تفريغ الذاكرة المؤقتة") },
                text = { Text("سيتم مسح الملفات المؤقتة المخزنة محلياً لتسريع التطبيق وتحرير المساحة.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showCacheDialog = false
                            com.siraj.app.core.privacy.PrivacyManager.clearDirectory(context.cacheDir)
                            viewModel.showMessage("تم تفريغ الذاكرة المؤقتة بنجاح")
                        }
                    ) {
                        Text("تفريغ الآن")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCacheDialog = false }) {
                        Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.cancel))
                    }
                }
            )
        }
    }
}

@Composable
fun SupportSettings(
    onNavigateToHelpCenter: () -> Unit = {},
    onNavigateToCreateTicket: (com.siraj.app.domain.models.support.TicketCategory?) -> Unit = {},
    onNavigateToServiceStatus: () -> Unit = {},
    onMessage: (String) -> Unit = {}
) {
    var showCrashDialog by remember { mutableStateOf(false) }
    var showBetaFeedbackDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("الدعم الفني والشكاوى", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onNavigateToHelpCenter,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.HelpCenter, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("فتح مركز المساعدة والأسئلة الشائعة")
        }

        Spacer(modifier = Modifier.height(10.dp))
        
        Button(
            onClick = { showBetaFeedbackDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Icon(androidx.compose.material.icons.Icons.Default.Feedback, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("إرسال تقرير ملاحظات النسخة التجريبية (Beta Feedback)")
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = { onNavigateToCreateTicket(com.siraj.app.domain.models.support.TicketCategory.TECHNICAL_BUG) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("فتح تذكرة مشكلة تقنية أو عطل")
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = { onNavigateToCreateTicket(com.siraj.app.domain.models.support.TicketCategory.SHARIA_CONTENT_ERROR) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("الإبلاغ عن خطأ في نص شرعي")
        }

        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = onNavigateToServiceStatus,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("فحص حالة الخوادم والخدمات")
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text("مراقبة الأعطال والتشخيص (Crashlytics)", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("البيئة: ${com.siraj.app.core.config.EnvironmentConfig.currentEnvironment.displayName}", style = MaterialTheme.typography.bodySmall)
                Text("حالة المراقبة: ${if (com.siraj.app.core.monitoring.CrashMonitoringManager.isCollectionEnabled()) "مفعلة (Active)" else "معطلة (Disabled)"}", style = MaterialTheme.typography.bodySmall)
                Text("نسخة التطبيق: ${com.siraj.app.core.config.EnvironmentConfig.buildIdentifier}", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = {
                com.siraj.app.core.monitoring.CrashMonitoringManager.triggerTestNonFatalError("طلب فحص يدوي من لوحة الإعدادات")
                onMessage("تم إرسال تقرير خطأ تجريبي غير قاتل إلى Crashlytics")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("إرسال تقرير خطأ تجريبي (Non-Fatal Test)")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { showCrashDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text("محاكاة عطل تجريبي (Trigger Test Crash)")
        }

        if (showCrashDialog) {
            AlertDialog(
                onDismissRequest = { showCrashDialog = false },
                title = { Text("تحذير: تجربة إغلاق التطبيق") },
                text = { Text("هذا الإجراء سيقوم بإحداث استثناء برمجي مقصود لإغلاق التطبيق فجأة لاختبار وصول تقرير Crashlytics إلى لوحة التحكم. هل تود المتابعة؟") },
                confirmButton = {
                    Button(
                        onClick = {
                            showCrashDialog = false
                            com.siraj.app.core.monitoring.CrashMonitoringManager.triggerTestCrash()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("نعم، تسبب بالعطل")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCrashDialog = false }) {
                        Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.cancel))
                    }
                }
            )
        }

        if (showBetaFeedbackDialog) {
            com.siraj.app.core.ui.components.BetaFeedbackDialog(
                currentRoute = "settings_support",
                onDismissRequest = { showBetaFeedbackDialog = false }
            )
        }
    }
}

@Composable
fun AccessibilitySettings(uiState: SettingsUiState, viewModel: SettingsViewModel) {
    val prefs = uiState.profile?.preferences ?: return
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Text("الرؤية والتباين العالي", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("وضع التباين العالي (High Contrast)", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "ألوان فائقة التباين متوافقة مع معايير WCAG AAA لقراءة مريحة وواضحة جداً",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = prefs.highContrastMode,
                    onCheckedChange = { viewModel.updatePreferences { p -> p.copy(highContrastMode = it) } }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text("حجم الخط والتكبير", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("اختر نسبة تكبير النصوص في كافة شاشات التطبيق:", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            val scales = listOf(
                1.0f to "100% (قياسي)",
                1.15f to "115% (متوسط)",
                1.30f to "130% (كبير)",
                1.50f to "150% (كبير جداً)"
            )
            
            scales.forEach { (scale, label) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = kotlin.math.abs(prefs.fontScaleMultiplier - scale) < 0.05f,
                        onClick = { viewModel.updatePreferences { p -> p.copy(fontScaleMultiplier = scale) } }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(label, style = MaterialTheme.typography.bodyLarge)
                }
            }

            // Live Preview Card
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "معاينة حجم النص والخط العربي:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ - سِرَاجٌ مُنِيرٌ",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text("الحركة والتأثيرات", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("تقليل الحركة (Reduce Motion)", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "إيقاف التأثيرات الحركية والانتقالات التفاعلية للحد من التشتت أو الإجهاد البصري",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = prefs.reduceMotion,
                    onCheckedChange = { viewModel.updatePreferences { p -> p.copy(reduceMotion = it) } }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text("الوسائط والترجمة والشروحات", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("إظهار الترجمة والشروحات (Captions)", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "عرض النصوص المكتوبة المصاحبة لمشاهد الفيديو تلقائياً لدعم ذوي الإعاقة السمعية",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = prefs.showCaptions,
                    onCheckedChange = { viewModel.updatePreferences { p -> p.copy(showCaptions = it) } }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("التفريغ النصي للصوتيات (Transcripts)", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "توفير زر الوصول السريع لقراءة تفريغ التلاوات والمحاضرات والدروس نصياً",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = prefs.showTranscripts,
                    onCheckedChange = { viewModel.updatePreferences { p -> p.copy(showTranscripts = it) } }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text("قارئات الشاشة والتفاعل اللمسي", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("تحسين قارئات الشاشة (TalkBack / VoiceOver)", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "تضمين أوصاف دلالية تفصيلية لكل الأيقونات والأزرار باللغة العربية",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = prefs.screenReaderOptimized,
                    onCheckedChange = { viewModel.updatePreferences { p -> p.copy(screenReaderOptimized = it) } }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("التنبيهات اللمسية والاهتزاز (Haptic Feedback)", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "استخدام الاهتزاز الخفيف لتأكيد الإجراءات والتنبيهات دون الاعتماد على الصوت وحده",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = prefs.soundAlertsWithHaptic,
                    onCheckedChange = { viewModel.updatePreferences { p -> p.copy(soundAlertsWithHaptic = it) } }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("حالة معايير الوصول في تطبيق سراج", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("✓ متوافق مع معايير WCAG 2.1 AA و AAA للتباين", style = MaterialTheme.typography.bodySmall)
                    Text("✓ دعم كامل لاتجاه القراءة العربي RTL وقارئات TalkBack", style = MaterialTheme.typography.bodySmall)
                    Text("✓ أزرار بمساحة لمس لا تقل عن 48×48 نقطة", style = MaterialTheme.typography.bodySmall)
                    Text("✓ عدم الاعتماد على اللون وحده لتوضيح الحالات", style = MaterialTheme.typography.bodySmall)
                    Text("✓ دعم نصوص بديلة وشروحات وتفريغات نصية للوسائط", style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun AboutSettings() {
    var showBetaDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(com.siraj.app.core.config.EnvironmentConfig.releaseLabel, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            if (com.siraj.app.core.config.EnvironmentConfig.isBeta) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        text = "نسخة تجريبية",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "منصة إسلامية عربية متكاملة لإنتاج ومراجعة المحتوى الهادف",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("معلومات الإصدار والبناء", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text("رقم الإصدار: ${com.siraj.app.core.config.EnvironmentConfig.versionName}", style = MaterialTheme.typography.bodySmall)
                Text("رقم البناء (Build Code): ${com.siraj.app.core.config.EnvironmentConfig.versionCode}", style = MaterialTheme.typography.bodySmall)
                Text("البيئة التشغيلية: ${com.siraj.app.core.config.EnvironmentConfig.currentEnvironment.displayName}", style = MaterialTheme.typography.bodySmall)
                Text("المعرف الشامل: ${com.siraj.app.core.config.EnvironmentConfig.buildIdentifier}", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (com.siraj.app.core.config.EnvironmentConfig.isBeta) {
            Button(
                onClick = { showBetaDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Icon(androidx.compose.material.icons.Icons.Default.Feedback, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("إرسال تقرير ملاحظات للمطورين")
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        TextButton(onClick = { /* TODO */ }, modifier = Modifier.fillMaxWidth()) {
            Text("سياسة الخصوصية وحماية البيانات")
        }
        TextButton(onClick = { /* TODO */ }, modifier = Modifier.fillMaxWidth()) {
            Text("شروط الاستخدام والضوابط الشرعية")
        }
    }

    if (showBetaDialog) {
        com.siraj.app.core.ui.components.BetaFeedbackDialog(
            currentRoute = "settings_about",
            onDismissRequest = { showBetaDialog = false }
        )
    }
}
