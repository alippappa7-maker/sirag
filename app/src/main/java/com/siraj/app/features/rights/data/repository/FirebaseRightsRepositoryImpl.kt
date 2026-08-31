package com.siraj.app.features.rights.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.Asset
import com.siraj.app.domain.models.RightsStatus
import com.siraj.app.features.rights.domain.repository.RightsRepository
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import java.util.UUID

class FirebaseRightsRepositoryImpl(
    private val firestore: FirebaseFirestore,
) : RightsRepository {
    override suspend fun updateAssetRights(
        assetId: String,
        updates: Map<String, Any?>,
    ): Resource<Unit> =
        try {
            firestore
                .collection("assets")
                .document(assetId)
                .update(updates)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update asset rights")
        }

    override suspend fun logRightsDecision(
        assetId: String,
        reviewerId: String,
        previousStatus: RightsStatus,
        newStatus: RightsStatus,
        reason: String,
    ): Resource<Unit> =
        try {
            val logData =
                hashMapOf(
                    "id" to UUID.randomUUID().toString(),
                    "assetId" to assetId,
                    "reviewerId" to reviewerId,
                    "previousStatus" to previousStatus.name,
                    "newStatus" to newStatus.name,
                    "reason" to reason,
                    "timestamp" to System.currentTimeMillis(),
                )
            firestore
                .collection("rights_decisions")
                .document(logData["id"] as String)
                .set(logData)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to log rights decision")
        }

    override fun getAssetsWithPendingRights(workspaceId: String): Flow<Resource<List<Asset>>> =
        flow<Resource<List<Asset>>> {
            emit(Resource.Loading)
            val snapshot =
                firestore
                    .collection("assets")
                    .whereEqualTo("workspaceId", workspaceId)
                    .whereEqualTo("rightsStatus", RightsStatus.PENDING_VERIFICATION.name)
                    .get()
                    .await()
            val assets = snapshot.documents.mapNotNull { it.toObject(Asset::class.java) }
            emit(Resource.Success(assets))
        }.catch { emit(Resource.Error(it.message ?: "Failed to fetch pending assets")) }

    override fun getAssetsWithExpiringRights(
        workspaceId: String,
        daysThreshold: Int,
    ): Flow<Resource<List<Asset>>> =
        flow<Resource<List<Asset>>> {
            emit(Resource.Loading)
            val thresholdMillis = System.currentTimeMillis() + (daysThreshold * 24L * 60 * 60 * 1000)
            val snapshot =
                firestore
                    .collection("assets")
                    .whereEqualTo("workspaceId", workspaceId)
                    .whereLessThanOrEqualTo("expiresAt", thresholdMillis)
                    .get()
                    .await()
            val assets = snapshot.documents.mapNotNull { it.toObject(Asset::class.java) }
            emit(Resource.Success(assets))
        }.catch { emit(Resource.Error(it.message ?: "Failed to fetch expiring assets")) }

    override suspend fun verifyRightsForExport(projectAssetIds: List<String>): Resource<Boolean> {
        return try {
            if (projectAssetIds.isEmpty()) return Resource.Success(true)
            val snapshot =
                firestore
                    .collection("assets")
                    .whereIn("id", projectAssetIds)
                    .get()
                    .await()
            val assets = snapshot.documents.mapNotNull { it.toObject(Asset::class.java) }
            val hasUnknownRights =
                assets.any {
                    it.rightsStatus == RightsStatus.UNKNOWN ||
                        it.rightsStatus == RightsStatus.REJECTED ||
                        it.rightsStatus == RightsStatus.EXPIRED
                }
            if (hasUnknownRights) {
                Resource.Error("بعض الأصول المستخدمة غير مصرح بها أو منتهية الصلاحية.")
            } else {
                Resource.Success(true)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to verify rights")
        }
    }
}
