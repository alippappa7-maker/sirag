package com.siraj.app.domain.repository.subscription

import com.siraj.app.domain.models.subscription.*
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    fun getAvailablePlans(): Flow<List<Plan>>

    fun getCurrentSubscription(
        userId: String,
        workspaceId: String? = null,
    ): Flow<Subscription?>

    fun getCurrentEntitlement(
        userId: String,
        workspaceId: String? = null,
    ): Flow<Entitlement?>

    fun getCreditBalance(
        userId: String,
        workspaceId: String? = null,
    ): Flow<CreditBalance?>

    fun getCreditTransactions(
        userId: String,
        workspaceId: String? = null,
        limit: Int = 50,
    ): Flow<List<CreditTransaction>>

    // Server-side credit operations
    suspend fun reserveCredits(
        userId: String,
        workspaceId: String?,
        jobId: String,
        operationType: String,
        amount: Int,
    ): Result<CreditTransaction>

    suspend fun confirmCredits(transactionId: String): Result<Boolean>

    suspend fun refundCredits(
        transactionId: String,
        reason: String,
    ): Result<Boolean>

    // Simulating server-side validation request
    suspend fun verifyPurchase(
        platform: String,
        productId: String,
        purchaseToken: String,
        workspaceId: String? = null,
    ): Result<Subscription>
}
