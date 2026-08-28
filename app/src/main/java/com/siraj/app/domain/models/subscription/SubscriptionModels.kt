package com.siraj.app.domain.models.subscription

enum class SubscriptionStatus {
    TRIAL, ACTIVE, GRACE_PERIOD, PAUSED, CANCELLED, EXPIRED, REFUNDED, REVOKED, PENDING
}

enum class BillingInterval {
    MONTHLY, YEARLY, LIFETIME, ONE_TIME
}

data class UsageLimit(
    val featureKey: String, // e.g. "AI_GENERATION", "VIDEO_EXPORT"
    val maxLimit: Int,
    val currentUsage: Int,
    val resetsAt: Long?
)

data class Entitlement(
    val id: String,
    val features: List<String>, // List of feature keys enabled by this entitlement
    val limits: List<UsageLimit>
)

data class Plan(
    val id: String,
    val name: String,
    val description: String,
    val interval: BillingInterval,
    val price: Double,
    val currency: String,
    val features: List<String>,
    val limits: List<UsageLimit>,
    val active: Boolean,
    val platformProductIds: Map<String, String> // e.g. {"android": "siraj_pro_monthly", "ios": "siraj_pro_monthly"}
)

data class Subscription(
    val id: String,
    val userId: String?,
    val workspaceId: String?,
    val planId: String,
    val platform: String, // e.g. "google_play", "app_store", "stripe"
    val productId: String,
    val status: SubscriptionStatus,
    val purchaseTokenHash: String, // Never raw token
    val startedAt: Long,
    val renewsAt: Long?,
    val expiresAt: Long?,
    val cancelledAt: Long?,
    val createdAt: Long,
    val updatedAt: Long
)

enum class TransactionType {
    CREDIT, DEBIT, REFUND, BONUS
}

enum class TransactionStatus {
    RESERVED, COMPLETED, FAILED, REFUNDED
}

data class CreditTransaction(
    val id: String,
    val userId: String?,
    val workspaceId: String?,
    val jobId: String?,
    val operationType: String,
    val amount: Int,
    val balanceBefore: Int,
    val balanceAfter: Int,
    val type: TransactionType,
    val status: TransactionStatus,
    val reason: String,
    val timestamp: Long
)

data class CreditBalance(
    val userId: String?,
    val workspaceId: String?,
    val availableCredits: Int,
    val totalPurchased: Int,
    val totalUsed: Int,
    val lastUpdated: Long
)
