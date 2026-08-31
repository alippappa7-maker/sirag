package com.siraj.app.features.review.domain

import com.siraj.app.domain.models.governance.*
import com.siraj.app.domain.models.review.CriticalTopic
import com.siraj.app.domain.models.review.RiskLevel
import com.siraj.app.domain.models.review.ShariaReviewItem

object ReviewerGovernanceEngine {
    sealed class EligibilityResult {
        object Eligible : EligibilityResult()

        data class Ineligible(
            val reason: String,
        ) : EligibilityResult()
    }

    sealed class ConflictCheckResult {
        object NoConflict : ConflictCheckResult()

        data class ConflictDetected(
            val conflictType: ConflictType,
            val details: String,
        ) : ConflictCheckResult()
    }

    data class AssignmentProposal(
        val isEligible: Boolean,
        val primaryReviewer: ReviewerProfile?,
        val secondReviewer: ReviewerProfile?,
        val isSecondReviewRequired: Boolean,
        val warningNotes: List<String> = emptyList(),
        val rejectionReasons: List<String> = emptyList(),
    )

    /**
     * التحقق من أهلية المراجع لمراجعة محتوى محدد
     */
    fun validateReviewerEligibility(
        reviewer: ReviewerProfile,
        domain: ReviewerDomain,
        riskLevel: RiskLevel,
        criticalTopic: CriticalTopic,
        isSecondReviewer: Boolean = false,
    ): EligibilityResult {
        // 1. التحقق من حالة الاعتماد
        if (reviewer.status != ReviewerStatus.ACTIVE) {
            return EligibilityResult.Ineligible("المراجع غير معتمد أو حسابه موقوف (${reviewer.status.arabicTitle})")
        }

        // 2. التحقق من توثيق المالك
        if (reviewer.verifiedByOwnerId.isNullOrBlank()) {
            return EligibilityResult.Ineligible("المراجع لم يتم اعتماده رسمياً من قِبل مالك المنظومة (Owner)")
        }

        // 3. التحقق من موعد إعادة التحقق الدوري
        val currentTime = System.currentTimeMillis()
        if (reviewer.nextReverificationDue != null && currentTime > reviewer.nextReverificationDue) {
            return EligibilityResult.Ineligible("انتهت صلاحية اعتماد المراجع ويتطلب إعادة تحقق دوري من المؤهلات")
        }

        // 4. التحقق من اختصاص المجال
        if (!reviewer.scope.allowedDomains.contains(domain)) {
            return EligibilityResult.Ineligible("مجال المحتوى (${domain.arabicTitle}) خارج نطاق اختصاص المراجع المعتمد")
        }

        // 5. التحقق من الموضوعات المستثناة
        if (criticalTopic != CriticalTopic.NONE && reviewer.scope.excludedTopics.contains(criticalTopic)) {
            return EligibilityResult.Ineligible(
                "الموضوع الحرج (${criticalTopic.arabicTitle}) يقع ضمن الموضوعات المستثناة من اختصاص المراجع",
            )
        }

        // 6. التحقق من سقف مستوى الخطورة
        if (riskLevel.levelPriority > reviewer.scope.maxRiskLevelAllowed.levelPriority) {
            return EligibilityResult.Ineligible(
                "مستوى خطورة المحتوى (${riskLevel.arabicTitle}) يتجاوز الحد الأقصى المسموح به للمراجع (${reviewer.scope.maxRiskLevelAllowed.arabicTitle})",
            )
        }

        // 7. التحقق من أهلية المراجعة الأولية أو الثانوية
        if (isSecondReviewer && !reviewer.scope.canBeSecondReviewer) {
            return EligibilityResult.Ineligible("المراجع غير مفوض للمراجعة الثانوية للموضوعات الحرجة")
        }
        if (!isSecondReviewer && !reviewer.scope.canBePrimaryReviewer) {
            return EligibilityResult.Ineligible("المراجع غير مفوض للمراجعة الأولية")
        }

        return EligibilityResult.Eligible
    }

    /**
     * فحص تعارض المصالح لمنع المحاباة أو الخصومة
     */
    fun checkConflictOfInterest(
        reviewerId: String,
        creatorId: String,
        projectId: String? = null,
        recordedConflicts: List<ReviewerConflict> = emptyList(),
    ): ConflictCheckResult {
        // قاعدة غير قابلة للتفاوض: لا يراجع الصانع محتواه بنفسه أبداً
        if (reviewerId.trim() == creatorId.trim()) {
            return ConflictCheckResult.ConflictDetected(
                ConflictType.OWN_CONTENT,
                "محظور قطعاً: صانع المحتوى لا يمكنه مراجعة أو اعتماد عمله بنفسه",
            )
        }

        // فحص التعارضات المسجلة
        val activeConflict =
            recordedConflicts.find { conflict ->
                conflict.reviewerId == reviewerId &&
                    conflict.isRestricted &&
                    (conflict.creatorId == creatorId || (projectId != null && conflict.projectId == projectId))
            }

        if (activeConflict != null) {
            return ConflictCheckResult.ConflictDetected(
                activeConflict.conflictType,
                "تعارض مصالح مسجل: ${activeConflict.conflictType.arabicTitle} (${activeConflict.reason})",
            )
        }

        return ConflictCheckResult.NoConflict
    }

    /**
     * تحديد متطلبات المراجعة المشتركة (المراجع الثاني)
     */
    fun isSecondReviewRequired(
        riskLevel: RiskLevel,
        criticalTopic: CriticalTopic,
    ): Boolean =
        riskLevel == RiskLevel.CRITICAL ||
            riskLevel == RiskLevel.HIGH ||
            criticalTopic != CriticalTopic.NONE

    /**
     * إنشاء سجل قرار شرعي ثابت غير قابل للحذف
     */
    fun createImmutableDecision(
        assignment: ReviewerAssignment,
        reviewer: ReviewerProfile,
        isSecondReviewer: Boolean,
        outcome: DecisionOutcome,
        notes: String,
        evidences: List<String> = emptyList(),
        correctionSummary: String? = null,
        supersedesDecisionId: String? = null,
    ): ReviewerDecision =
        ReviewerDecision(
            assignmentId = assignment.id,
            itemId = assignment.itemId,
            contentVersion = assignment.contentVersion,
            reviewerId = reviewer.id,
            reviewerName = reviewer.displayName,
            reviewerRole = if (isSecondReviewer) "SECOND" else "PRIMARY",
            outcome = outcome,
            notes = notes.trim(),
            shariaEvidencesUsed = evidences,
            correctionSummary = correctionSummary?.trim(),
            timestamp = System.currentTimeMillis(),
            isImmutable = true,
            supersedesDecisionId = supersedesDecisionId,
        )

    /**
     * اقتراح تعيين مراجعين مناسبين للمحتوى بناءً على الاختصاص والسلامة
     */
    fun proposeAssignment(
        item: ShariaReviewItem,
        domain: ReviewerDomain,
        activeReviewers: List<ReviewerProfile>,
        recordedConflicts: List<ReviewerConflict>,
        ownerId: String,
    ): AssignmentProposal {
        val criticalTopic = item.criticalTopics.firstOrNull() ?: CriticalTopic.NONE
        val requiresSecond = isSecondReviewRequired(item.riskLevel, criticalTopic)
        val warnings = mutableListOf<String>()
        val rejectionReasons = mutableListOf<String>()

        // 1. تصفية المرشحين المؤهلين للمراجعة الأولية
        val eligiblePrimaries =
            activeReviewers.filter { candidate ->
                val eligibility =
                    validateReviewerEligibility(
                        candidate,
                        domain,
                        item.riskLevel,
                        criticalTopic,
                        isSecondReviewer = false,
                    )
                val conflict =
                    checkConflictOfInterest(
                        candidate.id,
                        item.creatorId,
                        item.projectId,
                        recordedConflicts,
                    )
                eligibility is EligibilityResult.Eligible && conflict is ConflictCheckResult.NoConflict
            }

        if (eligiblePrimaries.isEmpty()) {
            rejectionReasons.add(
                "لا يوجد مراجع معتمد مؤهل وخالٍ من تعارض المصالح لمجال ${domain.arabicTitle} ومستوى ${item.riskLevel.arabicTitle}",
            )
            return AssignmentProposal(
                isEligible = false,
                primaryReviewer = null,
                secondReviewer = null,
                isSecondReviewRequired = requiresSecond,
                warningNotes = warnings,
                rejectionReasons = rejectionReasons,
            )
        }

        val primary = eligiblePrimaries.minByOrNull { it.totalReviewsCompleted } // موازنة عبء العمل

        // 2. اختيار المراجع الثاني إن لزم الأمر
        var second: ReviewerProfile? = null
        if (requiresSecond && primary != null) {
            val eligibleSeconds =
                activeReviewers.filter { candidate ->
                    candidate.id != primary.id &&
                        validateReviewerEligibility(
                            candidate,
                            domain,
                            item.riskLevel,
                            criticalTopic,
                            isSecondReviewer = true,
                        ) is EligibilityResult.Eligible &&
                        checkConflictOfInterest(
                            candidate.id,
                            item.creatorId,
                            item.projectId,
                            recordedConflicts,
                        ) is ConflictCheckResult.NoConflict
                }

            if (eligibleSeconds.isEmpty()) {
                warnings.add(
                    "المحتوى حرج ويتطلب مراجعاً ثانياً، ولكن لا يتوفر حالياً مراجع ثانٍ مؤهل بالمجال. سيبقى التعيين معلقاً لحين توفير مراجع ثانٍ.",
                )
            } else {
                second = eligibleSeconds.minByOrNull { it.totalReviewsCompleted }
            }
        }

        return AssignmentProposal(
            isEligible = true,
            primaryReviewer = primary,
            secondReviewer = second,
            isSecondReviewRequired = requiresSecond,
            warningNotes = warnings,
            rejectionReasons = rejectionReasons,
        )
    }

    /**
     * تحويل الشريحة العامة من السجلات إلى بطاقة مراجع عامة آمنة (لا تنشر بيانات المؤهل الحساسة دون موافقة)
     */
    fun sanitizePublicProfile(profile: ReviewerProfile): Map<String, Any?> =
        mapOf(
            "id" to profile.id,
            "displayName" to profile.displayName,
            "organization" to profile.organization,
            "specialties" to profile.specialties.map { it.arabicTitle },
            "status" to profile.status.arabicTitle,
            "languages" to profile.languages,
            "publicQualifications" to
                profile.qualifications.filter { it.isPubliclyVisible }.map {
                    "${it.degreeTitle} - ${it.institution} (${it.graduationYear})"
                },
        )
}
