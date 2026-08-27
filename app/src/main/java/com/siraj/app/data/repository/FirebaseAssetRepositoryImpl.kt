package com.siraj.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.Asset
import com.siraj.app.domain.models.AssetStatus
import com.siraj.app.domain.repository.AssetRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseAssetRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : AssetRepository {

    private val assetsCollection = firestore.collection("assets")

    override fun getProjectAssets(projectId: String): Flow<Resource<List<Asset>>> = callbackFlow {
        trySend(Resource.Loading)
        val registration = assetsCollection
            .whereEqualTo("projectId", projectId)
            .whereNotEqualTo("status", AssetStatus.DELETED.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to listen for assets"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val assets = snapshot.toObjects(Asset::class.java)
                    trySend(Resource.Success(assets))
                }
            }
        awaitClose { registration.remove() }
    }

    override suspend fun getAsset(assetId: String): Resource<Asset> {
        return try {
            val doc = assetsCollection.document(assetId).get().await()
            val asset = doc.toObject(Asset::class.java)
            if (asset != null) Resource.Success(asset) else Resource.Error("Asset not found")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun addAsset(asset: Asset): Resource<String> {
        return try {
            assetsCollection.document(asset.id).set(asset).await()
            Resource.Success(asset.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add asset")
        }
    }

    override suspend fun updateAsset(asset: Asset): Resource<Unit> {
        return try {
            assetsCollection.document(asset.id).set(asset).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update asset")
        }
    }

    override suspend fun deleteAsset(asset: Asset): Resource<Unit> {
        return try {
            // Delete from storage
            if (asset.storagePath.isNotEmpty()) {
                val ref = storage.reference.child(asset.storagePath)
                ref.delete().await()
            }
            // Mark as deleted or delete document
            assetsCollection.document(asset.id).update("status", AssetStatus.DELETED.name).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete asset")
        }
    }

    override fun uploadFile(path: String, bytes: ByteArray, mimeType: String): Flow<Resource<String>> = callbackFlow {
        trySend(Resource.Loading)
        val ref = storage.reference.child(path)
        
        val uploadTask = ref.putBytes(bytes)
        
        uploadTask.addOnProgressListener { taskSnapshot ->
            // Could emit progress here if Resource class supported it
            // val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
        }.addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { uri ->
                trySend(Resource.Success(uri.toString()))
                close()
            }.addOnFailureListener { e ->
                trySend(Resource.Error(e.message ?: "Failed to get download URL"))
                close()
            }
        }.addOnFailureListener { e ->
            trySend(Resource.Error(e.message ?: "Failed to upload file"))
            close()
        }
        
        awaitClose { 
            // uploadTask.cancel() if needed
        }
    }
}
