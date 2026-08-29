package com.siraj.app.features.studio.presentation.analytics

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.siraj.app.domain.models.analytics.AnalyticsTimeFilter
import com.siraj.app.domain.models.analytics.FlashAnalyticsSummary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorAnalyticsScreen(
    viewModel: CreatorAnalyticsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val timeFilter by viewModel.timeFilter.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تحليلات الأداء") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = androidx.compose.ui.res.stringResource(com.siraj.app.R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            val report = viewModel.generateExportReport()
                            if (report != null) {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, report)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "تصدير التقرير")
                                context.startActivity(shareIntent)
                            }
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "تصدير")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            
            // Time Filter Row
            ScrollableTabRow(
                selectedTabIndex = timeFilter.ordinal,
                edgePadding = 8.dp
            ) {
                AnalyticsTimeFilter.values().forEachIndexed { index, filter ->
                    Tab(
                        selected = timeFilter.ordinal == index,
                        onClick = { viewModel.setTimeFilter(filter) },
                        text = { Text(filter.displayName) }
                    )
                }
            }

            // Info Disclaimer
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = "تنبيه", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "تذكير: الأرقام هنا (مثل المشاهدات) تُستخدم لتقييم الأداء الفني فقط، ولا تعتبر معياراً على القيمة الشرعية أو صحة المحتوى. بعض الأرقام تقديرية.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            when (val state = uiState) {
                is CreatorAnalyticsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is CreatorAnalyticsUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is CreatorAnalyticsUiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            DashboardSummaryCards(state.dashboard)
                        }

                        item {
                            Text("أداء الومضات", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(state.dashboard.flashes) { flash ->
                            FlashAnalyticsCard(flash)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardSummaryCards(dashboard: com.siraj.app.domain.models.analytics.CreatorAnalyticsDashboard) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard(title = "المشاهدات", value = dashboard.totalViews.toString(), modifier = Modifier.weight(1f))
            MetricCard(title = "نمو المتابعين", value = "+${dashboard.followerGrowth}", modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard(title = "مشاهدات فريدة (تقديري)", value = dashboard.estimatedUniqueViews.toString(), modifier = Modifier.weight(1f))
            MetricCard(title = "أفضل قوالب", value = dashboard.topPerformingTemplates.keys.firstOrNull() ?: "-", modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("أفضل أوقات النشر (تقديري):", style = MaterialTheme.typography.labelMedium)
        Text(dashboard.bestPostingTimes.joinToString("، "), style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    OutlinedCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FlashAnalyticsCard(flash: FlashAnalyticsSummary) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            Text(flash.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("تاريخ النشر: ${sdf.format(Date(flash.publishedAt))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("المشاهدات", style = MaterialTheme.typography.labelSmall)
                    Text(flash.views.toString(), style = MaterialTheme.typography.bodyLarge)
                }
                Column {
                    Text("نسبة الإكمال", style = MaterialTheme.typography.labelSmall)
                    Text("${flash.completionRatePercentage}%", style = MaterialTheme.typography.bodyLarge)
                }
                Column {
                    Text("متوسط المشاهدة", style = MaterialTheme.typography.labelSmall)
                    Text("${flash.averageWatchTimeSeconds}ث", style = MaterialTheme.typography.bodyLarge)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Text("إعجابات: ${flash.likes}", style = MaterialTheme.typography.bodySmall)
                Text("حفظ: ${flash.saves}", style = MaterialTheme.typography.bodySmall)
                Text("مشاركة: ${flash.shares}", style = MaterialTheme.typography.bodySmall)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text("أبرز مصادر الوصول:", style = MaterialTheme.typography.labelSmall)
            flash.trafficSources.entries.take(2).forEach { (source, pct) ->
                Text("- $source: ${pct}%", style = MaterialTheme.typography.bodySmall)
            }
            
            if (flash.topCountries != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("توزع جغرافي (مجمع):", style = MaterialTheme.typography.labelSmall)
                flash.topCountries.entries.take(2).forEach { (country, pct) ->
                    Text("- $country: ${pct}%", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
