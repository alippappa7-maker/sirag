package com.siraj.app.features.subscription.presentation

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.siraj.app.data.billing.GooglePlayBillingManager
import com.siraj.app.domain.models.subscription.CreditBalance
import com.siraj.app.domain.models.subscription.Entitlement
import com.siraj.app.domain.models.subscription.Plan
import com.siraj.app.domain.models.subscription.Subscription
import com.siraj.app.domain.repository.subscription.SubscriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SubscriptionState(
    val isLoading: Boolean = false,
    val subscription: Subscription? = null,
    val entitlement: Entitlement? = null,
    val balance: CreditBalance? = null,
    val availablePlans: List<Plan> = emptyList(),
    val storeProducts: List<ProductDetails> = emptyList(),
    val isBillingConnected: Boolean = false,
    val error: String? = null
)

class SubscriptionViewModel(
    private val repository: SubscriptionRepository,
    private val billingManager: GooglePlayBillingManager
) : ViewModel() {

    private val _state = MutableStateFlow(SubscriptionState(isLoading = true))
    val state: StateFlow<SubscriptionState> = _state.asStateFlow()

    init {
        loadSubscriptionData()
        setupBilling()
    }

    private fun loadSubscriptionData() {
        val userId = "current_user" // In real app, get from AuthRepository

        viewModelScope.launch {
            launch {
                repository.getCurrentSubscription(userId).collectLatest { sub ->
                    _state.update { it.copy(subscription = sub, isLoading = false) }
                }
            }
            launch {
                repository.getCurrentEntitlement(userId).collectLatest { ent ->
                    _state.update { it.copy(entitlement = ent) }
                }
            }
            launch {
                repository.getCreditBalance(userId).collectLatest { bal ->
                    _state.update { it.copy(balance = bal) }
                }
            }
            launch {
                repository.getAvailablePlans().collectLatest { plans ->
                    _state.update { it.copy(availablePlans = plans) }
                }
            }
        }
    }

    private fun setupBilling() {
        billingManager.startConnection(
            onSuccess = {
                _state.update { it.copy(isBillingConnected = true) }
                fetchStoreProducts()
                syncActivePurchases()
            },
            onError = {
                _state.update { it.copy(error = "فشل الاتصال بمتجر Google Play") }
            }
        )

        viewModelScope.launch {
            billingManager.purchaseUpdates.collectLatest { purchases ->
                if (purchases != null) {
                    for (purchase in purchases) {
                        if (purchase.purchaseState == com.android.billingclient.api.Purchase.PurchaseState.PURCHASED) {
                            verifyPurchaseOnServer(purchase)
                        } else if (purchase.purchaseState == com.android.billingclient.api.Purchase.PurchaseState.PENDING) {
                            _state.update { it.copy(error = "عملية الشراء قيد المعالجة (Pending).") }
                        }
                    }
                }
            }
        }
    }

    private fun fetchStoreProducts() {
        viewModelScope.launch {
            val productIds = listOf("siraj_pro_monthly", "siraj_pro_yearly")
            val products = billingManager.queryProductDetails(productIds)
            _state.update { it.copy(storeProducts = products) }
        }
    }

    private fun syncActivePurchases() {
        viewModelScope.launch {
            val activePurchases = billingManager.queryPurchases()
            activePurchases.forEach { purchase ->
                if (purchase.purchaseState == com.android.billingclient.api.Purchase.PurchaseState.PURCHASED) {
                    // Send to server to ensure our database is in sync with Google Play
                    verifyPurchaseOnServer(purchase)
                }
            }
        }
    }

    fun restorePurchases() {
        _state.update { it.copy(isLoading = true, error = "جاري استعادة المشتريات...") }
        syncActivePurchases()
    }

    fun initiatePurchase(activity: Activity, plan: Plan) {
        val platformId = plan.platformProductIds["android"]
        if (platformId != null) {
            val product = _state.value.storeProducts.find { it.productId == platformId }
            if (product != null) {
                // Determine if this is an upgrade/downgrade by checking if there is an active purchase
                var oldToken: String? = null
                viewModelScope.launch {
                    val activePurchases = billingManager.queryPurchases()
                    if (activePurchases.isNotEmpty()) {
                        oldToken = activePurchases.first().purchaseToken
                    }
                    val offerToken = product.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: ""
                    billingManager.launchBillingFlow(activity, product, offerToken, oldPurchaseToken = oldToken)
                }
            } else {
                _state.update { it.copy(error = "المنتج غير متوفر في المتجر حالياً") }
            }
        } else {
             _state.update { it.copy(error = "هذه الخطة غير متاحة على أندرويد") }
        }
    }

    private fun verifyPurchaseOnServer(purchase: com.android.billingclient.api.Purchase) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = repository.verifyPurchase("google_play", purchase.products.firstOrNull() ?: "", purchase.purchaseToken)
            if (result.isSuccess) {
                // Once server verifies and updates database, acknowledge the purchase
                val ackResult = billingManager.acknowledgePurchase(purchase.purchaseToken)
                if (ackResult) {
                    _state.update { it.copy(isLoading = false, error = "تم تفعيل الاشتراك بنجاح!") }
                } else {
                    _state.update { it.copy(isLoading = false, error = "فشل تأكيد الاشتراك في المتجر") }
                }
            } else {
                _state.update { it.copy(isLoading = false, error = "فشل التحقق من الخادم: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        billingManager.endConnection()
    }
}
