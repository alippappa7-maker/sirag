package com.siraj.app.features.dashboard.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.core.ui.components.GlassCard
import com.siraj.app.core.ui.components.GlassStatCard
import com.siraj.app.core.ui.components.GlowingGlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToCopilot: () -> Unit = {},
    onNavigateToQibla: () -> Unit = {},
    onNavigateToTasbih: () -> Unit = {},
    onNavigateToPrayerIntel: () -> Unit = {},
    onNavigateToZakat: () -> Unit = {},
    onNavigateToTafsir: () -> Unit = {},
    onNavigateToHadith: () -> Unit = {},
    onNavigateToRamadan: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    toggleTheme: () -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            ),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 1. رأس تقني حي
        item { TechHeader(onNavigateToSettings, toggleTheme) }

        // 2. بطاقة الصلاة القادمة — متوهجة
        item { NextPrayerGlowCard(onNavigateToPrayerIntel) }

        // 3. شبكة الإحصائيات الزجاجية
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GlassStatCard(
                    title = "تسبيح اليوم",
                    value = "127",
                    icon = { Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToTasbih,
                )
                GlassStatCard(
                    title = "الصلوات",
                    value = "4/5",
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.weight(1f),
                    accentColor = MaterialTheme.colorScheme.secondary,
                    onClick = onNavigateToPrayerIntel,
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GlassStatCard(
                    title = "الاتجاه",
                    value = "293°",
                    icon = { Icon(Icons.Default.Explore, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToQibla,
                )
                GlassStatCard(
                    title = "الزكاة",
                    value = "2.5%",
                    icon = { Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.weight(1f),
                    accentColor = MaterialTheme.colorScheme.secondary,
                    onClick = onNavigateToZakat,
                )
            }
        }

        // 4. المساعد الذكي — بطاقة كبيرة بارزة
        item {
            GlowingGlassCard(
                isActive = true,
                glowColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("المساعد الإسلامي", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text("اسألني أي شيء", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        IconButton(onClick = onNavigateToCopilot) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "فتح", tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }

        // 5. اختصارات سريعة — صف زجاجي
        item {
            val primaryColor = MaterialTheme.colorScheme.primary
            val secondaryColor = MaterialTheme.colorScheme.secondary
            val tertiaryColor = MaterialTheme.colorScheme.tertiary
            val shortcuts = listOf(
                ShortcutData("القبلة", Icons.Default.Explore, primaryColor, onNavigateToQibla),
                ShortcutData("التسبيح", Icons.Default.TouchApp, secondaryColor, onNavigateToTasbih),
                ShortcutData("التفسير", Icons.Default.MenuBook, tertiaryColor, onNavigateToTafsir),
                ShortcutData("الحديث", Icons.Default.FormatQuote, primaryColor, onNavigateToHadith),
                ShortcutData("رمضان", Icons.Default.Nightlight, secondaryColor, onNavigateToRamadan),
                ShortcutData("الزكاة", Icons.Default.Calculate, tertiaryColor, onNavigateToZakat),
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 0.dp),
            ) {
                items(shortcuts) { shortcut ->
                    GlassCard(
                        modifier = Modifier.size(width = 100.dp, height = 100.dp),
                        onClick = shortcut.onClick,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(8.dp),
                        ) {
                            Icon(shortcut.icon, contentDescription = null, tint = shortcut.color, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.height(6.dp))
                            Text(shortcut.title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TechHeader(
    onNavigateToSettings: () -> Unit,
    toggleTheme: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text("السلام عليكم", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("سراج 2.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row {
            IconButton(onClick = toggleTheme) { Text("🌓") }
            IconButton(onClick = onNavigateToSettings) {
                Icon(Icons.Default.Settings, contentDescription = "الإعدادات", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun NextPrayerGlowCard(
    onClick: () -> Unit,
) {
    val pulseAlpha by animateFloatAsState(
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "next_prayer_pulse",
    )

    GlowingGlassCard(
        isActive = true,
        glowColor = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("الصلاة القادمة", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text("العصر", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("بعد 2 ساعة و 15 دقيقة", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary.copy(alpha = pulseAlpha + 0.5f))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Mosque, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

private data class ShortcutData(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val onClick: () -> Unit,
)
