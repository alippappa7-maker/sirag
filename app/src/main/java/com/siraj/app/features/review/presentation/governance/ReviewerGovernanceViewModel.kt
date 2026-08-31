package com.siraj.app.features.review.presentation.governance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.governance.*
import com.siraj.app.domain.models.review.CriticalTopic
import com.siraj.app.domain.models.review.RiskLevel
import com.siraj.app.domain.models.review.ShariaReviewItem
import com.siraj.app.domain.repository.review.ReviewerGovernanceRepository
import com.siraj.app.features.review.domain.ReviewerGovernanceEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ReviewerTabFilter(
    val arabicTitle: String,
) {
    ALL("الكل"),
    ACTIVE("المعتمدون النشطون"),
    PENDING("قيد التحقق"),
    SUSPENDED("الموقوفون"),
}

data class ReviewerGovernanceUiState(
    val reviewers: List<ReviewerProfile> = emptyList(),
    val filteredReviewers: List<ReviewerProfile> = emptyList(),
    val assignments: List<ReviewerAssignment> = emptyList(),
    val conflicts: List<ReviewerConflict> = emptyList(),
    val decisions: List<ReviewerDecision> = emptyList(),
    val selectedTab: ReviewerTabFilter = ReviewerTabFilter.ALL,
    val selectedDomainFilter: ReviewerDomain? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val currentOwnerId: String = "owner_system_admin",
    val selectedReviewerDetails: ReviewerProfile? = null,
    val assignmentProposal: ReviewerGovernanceEngine.AssignmentProposal? = null,
)

class ReviewerGovernanceViewModel(
    private val repository: ReviewerGovernanceRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReviewerGovernanceUiState())
    val uiState: StateFlow<ReviewerGovernanceUiState> = _uiState.asStateFlow()

    init {
        loadAllData()
    }

    fun loadAllData() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            repository.getReviewers().collect { res ->
                if (res is Resource.Success) {
                    val list = res.data ?: emptyList()
                    _uiState.update { current ->
                        current.copy(
                            reviewers = list,
                            filteredReviewers =
                                filterReviewers(
                                    list,
                                    current.selectedTab,
                                    current.selectedDomainFilter,
                                    current.searchQuery,
                                ),
                            isLoading = false,
                        )
                    }
                } else if (res is Resource.Error) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
            }
        }

        viewModelScope.launch {
            repository.getAssignments().collect { res ->
                if (res is Resource.Success) {
                    _uiState.update { it.copy(assignments = res.data ?: emptyList()) }
                }
            }
        }

        viewModelScope.launch {
            repository.getConflicts().collect { res ->
                if (res is Resource.Success) {
                    _uiState.update { it.copy(conflicts = res.data ?: emptyList()) }
                }
            }
        }
    }

    fun selectTab(tab: ReviewerTabFilter) {
        _uiState.update { current ->
            current.copy(
                selectedTab = tab,
                filteredReviewers = filterReviewers(current.reviewers, tab, current.selectedDomainFilter, current.searchQuery),
            )
        }
    }

    fun selectDomainFilter(domain: ReviewerDomain?) {
        _uiState.update { current ->
            current.copy(
                selectedDomainFilter = domain,
                filteredReviewers = filterReviewers(current.reviewers, current.selectedTab, domain, current.searchQuery),
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { current ->
            current.copy(
                searchQuery = query,
                filteredReviewers = filterReviewers(current.reviewers, current.selectedTab, current.selectedDomainFilter, query),
            )
        }
    }

    fun selectReviewerDetails(reviewer: ReviewerProfile?) {
        _uiState.update { it.copy(selectedReviewerDetails = reviewer) }
    }

    fun createNewReviewer(
        displayName: String,
        email: String,
        organization: String,
        domains: Set<ReviewerDomain>,
    ) {
        viewModelScope.launch {
            val newProfile =
                ReviewerProfile(
                    id = "rev_${java.util.UUID.randomUUID().toString().take(8)}",
                    displayName = displayName,
                    email = email,
                    organization = organization,
                    specialties = domains,
                    scope = ReviewerScope(allowedDomains = domains),
                    status = ReviewerStatus.PENDING_VERIFICATION,
                )
            val result = repository.createOrUpdateReviewer(newProfile)
            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(successMessage = "تم تسجيل طلب انضمام المراجع بنجاح وهو بانتظار اعتماد المالك") }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun verifyReviewer(
        reviewerId: String,
        reverificationDurationDays: Long = 365,
    ) {
        viewModelScope.launch {
            val dueTimestamp = System.currentTimeMillis() + (reverificationDurationDays * 24 * 3600 * 1000L)
            val result =
                repository.verifyReviewerByOwner(
                    reviewerId = reviewerId,
                    ownerId = _uiState.value.currentOwnerId,
                    nextReverificationDue = dueTimestamp,
                )
            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(successMessage = "تم اعتماد المراجع وتوثيق صلاحياته بنجاح") }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun suspendReviewer(
        reviewerId: String,
        reason: String,
    ) {
        viewModelScope.launch {
            val result =
                repository.suspendReviewer(
                    reviewerId = reviewerId,
                    ownerId = _uiState.value.currentOwnerId,
                    reason = reason,
                )
            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(successMessage = "تم إيقاف المراجع مؤقتاً مع الحفاظ الكامل على سجله التاريخي") }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun reactivateReviewer(reviewerId: String) {
        viewModelScope.launch {
            val result =
                repository.reactivateReviewer(
                    reviewerId = reviewerId,
                    ownerId = _uiState.value.currentOwnerId,
                )
            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(successMessage = "تمت إعادة تفعيل حساب المراجع بنجاح") }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun addQualification(
        reviewerId: String,
        degreeTitle: String,
        institution: String,
        graduationYear: Int,
        isPubliclyVisible: Boolean,
    ) {
        viewModelScope.launch {
            val qual =
                ReviewerQualification(
                    degreeTitle = degreeTitle,
                    institution = institution,
                    graduationYear = graduationYear,
                    verifiedByOwnerId = _uiState.value.currentOwnerId,
                    verifiedAt = System.currentTimeMillis(),
                    isVerified = true,
                    isPubliclyVisible = isPubliclyVisible,
                )
            val result = repository.addQualification(reviewerId, qual)
            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(successMessage = "تمت إضافة وتوثيق المؤهل العلمي للمراجع") }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun updateReviewerScope(
        reviewerId: String,
        allowedDomains: Set<ReviewerDomain>,
        excludedTopics: Set<CriticalTopic>,
        maxRiskLevel: RiskLevel,
        canPrimary: Boolean,
        canSecond: Boolean,
        dailyQuota: Int,
    ) {
        viewModelScope.launch {
            val newScope =
                ReviewerScope(
                    allowedDomains = allowedDomains,
                    excludedTopics = excludedTopics,
                    maxRiskLevelAllowed = maxRiskLevel,
                    canBePrimaryReviewer = canPrimary,
                    canBeSecondReviewer = canSecond,
                    dailyReviewQuota = dailyQuota,
                )
            val result = repository.updateScope(reviewerId, newScope)
            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(successMessage = "تم تحديث نطاق مجالات واختصاصات المراجع") }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun recordConflict(
        reviewerId: String,
        creatorId: String,
        projectId: String?,
        conflictType: ConflictType,
        reason: String,
    ) {
        viewModelScope.launch {
            val conflict =
                ReviewerConflict(
                    reviewerId = reviewerId,
                    creatorId = creatorId,
                    projectId = projectId,
                    conflictType = conflictType,
                    reason = reason,
                    isRestricted = true,
                )
            val result = repository.recordConflict(conflict)
            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(successMessage = "تم تسجيل قيد تعارض المصالح لمنع التعيين") }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun calculateAssignmentProposal(
        item: ShariaReviewItem,
        domain: ReviewerDomain,
    ) {
        val proposal =
            ReviewerGovernanceEngine.proposeAssignment(
                item = item,
                domain = domain,
                activeReviewers = _uiState.value.reviewers.filter { it.status == ReviewerStatus.ACTIVE },
                recordedConflicts = _uiState.value.conflicts,
                ownerId = _uiState.value.currentOwnerId,
            )
        _uiState.update { it.copy(assignmentProposal = proposal) }
    }

    fun assignReviewersToItem(
        item: ShariaReviewItem,
        domain: ReviewerDomain,
        primaryReviewer: ReviewerProfile,
        secondReviewer: ReviewerProfile?,
    ) {
        viewModelScope.launch {
            val criticalTopic = item.criticalTopics.firstOrNull() ?: CriticalTopic.NONE

            // تحقق صارم من الأهلية وتعارض المصالح
            val primaryEligibility =
                ReviewerGovernanceEngine.validateReviewerEligibility(
                    primaryReviewer,
                    domain,
                    item.riskLevel,
                    criticalTopic,
                    isSecondReviewer = false,
                )
            if (primaryEligibility is ReviewerGovernanceEngine.EligibilityResult.Ineligible) {
                _uiState.update { it.copy(errorMessage = "المراجع الأساسي غير مؤهل: ${primaryEligibility.reason}") }
                return@launch
            }

            val primaryConflict =
                ReviewerGovernanceEngine.checkConflictOfInterest(
                    primaryReviewer.id,
                    item.creatorId,
                    item.projectId,
                    _uiState.value.conflicts,
                )
            if (primaryConflict is ReviewerGovernanceEngine.ConflictCheckResult.ConflictDetected) {
                _uiState.update { it.copy(errorMessage = "تعارض مصالح للمراجع الأساسي: ${primaryConflict.details}") }
                return@launch
            }

            val isSecondRequired = ReviewerGovernanceEngine.isSecondReviewRequired(item.riskLevel, criticalTopic)
            if (isSecondRequired && secondReviewer == null) {
                _uiState.update { it.copy(errorMessage = "المحتوى حرج ويتطلب تعيين مراجع ثانٍ معتمد إلزامياً") }
                return@launch
            }

            if (secondReviewer != null) {
                if (secondReviewer.id == primaryReviewer.id) {
                    _uiState.update { it.copy(errorMessage = "لا يمكن أن يكون المراجع الأول والثاني نفس الشخص") }
                    return@launch
                }

                val secondEligibility =
                    ReviewerGovernanceEngine.validateReviewerEligibility(
                        secondReviewer,
                        domain,
                        item.riskLevel,
                        criticalTopic,
                        isSecondReviewer = true,
                    )
                if (secondEligibility is ReviewerGovernanceEngine.EligibilityResult.Ineligible) {
                    _uiState.update { it.copy(errorMessage = "المراجع الثاني غير مؤهل: ${secondEligibility.reason}") }
                    return@launch
                }

                val secondConflict =
                    ReviewerGovernanceEngine.checkConflictOfInterest(
                        secondReviewer.id,
                        item.creatorId,
                        item.projectId,
                        _uiState.value.conflicts,
                    )
                if (secondConflict is ReviewerGovernanceEngine.ConflictCheckResult.ConflictDetected) {
                    _uiState.update { it.copy(errorMessage = "تعارض مصالح للمراجع الثاني: ${secondConflict.details}") }
                    return@launch
                }
            }

            val assignment =
                ReviewerAssignment(
                    itemId = item.id,
                    contentTitle = item.contentTitle,
                    contentVersion = item.contentVersion,
                    domain = domain,
                    riskLevel = item.riskLevel,
                    criticalTopic = criticalTopic,
                    primaryReviewerId = primaryReviewer.id,
                    primaryReviewerName = primaryReviewer.displayName,
                    secondReviewerId = secondReviewer?.id,
                    secondReviewerName = secondReviewer?.displayName,
                    assignedByOwnerId = _uiState.value.currentOwnerId,
                    isSecondReviewRequired = isSecondRequired,
                )

            val result = repository.createAssignment(assignment)
            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(successMessage = "تم تعيين المراجعين الشرعيين بنجاح وتحديد سقف الـ SLA") }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun submitImmutableDecision(
        assignment: ReviewerAssignment,
        reviewer: ReviewerProfile,
        isSecondReviewer: Boolean,
        outcome: DecisionOutcome,
        notes: String,
        evidences: List<String>,
        correctionSummary: String? = null,
        supersedesDecisionId: String? = null,
    ) {
        viewModelScope.launch {
            if (notes.isBlank()) {
                _uiState.update { it.copy(errorMessage = "يجب كتابة مبررات وملاحظات القرار الشرعي بوضوح") }
                return@launch
            }

            val decision =
                ReviewerGovernanceEngine.createImmutableDecision(
                    assignment = assignment,
                    reviewer = reviewer,
                    isSecondReviewer = isSecondReviewer,
                    outcome = outcome,
                    notes = notes,
                    evidences = evidences,
                    correctionSummary = correctionSummary,
                    supersedesDecisionId = supersedesDecisionId,
                )

            val result = repository.recordDecision(decision)
            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(successMessage = "تم تسجيل القرار الشرعي في السجل الثابت غير القابل للتعديل") }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private fun filterReviewers(
        list: List<ReviewerProfile>,
        tab: ReviewerTabFilter,
        domain: ReviewerDomain?,
        query: String,
    ): List<ReviewerProfile> =
        list.filter { rev ->
            val matchesTab =
                when (tab) {
                    ReviewerTabFilter.ALL -> true
                    ReviewerTabFilter.ACTIVE -> rev.status == ReviewerStatus.ACTIVE
                    ReviewerTabFilter.PENDING -> rev.status == ReviewerStatus.PENDING_VERIFICATION
                    ReviewerTabFilter.SUSPENDED -> rev.status == ReviewerStatus.SUSPENDED
                }
            val matchesDomain = domain == null || rev.scope.allowedDomains.contains(domain)
            val matchesQuery =
                query.isBlank() ||
                    rev.displayName.contains(query, ignoreCase = true) ||
                    rev.organization.contains(query, ignoreCase = true) ||
                    rev.email.contains(query, ignoreCase = true)

            matchesTab && matchesDomain && matchesQuery
        }
}
