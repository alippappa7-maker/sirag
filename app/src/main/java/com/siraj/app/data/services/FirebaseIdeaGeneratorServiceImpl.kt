package com.siraj.app.data.services

import com.google.firebase.functions.FirebaseFunctions
import java.util.concurrent.TimeUnit
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.GeneratedIdea
import com.siraj.app.domain.models.IdeaGenerationRequest
import com.siraj.app.domain.models.RiskLevel
import com.siraj.app.domain.models.SuggestedScene
import com.siraj.app.domain.models.SceneType
import com.siraj.app.domain.models.ScenarioPlan
import com.siraj.app.domain.models.ScenarioScene
import com.siraj.app.domain.services.IdeaGeneratorService
import kotlinx.coroutines.tasks.await

class FirebaseIdeaGeneratorServiceImpl(
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance(),
) : IdeaGeneratorService {
    override suspend fun generateIdeas(request: IdeaGenerationRequest): Resource<List<GeneratedIdea>> =
        try {
            val data =
                hashMapOf(
                    "subject" to request.subject,
                    "audience" to request.audience,
                    "platform" to request.platform,
                    "duration" to request.duration,
                    "tone" to request.tone,
                    "goal" to request.goal,
                    "hasReligiousElement" to request.hasReligiousElement,
                )

            val result =
                functions
                    .getHttpsCallable("generateIdeas")
                    .withTimeout(60000L, TimeUnit.MILLISECONDS)
                    .call(data)
                    .await()

            val rawList = result.getData() as? List<Map<String, Any>> ?: emptyList()
            val ideas =
                rawList.map { map ->
                    val rawScenes = map["suggestedScenes"]
                    val scenes = when (rawScenes) {
                        is List<*> -> rawScenes.filterIsInstance<Map<String, Any>>().map { s ->
                            SuggestedScene(
                                order = (s["order"] as? Number)?.toInt() ?: 0,
                                title = s["title"] as? String ?: "",
                                description = s["description"] as? String ?: "",
                                durationSeconds = (s["durationSeconds"] as? Number)?.toInt() ?: 0,
                                type = try { SceneType.valueOf(s["type"] as? String ?: "NARRATION") } catch (_: Exception) { SceneType.NARRATION },
                            )
                        }
                        is Number -> {
                            val count = rawScenes.toInt()
                            (1..count).map { SuggestedScene(order = it, title = "مشهد $it") }
                        }
                        else -> emptyList()
                    }
                    GeneratedIdea(
                        id = map["id"] as? String ?: "",
                        title = map["title"] as? String ?: "",
                        hook = map["hook"] as? String ?: "",
                        summary = map["summary"] as? String ?: "",
                        audience = map["audience"] as? String ?: "",
                        suggestedScenes = scenes,
                        requiredSources = (map["requiredSources"] as? List<String>) ?: emptyList(),
                        riskLevel = RiskLevel.valueOf(map["riskLevel"] as? String ?: "HIGH"),
                        needsReview = map["needsReview"] as? Boolean ?: true,
                        disclaimer = map["disclaimer"] as? String,
                        platform = map["platform"] as? String ?: "",
                        tone = map["tone"] as? String ?: "",
                        estimatedDuration = map["estimatedDuration"] as? String ?: "",
                        tags = (map["tags"] as? List<String>) ?: emptyList(),
                    )
                }

            Resource.Success(ideas)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "فشل الاتصال بخادم الذكاء الاصطناعي. تأكد من اتصالك بالإنترنت.")
        }

    override suspend fun generateScenario(
        idea: GeneratedIdea,
        request: IdeaGenerationRequest,
    ): Resource<ScenarioPlan> =
        try {
            val data =
                hashMapOf(
                    "ideaId" to idea.id,
                    "ideaTitle" to idea.title,
                    "ideaSummary" to idea.summary,
                    "hook" to idea.hook,
                    "audience" to idea.audience,
                    "platform" to request.platform,
                    "duration" to request.duration,
                    "tone" to request.tone,
                )

            val result =
                functions
                    .getHttpsCallable("generateScenario")
                    .withTimeout(90000L, TimeUnit.MILLISECONDS)
                    .call(data)
                    .await()

            val map = result.getData() as? Map<String, Any> ?: emptyMap()
            val rawScenes = map["scenes"] as? List<Map<String, Any>> ?: emptyList()
            val scenes = rawScenes.map { s ->
                ScenarioScene(
                    id = s["id"] as? String ?: "",
                    order = (s["order"] as? Number)?.toInt() ?: 0,
                    title = s["title"] as? String ?: "",
                    narration = s["narration"] as? String ?: "",
                    visualDescription = s["visualDescription"] as? String ?: "",
                    durationSeconds = (s["durationSeconds"] as? Number)?.toInt() ?: 0,
                    type = try { SceneType.valueOf(s["type"] as? String ?: "NARRATION") } catch (_: Exception) { SceneType.NARRATION },
                    transitions = s["transitions"] as? String ?: "",
                    overlayText = s["overlayText"] as? String,
                    arabicText = s["arabicText"] as? String,
                    arabicSource = s["arabicSource"] as? String,
                )
            }

            val plan =
                ScenarioPlan(
                    ideaId = idea.id,
                    title = map["title"] as? String ?: idea.title,
                    overview = map["overview"] as? String ?: idea.summary,
                    scenes = scenes,
                    totalDurationSeconds = (map["totalDurationSeconds"] as? Number)?.toInt() ?: 0,
                    openingHook = map["openingHook"] as? String ?: idea.hook,
                    closingCTA = map["closingCTA"] as? String ?: "",
                    visualStyle = map["visualStyle"] as? String ?: "",
                    musicMood = map["musicMood"] as? String ?: "",
                )

            Resource.Success(plan)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "فشل توليد السيناريو. تأكد من اتصالك بالإنترنت.")
        }

    override suspend fun reportIdea(
        ideaId: String,
        reason: String,
    ): Resource<Unit> =
        try {
            functions.getHttpsCallable("reportIdea").call(mapOf("ideaId" to ideaId, "reason" to reason)).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to report idea")
        }
}
