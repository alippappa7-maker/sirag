package com.siraj.app.features.notification.presentation

import android.app.Application
import com.siraj.app.domain.models.notification.NotificationType
import com.siraj.app.domain.models.notification.SirajNotification
import com.siraj.app.domain.models.notification.NotificationFilter
import com.siraj.app.domain.repository.notification.NotificationRepository
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
class NotificationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: NotificationRepository
    private lateinit var application: Application
    private lateinit var viewModel: NotificationViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        application = mockk(relaxed = true)
        
        // Setup mock notifications
        val mockNotifications = listOf(
            SirajNotification(
                id = "notif1",
                userId = "user1",
                type = NotificationType.SYSTEM_MESSAGE,
                title = "Title",
                body = "Body",
                readAt = null,
                createdAt = 1000
            )
        )
        
        coEvery { repository.getNotificationsFlow(any()) } returns flowOf(mockNotifications)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization loads notifications correctly`() = runTest {
        viewModel = NotificationViewModel(application, repository)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.notifications.size)
        assertEquals(1, viewModel.uiState.value.filteredNotifications.size)
        assertEquals(1, viewModel.uiState.value.unreadCount)
    }

    @Test
    fun `setFilter updates filtered list correctly`() = runTest {
        viewModel = NotificationViewModel(application, repository)
        advanceUntilIdle()

        viewModel.setFilter(NotificationFilter.REVIEW) // There are no REVIEW notifications in mock
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.filteredNotifications.size)
        assertEquals(NotificationFilter.REVIEW, viewModel.uiState.value.selectedFilter)
    }
}
