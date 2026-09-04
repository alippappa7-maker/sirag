package com.siraj.app.data.repository.review

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.governance.*
import com.siraj.app.domain.models.review.CriticalTopic
import com.siraj.app.domain.models.review.RiskLevel
import com.siraj.app.domain.repository.review.ReviewerGovernanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class ReviewerGovernanceRepositoryImpl : ReviewerGovernanceRepository {
    private val reviewersState = MutableStateFlow<List<ReviewerProfile>>(emptyList())
    private val conflictsState = MutableStateFlow<List<ReviewerConflict>>(emptyList())
    private val assignmentsState = MutableStateFlow<List<ReviewerAssignment>>(emptyList())
    private val decisionsState = MutableStateFlow<List<ReviewerDecision>>(emptyList())

    override fun getReviewers(): Flow<Resource<List<ReviewerProfile>>> = reviewersState.map { Resource.Success(it) }

    override fun getReviewerById(reviewerId: String): Flow<Resource<ReviewerProfile>> =
        reviewersState.map { list ->
            val found = list.find { it.id == reviewerId }
            if (found != null) {
                Resource.Success(found)
            } else {
                Resource.Error("لم يتم العثور على ملف المراجع المحدد")
            }
        }

    override suspend fun createOrUpdateReviewer(profile: ReviewerProfile): Resource<Unit> {
        val current = reviewersState.value.toMutableList()
        val index = current.indexOfFirst { it.id == profile.id }
        if (index >= 0) {
            current[index] = profile.copy(updatedAt = System.currentTimeMillis())
        } else {
            current.add(profile)
        }
        reviewersState.value = current
        return Resource.Success(Unit)
    }

    override suspend fun verifyReviewerByOwner(
        reviewerId: String,
        ownerId: String,
        nextReverificationDue: Long?,
    ): Resource<Unit> {
        val current = reviewersState.value.toMutableList()
        val index = current.indexOfFirst { it.id == reviewerId }
        if (index >= 0) {
            val existing = current[index]
            val updated =
                existing.copy(
                    status = ReviewerStatus.ACTIVE,
                    verifiedByOwnerId = ownerId,
                    verificationDate = System.currentTimeMillis(),
                    nextReverificationDue = nextReverificationDue ?: (System.currentTimeMillis() + (365L * 24 * 3600 * 1000L)),
                    updatedAt = System.currentTimeMillis(),
                )
            current[index] = updated
            reviewersState.value = current
            return Resource.Success(Unit)
        }
        return Resource.Error("المراجع غير موجود")
    }

    override suspend fun suspendReviewer(
        reviewerId: String,
        ownerId: String,
        reason: String,
    ): Resource<Unit> {
        val current = reviewersState.value.toMutableList()
        val index = current.indexOfFirst { it.id == reviewerId }
        if (index >= 0) {
            val existing = current[index]
            current[index] =
                existing.copy(
                    status = ReviewerStatus.SUSPENDED,
                    updatedAt = System.currentTimeMillis(),
                )
            reviewersState.value = current
            return Resource.Success(Unit)
        }
        return Resource.Error("المراجع غير موجود")
    }

    override suspend fun reactivateReviewer(
        reviewerId: String,
        ownerId: String,
    ): Resource<Unit> {
        val current = reviewersState.value.toMutableList()
        val index = current.indexOfFirst { it.id == reviewerId }
        if (index >= 0) {
            val existing = current[index]
            current[index] =
                existing.copy(
                    status = ReviewerStatus.ACTIVE,
                    verifiedByOwnerId = ownerId,
                    updatedAt = System.currentTimeMillis(),
                )
            reviewersState.value = current
            return Resource.Success(Unit)
        }
        return Resource.Error("المراجع غير موجود")
    }

    override suspend fun addQualification(
        reviewerId: String,
        qualification: ReviewerQualification,
    ): Resource<Unit> {
        val current = reviewersState.value.toMutableList()
        val index = current.indexOfFirst { it.id == reviewerId }
        if (index >= 0) {
            val existing = current[index]
            val updatedQuals = existing.qualifications + qualification
            current[index] =
                existing.copy(
                    qualifications = updatedQuals,
                    updatedAt = System.currentTimeMillis(),
                )
            reviewersState.value = current
            return Resource.Success(Unit)
        }
        return Resource.Error("المراجع غير موجود")
    }

    override suspend fun updateScope(
        reviewerId: String,
        scope: ReviewerScope,
    ): Resource<Unit> {
        val current = reviewersState.value.toMutableList()
        val index = current.indexOfFirst { it.id == reviewerId }
        if (index >= 0) {
            val existing = current[index]
            current[index] =
                existing.copy(
                    scope = scope,
                    specialties = scope.allowedDomains,
                    updatedAt = System.currentTimeMillis(),
                )
            reviewersState.value = current
            return Resource.Success(Unit)
        }
        return Resource.Error("المراجع غير موجود")
    }

    override fun getConflicts(): Flow<Resource<List<ReviewerConflict>>> = conflictsState.map { Resource.Success(it) }

    override suspend fun recordConflict(conflict: ReviewerConflict): Resource<Unit> {
        val current = conflictsState.value.toMutableList()
        current.add(conflict)
        conflictsState.value = current
        return Resource.Success(Unit)
    }

    override suspend fun resolveConflict(conflictId: String): Resource<Unit> {
        val current = conflictsState.value.toMutableList()
        val index = current.indexOfFirst { it.id == conflictId }
        if (index >= 0) {
            val existing = current[index]
            current[index] = existing.copy(isRestricted = false)
            conflictsState.value = current
            return Resource.Success(Unit)
        }
        return Resource.Error("التعارض غير موجود")
    }

    override fun getAssignments(): Flow<Resource<List<ReviewerAssignment>>> = assignmentsState.map { Resource.Success(it) }

    override fun getAssignmentsForReviewer(reviewerId: String): Flow<Resource<List<ReviewerAssignment>>> =
        assignmentsState.map { list ->
            val userAssignments =
                list.filter {
                    it.primaryReviewerId == reviewerId || it.secondReviewerId == reviewerId
                }
            Resource.Success(userAssignments)
        }

    override suspend fun createAssignment(assignment: ReviewerAssignment): Resource<Unit> {
        val current = assignmentsState.value.toMutableList()
        current.add(assignment)
        assignmentsState.value = current
        return Resource.Success(Unit)
    }

    override suspend fun recordDecision(decision: ReviewerDecision): Resource<Unit> {
        // التحقق من الحفظ في سجل ثابت لا يقبل التعديل
        val currentDecisions = decisionsState.value.toMutableList()
        currentDecisions.add(decision)
        decisionsState.value = currentDecisions

        // تحديث حالة التعيين
        val currentAssignments = assignmentsState.value.toMutableList()
        val aIndex = currentAssignments.indexOfFirst { it.id == decision.assignmentId }
        if (aIndex >= 0) {
            val a = currentAssignments[aIndex]
            val isSecond = decision.reviewerRole == "SECOND"
            val updatedAssignment =
                a.copy(
                    primaryDecisionId = if (!isSecond) decision.decisionId else a.primaryDecisionId,
                    secondDecisionId = if (isSecond) decision.decisionId else a.secondDecisionId,
                    status =
                        if (a.isSecondReviewRequired) {
                            if (a.primaryDecisionId != null && (isSecond || a.secondDecisionId != null)) {
                                AssignmentStatus.COMPLETED
                            } else {
                                AssignmentStatus.IN_PROGRESS
                            }
                        } else {
                            AssignmentStatus.COMPLETED
                        },
                )
            currentAssignments[aIndex] = updatedAssignment
            assignmentsState.value = currentAssignments
        }

        // زيادة عدد المراجعات المنجزة للمراجع
        val currentReviewers = reviewersState.value.toMutableList()
        val rIndex = currentReviewers.indexOfFirst { it.id == decision.reviewerId }
        if (rIndex >= 0) {
            val r = currentReviewers[rIndex]
            currentReviewers[rIndex] = r.copy(totalReviewsCompleted = r.totalReviewsCompleted + 1)
            reviewersState.value = currentReviewers
        }

        return Resource.Success(Unit)
    }

    override fun getDecisionsForItem(itemId: String): Flow<Resource<List<ReviewerDecision>>> =
        decisionsState.map { list ->
            val itemDecisions = list.filter { it.itemId == itemId }.sortedByDescending { it.timestamp }
            Resource.Success(itemDecisions)
        }
}
