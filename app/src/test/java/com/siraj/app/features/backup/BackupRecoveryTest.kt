package com.siraj.app.features.backup

import com.siraj.app.core.backup.BackupDisasterRecoveryManager
import com.siraj.app.data.repository.backup.FirebaseBackupRepositoryImpl
import com.siraj.app.domain.models.backup.BackupEnvironment
import com.siraj.app.domain.models.backup.BackupScope
import com.siraj.app.domain.models.backup.BackupStatus
import com.siraj.app.domain.models.backup.BackupType
import com.siraj.app.domain.models.backup.RestoreStatus
import com.siraj.app.domain.models.backup.RestoreTargetEnvironment
import com.siraj.app.features.admin.presentation.backup.BackupRecoveryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
class BackupRecoveryTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FirebaseBackupRepositoryImpl
    private lateinit var viewModel: BackupRecoveryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FirebaseBackupRepositoryImpl()
        viewModel = BackupRecoveryViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial backup snapshots are loaded with valid CMEK and SHA256 signatures`() = runTest {
        val snapshots = repository.getBackupSnapshots().first()
        assertTrue("Snapshots list should not be empty", snapshots.isNotEmpty())
        
        val prodSnapshot = snapshots.first { it.environment == BackupEnvironment.PROD && it.backupType == BackupType.FULL }
        assertEquals(BackupStatus.VERIFIED_HEALTHY, prodSnapshot.status)
        assertTrue("Storage location must use isolated vault bucket", prodSnapshot.storageLocationUri.contains("siraj-prod-backups-isolated-vault"))
        assertTrue("Must use AES-256 CMEK encryption", prodSnapshot.encryptionAlgorithm.contains("CMEK"))
        assertNotNull("Must include sha256 checksum", prodSnapshot.checksumSha256)
        assertTrue("Must include essential collections", prodSnapshot.collectionsIncluded.contains("sharia_reviews"))
    }

    @Test
    fun `triggering a new backup creates an encrypted snapshot with valid hash`() = runTest {
        val result = repository.triggerBackup(
            type = BackupType.FULL,
            environment = BackupEnvironment.PROD,
            notes = "اختبار إنشاء نسخة مشفرة"
        )
        assertTrue("Trigger backup should succeed", result.isSuccess)
        val snapshot = result.getOrNull()!!
        assertEquals(BackupStatus.SUCCESS, snapshot.status)
        assertEquals(BackupEnvironment.PROD, snapshot.environment)
        assertTrue(snapshot.storageLocationUri.startsWith("gs://siraj-prod-backups-isolated-vault-eu"))
        assertFalse(snapshot.checksumSha256.isBlank())
    }

    @Test
    fun `right to be forgotten strictly excludes deleted user accounts during restoration`() {
        val rawData = listOf(
            mapOf("id" to "doc_1", "userId" to "active_user_1", "title" to "مشروع نشط"),
            mapOf("id" to "doc_2", "userId" to "deleted_user_xyz", "title" to "مشروع لمستخدم تم حذف حسابه"),
            mapOf("id" to "doc_3", "authorId" to "deleted_user_xyz", "title" to "مراجعة شرعية ملغاة"),
            mapOf("id" to "doc_4", "ownerId" to "active_user_2", "title" to "مشروع آخر نشط")
        )

        val deletedUserIds = setOf("deleted_user_xyz")
        val (sanitized, purgedCount) = BackupDisasterRecoveryManager.filterDeletedUserTombstones(rawData, deletedUserIds)

        assertEquals(2, purgedCount)
        assertEquals(2, sanitized.size)
        assertTrue("Must not contain any records belonging to deleted user", sanitized.none { it["userId"] == "deleted_user_xyz" || it["authorId"] == "deleted_user_xyz" })
    }

    @Test
    fun `dry-run restore executes successfully in isolated recovery sandbox`() = runTest {
        val result = repository.executeDryRunRestore(
            snapshotId = "snap_prod_daily_01",
            targetEnv = RestoreTargetEnvironment.ISOLATED_RECOVERY_SANDBOX
        )
        assertTrue("Dry run should succeed", result.isSuccess)
        val job = result.getOrNull()!!
        assertTrue("Job must be marked as dry run", job.isDryRun)
        assertEquals(RestoreStatus.COMPLETED, job.status)
        assertEquals(RestoreTargetEnvironment.ISOLATED_RECOVERY_SANDBOX, job.targetEnvironment)
        assertTrue("Must contain dry run verification logs", job.logs.any { it.contains("Right to be Forgotten") })
    }

    @Test
    fun `project level restore recovers specific project without restoring whole cluster`() = runTest {
        val result = repository.restoreProjectFromSnapshot(
            snapshotId = "snap_prod_daily_01",
            projectId = "proj_test_101",
            targetWorkspaceId = "ws_test_99"
        )
        assertTrue("Project restore should succeed", result.isSuccess)
        val job = result.getOrNull()!!
        assertFalse("Must be actual restore job", job.isDryRun)
        assertEquals("proj_test_101", job.targetProjectId)
        assertEquals("ws_test_99", job.targetWorkspaceId)
        assertEquals(RestoreStatus.COMPLETED, job.status)
    }

    @Test
    fun `sensitive credentials and keys are sanitized from backup metadata`() {
        val rawMeta = mapOf(
            "snapshotId" to "snap_101",
            "environment" to "PROD",
            "firebaseApiKey" to "AIzaSySecretApiKey123",
            "serviceAccountJson" to "{private_key: 'secret'}",
            "databasePassword" to "SuperSecretPass123",
            "documentCount" to 14250
        )

        val sanitized = BackupDisasterRecoveryManager.sanitizeMetadataForAudit(rawMeta)
        assertEquals("snap_101", sanitized["snapshotId"])
        assertEquals("PROD", sanitized["environment"])
        assertEquals(14250, sanitized["documentCount"])
        assertEquals("[REDACTED_BY_CMEK_POLICY]", sanitized["firebaseApiKey"])
        assertEquals("[REDACTED_BY_CMEK_POLICY]", sanitized["serviceAccountJson"])
        assertEquals("[REDACTED_BY_CMEK_POLICY]", sanitized["databasePassword"])
    }

    @Test
    fun `RPO and RTO compliance checks validate SLAs correctly`() {
        val now = System.currentTimeMillis()
        val recentBackup = now - (15 * 60 * 1000) // 15 minutes ago
        val staleBackup = now - (90 * 60 * 1000) // 90 minutes ago

        assertTrue("15 minutes should be RPO compliant (target <= 60m)", BackupDisasterRecoveryManager.isRpoCompliant(recentBackup, 60))
        assertFalse("90 minutes should violate RPO SLA (target <= 60m)", BackupDisasterRecoveryManager.isRpoCompliant(staleBackup, 60))
    }

    @Test
    fun `viewModel filters snapshots and coordinates backup actions`() = runTest {
        advanceUntilIdle()
        viewModel.setEnvironmentFilter(BackupEnvironment.PROD)
        val state = viewModel.uiState.value
        assertEquals(BackupEnvironment.PROD, state.selectedEnvironment)
        assertTrue("Filtered snapshots should only contain PROD", state.filteredSnapshots.all { it.environment == BackupEnvironment.PROD })

        viewModel.triggerNewBackup(BackupType.METADATA_ONLY, BackupEnvironment.DEV, "تجربة ViewModel")
        advanceUntilIdle()
        val updatedState = viewModel.uiState.value
        assertNotNull("Banner message should announce completion", updatedState.bannerMessage)
    }
}
