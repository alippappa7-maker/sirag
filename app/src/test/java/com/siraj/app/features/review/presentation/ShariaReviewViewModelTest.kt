package com.siraj.app.features.review.presentation

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.repository.review.ShariaReviewRepository
import io.mockk.coEvery
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShariaReviewViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: ShariaReviewRepository
    private lateinit var viewModel: ShariaReviewViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)

        // Mock default flow
        coEvery { repository.getReviewQueue(any()) } returns flowOf(Resource.Success(emptyList()))

        viewModel = ShariaReviewViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `approveItem success updates UI state`() =
        runTest {
            coEvery { repository.approveItem("item1", "reviewer1", "Test Reviewer", "Looks good", null) } returns Resource.Success(Unit)

            viewModel.approveItem("item1", "reviewer1", "Test Reviewer", "Looks good")
            advanceUntilIdle()

            assertEquals("تم تسجيل قرار الاعتماد الشرعي بنجاح", viewModel.uiState.value.successMessage)
            assertEquals(false, viewModel.uiState.value.isActionInProgress)
        }

    @Test
    fun `approveItem error updates UI state`() =
        runTest {
            coEvery { repository.approveItem("item1", "reviewer1", "Test Reviewer", "Looks good", null) } returns Resource.Error("Error")

            viewModel.approveItem("item1", "reviewer1", "Test Reviewer", "Looks good")
            advanceUntilIdle()

            assertEquals("Error", viewModel.uiState.value.errorMessage)
            assertEquals(false, viewModel.uiState.value.isActionInProgress)
        }

    @Test
    fun `rejectItem success updates UI state`() =
        runTest {
            coEvery { repository.rejectItem("item1", "reviewer1", "Test Reviewer", "Violation") } returns Resource.Success(Unit)

            viewModel.rejectItem("item1", "reviewer1", "Test Reviewer", "Violation")
            advanceUntilIdle()

            assertEquals("تم تسجيل قرار الرفض الشرعي بنجاح", viewModel.uiState.value.successMessage)
        }
}
