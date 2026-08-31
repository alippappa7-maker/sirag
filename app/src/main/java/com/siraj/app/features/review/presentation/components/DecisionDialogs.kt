package com.siraj.app.features.review.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.siraj.app.domain.models.review.CriticalTopic
import com.siraj.app.domain.models.review.RiskLevel
import com.siraj.app.domain.models.review.ShariaReviewItem
import java.util.*
import com.siraj.app.ui.theme.statusColors

@Composable
fun ApproveDialog(
    item: ShariaReviewItem,
    onConfirm: (reason: String, reReviewTimestamp: Long?) -> Unit,
    onDismiss: () -> Unit,
) {
    var reason by remember { mutableStateOf("") }
    val isCritical =
        item.riskLevel == RiskLevel.CRITICAL ||
            item.criticalTopics.any { it != CriticalTopic.NONE }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.statusColors.successFg,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isCritical) "اعتماد أولي (يتطلب مراجعاً ثانياً)" else "اعتماد المحتوى شرعياً",
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (isCritical) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تنبيه: هذا المحتوى يندرج تحت موضوع حرج (${item.criticalTopics
                                    .firstOrNull {
                                        it != CriticalTopic.NONE
                                    }?.arabicTitle ?: "فائق الخطورة"}) وسينتقل لحالة 'بانتظار الاعتماد المشترك' لمراجع ثانٍ مؤهل.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    text = "سبب ومسوغات الاعتماد الشرعي:",
                    style = MaterialTheme.typography.labelMedium,
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    placeholder = { Text("تم التحقق من النصوص وتوافقها مع المصادر المعتمدة...") },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("input_approve_reason"),
                    shape = RoundedCornerShape(8.dp),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(reason.ifBlank { "تم التدقيق والموافقة الشرعية" }, null)
                },
                modifier = Modifier.testTag("btn_confirm_approve"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.statusColors.successFg),
            ) {
                Text(if (isCritical) "تأكيد الاعتماد الأولي" else "اعتماد ونشر")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    androidx.compose.ui.res
                        .stringResource(com.siraj.app.R.string.cancel),
                )
            }
        },
    )
}

@Composable
fun RejectDialog(
    onConfirm: (reason: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Cancel,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("رفض المحتوى شرعياً", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "بيان سبب الرفض الشرعي والمخالفات المحددة:",
                    style = MaterialTheme.typography.labelMedium,
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    placeholder = { Text("وجود أحاديث موضوعة / تأويل باطل / خطأ عقدي صريح...") },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("input_reject_reason"),
                    shape = RoundedCornerShape(8.dp),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.isNotBlank()) onConfirm(reason)
                },
                enabled = reason.isNotBlank(),
                modifier = Modifier.testTag("btn_confirm_reject"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            ) {
                Text("تأكيد الرفض")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    androidx.compose.ui.res
                        .stringResource(com.siraj.app.R.string.cancel),
                )
            }
        },
    )
}

@Composable
fun RequestChangesDialog(
    onConfirm: (requiredChanges: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var requiredChanges by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.EditNote,
                    contentDescription = null,
                    tint = MaterialTheme.statusColors.warningFg,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("طلب تعديل شرعي من المنشئ", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "حدد التعديلات المطلوبة بدقة (تصحيح نص، إضافة مرجع، إزالة حكم جازم):",
                    style = MaterialTheme.typography.labelMedium,
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = requiredChanges,
                    onValueChange = { requiredChanges = it },
                    placeholder = { Text("يرجى تصحيح لفظ الحديث في المشهد 2، وتوثيق قول العالم...") },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("input_request_changes"),
                    shape = RoundedCornerShape(8.dp),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (requiredChanges.isNotBlank()) onConfirm(requiredChanges)
                },
                enabled = requiredChanges.isNotBlank(),
                modifier = Modifier.testTag("btn_confirm_request_changes"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.statusColors.warningFg),
            ) {
                Text("إرسال طلب التعديل")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    androidx.compose.ui.res
                        .stringResource(com.siraj.app.R.string.cancel),
                )
            }
        },
    )
}

@Composable
fun EscalateDialog(
    onConfirm: (reviewerId: String, reviewerName: String, reason: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedReviewer by remember { mutableStateOf("rev_othman" to "الشيخ عثمان الدوسري (خبير فقه المعاملات)") }
    var reason by remember { mutableStateOf("") }

    val availableReviewers =
        listOf(
            "rev_othman" to "الشيخ عثمان الدوسري (خبير فقه المعاملات)",
            "rev_tariq" to "د. طارق السلمان (متخصص في العقيدة والمذاهب)",
            "rev_khalid" to "د. خالد السعيد (خبير في الحديث الشريف وعلومه)",
        )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.GroupAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("تحويل لمراجع شرعي ثانٍ", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "اختر المراجع الشرعي المتخصص:",
                    style = MaterialTheme.typography.labelMedium,
                )
                Spacer(modifier = Modifier.height(6.dp))

                availableReviewers.forEach { (id, name) ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selectedReviewer.first == id,
                            onClick = { selectedReviewer = id to name },
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "سبب التحويل والاستشارة الشرعية:",
                    style = MaterialTheme.typography.labelMedium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    placeholder = { Text("المسألة تتطلب تدقيقاً فقهياً في تكييف العقد المعاصر...") },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .testTag("input_escalate_reason"),
                    shape = RoundedCornerShape(8.dp),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        selectedReviewer.first,
                        selectedReviewer.second,
                        reason.ifBlank { "تحويل للاختصاص والمراجعة المشتركة" },
                    )
                },
                modifier = Modifier.testTag("btn_confirm_escalate"),
            ) {
                Text("تأكيد التحويل")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    androidx.compose.ui.res
                        .stringResource(com.siraj.app.R.string.cancel),
                )
            }
        },
    )
}

@Composable
fun DualApprovalDialog(
    item: ShariaReviewItem,
    onConfirm: (approve: Boolean, reason: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var reason by remember { mutableStateOf("") }
    var approve by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("قرار الاعتماد المشترك (المراجع الثاني)", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "قرار المراجع الأول (${item.decision?.primaryReviewerName ?: "المراجع الأول"}):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = item.decision?.primaryNotes ?: "تمت التوصية بالاعتماد",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = approve, onClick = { approve = true })
                    Text("الموافقة والاعتماد المشترك النهائي", style = MaterialTheme.typography.bodyMedium)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = !approve, onClick = { approve = false })
                    Text("طلب تعديل / عدم الموافقة", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    placeholder = { Text("الملاحظات الفقهية للمراجع الثاني...") },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .testTag("input_dual_approval_notes"),
                    shape = RoundedCornerShape(8.dp),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(approve, reason.ifBlank { "تمت المراجعة والتوقيع المشترك" })
                },
                modifier = Modifier.testTag("btn_confirm_dual_approval"),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = if (approve) MaterialTheme.statusColors.successFg else MaterialTheme.statusColors.warningFg,
                    ),
            ) {
                Text(if (approve) "اعتماد مشترك نهائي" else "إرسال الملاحظات")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    androidx.compose.ui.res
                        .stringResource(com.siraj.app.R.string.cancel),
                )
            }
        },
    )
}

@Composable
fun AddInternalNoteDialog(
    onConfirm: (note: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("إضافة ملاحظة داخلية للمراجعين", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "هذه الملاحظة سرية ومحفوظة للمراجعين الشرعيين فقط ولن تظهر لصانع المحتوى:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("ملاحظة فنية أو تنبيه داخلي بخصوص المصدر...") },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("input_internal_note"),
                    shape = RoundedCornerShape(8.dp),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (note.isNotBlank()) onConfirm(note)
                },
                enabled = note.isNotBlank(),
                modifier = Modifier.testTag("btn_confirm_internal_note"),
            ) {
                Text("حفظ الملاحظة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    androidx.compose.ui.res
                        .stringResource(com.siraj.app.R.string.cancel),
                )
            }
        },
    )
}

@Composable
fun AddClaimCommentDialog(
    claim: com.siraj.app.domain.models.review.ShariaClaim,
    onConfirm: (comment: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var comment by remember { mutableStateOf(claim.reviewerComment ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.RateReview,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("التعليق على موضع محدد", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = claim.positionContext,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = claim.claimText,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "الملاحظة الشرعية الموجهة لهذا الجزء:",
                    style = MaterialTheme.typography.labelMedium,
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    placeholder = { Text("يرجى ذكر تخريج الحديث من المصدر الأصلي وتوضيح حكم المحدث...") },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("input_claim_comment"),
                    shape = RoundedCornerShape(8.dp),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (comment.isNotBlank()) onConfirm(comment)
                },
                enabled = comment.isNotBlank(),
                modifier = Modifier.testTag("btn_confirm_claim_comment"),
            ) {
                Text("حفظ الملاحظة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    androidx.compose.ui.res
                        .stringResource(com.siraj.app.R.string.cancel),
                )
            }
        },
    )
}

@Composable
fun ScheduleReReviewDialog(
    onConfirm: (reReviewTimestamp: Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedDays by remember { mutableStateOf(30) } // Default 1 month

    val options =
        listOf(
            7 to "بعد أسبوع (7 أيام)",
            30 to "بعد شهر (30 يوماً)",
            90 to "بعد 3 أشهر (دورية فصلية)",
            180 to "بعد 6 أشهر (نصف سنوية)",
            365 to "بعد سنة كاملة (سنوية)",
        )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.EventRepeat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("تحديد موعد إعادة التدقيق الشرعي", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "يساعد هذا الإجراء في مراجعة المحتوى دورياً للتأكد من مواكبة المصادر والقرارات المجمعية:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
                Spacer(modifier = Modifier.height(10.dp))

                options.forEach { (days, label) ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selectedDays == days,
                            onClick = { selectedDays = days },
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(label, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val timestamp = System.currentTimeMillis() + (selectedDays * 86400000L)
                    onConfirm(timestamp)
                },
                modifier = Modifier.testTag("btn_confirm_schedule_rereview"),
            ) {
                Text("تأكيد الموعد")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    androidx.compose.ui.res
                        .stringResource(com.siraj.app.R.string.cancel),
                )
            }
        },
    )
}
