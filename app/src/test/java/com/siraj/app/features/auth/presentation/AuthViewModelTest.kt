package com.siraj.app.features.auth.presentation

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.UserProfile
import com.siraj.app.domain.repository.AuthRepository
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk(relaxed = true)
        
        // Mock currentUser flow
        every { authRepository.currentUser } returns flowOf(null)
        
        viewModel = AuthViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login success updates actionState to Success`() = runTest {
        coEvery { authRepository.login("test@test.com", "password") } returns Resource.Success(Unit)

        viewModel.login("test@test.com", "password")
        advanceUntilIdle()

        assertTrue(viewModel.actionState.value is Resource.Success)
        coVerify(exactly = 1) { authRepository.login("test@test.com", "password") }
    }

    @Test
    fun `login error updates actionState to Error`() = runTest {
        coEvery { authRepository.login("test@test.com", "wrong_password") } returns Resource.Error("Invalid credentials")

        viewModel.login("test@test.com", "wrong_password")
        advanceUntilIdle()

        assertTrue(viewModel.actionState.value is Resource.Error)
        assertEquals("Invalid credentials", (viewModel.actionState.value as Resource.Error).message)
    }

    @Test
    fun `register calls repository with correct params`() = runTest {
        coEvery { authRepository.register("Name", "test@test.com", "password") } returns Resource.Success(Unit)

        viewModel.register("Name", "test@test.com", "password")
        advanceUntilIdle()

        assertTrue(viewModel.actionState.value is Resource.Success)
        coVerify(exactly = 1) { authRepository.register("Name", "test@test.com", "password") }
    }
}
