package com.siraj.app.features.migration

import com.siraj.app.core.utils.Resource
import com.siraj.app.data.repository.migration.MigrationRepositoryImpl
import com.siraj.app.domain.models.migration.MigrationStatus
import com.siraj.app.features.migration.domain.MigrationEngine
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MigrationTest {
    private lateinit var repository: MigrationRepositoryImpl
    private lateinit var engine: MigrationEngine

    @Before
    fun setup() {
        repository = MigrationRepositoryImpl()
        engine = MigrationEngine(repository)
    }

    @Test
    fun `test dry run migration does not alter actual data`() =
        runTest {
            // Create Dry Run Job
            val jobRes = repository.createMigrationJob(2, listOf("users"), isDryRun = true)
            val job = (jobRes as Resource.Success).data

            // Check baseline data
            val initialDocs = (repository.getOutdatedDocuments("users", 2, 100, null) as Resource.Success).data
            assertEquals(2, initialDocs.size)
            assertEquals(1, initialDocs[0].schemaVersion)

            // Run Engine
            val finalJobRes = engine.runMigrationJob(job.jobId)
            val finalJob = (finalJobRes as Resource.Success).data

            assertEquals(MigrationStatus.COMPLETED, finalJob.status)
            assertEquals(2, finalJob.successCount) // It successfully processed 2

            // Verify actual data remains unchanged (because it was a dry run)
            val afterDryRunDocs = (repository.getOutdatedDocuments("users", 2, 100, null) as Resource.Success).data
            assertEquals(2, afterDryRunDocs.size) // Still outdated
        }

    @Test
    fun `test real migration alters data and backward compatibility is preserved`() =
        runTest {
            // Create Real Job
            val jobRes = repository.createMigrationJob(2, listOf("users"), isDryRun = false)
            val job = (jobRes as Resource.Success).data

            // Run Engine
            val finalJobRes = engine.runMigrationJob(job.jobId)
            val finalJob = (finalJobRes as Resource.Success).data

            assertEquals(MigrationStatus.COMPLETED, finalJob.status)
            assertEquals(2, finalJob.successCount)

            // Verify actual data is updated
            val afterMigrationDocs = (repository.getOutdatedDocuments("users", 2, 100, null) as Resource.Success).data
            assertEquals(0, afterMigrationDocs.size) // No outdated docs left

            // Fetch to check transformation rules (we'll fetch V3 to get all V2s as outdated for viewing)
            val v2Docs = (repository.getOutdatedDocuments("users", 3, 100, null) as Resource.Success).data

            val aliDoc = v2Docs.find { it.documentId == "user1" }!!
            assertEquals(2, aliDoc.schemaVersion)

            // Rule: Do not delete old fields, do not change meaning.
            assertEquals("Ali", aliDoc.data["name"]) // Old field preserved
            assertEquals("Ali", aliDoc.data["fullName"]) // New field added
        }

    @Test
    fun `test concurrent migration lock prevents multiple jobs`() =
        runTest {
            val job1Res = repository.createMigrationJob(2, listOf("users"), false)
            val job2Res = repository.createMigrationJob(2, listOf("users"), false)

            val job1 = (job1Res as Resource.Success).data
            val job2 = (job2Res as Resource.Success).data

            repository.acquireMigrationLock(job1.jobId) // Job 1 gets lock

            // Engine attempt on Job 2 should fail
            val finalJob2Res = engine.runMigrationJob(job2.jobId)
            assertTrue(finalJob2Res is Resource.Error)
            assertEquals(
                "Could not acquire lock: Migration lock already held by another job: ${job1.jobId}",
                (finalJob2Res as Resource.Error).message,
            )
        }
}
