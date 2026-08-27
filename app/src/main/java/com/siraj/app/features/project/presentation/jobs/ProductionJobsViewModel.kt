package com.siraj.app.features.project.presentation.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.siraj.app.features.project.data.repositories.FirebaseProductionJobRepositoryImpl
import com.siraj.app.features.project.data.services.FirebaseVideoCompositionServiceImpl
import com.siraj.app.features.project.domain.models.ProductionJob
import com.siraj.app.features.project.domain.models.ProductionQuality
import com.siraj.app.features.project.domain.repositories.ProductionJobRepository
import com.siraj.app.features.project.domain.services.VideoCompositionService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProductionJobsViewModel(
    private val projectId: String?,
    private val jobRepository: ProductionJobRepository = FirebaseProductionJobRepositoryImpl(),
    private val compositionService: VideoCompositionService = FirebaseVideoCompositionServiceImpl(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _jobs = MutableStateFlow<List<ProductionJob>>(emptyList())
    val jobs: StateFlow<List<ProductionJob>> = _jobs.asStateFlow()

    private val _currentJob = MutableStateFlow<ProductionJob?>(null)
    val currentJob: StateFlow<ProductionJob?> = _currentJob.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Dialog & Options
    private val _showCreateJobDialog = MutableStateFlow(false)
    val showCreateJobDialog: StateFlow<Boolean> = _showCreateJobDialog.asStateFlow()

    private val _selectedQuality = MutableStateFlow(ProductionQuality.FHD_1080P)
    val selectedQuality: StateFlow<ProductionQuality> = _selectedQuality.asStateFlow()

    private val _burnSubtitles = MutableStateFlow(true)
    val burnSubtitles: StateFlow<Boolean> = _burnSubtitles.asStateFlow()

    private val _selectedAspectRatio = MutableStateFlow("9:16")
    val selectedAspectRatio: StateFlow<String> = _selectedAspectRatio.asStateFlow()

    private val _isPreviewMode = MutableStateFlow(false)
    val isPreviewMode: StateFlow<Boolean> = _isPreviewMode.asStateFlow()

    init {
        loadJobs()
    }

    private fun loadJobs() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = auth.currentUser?.uid ?: ""
            if (!projectId.isNullOrBlank()) {
                jobRepository.getJobsForProject(projectId).collect { list ->
                    _jobs.value = list
                    _isLoading.value = false
                }
            } else if (userId.isNotBlank()) {
                jobRepository.getJobsForUser(userId).collect { list ->
                    _jobs.value = list
                    _isLoading.value = false
                }
            } else {
                _isLoading.value = false
            }
        }
    }

    fun openCreateJobDialog(aspectRatio: String = "9:16", isPreview: Boolean = false) {
        _selectedAspectRatio.value = aspectRatio
        _isPreviewMode.value = isPreview
        _showCreateJobDialog.value = true
    }

    fun closeCreateJobDialog() {
        _showCreateJobDialog.value = false
    }

    fun setQuality(quality: ProductionQuality) {
        _selectedQuality.value = quality
    }

    fun setBurnSubtitles(burn: Boolean) {
        _burnSubtitles.value = burn
    }

    fun setAspectRatio(ratio: String) {
        _selectedAspectRatio.value = ratio
    }

    fun setIsPreviewMode(preview: Boolean) {
        _isPreviewMode.value = preview
    }

    fun submitProductionJob(targetProjectId: String, workspaceId: String = "") {
        if (targetProjectId.isBlank()) return
        viewModelScope.launch {
            _isSubmitting.value = true
            val isPreview = _isPreviewMode.value
            val idempotencyKey = "job_${targetProjectId}_${if (isPreview) "prev" else "full"}_${System.currentTimeMillis() / 60000}"

            val result = jobRepository.createJob(
                projectId = targetProjectId,
                workspaceId = workspaceId,
                quality = _selectedQuality.value,
                burnSubtitles = _burnSubtitles.value,
                aspectRatio = _selectedAspectRatio.value,
                idempotencyKey = idempotencyKey,
                fps = 30,
                includeSourceCitation = true,
                includeWatermark = true,
                isPreviewOnly = isPreview
            )

            result.onSuccess { newJob ->
                _showCreateJobDialog.value = false
                _userMessage.value = if (isPreview) "تم إدراج مهمة المعاينة السريعة بنجاح." else "تم إدراج مهمة الإنتاج بنجاح في طابور المعالجة."
                trackJob(newJob.jobId)

                // Trigger Composition Worker Pipeline
                launch {
                    val manifestRes = compositionService.buildManifest(
                        projectId = targetProjectId,
                        quality = _selectedQuality.value,
                        aspectRatio = _selectedAspectRatio.value,
                        burnSubtitles = _burnSubtitles.value,
                        fps = 30,
                        includeSourceCitation = true,
                        includeWatermark = true,
                        isPreview = isPreview
                    )
                    manifestRes.onSuccess { manifest ->
                        compositionService.executeComposition(newJob, manifest).collect { updatedJob ->
                            _currentJob.value = updatedJob
                        }
                    }.onFailure { e ->
                        _userMessage.value = "خطأ في بناء تفويض التركيب: ${e.message}"
                    }
                }
            }.onFailure { err ->
                _userMessage.value = err.message ?: "فشل إنشاء مهمة الإنتاج"
            }
            _isSubmitting.value = false
        }
    }

    fun deleteExportedFile(jobId: String) {
        viewModelScope.launch {
            val result = jobRepository.deleteExportedFile(jobId)
            result.onSuccess {
                _userMessage.value = "تم حذف الملف النهائي الناتج بنجاح."
            }.onFailure { err ->
                _userMessage.value = err.message ?: "تعذر حذف الملف"
            }
        }
    }

    fun trackJob(jobId: String) {
        viewModelScope.launch {
            jobRepository.observeJob(jobId).collect { job ->
                _currentJob.value = job
            }
        }
    }

    fun cancelJob(jobId: String) {
        viewModelScope.launch {
            val result = jobRepository.cancelJob(jobId)
            result.onSuccess {
                _userMessage.value = "تم إلغاء المهمة واسترداد الرصيد المحجوز."
            }.onFailure { err ->
                _userMessage.value = err.message ?: "تعذر إلغاء المهمة"
            }
        }
    }

    fun retryJob(jobId: String) {
        viewModelScope.launch {
            val result = jobRepository.retryJob(jobId)
            result.onSuccess { updatedJob ->
                _userMessage.value = "تمت إعادة محاولة المهمة بنجاح."
                if (!projectId.isNullOrBlank()) {
                    launch {
                        val manifestRes = compositionService.buildManifest(
                            projectId = projectId,
                            quality = updatedJob.quality,
                            aspectRatio = updatedJob.aspectRatio,
                            burnSubtitles = updatedJob.burnSubtitles,
                            isPreview = updatedJob.isPreviewOnly
                        )
                        manifestRes.onSuccess { manifest ->
                            compositionService.executeComposition(updatedJob, manifest).collect { stepJob ->
                                _currentJob.value = stepJob
                            }
                        }
                    }
                }
            }.onFailure { err ->
                _userMessage.value = err.message ?: "تعذر إعادة المحاولة"
            }
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}

class ProductionJobsViewModelFactory(
    private val projectId: String? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductionJobsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductionJobsViewModel(projectId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

