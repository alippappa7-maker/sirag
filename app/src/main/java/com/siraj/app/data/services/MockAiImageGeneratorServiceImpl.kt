package com.siraj.app.data.services

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.*
import com.siraj.app.domain.repository.AssetRepository
import com.siraj.app.domain.repository.ProjectRepository
import com.siraj.app.domain.services.AiImageGeneratorService
import com.siraj.app.data.repository.FirebaseAssetRepositoryImpl
import com.siraj.app.data.repository.FirebaseProjectRepositoryImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class MockAiImageGeneratorServiceImpl(
    private val projectRepository: ProjectRepository = FirebaseProjectRepositoryImpl(),
    private val assetRepository: AssetRepository = FirebaseAssetRepositoryImpl()
) : AiImageGeneratorService {

    private val cancelledRequests = ConcurrentHashMap.newKeySet<String>()
    private val generatedImagesCache = ConcurrentHashMap<String, MutableList<GeneratedImageItem>>()

    override fun generateImage(request: AiImageGenerationRequest): Flow<Resource<List<GeneratedImageItem>>> = flow {
        emit(Resource.Loading)
        
        // 1. Client-side Islamic safety validation
        val validation = IslamicPromptSafetyValidator.validatePrompt(request.promptText)
        if (!validation.isAllowed) {
            emit(Resource.Error(validation.reason ?: "الوصف المدخل غير مسموح به وفق الضوابط الشرعية."))
            return@flow
        }

        // 2. Simulated Server Queue state
        delay(800)
        if (cancelledRequests.contains(request.requestId)) {
            emit(Resource.Error("تم إلغاء الطلب من قبل المستخدم."))
            return@flow
        }

        // 3. Simulated Processing state
        delay(1200)
        if (cancelledRequests.contains(request.requestId)) {
            emit(Resource.Error("تم إلغاء الطلب من قبل المستخدم."))
            return@flow
        }

        val results = mutableListOf<GeneratedImageItem>()
        val baseSeed = request.seed ?: System.currentTimeMillis()

        for (i in 1..request.count) {
            val itemSeed = baseSeed + i
            val encodedPrompt = request.promptText.replace(" ", "_").take(20)
            val imgUrl = "https://picsum.photos/seed/$encodedPrompt$itemSeed/${request.aspectRatio.width}/${request.aspectRatio.height}"
            val thumbUrl = "https://picsum.photos/seed/$encodedPrompt$itemSeed/400/300"

            val item = GeneratedImageItem(
                id = UUID.randomUUID().toString(),
                requestId = request.requestId,
                projectId = request.projectId,
                sceneId = request.sceneId,
                imageUrl = imgUrl,
                thumbnailUrl = thumbUrl,
                promptText = request.promptText,
                negativePrompt = request.negativePrompt,
                style = request.style,
                model = request.model,
                provider = request.provider,
                width = request.aspectRatio.width,
                height = request.aspectRatio.height,
                seed = itemSeed,
                status = AiImageStatus.COMPLETED,
                costUnits = request.costUnits,
                sourceType = "ai_generated",
                generatedAt = System.currentTimeMillis(),
                isAiGenerated = true,
                licenseNotice = "مولد بالذكاء الاصطناعي (Imagen 3) - مخصص لإنتاج المحتوى البصري"
            )
            results.add(item)
        }

        // Cache results for project
        val projectImages = generatedImagesCache.getOrPut(request.projectId) { mutableListOf() }
        projectImages.addAll(results)

        // Automatically save to AssetRepository with status READY and AI tags
        results.forEach { img ->
            val asset = Asset(
                ownerId = "user_mock",
                workspaceId = "workspace_mock",
                projectId = request.projectId,
                type = AssetType.IMAGE,
                storagePath = "ai_generated/${img.id}.jpg",
                downloadUrl = img.imageUrl,
                thumbnailUrl = img.thumbnailUrl,
                mimeType = "image/jpeg",
                sizeBytes = 1024 * 1024 * 2L, // 2MB approximate
                sourceUrl = "AI Model: ${img.model}",
                license = "AI Generated (Safe Commercial Use)",
                attribution = "تم التوليد بواسطة ${img.provider} عبر منصة سراج",
                status = AssetStatus.READY
            )
            assetRepository.addAsset(asset)
        }

        emit(Resource.Success(results))
    }

    override suspend fun cancelGeneration(requestId: String): Resource<Unit> {
        cancelledRequests.add(requestId)
        return Resource.Success(Unit)
    }

    override suspend fun deleteGeneratedImage(projectId: String, imageId: String): Resource<Unit> {
        val list = generatedImagesCache[projectId]
        list?.removeAll { it.id == imageId }
        return Resource.Success(Unit)
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

        val project = projRes.data
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
