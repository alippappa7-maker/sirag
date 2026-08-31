package com.siraj.app.domain.models.cost

enum class CostProvider {
    GEMINI_API,
    OPENAI,
    ELEVEN_LABS,
    RUNWAY_ML,
    SYSTEM_INTERNAL
}

enum class OperationType {
    TEXT_GENERATION,
    IMAGE_GENERATION,
    AUDIO_TTS_GENERATION,
    VIDEO_GENERATION,
    DATA_STORAGE,
    REGENERATION
}

enum class TransactionStatus {
    RESERVED,
    COMMITTED,
    REFUNDED,
    FAILED
}

data class CostEstimate(
    val expectedCost: Double,
    val currency: String = "USD",
    val provider: CostProvider,
    val operation: OperationType,
    val warningThresholdExceeded: Boolean
)

data class CostTransaction(
    val transactionId: String,
    val idempotencyKey: String,
    val workspaceId: String,
    val userId: String,
    val provider: CostProvider,
    val operation: OperationType,
    val amount: Double,
    val status: TransactionStatus,
    val timestamp: Long
)

data class WorkspaceLimits(
    val workspaceId: String,
    val dailyLimitUsd: Double = 10.0,
    val monthlyLimitUsd: Double = 100.0,
    val perUserLimitUsd: Double = 2.0,
    val perOperationLimitUsd: Double = 0.5,
    val maxVideoDurationSeconds: Int = 30,
    val maxMediaSizeBytes: Long = 50 * 1024 * 1024, // 50 MB
    val maxRegenerationsPerPrompt: Int = 3
)

data class UsageMetrics(
    val currentDailyUsage: Double = 0.0,
    val currentMonthlyUsage: Double = 0.0,
    val userUsageMap: Map<String, Double> = emptyMap(),
    val regenerationsCountMap: Map<String, Int> = emptyMap()
)

data class AlertLevel(
    val percentage: Int, // 50, 80, 100
    val isTriggered: Boolean,
    val timestamp: Long? = null
)

data class WorkspaceUsageStatus(
    val workspaceId: String,
    val limits: WorkspaceLimits,
    val usage: UsageMetrics,
    val alerts: Map<Int, AlertLevel> = mapOf(
        50 to AlertLevel(50, false),
        80 to AlertLevel(80, false),
        100 to AlertLevel(100, false)
    ),
    val isSuspended: Boolean = false
)

data class ProviderEmergencyStatus(
    val provider: CostProvider,
    val isEnabled: Boolean = true,
    val disabledByAdminId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
