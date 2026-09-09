package com.siraj.app.core.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.animation.animateContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.siraj.app.ui.theme.LocalSpacing
import com.siraj.app.ui.theme.MyApplicationTheme
import com.siraj.app.ui.theme.SirajGlow
import com.siraj.app.ui.theme.extendedColors

// Siraj Techno-Spiritual — Purple Glow Enhancements

@Composable
fun SirajGlowContainer(
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    glowColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit,
) {
    val borderWidth by animateDpAsState(targetValue = if (isActive) 1.5.dp else 0.dp, label = "borderWidth")
    val borderAlpha by animateFloatAsState(targetValue = if (isActive) 0.6f else 0.1f, label = "borderAlpha")
    val elevation by animateDpAsState(targetValue = if (isActive) LocalSpacing.current.elevations.medium else 0.dp, label = "elevation")

    val surfaceBackground = if (isActive) {
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                MaterialTheme.colorScheme.surface,
            ),
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surface,
            ),
        )
    }

    Box(
        modifier =
            modifier
                .shadow(
                    elevation = elevation,
                    shape = MaterialTheme.shapes.medium,
                    ambientColor = glowColor.copy(alpha = if (isActive) 0.3f else 0f),
                    spotColor = glowColor.copy(alpha = if (isActive) 0.2f else 0f),
                ).border(
                    width = borderWidth,
                    color = glowColor.copy(alpha = borderAlpha),
                    shape = MaterialTheme.shapes.medium,
                ).clip(MaterialTheme.shapes.medium)
                .background(surfaceBackground),
    ) {
        content()
    }
}

@Composable
fun SirajTechCard(
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val spacing = LocalSpacing.current
    SirajGlowContainer(
        modifier = modifier,
        isActive = isActive,
        glowColor = MaterialTheme.colorScheme.primary,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                    .padding(spacing.medium),
        ) {
            content()
        }
    }
}

@Composable
fun SirajAiStatusChip(
    text: String,
    modifier: Modifier = Modifier,
    isProcessing: Boolean = false,
) {
    val extendedColors = MaterialTheme.extendedColors
    val bgColor = if (isProcessing) extendedColors.processing.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isProcessing) extendedColors.processing else MaterialTheme.colorScheme.onSurfaceVariant
    val icon = if (isProcessing) Icons.Default.AutoAwesome else Icons.Default.CheckCircle

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = bgColor,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.25f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = LocalSpacing.current.small, vertical = LocalSpacing.current.extraSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LocalSpacing.current.extraSmall),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = contentColor,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
            )
        }
    }
}

@Composable
fun SirajProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    LinearProgressIndicator(
        progress = { progress },
        modifier =
            modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(MaterialTheme.shapes.small),
        color = color,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}

@Composable
fun SirajSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
fun SirajPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun SirajSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelMedium)
    }
}

enum class StatusType {
    SUCCESS, WARNING, ERROR, INFO, NEUTRAL
}

@Composable
fun SirajStatusBadge(
    text: String,
    statusType: StatusType,
    modifier: Modifier = Modifier,
) {
    val statusColors = MaterialTheme.extendedColors
    val (bgColor, fgColor) =
        when (statusType) {
            StatusType.SUCCESS -> statusColors.success.copy(alpha = 0.12f) to statusColors.success
            StatusType.WARNING -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
            StatusType.ERROR -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f) to MaterialTheme.colorScheme.error
            StatusType.INFO -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f) to MaterialTheme.colorScheme.tertiary
            StatusType.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraSmall,
        color = bgColor,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = fgColor,
        )
    }
}

@Composable
fun SirajMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector? = null,
    trend: String? = null,
    isPositiveTrend: Boolean = true,
) {
    val spacing = LocalSpacing.current
    SirajTechCard(
        modifier = modifier,
        isActive = false,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (trend != null) {
                val trendColor = if (isPositiveTrend) MaterialTheme.extendedColors.success else MaterialTheme.colorScheme.error
                Text(
                    text = trend,
                    style = MaterialTheme.typography.labelSmall,
                    color = trendColor,
                )
            }
        }
    }
}

// ==========================================
// Previews for testing the Techno-Spiritual UI
// ==========================================

@Preview(showBackground = true, locale = "ar")
@Composable
fun SirajTechComponentsPreview() {
    MyApplicationTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier =
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SirajSectionHeader(title = "تحليل البيانات", actionText = "عرض الكل", onActionClick = {})

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SirajPrimaryButton(text = "معالجة", onClick = {})
                    SirajSecondaryButton(text = "إلغاء", onClick = {})
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SirajAiStatusChip(text = "جاري التحليل...", isProcessing = true)
                    SirajAiStatusChip(text = "مكتمل", isProcessing = false)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SirajStatusBadge(text = "نشط", statusType = StatusType.SUCCESS)
                    SirajStatusBadge(text = "مراجعة", statusType = StatusType.WARNING)
                    SirajStatusBadge(text = "فشل", statusType = StatusType.ERROR)
                    SirajStatusBadge(text = "معالجة", statusType = StatusType.INFO)
                }

                SirajProgressIndicator(progress = 0.65f)

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SirajMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "الإنتاجية",
                        value = "85%",
                        icon = Icons.Default.Analytics,
                        trend = "+5% هذا الأسبوع",
                        isPositiveTrend = true,
                    )
                    SirajMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "الأخطاء",
                        value = "12",
                        icon = Icons.Default.Analytics,
                        trend = "-2% هذا الأسبوع",
                        isPositiveTrend = false,
                    )
                }

                SirajTechCard(isActive = true) {
                    Text(text = "بطاقة تقنية مفعلة", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "تستخدم هذه البطاقة الحدود الدقيقة والتوهج البنفسجي المتوافق مع الهوية.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                SirajTechCard(isActive = false) {
                    Text(text = "بطاقة تقنية عادية", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "بطاقة غير مفعلة تستخدم ألوان السطح القياسية.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
