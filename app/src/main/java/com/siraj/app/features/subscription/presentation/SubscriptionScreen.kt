package com.siraj.app.features.subscription.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.siraj.app.domain.models.subscription.Plan
import com.siraj.app.domain.models.subscription.UsageLimit

import android.app.Activity
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    viewModel: SubscriptionViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الاشتراكات والباقات") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                state.error?.let { errorMsg ->
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                            Text(errorMsg, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
                
                if (!state.isBillingConnected) {
                    item {
                        Text("جاري الاتصال بمتجر جوجل بلاي...", color = Color.Gray)
                    }
                }
                // Current Plan & Status
                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("حالة الاشتراك الحالي", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("الخطة: ${state.subscription?.planId ?: "لا توجد خطة فعالة"}", fontWeight = FontWeight.Bold)
                            Text("الحالة: ${state.subscription?.status?.name ?: "غير معروف"}")
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(onClick = { viewModel.restorePurchases() }) {
                                Text("استعادة المشتريات")
                            }
                        }
                    }
                }

                // Credit Balance
                item {
                    state.balance?.let { balance ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("الرصيد المتاح", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("${balance.availableCredits} نقطة", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                                Text("تم استهلاك ${balance.totalUsed} من أصل ${balance.totalPurchased}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // Entitlement Limits
                item {
                    state.entitlement?.let { entitlement ->
                        Text("الحدود المتاحة", style = MaterialTheme.typography.titleMedium)
                        entitlement.limits.forEach { limit ->
                            UsageLimitItem(limit)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("ترقية الخطة", style = MaterialTheme.typography.titleLarge)
                }

                // Available Plans
                items(state.availablePlans) { plan ->
                    PlanCard(
                        plan = plan,
                        isCurrentPlan = state.subscription?.planId == plan.id,
                        onPurchaseClick = {
                            if (context is Activity) {
                                viewModel.initiatePurchase(context, plan)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun UsageLimitItem(limit: UsageLimit) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(limit.featureKey)
            Text("${limit.currentUsage} / ${limit.maxLimit}")
        }
        LinearProgressIndicator(
            progress = if (limit.maxLimit > 0) limit.currentUsage.toFloat() / limit.maxLimit else 0f,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        )
    }
}

@Composable
fun PlanCard(plan: Plan, isCurrentPlan: Boolean, onPurchaseClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = if (isCurrentPlan) CardDefaults.outlinedCardBorder() else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(plan.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (isCurrentPlan) {
                    Badge(containerColor = MaterialTheme.colorScheme.primary) { Text("الخطة الحالية") }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("${plan.price} ${plan.currency} / ${plan.interval.name}", style = MaterialTheme.typography.bodyLarge)
            Text(plan.description, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(8.dp))
            plan.features.take(3).forEach { feature ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(feature, style = MaterialTheme.typography.bodySmall)
                }
            }
            
            if (!isCurrentPlan) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onPurchaseClick, modifier = Modifier.fillMaxWidth()) {
                    Text("اختيار الخطة")
                }
            }
        }
    }
}
