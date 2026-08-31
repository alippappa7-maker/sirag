package com.siraj.app.features.beta

import com.siraj.app.domain.models.UserProfile
import com.siraj.app.domain.models.beta.BetaFeedback
import com.siraj.app.domain.models.beta.FeedbackCategory
import com.siraj.app.domain.models.beta.FeedbackSeverity
import com.siraj.app.domain.repository.AuthRepository
import com.siraj.app.domain.repository.BetaFeedbackRepository
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BetaFeedbackViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var feedbackRepository: BetaFeedbackRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: BetaFeedbackViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        feedbackRepository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)

        val mockUser = UserProfile(id = "tester_1", email = "beta@siraj.app", name = "مختبر تجريبي")
        every { authRepository.currentUser } returns flowOf(mockUser)
        every { feedbackRepository.getMyFeedback("tester_1") } returns
            flowOf(
                listOf(
                    BetaFeedback(
                        id = "fb_1",
                        userId = "tester_1",
                        title = "خلل تجريبي",
                        category = FeedbackCategory.BUG,
                    ),
                ),
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization loads my feedback list`() =
        runTest {
            viewModel = BetaFeedbackViewModel(feedbackRepository, authRepository)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(1, state.myFeedbackList.size)
            assertEquals("fb_1", state.myFeedbackList[0].id)
        }

    @Test
    fun `submitFeedback fails when title is empty`() =
        runTest {
            viewModel = BetaFeedbackViewModel(feedbackRepository, authRepository)
            advanceUntilIdle()

            viewModel.updateTitle("")
            viewModel.updateDescription("وصف المشكلة")
            viewModel.submitFeedback()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isSuccess)
            assertNotNull(state.errorMessage)
        }

    @Test
    fun `submitFeedback succeeds when required fields are filled`() =
        runTest {
            coEvery { feedbackRepository.submitFeedback(any()) } returns Result.success("fb_123")

            viewModel = BetaFeedbackViewModel(feedbackRepository, authRepository)
            advanceUntilIdle()

            viewModel.updateCategory(FeedbackCategory.SHARIA_ISSUE)
            viewModel.updateSeverity(FeedbackSeverity.HIGH)
            viewModel.updateTitle("خطأ في تشكيل الآية")
            viewModel.updateDescription("هناك فتحة بدلاً من ضمة في سورة كذا")
            viewModel.updateSteps("فتح المصحف ثم الانتقال للآية")

            viewModel.submitFeedback()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.isSuccess)
            assertNotNull(state.successMessage)
            assertEquals("", state.title)
            assertEquals("", state.description)

            coVerify {
                feedbackRepository.submitFeedback(
                    match {
                        it.title == "خطأ في تشكيل الآية" &&
                            it.category == FeedbackCategory.SHARIA_ISSUE &&
                            it.severity == FeedbackSeverity.HIGH
                    },
                )
            }
        }
}
