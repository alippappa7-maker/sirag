package com.siraj.app.domain.repository.cost

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.cost.*
import kotlinx.coroutines.flow.Flow

interface CostManagementRepository {
    suspend fun estimateCost(
        provider: CostProvider,
        operation: OperationType,
        units: Double // e.g., tokens, seconds, bytes
    ): Resource<CostEstimate>

    suspend fun reserveCredit(
        workspaceId: String,
        userId: String,
        amount: Double,
        provider: CostProvider,
        operation: OperationType,
        idempotencyKey: String,
        promptHash: String? = null
    ): Resource<CostTransaction>

    suspend fun commitTransaction(transactionId: String): Resource<Unit>

    suspend fun refundTransaction(transactionId: String): Resource<Unit>

    fun getWorkspaceUsage(workspaceId: String): Flow<WorkspaceUsageStatus>

    suspend fun updateWorkspaceLimits(
        workspaceId: String,
        newLimits: WorkspaceLimits,
        adminId: String
    ): Resource<Unit>

    fun getProviderEmergencyStatuses(): Flow<List<ProviderEmergencyStatus>>

    suspend fun setProviderEmergencyStatus(
        provider: CostProvider,
        isEnabled: Boolean,
        adminId: String
    ): Resource<Unit>

    fun getTransactionLogs(workspaceId: String): Flow<List<CostTransaction>>
}
