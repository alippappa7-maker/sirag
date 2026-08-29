package com.siraj.app.domain.repository.privacy

import android.content.Context
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.privacy.*
import kotlinx.coroutines.flow.Flow
import java.io.File

interface PrivacyRepository {
    fun observePrivacyOverview(userId: String): Flow<PrivacyOverviewData>
    suspend fun getPrivacyOverview(userId: String): PrivacyOverviewData
    suspend fun generateUserDataExport(userId: String): Resource<UserDataExportPackage>
    suspend fun exportUserDataToJson(userId: String): Resource<String>
    suspend fun saveExportJsonToFile(context: Context, json: String): Resource<File>
    suspend fun clearWatchHistory(userId: String): Resource<Unit>
    suspend fun clearDownloads(userId: String): Resource<Unit>
    suspend fun clearLocalCache(context: Context): Resource<Long>
    suspend fun deleteUserProject(projectId: String): Resource<Unit>
    suspend fun requestAccountDeletion(userId: String, reason: String, gracePeriodDays: Int = 14): Resource<AccountDeletionRequest>
    suspend fun cancelAccountDeletion(userId: String): Resource<Unit>
    suspend fun submitDataCorrection(request: DataCorrectionRequest): Resource<Unit>
    fun getStandardRetentionPolicies(): List<StoredDataCategory>
}
