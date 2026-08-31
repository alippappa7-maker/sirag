package com.siraj.app.features.studio.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siraj.app.domain.models.analytics.AnalyticsTimeFilter
import com.siraj.app.domain.models.analytics.CreatorAnalyticsDashboard
import com.siraj.app.domain.repository.AuthRepository
import com.siraj.app.domain.repository.analytics.CreatorAnalyticsRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class CreatorAnalyticsUiState {
    object Loading : CreatorAnalyticsUiState()

    data class Success(
        val dashboard: CreatorAnalyticsDashboard,
    ) : CreatorAnalyticsUiState()

    data class Error(
        val message: String,
    ) : CreatorAnalyticsUiState()
}

class CreatorAnalyticsViewModel(
    private val repository: CreatorAnalyticsRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<CreatorAnalyticsUiState>(CreatorAnalyticsUiState.Loading)
    val uiState: StateFlow<CreatorAnalyticsUiState> = _uiState.asStateFlow()

    private val _timeFilter = MutableStateFlow(AnalyticsTimeFilter.LAST_30_DAYS)
    val timeFilter: StateFlow<AnalyticsTimeFilter> = _timeFilter.asStateFlow()

    init {
        loadAnalytics()
    }

    fun setTimeFilter(filter: AnalyticsTimeFilter) {
        _timeFilter.value = filter
        loadAnalytics()
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            _uiState.value = CreatorAnalyticsUiState.Loading
            val user = authRepository.currentUser.firstOrNull()
            if (user == null) {
                _uiState.value = CreatorAnalyticsUiState.Error("يجب تسجيل الدخول لعرض التحليلات.")
                return@launch
            }

            repository
                .getCreatorDashboard(user.id, _timeFilter.value)
                .catch { e ->
                    _uiState.value = CreatorAnalyticsUiState.Error(e.message ?: "حدث خطأ غير معروف")
                }.collect { dashboard ->
                    _uiState.value = CreatorAnalyticsUiState.Success(dashboard)
                }
        }
    }

    suspend fun generateExportReport(): String? {
        val user = authRepository.currentUser.firstOrNull() ?: return null
        return repository.generateExportReport(user.id, _timeFilter.value)
    }
}
