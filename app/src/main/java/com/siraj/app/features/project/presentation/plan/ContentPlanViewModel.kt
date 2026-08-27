package com.siraj.app.features.project.presentation.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.data.repository.FirebaseProjectRepositoryImpl
import com.siraj.app.domain.models.*
import com.siraj.app.domain.repository.ProjectRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class SaveState {
    object Idle : SaveState()
    object Saving : SaveState()
    object Saved : SaveState()
    data class Error(val message: String) : SaveState()
}

@OptIn(FlowPreview::class)
class ContentPlanViewModel(
    private val projectId: String,
    private val projectRepository: ProjectRepository = FirebaseProjectRepositoryImpl()
) : ViewModel() {

    private val _projectState = MutableStateFlow<Resource<Project>>(Resource.Loading)
    val projectState: StateFlow<Resource<Project>> = _projectState.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    private val pendingUpdates = MutableSharedFlow<Project>(replay = 1)

    init {
        loadProject()
        
        viewModelScope.launch {
            pendingUpdates
                .debounce(1500L) // Auto-save
                .collect { projectToSave ->
                    _saveState.value = SaveState.Saving
                    val result = projectRepository.updateProject(projectToSave)
                    if (result is Resource.Success) {
                        _saveState.value = SaveState.Saved
                    } else if (result is Resource.Error) {
                        _saveState.value = SaveState.Error(result.message)
                    }
                }
        }
    }

    private fun loadProject() {
        viewModelScope.launch {
            _projectState.value = Resource.Loading
            _projectState.value = projectRepository.getProject(projectId)
        }
    }

    fun updatePlan(update: (ContentPlan) -> ContentPlan) {
        val current = _projectState.value
        if (current is Resource.Success) {
            val project = current.data
            val currentPlan = project.contentPlan ?: ContentPlan(
                title = project.title,
                hook = project.brief.idea.take(50),
                reviewLevel = if (project.brief.hasQuran || project.brief.hasHadith || project.brief.hasFatwa) RiskLevel.HIGH else RiskLevel.LOW
            )
            val updatedPlan = update(currentPlan).copy(
                version = currentPlan.version + 1,
                lastEditedAt = System.currentTimeMillis()
            )
            val updatedProject = project.copy(contentPlan = updatedPlan)
            _projectState.value = Resource.Success(updatedProject)
            viewModelScope.launch { pendingUpdates.emit(updatedProject) }
        }
    }
    
    
    fun submitForReview() {
        val current = _projectState.value
        if (current is Resource.Success) {
            val project = current.data
            // Auto check
            val unverifiedCount = project.contentPlan?.claims?.count { it.attachedSource?.reviewStatus != SourceVerificationStatus.VERIFIED } ?: 0
            val riskLevel = project.contentPlan?.claims?.maxByOrNull { it.riskLevel }?.riskLevel ?: RiskLevel.LOW
            
            val log = ReviewLog(
                projectId = project.id,
                previousState = project.reviewState,
                newState = ReviewState.SUBMITTED,
                comments = "تم إرسال المشروع للمراجعة. عدد الادعاءات غير الموثقة: $unverifiedCount | مستوى الخطورة: ${riskLevel.name}"
            )
            
            val updatedProject = project.copy(
                reviewState = ReviewState.SUBMITTED,
                reviewLogs = project.reviewLogs + log
            )
            
            _projectState.value = Resource.Success(updatedProject)
            viewModelScope.launch { pendingUpdates.emit(updatedProject) }
        }
    }

    fun submitReviewDecision(decision: ReviewState, comments: String) {
        val current = _projectState.value
        if (current is Resource.Success) {
            val project = current.data
            
            val log = ReviewLog(
                projectId = project.id,
                previousState = project.reviewState,
                newState = decision,
                comments = comments
            )
            
            val updatedProject = project.copy(
                reviewState = decision,
                reviewLogs = project.reviewLogs + log
            )
            
            _projectState.value = Resource.Success(updatedProject)
            viewModelScope.launch { pendingUpdates.emit(updatedProject) }
        }
    }

    fun generateMockPlan() {
        // This is a stub for generating a plan using Gemini Backend later
        val current = _projectState.value
        if (current is Resource.Success) {
            val project = current.data
            
            val mockClaims = mutableListOf<ContentClaim>()
            if (project.brief.hasQuran) {
                mockClaims.add(ContentClaim(text = "إِنَّ مَعَ الْعُسْرِ يُسْرًا", type = ClaimType.QURAN, riskLevel = RiskLevel.HIGH))
            }
            if (project.brief.hasFatwa) {
                mockClaims.add(ContentClaim(text = "حكم هذه المسألة الجواز بشروط", type = ClaimType.FIQH, riskLevel = RiskLevel.HIGH))
            }
            if (mockClaims.isEmpty()) {
                mockClaims.add(ContentClaim(text = "الاستمرارية سر النجاح", type = ClaimType.GENERAL, riskLevel = RiskLevel.LOW))
            }

            val mockPlan = ContentPlan(
                title = project.title,
                hook = "سؤال يطرح نفسه دائماً: " + project.brief.idea.take(30),
                mainPoints = "1. النقطة الأولى\n2. النقطة الثانية\n3. النقطة الثالثة",
                conclusion = "في النهاية، الأمر يعتمد على الالتزام.",
                callToAction = "شارك هذا المقطع مع من تحب.",
                estimatedDuration = project.brief.duration,
                claims = mockClaims,
                reviewLevel = if (mockClaims.any { it.riskLevel == RiskLevel.HIGH }) RiskLevel.HIGH else RiskLevel.LOW,
                warnings = if (mockClaims.any { it.riskLevel == RiskLevel.HIGH }) listOf("تنبيه: يحتوي السيناريو على نصوص دينية تتطلب المراجعة.") else emptyList()
            )
            
            val updatedProject = project.copy(contentPlan = mockPlan, status = ProjectStatus.READY)
            _projectState.value = Resource.Success(updatedProject)
            viewModelScope.launch { pendingUpdates.emit(updatedProject) }
        }
    }

    
    fun updateClaimSource(claimId: String, source: Source) {
        val current = _projectState.value
        if (current is Resource.Success) {
            val project = current.data
            val updatedPlan = project.contentPlan?.copy(
                claims = project.contentPlan.claims.map { claim ->
                    if (claim.id == claimId) {
                        val newSource = if (claim.attachedSource?.reviewStatus == SourceVerificationStatus.VERIFIED) {
                            // Prevent modifying a verified source directly; create a new version and reset status
                            source.copy(
                                version = (claim.attachedSource.version) + 1,
                                reviewStatus = SourceVerificationStatus.UNVERIFIED,
                                reviewedBy = null,
                                reviewedAt = null
                            )
                        } else {
                            source
                        }
                        claim.copy(attachedSource = newSource, sourceStatus = SourceStatus.PENDING_VERIFICATION)
                    } else claim
                },
                lastEditedAt = System.currentTimeMillis()
            )
            if (updatedPlan != null) {
                val updatedProject = project.copy(contentPlan = updatedPlan)
                _projectState.value = Resource.Success(updatedProject)
                viewModelScope.launch { pendingUpdates.emit(updatedProject) }
            }
        }
    }
    
    fun removeClaimSource(claimId: String) {
        val current = _projectState.value
        if (current is Resource.Success) {
            val project = current.data
            val updatedPlan = project.contentPlan?.copy(
                claims = project.contentPlan.claims.map { claim ->
                    if (claim.id == claimId) claim.copy(attachedSource = null, sourceStatus = SourceStatus.MISSING) else claim
                },
                lastEditedAt = System.currentTimeMillis()
            )
            if (updatedPlan != null) {
                val updatedProject = project.copy(contentPlan = updatedPlan)
                _projectState.value = Resource.Success(updatedProject)
                viewModelScope.launch { pendingUpdates.emit(updatedProject) }
            }
        }
    }

    fun sendSourceForReview(claimId: String) {
        val current = _projectState.value
        if (current is Resource.Success) {
            val project = current.data
            val updatedPlan = project.contentPlan?.copy(
                claims = project.contentPlan.claims.map { claim ->
                    if (claim.id == claimId && claim.attachedSource != null) {
                        val updatedSource = claim.attachedSource.copy(reviewStatus = SourceVerificationStatus.PENDING_REVIEW)
                        claim.copy(attachedSource = updatedSource, reviewStatus = ReviewStatus.PENDING_REVIEW)
                    } else claim
                }
            )
            if (updatedPlan != null) {
                val updatedProject = project.copy(contentPlan = updatedPlan, status = ProjectStatus.PROCESSING)
                _projectState.value = Resource.Success(updatedProject)
                viewModelScope.launch { pendingUpdates.emit(updatedProject) }
            }
        }
    }

    fun sendForReview() {
        val current = _projectState.value
        if (current is Resource.Success) {
            val project = current.data
            val updatedPlan = project.contentPlan?.copy(
                claims = project.contentPlan.claims.map { 
                    if (it.riskLevel == RiskLevel.HIGH && it.reviewStatus == ReviewStatus.DRAFT) 
                        it.copy(reviewStatus = ReviewStatus.PENDING_REVIEW) 
                    else it 
                }
            )
            if (updatedPlan != null) {
                val updatedProject = project.copy(contentPlan = updatedPlan, status = ProjectStatus.PROCESSING)
                _projectState.value = Resource.Success(updatedProject)
                viewModelScope.launch { pendingUpdates.emit(updatedProject) }
            }
        }
    }
}

class ContentPlanViewModelFactory(private val projectId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContentPlanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContentPlanViewModel(projectId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
