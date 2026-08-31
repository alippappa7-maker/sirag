package com.siraj.app.features.studio.presentation

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.Project
import com.siraj.app.domain.models.ProjectStatus
import com.siraj.app.domain.models.UserProfile
import com.siraj.app.domain.models.UserPreferences
import com.siraj.app.domain.repository.AuthRepository
import com.siraj.app.domain.repository.ProjectRepository
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
class StudioViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var projectRepository: ProjectRepository
    private lateinit var viewModel: StudioViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk(relaxed = true)
        projectRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadProjects loads projects and applies default filters`() =
        runTest {
            val mockUser =
                UserProfile(
                    id = "user123",
                    email = "test@test.com",
                    name = "Test",
                    preferences = UserPreferences(activeWorkspaceId = "ws1"),
                )
            val mockProjects =
                listOf(
                    Project(id = "p1", title = "Active Project", status = ProjectStatus.DRAFT, updatedAt = 1000),
                    Project(id = "p2", title = "Archived Project", status = ProjectStatus.ARCHIVED, updatedAt = 2000),
                )

            every { authRepository.currentUser } returns flowOf(mockUser)
            every { projectRepository.getAllProjects("ws1") } returns flowOf(Resource.Success(mockProjects))

            viewModel = StudioViewModel(authRepository, projectRepository)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.projects is Resource.Success)
            val filteredProjects = (viewModel.uiState.value.projects as Resource.Success).data
            assertEquals(1, filteredProjects.size)
            assertEquals("Active Project", filteredProjects[0].title)
        }

    @Test
    fun `updateSearchQuery filters projects by title`() =
        runTest {
            val mockUser =
                UserProfile(
                    id = "user123",
                    email = "test@test.com",
                    name = "Test",
                    preferences = UserPreferences(activeWorkspaceId = "ws1"),
                )
            val mockProjects =
                listOf(
                    Project(id = "p1", title = "First Project", status = ProjectStatus.DRAFT),
                    Project(id = "p2", title = "Second Project", status = ProjectStatus.DRAFT),
                )

            every { authRepository.currentUser } returns flowOf(mockUser)
            every { projectRepository.getAllProjects("ws1") } returns flowOf(Resource.Success(mockProjects))

            viewModel = StudioViewModel(authRepository, projectRepository)
            advanceUntilIdle()

            viewModel.updateSearchQuery("second")
            advanceUntilIdle()

            val filteredProjects = (viewModel.uiState.value.projects as Resource.Success).data
            assertEquals(1, filteredProjects.size)
            assertEquals("Second Project", filteredProjects[0].title)
        }
}
