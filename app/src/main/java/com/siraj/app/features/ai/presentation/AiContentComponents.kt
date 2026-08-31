package com.siraj.app.features.ai.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.siraj.app.domain.models.AiMetadata

@Composable
fun AiContentLabel(
    aiMetadata: AiMetadata,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    if (aiMetadata.generatedByAI) {
        AssistChip(
            onClick = onClick,
            label = { Text("مُوَلَّد بالذكاء الاصطناعي") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "مُوَلَّد بالذكاء الاصطناعي",
                    modifier = Modifier.size(16.dp),
                )
            },
            colors =
                AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    labelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    leadingIconContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                ),
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiDisclosureBottomSheet(
    aiMetadata: AiMetadata,
    onDismissRequest: () -> Unit,
    onReportClick: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "إفصاح الذكاء الاصطناعي",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }

            Text(
                text = "هذا المحتوى تم إنشاؤه أو تعديله باستخدام الذكاء الاصطناعي لتسهيل عملية الإنتاج.",
                style = MaterialTheme.typography.bodyMedium,
            )

            Divider()

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                aiMetadata.provider?.let {
                    Text("المزود: $it", style = MaterialTheme.typography.bodySmall)
                }
                aiMetadata.model?.let {
                    Text("النموذج: $it", style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    text = if (aiMetadata.humanReviewed) "تمت مراجعته بشرياً" else "لم تتم مراجعته بشرياً",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (aiMetadata.humanReviewed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                )
            }

            if (aiMetadata.aiDisclaimers.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                            Text("إخلاء مسؤولية", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                        aiMetadata.aiDisclaimers.forEach { disclaimer ->
                            Text(
                                "- $disclaimer",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onReportClick,
                modifier = Modifier.fillMaxWidth(),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
            ) {
                Icon(Icons.Default.Report, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("إبلاغ عن محتوى مسيء أو مضلل")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AiReportDialog(
    onDismissRequest: () -> Unit,
    onSubmitReport: (String, String) -> Unit,
) {
    var selectedReason by remember { mutableStateOf("محتوى مضلل أو كاذب") }
    var details by remember { mutableStateOf("") }

    val reasons =
        listOf(
            "محتوى مضلل أو كاذب",
            "انتحال شخصية أو صوت",
            "مخالفة شرعية جسيمة",
            "محتوى مسيء أو غير لائق",
            "أخرى",
        )

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("الإبلاغ عن المحتوى المولد") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("يرجى تحديد سبب الإبلاغ عن هذا المحتوى:")
                reasons.forEach { reason ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { selectedReason = reason }
                                .padding(vertical = 4.dp),
                    ) {
                        RadioButton(
                            selected = (reason == selectedReason),
                            onClick = { selectedReason = reason },
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(reason)
                    }
                }

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("تفاصيل إضافية (اختياري)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSubmitReport(selectedReason, details) }) {
                Text("إرسال الإبلاغ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("إلغاء")
            }
        },
    )
}
