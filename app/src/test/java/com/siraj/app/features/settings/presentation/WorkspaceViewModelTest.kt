package com.siraj.app.features.settings.presentation

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.UserProfile
import com.siraj.app.domain.models.UserPreferences
import com.siraj.app.domain.models.Workspace
import com.siraj.app.domain.models.WorkspaceMember
import com.siraj.app.domain.models.WorkspaceRole
import com.siraj.app.domain.repository.AuthRepository
import com.siraj.app.domain.repository.WorkspaceRepository
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkspaceViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var workspaceRepository: WorkspaceRepository
    private lateinit var viewModel: WorkspaceViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk(relaxed = true)
        workspaceRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `inviteMember fails if user is not OWNER or MANAGER`() = runTest {
        val mockUser = UserProfile(id = "user123", email = "test@test.com", name = "Test", preferences = UserPreferences(activeWorkspaceId = "ws1"))
        val mockWorkspace = Workspace(id = "ws1", name = "Test WS", ownerId = "owner123")
        val mockMembers = listOf(WorkspaceMember(userId = "user123", role = WorkspaceRole.EDITOR))
        
        every { authRepository.currentUser } returns flowOf(mockUser)
        every { workspaceRepository.getUserWorkspaces("user123") } returns flowOf(Resource.Success(listOf(mockWorkspace)))
        every { workspaceRepository.getWorkspaceMembers("ws1") } returns flowOf(Resource.Success(mockMembers))
        every { workspaceRepository.getUserInvitations("test@test.com") } returns flowOf(Resource.Success(emptyList()))
        
        viewModel = WorkspaceViewModel(authRepository, workspaceRepository)
        advanceUntilIdle()

        viewModel.inviteMember("new@test.com", WorkspaceRole.EDITOR)
        advanceUntilIdle()

        assertEquals("ليس لديك صلاحية لدعوة أعضاء", viewModel.uiState.value.error)
    }

    @Test
    fun `inviteMember succeeds if user is OWNER`() = runTest {
        val mockUser = UserProfile(id = "owner123", email = "test@test.com", name = "Test", preferences = UserPreferences(activeWorkspaceId = "ws1"))
        val mockWorkspace = Workspace(id = "ws1", name = "Test WS", ownerId = "owner123")
        val mockMembers = listOf(WorkspaceMember(userId = "owner123", role = WorkspaceRole.OWNER))
        
        every { authRepository.currentUser } returns flowOf(mockUser)
        every { workspaceRepository.getUserWorkspaces("owner123") } returns flowOf(Resource.Success(listOf(mockWorkspace)))
        every { workspaceRepository.getWorkspaceMembers("ws1") } returns flowOf(Resource.Success(mockMembers))
        every { workspaceRepository.getUserInvitations("test@test.com") } returns flowOf(Resource.Success(emptyList()))
        
        coEvery { workspaceRepository.inviteMember("ws1", "new@test.com", WorkspaceRole.EDITOR, "owner123") } returns Resource.Success(Unit)
        
        viewModel = WorkspaceViewModel(authRepository, workspaceRepository)
        advanceUntilIdle()

        viewModel.inviteMember("new@test.com", WorkspaceRole.EDITOR)
        advanceUntilIdle()

        assertEquals(null, viewModel.uiState.value.error)
    }
}
