package com.siraj.app.features.minor.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.siraj.app.domain.models.minor.*
import com.siraj.app.ui.theme.statusColors

@Composable
fun AgeBracketBadge(
    ageBracket: UserAgeBracket,
    modifier: Modifier = Modifier
) {
    val (bg, textColor, icon) = when (ageBracket) {
        UserAgeBracket.ADULT_18_PLUS -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            Icons.Default.Person
        )
        UserAgeBracket.TEEN_13_TO_17 -> Triple(
            MaterialTheme.statusColors.infoBg,
            MaterialTheme.statusColors.infoFg,
            Icons.Default.School
        )
        UserAgeBracket.CHILD_UNDER_13 -> Triple(
            MaterialTheme.statusColors.warningBg,
            MaterialTheme.statusColors.warningFg,
            Icons.Default.ChildCare
        )
        UserAgeBracket.UNSPECIFIED -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Default.Security
        )
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.testTag("badge_age_${ageBracket.code}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = ageBracket.titleArabic,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = ageBracket.titleArabic,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
fun SafetyGuardrailRow(
    title: String,
    description: String,
    isActive: Boolean,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                color = if (isActive) MaterialTheme.statusColors.successBg else MaterialTheme.statusColors.errorBg,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isActive) "مفعل ومحمٍ" else "غير مقيد",
                    color = if (isActive) MaterialTheme.statusColors.successFg else MaterialTheme.statusColors.errorFg,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ParentalConsentCard(
    consent: ParentalConsentRecord,
    onVerifyClick: (String) -> Unit,
    onRevokeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_consent_${consent.consentId}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ولي الأمر: ${consent.guardianName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = consent.guardianEmail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = when (consent.status) {
                        ParentalConsentStatus.APPROVED_VERIFIED -> MaterialTheme.statusColors.successBg
                        ParentalConsentStatus.PENDING_VERIFICATION -> MaterialTheme.statusColors.warningBg
                        ParentalConsentStatus.REJECTED, ParentalConsentStatus.REVOKED -> MaterialTheme.statusColors.errorBg
                        ParentalConsentStatus.NOT_REQUESTED -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = consent.status.titleArabic,
                        color = when (consent.status) {
                            ParentalConsentStatus.APPROVED_VERIFIED -> MaterialTheme.statusColors.successFg
                            ParentalConsentStatus.PENDING_VERIFICATION -> MaterialTheme.statusColors.warningFg
                            ParentalConsentStatus.REJECTED, ParentalConsentStatus.REVOKED -> MaterialTheme.statusColors.errorFg
                            ParentalConsentStatus.NOT_REQUESTED -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = consent.legalNotice,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (consent.status == ParentalConsentStatus.PENDING_VERIFICATION) {
                    Button(
                        onClick = { onVerifyClick(consent.consentId) },
                        modifier = Modifier.testTag("btn_verify_consent_${consent.consentId}")
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إدخال رمز التأكيد (OTP)")
                    }
                } else if (consent.status == ParentalConsentStatus.APPROVED_VERIFIED) {
                    OutlinedButton(
                        onClick = { onRevokeClick(consent.consentId) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("btn_revoke_consent_${consent.consentId}")
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("سحب الموافقة")
                    }
                }
            }
        }
    }
}
