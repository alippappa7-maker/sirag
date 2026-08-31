package com.siraj.app.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SystemStatusBanner(
    isMaintenanceMode: Boolean,
    isReadOnlyMode: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = isMaintenanceMode || isReadOnlyMode,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier,
    ) {
        val backgroundColor = if (isMaintenanceMode) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.tertiaryContainer
        val contentColor = if (isMaintenanceMode) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer
        val title = if (isMaintenanceMode) "وضع الصيانة الشامل" else "وضع القراءة فقط"
        val description = if (isMaintenanceMode) "النظام حالياً في وضع الصيانة المغلق. بعض الخدمات قد تكون غير متاحة أو معطلة تماماً." else "نظام الإنتاج والتوليد معلق حالياً لأغراض الطوارئ، يمكنك فقط استعراض البيانات والمشاريع."

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.WarningAmber,
                contentDescription = "تحذير النظام",
                tint = contentColor,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = contentColor,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor,
                    textAlign = TextAlign.Start,
                )
            }
        }
    }
}
