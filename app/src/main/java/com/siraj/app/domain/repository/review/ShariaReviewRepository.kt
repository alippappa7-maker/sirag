package com.siraj.app.domain.repository.review

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.review.*
import kotlinx.coroutines.flow.Flow

interface ShariaReviewRepository {
    fun getReviewQueue(filter: ShariaReviewFilter): Flow<Resource<List<ShariaReviewItem>>>
    fun getReviewItemById(itemId: String): Flow<Resource<ShariaReviewItem>>
    
    suspend fun claimReview(itemId: String, reviewerId: String, reviewerName: String): Resource<Unit>
    suspend fun releaseReview(itemId: String, reviewerId: String, reviewerName: String): Resource<Unit>
    
    suspend fun approveItem(
        itemId: String,
        reviewerId: String,
        reviewerName: String,
        reason: String,
        scheduledReReviewDate: Long? = null
    ): Resource<Unit>
    
    suspend fun rejectItem(
        itemId: String,
        reviewerId: String,
        reviewerName: String,
        reason: String
    ): Resource<Unit>
    
    suspend fun requestChanges(
        itemId: String,
        reviewerId: String,
        reviewerName: String,
        requiredChanges: String
    ): Resource<Unit>
    
    suspend fun escalateToSecondReviewer(
        itemId: String,
        reviewerId: String,
        reviewerName: String,
        targetReviewerId: String,
        targetReviewerName: String,
        reason: String
    ): Resource<Unit>
    
    suspend fun submitSecondReviewDecision(
        itemId: String,
        secondReviewerId: String,
        secondReviewerName: String,
        approve: Boolean,
        reason: String
    ): Resource<Unit>
    
    suspend fun addClaimComment(
        itemId: String,
        claimId: String,
        reviewerId: String,
        reviewerName: String,
        comment: String
    ): Resource<Unit>
    
    suspend fun addInternalNote(
        itemId: String,
        authorId: String,
        authorName: String,
        note: String
    ): Resource<Unit>
    
    suspend fun scheduleReReviewDate(
        itemId: String,
        reviewerId: String,
        reviewerName: String,
        reReviewTimestamp: Long
    ): Resource<Unit>
    
    suspend fun notifyContentModified(
        itemId: String,
        editorId: String,
        editorName: String,
        summary: String,
        newFullText: String
    ): Resource<Unit>
}
