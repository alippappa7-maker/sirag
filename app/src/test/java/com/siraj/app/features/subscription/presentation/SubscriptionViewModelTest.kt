package com.siraj.app.features.subscription.presentation

import com.siraj.app.data.billing.GooglePlayBillingManager
import com.siraj.app.domain.models.subscription.CreditBalance
import com.siraj.app.domain.repository.subscription.SubscriptionRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SubscriptionViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: SubscriptionRepository
    private lateinit var billingManager: GooglePlayBillingManager
    private lateinit var viewModel: SubscriptionViewModel

    // Using simple mock objects instead of actual billing entities which are hard to mock completely

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        billingManager = mockk(relaxed = true)

        // Mock default flows
        coEvery { repository.getCurrentSubscription(any()) } returns flowOf(null)
        coEvery { repository.getCurrentEntitlement(any()) } returns flowOf(null)
        coEvery { repository.getCreditBalance(any()) } returns flowOf(CreditBalance("test", "user1", 100, 50, 0, 150))
        coEvery { repository.getAvailablePlans() } returns flowOf(emptyList())
        coEvery { repository.getCreditTransactions(any(), any(), any()) } returns flowOf(emptyList())

        every { billingManager.purchaseUpdates } returns MutableStateFlow(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization loads user balances correctly`() =
        runTest {
            viewModel = SubscriptionViewModel(repository, billingManager)
            advanceUntilIdle()

            assertEquals(
                100,
                viewModel.state.value.balance
                    ?.availableCredits,
            )
            assertEquals(
                50,
                viewModel.state.value.balance
                    ?.totalPurchased,
            )
            assertEquals(
                0,
                viewModel.state.value.balance
                    ?.totalUsed,
            )
        }

    @Test
    fun `initialization triggers billing connection`() =
        runTest {
            viewModel = SubscriptionViewModel(repository, billingManager)
            advanceUntilIdle()

            verify(exactly = 1) { billingManager.startConnection(any(), any()) }
        }
}
