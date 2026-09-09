package com.siraj.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.siraj.app.domain.models.*
import com.siraj.app.ui.theme.statusColors

// Siraj Techno-Spiritual — Purple Card Accents
private val CardPurpleGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF7C3AED).copy(alpha = 0.06f), Color(0xFFA78BFA).copy(alpha = 0.02f))
)
private val CardPurpleAccent = Color(0xFF7C3AED)

@Composable
fun SirajProjectCard(
    project: ProjectPreview,
    modifier: Modifier = Modifier,
) {
    SirajCard(modifier = modifier) {
        Column {
            Text(
                text = project.title,
                style = MaterialTheme.typography.titleMedium,
                color = CardPurpleAccent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = project.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "آخر تعديل: ${project.lastModified}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
fun SirajVideoCard(
    video: VideoPreview,
    modifier: Modifier = Modifier,
) {
    SirajCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier =
                    Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF4C1D95), Color(0xFF3B1A7E))
                            )
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "تشغيل",
                    tint = Color(0xFFA78BFA),
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "المدة: ${video.duration}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun SirajAudioCard(
    audio: AudioItem,
    modifier: Modifier = Modifier,
) {
    SirajCard(modifier = modifier) {
        Column {
            Text(
                text = audio.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = audio.reciter,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = audio.duration,
                    style = MaterialTheme.typography.labelSmall,
                    color = CardPurpleAccent,
                )
            }
        }
    }
}

@Composable
fun SirajSourceCard(
    source: SourcePreview,
    modifier: Modifier = Modifier,
) {
    SirajCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = source.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = source.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            VerificationStatusBadge(status = source.verificationStatus)
        }
    }
}

@Composable
fun VerificationStatusBadge(
    status: VerificationStatus,
    modifier: Modifier = Modifier,
) {
    val statusColors = MaterialTheme.statusColors
    val (backgroundColor, textColor) =
        when (status) {
            VerificationStatus.VERIFIED -> statusColors.successBg to statusColors.successFg
            VerificationStatus.PENDING -> statusColors.warningBg to statusColors.warningFg
            VerificationStatus.REJECTED -> statusColors.errorBg to statusColors.errorFg
        }

    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(8.dp))
                .background(backgroundColor)
                .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
        )
    }
}

@Composable
fun SirajFlashCard(
    flash: FlashItem,
    modifier: Modifier = Modifier,
) {
    SirajCard(modifier = modifier) {
        Column {
            Text(
                text = flash.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = flash.author,
                    style = MaterialTheme.typography.labelMedium,
                    color = CardPurpleAccent,
                )
                Text(
                    text = flash.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
