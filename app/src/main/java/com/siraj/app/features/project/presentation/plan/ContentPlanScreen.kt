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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentPlanScreen(
    projectId: String,
    onNavigateBack: () -> Unit,
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
                            onSendSourceForReview = viewModel::sendSourceForReview
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
    onSendReview: () -> Unit,
    onUpdateSource: (String, Source) -> Unit,
    onRemoveSource: (String) -> Unit,
    onSendSourceForReview: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    val pendingClaims = plan.claims.filter { it.riskLevel == RiskLevel.HIGH && it.reviewStatus != ReviewStatus.APPROVED }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Disclaimer Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "تنبيه: هذه المسودة المولدة تعبر عن صياغة إبداعية، وليست فتوى دينية. النصوص الشرعية يجب التحقق منها.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
        
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("السيناريو") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { 
                Text("الادعاءات والمصادر ${if(pendingClaims.isNotEmpty()) "(${pendingClaims.size})" else ""}") 
            })
        }
        
        if (selectedTab == 0) {
            // Script Tab
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedCard(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("الجمهور", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            Text(brief.targetAudience, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    OutlinedCard(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("المدة التقديرية", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            Text(plan.estimatedDuration, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            
                OutlinedTextField(
                    value = plan.title,
                    onValueChange = { s -> onUpdate { it.copy(title = s) } },
                    label = { Text("العنوان") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = plan.hook,
                    onValueChange = { s -> onUpdate { it.copy(hook = s) } },
                    label = { Text("الخطاف (المقدمة الجاذبة)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                OutlinedTextField(
                    value = plan.mainPoints,
                    onValueChange = { s -> onUpdate { it.copy(mainPoints = s) } },
                    label = { Text("النقاط الرئيسية (جسم المحتوى)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5
                )
                OutlinedTextField(
                    value = plan.conclusion,
                    onValueChange = { s -> onUpdate { it.copy(conclusion = s) } },
                    label = { Text("الخاتمة / الرسالة") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                OutlinedTextField(
                    value = plan.callToAction,
                    onValueChange = { s -> onUpdate { it.copy(callToAction = s) } },
                    label = { Text("الدعوة لاتخاذ إجراء (CTA)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        } else {
            // Claims Tab
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                if (plan.claims.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("لا توجد ادعاءات شرعية حساسة تم رصدها.")
                    }
                } else {
                    Text("الادعاءات والنصوص المستخرجة (${plan.claims.size})", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("يتم فصل الحقائق والنصوص الشرعية عن الصياغة الإبداعية لمراجعتها وإرفاق المصادر.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(plan.claims) { claim ->
                            ClaimCard(claim = claim, onUpdateSource = onUpdateSource, onRemoveSource = onRemoveSource, onSendSourceForReview = onSendSourceForReview)
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            if (pendingClaims.isNotEmpty()) {
                                Button(
                                    onClick = onSendReview,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("إرسال الادعاءات للمراجعة الشرعية")
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("لا يمكن نقل المحتوى للإنتاج قبل مراجعة واعتماد الادعاءات الحساسة.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            } else {
                                Button(
                                    onClick = { /* Move to Production */ },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("المحتوى جاهز للإنتاج والتصدير")
                                }
                            }
                        }
                    }
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
    onSendSourceForReview: (String) -> Unit
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
                        TextButton(onClick = { showSourceDialog = true }) {
                            Text(if (attachedSource.reviewStatus == SourceVerificationStatus.VERIFIED) "تعديل (سينشئ نسخة جديدة)" else "تعديل")
                        }
                        TextButton(onClick = { onRemoveSource(claim.id) }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                            Text("إزالة")
                        }
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("حالة المصدر: مفقود", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = { showSourceDialog = true }) {
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
