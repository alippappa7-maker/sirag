package com.siraj.app.data.repository.subscription

import com.siraj.app.domain.models.subscription.*
import com.siraj.app.domain.repository.subscription.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FirebaseSubscriptionRepositoryImpl : SubscriptionRepository {

    private val mockPlans = MutableStateFlow(
        listOf(
            Plan(
                id = "plan_free",
                name = "مجاني",
                description = "ميزات محراب سراج الأساسية",
                interval = BillingInterval.MONTHLY,
                price = 0.0,
                currency = "USD",
                features = listOf("QURAN_READER", "PRAYER_TIMES", "ADHAKAR"),
                limits = listOf(
                    UsageLimit("AI_IMAGE_GENERATION", 5, 0, null),
                    UsageLimit("AUDIO_GENERATION", 2, 0, null)
                ),
                active = true,
                platformProductIds = emptyMap()
            ),
            Plan(
                id = "plan_pro",
                name = "الاحترافي",
                description = "لمنشئي المحتوى المتقدمين",
                interval = BillingInterval.MONTHLY,
                price = 9.99,
                currency = "USD",
                features = listOf("QURAN_READER", "PRAYER_TIMES", "ADHAKAR", "ADVANCED_EXPORT", "NO_WATERMARK"),
                limits = listOf(
                    UsageLimit("AI_IMAGE_GENERATION", 100, 0, null),
                    UsageLimit("AUDIO_GENERATION", 50, 0, null)
                ),
                active = true,
                platformProductIds = mapOf("android" to "siraj_pro_monthly", "ios" to "siraj_pro_monthly")
            )
        )
    )

    private val mockSub = MutableStateFlow<Subscription?>(
        Subscription(
            id = "sub_123",
            userId = "current_user",
            workspaceId = null,
            planId = "plan_free",
            platform = "system",
            productId = "free",
            status = SubscriptionStatus.ACTIVE,
            purchaseTokenHash = "hash_12345",
            startedAt = System.currentTimeMillis(),
            renewsAt = null,
            expiresAt = null,
            cancelledAt = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    )

    override fun getAvailablePlans(): Flow<List<Plan>> = mockPlans

    override fun getCurrentSubscription(userId: String, workspaceId: String?): Flow<Subscription?> = mockSub

    override fun getCurrentEntitlement(userId: String, workspaceId: String?): Flow<Entitlement?> {
        return mockSub.map { sub ->
            if (sub != null && (sub.status == SubscriptionStatus.ACTIVE || sub.status == SubscriptionStatus.TRIAL)) {
                val plan = mockPlans.value.find { it.id == sub.planId }
                if (plan != null) {
                    Entitlement(
                        id = "ent_${sub.id}",
                        features = plan.features,
                        limits = plan.limits
                    )
                } else null
            } else null
        }
    }

    override fun getCreditBalance(userId: String, workspaceId: String?): Flow<CreditBalance?> {
        return MutableStateFlow(
            CreditBalance(
                userId = userId,
                workspaceId = workspaceId,
                availableCredits = 50,
                totalPurchased = 100,
                totalUsed = 50,
                lastUpdated = System.currentTimeMillis()
            )
        )
    }

    override fun getCreditTransactions(userId: String, workspaceId: String?, limit: Int): Flow<List<CreditTransaction>> {
        return MutableStateFlow(
            listOf(
                CreditTransaction(
                    id = "tx_1",
                    userId = userId,
                    workspaceId = workspaceId,
                    jobId = "job_1",
                    operationType = "AI_IMAGE_GENERATION",
                    amount = 5,
                    balanceBefore = 55,
                    balanceAfter = 50,
                    type = TransactionType.DEBIT,
                    status = TransactionStatus.COMPLETED,
                    reason = "AI Image Generation",
                    timestamp = System.currentTimeMillis() - 86400000
                ),
                CreditTransaction(
                    id = "tx_2",
                    userId = userId,
                    workspaceId = workspaceId,
                    jobId = null,
                    operationType = "PURCHASE",
                    amount = 100,
                    balanceBefore = 0,
                    balanceAfter = 100,
                    type = TransactionType.CREDIT,
                    status = TransactionStatus.COMPLETED,
                    reason = "Purchase 100 Credits",
                    timestamp = System.currentTimeMillis() - 172800000
                )
            )
        )
    }

    override suspend fun reserveCredits(userId: String, workspaceId: String?, jobId: String, operationType: String, amount: Int): Result<CreditTransaction> {
        // Real implementation should call a Cloud Function or perform a Firestore Transaction
        // to atomically check balance, decrement it, and write a RESERVED transaction.
        return Result.success(
            CreditTransaction(
                id = "tx_new_${System.currentTimeMillis()}",
                userId = userId,
                workspaceId = workspaceId,
                jobId = jobId,
                operationType = operationType,
                amount = amount,
                balanceBefore = 50, // mock
                balanceAfter = 50 - amount,
                type = TransactionType.DEBIT,
                status = TransactionStatus.RESERVED,
                reason = "Reserving credits for $operationType",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    override suspend fun confirmCredits(transactionId: String): Result<Boolean> {
        // Server side: update transaction status to COMPLETED
        return Result.success(true)
    }

    override suspend fun refundCredits(transactionId: String, reason: String): Result<Boolean> {
        // Server side: update transaction status to REFUNDED, return amount to balance atomically
        return Result.success(true)
    }

    override suspend fun verifyPurchase(platform: String, productId: String, purchaseToken: String, workspaceId: String?): Result<Subscription> {
        // In a real app, this calls a Cloud Function to securely verify the purchase token.
        // It should never trust the client's assertion of a successful purchase.
        // For app_store, the server will call App Store Server API using the signed transaction.
        // For google_play, the server will call Google Play Developer API.
        
        return Result.success(
            Subscription(
                id = "sub_new_${System.currentTimeMillis()}",
                userId = "current_user",
                workspaceId = workspaceId,
                planId = "plan_pro",
                platform = platform,
                productId = productId,
                status = SubscriptionStatus.ACTIVE,
                purchaseTokenHash = purchaseToken.hashCode().toString(),
                startedAt = System.currentTimeMillis(),
                renewsAt = System.currentTimeMillis() + 2592000000L, // 30 days
                expiresAt = System.currentTimeMillis() + 2592000000L,
                cancelledAt = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }
}
