package com.siraj.app.features.cost.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.cost.CostProvider
import com.siraj.app.domain.models.cost.ProviderEmergencyStatus
import com.siraj.app.domain.models.cost.WorkspaceUsageStatus
import com.siraj.app.domain.repository.cost.CostManagementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CostDashboardViewModel(
    private val repository: CostManagementRepository,
    private val workspaceId: String = "ws_test_123"
) : ViewModel() {

    private val _usageState = MutableStateFlow<WorkspaceUsageStatus?>(null)
    val usageState: StateFlow<WorkspaceUsageStatus?> = _usageState.asStateFlow()

    private val _providerStatuses = MutableStateFlow<List<ProviderEmergencyStatus>>(emptyList())
    val providerStatuses: StateFlow<List<ProviderEmergencyStatus>> = _providerStatuses.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getWorkspaceUsage(workspaceId).collect { status ->
                _usageState.value = status
            }
        }
        
        viewModelScope.launch {
            repository.getProviderEmergencyStatuses().collect { statuses ->
                _providerStatuses.value = statuses
            }
        }
    }

    fun toggleProviderStatus(provider: CostProvider, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.setProviderEmergencyStatus(provider, isEnabled, "admin_current_user")
        }
    }
}
