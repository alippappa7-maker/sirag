package com.siraj.app.features.project.presentation.ai

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.repository.AssetRepository
import com.siraj.app.domain.repository.ProjectRepository
import com.siraj.app.domain.services.AiImageGeneratorService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AiImageGeneratorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var aiService: AiImageGeneratorService
    private lateinit var projectRepository: ProjectRepository
    private lateinit var assetRepository: AssetRepository
    private lateinit var viewModel: AiImageGeneratorViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        aiService = mockk(relaxed = true)
        projectRepository = mockk(relaxed = true)
        assetRepository = mockk(relaxed = true)
        
        coEvery { projectRepository.getProject(any()) } returns Resource.Success(mockk(relaxed=true))
        
        viewModel = AiImageGeneratorViewModel("proj1", null, aiService, projectRepository, assetRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `viewModel initialization`() {
        assertNotNull(viewModel)
    }
}
