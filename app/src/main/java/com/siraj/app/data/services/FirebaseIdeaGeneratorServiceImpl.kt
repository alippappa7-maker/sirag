package com.siraj.app.data.services

import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.GeneratedIdea
import com.siraj.app.domain.models.IdeaGenerationRequest
import com.siraj.app.domain.models.RiskLevel
import com.siraj.app.domain.services.IdeaGeneratorService
import kotlinx.coroutines.tasks.await

class FirebaseIdeaGeneratorServiceImpl(
    // Using a specific region is recommended for Cloud Functions, e.g., europe-west3
    private val functions: FirebaseFunctions = Firebase.functions
) : IdeaGeneratorService {

    override suspend fun generateIdeas(request: IdeaGenerationRequest): Resource<List<GeneratedIdea>> {
        return try {
            val data = hashMapOf(
                "subject" to request.subject,
                "audience" to request.audience,
                "platform" to request.platform,
                "duration" to request.duration,
                "tone" to request.tone,
                "goal" to request.goal,
                "hasReligiousElement" to request.hasReligiousElement
            )

            // Calls the secure Cloud Function (timeout 60s)
            val result = functions.getHttpsCallable("generateIdeas")
                .withTimeout(60000L)
                .call(data)
                .await()

            val rawList = result.data as? List<Map<String, Any>> ?: emptyList()
            val ideas = rawList.map { map ->
                GeneratedIdea(
                    id = map["id"] as? String ?: "",
                    title = map["title"] as? String ?: "",
                    hook = map["hook"] as? String ?: "",
                    summary = map["summary"] as? String ?: "",
                    audience = map["audience"] as? String ?: "",
                    suggestedScenes = (map["suggestedScenes"] as? Number)?.toInt() ?: 0,
                    requiredSources = (map["requiredSources"] as? List<String>) ?: emptyList(),
                    riskLevel = RiskLevel.valueOf(map["riskLevel"] as? String ?: "HIGH"),
                    needsReview = map["needsReview"] as? Boolean ?: true,
                    disclaimer = map["disclaimer"] as? String
                )
            }

            Resource.Success(ideas)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "فشل الاتصال بخادم الذكاء الاصطناعي. تأكد من اتصالك بالإنترنت.")
        }
    }

    override suspend fun reportIdea(ideaId: String, reason: String): Resource<Unit> {
        return try {
            functions.getHttpsCallable("reportIdea").call(mapOf("ideaId" to ideaId, "reason" to reason)).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to report idea")
        }
    }
}
