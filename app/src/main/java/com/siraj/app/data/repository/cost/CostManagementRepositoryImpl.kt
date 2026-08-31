package com.siraj.app.data.repository.cost

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.cost.*
import com.siraj.app.domain.repository.cost.CostManagementRepository
import com.siraj.app.features.cost.domain.CostEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

class CostManagementRepositoryImpl(
    private val costEngine: CostEngine
) : CostManagementRepository {

    private val providerStatusMap = MutableStateFlow<Map<CostProvider, ProviderEmergencyStatus>>(
        CostProvider.values().associateWith { ProviderEmergencyStatus(it) }
    )

    private val workspaceLimitsMap = mutableMapOf<String, WorkspaceLimits>()
    private val usageMetricsMap = MutableStateFlow<Map<String, UsageMetrics>>(emptyMap())
    private val transactionLogs = MutableStateFlow<List<CostTransaction>>(emptyList())
    private val alertMap = MutableStateFlow<Map<String, Map<Int, AlertLevel>>>(emptyMap())
    
    // In-memory idempotency check
    private val processedIdempotencyKeys = mutableSetOf<String>()

    override suspend fun estimateCost(
        provider: CostProvider,
        operation: OperationType,
        units: Double
    ): Resource<CostEstimate> {
        val status = providerStatusMap.value[provider]
        if (status?.isEnabled == false) {
            return Resource.Error("Provider is currently disabled by administrator.")
        }
        
        // Simple mock estimation logic
        val baseRate = when (operation) {
            OperationType.TEXT_GENERATION -> 0.0001
            OperationType.IMAGE_GENERATION -> 0.05
            OperationType.AUDIO_TTS_GENERATION -> 0.01
            OperationType.VIDEO_GENERATION -> 0.2
            OperationType.DATA_STORAGE -> 0.001
            OperationType.REGENERATION -> 0.0001
        }
        
        val estimatedCost = baseRate * units
        return Resource.Success(
            CostEstimate(
                expectedCost = estimatedCost,
                provider = provider,
                operation = operation,
                warningThresholdExceeded = estimatedCost > 0.4 // Arbitrary threshold for warning
            )
        )
    }

    override suspend fun reserveCredit(
        workspaceId: String,
        userId: String,
        amount: Double,
        provider: CostProvider,
        operation: OperationType,
        idempotencyKey: String,
        promptHash: String?
    ): Resource<CostTransaction> {
        if (processedIdempotencyKeys.contains(idempotencyKey)) {
            // Find existing transaction to prevent double billing
            val existing = transactionLogs.value.find { it.idempotencyKey == idempotencyKey }
            if (existing != null) {
                return Resource.Success(existing)
            }
        }

        val limits = workspaceLimitsMap.getOrPut(workspaceId) { WorkspaceLimits(workspaceId = workspaceId) }
        val currentMetrics = usageMetricsMap.value[workspaceId] ?: UsageMetrics()

        if (!costEngine.canPerformOperation(limits, currentMetrics, userId, amount, promptHash)) {
            return Resource.Error("Workspace or user usage limits exceeded.")
        }
        
        val status = providerStatusMap.value[provider]
        if (status?.isEnabled == false) {
            return Resource.Error("Provider is currently disabled.")
        }

        val transaction = CostTransaction(
            transactionId = "txn_${UUID.randomUUID()}",
            idempotencyKey = idempotencyKey,
            workspaceId = workspaceId,
            userId = userId,
            provider = provider,
            operation = operation,
            amount = amount,
            status = TransactionStatus.RESERVED,
            timestamp = System.currentTimeMillis()
        )

        // Update idempotency
        processedIdempotencyKeys.add(idempotencyKey)
        
        // Add to logs
        val newLogs = transactionLogs.value.toMutableList()
        newLogs.add(transaction)
        transactionLogs.value = newLogs

        // Update metrics temporarily
        updateMetrics(workspaceId, userId, amount, promptHash)

        return Resource.Success(transaction)
    }

    override suspend fun commitTransaction(transactionId: String): Resource<Unit> {
        val currentLogs = transactionLogs.value.toMutableList()
        val index = currentLogs.indexOfFirst { it.transactionId == transactionId }
        
        if (index == -1) return Resource.Error("Transaction not found")
        
        val txn = currentLogs[index]
        if (txn.status != TransactionStatus.RESERVED) {
            return Resource.Error("Transaction cannot be committed from status ${txn.status}")
        }
        
        currentLogs[index] = txn.copy(status = TransactionStatus.COMMITTED)
        transactionLogs.value = currentLogs
        
        return Resource.Success(Unit)
    }

    override suspend fun refundTransaction(transactionId: String): Resource<Unit> {
        val currentLogs = transactionLogs.value.toMutableList()
        val index = currentLogs.indexOfFirst { it.transactionId == transactionId }
        
        if (index == -1) return Resource.Error("Transaction not found")
        
        val txn = currentLogs[index]
        if (txn.status == TransactionStatus.REFUNDED) {
            return Resource.Success(Unit)
        }
        
        currentLogs[index] = txn.copy(status = TransactionStatus.REFUNDED)
        transactionLogs.value = currentLogs
        
        // Remove metrics
        reverseMetrics(txn.workspaceId, txn.userId, txn.amount)
        
        return Resource.Success(Unit)
    }

    private fun updateMetrics(workspaceId: String, userId: String, amount: Double, promptHash: String?) {
        val currentMap = usageMetricsMap.value.toMutableMap()
        val currentMetrics = currentMap[workspaceId] ?: UsageMetrics()
        
        val userMap = currentMetrics.userUsageMap.toMutableMap()
        userMap[userId] = (userMap[userId] ?: 0.0) + amount
        
        val regenMap = currentMetrics.regenerationsCountMap.toMutableMap()
        if (promptHash != null) {
            regenMap[promptHash] = (regenMap[promptHash] ?: 0) + 1
        }
        
        val newMetrics = currentMetrics.copy(
            currentDailyUsage = currentMetrics.currentDailyUsage + amount,
            currentMonthlyUsage = currentMetrics.currentMonthlyUsage + amount,
            userUsageMap = userMap,
            regenerationsCountMap = regenMap
        )
        
        currentMap[workspaceId] = newMetrics
        usageMetricsMap.value = currentMap
        
        // Calculate alerts
        val limits = workspaceLimitsMap[workspaceId] ?: WorkspaceLimits(workspaceId = workspaceId)
        val triggers = costEngine.calculateAlertTriggers(newMetrics.currentMonthlyUsage, limits.monthlyLimitUsd)
        
        val alertsMapForWorkspace = alertMap.value[workspaceId]?.toMutableMap() ?: mapOf(
            50 to AlertLevel(50, false),
            80 to AlertLevel(80, false),
            100 to AlertLevel(100, false)
        ).toMutableMap()
        
        triggers.forEach { t ->
            if (alertsMapForWorkspace[t]?.isTriggered != true) {
                 alertsMapForWorkspace[t] = AlertLevel(t, true, System.currentTimeMillis())
            }
        }
        
        val newAlertMap = alertMap.value.toMutableMap()
        newAlertMap[workspaceId] = alertsMapForWorkspace
        alertMap.value = newAlertMap
    }
    
    private fun reverseMetrics(workspaceId: String, userId: String, amount: Double) {
        val currentMap = usageMetricsMap.value.toMutableMap()
        val currentMetrics = currentMap[workspaceId] ?: return
        
        val userMap = currentMetrics.userUsageMap.toMutableMap()
        val currentUserUsage = userMap[userId] ?: 0.0
        userMap[userId] = maxOf(0.0, currentUserUsage - amount)
        
        val newMetrics = currentMetrics.copy(
            currentDailyUsage = maxOf(0.0, currentMetrics.currentDailyUsage - amount),
            currentMonthlyUsage = maxOf(0.0, currentMetrics.currentMonthlyUsage - amount),
            userUsageMap = userMap
        )
        
        currentMap[workspaceId] = newMetrics
        usageMetricsMap.value = currentMap
    }

    override fun getWorkspaceUsage(workspaceId: String): Flow<WorkspaceUsageStatus> {
        return usageMetricsMap.map { metricsMap ->
            val metrics = metricsMap[workspaceId] ?: UsageMetrics()
            val limits = workspaceLimitsMap[workspaceId] ?: WorkspaceLimits(workspaceId = workspaceId)
            val alerts = alertMap.value[workspaceId] ?: mapOf(
                50 to AlertLevel(50, false),
                80 to AlertLevel(80, false),
                100 to AlertLevel(100, false)
            )
            val isSuspended = metrics.currentMonthlyUsage >= limits.monthlyLimitUsd
            WorkspaceUsageStatus(workspaceId, limits, metrics, alerts, isSuspended)
        }
    }

    override suspend fun updateWorkspaceLimits(
        workspaceId: String,
        newLimits: WorkspaceLimits,
        adminId: String
    ): Resource<Unit> {
        workspaceLimitsMap[workspaceId] = newLimits
        return Resource.Success(Unit)
    }

    override fun getProviderEmergencyStatuses(): Flow<List<ProviderEmergencyStatus>> {
        return providerStatusMap.map { it.values.toList() }
    }

    override suspend fun setProviderEmergencyStatus(
        provider: CostProvider,
        isEnabled: Boolean,
        adminId: String
    ): Resource<Unit> {
        val newMap = providerStatusMap.value.toMutableMap()
        newMap[provider] = ProviderEmergencyStatus(
            provider = provider,
            isEnabled = isEnabled,
            disabledByAdminId = if (!isEnabled) adminId else null,
            timestamp = System.currentTimeMillis()
        )
        providerStatusMap.value = newMap
        return Resource.Success(Unit)
    }

    override fun getTransactionLogs(workspaceId: String): Flow<List<CostTransaction>> {
        return transactionLogs.map { logs ->
            logs.filter { it.workspaceId == workspaceId }.sortedByDescending { it.timestamp }
        }
    }
}
