package com.siraj.app.domain.models.migration

enum class MigrationStatus {
    PENDING,
    RUNNING,
    PAUSED,
    COMPLETED,
    FAILED,
    ROLLED_BACK,
}

data class MigrationError(
    val documentId: String,
    val collectionName: String,
    val errorReason: String,
    val timestamp: Long,
)

data class MigrationJob(
    val jobId: String,
    val targetVersion: Int,
    val collections: List<String>,
    val status: MigrationStatus = MigrationStatus.PENDING,
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val isDryRun: Boolean = false,
    val batchSize: Int = 100, // Firestore recommended batch size limit is 500, we use 100 for safety
    val totalDocumentsFound: Int = 0,
    val processedCount: Int = 0,
    val successCount: Int = 0,
    val failureCount: Int = 0,
    val errors: List<MigrationError> = emptyList(),
    val hasBackupVerified: Boolean = false,
)

data class DocumentSchemaState(
    val documentId: String,
    val collectionName: String,
    val schemaVersion: Int,
    val data: Map<String, Any>,
)

object SchemaConstants {
    const val CURRENT_VERSION = 2

    val ALL_COLLECTIONS =
        listOf(
            "users",
            "workspaces",
            "projects",
            "scenes",
            "assets",
            "sources",
            "reviews",
            "flashes",
            "audio",
            "subscriptions",
            "credits",
            "notifications",
            "auditLogs",
        )
}
