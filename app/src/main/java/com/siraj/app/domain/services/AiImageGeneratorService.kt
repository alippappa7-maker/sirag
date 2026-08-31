package com.siraj.app.domain.services

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.AiImageGenerationRequest
import com.siraj.app.domain.models.GeneratedImageItem
import kotlinx.coroutines.flow.Flow

interface AiImageGeneratorService {
    fun generateImage(request: AiImageGenerationRequest): Flow<Resource<List<GeneratedImageItem>>>

    suspend fun cancelGeneration(requestId: String): Resource<Unit>

    suspend fun deleteGeneratedImage(
        projectId: String,
        imageId: String,
    ): Resource<Unit>

    suspend fun attachImageToScene(
        projectId: String,
        sceneId: String,
        image: GeneratedImageItem,
        asBackground: Boolean,
    ): Resource<Unit>
}
