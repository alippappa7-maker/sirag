package com.siraj.app.features.rights.domain.repository

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.Asset
import com.siraj.app.domain.models.RightsStatus
import kotlinx.coroutines.flow.Flow

interface RightsRepository {
    suspend fun updateAssetRights(
        assetId: String,
        updates: Map<String, Any?>,
    ): Resource<Unit>

    suspend fun logRightsDecision(
        assetId: String,
        reviewerId: String,
        previousStatus: RightsStatus,
        newStatus: RightsStatus,
        reason: String,
    ): Resource<Unit>

    fun getAssetsWithPendingRights(workspaceId: String): Flow<Resource<List<Asset>>>

    fun getAssetsWithExpiringRights(
        workspaceId: String,
        daysThreshold: Int = 30,
    ): Flow<Resource<List<Asset>>>

    suspend fun verifyRightsForExport(projectAssetIds: List<String>): Resource<Boolean>
}
