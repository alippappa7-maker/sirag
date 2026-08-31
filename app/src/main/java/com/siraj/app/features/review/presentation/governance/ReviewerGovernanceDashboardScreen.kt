package com.siraj.app.features.review.presentation.governance

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siraj.app.domain.models.governance.*
import com.siraj.app.domain.models.review.CriticalTopic
import com.siraj.app.domain.models.review.RiskLevel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReviewerGovernanceDashboardScreen(
    viewModel: ReviewerGovernanceViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var showVerifyDialog by remember { mutableStateOf<ReviewerProfile?>(null) }
    var showSuspendDialog by remember { mutableStateOf<ReviewerProfile?>(null) }
    var showAddQualDialog by remember { mutableStateOf<ReviewerProfile?>(null) }
    var showEditScopeDialog by remember { mutableStateOf<ReviewerProfile?>(null) }
    var showConflictDialog by remember { mutableStateOf(false) }
    var showAddReviewerDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        modifier = modifier.testTag("reviewer_governance_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "حوكمة المراجعين الشرعيين",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "لوحة المالك (Owner) لإدارة التوثيق والاختصاصات والتعيين",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showConflictDialog = true },
                        modifier = Modifier.testTag("conflicts_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = "تعارض المصالح",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddReviewerDialog = true },
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("تسجيل مراجع جديد") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_reviewer_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // شريط إحصائيات الحوكمة
            GovernanceStatsRow(
                reviewers = uiState.reviewers,
                assignments = uiState.assignments
            )

            // شريط البحث
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_reviewers_input"),
                placeholder = { Text("البحث بالاسم أو المؤسسة أو البريد...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // تبويبات التصفية
            TabRow(
                selectedTabIndex = uiState.selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                ReviewerTabFilter.values().forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = {
                            Text(
                                text = tab.arabicTitle,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    )
                }
            }

            // رقائق المجالات الشرعية
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedDomainFilter == null,
                        onClick = { viewModel.selectDomainFilter(null) },
                        label = { Text("كافة المجالات") }
                    )
                }
                items(ReviewerDomain.values()) { domain ->
                    FilterChip(
                        selected = uiState.selectedDomainFilter == domain,
                        onClick = {
                            if (uiState.selectedDomainFilter == domain) viewModel.selectDomainFilter(null)
                            else viewModel.selectDomainFilter(domain)
                        },
                        label = { Text(domain.arabicTitle) }
                    )
                }
            }

            // قائمة المراجعين
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredReviewers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PersonSearch,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "لا يوجد مراجعون يطابقون معايير التصفية الحالية",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredReviewers, key = { it.id }) { reviewer ->
                        ReviewerProfileCard(
                            reviewer = reviewer,
                            onVerifyClick = { showVerifyDialog = reviewer },
                            onSuspendClick = { showSuspendDialog = reviewer },
                            onReactivateClick = { viewModel.reactivateReviewer(reviewer.id) },
                            onAddQualificationClick = { showAddQualDialog = reviewer },
                            onEditScopeClick = { showEditScopeDialog = reviewer },
                            modifier = Modifier.testTag("reviewer_card_${reviewer.id}")
                        )
                    }
                }
            }
        }
    }

    // الحوارات المنبثقة
    showVerifyDialog?.let { reviewer ->
        VerifyReviewerDialog(
            reviewer = reviewer,
            onDismiss = { showVerifyDialog = null },
            onConfirm = { days ->
                viewModel.verifyReviewer(reviewer.id, days)
                showVerifyDialog = null
            }
        )
    }

    showSuspendDialog?.let { reviewer ->
        SuspendReviewerDialog(
            reviewer = reviewer,
            onDismiss = { showSuspendDialog = null },
            onConfirm = { reason ->
                viewModel.suspendReviewer(reviewer.id, reason)
                showSuspendDialog = null
            }
        )
    }

    showAddQualDialog?.let { reviewer ->
        AddQualificationDialog(
            reviewer = reviewer,
            onDismiss = { showAddQualDialog = null },
            onConfirm = { title, inst, year, isPublic ->
                viewModel.addQualification(reviewer.id, title, inst, year, isPublic)
                showAddQualDialog = null
            }
        )
    }

    showEditScopeDialog?.let { reviewer ->
        EditScopeDialog(
            reviewer = reviewer,
            onDismiss = { showEditScopeDialog = null },
            onConfirm = { domains, excluded, maxRisk, canPrimary, canSecond, quota ->
                viewModel.updateReviewerScope(reviewer.id, domains, excluded, maxRisk, canPrimary, canSecond, quota)
                showEditScopeDialog = null
            }
        )
    }

    if (showConflictDialog) {
        ConflictsManagementDialog(
            conflicts = uiState.conflicts,
            reviewers = uiState.reviewers,
            onDismiss = { showConflictDialog = false },
            onAddConflict = { rId, cId, pId, type, reason ->
                viewModel.recordConflict(rId, cId, pId, type, reason)
            }
        )
    }

    if (showAddReviewerDialog) {
        AddReviewerDialog(
            onDismiss = { showAddReviewerDialog = false },
            onConfirm = { name, email, org, domains ->
                viewModel.createNewReviewer(name, email, org, domains)
                showAddReviewerDialog = false
            }
        )
    }
}

@Composable
private fun GovernanceStatsRow(
    reviewers: List<ReviewerProfile>,
    assignments: List<ReviewerAssignment>
) {
    val activeCount = reviewers.count { it.status == ReviewerStatus.ACTIVE }
    val pendingCount = reviewers.count { it.status == ReviewerStatus.PENDING_VERIFICATION }
    val suspendedCount = reviewers.count { it.status == ReviewerStatus.SUSPENDED }
    val totalReviews = reviewers.sumOf { it.totalReviewsCompleted }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem(label = "المعتمدون", value = "$activeCount", color = MaterialTheme.colorScheme.primary)
            StatItem(label = "قيد التحقق", value = "$pendingCount", color = Color(0xFFD97706))
            StatItem(label = "الموقوفون", value = "$suspendedCount", color = MaterialTheme.colorScheme.error)
            StatItem(label = "المراجعات المنجزة", value = "$totalReviews", color = MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReviewerProfileCard(
    reviewer: ReviewerProfile,
    onVerifyClick: () -> Unit,
    onSuspendClick: () -> Unit,
    onReactivateClick: () -> Unit,
    onAddQualificationClick: () -> Unit,
    onEditScopeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ترويسة المراجع
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Column {
                        Text(
                            text = reviewer.displayName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = reviewer.organization,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // شارة الحالة
                StatusBadge(status = reviewer.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // مجالات الاختصاص
            Text(
                text = "مجالات الاختصاص الشرعي:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                reviewer.scope.allowedDomains.forEach { domain ->
                    AssistChip(
                        onClick = {},
                        label = { Text(domain.arabicTitle, fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                        )
                    )
                }
            }

            // الموضوعات المستثناة إن وجدت
            if (reviewer.scope.excludedTopics.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "مستثنى من: " + reviewer.scope.excludedTopics.joinToString { it.arabicTitle },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // المؤهلات الأكاديمية والشرعية
            if (reviewer.qualifications.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "المؤهلات الشرعية الموثقة:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                reviewer.qualifications.forEach { qual ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (qual.isVerified) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = if (qual.isVerified) MaterialTheme.colorScheme.primary else Color(0xFFD97706),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${qual.degreeTitle} - ${qual.institution} (${qual.graduationYear})",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // بيانات الاعتماد وتاريخ إعادة التحقق
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (reviewer.verificationDate != null) {
                    Text(
                        text = "تم التوثيق: ${dateFormat.format(Date(reviewer.verificationDate))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (reviewer.nextReverificationDue != null) {
                    Text(
                        text = "تاريخ التجديد: ${dateFormat.format(Date(reviewer.nextReverificationDue))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // أزرار الإجراءات
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onAddQualificationClick,
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("مؤهل جديد", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onEditScopeClick,
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("النطاق", fontSize = 12.sp)
                }

                when (reviewer.status) {
                    ReviewerStatus.PENDING_VERIFICATION -> {
                        Button(
                            onClick = onVerifyClick,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("اعتماد وتوثيق", fontSize = 12.sp)
                        }
                    }
                    ReviewerStatus.ACTIVE -> {
                        Button(
                            onClick = onSuspendClick,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.PauseCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إيقاف مؤقت", fontSize = 12.sp)
                        }
                    }
                    ReviewerStatus.SUSPENDED -> {
                        Button(
                            onClick = onReactivateClick,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إعادة تفعيل", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: ReviewerStatus) {
    val (bgColor, textColor, label) = when (status) {
        ReviewerStatus.ACTIVE -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "معتمد نشط"
        )
        ReviewerStatus.PENDING_VERIFICATION -> Triple(
            Color(0xFFFEF3C7),
            Color(0xFF92400E),
            "قيد التحقق"
        )
        ReviewerStatus.SUSPENDED -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "موقوف مؤقتاً"
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun VerifyReviewerDialog(
    reviewer: ReviewerProfile,
    onDismiss: () -> Unit,
    onConfirm: (days: Long) -> Unit
) {
    var selectedDays by remember { mutableStateOf(365L) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("اعتماد وتوثيق المراجع الشرعي", textAlign = TextAlign.Center) },
        text = {
            Column {
                Text("هل ترغب في اعتماد ${reviewer.displayName} كمراجع شرعي رسمي في المنظومة؟")
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "فترة صلاحية الاعتماد قبل إعادة التحقق الدوري:",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedDays == 180L,
                        onClick = { selectedDays = 180L },
                        label = { Text("6 أشهر") }
                    )
                    FilterChip(
                        selected = selectedDays == 365L,
                        onClick = { selectedDays = 365L },
                        label = { Text("سنة كاملة") }
                    )
                    FilterChip(
                        selected = selectedDays == 730L,
                        onClick = { selectedDays = 730L },
                        label = { Text("سنتان") }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedDays) }) {
                Text("تأكيد الاعتماد")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
private fun SuspendReviewerDialog(
    reviewer: ReviewerProfile,
    onDismiss: () -> Unit,
    onConfirm: (reason: String) -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.PauseCircle, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("إيقاف حساب المراجع مؤقتاً") },
        text = {
            Column {
                Text("سيتم إيقاف المراجع ${reviewer.displayName} عن استلام مراجعات جديدة مع الحفاظ الكامل على كافة قراراته السابقة في السجل الثابت.")
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("سبب الإيقاف المؤقت") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(reason) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("تأكيد الإيقاف")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
private fun AddQualificationDialog(
    reviewer: ReviewerProfile,
    onDismiss: () -> Unit,
    onConfirm: (degree: String, inst: String, year: Int, isPublic: Boolean) -> Unit
) {
    var degree by remember { mutableStateOf("") }
    var inst by remember { mutableStateOf("") }
    var yearStr by remember { mutableStateOf("2020") }
    var isPublic by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة مؤهل شرعي موثق") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = degree,
                    onValueChange = { degree = it },
                    label = { Text("اسم المؤهل / الإجازة") },
                    placeholder = { Text("مثال: دكتوراه في الفقه وأصوله") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = inst,
                    onValueChange = { inst = it },
                    label = { Text("الجهة المانحة / الجامعة") },
                    placeholder = { Text("مثال: الجامعة الإسلامية") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = yearStr,
                    onValueChange = { yearStr = it },
                    label = { Text("سنة التخرج") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isPublic, onCheckedChange = { isPublic = it })
                    Text("إظهار المؤهل للعامة في بطاقة التعريف", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val year = yearStr.toIntOrNull() ?: 2020
                    if (degree.isNotBlank() && inst.isNotBlank()) {
                        onConfirm(degree, inst, year, isPublic)
                    }
                }
            ) {
                Text("إضافة وتوثيق")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditScopeDialog(
    reviewer: ReviewerProfile,
    onDismiss: () -> Unit,
    onConfirm: (
        domains: Set<ReviewerDomain>,
        excluded: Set<CriticalTopic>,
        maxRisk: RiskLevel,
        canPrimary: Boolean,
        canSecond: Boolean,
        quota: Int
    ) -> Unit
) {
    var selectedDomains by remember { mutableStateOf(reviewer.scope.allowedDomains) }
    var selectedExcluded by remember { mutableStateOf(reviewer.scope.excludedTopics) }
    var maxRisk by remember { mutableStateOf(reviewer.scope.maxRiskLevelAllowed) }
    var canPrimary by remember { mutableStateOf(reviewer.scope.canBePrimaryReviewer) }
    var canSecond by remember { mutableStateOf(reviewer.scope.canBeSecondReviewer) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل نطاق الاختصاص والموضوعات") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text("المجالات الشرعية المسموحة:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ReviewerDomain.values().forEach { d ->
                            FilterChip(
                                selected = selectedDomains.contains(d),
                                onClick = {
                                    selectedDomains = if (selectedDomains.contains(d)) selectedDomains - d else selectedDomains + d
                                },
                                label = { Text(d.arabicTitle, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("سقف مستوى الخطورة المسموح:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        RiskLevel.values().forEach { r ->
                            FilterChip(
                                selected = maxRisk == r,
                                onClick = { maxRisk = r },
                                label = { Text(r.arabicTitle, fontSize = 10.sp) }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = canPrimary, onCheckedChange = { canPrimary = it })
                        Text("مفوض كمراجع أساسي", style = MaterialTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = canSecond, onCheckedChange = { canSecond = it })
                        Text("مفوض كمراجع ثانٍ للموضوعات الحرجة", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(selectedDomains, selectedExcluded, maxRisk, canPrimary, canSecond, reviewer.scope.dailyReviewQuota)
            }) {
                Text("حفظ النطاق")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
private fun ConflictsManagementDialog(
    conflicts: List<ReviewerConflict>,
    reviewers: List<ReviewerProfile>,
    onDismiss: () -> Unit,
    onAddConflict: (reviewerId: String, creatorId: String, projectId: String?, type: ConflictType, reason: String) -> Unit
) {
    var reviewerId by remember { mutableStateOf(reviewers.firstOrNull()?.id ?: "") }
    var creatorId by remember { mutableStateOf("") }
    var conflictType by remember { mutableStateOf(ConflictType.PERSONAL_AFFILIATION) }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Gavel, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("إدارة قيود تعارض المصالح") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text("القيود المسجلة حالياً:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    if (conflicts.isEmpty()) {
                        Text("لا توجد قيود تعارض مصالح مسجلة.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        conflicts.forEach { c ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(c.conflictType.arabicTitle, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.error)
                                    Text(c.reason, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("تسجيل قيد تعارض جديد:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    OutlinedTextField(
                        value = creatorId,
                        onValueChange = { creatorId = it },
                        label = { Text("معرف صانع المحتوى (Creator ID)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("سبب تعارض المصالح") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (creatorId.isNotBlank() && reason.isNotBlank()) {
                        onAddConflict(reviewerId, creatorId, null, conflictType, reason)
                        creatorId = ""
                        reason = ""
                    }
                }
            ) {
                Text("تسجيل القيد")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddReviewerDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, email: String, org: String, domains: Set<ReviewerDomain>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var org by remember { mutableStateOf("") }
    var selectedDomains by remember { mutableStateOf(setOf(ReviewerDomain.GENERAL)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تسجيل مراجع شرعي جديد") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("الاسم الكامل للمراجع") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("البريد الإلكتروني المهني") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = org,
                    onValueChange = { org = it },
                    label = { Text("المؤسسة أو الهيئة العلمية") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("المجال المبدئي:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ReviewerDomain.values().forEach { d ->
                        FilterChip(
                            selected = selectedDomains.contains(d),
                            onClick = {
                                selectedDomains = if (selectedDomains.contains(d)) selectedDomains - d else selectedDomains + d
                            },
                            label = { Text(d.arabicTitle, fontSize = 10.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank()) {
                        onConfirm(name, email, org, selectedDomains)
                    }
                }
            ) {
                Text("تسجيل وإرسال للتحقق")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
