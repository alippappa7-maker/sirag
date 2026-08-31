package com.siraj.app.features.review.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siraj.app.domain.models.review.*
import java.text.SimpleDateFormat
import java.util.*
import com.siraj.app.ui.theme.statusColors

@Composable
fun RiskBadge(riskLevel: RiskLevel, modifier: Modifier = Modifier) {
    val (bgColor, textColor, icon) = when (riskLevel) {
        RiskLevel.LOW -> Triple(
            MaterialTheme.statusColors.successBg,
            MaterialTheme.statusColors.successFg,
            Icons.Default.CheckCircle
        )
        RiskLevel.MEDIUM -> Triple(
            MaterialTheme.statusColors.warningBg,
            MaterialTheme.statusColors.warningFg,
            Icons.Default.Info
        )
        RiskLevel.HIGH -> Triple(
            Color(0xFFFFE0B2),
            MaterialTheme.statusColors.warningFg,
            Icons.Default.Warning
        )
        RiskLevel.CRITICAL -> Triple(
            MaterialTheme.statusColors.errorBg,
            MaterialTheme.statusColors.errorFg,
            Icons.Default.ReportProblem
        )
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = riskLevel.arabicTitle,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatusBadge(status: ShariaReviewStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, icon) = when (status) {
        ShariaReviewStatus.PENDING -> Triple(MaterialTheme.statusColors.neutralBg, Color(0xFF455A64), Icons.Default.HourglassEmpty)
        ShariaReviewStatus.IN_REVIEW -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), Icons.Default.RateReview)
        ShariaReviewStatus.CHANGES_REQUESTED -> Triple(MaterialTheme.statusColors.warningBg, MaterialTheme.statusColors.warningFg, Icons.Default.EditNote)
        ShariaReviewStatus.ESCALATED_SECOND_REVIEW -> Triple(Color(0xFFF3E5F5), Color(0xFF7B1FA2), Icons.Default.SupervisorAccount)
        ShariaReviewStatus.DUAL_APPROVAL_PENDING -> Triple(MaterialTheme.statusColors.draftBg, MaterialTheme.statusColors.draftFg, Icons.Default.HowToReg)
        ShariaReviewStatus.APPROVED -> Triple(MaterialTheme.statusColors.successBg, MaterialTheme.statusColors.successFg, Icons.Default.CheckCircle)
        ShariaReviewStatus.REJECTED -> Triple(MaterialTheme.statusColors.errorBg, MaterialTheme.statusColors.errorFg, Icons.Default.Cancel)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status.arabicTitle,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun CriticalTopicChip(topic: CriticalTopic, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = topic.arabicTitle,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ReviewQueueItemCard(
    item: ShariaReviewItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("ar"))
    val formattedDate = dateFormat.format(Date(item.submittedAt))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("review_queue_card_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Status and Risk Level
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(status = item.status)
                RiskBadge(riskLevel = item.riskLevel)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = item.contentTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Snippet
            Text(
                text = item.fullContentText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Critical Topics Tags if any
            if (item.criticalTopics.any { it != CriticalTopic.NONE }) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item.criticalTopics.filter { it != CriticalTopic.NONE }.forEach { topic ->
                        CriticalTopicChip(topic = topic)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Footer Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.creatorName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "• ${item.category}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$formattedDate (${item.claims.size} مراجع موثقة)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "فحص وتدقيق",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
