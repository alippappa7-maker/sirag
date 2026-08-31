package com.siraj.app.features.migration.domain

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.migration.*
import com.siraj.app.domain.repository.migration.MigrationRepository
import kotlinx.coroutines.delay

class MigrationEngine(
    private val repository: MigrationRepository,
) {
    // Defines the transformation logic for a specific version bump
    fun migrateDocumentToV2(oldDoc: DocumentSchemaState): DocumentSchemaState {
        val newData = oldDoc.data.toMutableMap()

        // Rules: Do not delete old fields, do not change meaning.
        // Example: V1 had 'name', V2 uses 'fullName', but we keep 'name' for backward compatibility.
        if (newData.containsKey("name") && !newData.containsKey("fullName")) {
            newData["fullName"] = newData["name"] as String
        }

        return oldDoc.copy(
            schemaVersion = 2,
            data = newData,
        )
    }

    suspend fun runMigrationJob(jobId: String): Resource<MigrationJob> {
        val lockRes = repository.acquireMigrationLock(jobId)
        if (lockRes is Resource.Error) return Resource.Error("Could not acquire lock: ${lockRes.message}")

        var jobRes = repository.getMigrationJob(jobId)
        if (jobRes !is Resource.Success) {
            repository.releaseMigrationLock(jobId)
            return Resource.Error("Job not found")
        }

        var job = jobRes.data

        if (!job.isDryRun) {
            val backupRes = repository.verifyPreMigrationBackup()
            if (backupRes !is Resource.Success || !backupRes.data) {
                repository.releaseMigrationLock(jobId)
                return Resource.Error("Pre-migration backup missing. Aborting for safety.")
            }
        }

        job = job.copy(status = MigrationStatus.RUNNING, startedAt = System.currentTimeMillis())
        repository.updateMigrationJob(job)

        for (collection in job.collections) {
            var hasMore = true
            var lastDocId: String? = null
            var retryCount = 0
            val maxRetries = 3

            while (hasMore) {
                // Fetch outdated documents in batches
                val outdatedDocsRes = repository.getOutdatedDocuments(collection, job.targetVersion, job.batchSize, lastDocId)

                if (outdatedDocsRes !is Resource.Success) {
                    if (retryCount < maxRetries) {
                        retryCount++
                        delay(1000L * retryCount) // exponential backoff
                        continue
                    } else {
                        job =
                            job.copy(
                                status = MigrationStatus.FAILED,
                                completedAt = System.currentTimeMillis(),
                            )
                        repository.updateMigrationJob(job)
                        repository.releaseMigrationLock(jobId)
                        return Resource.Error("Failed to fetch documents from $collection after retries.")
                    }
                }

                val outdatedDocs = outdatedDocsRes.data
                if (outdatedDocs.isEmpty()) {
                    hasMore = false
                    break
                }

                job = job.copy(totalDocumentsFound = job.totalDocumentsFound + outdatedDocs.size)

                // Transform documents
                val migratedDocs =
                    outdatedDocs.map {
                        if (job.targetVersion == 2) migrateDocumentToV2(it) else it
                    }

                // Apply batch
                val batchRes = repository.applyMigrationBatch(collection, migratedDocs, job.isDryRun)

                if (batchRes is Resource.Success) {
                    val (successes, failures) = batchRes.data

                    val newErrors =
                        failures.map { (docId, reason) ->
                            MigrationError(docId, collection, reason, System.currentTimeMillis())
                        }

                    job =
                        job.copy(
                            processedCount = job.processedCount + outdatedDocs.size,
                            successCount = job.successCount + successes,
                            failureCount = job.failureCount + failures.size,
                            errors = job.errors + newErrors,
                        )
                    repository.updateMigrationJob(job)

                    lastDocId = outdatedDocs.last().documentId
                    retryCount = 0 // Reset retries after success
                } else {
                    // Batch application failed
                    if (retryCount < maxRetries) {
                        retryCount++
                        delay(2000L)
                        continue
                    } else {
                        job =
                            job.copy(
                                status = MigrationStatus.FAILED,
                                completedAt = System.currentTimeMillis(),
                            )
                        repository.updateMigrationJob(job)
                        repository.releaseMigrationLock(jobId)
                        return Resource.Error("Failed to apply batch to $collection after retries.")
                    }
                }
            }
        }

        job =
            job.copy(
                status = MigrationStatus.COMPLETED,
                completedAt = System.currentTimeMillis(),
            )
        repository.updateMigrationJob(job)
        repository.releaseMigrationLock(jobId)

        return Resource.Success(job)
    }
}
