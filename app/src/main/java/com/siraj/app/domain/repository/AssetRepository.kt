package com.siraj.app.domain.repository

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.Asset
import kotlinx.coroutines.flow.Flow

interface AssetRepository {
    fun getProjectAssets(projectId: String): Flow<Resource<List<Asset>>>

    suspend fun getAsset(assetId: String): Resource<Asset>

    suspend fun addAsset(asset: Asset): Resource<String>

    suspend fun updateAsset(asset: Asset): Resource<Unit>

    suspend fun deleteAsset(asset: Asset): Resource<Unit>

    // Simulating file upload (takes bytes, returns a Flow of progress or final Resource)
    fun uploadFile(
        path: String,
        bytes: ByteArray,
        mimeType: String,
    ): Flow<Resource<String>>
}
