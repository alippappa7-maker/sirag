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
    private val reviewersState = MutableStateFlow<List<ReviewerProfile>>(createInitialReviewers())
    private val conflictsState = MutableStateFlow<List<ReviewerConflict>>(createInitialConflicts())
    private val assignmentsState = MutableStateFlow<List<ReviewerAssignment>>(createInitialAssignments())
    private val decisionsState = MutableStateFlow<List<ReviewerDecision>>(createInitialDecisions())

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

    private fun createInitialReviewers(): List<ReviewerProfile> {
        val now = System.currentTimeMillis()
        val oneYearLater = now + (365L * 24 * 3600 * 1000L)

        return listOf(
            ReviewerProfile(
                id = "rev_dr_abdullah_almansoor",
                displayName = "د. عبد الله المنصور",
                email = "a.almansoor@sharia-audit.org",
                organization = "هيئة كبار العلماء والبحوث الإسلامية",
                qualifications =
                    listOf(
                        ReviewerQualification(
                            degreeTitle = "دكتوراه في الفقه المقارن وأصوله",
                            institution = "الجامعة الإسلامية بالمدينة المنورة",
                            graduationYear = 2012,
                            verifiedByOwnerId = "owner_system_admin",
                            verifiedAt = now - (30L * 24 * 3600 * 1000L),
                            isVerified = true,
                            isPubliclyVisible = true,
                        ),
                        ReviewerQualification(
                            degreeTitle = "إجازة مسندة في القراءات العشر المتواترة",
                            institution = "معهد القراءات بالمسجد النبوي",
                            graduationYear = 2014,
                            verifiedByOwnerId = "owner_system_admin",
                            verifiedAt = now - (30L * 24 * 3600 * 1000L),
                            isVerified = true,
                            isPubliclyVisible = true,
                        ),
                    ),
                specialties = setOf(ReviewerDomain.FIQH, ReviewerDomain.QURAN, ReviewerDomain.TAFSIR),
                languages = listOf("ar", "en"),
                scope =
                    ReviewerScope(
                        allowedDomains = setOf(ReviewerDomain.FIQH, ReviewerDomain.QURAN, ReviewerDomain.TAFSIR, ReviewerDomain.GENERAL),
                        excludedTopics = setOf(CriticalTopic.TAKFIER),
                        maxRiskLevelAllowed = RiskLevel.CRITICAL,
                        canBePrimaryReviewer = true,
                        canBeSecondReviewer = true,
                        dailyReviewQuota = 15,
                    ),
                status = ReviewerStatus.ACTIVE,
                verifiedByOwnerId = "owner_system_admin",
                verificationDate = now - (30L * 24 * 3600 * 1000L),
                nextReverificationDue = oneYearLater,
                totalReviewsCompleted = 42,
            ),
            ReviewerProfile(
                id = "rev_sheikh_tariq_alhadith",
                displayName = "الشيخ طارق بن يوسف المحمود",
                email = "tariq.mahmoud@hadith-studies.edu",
                organization = "مركز دار الحديث والسنّة",
                qualifications =
                    listOf(
                        ReviewerQualification(
                            degreeTitle = "ماجستير في علوم الحديث وتخريجه",
                            institution = "جامعة أم القرى",
                            graduationYear = 2016,
                            verifiedByOwnerId = "owner_system_admin",
                            verifiedAt = now - (60L * 24 * 3600 * 1000L),
                            isVerified = true,
                            isPubliclyVisible = true,
                        ),
                    ),
                specialties = setOf(ReviewerDomain.HADITH, ReviewerDomain.SEERAH),
                languages = listOf("ar"),
                scope =
                    ReviewerScope(
                        allowedDomains = setOf(ReviewerDomain.HADITH, ReviewerDomain.SEERAH, ReviewerDomain.GENERAL),
                        excludedTopics = emptySet(),
                        maxRiskLevelAllowed = RiskLevel.HIGH,
                        canBePrimaryReviewer = true,
                        canBeSecondReviewer = true,
                        dailyReviewQuota = 12,
                    ),
                status = ReviewerStatus.ACTIVE,
                verifiedByOwnerId = "owner_system_admin",
                verificationDate = now - (60L * 24 * 3600 * 1000L),
                nextReverificationDue = oneYearLater,
                totalReviewsCompleted = 68,
            ),
            ReviewerProfile(
                id = "rev_prof_khaled_aqeedah",
                displayName = "أ.د. خالد السعيد",
                email = "k.alsaeed@islamic-studies.net",
                organization = "مجمع الدراسات العقدية والفكرية",
                qualifications =
                    listOf(
                        ReviewerQualification(
                            degreeTitle = "أستاذ دكتور في العقيدة والمذاهب المعاصرة",
                            institution = "جامعة الأزهر",
                            graduationYear = 2008,
                            verifiedByOwnerId = "owner_system_admin",
                            verifiedAt = now - (90L * 24 * 3600 * 1000L),
                            isVerified = true,
                            isPubliclyVisible = true,
                        ),
                    ),
                specialties = setOf(ReviewerDomain.AQEEDAH, ReviewerDomain.EDUCATION),
                languages = listOf("ar", "fr"),
                scope =
                    ReviewerScope(
                        allowedDomains = setOf(ReviewerDomain.AQEEDAH, ReviewerDomain.EDUCATION, ReviewerDomain.GENERAL),
                        excludedTopics = emptySet(),
                        maxRiskLevelAllowed = RiskLevel.CRITICAL,
                        canBePrimaryReviewer = true,
                        canBeSecondReviewer = true,
                        dailyReviewQuota = 8,
                    ),
                status = ReviewerStatus.ACTIVE,
                verifiedByOwnerId = "owner_system_admin",
                verificationDate = now - (90L * 24 * 3600 * 1000L),
                nextReverificationDue = oneYearLater,
                totalReviewsCompleted = 31,
            ),
            ReviewerProfile(
                id = "rev_applicant_ibrahim",
                displayName = "الشيخ إبراهيم الفاسي",
                email = "ibrahim.fassi@studies.org",
                organization = "الرابطة التربوية لعلوم الشريعة",
                qualifications =
                    listOf(
                        ReviewerQualification(
                            degreeTitle = "بكالوريوس الشريعة والدراسات الإسلامية",
                            institution = "جامعة القرويين",
                            graduationYear = 2021,
                            isVerified = false,
                            isPubliclyVisible = false,
                        ),
                    ),
                specialties = setOf(ReviewerDomain.EDUCATION, ReviewerDomain.GENERAL),
                languages = listOf("ar"),
                scope =
                    ReviewerScope(
                        allowedDomains = setOf(ReviewerDomain.EDUCATION, ReviewerDomain.GENERAL),
                        maxRiskLevelAllowed = RiskLevel.LOW,
                        canBePrimaryReviewer = true,
                        canBeSecondReviewer = false,
                    ),
                status = ReviewerStatus.PENDING_VERIFICATION,
                verifiedByOwnerId = null,
                verificationDate = null,
                totalReviewsCompleted = 0,
            ),
        )
    }

    private fun createInitialConflicts(): List<ReviewerConflict> =
        listOf(
            ReviewerConflict(
                id = "conflict_01",
                reviewerId = "rev_dr_abdullah_almansoor",
                creatorId = "creator_mansoor_production",
                conflictType = ConflictType.PERSONAL_AFFILIATION,
                reason = "صلة قرابة مباشرة مع مؤسس شركة الإنتاج",
                isRestricted = true,
            ),
        )

    private fun createInitialAssignments(): List<ReviewerAssignment> {
        val now = System.currentTimeMillis()
        return listOf(
            ReviewerAssignment(
                id = "assign_001",
                itemId = "review_item_001",
                contentTitle = "ومضة: حديث إنما الأعمال بالنيات ومقاصد الإخلاص",
                contentVersion = 1,
                domain = ReviewerDomain.HADITH,
                riskLevel = RiskLevel.MEDIUM,
                criticalTopic = CriticalTopic.NONE,
                primaryReviewerId = "rev_sheikh_tariq_alhadith",
                primaryReviewerName = "الشيخ طارق بن يوسف المحمود",
                secondReviewerId = null,
                assignedByOwnerId = "owner_system_admin",
                assignedAt = now - (2 * 3600 * 1000L),
                status = AssignmentStatus.IN_PROGRESS,
                isSecondReviewRequired = false,
            ),
            ReviewerAssignment(
                id = "assign_002",
                itemId = "review_item_002",
                contentTitle = "فيديو: أحكام الصيام والنوازل الطبية المعاصرة",
                contentVersion = 1,
                domain = ReviewerDomain.FIQH,
                riskLevel = RiskLevel.CRITICAL,
                criticalTopic = CriticalTopic.FATWA,
                primaryReviewerId = "rev_dr_abdullah_almansoor",
                primaryReviewerName = "د. عبد الله المنصور",
                secondReviewerId = "rev_prof_khaled_aqeedah",
                secondReviewerName = "أ.د. خالد السعيد",
                assignedByOwnerId = "owner_system_admin",
                assignedAt = now - (5 * 3600 * 1000L),
                status = AssignmentStatus.IN_PROGRESS,
                isSecondReviewRequired = true,
            ),
        )
    }

    private fun createInitialDecisions(): List<ReviewerDecision> {
        val now = System.currentTimeMillis()
        return listOf(
            ReviewerDecision(
                decisionId = "dec_init_01",
                assignmentId = "assign_hist_001",
                itemId = "hist_item_001",
                contentVersion = 1,
                reviewerId = "rev_sheikh_tariq_alhadith",
                reviewerName = "الشيخ طارق بن يوسف المحمود",
                reviewerRole = "PRIMARY",
                outcome = DecisionOutcome.APPROVED,
                notes = "تمت مطابقة نص الحديث مع صحيح البخاري برقم 1 وصحيح مسلم برقم 1907، التخريج سليم والمعنى موافق لقواعد أهل السنة.",
                shariaEvidencesUsed = listOf("صحيح البخاري - كتاب بدء الوحي", "جامع العلوم والحكم لابن رجب"),
                timestamp = now - (24 * 3600 * 1000L),
                isImmutable = true,
            ),
        )
    }
}
