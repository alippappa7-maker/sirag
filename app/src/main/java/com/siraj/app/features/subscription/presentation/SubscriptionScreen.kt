package com.siraj.app.features.subscription.presentation

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.billingclient.api.ProductDetails
import com.siraj.app.domain.models.subscription.BillingInterval
import com.siraj.app.domain.models.subscription.Plan
import com.siraj.app.domain.models.subscription.UsageLimit

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
                title = { Text("باقات سراج") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = androidx.compose.ui.res.stringResource(com.siraj.app.R.string.back))
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.restorePurchases() }) {
                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("استعادة المشتريات")
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
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                
                // Header
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ارتقِ بإنتاجك الإسلامي",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "اختر الباقة التي تناسب احتياجاتك. القرآن الكريم ومواقيت الصلاة ستظل دائماً مجانية.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                state.error?.let { errorMsg ->
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                            Text(errorMsg, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }

                if (!state.isBillingConnected) {
                    item {
                        Text(
                            text = "جاري الاتصال بمتجر جوجل بلاي لعرض الأسعار المحلية...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Current Status & Limits if Active
                item {
                    if (state.subscription != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("باقتك الحالية", style = MaterialTheme.typography.labelMedium)
                                        Text(
                                            state.availablePlans.find { it.id == state.subscription?.planId }?.name ?: "غير معروف",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Badge(containerColor = MaterialTheme.colorScheme.primary) { 
                                        Text(state.subscription?.status?.name ?: "")
                                    }
                                }
                                
                                state.entitlement?.let { entitlement ->
                                    Spacer(modifier = Modifier.height(16.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("استهلاك الذكاء الاصطناعي (يتجدد شهرياً)", style = MaterialTheme.typography.titleSmall)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    entitlement.limits.forEach { limit ->
                                        UsageLimitItem(limit)
                                    }
                                }
                            }
                        }
                    }
                }

                // Plans
                val freePlan = state.availablePlans.find { it.price == 0.0 }
                val proMonthly = state.availablePlans.find { it.id == "plan_pro_monthly" }
                val proYearly = state.availablePlans.find { it.id == "plan_pro_yearly" }
                val enterprise = state.availablePlans.find { it.id == "plan_enterprise" }

                // Free Plan
                freePlan?.let { plan ->
                    item {
                        PlanCard(
                            plan = plan,
                            storeProduct = null,
                            isCurrentPlan = state.subscription?.planId == plan.id,
                            onPurchaseClick = {}
                        )
                    }
                }

                // Pro Plans
                if (proMonthly != null || proYearly != null) {
                    item {
                        Text("الخطط الاحترافية", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
                
                proMonthly?.let { plan ->
                    item {
                        PlanCard(
                            plan = plan,
                            storeProduct = state.storeProducts.find { it.productId == plan.platformProductIds["android"] },
                            isCurrentPlan = state.subscription?.planId == plan.id,
                            onPurchaseClick = {
                                if (context is Activity) viewModel.initiatePurchase(context, plan)
                            }
                        )
                    }
                }

                proYearly?.let { plan ->
                    item {
                        PlanCard(
                            plan = plan,
                            storeProduct = state.storeProducts.find { it.productId == plan.platformProductIds["android"] },
                            isCurrentPlan = state.subscription?.planId == plan.id,
                            isPopular = true,
                            onPurchaseClick = {
                                if (context is Activity) viewModel.initiatePurchase(context, plan)
                            }
                        )
                    }
                }

                // Enterprise
                enterprise?.let { plan ->
                    item {
                        Text("للمؤسسات", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    item {
                        PlanCard(
                            plan = plan,
                            storeProduct = state.storeProducts.find { it.productId == plan.platformProductIds["android"] },
                            isCurrentPlan = state.subscription?.planId == plan.id,
                            onPurchaseClick = {
                                if (context is Activity) viewModel.initiatePurchase(context, plan)
                            }
                        )
                    }
                }

                // Info / Terms
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("معلومات هامة حول الاشتراك", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "• يتم تجديد الاشتراك تلقائياً ما لم يتم الإلغاء قبل 24 ساعة من نهاية الفترة الحالية.\n" +
                                    "• سيتم الخصم من حسابك في متجر Play عند تأكيد الشراء.\n" +
                                    "• يمكنك إدارة اشتراكك أو إلغاؤه في أي وقت من إعدادات حساب Google Play.\n" +
                                    "• الأسعار المعروضة قد تختلف حسب دولتك وتتضمن الضرائب المطبقة.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun UsageLimitItem(limit: UsageLimit) {
    val title = when (limit.featureKey) {
        "AI_IMAGE_GENERATION" -> "توليد الصور بالذكاء الاصطناعي"
        "AUDIO_GENERATION" -> "التوليد الصوتي"
        else -> limit.featureKey
    }
    
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text("${limit.currentUsage} / ${if(limit.maxLimit > 0) limit.maxLimit else "غير محدود"}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
        if (limit.maxLimit > 0) {
            val progress = (limit.currentUsage.toFloat() / limit.maxLimit).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (progress >= 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun PlanCard(
    plan: Plan,
    storeProduct: ProductDetails?,
    isCurrentPlan: Boolean,
    isPopular: Boolean = false,
    onPurchaseClick: () -> Unit
) {
    val displayPrice = if (plan.price == 0.0) {
        "مجاناً"
    } else if (storeProduct != null) {
        storeProduct.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice ?: "${plan.price} ${plan.currency}"
    } else {
        "${plan.price} ${plan.currency}"
    }

    val period = when (plan.interval) {
        BillingInterval.MONTHLY -> "شهرياً"
        BillingInterval.YEARLY -> "سنوياً"
        else -> ""
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        border = if (isCurrentPlan || isPopular) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            if (isPopular && !isCurrentPlan) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) { 
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("الأكثر توفيراً", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(plan.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (isCurrentPlan) {
                    Badge(containerColor = MaterialTheme.colorScheme.primary) { Text("الخطة الحالية", modifier = Modifier.padding(4.dp)) }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.Bottom) {
                Text(displayPrice, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                if (plan.price > 0.0) {
                    Text(" / $period", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            Text(plan.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            
            plan.features.forEach { feature ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(feature, style = MaterialTheme.typography.bodyMedium)
                }
            }
            
            if (!isCurrentPlan) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onPurchaseClick, 
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = plan.price > 0.0 // Free plan cannot be "purchased" again
                ) {
                    Text(if (plan.price == 0.0) "أنت على هذه الخطة" else "اختر الباقة", style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}
