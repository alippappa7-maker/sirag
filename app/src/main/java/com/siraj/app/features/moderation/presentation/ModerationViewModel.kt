package com.siraj.app.features.moderation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.community.*
import com.siraj.app.domain.repository.community.SafetyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ModerationStateUI(
    val isLoading: Boolean = false,
    val reports: List<Report> = emptyList(),
    val ugcItems: List<UgcItem> = emptyList(),
    val appeals: List<UgcAppeal> = emptyList(),
    val logs: List<ModerationDecisionLog> = emptyList(),
    val selectedTab: Int = 0, // 0: Reports, 1: UGC Queue, 2: Appeals, 3: Analytics & SLA
    val filterUgcState: UgcState? = null,
    val error: String? = null,
    val successMessage: String? = null
) {
    val totalPendingReports: Int
        get() = reports.count { it.status == ReportStatus.PENDING }

    val overdueReportsCount: Int
        get() = reports.count { it.isOverdue }

    val pendingAppealsCount: Int
        get() = appeals.count { it.status == AppealStatus.PENDING }

    val pendingUgcReviewCount: Int
        get() = ugcItems.count { it.state == UgcState.PENDING_REVIEW || it.state == UgcState.SCANNING }
}

class ModerationViewModel(
    private val safetyRepository: SafetyRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ModerationStateUI())
    val state: StateFlow<ModerationStateUI> = _state.asStateFlow()

    fun setSelectedTab(tabIndex: Int) {
        _state.value = _state.value.copy(selectedTab = tabIndex)
    }

    fun setUgcFilter(state: UgcState?) {
        _state.value = _state.value.copy(filterUgcState = state)
    }

    fun loadAll(role: String) {
        loadReports(role)
        loadUgcQueue(role)
        loadAppeals()
        loadLogs()
    }

    fun loadReports(role: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val res = safetyRepository.getPendingReports(role)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(isLoading = false, reports = res.data ?: emptyList())
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = res.message)
                }
                else -> {}
            }
        }
    }

    fun loadUgcQueue(role: String) {
        viewModelScope.launch {
            when (val res = safetyRepository.getUgcQueue(role, _state.value.filterUgcState)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(ugcItems = res.data ?: emptyList())
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(error = res.message)
                }
                else -> {}
            }
        }
    }

    fun loadAppeals() {
        viewModelScope.launch {
            when (val res = safetyRepository.getAppeals()) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(appeals = res.data ?: emptyList())
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(error = res.message)
                }
                else -> {}
            }
        }
    }

    fun loadLogs() {
        viewModelScope.launch {
            when (val res = safetyRepository.getAllModerationLogs()) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(logs = res.data ?: emptyList())
                }
                is Resource.Error -> {}
                else -> {}
            }
        }
    }

    fun resolveReport(reportId: String, resolverId: String, resolution: String, notes: String, role: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            when (val res = safetyRepository.resolveReport(reportId, resolverId, resolution, notes)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(successMessage = "تم معالجة البلاغ بنجاح وتوثيق القرار")
                    loadAll(role)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = res.message)
                }
                else -> {}
            }
        }
    }

    fun takeUgcAction(ugcId: String, moderatorId: String, action: ModeratorAction, notes: String, role: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            when (val res = safetyRepository.takeModeratorActionOnUgc(ugcId, moderatorId, action, notes)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(successMessage = "تم اتخاذ الإجراء (${action.titleArabic}) بنجاح")
                    loadAll(role)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = res.message)
                }
                else -> {}
            }
        }
    }

    fun resolveAppeal(appealId: String, moderatorId: String, isApproved: Boolean, notes: String, role: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            when (val res = safetyRepository.resolveAppeal(appealId, moderatorId, isApproved, notes)) {
                is Resource.Success -> {
                    val outcome = if (isApproved) "تم قبول الاستئناف واستعادة المحتوى" else "تم رفض الاستئناف وتأييد القرار"
                    _state.value = _state.value.copy(successMessage = outcome)
                    loadAll(role)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = res.message)
                }
                else -> {}
            }
        }
    }

    fun suspendUser(userId: String, moderatorId: String, reason: String, days: Int, role: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            when (val res = safetyRepository.suspendUserAccount(userId, moderatorId, reason, days)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(successMessage = "تم إيقاف حساب المستخدم لمدة $days يوم")
                    loadAll(role)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = res.message)
                }
                else -> {}
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(error = null, successMessage = null)
    }
}

class ModerationViewModelFactory(
    private val repository: SafetyRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ModerationViewModel(repository) as T
    }
}

