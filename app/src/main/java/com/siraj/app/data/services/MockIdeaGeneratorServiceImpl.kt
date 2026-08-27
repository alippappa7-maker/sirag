package com.siraj.app.data.services

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.GeneratedIdea
import com.siraj.app.domain.models.IdeaGenerationRequest
import com.siraj.app.domain.models.RiskLevel
import com.siraj.app.domain.services.IdeaGeneratorService
import kotlinx.coroutines.delay
import java.util.UUID

class MockIdeaGeneratorServiceImpl : IdeaGeneratorService {
    override suspend fun generateIdeas(request: IdeaGenerationRequest): Resource<List<GeneratedIdea>> {
        delay(2000) // Simulate network delay
        
        val needsReview = request.hasReligiousElement
        val risk = if (request.hasReligiousElement) RiskLevel.HIGH else RiskLevel.LOW
        
        val disclaimer = if (request.hasReligiousElement) 
            "تنبيه: هذا المحتوى يتطلب مراجعة بشرية وإضافة مصادر موثقة قبل النشر النهائي." 
        else null
        
        val sources = if (request.hasReligiousElement) listOf("القرآن الكريم", "الحديث الشريف", "المصادر الفقهية") else emptyList()

        val mockIdeas = listOf(
            GeneratedIdea(
                id = UUID.randomUUID().toString(),
                title = "فكرة 1: ${request.subject} - مدخل مباشر",
                hook = "هل فكرت يوماً في ${request.subject}؟ إليك ما يهمك...",
                summary = "محتوى يركز على تقديم فكرة مبسطة للجمهور (${request.audience}) بأسلوب ${request.tone}.",
                audience = request.audience,
                suggestedScenes = 4,
                requiredSources = sources,
                riskLevel = risk,
                needsReview = needsReview,
                disclaimer = disclaimer
            ),
            GeneratedIdea(
                id = UUID.randomUUID().toString(),
                title = "فكرة 2: القصة العميقة عن ${request.subject}",
                hook = "قصة قصيرة ستغير نظرتك تماماً عن هذا الموضوع.",
                summary = "سرد قصصي يستهدف العاطفة ويصل لنتيجة منطقية تخدم هدف (${request.goal}).",
                audience = request.audience,
                suggestedScenes = 6,
                requiredSources = sources,
                riskLevel = risk,
                needsReview = needsReview,
                disclaimer = disclaimer
            )
        )
        return Resource.Success(mockIdeas)
    }

    override suspend fun reportIdea(ideaId: String, reason: String): Resource<Unit> {
        delay(500)
        return Resource.Success(Unit)
    }
}
