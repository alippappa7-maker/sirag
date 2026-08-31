package com.siraj.app.domain.repository.migration

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.migration.DocumentSchemaState
import com.siraj.app.domain.models.migration.MigrationJob

interface MigrationRepository {
    suspend fun acquireMigrationLock(jobId: String): Resource<Boolean>

    suspend fun releaseMigrationLock(jobId: String): Resource<Unit>

    suspend fun verifyPreMigrationBackup(): Resource<Boolean>

    suspend fun createMigrationJob(
        targetVersion: Int,
        collections: List<String>,
        isDryRun: Boolean,
    ): Resource<MigrationJob>

    suspend fun updateMigrationJob(job: MigrationJob): Resource<Unit>

    suspend fun getMigrationJob(jobId: String): Resource<MigrationJob>

    suspend fun getOutdatedDocuments(
        collectionName: String,
        targetVersion: Int,
        limit: Int,
        lastDocumentId: String?,
    ): Resource<List<DocumentSchemaState>>

    // Batch process returns the number of successfully migrated documents and a list of failed ones with reasons
    suspend fun applyMigrationBatch(
        collectionName: String,
        migratedDocuments: List<DocumentSchemaState>,
        isDryRun: Boolean,
    ): Resource<Pair<Int, List<Pair<String, String>>>> // <SuccessCount, List<DocId, ErrorReason>>
}
