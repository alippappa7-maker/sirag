package com.siraj.app.domain.models

import java.util.UUID

enum class ReviewState {
    DRAFT,
    SUBMITTED,
    IN_REVIEW,
    CHANGES_REQUESTED,
    APPROVED,
    REJECTED,
    PUBLISHED,
    SUSPENDED,
    CORRECTED,
}

data class ReviewLog(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val actorId: String = "current_user_id", // Should be actual user ID in production
    val previousState: ReviewState,
    val newState: ReviewState,
    val comments: String,
    val timestamp: Long = System.currentTimeMillis(),
)
