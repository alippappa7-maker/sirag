package com.siraj.app.features.flashes.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.siraj.app.core.ui.components.SirajButton
import com.siraj.app.domain.models.flash.FlashPublishingState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashPublishingScreen(
    viewModel: FlashPublishingViewModel,
    currentUserId: String,
    currentUserName: String,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("عام") }
    var visibility by remember { mutableStateOf("PUBLIC") }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error, state.successMessage) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(state.currentFlash) {
        state.currentFlash?.let {
            title = it.title
            description = it.description
            category = it.category
            visibility = it.visibility.name
            viewModel.loadAuditLogs(it.id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("نشر ومضة") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.currentFlash == null) {
                // Step 1: Upload / Create Draft Mock
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("الخطوة 1: اختيار مقطع الفيديو", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .background(Color.Gray.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("منطقة اختيار المقطع وقصه وتحديد الغلاف")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        SirajButton(
                            text = "رفع وحفظ كمسودة",
                            onClick = {
                                viewModel.createDraft(
                                    creatorId = currentUserId,
                                    creatorName = currentUserName,
                                    workspaceId = "workspace_1",
                                    videoFile = null,
                                    assetId = "video_123",
                                    durationMs = 45000 // 45s
                                )
                            },
                            
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                val flash = state.currentFlash!!
                
                // Status Banner
                val statusColor = when(flash.publishingState) {
                    FlashPublishingState.APPROVED, FlashPublishingState.PUBLISHED -> Color(0xFF4CAF50)
                    FlashPublishingState.REJECTED, FlashPublishingState.SUSPENDED -> Color(0xFFF44336)
                    FlashPublishingState.PENDING_REVIEW -> Color(0xFFFF9800)
                    else -> MaterialTheme.colorScheme.primary
                }

                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (flash.publishingState == FlashPublishingState.REJECTED) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = statusColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("حالة الومضة:", style = MaterialTheme.typography.labelMedium)
                            Text(flash.publishingState.titleArabic, style = MaterialTheme.typography.titleMedium, color = statusColor, fontWeight = FontWeight.Bold)
                            
                            flash.rejectionReason?.let {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("السبب: $it", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // Step 2: Edit Details
                Text("تفاصيل الومضة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                val isEditable = flash.publishingState == FlashPublishingState.DRAFT || flash.publishingState == FlashPublishingState.REJECTED || flash.publishingState == FlashPublishingState.CORRECTED

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("العنوان") },
                    enabled = isEditable
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("الوصف") },
                    enabled = isEditable,
                    singleLine = false,
                    maxLines = 4
                )

                if (isEditable) {
                    SirajButton(
                        text = "حفظ التعديلات",
                        onClick = {
                            viewModel.updateDetails(
                                flashId = flash.id,
                                title = title,
                                description = description,
                                category = category,
                                tags = emptyList(),
                                visibility = visibility,
                                showCreatorInfo = true,
                                sourceIds = listOf("src_1") // mock source
                            )
                        },
                        
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Actions based on state
                when (flash.publishingState) {
                    FlashPublishingState.DRAFT, FlashPublishingState.REJECTED -> {
                        SirajButton(
                            text = "إرسال للمراجعة (فحص تلقائي)",
                            onClick = { viewModel.submitForReview(flash.id, currentUserId) },
                            
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    FlashPublishingState.PENDING_REVIEW -> {
                        // Mock Admin Approval for testing
                        OutlinedButton(
                            onClick = { viewModel.mockApprove(flash.id, "admin_user") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("[محاكاة] اعتماد كمراجع")
                        }
                    }
                    FlashPublishingState.APPROVED -> {
                        SirajButton(
                            text = "نشر الآن",
                            onClick = { viewModel.publish(flash.id, currentUserId) },
                            
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    else -> {}
                }

                if (state.auditLogs.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("سجل الحالات (Audit Log)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    state.auditLogs.forEach { log ->
                        Text(
                            text = "- انتقلت إلى ${log.toState.titleArabic} بواسطة ${log.actionBy}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
