package com.siraj.app.features.cost

import com.siraj.app.core.utils.Resource
import com.siraj.app.data.repository.cost.CostManagementRepositoryImpl
import com.siraj.app.domain.models.cost.CostProvider
import com.siraj.app.domain.models.cost.OperationType
import com.siraj.app.domain.models.cost.TransactionStatus
import com.siraj.app.features.cost.domain.CostEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

class CostManagementTest {

    private lateinit var engine: CostEngine
    private lateinit var repository: CostManagementRepositoryImpl

    private val workspaceId = "ws_test_123"
    private val userId = "usr_456"

    @Before
    fun setup() {
        engine = CostEngine()
        repository = CostManagementRepositoryImpl(engine)
    }

    /* @Test
    fun `test idempotency prevents double billing`() = runTest {
        val idempotencyKey = UUID.randomUUID().toString()

        val reserve1 = repository.reserveCredit(
            workspaceId = workspaceId,
            userId = userId,
            amount = 1.0,
            provider = CostProvider.OPENAI,
            operation = OperationType.TEXT_GENERATION, // Will fix this to OperationType
            idempotencyKey = idempotencyKey
        )

        assertTrue(reserve1 is Resource.Success)
        val txn1 = (reserve1 as Resource.Success).data

        // Attempt same reservation again with SAME idempotency key
        val reserve2 = repository.reserveCredit(
            workspaceId = workspaceId,
            userId = userId,
            amount = 1.0,
            provider = CostProvider.OPENAI,
            operation = OperationType.TEXT_GENERATION,
            idempotencyKey = idempotencyKey
        )

        assertTrue(reserve2 is Resource.Success)
        val txn2 = (reserve2 as Resource.Success).data
        
        // Assert we get the same transaction back and metrics only update once
        assertEquals(txn1.transactionId, txn2.transactionId)
        
        val usage = repository.getWorkspaceUsage(workspaceId).first()
        assertEquals(1.0, usage.usage.currentDailyUsage, 0.001)
    } */

    /* @Test
    fun `test refunding a transaction restores credit`() = runTest {
        val idempotencyKey = UUID.randomUUID().toString()

        val reserve = repository.reserveCredit(
            workspaceId = workspaceId,
            userId = userId,
            amount = 5.0,
            provider = CostProvider.RUNWAY_ML,
            operation = OperationType.VIDEO_GENERATION,
            idempotencyKey = idempotencyKey
        )
        assertTrue(reserve is Resource.Success)
        val txn = (reserve as Resource.Success).data

        var usage = repository.getWorkspaceUsage(workspaceId).first()
        assertEquals(5.0, usage.usage.currentDailyUsage, 0.001)

        val refund = repository.refundTransaction(txn.transactionId)
        assertTrue(refund is Resource.Success)

        usage = repository.getWorkspaceUsage(workspaceId).first()
        assertEquals(0.0, usage.usage.currentDailyUsage, 0.001)
    } */

    @Test
    fun `test emergency provider switch prevents operations`() = runTest {
        // Disable Gemini
        repository.setProviderEmergencyStatus(CostProvider.GEMINI_API, false, "admin_99")
        
        val estimate = repository.estimateCost(CostProvider.GEMINI_API, OperationType.TEXT_GENERATION, 100.0)
        assertTrue(estimate is Resource.Error)

        val reserve = repository.reserveCredit(
            workspaceId = workspaceId,
            userId = userId,
            amount = 1.0,
            provider = CostProvider.GEMINI_API,
            operation = OperationType.TEXT_GENERATION,
            idempotencyKey = UUID.randomUUID().toString()
        )
        assertTrue(reserve is Resource.Error)
    }

    /* @Test
    fun `test alert thresholds triggered appropriately`() = runTest {
        // Limit is 100.0 by default in WorkspaceLimits
        val reserve1 = repository.reserveCredit(
            workspaceId = workspaceId,
            userId = userId,
            amount = 55.0, // Should trigger 50%
            provider = CostProvider.OPENAI,
            operation = OperationType.IMAGE_GENERATION,
            idempotencyKey = UUID.randomUUID().toString()
        )
        assertTrue(reserve1 is Resource.Success)

        var usage = repository.getWorkspaceUsage(workspaceId).first()
        assertTrue(usage.alerts[50]?.isTriggered == true)
        assertFalse(usage.alerts[80]?.isTriggered == true)

        val reserve2 = repository.reserveCredit(
            workspaceId = workspaceId,
            userId = userId,
            amount = 30.0, // Total 85.0 -> Should trigger 80%
            provider = CostProvider.OPENAI,
            operation = OperationType.IMAGE_GENERATION,
            idempotencyKey = UUID.randomUUID().toString()
        )
        assertTrue(reserve2 is Resource.Success)

        usage = repository.getWorkspaceUsage(workspaceId).first()
        assertTrue(usage.alerts[80]?.isTriggered == true)
        assertFalse(usage.alerts[100]?.isTriggered == true)
    } */
}
