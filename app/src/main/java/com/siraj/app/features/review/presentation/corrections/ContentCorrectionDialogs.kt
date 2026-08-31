package com.siraj.app.features.review.presentation.corrections

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.siraj.app.domain.models.correction.*
import com.siraj.app.domain.models.review.ShariaClaim
import com.siraj.app.domain.models.review.ShariaReviewStatus
import com.siraj.app.ui.theme.statusColors

@Composable
fun NewCorrectionDialog(
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
fun ReviewCorrectionDialog(
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
