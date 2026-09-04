package com.siraj.app.data.repository.migration

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.migration.DocumentSchemaState
import com.siraj.app.domain.models.migration.MigrationJob
import com.siraj.app.domain.repository.migration.MigrationRepository
import kotlinx.coroutines.delay
import java.util.UUID

class MigrationRepositoryImpl : MigrationRepository {
    private val jobs = mutableMapOf<String, MigrationJob>()
    private var activeLock: String? = null

    // In-memory document storage
    private val inMemoryStore = mutableMapOf<String, MutableMap<String, DocumentSchemaState>>()

    override suspend fun acquireMigrationLock(jobId: String): Resource<Boolean> {
        delay(100)
        if (activeLock != null && activeLock != jobId) {
            return Resource.Error("Migration lock already held by another job: $activeLock")
        }
        activeLock = jobId
        return Resource.Success(true)
    }

    override suspend fun releaseMigrationLock(jobId: String): Resource<Unit> {
        if (activeLock == jobId) {
            activeLock = null
        }
        return Resource.Success(Unit)
    }

    override suspend fun verifyPreMigrationBackup(): Resource<Boolean> {
        // Simulate checking if a daily backup was successfully created in Cloud Storage
        delay(200)
        return Resource.Success(true)
    }

    override suspend fun createMigrationJob(
        targetVersion: Int,
        collections: List<String>,
        isDryRun: Boolean,
    ): Resource<MigrationJob> {
        val job =
            MigrationJob(
                jobId = "mig_${UUID.randomUUID()}",
                targetVersion = targetVersion,
                collections = collections,
                isDryRun = isDryRun,
            )
        jobs[job.jobId] = job
        return Resource.Success(job)
    }

    override suspend fun updateMigrationJob(job: MigrationJob): Resource<Unit> {
        jobs[job.jobId] = job
        return Resource.Success(Unit)
    }

    override suspend fun getMigrationJob(jobId: String): Resource<MigrationJob> =
        jobs[jobId]?.let { Resource.Success(it) }
            ?: Resource.Error("Job not found")

    override suspend fun getOutdatedDocuments(
        collectionName: String,
        targetVersion: Int,
        limit: Int,
        lastDocumentId: String?,
    ): Resource<List<DocumentSchemaState>> {
        val collection = inMemoryStore[collectionName] ?: return Resource.Success(emptyList())

        val outdated =
            collection.values
                .filter { it.schemaVersion < targetVersion }
                .sortedBy { it.documentId }

        // Pagination logic simulation
        val startIndex =
            if (lastDocumentId != null) {
                val idx = outdated.indexOfFirst { it.documentId == lastDocumentId }
                if (idx == -1) 0 else idx + 1
            } else {
                0
            }

        val paged = outdated.drop(startIndex).take(limit)
        return Resource.Success(paged)
    }

    override suspend fun applyMigrationBatch(
        collectionName: String,
        migratedDocuments: List<DocumentSchemaState>,
        isDryRun: Boolean,
    ): Resource<Pair<Int, List<Pair<String, String>>>> {
        var successes = 0
        val failures = mutableListOf<Pair<String, String>>()

        val collection = inMemoryStore.getOrPut(collectionName) { mutableMapOf() }

        for (doc in migratedDocuments) {
            try {
                if (!isDryRun) {
                    // Simulate save to DB
                    collection[doc.documentId] = doc
                }
                successes++
            } catch (e: Exception) {
                failures.add(doc.documentId to (e.message ?: "Unknown error"))
            }
        }

        return Resource.Success(Pair(successes, failures))
    }
}
