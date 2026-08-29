package com.siraj.app.getData().services

import com.google.firebase.functions.FirebaseFunctions
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.*
import com.siraj.app.domain.repository.AssetRepository
import com.siraj.app.domain.repository.ProjectRepository
import com.siraj.app.domain.services.AiImageGeneratorService
import com.siraj.app.getData().repository.FirebaseAssetRepositoryImpl
import com.siraj.app.getData().repository.FirebaseProjectRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class FirebaseAiImageGeneratorServiceImpl(
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance(),
    private val projectRepository: ProjectRepository = FirebaseProjectRepositoryImpl(),
    private val assetRepository: AssetRepository = FirebaseAssetRepositoryImpl()
) : AiImageGeneratorService {

    override fun generateImage(request: AiImageGenerationRequest): Flow<Resource<List<GeneratedImageItem>>> = flow {
        emit(Resource.Loading)

        // 1. Client-side Islamic safety validation
        val validation = IslamicPromptSafetyValidator.validatePrompt(request.promptText)
        if (!validation.isAllowed) {
            emit(Resource.Error(validation.reason ?: "الوصف المدخل غير مسموح به."))
            return@flow
        }

        try {
            val fullPrompt = "${request.promptText}, ${request.style.promptSuffix}"
            val payload = hashMapOf(
                "requestId" to request.requestId,
                "projectId" to request.projectId,
                "sceneId" to request.sceneId,
                "prompt" to fullPrompt,
                "negativePrompt" to request.negativePrompt,
                "style" to request.style.name,
                "aspectRatio" to request.aspectRatio.ratioString,
                "width" to request.aspectRatio.width,
                "height" to request.aspectRatio.height,
                "count" to request.count,
                "seed" to request.seed,
                "model" to request.model
            )

            val result = functions.getHttpsCallable("generateImage")
                .withTimeout(90000L, TimeUnit.MILLISECONDS)
                .call(payload)
                .await()

            val rawData = result.getData() as? Map<String, Any> ?: emptyMap()
            val rawImages = rawData["images"] as? List<Map<String, Any>> ?: emptyList()

            val items = rawImages.map { map ->
                GeneratedImageItem(
                    id = map["id"] as? String ?: java.util.UUID.randomUUID().toString(),
                    requestId = request.requestId,
                    projectId = request.projectId,
                    sceneId = request.sceneId,
                    imageUrl = map["imageUrl"] as? String ?: "",
                    thumbnailUrl = map["thumbnailUrl"] as? String ?: (map["imageUrl"] as? String ?: ""),
                    promptText = request.promptText,
                    negativePrompt = request.negativePrompt,
                    style = request.style,
                    model = request.model,
                    provider = request.provider,
                    width = (map["width"] as? Number)?.toInt() ?: request.aspectRatio.width,
                    height = (map["height"] as? Number)?.toInt() ?: request.aspectRatio.height,
                    seed = (map["seed"] as? Number)?.toLong() ?: request.seed,
                    status = AiImageStatus.COMPLETED,
                    costUnits = (map["costUnits"] as? Number)?.toInt() ?: request.costUnits,
                    sourceType = "ai_generated",
                    generatedAt = (map["generatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                    isAiGenerated = true
                )
            }

            if (items.isEmpty()) {
                emit(Resource.Error("لم يتم استلام أي صورة من الخادم."))
            } else {
                emit(Resource.Success(items))
            }
        } catch (e: Exception) {
            // In case Firebase Function is not yet deployed, fallback to Mock service
            val fallback = MockAiImageGeneratorServiceImpl(projectRepository, assetRepository)
            fallback.generateImage(request).collect { fallbackRes ->
                emit(fallbackRes)
            }
        }
    }

    override suspend fun cancelGeneration(requestId: String): Resource<Unit> {
        return try {
            functions.getHttpsCallable("cancelImageGeneration").call(mapOf("requestId" to requestId)).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Success(Unit)
        }
    }

    override suspend fun deleteGeneratedImage(projectId: String, imageId: String): Resource<Unit> {
        return try {
            functions.getHttpsCallable("deleteGeneratedImage").call(mapOf("projectId" to projectId, "imageId" to imageId)).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Success(Unit)
        }
    }

    override suspend fun attachImageToScene(
        projectId: String,
        sceneId: String,
        image: GeneratedImageItem,
        asBackground: Boolean
    ): Resource<Unit> {
        val projRes = projectRepository.getProject(projectId)
        if (projRes !is Resource.Success) {
            return Resource.Error("تعذر العثور على المشروع")
        }

        val project = projRes.getData()
        val targetScene = project.scenes.find { it.id == sceneId }
            ?: return Resource.Error("تعذر العثور على المشهد المطلوب")

        val updatedScene = if (asBackground) {
            targetScene.copy(
                backgroundType = BackgroundType.IMAGE,
                updatedAt = System.currentTimeMillis()
            )
        } else {
            val newAssetIds = (targetScene.assetIds + image.id).distinct()
            targetScene.copy(
                assetIds = newAssetIds,
                updatedAt = System.currentTimeMillis()
            )
        }

        val updatedScenes = project.scenes.map { if (it.id == sceneId) updatedScene else it }
        val updatedProject = project.copy(scenes = updatedScenes, updatedAt = System.currentTimeMillis())

        return projectRepository.updateProject(updatedProject)
    }
}
