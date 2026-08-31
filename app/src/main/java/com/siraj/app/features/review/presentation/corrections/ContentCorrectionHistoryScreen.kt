package com.siraj.app.features.review.presentation.corrections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.siraj.app.domain.models.Source
import com.siraj.app.domain.models.SourceType
import com.siraj.app.domain.models.SourceVerificationStatus
import com.siraj.app.domain.models.correction.*
import com.siraj.app.domain.models.review.ShariaClaim
import com.siraj.app.domain.models.review.ShariaReviewStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentCorrectionHistoryScreen(
    viewModel: ContentCorrectionViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showNewCorrectionDialog by remember { mutableStateOf(false) }
    var showReviewDialogForNotice by remember { mutableStateOf<CorrectionNotice?>(null) }

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
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "نظام التصحيح والإصدارات الشرعية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "سجل التعديلات الثابت ومتابعة الأثر والمصادر",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showNewCorrectionDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = "طلب تصحيح جديد",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNewCorrectionDialog = true },
                icon = { Icon(Icons.Default.EditNote, contentDescription = null) },
                text = { Text("طلب تصحيح وإصدار جديد") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("الإصدارات (${uiState.versions.size})") },
                    icon = { Icon(Icons.Default.History, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("إشعارات التصحيح (${uiState.notices.size})") },
                    icon = { Icon(Icons.Default.NotificationsActive, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("مقارنة المصادر (${uiState.sourceRevisions.size})") },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("المحتوى المتأثر (${uiState.affectedAssets.size})") },
                    icon = { Icon(Icons.Default.AutoFixHigh, contentDescription = null) }
                )
            }

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Content per tab
            when (selectedTab) {
                0 -> VersionsTimelineView(
                    versions = uiState.versions,
                    selectedVersion = uiState.selectedVersion,
                    onSelectVersion = { viewModel.selectVersion(it) }
                )
                1 -> CorrectionNoticesView(
                    notices = uiState.notices,
                    selectedNotice = uiState.selectedNotice,
                    onSelectNotice = { viewModel.selectNotice(it) },
                    onReviewNotice = { showReviewDialogForNotice = it },
                    onGenerateReport = { viewModel.generateImpactReport(it.id) }
                )
                2 -> SourceRevisionsView(
                    sourceRevisions = uiState.sourceRevisions
                )
                3 -> AffectedAssetsView(
                    affectedAssets = uiState.affectedAssets,
                    onUpdateStatus = { id, status -> viewModel.updateAssetStatus(id, status) }
                )
            }
        }
    }

    // Dialogs
    if (showNewCorrectionDialog) {
        NewCorrectionDialog(
            currentVersion = uiState.versions.maxByOrNull { it.versionNumber },
            onDismiss = { showNewCorrectionDialog = false },
            onConfirm = { type, reason, explanation, discoverer, discType, title, text, claims, sources, sourceRevs, assets, summary, noticeText, forceSuspend ->
                viewModel.createCorrection(
                    correctionType = type,
                    reason = reason,
                    detailedExplanation = explanation,
                    discoveredBy = discoverer,
                    discoveredByType = discType,
                    correctedTitle = title,
                    correctedFullContentText = text,
                    correctedClaims = claims,
                    correctedSources = sources,
                    sourceRevisions = sourceRevs,
                    affectedAssets = assets,
                    createdBy = "creator_current",
                    createdByName = "فريق تحرير سراج",
                    changeSummary = summary,
                    publicNoticeText = noticeText,
                    forceImmediateSuspension = forceSuspend
                )
                showNewCorrectionDialog = false
            }
        )
    }

    showReviewDialogForNotice?.let { notice ->
        ReviewCorrectionDialog(
            notice = notice,
            onDismiss = { showReviewDialogForNotice = null },
            onConfirm = { isApproved, reviewerName, notes, evidences ->
                viewModel.submitReview(
                    noticeId = notice.id,
                    isApproved = isApproved,
                    reviewerId = "rev_sharia_expert_1",
                    reviewerName = reviewerName,
                    reviewerSpecialty = "الحديث الشريف وتخريجه",
                    notes = notes,
                    shariaEvidences = evidences
                )
                showReviewDialogForNotice = null
            }
        )
    }

    uiState.impactReport?.let { report ->
        ImpactReportDialog(
            report = report,
            onDismiss = { viewModel.clearImpactReport() }
        )
    }
}

// -------------------------------------------------------------
// 1. Versions Timeline View
// -------------------------------------------------------------
@Composable
private fun VersionsTimelineView(
    versions: List<ContentVersion>,
    selectedVersion: ContentVersion?,
    onSelectVersion: (ContentVersion) -> Unit
) {
    if (versions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا توجد إصدارات مسجلة بعد", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(versions) { version ->
            VersionItemCard(
                version = version,
                isSelected = selectedVersion?.id == version.id,
                onClick = { onSelectVersion(version) }
            )
        }
    }
}

@Composable
private fun VersionItemCard(
    version: ContentVersion,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale("ar")) }
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    val (statusBg, statusFg) = when (version.status) {
        VersionStatus.ACTIVE_PUBLISHED -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        VersionStatus.SUPERSEDED -> Color(0xFFECEFF1) to Color(0xFF546E7A)
        VersionStatus.RESTRICTED_SUSPENDED -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        VersionStatus.IN_REVIEW -> Color(0xFFFFF8E1) to Color(0xFFF57F17)
        VersionStatus.DRAFT -> Color(0xFFEDE7F6) to Color(0xFF512DA8)
        VersionStatus.ARCHIVED -> Color(0xFFF5F5F5) to Color(0xFF757575)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "الإصدار v${version.versionNumber}",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = version.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = statusBg,
                    contentColor = statusFg
                ) {
                    Text(
                        text = version.status.arabicTitle,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = version.fullContentText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (version.changeSummary.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "ملخص التعديل: ${version.changeSummary}",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (version.isRestricted && version.restrictionReason != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFFEBEE)
                ) {
                    Text(
                        text = "⚠️ قيد مفروض: ${version.restrictionReason}",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC62828)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "أنشأه: ${version.createdByName} • ${dateFormat.format(Date(version.createdAt))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (version.immutableHash.isNotBlank()) {
                    Text(
                        text = "بصمة: ${version.immutableHash.take(8)}...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. Correction Notices View
// -------------------------------------------------------------
@Composable
private fun CorrectionNoticesView(
    notices: List<CorrectionNotice>,
    selectedNotice: CorrectionNotice?,
    onSelectNotice: (CorrectionNotice) -> Unit,
    onReviewNotice: (CorrectionNotice) -> Unit,
    onGenerateReport: (CorrectionNotice) -> Unit
) {
    if (notices.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا توجد إشعارات تصحيح مسجلة", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(notices) { notice ->
            CorrectionNoticeCard(
                notice = notice,
                isSelected = selectedNotice?.id == notice.id,
                onClick = { onSelectNotice(notice) },
                onReview = { onReviewNotice(notice) },
                onGenerateReport = { onGenerateReport(notice) }
            )
        }
    }
}

@Composable
private fun CorrectionNoticeCard(
    notice: CorrectionNotice,
    isSelected: Boolean,
    onClick: () -> Unit,
    onReview: () -> Unit,
    onGenerateReport: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale("ar")) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                if (isSelected) 2.dp else 1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = notice.correctionType.arabicTitle,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "من v${notice.fromVersionNumber} إلى v${notice.toVersionNumber}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "السبب: ${notice.reason}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = notice.detailedExplanation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (notice.publicNoticeText.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFFF3E0)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Campaign,
                            contentDescription = null,
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "الإفصاح العام للمستخدمين: ${notice.publicNoticeText}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "اكتشفه: ${notice.discoveredBy} (${notice.discoveredByType.arabicTitle}) • ${dateFormat.format(Date(notice.reportedAt))}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onGenerateReport,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تقرير الأثر", fontSize = 12.sp)
                }

                if (notice.status == ShariaReviewStatus.PENDING) {
                    Button(
                        onClick = onReview,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مراجعة واعتماد شرعي", fontSize = 12.sp)
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (notice.status == ShariaReviewStatus.APPROVED) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    ) {
                        Text(
                            text = notice.status.arabicTitle,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (notice.status == ShariaReviewStatus.APPROVED) Color(0xFF2E7D32) else Color(0xFFC62828),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. Source Revisions View
// -------------------------------------------------------------
@Composable
private fun SourceRevisionsView(
    sourceRevisions: List<SourceRevision>
) {
    if (sourceRevisions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا توجد استدراكات مصادر مرتبطة بالإشعار الحالي", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(sourceRevisions) { rev ->
            SourceRevisionCard(revision = rev)
        }
    }
}

@Composable
private fun SourceRevisionCard(revision: SourceRevision) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "سبب التعديل: ${revision.correctionReason}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Original vs Corrected comparison
            Row(modifier = Modifier.fillMaxWidth()) {
                // Original Box
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFEBEE)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "❌ المصدر السابق",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = revision.originalSourceTitle,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = revision.originalReference,
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                        if (revision.originalGrade != null) {
                            Text(
                                text = "الدرجة: ${revision.originalGrade}",
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }

                // Corrected Box
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "✅ المصدر المصحح",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = revision.correctedSourceTitle,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = revision.correctedReference,
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                        if (revision.correctedGrade != null) {
                            Text(
                                text = "الدرجة: ${revision.correctedGrade}",
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "النص المصحح: ${revision.correctedText}",
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 4. Affected Assets View
// -------------------------------------------------------------
@Composable
private fun AffectedAssetsView(
    affectedAssets: List<AffectedAsset>,
    onUpdateStatus: (String, AssetImpactStatus) -> Unit
) {
    if (affectedAssets.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا توجد أصول متأثرة مسجلة في هذا الإشعار", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(affectedAssets) { asset ->
            AffectedAssetCard(
                asset = asset,
                onUpdateStatus = { newStatus -> onUpdateStatus(asset.id, newStatus) }
            )
        }
    }
}

@Composable
private fun AffectedAssetCard(
    asset: AffectedAsset,
    onUpdateStatus: (AssetImpactStatus) -> Unit
) {
    val (statusColor, statusBg) = when (asset.status) {
        AssetImpactStatus.REQUIRES_RE_RENDER -> Color(0xFFE65100) to Color(0xFFFFF3E0)
        AssetImpactStatus.SUSPENDED -> Color(0xFFC62828) to Color(0xFFFFEBEE)
        AssetImpactStatus.UPDATED -> Color(0xFF2E7D32) to Color(0xFFE8F5E9)
        AssetImpactStatus.DEPRECATED -> Color(0xFF757575) to Color(0xFFEEEEEE)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (asset.assetType) {
                            AffectedAssetType.SCENE -> Icons.Default.Movie
                            AffectedAssetType.VIDEO_RENDER -> Icons.Default.VideoLibrary
                            AffectedAssetType.AUDIO_TRACK -> Icons.Default.Audiotrack
                            AffectedAssetType.SUBTITLE -> Icons.Default.Subtitles
                            AffectedAssetType.PUBLISHED_FLASH -> Icons.Default.Bolt
                            AffectedAssetType.PROJECT -> Icons.Default.Folder
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = asset.assetName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${asset.projectTitle} • ${asset.impactDescription}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusBg
                ) {
                    Text(
                        text = asset.status.arabicTitle,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (asset.status == AssetImpactStatus.REQUIRES_RE_RENDER) {
                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(
                        onClick = { onUpdateStatus(AssetImpactStatus.UPDATED) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text("تعليم كمحدث", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. New Correction Dialog
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewCorrectionDialog(
    currentVersion: ContentVersion?,
    onDismiss: () -> Unit,
    onConfirm: (
        CorrectionType,
        String,
        String,
        String,
        DiscoveredByType,
        String,
        String,
        List<ShariaClaim>,
        List<Source>,
        List<SourceRevision>,
        List<AffectedAsset>,
        String,
        String,
        Boolean
    ) -> Unit
) {
    var selectedType by remember { mutableStateOf(CorrectionType.SOURCE_ERROR) }
    var reason by remember { mutableStateOf("") }
    var explanation by remember { mutableStateOf("") }
    var discoverer by remember { mutableStateOf("صالح العتيبي") }
    var discoveredByType by remember { mutableStateOf(DiscoveredByType.REVIEWER_AUDIT) }
    var correctedTitle by remember { mutableStateOf(currentVersion?.title ?: "") }
    var correctedText by remember { mutableStateOf(currentVersion?.fullContentText ?: "") }
    var changeSummary by remember { mutableStateOf("") }
    var publicNoticeText by remember { mutableStateOf("") }
    var forceSuspend by remember { mutableStateOf(false) }

    // Source revision state
    var oldSourceTitle by remember { mutableStateOf("المصدر السابق") }
    var correctedSourceTitle by remember { mutableStateOf("صحيح البخاري") }
    var correctedRef by remember { mutableStateOf("كتاب الإيمان، باب 1") }
    var correctedGrade by remember { mutableStateOf("صحيح") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "طلب تصحيح وإصدار نسخة جديدة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "سيتم إنشاء إصدار جديد (v${(currentVersion?.versionNumber ?: 0) + 1}) مع الحفاظ على السجل القديم كاملاً",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Correction Type Dropdown / Chips
                Text("نوع الخطأ المراد تصحيحه:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    CorrectionType.values().take(4).forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.arabicTitle, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("سبب التصحيح الرئيسي") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = explanation,
                    onValueChange = { explanation = it },
                    label = { Text("الشرح التفصيلي لمسوغات التصحيح") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = discoverer,
                    onValueChange = { discoverer = it },
                    label = { Text("الجهة المكتشفة للخطأ") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = correctedText,
                    onValueChange = { correctedText = it },
                    label = { Text("النص الكامل بعد التصحيح") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = changeSummary,
                    onValueChange = { changeSummary = it },
                    label = { Text("ملخص التعديلات في النسخة الجديدة") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = publicNoticeText,
                    onValueChange = { publicNoticeText = it },
                    label = { Text("نص الإفصاح والتنبيه للمستخدمين") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = forceSuspend,
                        onCheckedChange = { forceSuspend = it }
                    )
                    Text("تعليق وإيقاف النسخة القديمة فورياً لحين المراجعة", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (reason.isNotBlank() && explanation.isNotBlank() && discoverer.isNotBlank()) {
                                val sampleSourceRev = SourceRevision(
                                    correctionNoticeId = "",
                                    originalSourceId = "src_old",
                                    originalSourceTitle = oldSourceTitle,
                                    originalReference = "رقم غير دقيق",
                                    originalText = currentVersion?.fullContentText ?: "",
                                    correctedSourceTitle = correctedSourceTitle,
                                    correctedReference = correctedRef,
                                    correctedText = correctedText,
                                    correctedGrade = correctedGrade,
                                    correctionReason = reason
                                )
                                val sampleAffectedAsset = AffectedAsset(
                                    contentId = currentVersion?.contentId ?: "",
                                    correctionNoticeId = "",
                                    projectId = "proj_siraj_101",
                                    projectTitle = currentVersion?.title ?: "مشروع سراج",
                                    assetType = AffectedAssetType.SCENE,
                                    assetName = "المشهد 1 - الركيزة النصية",
                                    impactDescription = "يتضمن اللفظ المراد استدراكه",
                                    remediationAction = "تحديث بطاقة النص وإعادة الرندرة"
                                )

                                onConfirm(
                                    selectedType,
                                    reason,
                                    explanation,
                                    discoverer,
                                    discoveredByType,
                                    correctedTitle,
                                    correctedText,
                                    emptyList(),
                                    emptyList(),
                                    listOf(sampleSourceRev),
                                    listOf(sampleAffectedAsset),
                                    changeSummary,
                                    publicNoticeText,
                                    forceSuspend
                                )
                            }
                        }
                    ) {
                        Text("إنشاء وإرسال للمراجعة")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 6. Review Correction Dialog
// -------------------------------------------------------------
@Composable
private fun ReviewCorrectionDialog(
    notice: CorrectionNotice,
    onDismiss: () -> Unit,
    onConfirm: (Boolean, String, String, List<String>) -> Unit
) {
    var isApproved by remember { mutableStateOf(true) }
    var reviewerName by remember { mutableStateOf("د. عبد العزيز المقرن") }
    var notes by remember { mutableStateOf("تمت المراجعة والتأكد من صحة التخريج والاستدراك الشرعي.") }
    var evidences by remember { mutableStateOf("صحيح البخاري، مسند الإمام أحمد") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "المراجعة والاعتماد الشرعي للتصحيح",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "إشعار من v${notice.fromVersionNumber} إلى v${notice.toVersionNumber} (${notice.reason})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = isApproved,
                        onClick = { isApproved = true }
                    )
                    Text("اعتماد ونشر النسخة المصححة", fontWeight = FontWeight.SemiBold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = !isApproved,
                        onClick = { isApproved = false }
                    )
                    Text("رفض التصحيح وطلب تعديل إضافي", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = reviewerName,
                    onValueChange = { reviewerName = it },
                    label = { Text("اسم المراجع الشرعي") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات المراجع الشرعي") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = evidences,
                    onValueChange = { evidences = it },
                    label = { Text("الأدلة والمراجع المعتمدة (مفصولة بفواصل)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val evidenceList = evidences.split("،", ",").map { it.trim() }.filter { it.isNotBlank() }
                            onConfirm(isApproved, reviewerName, notes, evidenceList)
                        }
                    ) {
                        Text("حفظ القرار الشرعي")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 7. Impact Report Dialog
// -------------------------------------------------------------
@Composable
private fun ImpactReportDialog(
    report: ImpactReport,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "📊 تقرير حصر الأثر للمحتوى المتأثر",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = report.summary,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "إحصائيات الأثر:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text("• إجمالي المواد المتأثرة: ${report.totalAffectedAssetsCount}")
                Text("• المشاريع الإنتاجية المتأثرة: ${report.affectedProjectsCount}")
                Text("• المشاهد المتأثرة: ${report.affectedScenesCount}")
                Text("• ملفات الفيديو المرندرة: ${report.affectedVideoRendersCount}")
                Text("• الومضات المنشورة: ${report.affectedPublishedFlashesCount}")

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("إغلاق")
                }
            }
        }
    }
}
