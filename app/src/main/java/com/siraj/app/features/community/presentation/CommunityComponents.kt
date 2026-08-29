package com.siraj.app.features.community.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.siraj.app.domain.models.community.ReportTargetType
import com.siraj.app.domain.models.community.ReportType
import androidx.compose.material3.OutlinedTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (ReportType, String) -> Unit
) {
    if (!showDialog) return

    var selectedType by remember { mutableStateOf(ReportType.MISINFORMATION) }
    var description by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إبلاغ عن محتوى")
            }
        },
        text = {
            Column {
                Text("يرجى تحديد سبب الإبلاغ بدقة. سيتم مراجعة بلاغك بسرية تامة دون كشف هويتك للطرف الآخر.", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(16.dp))
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedType.titleArabic,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("نوع المخالفة") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ReportType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.titleArabic) },
                                onClick = {
                                    selectedType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("تفاصيل إضافية (اختياري)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    onSubmit(selectedType, description)
                    onDismiss()
                }
            ) {
                Text("إرسال البلاغ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.cancel))
            }
        }
    )
}
