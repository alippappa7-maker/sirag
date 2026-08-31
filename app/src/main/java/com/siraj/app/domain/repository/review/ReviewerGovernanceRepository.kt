package com.siraj.app.domain.repository.review

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.governance.*
import kotlinx.coroutines.flow.Flow

interface ReviewerGovernanceRepository {
    fun getReviewers(): Flow<Resource<List<ReviewerProfile>>>
    fun getReviewerById(reviewerId: String): Flow<Resource<ReviewerProfile>>
    
    suspend fun createOrUpdateReviewer(profile: ReviewerProfile): Resource<Unit>
    suspend fun verifyReviewerByOwner(reviewerId: String, ownerId: String, nextReverificationDue: Long?): Resource<Unit>
    suspend fun suspendReviewer(reviewerId: String, ownerId: String, reason: String): Resource<Unit>
    suspend fun reactivateReviewer(reviewerId: String, ownerId: String): Resource<Unit>
    
    suspend fun addQualification(reviewerId: String, qualification: ReviewerQualification): Resource<Unit>
    suspend fun updateScope(reviewerId: String, scope: ReviewerScope): Resource<Unit>
    
    fun getConflicts(): Flow<Resource<List<ReviewerConflict>>>
    suspend fun recordConflict(conflict: ReviewerConflict): Resource<Unit>
    suspend fun resolveConflict(conflictId: String): Resource<Unit>
    
    fun getAssignments(): Flow<Resource<List<ReviewerAssignment>>>
    fun getAssignmentsForReviewer(reviewerId: String): Flow<Resource<List<ReviewerAssignment>>>
    suspend fun createAssignment(assignment: ReviewerAssignment): Resource<Unit>
    
    suspend fun recordDecision(decision: ReviewerDecision): Resource<Unit>
    fun getDecisionsForItem(itemId: String): Flow<Resource<List<ReviewerDecision>>>
}
