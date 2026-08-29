package com.siraj.app.core.ui.components

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.core.config.EnvironmentConfig
import com.siraj.app.domain.models.beta.FeedbackCategory
import com.siraj.app.domain.models.beta.FeedbackSeverity
import com.siraj.app.features.beta.BetaFeedbackViewModel
import com.siraj.app.features.beta.BetaFeedbackViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BetaFeedbackDialog(
    currentRoute: String = "",
    onDismissRequest: () -> Unit,
    viewModel: BetaFeedbackViewModel = viewModel(factory = BetaFeedbackViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(currentRoute) {
        viewModel.setRoute(currentRoute)
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "BETA",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = "إرسال ملاحظة أو عطل فني",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                if (uiState.isSuccess) {
                    // Success View
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.successMessage ?: "تم استلام ملاحظتك بنجاح",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "شكراً لمساهمتك في تجربة النسخة التجريبية لسراج!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                viewModel.clearStatus()
                                onDismissRequest()
                            },
                            modifier = Modifier.fillMaxWidth(0.6f)
                        ) {
                            Text("إغلاق")
                        }
                    }
                } else {
                    // Feedback Form
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Environment & Device Diagnostics Info Box
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "معلومات التشخيص التلقائية",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        text = "النسخة: ${EnvironmentConfig.versionName} (${EnvironmentConfig.currentEnvironment.displayName})",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "الجهاز: ${uiState.deviceModel} | ${uiState.androidVersion}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    if (currentRoute.isNotBlank()) {
                                        Text(
                                            text = "الشاشة الحالية: $currentRoute",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }

                        // Category Selection
                        item {
                            Text(
                                text = "نوع الملاحظة:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(FeedbackCategory.values()) { category ->
                                    FilterChip(
                                        selected = uiState.category == category,
                                        onClick = { viewModel.updateCategory(category) },
                                        label = { Text(category.title) }
                                    )
                                }
                            }
                        }

                        // Severity Level
                        item {
                            Text(
                                text = "مستوى الأهمية:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                FeedbackSeverity.values().forEach { severity ->
                                    val isSelected = uiState.severity == severity
                                    val chipColor = Color(severity.levelColorHex)
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) chipColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, chipColor) else null,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { viewModel.updateSeverity(severity) }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(chipColor)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = severity.label,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Title Field
                        item {
                            OutlinedTextField(
                                value = uiState.title,
                                onValueChange = { viewModel.updateTitle(it) },
                                label = { Text("عنوان الملاحظة / العطل *") },
                                placeholder = { Text("مثال: توقف تصدير الفيديو عند المشهد الثاني") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Description Field
                        item {
                            OutlinedTextField(
                                value = uiState.description,
                                onValueChange = { viewModel.updateDescription(it) },
                                label = { Text("وصف التفاصيل وما حدث معك *") },
                                placeholder = { Text("اشرح بالتفصيل ما الذي كنت تحاول فعله وما هي النتيجة...") },
                                minLines = 3,
                                maxLines = 5,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Steps to Reproduce Field
                        item {
                            OutlinedTextField(
                                value = uiState.stepsToReproduce,
                                onValueChange = { viewModel.updateSteps(it) },
                                label = { Text("خطوات تكرار المشكلة (اختياري)") },
                                placeholder = { Text("1. الدخول على محرر المشاهد\n2. النقر على توليد الصوت\n3. ظهور رسالة خطأ") },
                                minLines = 2,
                                maxLines = 4,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Error Message
                        if (uiState.errorMessage != null) {
                            item {
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = uiState.errorMessage ?: "",
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Submit Actions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismissRequest,
                            enabled = !uiState.isSubmitting
                        ) {
                            Text("إلغاء")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.submitFeedback() },
                            enabled = !uiState.isSubmitting
                        ) {
                            if (uiState.isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("جاري الإرسال...")
                            } else {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("إرسال الملاحظة")
                            }
                        }
                    }
                }
            }
        }
    }
}
