package com.siraj.app.domain.services

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.GeneratedIdea
import com.siraj.app.domain.models.IdeaGenerationRequest

interface IdeaGeneratorService {
    suspend fun generateIdeas(request: IdeaGenerationRequest): Resource<List<GeneratedIdea>>

    suspend fun reportIdea(
        ideaId: String,
        reason: String,
    ): Resource<Unit>
}
