package com.siraj.app.features.project.presentation

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.Project
import com.siraj.app.domain.repository.AuthRepository
import com.siraj.app.domain.repository.ProjectRepository
import com.siraj.app.domain.repository.TemplateRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectEditorViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var projectRepository: ProjectRepository
    private lateinit var templateRepository: TemplateRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: ProjectEditorViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        projectRepository = mockk(relaxed = true)
        templateRepository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)

        coEvery { projectRepository.getProject(any()) } returns Resource.Success(Project(id = "test_project", title = "Original Title"))
        coEvery { projectRepository.updateProject(any()) } returns Resource.Success(Unit)
        coEvery { templateRepository.getActiveTemplates() } returns flowOf(Resource.Success(emptyList()))

        viewModel = ProjectEditorViewModel("test_project", projectRepository, templateRepository, authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `auto-save triggers after delay`() =
        runTest {
            advanceUntilIdle() // Load initial project

            // Trigger an update
            viewModel.updateTitle("New Title")

            // At 0ms, save shouldn't be called yet due to debounce
            coVerify(exactly = 0) { projectRepository.updateProject(any()) }

            // Advance time by 1000ms
            advanceTimeBy(1000L)
            coVerify(exactly = 0) { projectRepository.updateProject(any()) }

            // Advance to 1500ms+
            advanceTimeBy(600L)
            advanceUntilIdle()

            // Now it should be called
            coVerify(exactly = 1) { projectRepository.updateProject(match { it.title == "New Title" }) }
            assertEquals(SaveState.Saved, viewModel.saveState.value)
        }

    @Test
    fun `multiple quick updates only trigger one save`() =
        runTest {
            advanceUntilIdle()

            viewModel.updateTitle("New Title 1")
            advanceTimeBy(500L)
            viewModel.updateTitle("New Title 2")
            advanceTimeBy(500L)
            viewModel.updateTitle("New Title 3")

            advanceUntilIdle()

            coVerify(exactly = 1) { projectRepository.updateProject(match { it.title == "New Title 3" }) }
        }
}
