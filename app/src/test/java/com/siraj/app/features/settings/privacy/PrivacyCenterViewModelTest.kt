package com.siraj.app.features.settings.privacy

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.privacy.*
import com.siraj.app.domain.repository.privacy.PrivacyRepository
import com.siraj.app.features.settings.presentation.privacy.PrivacyCenterViewModel
import com.siraj.app.features.settings.presentation.privacy.PrivacyDialogType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

class FakePrivacyRepository : PrivacyRepository {
    var overviewData = PrivacyOverviewData(projectsCount = 3, downloadsCount = 2, downloadsSizeBytes = 2048L, cacheSizeBytes = 1024L)
    var clearHistoryCalled = false
    var clearDownloadsCalled = false
    var clearCacheCalled = false
    var deletionRequested = false
    var deletionCancelled = false
    var correctionSubmitted = false

    override fun observePrivacyOverview(userId: String): Flow<PrivacyOverviewData> = flowOf(overviewData)
    override suspend fun getPrivacyOverview(userId: String): PrivacyOverviewData = overviewData

    override suspend fun generateUserDataExport(userId: String): Resource<UserDataExportPackage> {
        return Resource.Success(
            UserDataExportPackage(
                exportId = "TEST-EXP-1",
                userId = userId,
                exportTimestamp = 123456789L,
                exportDateFormatted = "2024-01-01 10:00",
                accountInfo = mapOf("name" to "User"),
                projects = emptyList(),
                activityHistory = emptyList(),
                preferences = emptyMap(),
                anonymizedInvoicesSummary = emptyList(),
                sha256Checksum = "abc123sha256"
            )
        )
    }

    override suspend fun exportUserDataToJson(userId: String): Resource<String> {
        return Resource.Success("{\"userId\":\"$userId\",\"exportId\":\"TEST-EXP-1\"}")
    }

    override suspend fun saveExportJsonToFile(context: Context, json: String): Resource<File> {
        val file = File(context.cacheDir, "test_export.json")
        file.writeText(json)
        return Resource.Success(file)
    }

    override suspend fun clearWatchHistory(userId: String): Resource<Unit> {
        clearHistoryCalled = true
        return Resource.Success(Unit)
    }

    override suspend fun clearDownloads(userId: String): Resource<Unit> {
        clearDownloadsCalled = true
        return Resource.Success(Unit)
    }

    override suspend fun clearLocalCache(context: Context): Resource<Long> {
        clearCacheCalled = true
        return Resource.Success(1024L)
    }

    override suspend fun deleteUserProject(projectId: String): Resource<Unit> {
        return Resource.Success(Unit)
    }

    override suspend fun requestAccountDeletion(userId: String, reason: String, gracePeriodDays: Int): Resource<AccountDeletionRequest> {
        deletionRequested = true
        return Resource.Success(
            AccountDeletionRequest(
                requestId = "DEL-1",
                userId = userId,
                status = DeletionStatus.GRACE_PERIOD_ACTIVE,
                requestedAt = System.currentTimeMillis(),
                scheduledPurgeAt = System.currentTimeMillis() + 14 * 86400000L,
                gracePeriodDays = gracePeriodDays,
                reason = reason
            )
        )
    }

    override suspend fun cancelAccountDeletion(userId: String): Resource<Unit> {
        deletionCancelled = true
        return Resource.Success(Unit)
    }

    override suspend fun submitDataCorrection(request: DataCorrectionRequest): Resource<Unit> {
        correctionSubmitted = true
        return Resource.Success(Unit)
    }

    override fun getStandardRetentionPolicies(): List<StoredDataCategory> {
        return emptyList()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class PrivacyCenterViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var app: Application
    private lateinit var fakeRepo: FakePrivacyRepository
    private lateinit var viewModel: PrivacyCenterViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        app = ApplicationProvider.getApplicationContext()
        fakeRepo = FakePrivacyRepository()
        viewModel = PrivacyCenterViewModel(app, fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadOverview updates state with repository data`() = runTest {
        viewModel.loadOverview("test_user_id")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(3, state.overview.projectsCount)
        assertEquals(2, state.overview.downloadsCount)
        assertEquals(2048L, state.overview.downloadsSizeBytes)
    }

    @Test
    fun `exportUserData generates file and opens EXPORT_SUCCESS dialog`() = runTest {
        viewModel.exportUserData(app, "test_user_id")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isExporting)
        assertEquals(PrivacyDialogType.EXPORT_SUCCESS, state.activeDialog)
        assertNotNull(state.exportedJson)
        assertEquals("abc123sha256", state.lastExportChecksum)
    }

    @Test
    fun `clearWatchHistory triggers repository and updates message`() = runTest {
        viewModel.clearWatchHistory("test_user_id")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeRepo.clearHistoryCalled)
        assertNotNull(viewModel.uiState.value.actionMessage)
    }

    @Test
    fun `clearDownloads triggers repository and updates message`() = runTest {
        viewModel.clearDownloads("test_user_id")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeRepo.clearDownloadsCalled)
        assertNotNull(viewModel.uiState.value.actionMessage)
    }

    @Test
    fun `requestAccountDeletion initiates grace period and calls onScheduled callback`() = runTest {
        var callbackCalled = false
        viewModel.requestAccountDeletion("user_123", "طلب شخصي", 14) {
            callbackCalled = true
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeRepo.deletionRequested)
        assertTrue(callbackCalled)
        assertNotNull(viewModel.uiState.value.actionMessage)
    }

    @Test
    fun `submitDataCorrection sends request to repository`() = runTest {
        viewModel.submitDataCorrection("user_123", "الاسم", "اسم قديم", "اسم جديد", "تصحيح إملائي")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeRepo.correctionSubmitted)
        assertEquals(PrivacyDialogType.NONE, viewModel.uiState.value.activeDialog)
    }
}
