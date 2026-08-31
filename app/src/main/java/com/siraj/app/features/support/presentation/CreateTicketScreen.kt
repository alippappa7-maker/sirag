package com.siraj.app.features.support.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.siraj.app.core.support.SupportSanitizerEngine
import com.siraj.app.domain.models.support.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTicketScreen(
    initialCategory: TicketCategory? = null,
    viewModel: SupportViewModel,
    onNavigateBack: () -> Unit,
    onTicketCreated: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showPriorityDropdown by remember { mutableStateOf(false) }
    var showLogsPreviewDialog by remember { mutableStateOf(false) }

    LaunchedEffect(initialCategory) {
        if (initialCategory != null) {
            viewModel.setFormCategory(initialCategory)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("فتح تذكرة دعم جديدة", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Security & Sharia Warning Notice
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تنبيهات وضوابط الدعم", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "• لن يطلب منك موظفو سراج كلمة المرور أو مفاتيح API إطلاقاً.\n" +
                                    "• مركز الدعم ليس جهة إفتاء، والبلاغات الشرعية تُحول مباشرة إلى المراجعين الشرعيين.\n" +
                                    "• التذاكر المالية تُوجه بشكل مشفر إلى فريق الفوترة المعتمد.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Target Category Picker
            item {
                Text("نوع المشكلة أو الاستفسار *", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = showCategoryDropdown,
                    onExpandedChange = { showCategoryDropdown = !showCategoryDropdown }
                ) {
                    OutlinedTextField(
                        value = uiState.formCategory.titleAr,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown) },
                        leadingIcon = {
                            Icon(
                                when (uiState.formCategory) {
                                    TicketCategory.SHARIA_CONTENT_ERROR, TicketCategory.MIHRAB_AND_QURAN, TicketCategory.SOURCE_CORRECTION -> Icons.Default.AutoStories
                                    TicketCategory.PAYMENT_AND_BILLING -> Icons.Default.Payment
                                    TicketCategory.EXPORT_AND_RENDERING -> Icons.Default.MovieCreation
                                    TicketCategory.ACCOUNT_AND_PRIVACY -> Icons.Default.Lock
                                    TicketCategory.APPEAL_AND_POLICY -> Icons.Default.Gavel
                                    else -> Icons.Default.HelpOutline
                                },
                                contentDescription = null
                            )
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        TicketCategory.values().forEach { cat ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(cat.titleAr, fontWeight = FontWeight.Medium)
                                        Text(
                                            "يتم التوجيه إلى: ${cat.defaultTeam.titleAr}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.setFormCategory(cat)
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Sharia-Specific Field (If Sharia Error selected)
            if (uiState.formCategory == TicketCategory.SHARIA_CONTENT_ERROR || 
                uiState.formCategory == TicketCategory.MIHRAB_AND_QURAN || 
                uiState.formCategory == TicketCategory.SOURCE_CORRECTION) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                "بيانات النص الشرعي أو القرآني المراد مراجعته",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = uiState.formShariaRef,
                                onValueChange = { viewModel.setFormShariaRef(it) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("مثال: سورة الكهف الآية 10 / حديث إنما الأعمال بالنيات") },
                                label = { Text("السورة والآية أو المصدر والحديث") },
                                singleLine = true
                            )
                        }
                    }
                }
            }

            // Billing-Specific Field (If Billing Issue selected)
            if (uiState.formCategory == TicketCategory.PAYMENT_AND_BILLING) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                "بيانات العملية المالية والاشتراك",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = uiState.formBillingTxId,
                                onValueChange = { viewModel.setFormBillingTxId(it) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("مثال: GPA.3481-9281-9012-39210") },
                                label = { Text("رقم الطلب في متجر Google Play (اختياري)") },
                                singleLine = true
                            )
                        }
                    }
                }
            }

            // Subject
            item {
                Text("عنوان التذكرة *", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = uiState.formSubject,
                    onValueChange = { viewModel.setFormSubject(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("ملخص المشكلة باختصار...") },
                    singleLine = true
                )
            }

            // Priority Selection
            item {
                Text("مستوى الأهمية", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = showPriorityDropdown,
                    onExpandedChange = { showPriorityDropdown = !showPriorityDropdown }
                ) {
                    OutlinedTextField(
                        value = uiState.formPriority.titleAr,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPriorityDropdown) }
                    )
                    ExposedDropdownMenu(
                        expanded = showPriorityDropdown,
                        onDismissRequest = { showPriorityDropdown = false }
                    ) {
                        TicketPriority.values().forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p.titleAr) },
                                onClick = {
                                    viewModel.setFormPriority(p)
                                    showPriorityDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Description
            item {
                Text("تفاصيل المشكلة والخطوات *", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = uiState.formDescription,
                    onValueChange = { viewModel.setFormDescription(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    placeholder = { Text("يرجى كتابة التفاصيل لمساعدتنا في معالجة المشكلة بأسرع وقت...") },
                    maxLines = 6
                )
            }

            // Safe Diagnostics Log Attachment Checkbox
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = uiState.isSafeLogsAttached,
                                    onCheckedChange = { viewModel.setSafeLogsAttached(it) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        "إرفاق سجلات التشخيص الآمنة",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "معلومات النظام والذاكرة مجردة تماماً من أي أسرار أو بيانات خاصة",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        if (uiState.isSafeLogsAttached && uiState.safeLogsPreview != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { showLogsPreviewDialog = true }) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("معاينة السجلات الآمنة المرفقة")
                            }
                        }
                    }
                }
            }

            // Submit Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        viewModel.submitTicket { created ->
                            onTicketCreated(created.id)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !uiState.isLoading && uiState.formSubject.isNotBlank() && uiState.formDescription.isNotBlank()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إرسال تذكرة الدعم", style = MaterialTheme.typography.titleMedium)
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // Safe logs preview dialog
        if (showLogsPreviewDialog && uiState.safeLogsPreview != null) {
            val logs = uiState.safeLogsPreview ?: ""
            AlertDialog(
                onDismissRequest = { showLogsPreviewDialog = false },
                title = { Text("معاينة سجلات التشخيص الآمنة") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("إصدار التطبيق: ${logs.appVersion} (${logs.buildNumber})", style = MaterialTheme.typography.bodySmall)
                        Text("نظام التشغيل: ${logs.osVersion}", style = MaterialTheme.typography.bodySmall)
                        Text("طراز الجهاز: ${logs.deviceModel}", style = MaterialTheme.typography.bodySmall)
                        Text("الذاكرة المتاحة: ${logs.memoryAvailableMb} MB", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("السجلات المجردة:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        logs.sanitizedLogs.forEach { log ->
                            Text("• $log", style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "✓ تم التأكد: لا تحتوي هذه السجلات على أي كلمات مرور أو مفاتيح API أو نصوص خاصة.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLogsPreviewDialog = false }) {
                        Text("حسناً")
                    }
                }
            )
        }
    }
}
