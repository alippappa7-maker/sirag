package com.siraj.app.features.settings.presentation.privacy

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.core.privacy.PrivacyManager
import com.siraj.app.domain.models.UserPreferences
import com.siraj.app.domain.models.UserProfile
import com.siraj.app.domain.models.privacy.AccountDeletionRequest
import com.siraj.app.domain.models.privacy.DeletionStatus
import com.siraj.app.domain.models.privacy.StoredDataCategory
import java.io.File
import com.siraj.app.core.error.GlobalErrorHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyCenterScreen(
    userProfile: UserProfile?,
    onUpdatePreferences: ((UserPreferences) -> UserPreferences) -> Unit,
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToActivityHistory: () -> Unit = {},
    onNavigateBack: () -> Unit,
    onAccountDeleted: () -> Unit = {},
    viewModel: PrivacyCenterViewModel = viewModel(
        factory = PrivacyCenterViewModelFactory(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val prefs = userProfile?.preferences ?: UserPreferences()
    val userId = userProfile?.id ?: "user_default"
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userId) {
        viewModel.loadOverview(userId)
    }

    LaunchedEffect(uiState.actionMessage) {
        uiState.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مركز الخصوصية وبيانات المستخدم") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = androidx.compose.ui.res.stringResource(com.siraj.app.R.string.back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
            ) {
                // Account Deletion Status Banner (if active)
                if (prefs.accountDeletionStatus == DeletionStatus.GRACE_PERIOD_ACTIVE.name ||
                    uiState.overview.deletionRequest?.status == DeletionStatus.GRACE_PERIOD_ACTIVE
                ) {
                    item {
                        DeletionGracePeriodBanner(
                            scheduledAt = prefs.accountDeletionScheduledAt ?: uiState.overview.deletionRequest?.scheduledPurgeAt ?: 0L,
                            onCancel = { viewModel.cancelAccountDeletion(userId) }
                        )
                    }
                }

                // Overview Header Card
                item {
                    PrivacyOverviewHeaderCard(
                        overview = uiState.overview,
                        onViewDetails = { viewModel.showDialog(PrivacyDialogType.STORED_DATA_OVERVIEW) }
                    )
                }

                // Section 1: Data Export
                item {
                    PrivacySectionHeader(title = "تصدير البيانات الشخصية (Data Portability)", icon = Icons.Default.Download)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "يمكنك تصدير نسخة كاملة من بياناتك ومشاريعك وسجل نشاطك بصيغة JSON آمنة ومطهرة من أي أسرار أو كلمات مرور.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.exportUserData(context, userId) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !uiState.isExporting
                            ) {
                                if (uiState.isExporting) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("جاري استخراج البيانات...")
                                } else {
                                    Icon(Icons.Default.FileDownload, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("تصدير جميع بياناتي (JSON)")
                                }
                            }
                        }
                    }
                }

                // Section 2: Storage, History & Downloads Management
                item {
                    PrivacySectionHeader(title = "إدارة السجلات والتخزين المؤقت", icon = Icons.Default.Storage)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            PrivacyActionRow(
                                title = "مسح سجل المشاهدة والتلاوات",
                                subtitle = "حذف كافة مواضع الاستماع والتلاوة والمشاهدة المحفوظة في حسابك.",
                                buttonText = "مسح السجل",
                                isDestructive = false,
                                onClick = { viewModel.showDialog(PrivacyDialogType.CLEAR_HISTORY_CONFIRM) }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            PrivacyActionRow(
                                title = "حذف المقاطع المحملة (Downloads)",
                                subtitle = "تفريغ الملفات الصوتية والمرئية المحفوظة بدون إنترنت (${PrivacyManager.formatBytes(uiState.overview.downloadsSizeBytes)}).",
                                buttonText = "حذف التنزيلات",
                                isDestructive = false,
                                onClick = { viewModel.showDialog(PrivacyDialogType.CLEAR_DOWNLOADS_CONFIRM) }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            PrivacyActionRow(
                                title = "تفريغ الذاكرة المؤقتة (Cache)",
                                subtitle = "مسح ملفات الصور والتخزين المؤقت المحلي (${PrivacyManager.formatBytes(uiState.overview.cacheSizeBytes)}).",
                                buttonText = "تفريغ الذاكرة",
                                isDestructive = false,
                                onClick = { viewModel.showDialog(PrivacyDialogType.CLEAR_CACHE_CONFIRM) }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            OutlinedButton(
                                onClick = onNavigateToActivityHistory,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.History, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("الانتقال إلى سجل النشاط الكامل والمزامنة")
                            }
                        }
                    }
                }

                // Section 3: Telemetry, Personalization & Location
                item {
                    PrivacySectionHeader(title = "التتبع والموقع والتخصيص", icon = Icons.Default.Security)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Analytics Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                    Text("مشاركة بيانات الاستخدام المجهولة (Analytics)", style = MaterialTheme.typography.titleSmall)
                                    Text("مساعدة فريق سراج في تحسين التطبيق دون جمع أي مدخلات شخصية أو نصوص قرآنية.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = prefs.analyticsOptIn,
                                    onCheckedChange = { isChecked ->
                                        viewModel.toggleAnalytics(isChecked) {
                                            onUpdatePreferences { p -> p.copy(analyticsOptIn = isChecked) }
                                        }
                                    }
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            // Crashlytics Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                    Text("تقارير الأعطال الفنية (Crashlytics)", style = MaterialTheme.typography.titleSmall)
                                    Text("إرسال تقارير الأعطال البرمجية مع تشفير كامل للمعرفات وحجب صارم للبيانات الشخصية.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = prefs.crashReportsOptIn,
                                    onCheckedChange = { isChecked ->
                                        viewModel.toggleCrashReports(isChecked) {
                                            onUpdatePreferences { p -> p.copy(crashReportsOptIn = isChecked) }
                                        }
                                    }
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            // Personalization Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                    Text("تخصيص المحتوى والتوصيات (Personalization)", style = MaterialTheme.typography.titleSmall)
                                    Text("عند التعطيل، يتم عرض المحتوى وفق الترتيب الزمني والتحريري العام فقط.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = prefs.personalizationOptIn,
                                    onCheckedChange = { isChecked ->
                                        viewModel.togglePersonalization(isChecked) {
                                            onUpdatePreferences { p -> p.copy(personalizationOptIn = isChecked) }
                                        }
                                    }
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            // Location Opt-in
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                    Text("استخدام الموقع لمواقيت الصلاة والقبلة", style = MaterialTheme.typography.titleSmall)
                                    Text("تحديد اتجاه القبلة ومواقيت الأذان. تُعالج البيانات محلياً دون إرسالها لأي طرف ثالث.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = prefs.locationOptIn,
                                    onCheckedChange = { isChecked ->
                                        viewModel.toggleLocationOptIn(isChecked) {
                                            onUpdatePreferences { p -> p.copy(locationOptIn = isChecked) }
                                        }
                                    }
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            // Precise vs Approximate Location
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                    Text("استخدام الموقع التقريبي فقط (موصى به)", style = MaterialTheme.typography.titleSmall)
                                    Text("الاعتماد على اسم المدينة أو الإحداثيات التقريبية بدلاً من GPS الدقيق لتعزيز الخصوصية.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = !prefs.preciseLocationOptIn,
                                    onCheckedChange = { isApproximate ->
                                        val isPrecise = !isApproximate
                                        viewModel.togglePreciseLocation(isPrecise) {
                                            onUpdatePreferences { p -> p.copy(preciseLocationOptIn = isPrecise) }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Section 4: Notifications Shortcut
                item {
                    PrivacySectionHeader(title = "إدارة الإشعارات والصلاحيات", icon = Icons.Default.Notifications)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("التحكم الكامل في إشعارات الأذان، الأذكار، تنبيهات المراجعة الشرعية، وحالة المشاريع.", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = onNavigateToNotifications,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("تخصيص تفضيلات الإشعارات")
                            }
                        }
                    }
                }

                // Section 5: Data Correction, Retention Policy & Legal Documents
                item {
                    PrivacySectionHeader(title = "حقوق المستخدم والسياسات القانونية", icon = Icons.Default.Gavel)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { viewModel.showDialog(PrivacyDialogType.DATA_CORRECTION) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(Icons.Default.EditNote, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("طلب تصحيح أو تعديل البيانات (Right of Rectification)")
                            }

                            OutlinedButton(
                                onClick = { viewModel.showDialog(PrivacyDialogType.RETENTION_POLICY_DETAILS) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Policy, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("سياسة الاحتفاظ بالبيانات وفترات التخزين")
                            }

                            OutlinedButton(
                                onClick = { viewModel.showDialog(PrivacyDialogType.TERMS_AND_PRIVACY_VIEWER) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("اتفاقية الخصوصية وشروط الاستخدام")
                            }
                        }
                    }
                }

                // Section 6: Dangerous Zone - Account Deletion
                item {
                    PrivacySectionHeader(
                        title = "المنطقة الحساسة - حذف الحساب",
                        icon = Icons.Default.Warning,
                        titleColor = MaterialTheme.colorScheme.error
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error.copy(alpha = 0.5f)))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "حذف الحساب نهائياً وإزالة كافة البيانات الشخصية والمشاريع والسجلات المرتبطة بك.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "تتيح المنصة فترة سماح لمدة 14 يوماً لإمكانية استعادة الحساب قبل التطهير النهائي للبيانات، مع التزامنا بالاحتفاظ بالملخصات المالية المشفرة لأغراض الامتثال الضريبي فقط.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.showDialog(PrivacyDialogType.DELETE_ACCOUNT_WARNING) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("بدء إجراءات حذف الحساب")
                            }
                        }
                    }
                }
            }

            // Dialogs
            when (uiState.activeDialog) {
                PrivacyDialogType.STORED_DATA_OVERVIEW -> {
                    StoredDataOverviewDialog(
                        categories = uiState.overview.categories,
                        onDismiss = { viewModel.dismissDialog() }
                    )
                }
                PrivacyDialogType.EXPORT_SUCCESS -> {
                    ExportSuccessDialog(
                        checksum = uiState.lastExportChecksum ?: "",
                        file = uiState.exportedFile,
                        jsonContent = uiState.exportedJson ?: "",
                        onDismiss = { viewModel.dismissDialog() },
                        onShare = { file -> shareExportedFile(context, file) }
                    )
                }
                PrivacyDialogType.CLEAR_HISTORY_CONFIRM -> {
                    ConfirmationActionDialog(
                        title = "مسح سجل المشاهدة والاستماع",
                        message = "هل أنت متأكد من رغبتك في مسح كافة مواضع الاستماع والتلاوة ومقاطع الفيديو من حسابك؟ لن يؤثر هذا على مشاريعك الخاصة.",
                        confirmButtonText = "نعم، امسح السجل",
                        onConfirm = { viewModel.clearWatchHistory(userId) },
                        onDismiss = { viewModel.dismissDialog() }
                    )
                }
                PrivacyDialogType.CLEAR_DOWNLOADS_CONFIRM -> {
                    ConfirmationActionDialog(
                        title = "حذف كافة المقاطع المحملة",
                        message = "سيتم حذف جميع الملفات الصوتية والمرئية المخزنة للاستخدام بدون إنترنت لتوفير مساحة التخزين على جهازك.",
                        confirmButtonText = "نعم، احذف التنزيلات",
                        onConfirm = { viewModel.clearDownloads(userId) },
                        onDismiss = { viewModel.dismissDialog() }
                    )
                }
                PrivacyDialogType.CLEAR_CACHE_CONFIRM -> {
                    ConfirmationActionDialog(
                        title = "تفريغ الذاكرة المؤقتة (Cache)",
                        message = "سيتم مسح الملفات المؤقتة المخزنة محلياً لتسريع التطبيق وتحرير المساحة. لن يتم حذف أي بيانات أو مشاريع سحابية.",
                        confirmButtonText = "تفريغ الآن",
                        onConfirm = { viewModel.clearAppCache(context) },
                        onDismiss = { viewModel.dismissDialog() }
                    )
                }
                PrivacyDialogType.DATA_CORRECTION -> {
                    DataCorrectionDialog(
                        onDismiss = { viewModel.dismissDialog() },
                        onSubmit = { field, current, req, reason ->
                            viewModel.submitDataCorrection(userId, field, current, req, reason)
                        }
                    )
                }
                PrivacyDialogType.DELETE_ACCOUNT_WARNING -> {
                    AccountDeletionWarningDialog(
                        onDismiss = { viewModel.dismissDialog() },
                        onProceed = { viewModel.showDialog(PrivacyDialogType.DELETE_ACCOUNT_CONFIRM) }
                    )
                }
                PrivacyDialogType.DELETE_ACCOUNT_CONFIRM -> {
                    AccountDeletionConfirmDialog(
                        onDismiss = { viewModel.dismissDialog() },
                        onConfirmDelete = { reason, graceDays ->
                            viewModel.requestAccountDeletion(userId, reason, graceDays) {
                                onUpdatePreferences { p ->
                                    p.copy(
                                        accountDeletionStatus = DeletionStatus.GRACE_PERIOD_ACTIVE.name,
                                        accountDeletionScheduledAt = System.currentTimeMillis() + (graceDays * 86400000L)
                                    )
                                }
                                onAccountDeleted()
                            }
                        }
                    )
                }
                PrivacyDialogType.RETENTION_POLICY_DETAILS -> {
                    RetentionPolicyDialog(
                        policies = uiState.overview.categories,
                        onDismiss = { viewModel.dismissDialog() }
                    )
                }
                PrivacyDialogType.TERMS_AND_PRIVACY_VIEWER -> {
                    TermsAndPrivacyPolicyViewerDialog(
                        onDismiss = { viewModel.dismissDialog() }
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
fun DeletionGracePeriodBanner(
    scheduledAt: Long,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "طلب حذف الحساب قيد المعالجة (فترة السماح نشطة)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "تم قفل حسابك وسيبدأ التطهير النهائي للبيانات في: ${PrivacyManager.formatDate(scheduledAt)}. يمكنك إلغاء الطلب في أي وقت خلال فترة السماح واستئناف استخدام حسابك.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Undo, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إلغاء طلب الحذف واستعادة الحساب")
            }
        }
    }
}

@Composable
fun PrivacyOverviewHeaderCard(
    overview: com.siraj.app.domain.models.privacy.PrivacyOverviewData,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ملخص بياناتك في سراج",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "تحكم كامل وشفافية مطلقة وفق الضوابط الشرعية وحقوق الخصوصية.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                OverviewStatItem(
                    label = "المشاريع النشطة",
                    value = "${overview.projectsCount}",
                    icon = Icons.Default.VideoLibrary
                )
                OverviewStatItem(
                    label = "المساحة المحلية",
                    value = PrivacyManager.formatBytes(overview.totalStorageBytes),
                    icon = Icons.Default.Storage
                )
                OverviewStatItem(
                    label = "عناصر التنزيل",
                    value = "${overview.downloadsCount}",
                    icon = Icons.Default.DownloadDone
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onViewDetails,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ListAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("استعراض تفاصيل ما تحفظه المنصة ومواقع التخزين")
            }
        }
    }
}

@Composable
fun OverviewStatItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PrivacySectionHeader(title: String, icon: ImageVector, titleColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = titleColor, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = titleColor)
    }
}

@Composable
fun PrivacyActionRow(
    title: String,
    subtitle: String,
    buttonText: String,
    isDestructive: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (isDestructive) {
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(buttonText)
            }
        } else {
            OutlinedButton(onClick = onClick) {
                Text(buttonText)
            }
        }
    }
}

// Dialogs

@Composable
fun StoredDataOverviewDialog(
    categories: List<StoredDataCategory>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تفاصيل البيانات المخزنة ومواقعها")
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(categories) { cat ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(cat.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(cat.description, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("موقع التخزين: ${cat.storageLocation}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text("سياسة الاحتفاظ: ${cat.retentionPolicy}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.close))
            }
        }
    )
}

@Composable
fun ExportSuccessDialog(
    checksum: String,
    file: File?,
    jsonContent: String,
    onDismiss: () -> Unit,
    onShare: (File) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تم استخراج بياناتك بنجاح")
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text("تم إعداد وتطهير ملف التصدير الخاص بحسابك بصيغة JSON القياسية.")
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("بصمة سلامة البيانات (SHA-256):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(checksum, style = MaterialTheme.typography.bodySmall, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("حجم الملف: ${file?.length()?.let { PrivacyManager.formatBytes(it) } ?: "جاهز"}", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "يحتوي الملف على ملفك التعريفي، ومشاريعك، وسجل تلاواتك ومشاهدتك، دون أي كلمات مرور أو مفاتيح سرية.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    file?.let { onShare(it) } ?: onDismiss()
                }
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("مشاركة أو حفظ الملف")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("تم")
            }
        }
    )
}

@Composable
fun ConfirmationActionDialog(
    title: String,
    message: String,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                }
            ) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.cancel))
            }
        }
    )
}

@Composable
fun DataCorrectionDialog(
    onDismiss: () -> Unit,
    onSubmit: (fieldName: String, currentValue: String, requestedValue: String, reason: String) -> Unit
) {
    var fieldName by remember { mutableStateOf("الاسم الكامل") }
    var currentValue by remember { mutableStateOf("") }
    var requestedValue by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("طلب تصحيح البيانات الشخصية") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("يحق لك قانونياً وشرعياً طلب تصحيح أي بيانات غير دقيقة مخزنة في حسابك.", style = MaterialTheme.typography.bodySmall)
                
                OutlinedTextField(
                    value = fieldName,
                    onValueChange = { fieldName = it },
                    label = { Text("الحقل المراد تصحيحه") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = currentValue,
                    onValueChange = { currentValue = it },
                    label = { Text("القيمة الحالية غير الدقيقة") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = requestedValue,
                    onValueChange = { requestedValue = it },
                    label = { Text("القيمة الصحيحة المطلوبة") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("سبب التصحيح") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fieldName.isNotBlank() && requestedValue.isNotBlank()) {
                        onSubmit(fieldName, currentValue, requestedValue, reason)
                    }
                },
                enabled = fieldName.isNotBlank() && requestedValue.isNotBlank()
            ) {
                Text("إرسال الطلب")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.cancel))
            }
        }
    )
}

@Composable
fun AccountDeletionWarningDialog(
    onDismiss: () -> Unit,
    onProceed: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text("حذف الحساب - الخطوة 1 من 2")
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text(
                    text = "تنبيه هام حول حذف الحساب والبيانات:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("1. سيتم مسح كافة مشاريعك ومسوداتك وتوليدات الذكاء الاصطناعي نهائياً.", style = MaterialTheme.typography.bodySmall)
                Text("2. سيتم مسح سجل النشاط، ومواضع التلاوة، والمفضلة، والتنزيلات.", style = MaterialTheme.typography.bodySmall)
                Text("3. سيتم قفل الحساب فوراً وإعطاء فترة سماح (Grace Period) لمدة 14 يوماً لإمكانية التراجع قبل التطهير الدائم.", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("الامتثال المالي والقانوني:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "يتم الاحتفاظ بملخص المعاملات المالية والفواتير الضريبية بصيغة مجهولة الهوية (Anonymized) لمدة 5 سنوات التزاماً بالأنظمة المالية وقوانين التجارة الإلكترونية، دون ربطها بهويتك بعد الحذف.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onProceed,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("أفهم ذلك، متابعة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.cancel))
            }
        }
    )
}

@Composable
fun AccountDeletionConfirmDialog(
    onDismiss: () -> Unit,
    onConfirmDelete: (reason: String, graceDays: Int) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var confirmationText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تأكيد حذف الحساب نهائياً") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("لإتمام العملية، يرجى كتابة كلمة \"حذف\" في الحقل أدناه لتأكيد رغبتك:", style = MaterialTheme.typography.bodySmall)
                
                OutlinedTextField(
                    value = confirmationText,
                    onValueChange = { confirmationText = it },
                    label = { Text("اكتب كلمة: حذف") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("سبب المغادرة (اختياري)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (confirmationText.trim() == "حذف") {
                        onConfirmDelete(reason, 14)
                    }
                },
                enabled = confirmationText.trim() == "حذف",
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("تأكيد طلب الحذف")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.undo))
            }
        }
    )
}

@Composable
fun RetentionPolicyDialog(
    policies: List<StoredDataCategory>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("سياسة الاحتفاظ بالبيانات") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "يلتزم تطبيق سراج بمبدأ تصغير البيانات (Data Minimization) وعدم الاحتفاظ بأي بيانات تتجاوز الغرض التشغيلي والشرعي المحدد لها.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(policies) { p ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(p.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("المدة: ${p.retentionPolicy}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            Text("المكان: ${p.storageLocation}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.close))
            }
        }
    )
}

@Composable
fun TermsAndPrivacyPolicyViewerDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("اتفاقية الخصوصية وشروط الاستخدام") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("منصة سراج - الميثاق الشرعي والأخلاقي للخصوصية", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    "1. الالتزام بالأمانة الرقمية: بياناتك ومشاريعك وتلاواتك أمانة لا يتم بيعها أو مشاركتها مع أطراف خارجية لأغراض إعلانية أو استهداف تجاري.",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "2. الذكاء الاصطناعي كمساعد إنتاج: لا تُستخدم نصوصك أو مشاريعك الخاصة لتدريب نماذج الذكاء الاصطناعي العامة دون إذن صريح ومواثيق حماية معتمدة.",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "3. الشفافية والموقع: لا نحتفظ بالموقع الجغرافي الدقيق على خوادمنا إطلاقاً، وتُحسب مواقيت الصلاة واتجاه القبلة محلياً على جهازك.",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "4. حقوق التصدير والحذف: لك الحق الكامل في تصدير بياناتك أو حذف حسابك دون أي عوائق برمجية.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("موافق")
            }
        }
    )
}

private fun shareExportedFile(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة ملف تصدير بيانات سراج"))
    } catch (e: Exception) {
            GlobalErrorHandler.handle(e)
        // Fallback: simple text share if FileProvider is not configured
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, file.readText())
        }
        context.startActivity(Intent.createChooser(intent, "تصدير بيانات سراج"))
    }
}
