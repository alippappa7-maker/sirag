package com.siraj.app.features.project.presentation.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.*

import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.window.Dialog

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



fun translateReviewState(state: ReviewState): String {
    return when(state) {
        ReviewState.DRAFT -> "مسودة"
        ReviewState.SUBMITTED -> "تم الإرسال"
        ReviewState.IN_REVIEW -> "قيد المراجعة"
        ReviewState.CHANGES_REQUESTED -> "مطلوب تعديلات"
        ReviewState.APPROVED -> "معتمد"
        ReviewState.REJECTED -> "مرفوض"
        ReviewState.PUBLISHED -> "منشور"
        ReviewState.SUSPENDED -> "معلق"
        ReviewState.CORRECTED -> "تم التصحيح"
    }
}

fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(timestamp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentPlanScreen(
    projectId: String,
    onNavigateBack: () -> Unit,
    onNavigateToScenes: (String) -> Unit = {},
    viewModel: ContentPlanViewModel = viewModel(factory = ContentPlanViewModelFactory(projectId))
) {
    val projectState by viewModel.projectState.collectAsState()
    val saveState by viewModel.saveState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الخطة والسيناريو") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع") }
                },
                actions = {
                    when(saveState) {
                        is SaveState.Saving -> Text("جاري الحفظ...", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(end = 16.dp))
                        is SaveState.Saved -> Text("محفوظ (نسخة تلقائية)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 16.dp))
                        is SaveState.Error -> Text("خطأ حفظ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(end = 16.dp))
                        is SaveState.Idle -> {}
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = projectState) {
                is Resource.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is Resource.Error -> Text(state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                is Resource.Success -> {
                    val project = state.data
                    if (project.contentPlan == null) {
                        // Needs generation
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("الخطة غير موجودة", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("قم بتوليد الخطة والسيناريو بناءً على فكرة الاستوديو.", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = { viewModel.generateMockPlan() }) {
                                Text("توليد السيناريو الآن")
                            }
                        }
                    } else {
                        // Display Editor
                        PlanEditor(
                            plan = project.contentPlan,
                            brief = project.brief,
                            onUpdate = viewModel::updatePlan,
                            onSendReview = viewModel::sendForReview,
                            onUpdateSource = viewModel::updateClaimSource,
                            onRemoveSource = viewModel::removeClaimSource,
                            onSendSourceForReview = viewModel::sendSourceForReview,
                            project = project,
                            onSubmitForReview = viewModel::submitForReview,
                            onSubmitReviewDecision = viewModel::submitReviewDecision,
                            onNavigateToScenes = onNavigateToScenes
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun PlanEditor(
    plan: ContentPlan,
    brief: ContentBrief,
    onUpdate: ((ContentPlan) -> ContentPlan) -> Unit,
    onSave: (ContentPlan) -> Unit = {},
    onSendReview: () -> Unit,
    onUpdateSource: (String, Source) -> Unit,
    onRemoveSource: (String) -> Unit,
    onSendSourceForReview: (String) -> Unit,
    project: Project,
    onSubmitForReview: () -> Unit,
    onSubmitReviewDecision: (ReviewState, String) -> Unit,
    onNavigateToScenes: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showSubmitDialog by remember { mutableStateOf(false) }
    var showReviewerDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Review Status Banner
        val bannerColor = when(project.reviewState) {
            ReviewState.APPROVED, ReviewState.PUBLISHED -> Color(0xFF4CAF50)
            ReviewState.CHANGES_REQUESTED, ReviewState.REJECTED -> MaterialTheme.colorScheme.error
            ReviewState.DRAFT -> MaterialTheme.colorScheme.onSurfaceVariant
            else -> Color(0xFFFFA000)
        }
        
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = bannerColor.copy(alpha = 0.1f)), border = androidx.compose.foundation.BorderStroke(1.dp, bannerColor)) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("حالة الاعتماد: ${translateReviewState(project.reviewState)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = bannerColor)
                    if (project.reviewState == ReviewState.DRAFT || project.reviewState == ReviewState.CHANGES_REQUESTED) {
                        Text("يجب إرسال المحتوى للمراجعة قبل النشر.", style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (project.reviewState == ReviewState.DRAFT || project.reviewState == ReviewState.CHANGES_REQUESTED) {
                    Button(onClick = { showSubmitDialog = true }) {
                        Text("إرسال للمراجعة")
                    }
                } else if (project.reviewState == ReviewState.SUBMITTED || project.reviewState == ReviewState.IN_REVIEW) {
                    // Mock Reviewer Action
                    OutlinedButton(onClick = { showReviewerDialog = true }) {
                        Text("أدوات المراجع")
                    }
                } else if (project.reviewState == ReviewState.APPROVED || project.reviewState == ReviewState.PUBLISHED) {
                    Button(onClick = { onNavigateToScenes(project.id) }) {
                        Text("إنتاج المشاهد")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("الادعاءات والمصادر") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("سجل المراجعة") })
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (selectedTab == 0) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f)) {
                items(plan.claims) { claim ->
                    val isEditable = project.reviewState == ReviewState.DRAFT || project.reviewState == ReviewState.CHANGES_REQUESTED
                    ClaimCard(claim = claim, onUpdateSource = onUpdateSource, onRemoveSource = onRemoveSource, onSendSourceForReview = onSendSourceForReview, isEditable = isEditable)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                if (project.reviewLogs.isEmpty()) {
                    item { Text("لا يوجد سجل للمراجعة بعد.", modifier = Modifier.padding(16.dp)) }
                }
                items(project.reviewLogs.sortedByDescending { it.timestamp }) { log ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${translateReviewState(log.previousState)} -> ${translateReviewState(log.newState)}", fontWeight = FontWeight.Bold)
                                Text(formatDate(log.timestamp), style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(log.comments, style = MaterialTheme.typography.bodyMedium)
                            Text("بواسطة: ${log.actorId}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
    
    if (showSubmitDialog) {
        val unverifiedCount = plan.claims.count { it.attachedSource?.reviewStatus != SourceVerificationStatus.VERIFIED }
        val hasHighRisk = plan.claims.any { it.riskLevel == RiskLevel.HIGH }
        
        Dialog(onDismissRequest = { showSubmitDialog = false }) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("تأكيد الإرسال", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("هل أنت متأكد من إرسال المحتوى للمراجعة؟")
                    
                    HorizontalDivider()
                    
                    Text("تقرير الفحص الآلي:", fontWeight = FontWeight.Bold)
                    Text("• ادعاءات غير موثقة: $unverifiedCount", color = if (unverifiedCount > 0) MaterialTheme.colorScheme.error else Color(0xFF4CAF50))
                    if (hasHighRisk) {
                        Text("• تنبيه: يحتوي المحتوى على ادعاءات عالية الخطورة وتتطلب مراجعة بشرية متخصصة.", color = MaterialTheme.colorScheme.error)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showSubmitDialog = false }) { Text("إلغاء") }
                        Button(onClick = { 
                            onSubmitForReview()
                            showSubmitDialog = false
                        }) {
                            Text("تأكيد الإرسال")
                        }
                    }
                }
            }
        }
    }
    
    if (showReviewerDialog) {
        var reviewerComment by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showReviewerDialog = false }) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("شاشة المراجع", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = reviewerComment,
                        onValueChange = { reviewerComment = it },
                        label = { Text("أضف تعليقاً يبرر قرارك...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = { 
                            onSubmitReviewDecision(ReviewState.REJECTED, reviewerComment.ifEmpty { "تم الرفض بدون تعليق" })
                            showReviewerDialog = false
                        }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                            Text("رفض")
                        }
                        
                        TextButton(onClick = { 
                            onSubmitReviewDecision(ReviewState.CHANGES_REQUESTED, reviewerComment.ifEmpty { "مطلوب تعديلات" })
                            showReviewerDialog = false
                        }, colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFFA000))) {
                            Text("طلب تعديل")
                        }
                        
                        Button(onClick = { 
                            onSubmitReviewDecision(ReviewState.APPROVED, reviewerComment.ifEmpty { "تم الاعتماد" })
                            showReviewerDialog = false
                        }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                            Text("اعتماد")
                        }
                    }
                    TextButton(onClick = { showReviewerDialog = false }, modifier = Modifier.fillMaxWidth()) { Text("إلغاء") }
                }
            }
        }
    }
}
@Composable
fun ClaimCard(
    claim: ContentClaim,
    onUpdateSource: (String, Source) -> Unit,
    onRemoveSource: (String) -> Unit,
    onSendSourceForReview: (String) -> Unit,
    isEditable: Boolean
) {
    val riskColor = when (claim.riskLevel) {
        RiskLevel.HIGH -> MaterialTheme.colorScheme.error
        RiskLevel.MEDIUM -> Color(0xFFFFA000)
        RiskLevel.LOW -> MaterialTheme.colorScheme.primary
    }
    
    val reviewText = when(claim.reviewStatus) {
        ReviewStatus.DRAFT -> "مسودة غير مراجعة"
        ReviewStatus.PENDING_REVIEW -> "قيد المراجعة"
        ReviewStatus.APPROVED -> "معتمد"
        ReviewStatus.REJECTED -> "مرفوض"
    }
    
    var showSourceDialog by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, riskColor.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                AssistChip(onClick = {}, label = { Text(claim.type.name) }, colors = AssistChipDefaults.assistChipColors(leadingIconContentColor = riskColor))
                Text(reviewText, style = MaterialTheme.typography.labelMedium, color = riskColor, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(claim.text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            
            val attachedSource = claim.attachedSource
            if (attachedSource != null) {
                Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small).padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("المصدر المرفق:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = when(attachedSource.reviewStatus) {
                                SourceVerificationStatus.VERIFIED -> "موثق ✓"
                                SourceVerificationStatus.PENDING_REVIEW -> "قيد المراجعة"
                                SourceVerificationStatus.SUGGESTED -> "مقترح"
                                SourceVerificationStatus.REJECTED -> "مرفوض"
                                else -> "غير معتمد"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (attachedSource.reviewStatus == SourceVerificationStatus.VERIFIED) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("العنوان: ${attachedSource.title}", style = MaterialTheme.typography.bodySmall)
                    Text("المرجع: ${attachedSource.reference}", style = MaterialTheme.typography.bodySmall)
                    
                    if (attachedSource.url.isNotEmpty()) {
                        TextButton(onClick = { 
                            try { uriHandler.openUri(attachedSource.url) } catch (e: Exception) { /* handle error */ }
                        }) {
                            Text("فتح الرابط")
                        }
                    }
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        if (attachedSource.reviewStatus != SourceVerificationStatus.VERIFIED && attachedSource.reviewStatus != SourceVerificationStatus.PENDING_REVIEW) {
                            TextButton(onClick = { onSendSourceForReview(claim.id) }) {
                                Text("إرسال للمراجعة")
                            }
                        }
                        TextButton(onClick = { showSourceDialog = true }, enabled = isEditable) {
                            Text(if (attachedSource.reviewStatus == SourceVerificationStatus.VERIFIED) "تعديل (سينشئ نسخة جديدة)" else "تعديل")
                        }
                        TextButton(onClick = { onRemoveSource(claim.id) }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error), enabled = isEditable) {
                            Text("إزالة")
                        }
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("حالة المصدر: مفقود", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = { showSourceDialog = true }, enabled = isEditable) {
                        Text("إرفاق مصدر")
                    }
                }
            }
        }
    }
    
    if (showSourceDialog) {
        SourceEditorDialog(
            initialSource = claim.attachedSource ?: Source(),
            onDismiss = { showSourceDialog = false },
            onSave = { updatedSource ->
                onUpdateSource(claim.id, updatedSource)
                showSourceDialog = false
            }
        )
    }
}

@Composable
fun SourceEditorDialog(
    initialSource: Source,
    onDismiss: () -> Unit,
    onSave: (Source) -> Unit
) {
    var title by remember { mutableStateOf(initialSource.title) }
    var reference by remember { mutableStateOf(initialSource.reference) }
    var url by remember { mutableStateOf(initialSource.url) }
    var author by remember { mutableStateOf(initialSource.authorOrNarrator) }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("إرفاق / تعديل مصدر", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان المصدر (الكتاب، واسم السورة، إلخ)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("المؤلف أو الراوي") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reference,
                    onValueChange = { reference = it },
                    label = { Text("المرجع (الجزء/الصفحة، أو رقم الحديث)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("رابط توثيق (اختياري)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Button(onClick = {
                        onSave(initialSource.copy(
                            title = title,
                            authorOrNarrator = author,
                            reference = reference,
                            url = url,
                            reviewStatus = if (initialSource.reviewStatus == SourceVerificationStatus.UNVERIFIED) SourceVerificationStatus.SUGGESTED else initialSource.reviewStatus
                        ))
                    }) {
                        Text("حفظ المصدر")
                    }
                }
            }
        }
    }
}
