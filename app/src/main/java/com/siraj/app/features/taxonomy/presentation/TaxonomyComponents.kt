package com.siraj.app.features.taxonomy.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siraj.app.domain.models.taxonomy.*
import com.siraj.app.ui.theme.statusColors

/**
 * شارات ووسوم تصنيف المحتوى ومصدره للمستخدم والإدارة (PROMPT 088)
 */

@Composable
fun ContentOriginBadge(
    originType: ContentOriginType,
    isAiAssisted: Boolean = false,
    modifier: Modifier = Modifier
) {
    val (bg, textColor, icon) = when (originType) {
        ContentOriginType.SYSTEM_CONTENT -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Default.SettingsSuggest
        )
        ContentOriginType.EDITORIAL_CONTENT -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            Icons.Default.Verified
        )
        ContentOriginType.USER_GENERATED -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            Icons.Default.Person
        )
        ContentOriginType.AI_GENERATED -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            Icons.Default.SmartToy
        )
        ContentOriginType.LICENSED_EXTERNAL -> Triple(
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.onErrorContainer,
            Icons.Default.Copyright
        )
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.testTag("origin_badge_${originType.code}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = originType.titleArabic,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            if (isAiAssisted && originType != ContentOriginType.AI_GENERATED) {
                Text(
                    text = "(بمساعدة AI)",
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.8f),
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
fun ContentDisciplineBadge(
    disciplineType: ContentDisciplineType,
    modifier: Modifier = Modifier
) {
    val (bg, icon) = when (disciplineType) {
        ContentDisciplineType.QURAN_TEXT -> Pair(MaterialTheme.statusColors.successFg.copy(alpha = 0.15f), Icons.AutoMirrored.Filled.MenuBook)
        ContentDisciplineType.TAFSIR -> Pair(Color(0xFF00695C).copy(alpha = 0.15f), Icons.Default.AutoStories)
        ContentDisciplineType.HADITH -> Pair(Color(0xFF4E342E).copy(alpha = 0.15f), Icons.Default.BookmarkBorder)
        ContentDisciplineType.FIQH -> Pair(Color(0xFF283593).copy(alpha = 0.15f), Icons.Default.Balance)
        ContentDisciplineType.EDUCATIONAL -> Pair(MaterialTheme.statusColors.warningFg.copy(alpha = 0.15f), Icons.Default.School)
        ContentDisciplineType.GENERAL -> Pair(MaterialTheme.colorScheme.surfaceVariant, Icons.Default.Category)
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.testTag("discipline_badge_${disciplineType.code}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = disciplineType.titleArabic,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ContentRightsBadge(
    rightsStatus: TaxonomyRightsStatus,
    modifier: Modifier = Modifier
) {
    val (bg, color) = when (rightsStatus) {
        TaxonomyRightsStatus.PUBLIC_DOMAIN -> Pair(MaterialTheme.statusColors.successBg, MaterialTheme.statusColors.successFg)
        TaxonomyRightsStatus.SIRAJ_ORIGINAL -> Pair(Color(0xFFE3F2FD), Color(0xFF1565C0))
        TaxonomyRightsStatus.LICENSED_CC -> Pair(MaterialTheme.statusColors.warningBg, MaterialTheme.statusColors.warningFg)
        TaxonomyRightsStatus.LICENSED_COMMERCIAL -> Pair(MaterialTheme.statusColors.draftBg, MaterialTheme.statusColors.draftFg)
        TaxonomyRightsStatus.RESTRICTED -> Pair(MaterialTheme.statusColors.errorBg, MaterialTheme.statusColors.errorFg)
        TaxonomyRightsStatus.UNKNOWN -> Pair(MaterialTheme.statusColors.errorBg, Color(0xFFB71C1C))
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier.testTag("rights_badge_${rightsStatus.code}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = if (rightsStatus.isPublicDomainOrLicensed) Icons.Default.LockOpen else Icons.Default.Lock,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = rightsStatus.titleArabic,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun LockedQuranIndicator(modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.statusColors.successFg.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.statusColors.successFg.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.fillMaxWidth().testTag("locked_quran_indicator")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.statusColors.successFg,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "نص قرآني محمي ومقفل (Immutable)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.statusColors.successFg
                )
                Text(
                    text = "مستورد من مجمع الملك فهد بالرسم العثماني. غير قابل للتحرير أو التعديل بأي شكل.",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.statusColors.successFg
                )
            }
        }
    }
}

@Composable
fun SourceProvenanceCard(
    metadata: ContentTaxonomyMetadata,
    modifier: Modifier = Modifier,
    onOpenUrl: ((String) -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth().testTag("source_provenance_card")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "بيانات المصدر والتوثيق (Provenance)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                ContentRightsBadge(rightsStatus = metadata.rightsStatus)
            }

            metadata.sourceTitle?.let { title ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "المصدر المعتمد:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            metadata.authorOrScholarName?.let { scholar ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "العالم / المؤلف:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = scholar,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            metadata.sourceReference?.let { ref ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "التخريج / المرجع:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = ref,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            metadata.licenseAttributionText?.let { attr ->
                Text(
                    text = "العزو والترخيص: $attr",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            if (!metadata.sourceUrl.isNullOrBlank() && onOpenUrl != null) {
                Button(
                    onClick = { onOpenUrl(metadata.sourceUrl) },
                    modifier = Modifier.align(Alignment.End).testTag("open_source_url_btn"),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "رابط المصدر", fontSize = 11.sp)
                }
            }
        }
    }
}
