package com.siraj.app.domain.repository

import com.siraj.app.domain.models.beta.BetaFeedback
import kotlinx.coroutines.flow.Flow

interface BetaFeedbackRepository {
    suspend fun submitFeedback(feedback: BetaFeedback): Result<String>
    fun getMyFeedback(userId: String): Flow<List<BetaFeedback>>
}
