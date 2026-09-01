package com.siraj.app.features.tasbih.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihScreen(
    onNavigateBack: () -> Unit,
    viewModel: TasbihViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("المسبحة") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.reset() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "إعادة تعيين")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // اختيار الذكر
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.dhikrList) { dhikr ->
                    FilterChip(
                        selected = dhikr.id == state.selectedDhikr.id,
                        onClick = { viewModel.selectDhikr(dhikr) },
                        label = { Text(dhikr.text) },
                    )
                }
            }

            // نص الذكر
            Text(
                text = state.selectedDhikr.arabicText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = state.selectedDhikr.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // العداد الدائري
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                TasbihCounterButton(
                    count = state.count,
                    target = state.target,
                    isCompleted = state.isCompleted,
                    onClick = { viewModel.increment(context) },
                )
            }

            // الإحصائيات
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatItem(label = "الجلسة", value = "${state.count}/${state.target}")
                StatItem(label = "إجمالي اليوم", value = state.dailyTotal.toString())
                StatItem(label = "إجمالي التسبيح", value = state.totalDhikr.toString())
            }
        }
    }
}

@Composable
private fun TasbihCounterButton(
    count: Int,
    target: Int,
    isCompleted: Boolean,
    onClick: () -> Unit,
) {
    val progress = if (target > 0) count.toFloat() / target.toFloat() else 0f
    val animatedScale by animateFloatAsState(
        targetValue = if (isCompleted) 1.1f else 1.0f,
        animationSpec = tween(300),
        label = "scale",
    )
    val animatedColor by animateColorAsState(
        targetValue = if (isCompleted)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.primaryContainer,
        animationSpec = tween(300),
        label = "color",
    )

    Box(
        modifier = Modifier
            .size(220.dp)
            .scale(animatedScale)
            .clip(CircleShape)
            .background(animatedColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = if (isCompleted)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = "/ $target",
                style = MaterialTheme.typography.titleMedium,
                color = if (isCompleted)
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                else
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            )
            if (isCompleted) {
                Text(
                    text = "تم بحمد الله",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
