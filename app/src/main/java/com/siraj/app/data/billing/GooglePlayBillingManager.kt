package com.siraj.app.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class BillingState(
    val isConnected: Boolean = false,
    val availableProducts: List<ProductDetails> = emptyList(),
    val activePurchases: List<Purchase> = emptyList()
)

class GooglePlayBillingManager(private val context: Context) : PurchasesUpdatedListener {

    private val _billingState = MutableStateFlow(BillingState())
    val billingState: StateFlow<BillingState> = _billingState.asStateFlow()

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    // Pass purchases to this flow to be processed by a repository or viewmodel
    val purchaseUpdates = MutableStateFlow<List<Purchase>?>(null)

    fun startConnection(onSuccess: () -> Unit, onError: () -> Unit) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    _billingState.value = _billingState.value.copy(isConnected = true)
                    onSuccess()
                } else {
                    onError()
                }
            }

            override fun onBillingServiceDisconnected() {
                _billingState.value = _billingState.value.copy(isConnected = false)
                // Retry logic could go here
            }
        })
    }

    suspend fun queryProductDetails(productIds: List<String>): List<ProductDetails> {
        val productList = productIds.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

        return suspendCancellableCoroutine { continuation ->
            billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                if (billingResult.responseCode == BillingResponseCode.OK && productDetailsList != null) {
                    _billingState.value = _billingState.value.copy(availableProducts = productDetailsList)
                    continuation.resume(productDetailsList)
                } else {
                    continuation.resume(emptyList())
                }
            }
        }
    }

    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails, offerToken: String, oldPurchaseToken: String? = null) {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )
        val billingFlowParamsBuilder = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            
        if (!oldPurchaseToken.isNullOrEmpty()) {
            val updateParams = BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                .setOldPurchaseToken(oldPurchaseToken)
                .setSubscriptionReplacementMode(BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.CHARGE_FULL_PRICE)
                .build()
            billingFlowParamsBuilder.setSubscriptionUpdateParams(updateParams)
        }

        billingClient.launchBillingFlow(activity, billingFlowParamsBuilder.build())
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingResponseCode.OK && purchases != null) {
            purchaseUpdates.value = purchases
        } else if (billingResult.responseCode == BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            // Handle any other error codes.
        }
    }

    suspend fun queryPurchases(): List<Purchase> {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        return suspendCancellableCoroutine { continuation ->
            billingClient.queryPurchasesAsync(params) { billingResult, purchasesList ->
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    _billingState.value = _billingState.value.copy(activePurchases = purchasesList)
                    continuation.resume(purchasesList)
                } else {
                    continuation.resume(emptyList())
                }
            }
        }
    }

    suspend fun acknowledgePurchase(purchaseToken: String): Boolean {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        return suspendCancellableCoroutine { continuation ->
            billingClient.acknowledgePurchase(params) { billingResult ->
                continuation.resume(billingResult.responseCode == BillingResponseCode.OK)
            }
        }
    }

    fun endConnection() {
        billingClient.endConnection()
    }
}
