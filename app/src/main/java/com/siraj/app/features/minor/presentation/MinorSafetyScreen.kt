package com.siraj.app.features.minor.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siraj.app.domain.models.minor.*
import com.siraj.app.ui.theme.statusColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinorSafetyScreen(
    viewModel: MinorSafetyViewModel,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf(
        "ضوابط الأمان",
        "بوابة ولي الأمر",
        "البلاغات الطارئة",
        "فاحص المحتوى التعليمي",
        "حذف البيانات"
    )

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissUserMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "حماية القاصرين وسلامة الأطفال",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ضوابط صارمة للخصوصية، منع التتبع، وحظر استغلال الأطفال",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_back_minor_safety")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "الرجوع")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.openReportDialog() },
                        modifier = Modifier.testTag("btn_open_emergency_report")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReportProblem,
                            contentDescription = "إبلاغ طارئ",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Store Age Rating Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "تصنيف المتجر الرسمي: Everyone 10+ / PEGI 3 (محتوى آمن)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "التطبيق في MVP موجه لصناع المحتوى والبالغين، مع وضع حماية إلزامي لأي حساب قاصر لمنع التتبع وجمع الموقع والرسائل الخاصة.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Tab Bar
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("tab_minor_safety_$index")
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> PolicyAndGuardrailsTab(
                    uiState = uiState,
                    onSelectBracket = { bracket -> viewModel.setUserAgeBracket(bracket) }
                )
                1 -> ParentalConsentPortalTab(
                    uiState = uiState,
                    onRequestConsentClick = { viewModel.openConsentDialog() },
                    onVerifyClick = { id -> viewModel.openVerifyOtpDialog(id) },
                    onRevokeClick = { id -> viewModel.revokeParentalConsent(id) }
                )
                2 -> EmergencyReportsTab(
                    uiState = uiState,
                    onNewReportClick = { viewModel.openReportDialog() }
                )
                3 -> EducationalAuditorTab(
                    uiState = uiState,
                    onEvaluateClick = { title, text -> viewModel.evaluateEducationalContent(title, text) }
                )
                4 -> DataErasureTab(
                    uiState = uiState,
                    onPurgeClick = { viewModel.openPurgeConfirmDialog() }
                )
            }
        }
    }

    // Dialogs
    if (uiState.showConsentDialog) {
        RequestParentalConsentDialog(
            onDismiss = { viewModel.closeConsentDialog() },
            onSubmit = { email, name -> viewModel.requestParentalConsent(email, name) }
        )
    }

    if (uiState.showVerifyOtpDialog && uiState.selectedConsentIdToVerify != null) {
        val consentId = uiState.selectedConsentIdToVerify!!
        VerifyOtpDialog(
            consentId = consentId,
            onDismiss = { viewModel.closeVerifyOtpDialog() },
            onSubmit = { code -> viewModel.verifyParentalConsent(consentId, code) }
        )
    }

    if (uiState.showReportDialog) {
        EmergencyReportDialog(
            onDismiss = { viewModel.closeReportDialog() },
            onSubmit = { type, desc, user, content ->
                viewModel.submitEmergencyChildSafetyReport(type, desc, user, content)
            }
        )
    }

    if (uiState.showPurgeConfirmDialog) {
        PurgeConfirmDialog(
            onDismiss = { viewModel.closePurgeConfirmDialog() },
            onConfirm = { viewModel.purgeMinorData() }
        )
    }
}

@Composable
private fun PolicyAndGuardrailsTab(
    uiState: MinorSafetyUiState,
    onSelectBracket: (UserAgeBracket) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "الفئة العمرية الحالية للحساب:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UserAgeBracket.values().filter { it != UserAgeBracket.UNSPECIFIED }.forEach { bracket ->
                    val isSelected = uiState.ageBracket == bracket
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectBracket(bracket) },
                        label = { Text(bracket.titleArabic) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        modifier = Modifier.testTag("chip_bracket_${bracket.code}")
                    )
                }
            }
        }

        item {
            Text(
                text = "ضوابط الحماية المفروضة برمجياً:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            SafetyGuardrailRow(
                title = "الحساب خاص افتراضياً (Private by Default)",
                description = "حظر ظهور حساب القاصر أو مشاريعه في الاكتشاف العام دون إذن صريح.",
                isActive = uiState.policy.isPrivateByDefault,
                icon = Icons.Default.Lock
            )
        }

        item {
            SafetyGuardrailRow(
                title = "حظر الرسائل الخاصة المباشرة (Zero DMs)",
                description = "منع أي محادثات فردية مباشرة بين المستخدمين لحماية القاصرين من الاستدراج.",
                isActive = uiState.policy.blockDirectMessages,
                icon = Icons.Default.Forum
            )
        }

        item {
            SafetyGuardrailRow(
                title = "منع جمع الموقع الدقيق (Zero Fine Location)",
                description = "الحظر المطلق لجمع إحداثيات GPS للقاصرين؛ الاعتماد على الإدخال اليدوي أو التقريب.",
                isActive = uiState.policy.disableFineLocation,
                icon = Icons.Default.LocationOff
            )
        }

        item {
            SafetyGuardrailRow(
                title = "تعطيل الإعلانات الموجهة والتحليلات الفردية",
                description = "منع تتبع السلوك وبناء ملفات تعريفية للقاصرين (COPPA / GDPR-K).",
                isActive = uiState.policy.disablePersonalizedAds,
                icon = Icons.Default.DoNotDisturb
            )
        }

        item {
            SafetyGuardrailRow(
                title = "حظر تدريب نماذج الذكاء الاصطناعي على بيانات الأطفال",
                description = "عزل تام لمدخلات وتسجيلات القاصرين عن أي خوارزميات أو تدريب خارجي.",
                isActive = uiState.policy.disableModelTrainingOnData,
                icon = Icons.Default.Psychology
            )
        }

        item {
            SafetyGuardrailRow(
                title = "حظر استنساخ أصوات الأطفال (Voice Cloning Ban)",
                description = "المنع الصارم لاستنساخ أو محاكاة أصوات القاصرين بواسطة الذكاء الاصطناعي.",
                isActive = uiState.policy.blockVoiceCloning,
                icon = Icons.Default.MicOff
            )
        }
    }
}

@Composable
private fun ParentalConsentPortalTab(
    uiState: MinorSafetyUiState,
    onRequestConsentClick: () -> Unit,
    onVerifyClick: (String) -> Unit,
    onRevokeClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "سجل وموافقات أولياء الأمور",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "توثيق رسمي وإشراف دائم على حسابات الأطفال واليافعين",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onRequestConsentClick,
                    modifier = Modifier.testTag("btn_request_parental_consent")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("طلب موافقة")
                }
            }
        }

        if (uiState.consents.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "لا توجد طلبات موافقة مسجلة حالياً. اضغط على 'طلب موافقة' لربط حساب ولي الأمر.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(uiState.consents) { consent ->
                ParentalConsentCard(
                    consent = consent,
                    onVerifyClick = onVerifyClick,
                    onRevokeClick = onRevokeClick
                )
            }
        }
    }
}

@Composable
private fun EmergencyReportsTab(
    uiState: MinorSafetyUiState,
    onNewReportClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مسار الطوارئ الفوري لحماية الأطفال",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "يتم التعامل مع بلاغات الاستغلال أو التحرش أو المحتوى غير اللائق بأولوية قصوى وبمهلة استجابة عاجلة لا تتجاوز 15 دقيقة مع إمكانية تصعيد الحالة للجهات الرسمية.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onNewReportClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("btn_trigger_emergency_report")
                    ) {
                        Text("تقديم بلاغ طارئ لحماية قاصر")
                    }
                }
            }
        }

        item {
            Text(
                text = "سجل البلاغات المصعدة:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(uiState.incidentReports) { rep ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_report_${rep.reportId}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = rep.incidentType.titleArabic,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Surface(
                            color = MaterialTheme.statusColors.errorBg,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = rep.urgency.titleArabic,
                                color = MaterialTheme.statusColors.errorFg,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = rep.description, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "الحالة: ${rep.status.titleArabic}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (rep.internalNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = rep.internalNotes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EducationalAuditorTab(
    uiState: MinorSafetyUiState,
    onEvaluateClick: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("قصة أصحاب الكهف للأطفال") }
    var content by remember { mutableStateOf("قصة تفاعلية ميسرة تشرح معنى الإيمان والتوكل على الله بأسلوب تربوي لطيف.") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "فحص ملاءمة وسلامة المحتوى التعليمي للأطفال:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "التحقق من خلو المحتوى من أي محفزات شراء مضللة، أو عناصر مخيفة، ومطابقته للضوابط الشرعية للأطفال.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("عنوان المادة التعليمية") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_edu_title")
            )
        }

        item {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("مقتطف أو نص المحتوى") },
                minLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_edu_content")
            )
        }

        item {
            Button(
                onClick = { onEvaluateClick(title, content) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_evaluate_edu_content")
            ) {
                Icon(Icons.Default.Verified, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إجراء الفحص والتدقيق الأمني")
            }
        }

        uiState.educationalSafetyCheck?.let { check ->
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (check.isChildSafe) MaterialTheme.statusColors.successBg else MaterialTheme.statusColors.errorBg
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (check.isChildSafe) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (check.isChildSafe) MaterialTheme.statusColors.successFg else MaterialTheme.statusColors.errorFg,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (check.isChildSafe) "المحتوى آمن ومناسب للأطفال" else "تنبيه: المحتوى يتضمن محاذير",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (check.isChildSafe) MaterialTheme.statusColors.successFg else MaterialTheme.statusColors.errorFg
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "الفئة المستحسنة: ${check.ageRecommendation}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        check.safetyNotes.forEach { note ->
                            Text(text = "• $note", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DataErasureTab(
    uiState: MinorSafetyUiState,
    onPurgeClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "حق المحو والتطهير الشامل لبيانات القاصر (Right to Erasure)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "يمكن لولي الأمر أو المستخدم طلب مسح وتطهير فوري لكافة التسجيلات الصوتية، ومسودات المشاريع، والملفات، وسجلات النشاط من خوادم سراج بشكل نهائي وغير قابل للاسترجاع.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onPurgeClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("btn_trigger_minor_purge")
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("مسح وتطهير بيانات القاصر فوراً")
                    }
                }
            }
        }

        uiState.lastDeletionSummary?.let { summary ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.statusColors.successBg),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "إشعار إتمام التطهير بنجاح",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.statusColors.successFg
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "تم مسح ${summary.deletedRecordingsCount} تسجيلات صوتية و ${summary.deletedProjectsCount} مشاريع.")
                        Text(text = "رمز الإيصال الأمني: ${summary.confirmationReceiptHash.take(16)}...")
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = summary.legalComplianceStatement,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.statusColors.successFg
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestParentalConsentDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("طلب موافقة ولي الأمر") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "أدخل بريد ولي الأمر لإرسال إشعار ورمز التحقق لتأكيد الإشراف والموافقة على استخدام أدوات سراج.",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم ولي الأمر") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_guardian_name")
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("بريد ولي الأمر الإلكتروني") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_guardian_email")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(email, name) },
                enabled = email.contains("@"),
                modifier = Modifier.testTag("btn_confirm_request_consent")
            ) {
                Text("إرسال الطلب")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
private fun VerifyOtpDialog(
    consentId: String,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var code by remember { mutableStateOf("123456") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تأكيد رمز ولي الأمر (OTP)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "أدخل رمز التحقق المكون من 6 أرقام المرسل إلى بريد ولي الأمر:",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("رمز التحقق (123456)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_otp_code")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(code) },
                enabled = code.isNotBlank(),
                modifier = Modifier.testTag("btn_submit_verify_otp")
            ) {
                Text("تأكيد وتفعيل")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
private fun EmergencyReportDialog(
    onDismiss: () -> Unit,
    onSubmit: (ChildSafetyIncidentType, String, String?, String?) -> Unit
) {
    var selectedType by remember { mutableStateOf(ChildSafetyIncidentType.EXPLOITATION_OR_ABUSE) }
    var description by remember { mutableStateOf("") }
    var reportedUser by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ReportProblem, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إبلاغ طارئ لحماية قاصر", color = MaterialTheme.colorScheme.error)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "اختر نوع المخالفة واكتب وصفاً دقيقاً. سيتم تحويل البلاغ فوراً لمسار التدخل العاجل.",
                    style = MaterialTheme.typography.bodySmall
                )
                ChildSafetyIncidentType.values().forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedType = type }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            modifier = Modifier.testTag("radio_incident_${type.code}")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = type.titleArabic, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("تفاصيل البلاغ") },
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_report_desc")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(selectedType, description, reportedUser.ifBlank { null }, null) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                enabled = description.isNotBlank(),
                modifier = Modifier.testTag("btn_submit_emergency_report")
            ) {
                Text("إرسال البلاغ فوراً")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
private fun PurgeConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تأكيد مسح وتطهير بيانات القاصر") },
        text = {
            Text("هل أنت متأكد من رغبتك في حذف وتطهير كافة البيانات والتسجيلات والمشاريع نهائياً؟ هذا الإجراء لا يمكن التراجع عنه.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("btn_confirm_purge_modal")
            ) {
                Text("نعم، مسح نهائي")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
