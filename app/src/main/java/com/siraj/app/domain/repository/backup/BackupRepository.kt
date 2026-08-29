package com.siraj.app.domain.repository.backup

import com.siraj.app.domain.models.backup.BackupEnvironment
import com.siraj.app.domain.models.backup.BackupRetentionPolicy
import com.siraj.app.domain.models.backup.BackupSnapshot
import com.siraj.app.domain.models.backup.BackupType
import com.siraj.app.domain.models.backup.DisasterRecoveryPlan
import com.siraj.app.domain.models.backup.RestoreJob
import com.siraj.app.domain.models.backup.RestoreTargetEnvironment
import kotlinx.coroutines.flow.Flow

interface BackupRepository {
    fun getBackupSnapshots(environment: BackupEnvironment? = null): Flow<List<BackupSnapshot>>
    fun getRestoreJobs(): Flow<List<RestoreJob>>
    fun getDisasterRecoveryPlan(): Flow<DisasterRecoveryPlan>
    fun getRetentionPolicy(): Flow<BackupRetentionPolicy>
    suspend fun triggerBackup(
        type: BackupType,
        environment: BackupEnvironment,
        notes: String
    ): Result<BackupSnapshot>
    suspend fun executeDryRunRestore(
        snapshotId: String,
        targetEnv: RestoreTargetEnvironment = RestoreTargetEnvironment.ISOLATED_RECOVERY_SANDBOX
    ): Result<RestoreJob>
    suspend fun restoreProjectFromSnapshot(
        snapshotId: String,
        projectId: String,
        targetWorkspaceId: String
    ): Result<RestoreJob>
    suspend fun getDeletedUsersTombstoneCount(): Int
}
