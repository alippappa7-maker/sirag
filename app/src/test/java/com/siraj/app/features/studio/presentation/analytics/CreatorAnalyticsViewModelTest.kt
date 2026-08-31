package com.siraj.app.features.studio.presentation.analytics

import com.siraj.app.domain.models.UserProfile
import com.siraj.app.domain.models.analytics.CreatorAnalyticsDashboard
import com.siraj.app.domain.repository.AuthRepository
import com.siraj.app.domain.repository.analytics.CreatorAnalyticsRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreatorAnalyticsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var analyticsRepository: CreatorAnalyticsRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: CreatorAnalyticsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        analyticsRepository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadAnalytics when user is not logged in returns Error state`() =
        runTest {
            every { authRepository.currentUser } returns flowOf(null)

            viewModel = CreatorAnalyticsViewModel(analyticsRepository, authRepository)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value is CreatorAnalyticsUiState.Error)
            assertEquals("يجب تسجيل الدخول لعرض التحليلات.", (viewModel.uiState.value as CreatorAnalyticsUiState.Error).message)
        }

    @Test
    fun `loadAnalytics when user is logged in returns Success state`() =
        runTest {
            val mockUser = UserProfile(id = "user123", email = "test@test.com", name = "Test")
            val mockDashboard = CreatorAnalyticsDashboard(totalViews = 1000)

            every { authRepository.currentUser } returns flowOf(mockUser)
            coEvery { analyticsRepository.getCreatorDashboard("user123", any()) } returns flowOf(mockDashboard)

            viewModel = CreatorAnalyticsViewModel(analyticsRepository, authRepository)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value is CreatorAnalyticsUiState.Success)
            assertEquals(1000L, (viewModel.uiState.value as CreatorAnalyticsUiState.Success).dashboard.totalViews)
        }
}
