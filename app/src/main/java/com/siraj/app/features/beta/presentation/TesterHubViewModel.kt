package com.siraj.app.features.beta.presentation

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.config.EnvironmentConfig
import com.siraj.app.data.repository.FirebaseAuthRepositoryImpl
import com.siraj.app.data.repository.beta.FirebaseBetaTesterDistributionRepositoryImpl
import com.siraj.app.domain.models.beta.*
import com.siraj.app.domain.repository.AuthRepository
import com.siraj.app.domain.repository.BetaTesterDistributionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TesterHubUiState(
    val isLoading: Boolean = false,
    val selectedTab: Int = 0,
    val testerProfile: BetaTesterProfile? = null,
    val criticalJourneys: List<CriticalJourney> = emptyList(),
    val releaseNotes: List<BetaReleaseNote> = emptyList(),
    val distributionChannels: List<DistributionChannelInfo> = emptyList(),
    val deviceModel: String = "${Build.MANUFACTURER} ${Build.MODEL}",
    val osVersion: String = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
    val appVersion: String = EnvironmentConfig.versionName,
    val buildCode: Int = EnvironmentConfig.versionCode,
    // Survey State
    val overallRating: Int = 5,
    val easeOfUseRating: Int = 5,
    val shariaContentRating: Int = 5,
    val performanceRating: Int = 5,
    val mostValuableFeature: String = "",
    val biggestPainPoint: String = "",
    val generalSuggestions: String = "",
    val isSubmittingSurvey: Boolean = false,
    val surveySuccess: Boolean = false,
    val surveyErrorMessage: String? = null,
    val surveySuccessMessage: String? = null,
    // Revocation / Leave Testing Program State
    val isRevoking: Boolean = false,
    val isRevoked: Boolean = false,
    val revocationMessage: String? = null,
)

class TesterHubViewModel(
    private val distributionRepository: BetaTesterDistributionRepository = FirebaseBetaTesterDistributionRepositoryImpl(),
    private val authRepository: AuthRepository = FirebaseAuthRepositoryImpl(),
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(
            TesterHubUiState(
                criticalJourneys = distributionRepository.getCriticalJourneys(),
                releaseNotes = distributionRepository.getReleaseNotes(),
                distributionChannels = distributionRepository.getDistributionChannels(),
            ),
        )
    val uiState: StateFlow<TesterHubUiState> = _uiState.asStateFlow()

    init {
        initTesterSession()
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun initTesterSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val user = authRepository.currentUser.firstOrNull()
            val userId = user?.id ?: "guest_beta_tester"
            val userEmail = user?.email ?: "tester@siraj.app"
            val userName = user?.name ?: "مختبر معتمد"

            // Register session & telemetry info
            distributionRepository.registerTesterSession(
                testerId = userId,
                email = userEmail,
                name = userName,
                deviceModel = _uiState.value.deviceModel,
                osVersion = _uiState.value.osVersion,
                appVersion = _uiState.value.appVersion,
                buildCode = _uiState.value.buildCode,
            )

            // Listen to profile updates
            distributionRepository.getTesterProfile(userId).collect { profile ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        testerProfile =
                            profile ?: BetaTesterProfile(
                                testerId = userId,
                                email = userEmail,
                                name = userName,
                                deviceModel = it.deviceModel,
                                osVersion = it.osVersion,
                                installedAppVersion = it.appVersion,
                                installedBuildCode = it.buildCode,
                            ),
                    )
                }
            }
        }
    }

    fun markJourneyCompleted(journeyId: String) {
        viewModelScope.launch {
            val testerId = _uiState.value.testerProfile?.testerId ?: return@launch
            distributionRepository.recordJourneyCompletion(testerId, journeyId)
        }
    }

    fun updateOverallRating(rating: Int) {
        _uiState.update { it.copy(overallRating = rating) }
    }

    fun updateEaseOfUseRating(rating: Int) {
        _uiState.update { it.copy(easeOfUseRating = rating) }
    }

    fun updateShariaContentRating(rating: Int) {
        _uiState.update { it.copy(shariaContentRating = rating) }
    }

    fun updatePerformanceRating(rating: Int) {
        _uiState.update { it.copy(performanceRating = rating) }
    }

    fun updateMostValuableFeature(text: String) {
        _uiState.update { it.copy(mostValuableFeature = text, surveyErrorMessage = null) }
    }

    fun updateBiggestPainPoint(text: String) {
        _uiState.update { it.copy(biggestPainPoint = text, surveyErrorMessage = null) }
    }

    fun updateGeneralSuggestions(text: String) {
        _uiState.update { it.copy(generalSuggestions = text) }
    }

    fun clearSurveyStatus() {
        _uiState.update { it.copy(surveySuccess = false, surveyErrorMessage = null, surveySuccessMessage = null) }
    }

    fun submitSurvey() {
        val state = _uiState.value
        val user = _uiState.value.testerProfile
        val testerId = user?.testerId ?: "guest_tester"
        val testerEmail = user?.email ?: "guest@tester.siraj.app"

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingSurvey = true, surveyErrorMessage = null) }

            val survey =
                TesterExperienceSurvey(
                    testerId = testerId,
                    testerEmail = testerEmail,
                    overallRating = state.overallRating,
                    easeOfUseRating = state.easeOfUseRating,
                    shariaContentRating = state.shariaContentRating,
                    performanceRating = state.performanceRating,
                    mostValuableFeature = state.mostValuableFeature.trim(),
                    biggestPainPoint = state.biggestPainPoint.trim(),
                    generalSuggestions = state.generalSuggestions.trim(),
                    deviceModel = state.deviceModel,
                    appVersion = state.appVersion,
                )

            val result = distributionRepository.submitExperienceSurvey(survey)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isSubmittingSurvey = false,
                        surveySuccess = true,
                        surveySuccessMessage = "تم حفظ تقييمك بنجاح. شكراً جزيلاً لمساهمتك القيمة في تحسين سراج!",
                        mostValuableFeature = "",
                        biggestPainPoint = "",
                        generalSuggestions = "",
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isSubmittingSurvey = false,
                        surveyErrorMessage = "تعذر إرسال التقييم: ${result.exceptionOrNull()?.localizedMessage ?: "خطأ في الشبكة"}",
                    )
                }
            }
        }
    }

    fun revokeMyTesterAccess(reason: String = "طلب المستخدم الخروج من برنامج الاختبار") {
        val testerId = _uiState.value.testerProfile?.testerId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isRevoking = true) }
            val result = distributionRepository.revokeTesterAccess(testerId, reason)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isRevoking = false,
                        isRevoked = true,
                        revocationMessage = "تم إلغاء تسجيلك وسحب صلاحية حساب الاختبار بنجاح بناءً على طلبك.",
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isRevoking = false,
                        revocationMessage = "تعذر سحب الصلاحية حالياً: ${result.exceptionOrNull()?.localizedMessage}",
                    )
                }
            }
        }
    }
}

class TesterHubViewModelFactory(
    private val distributionRepository: BetaTesterDistributionRepository = FirebaseBetaTesterDistributionRepositoryImpl(),
    private val authRepository: AuthRepository = FirebaseAuthRepositoryImpl(),
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = TesterHubViewModel(distributionRepository, authRepository) as T
}
