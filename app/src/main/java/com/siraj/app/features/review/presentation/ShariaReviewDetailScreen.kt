package com.siraj.app.features.review.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siraj.app.domain.models.review.*
import com.siraj.app.features.review.presentation.components.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShariaReviewDetailScreen(
    itemId: String,
    viewModel: ShariaReviewViewModel,
    currentUserRole: String,
    currentUserId: String,
    currentUserName: String,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("سياق المحتوى", "المطالبات والمصادر", "تنوع المصادر", "سجل التعديلات", "الملاحظات والتدقيق")

    // Dialog state controllers
    var showApproveDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var showRequestChangesDialog by remember { mutableStateOf(false) }
    var showEscalateDialog by remember { mutableStateOf(false) }
    var showDualApprovalDialog by remember { mutableStateOf(false) }
    var showInternalNoteDialog by remember { mutableStateOf(false) }
    var showScheduleReReviewDialog by remember { mutableStateOf(false) }
    var selectedClaimForComment by remember { mutableStateOf<ShariaClaim?>(null) }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("ar"))

    LaunchedEffect(itemId) {
        viewModel.loadItemDetails(itemId)
    }

    LaunchedEffect(state.errorMessage, state.successMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    val item = state.selectedItem

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "تفاصيل التدقيق الشرعي",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_back_detail")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { showInternalNoteDialog = true }) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "ملاحظة داخلية",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showScheduleReReviewDialog = true }) {
                        Icon(
                            Icons.Default.EventRepeat,
                            contentDescription = "جدولة إعادة المراجعة",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (item != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Check if creator is the current reviewer -> Disallow self-review actions
                        val isCreatorSelf = item.creatorId == currentUserId

                        if (isCreatorSelf) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Block,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "أنت صانع هذا المحتوى. لا يحق لك مراجعة أو اعتماد محتواك الخاص درءاً لتعارض المصالح.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        } else {
                            when (item.status) {
                                ShariaReviewStatus.PENDING -> {
                                    Button(
                                        onClick = {
                                            viewModel.claimReview(item.id, currentUserId, currentUserName)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("btn_claim_review"),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.AssignmentInd, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("حجز وبدء التدقيق الشرعي")
                                    }
                                }

                                ShariaReviewStatus.IN_REVIEW, ShariaReviewStatus.ESCALATED_SECOND_REVIEW -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { showApproveDialog = true },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("btn_action_approve"),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("اعتماد")
                                        }

                                        Button(
                                            onClick = { showRequestChangesDialog = true },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("btn_action_request_changes"),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                                        ) {
                                            Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("طلب تعديل")
                                        }

                                        OutlinedButton(
                                            onClick = { showEscalateDialog = true },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("btn_action_escalate"),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("تحويل")
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = { showRejectDialog = true },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("btn_action_reject"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("رفض المحتوى شرعياً")
                                    }
                                }

                                ShariaReviewStatus.DUAL_APPROVAL_PENDING -> {
                                    val isPrimaryReviewer = item.decision?.primaryReviewerId == currentUserId

                                    if (isPrimaryReviewer) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "لقد قمت بالاعتماد الأولي. بانتظار اعتماد المراجع الثاني المؤهل لإتمام التوقيع المشترك.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.padding(10.dp)
                                            )
                                        }
                                    } else {
                                        Button(
                                            onClick = { showDualApprovalDialog = true },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("btn_action_dual_approve"),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Icon(Icons.Default.VerifiedUser, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("اتخاذ قرار الاعتماد المشترك (المراجع الثاني)")
                                        }
                                    }
                                }

                                else -> {
                                    // Approved or Rejected or Changes Requested
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        StatusBadge(status = item.status)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (state.isLoading && item == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (item == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("لم يتم العثور على عنصر المراجعة")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Header info card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusBadge(status = item.status)
                            RiskBadge(riskLevel = item.riskLevel)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = item.contentTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "المنشئ: ${item.creatorName} | القسم: ${item.category}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = dateFormat.format(Date(item.submittedAt)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        if (item.criticalTopics.any { it != CriticalTopic.NONE }) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                item.criticalTopics.filter { it != CriticalTopic.NONE }.forEach { topic ->
                                    CriticalTopicChip(topic = topic)
                                }
                            }
                        }
                    }
                }

                // Tabs Navigation
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                // Tab Content Body
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when (selectedTab) {
                        0 -> {
                            // Tab 0: Full Text in Context
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                item {
                                    Card(
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = CardDefaults.outlinedCardBorder()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text(
                                                text = "النص الكامل للمحتوى المعروض في الفيديو/المقطع:",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = item.fullContentText,
                                                style = MaterialTheme.typography.bodyMedium,
                                                lineHeight = 24.sp
                                            )
                                        }
                                    }
                                }

                                item {
                                    Text(
                                        text = "المواضع المرتبطة بمصادر شرعية (${item.claims.size} مواضع):",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                items(item.claims) { claim ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = claim.positionContext,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "« ${claim.claimText} »",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "المرجع: ${claim.sourceTitle} (${claim.hadithGrade ?: "موثق"})",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            // Tab 1: Claims & Detailed Sources
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(item.claims, key = { it.id }) { claim ->
                                    ClaimReviewCard(
                                        claim = claim,
                                        onAddCommentClick = { selectedClaimForComment = it }
                                    )
                                }
                            }
                        }

                        2 -> {
                            // Tab 2: Source Variations & Discrepancies
                            val allVariations = item.claims.flatMap { it.sourceVariations }
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp)
                            ) {
                                item {
                                    SourceVariationViewer(variations = allVariations)
                                }
                            }
                        }

                        3 -> {
                            // Tab 3: Revision History
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp)
                            ) {
                                item {
                                    RevisionHistoryViewer(revisions = item.revisions)
                                }
                            }
                        }

                        4 -> {
                            // Tab 4: Internal Notes & Immutable Audit Logs
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "الملاحظات الداخلية للمراجعين (${item.internalNotes.size}):",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        TextButton(onClick = { showInternalNoteDialog = true }) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("إضافة ملاحظة")
                                        }
                                    }
                                }

                                if (item.internalNotes.isEmpty()) {
                                    item {
                                        Text(
                                            text = "لا توجد ملاحظات داخلية سرية حتى الآن.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                } else {
                                    items(item.internalNotes) { note ->
                                        Surface(
                                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = note.authorName,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = dateFormat.format(Date(note.createdAt)),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.outline
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = note.noteText,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }
                                }

                                item {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "سجل التدقيق غير القابل للتعديل (${item.auditLogs.size} إجراءات مسجلة):",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                items(item.auditLogs.sortedByDescending { it.timestamp }) { log ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(8.dp),
                                        border = CardDefaults.outlinedCardBorder(),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .padding(top = 4.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "${log.reviewerName} • ${log.action}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = dateFormat.format(Date(log.timestamp)),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.outline
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = log.details,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showApproveDialog && item != null) {
        ApproveDialog(
            item = item,
            onConfirm = { reason, reReviewDate ->
                viewModel.approveItem(item.id, currentUserId, currentUserName, reason, reReviewDate)
                showApproveDialog = false
            },
            onDismiss = { showApproveDialog = false }
        )
    }

    if (showRejectDialog && item != null) {
        RejectDialog(
            onConfirm = { reason ->
                viewModel.rejectItem(item.id, currentUserId, currentUserName, reason)
                showRejectDialog = false
            },
            onDismiss = { showRejectDialog = false }
        )
    }

    if (showRequestChangesDialog && item != null) {
        RequestChangesDialog(
            onConfirm = { changes ->
                viewModel.requestChanges(item.id, currentUserId, currentUserName, changes)
                showRequestChangesDialog = false
            },
            onDismiss = { showRequestChangesDialog = false }
        )
    }

    if (showEscalateDialog && item != null) {
        EscalateDialog(
            onConfirm = { targetId, targetName, reason ->
                viewModel.escalateToSecondReviewer(item.id, currentUserId, currentUserName, targetId, targetName, reason)
                showEscalateDialog = false
            },
            onDismiss = { showEscalateDialog = false }
        )
    }

    if (showDualApprovalDialog && item != null) {
        DualApprovalDialog(
            item = item,
            onConfirm = { approve, reason ->
                viewModel.submitSecondReviewDecision(item.id, currentUserId, currentUserName, approve, reason)
                showDualApprovalDialog = false
            },
            onDismiss = { showDualApprovalDialog = false }
        )
    }

    if (showInternalNoteDialog && item != null) {
        AddInternalNoteDialog(
            onConfirm = { note ->
                viewModel.addInternalNote(item.id, currentUserId, currentUserName, note)
                showInternalNoteDialog = false
            },
            onDismiss = { showInternalNoteDialog = false }
        )
    }

    if (selectedClaimForComment != null && item != null) {
        AddClaimCommentDialog(
            claim = selectedClaimForComment!!,
            onConfirm = { comment ->
                viewModel.addClaimComment(item.id, selectedClaimForComment!!.id, currentUserId, currentUserName, comment)
                selectedClaimForComment = null
            },
            onDismiss = { selectedClaimForComment = null }
        )
    }

    if (showScheduleReReviewDialog && item != null) {
        ScheduleReReviewDialog(
            onConfirm = { timestamp ->
                viewModel.scheduleReReview(item.id, currentUserId, currentUserName, timestamp)
                showScheduleReReviewDialog = false
            },
            onDismiss = { showScheduleReReviewDialog = false }
        )
    }
}
