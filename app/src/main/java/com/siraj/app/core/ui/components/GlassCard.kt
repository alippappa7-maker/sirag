package com.siraj.app.core.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * بطاقة زجاجية شفافة (Glassmorphism)
 * خلفية ضبابية شفافة مع حدود رفيعة
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(24.dp)
    val baseModifier = Modifier
        .then(modifier)
        .clip(shape)
        .background(
            Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.15f),
                    Color.White.copy(alpha = 0.05f),
                ),
            ),
        )
        .border(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.2f),
            shape = shape,
        )
        .shadow(elevation = 0.dp)

    if (onClick != null) {
        Box(modifier = baseModifier.clickable(onClick = onClick), contentAlignment = Alignment.Center) {
            content()
        }
    } else {
        Box(modifier = baseModifier, contentAlignment = Alignment.Center) {
            content()
        }
    }
}

/**
 * بطاقة زجاجية متوهجة
 * تومض عند النشاط
 */
@Composable
fun GlowingGlassCard(
    isActive: Boolean = false,
    glowColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val pulseAlpha by animateFloatAsState(
        targetValue = if (isActive) 0.4f else 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow",
    )

    val shape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.05f),
                    ),
                ),
            )
            .border(
                width = 1.dp,
                color = if (isActive) glowColor.copy(alpha = pulseAlpha) else Color.White.copy(alpha = 0.2f),
                shape = shape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/**
 * بطاقة إحصائية زجاجية
 */
@Composable
fun GlassStatCard(
    title: String,
    value: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null,
) {
    val animatedValue by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_anim",
    )

    GlassCard(
        modifier = modifier.height(110.dp),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
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
                icon()
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor,
            )
        }
    }
}
