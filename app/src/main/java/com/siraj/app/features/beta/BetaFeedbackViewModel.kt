package com.siraj.app.features.beta

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.config.EnvironmentConfig
import com.siraj.app.data.repository.FirebaseAuthRepositoryImpl
import com.siraj.app.data.repository.beta.FirebaseBetaFeedbackRepositoryImpl
import com.siraj.app.domain.models.beta.BetaFeedback
import com.siraj.app.domain.models.beta.FeedbackCategory
import com.siraj.app.domain.models.beta.FeedbackSeverity
import com.siraj.app.domain.repository.AuthRepository
import com.siraj.app.domain.repository.BetaFeedbackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BetaFeedbackUiState(
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val myFeedbackList: List<BetaFeedback> = emptyList(),
    val category: FeedbackCategory = FeedbackCategory.BUG,
    val severity: FeedbackSeverity = FeedbackSeverity.MEDIUM,
    val title: String = "",
    val description: String = "",
    val stepsToReproduce: String = "",
    val deviceModel: String = "${Build.MANUFACTURER} ${Build.MODEL}",
    val androidVersion: String = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
    val appVersion: String = EnvironmentConfig.versionName,
    val currentRoute: String = ""
)

class BetaFeedbackViewModel(
    private val feedbackRepository: BetaFeedbackRepository = FirebaseBetaFeedbackRepositoryImpl(),
    private val authRepository: AuthRepository = FirebaseAuthRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(BetaFeedbackUiState())
    val uiState: StateFlow<BetaFeedbackUiState> = _uiState.asStateFlow()

    init {
        loadMyFeedback()
    }

    fun setRoute(route: String) {
        _uiState.update { it.copy(currentRoute = route) }
    }

    fun updateCategory(category: FeedbackCategory) {
        _uiState.update { it.copy(category = category) }
    }

    fun updateSeverity(severity: FeedbackSeverity) {
        _uiState.update { it.copy(severity = severity) }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title, errorMessage = null) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description, errorMessage = null) }
    }

    fun updateSteps(steps: String) {
        _uiState.update { it.copy(stepsToReproduce = steps) }
    }

    fun clearStatus() {
        _uiState.update { it.copy(isSuccess = false, errorMessage = null, successMessage = null) }
    }

    fun loadMyFeedback() {
        viewModelScope.launch {
            val user = authRepository.currentUser.firstOrNull()
            if (user != null) {
                feedbackRepository.getMyFeedback(user.id).collect { list ->
                    _uiState.update { it.copy(myFeedbackList = list) }
                }
            }
        }
    }

    fun submitFeedback(onComplete: () -> Unit = {}) {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "يرجى كتابة عنوان مختصر للملاحظة أو المشكلة") }
            return
        }
        if (state.description.isBlank()) {
            _uiState.update { it.copy(errorMessage = "يرجى كتابة تفاصيل المشكلة لمساعدتنا في معالجتها") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val user = authRepository.currentUser.firstOrNull()
            val userId = user?.id ?: "guest_tester"
            val userEmail = user?.email ?: "guest@tester.siraj.app"
            val userName = user?.name ?: "مختبر تجريبي"

            val feedback = BetaFeedback(
                userId = userId,
                userEmail = userEmail,
                userName = userName,
                category = state.category,
                severity = state.severity,
                title = state.title.trim(),
                description = state.description.trim(),
                stepsToReproduce = state.stepsToReproduce.trim(),
                currentRoute = state.currentRoute,
                appVersion = state.appVersion,
                deviceModel = state.deviceModel,
                androidOsVersion = state.androidVersion,
                timestamp = System.currentTimeMillis()
            )

            val result = feedbackRepository.submitFeedback(feedback)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        isSuccess = true,
                        successMessage = "تم إرسال ملاحظتك بنجاح لفريق التطوير. جزاك الله خيراً!",
                        title = "",
                        description = "",
                        stepsToReproduce = ""
                    )
                }
                onComplete()
            } else {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        isSuccess = false,
                        errorMessage = "تعذر إرسال الملاحظة: ${result.exceptionOrNull()?.localizedMessage ?: "خطأ غير معروف"}"
                    )
                }
            }
        }
    }
}

class BetaFeedbackViewModelFactory(
    private val feedbackRepository: BetaFeedbackRepository = FirebaseBetaFeedbackRepositoryImpl(),
    private val authRepository: AuthRepository = FirebaseAuthRepositoryImpl()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BetaFeedbackViewModel(feedbackRepository, authRepository) as T
    }
}
