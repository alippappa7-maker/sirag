package com.siraj.app.features.subscription.presentation.billing

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.siraj.app.domain.models.subscription.CreditTransaction
import com.siraj.app.domain.models.subscription.SubscriptionStatus
import com.siraj.app.domain.models.subscription.TransactionType
import com.siraj.app.features.subscription.presentation.SubscriptionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageAndBillingScreen(
    viewModel: SubscriptionViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPlans: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الاستخدام والفوترة") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = androidx.compose.ui.res.stringResource(com.siraj.app.R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading && state.subscription == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Subscription Info
                item {
                    val currentPlanName = state.availablePlans.find { it.id == state.subscription?.planId }?.name ?: "الخطة المجانية"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("الخطة الحالية", style = MaterialTheme.typography.labelMedium)
                            Text(currentPlanName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val sub = state.subscription
                            if (sub != null && sub.status == SubscriptionStatus.ACTIVE) {
                                val date = sub.renewsAt?.let { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(it)) } ?: "غير معروف"
                                Text("تتجدد في: $date", style = MaterialTheme.typography.bodyMedium)
                            } else if (sub != null && sub.status == SubscriptionStatus.CANCELLED) {
                                val date = sub.expiresAt?.let { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(it)) } ?: "غير معروف"
                                Text("تنتهي في: $date (تم الإلغاء)", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = onNavigateToPlans, modifier = Modifier.weight(1f)) {
                                    Text("تغيير الخطة")
                                }
                                
                                OutlinedButton(
                                    onClick = {
                                        // Open Google Play Subscriptions
                                        val url = if (sub?.productId != null && sub.productId != "free") {
                                            "https://play.google.com/store/account/subscriptions?sku=${sub.productId}&package=${context.packageName}"
                                        } else {
                                            "https://play.google.com/store/account/subscriptions"
                                        }
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("إدارة في المتجر")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                            
                            TextButton(onClick = { viewModel.restorePurchases() }, modifier = Modifier.fillMaxWidth()) {
                                Text("استعادة المشتريات")
                            }
                        }
                    }
                }

                // Balance & Usage Alerts
                item {
                    state.balance?.let { balance ->
                        val usagePercent = if (balance.totalPurchased > 0) (balance.totalUsed.toFloat() / balance.totalPurchased) else 0f
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("رصيد العمليات الإضافية", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text("المتبقي", style = MaterialTheme.typography.labelSmall)
                                        Text("${balance.availableCredits} نقطة", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("المستهلك", style = MaterialTheme.typography.labelSmall)
                                        Text("${balance.totalUsed} نقطة", style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                                
                                if (usagePercent > 0.8f) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.errorContainer)
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "لقد استهلكت أكثر من 80% من رصيدك. يرجى الترقية لتجنب توقف الخدمات.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Highest Cost Operations
                item {
                    if (state.transactions.isNotEmpty()) {
                        val aggregated = state.transactions
                            .filter { it.type == TransactionType.DEBIT }
                            .groupBy { it.operationType }
                            .mapValues { entry -> entry.value.sumOf { it.amount } }
                            .entries.sortedByDescending { it.value }
                            .take(3)
                            
                        if (aggregated.isNotEmpty()) {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("أكثر العمليات استهلاكاً للرصيد", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    aggregated.forEach { (operation, totalAmount) ->
                                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(operation, style = MaterialTheme.typography.bodyMedium)
                                            Text("$totalAmount نقطة", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Transaction History Title
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                        Icon(Icons.Default.History, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("سجل المعاملات", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }

                // Transactions List
                if (state.transactions.isEmpty()) {
                    item {
                        Text("لا توجد معاملات سابقة.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                    }
                } else {
                    items(state.transactions) { tx ->
                        TransactionItem(tx)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(tx: CreditTransaction) {
    val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(tx.timestamp))
    val isCredit = tx.type == TransactionType.CREDIT || tx.type == TransactionType.BONUS
    val color = if (isCredit) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
    val sign = if (isCredit) "+" else "-"
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(tx.reason, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                if (tx.workspaceId != null) {
                    Text("مساحة عمل", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            Text(
                "$sign${tx.amount}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
