package com.siraj.app.features.notification.presentation

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.domain.models.notification.NotificationType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationViewModel =
        viewModel(
            factory = NotificationViewModelFactory(LocalContext.current.applicationContext as Application),
        ),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val prefs = uiState.preferences
    val snackbarHostState = remember { SnackbarHostState() }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            },
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            hasNotificationPermission = isGranted
            if (isGranted) {
                viewModel.sendTestNotification(
                    NotificationType.SYSTEM_MESSAGE,
                    "تم تفعيل الإشعارات بنجاح",
                    "ستصلك التنبيهات المهمة أولاً بأول.",
                )
            }
        }

    var showTestNotificationSheet by remember { mutableStateOf(false) }
    var showQuietHoursDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إعدادات الإشعارات", fontWeight = FontWeight.Bold) },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 8.dp,
            ) {
                Button(
                    onClick = { showTestNotificationSheet = true },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تجربة إرسال إشعار تجريبي", fontWeight = FontWeight.Bold)
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
        ) {
            // Android 13+ Permission Banner
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                item {
                    Card(
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.NotificationsNone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "إذن الإشعارات غير مفعل",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "يرجى منح الإذن لتتمكن من تلقي تنبيهات اكتمال الفيديو، مواقيت الصلاة، واعتمادات المحتوى فور صدورها.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                },
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text("تفعيل الإشعارات الآن")
                            }
                        }
                    }
                }
            }

            // Section 1: Projects & Production
            item {
                NotificationSectionHeader("المشاريع والإنتاج")
            }
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column {
                        NotificationToggleItem(
                            title = "اكتمال تصيير الفيديو",
                            subtitle = "إشعار فوري عند جهوزية الفيديو للتنزيل والمشاركة",
                            icon = Icons.Default.VideoLibrary,
                            checked = prefs.videoGeneration,
                            onCheckedChange = { viewModel.updatePreferences(prefs.copy(videoGeneration = it)) },
                        )
                        HorizontalDivider()
                        NotificationToggleItem(
                            title = "تنبيهات أخطاء التصدير",
                            subtitle = "تنبيه فوري في حال حدوث أي عائق أثناء المعالجة",
                            icon = Icons.Default.ErrorOutline,
                            checked = prefs.exportStatus,
                            onCheckedChange = { viewModel.updatePreferences(prefs.copy(exportStatus = it)) },
                        )
                        HorizontalDivider()
                        NotificationToggleItem(
                            title = "تحديثات وتعليقات المشروع",
                            subtitle = "إشعار بتعليقات المراجعين وتعديلات المشاهد",
                            icon = Icons.Default.Comment,
                            checked = prefs.projectComments,
                            onCheckedChange = { viewModel.updatePreferences(prefs.copy(projectComments = it)) },
                        )
                    }
                }
            }

            // Section 2: Review & Moderation
            item {
                NotificationSectionHeader("المراجعة والاعتماد الشرعي")
            }
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column {
                        NotificationToggleItem(
                            title = "نتائج الاعتماد والتدقيق",
                            subtitle = "إشعار باعتماد المحتوى وحصوله على وسم موثق",
                            icon = Icons.Default.Verified,
                            checked = prefs.reviewResults,
                            onCheckedChange = { viewModel.updatePreferences(prefs.copy(reviewResults = it)) },
                        )
                        HorizontalDivider()
                        NotificationToggleItem(
                            title = "طلبات التدقيق الجديدة",
                            subtitle = "تنبيه المراجعين عند وصول سيناريو أو مشروع جديد",
                            icon = Icons.Default.RateReview,
                            checked = prefs.reviewRequests,
                            onCheckedChange = { viewModel.updatePreferences(prefs.copy(reviewRequests = it)) },
                        )
                    }
                }
            }

            // Section 3: Mihrab & Prayers
            item {
                NotificationSectionHeader("المحراب والصلاة والأذكار")
            }
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column {
                        NotificationToggleItem(
                            title = "تذكير مواقيت الصلاة",
                            subtitle = "تنبيه قبل وعند دخول وقت الصلاة في موقعك",
                            icon = Icons.Default.AccessTime,
                            checked = prefs.prayerReminders,
                            onCheckedChange = { viewModel.updatePreferences(prefs.copy(prayerReminders = it)) },
                        )
                        HorizontalDivider()
                        NotificationToggleItem(
                            title = "أذكار الصباح والمساء",
                            subtitle = "تذكير يومي في الصباح الباكر وقبل الغروب",
                            icon = Icons.Default.MenuBook,
                            checked = prefs.adhkarReminders,
                            onCheckedChange = { viewModel.updatePreferences(prefs.copy(adhkarReminders = it)) },
                        )
                    }
                }
            }

            // Section 4: Content & Flashes
            item {
                NotificationSectionHeader("المحتوى والومضات")
            }
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column {
                        NotificationToggleItem(
                            title = "المحتوى الصوتي والتلاوات الجديدة",
                            subtitle = "إشعار عند إضافة تلاوة خاشعة أو تسجيل جديد",
                            icon = Icons.Default.Headphones,
                            checked = prefs.newAudio,
                            onCheckedChange = { viewModel.updatePreferences(prefs.copy(newAudio = it)) },
                        )
                        HorizontalDivider()
                        NotificationToggleItem(
                            title = "الومضات الدعوية الجديدة",
                            subtitle = "إشعار بالمقاطع القصيرة والهادفة المنشورة حديثاً",
                            icon = Icons.Default.Bolt,
                            checked = prefs.newFlashes,
                            onCheckedChange = { viewModel.updatePreferences(prefs.copy(newFlashes = it)) },
                        )
                    }
                }
            }

            // Section 5: Quiet Hours & Privacy
            item {
                NotificationSectionHeader("وقت الهدوء والخصوصية")
            }
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column {
                        NotificationToggleItem(
                            title = "تفعيل وقت الهدوء",
                            subtitle =
                                if (prefs.quietHoursEnabled) {
                                    "كتم الإشعارات من ${String.format(
                                        "%02d:00",
                                        prefs.quietHoursStartHour,
                                    )} إلى ${String.format("%02d:00", prefs.quietHoursEndHour)}"
                                } else {
                                    "إيقاف الأصوات والتنبيهات غير الحرجة أثناء الليل"
                                },
                            icon = Icons.Default.Bedtime,
                            checked = prefs.quietHoursEnabled,
                            onCheckedChange = { viewModel.updatePreferences(prefs.copy(quietHoursEnabled = it)) },
                        )
                        if (prefs.quietHoursEnabled) {
                            HorizontalDivider()
                            ListItem(
                                headlineContent = { Text("تعديل ساعات الهدوء") },
                                supportingContent = {
                                    Text(
                                        "من ${String.format(
                                            "%02d:%02d",
                                            prefs.quietHoursStartHour,
                                            prefs.quietHoursStartMinute,
                                        )} حتى ${String.format("%02d:%02d", prefs.quietHoursEndHour, prefs.quietHoursEndMinute)}",
                                    )
                                },
                                leadingContent = { Icon(Icons.Default.AccessTime, contentDescription = null) },
                                trailingContent = {
                                    TextButton(onClick = { showQuietHoursDialog = true }) {
                                        Text(
                                            androidx.compose.ui.res
                                                .stringResource(com.siraj.app.R.string.edit),
                                        )
                                    }
                                },
                            )
                        }
                        HorizontalDivider()
                        NotificationToggleItem(
                            title = "حماية المحتوى في شاشة القفل",
                            subtitle = "إخفاء التفاصيل الدينية والشرعية الحساسة في شاشة القفل",
                            icon = Icons.Default.Lock,
                            checked = prefs.hideSensitiveOnLockScreen,
                            onCheckedChange = { viewModel.updatePreferences(prefs.copy(hideSensitiveOnLockScreen = it)) },
                        )
                    }
                }
            }

            // Section 6: System & Marketing (Marketing disabled by default)
            item {
                NotificationSectionHeader("النظام والعروض")
            }
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column {
                        NotificationToggleItem(
                            title = "رسائل وتنبيهات النظام والفوترة",
                            subtitle = "تنبيهات حالة الحساب، الرصيد، والتحديثات التقنية",
                            icon = Icons.Default.Info,
                            checked = prefs.systemMessages,
                            onCheckedChange = { viewModel.updatePreferences(prefs.copy(systemMessages = it)) },
                        )
                        HorizontalDivider()
                        NotificationToggleItem(
                            title = "العروض والرسائل الترويجية",
                            subtitle = "معطل افتراضياً - تفعيله يتيح استقبال العروض والخصومات",
                            icon = Icons.Default.Campaign,
                            checked = prefs.marketingAllowed,
                            onCheckedChange = { viewModel.updatePreferences(prefs.copy(marketingAllowed = it)) },
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Quiet Hours Setup Dialog
    if (showQuietHoursDialog) {
        var startH by remember { mutableStateOf(prefs.quietHoursStartHour) }
        var endH by remember { mutableStateOf(prefs.quietHoursEndHour) }

        AlertDialog(
            onDismissRequest = { showQuietHoursDialog = false },
            title = { Text("ضبط ساعات وقت الهدوء") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("اختر ساعات بدء وانتهاء وقت الهدوء لكتم التنبيهات تلقائياً:")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("وقت البدء (مساءً):")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (startH > 0) startH-- }) {
                                Icon(Icons.Default.Remove, contentDescription = null)
                            }
                            Text(String.format("%02d:00", startH), fontWeight = FontWeight.Bold)
                            IconButton(onClick = { if (startH < 23) startH++ }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("وقت الانتهاء (صباحاً):")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (endH > 0) endH-- }) {
                                Icon(Icons.Default.Remove, contentDescription = null)
                            }
                            Text(String.format("%02d:00", endH), fontWeight = FontWeight.Bold)
                            IconButton(onClick = { if (endH < 23) endH++ }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updatePreferences(
                        prefs.copy(
                            quietHoursStartHour = startH,
                            quietHoursEndHour = endH,
                        ),
                    )
                    showQuietHoursDialog = false
                }) {
                    Text(
                        androidx.compose.ui.res
                            .stringResource(com.siraj.app.R.string.save),
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuietHoursDialog = false }) {
                    Text(
                        androidx.compose.ui.res
                            .stringResource(com.siraj.app.R.string.cancel),
                    )
                }
            },
        )
    }

    // Test Notification Sender Bottom Sheet
    if (showTestNotificationSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTestNotificationSheet = false },
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "اختبار إرسال الإشعارات",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "اختر نوع الإشعار لإرساله محلياً واختبار ظهوره في شريط الإشعارات ومركز التنبيهات:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(8.dp))

                val sampleTypes =
                    listOf(
                        NotificationType.VIDEO_GENERATION_COMPLETED,
                        NotificationType.REVIEW_RESULT,
                        NotificationType.PRAYER_REMINDER,
                        NotificationType.NEW_AUDIO_CONTENT,
                        NotificationType.MORNING_EVENING_ADHKAR,
                        NotificationType.SUBSCRIPTION_BILLING,
                    )

                sampleTypes.forEach { type ->
                    OutlinedCard(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.sendTestNotification(type)
                                    showTestNotificationSheet = false
                                },
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(type.titleAr, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    type.categoryAr,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun NotificationSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
    )
}

@Composable
fun NotificationToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text(subtitle, style = MaterialTheme.typography.bodySmall) },
        leadingContent = {
            Icon(
                icon,
                contentDescription = null,
                tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        },
    )
}
