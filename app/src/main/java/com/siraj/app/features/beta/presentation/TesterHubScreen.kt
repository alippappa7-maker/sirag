package com.siraj.app.features.beta.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.core.ui.components.BetaFeedbackDialog
import com.siraj.app.domain.models.beta.CriticalJourney
import com.siraj.app.domain.models.beta.DistributionChannelInfo
import com.siraj.app.domain.models.beta.TesterStatus
import com.siraj.app.ui.theme.SirajGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TesterHubScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRoute: (String) -> Unit,
    viewModel: TesterHubViewModel = viewModel(factory = TesterHubViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()
    var showBugReportDialog by remember { mutableStateOf(false) }
    var showRevokeConfirmDialog by remember { mutableStateOf(false) }

    val tabTitles = listOf("بوابة المختبر", "تعليمات التثبيت", "تقييم التجربة", "ملاحظات الإصدار")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "مركز وخدمات المختبرين",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Siraj Beta Tester Hub • v${uiState.appVersion}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("tester_hub_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onNavigateToRoute("defect_triage") },
                        modifier = Modifier.testTag("open_defect_triage_action_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Rule,
                            contentDescription = "فرز العيوب والملاحظات",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { showBugReportDialog = true },
                        modifier = Modifier.testTag("open_bug_report_action_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = "إرسال تقرير خطأ",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Selector
            PrimaryScrollableTabRow(
                selectedTabIndex = uiState.selectedTab,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (uiState.selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Main Tab Content
            when (uiState.selectedTab) {
                0 -> TesterDashboardTab(
                    uiState = uiState,
                    onNavigateToRoute = onNavigateToRoute,
                    onMarkJourney = { journeyId -> viewModel.markJourneyCompleted(journeyId) },
                    onOpenFeedback = { showBugReportDialog = true },
                    onOpenRevokeDialog = { showRevokeConfirmDialog = true }
                )
                1 -> InstallationGuidesTab(
                    channels = uiState.distributionChannels
                )
                2 -> ExperienceSurveyTab(
                    uiState = uiState,
                    onUpdateOverallRating = viewModel::updateOverallRating,
                    onUpdateEaseRating = viewModel::updateEaseOfUseRating,
                    onUpdateShariaRating = viewModel::updateShariaContentRating,
                    onUpdatePerfRating = viewModel::updatePerformanceRating,
                    onUpdateValuableFeature = viewModel::updateMostValuableFeature,
                    onUpdatePainPoint = viewModel::updateBiggestPainPoint,
                    onUpdateSuggestions = viewModel::updateGeneralSuggestions,
                    onSubmitSurvey = viewModel::submitSurvey,
                    onClearStatus = viewModel::clearSurveyStatus
                )
                3 -> ReleaseNotesTab(
                    releaseNotes = uiState.releaseNotes
                )
            }
        }
    }

    // Bug Report Dialog
    if (showBugReportDialog) {
        BetaFeedbackDialog(
            currentRoute = "tester_hub",
            onDismissRequest = { showBugReportDialog = false }
        )
    }

    // Revocation Confirmation Dialog
    if (showRevokeConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRevokeConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("سحب صلاحية حساب الاختبار") },
            text = {
                Text(
                    "هل أنت متأكد من رغبتك في مغادرة برنامج الاختبار التجريبي لسراج؟ سيتم سحب صلاحية حساب المختبر وإيقاف استلام التحديثات التجريبية."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.revokeMyTesterAccess()
                        showRevokeConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_revoke_btn")
                ) {
                    Text("تأكيد الانسحاب")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRevokeConfirmDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun TesterDashboardTab(
    uiState: TesterHubUiState,
    onNavigateToRoute: (String) -> Unit,
    onMarkJourney: (String) -> Unit,
    onOpenFeedback: () -> Unit,
    onOpenRevokeDialog: () -> Unit
) {
    val profile = uiState.testerProfile
    val completedCount = profile?.completedJourneys?.size ?: 0
    val totalJourneys = uiState.criticalJourneys.size
    val progress = if (totalJourneys > 0) completedCount.toFloat() / totalJourneys.toFloat() else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tester Profile Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = profile?.name?.ifBlank { "مختبر معتمد" } ?: "مختبر معتمد",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = profile?.email ?: "tester@siraj.app",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        // Status Badge
                        val status = profile?.status ?: TesterStatus.ACTIVE
                        Surface(
                            color = Color(status.colorHex).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = status.title,
                                color = Color(status.colorHex),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Group & Metadata
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "مجموعة الاختبار:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = profile?.group?.title ?: "مختبرو المجتمع",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "الجهاز والنظام:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = uiState.deviceModel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        // Critical Journeys Progress Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "المسارات الأساسية للاختبار",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "$completedCount من $totalJourneys مكتمل",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "يُرجى تجربة جميع المسارات والتأكد من سلامة الأداء والمحتوى الشرعي.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // List of Critical Journeys
        items(uiState.criticalJourneys) { journey ->
            val isCompleted = profile?.completedJourneys?.contains(journey.id) == true
            CriticalJourneyCard(
                journey = journey,
                isCompleted = isCompleted,
                onStartJourney = { onNavigateToRoute(journey.targetRoute) },
                onToggleComplete = { onMarkJourney(journey.id) }
            )
        }

        // Action Buttons: Triage, Bug Report & Leave Program
        item {
            Spacer(modifier = Modifier.height(8.dp))
            FilledTonalButton(
                onClick = { onNavigateToRoute("defect_triage") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("dashboard_defect_triage_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Rule, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("لوحة تحليل وإدارة العيوب وفرز الملاحظات", fontWeight = FontWeight.Bold)
            }
        }

        item {
            Button(
                onClick = onOpenFeedback,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("dashboard_report_bug_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.BugReport, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إرسال تقرير خطأ تشخيصي أو ملاحظة شرعية", fontWeight = FontWeight.Bold)
            }
        }

        item {
            OutlinedButton(
                onClick = onOpenRevokeDialog,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_revoke_access_btn"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PersonRemove, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("مغادرة برنامج الاختبار وسحب صلاحية الحساب")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CriticalJourneyCard(
    journey: CriticalJourney,
    isCompleted: Boolean,
    onStartJourney: () -> Unit,
    onToggleComplete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkmark or Icon
            Surface(
                color = if (isCompleted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .size(44.dp)
                    .clickable { onToggleComplete() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = if (isCompleted) "مكتمل" else "غير مكتمل",
                        tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = journey.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = journey.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            FilledTonalButton(
                onClick = onStartJourney,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("بدء", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun InstallationGuidesTab(
    channels: List<DistributionChannelInfo>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "دليل تثبيت وتحديث الإصدار التجريبي",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "إرشادات التثبيت الآمن عبر منصات التوزيع المعتمدة (Android & iOS)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(channels) { channel ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = channel.platform,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "${channel.channelName} (${channel.methodTitle})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "خطوات التثبيت:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    channel.stepGuide.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "${index + 1}.",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.width(22.dp)
                            )
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = channel.updateInstructions,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExperienceSurveyTab(
    uiState: TesterHubUiState,
    onUpdateOverallRating: (Int) -> Unit,
    onUpdateEaseRating: (Int) -> Unit,
    onUpdateShariaRating: (Int) -> Unit,
    onUpdatePerfRating: (Int) -> Unit,
    onUpdateValuableFeature: (String) -> Unit,
    onUpdatePainPoint: (String) -> Unit,
    onUpdateSuggestions: (String) -> Unit,
    onSubmitSurvey: () -> Unit,
    onClearStatus: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "استبانة تقييم تجربة الإصدار التجريبي",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "تقييمك الدقيق وملاحظاتك تساهم بشكل مباشر في ضبط التجربة والتحقق قبل الإطلاق الرسمي.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Success or Error Alerts
        item {
            AnimatedVisibility(visible = uiState.surveySuccess) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = uiState.surveySuccessMessage ?: "تم إرسال التقييم بنجاح!",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            AnimatedVisibility(visible = uiState.surveyErrorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = uiState.surveyErrorMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        // Rating Questions Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    RatingQuestionRow(
                        title = "1. التقييم العام لتطبيق سراج",
                        rating = uiState.overallRating,
                        onRatingChanged = onUpdateOverallRating
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    RatingQuestionRow(
                        title = "2. سهولة الاستخدام وتجربة الواجهات (UX/UI)",
                        rating = uiState.easeOfUseRating,
                        onRatingChanged = onUpdateEaseRating
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    RatingQuestionRow(
                        title = "3. دقة وتوثيق المحتوى الشرعي والمصادر",
                        rating = uiState.shariaContentRating,
                        onRatingChanged = onUpdateShariaRating
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    RatingQuestionRow(
                        title = "4. استقرار الأداء وسرعة الاستجابة",
                        rating = uiState.performanceRating,
                        onRatingChanged = onUpdatePerfRating
                    )
                }
            }
        }

        // Qualitative Text Questions
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = uiState.mostValuableFeature,
                        onValueChange = onUpdateValuableFeature,
                        label = { Text("أكثر ميزة نالت إعجابك وفائدتها") },
                        placeholder = { Text("مثال: سرعة ربط الآيات بالمصادر وتلاوة القراء في المحراب...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("survey_valuable_feature_input"),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = uiState.biggestPainPoint,
                        onValueChange = onUpdatePainPoint,
                        label = { Text("أكبر صعوبة أو عائق واجهك أثناء الاستخدام") },
                        placeholder = { Text("مثال: وضوح زر الانتقال في الاستوديو أو استجابة البحث...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("survey_pain_point_input"),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = uiState.generalSuggestions,
                        onValueChange = onUpdateSuggestions,
                        label = { Text("مقترحات عامة ترغب بإضافتها مستقبلاً") },
                        placeholder = { Text("شاركنا أي فكرة أو تطوير تراه مناسباً...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("survey_suggestions_input"),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // Diagnostic Footer
        item {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "يتم إرفاق تشخيص الجهاز (${uiState.deviceModel} - ${uiState.osVersion} - v${uiState.appVersion}) تلقائياً للمساعدة في تحليل النتائج دون جمع أي بيانات شخصية حساسة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Submit Button
        item {
            Button(
                onClick = onSubmitSurvey,
                enabled = !uiState.isSubmittingSurvey,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_survey_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isSubmittingSurvey) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إرسال التقييم النهائي", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RatingQuestionRow(
    title: String,
    rating: Int,
    onRatingChanged: (Int) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            (1..5).forEach { star ->
                IconButton(
                    onClick = { onRatingChanged(star) }
                ) {
                    Icon(
                        imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "$star نجوم",
                        tint = if (star <= rating) com.siraj.app.ui.theme.SirajGold else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Text(
                text = "$rating / 5",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ReleaseNotesTab(
    releaseNotes: List<com.siraj.app.domain.models.beta.BetaReleaseNote>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "سجل ملاحظات إصدارات النسخة التجريبية (Release Notes)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(releaseNotes) { note ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الإصدار ${note.versionName} (Build ${note.buildCode})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = note.releaseDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "القناة: ${note.channel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Highlights
                    Text(
                        text = "أبرز الميزات والوظائف:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    note.highlights.forEach { item ->
                        BulletItem(text = item, icon = Icons.Default.Check, iconTint = Color(0xFF4CAF50))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Fixed Issues
                    Text(
                        text = "التحسينات والإصلاحات:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    note.fixedIssues.forEach { item ->
                        BulletItem(text = item, icon = Icons.Default.Build, iconTint = Color(0xFF2196F3))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Known Limitations
                    Text(
                        text = "القيود المعروفة في هذه النسخة:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    note.knownLimitations.forEach { item ->
                        BulletItem(text = item, icon = Icons.Default.Info, iconTint = Color(0xFFFF9800))
                    }
                }
            }
        }
    }
}

@Composable
private fun BulletItem(text: String, icon: ImageVector, iconTint: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
