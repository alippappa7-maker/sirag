package com.siraj.app.features.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.admin.AdminRole
import com.siraj.app.domain.models.admin.AdminSecurityConfig
import com.siraj.app.domain.models.admin.AdminSession
import com.siraj.app.domain.models.admin.SecurityAuditLog
import com.siraj.app.domain.repository.admin.AdminSecurityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminSecurityViewModel(
    private val repository: AdminSecurityRepository,
    private val currentAdminId: String = "admin_1"
) : ViewModel() {

    private val _config = MutableStateFlow<AdminSecurityConfig?>(null)
    val config: StateFlow<AdminSecurityConfig?> = _config.asStateFlow()

    private val _activeSessions = MutableStateFlow<List<AdminSession>>(emptyList())
    val activeSessions: StateFlow<List<AdminSession>> = _activeSessions.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<SecurityAuditLog>>(emptyList())
    val auditLogs: StateFlow<List<SecurityAuditLog>> = _auditLogs.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val configRes = repository.getAdminSecurityConfig(currentAdminId)
            if (configRes is Resource.Success) {
                _config.value = configRes.data
            }

            repository.getActiveSessions(currentAdminId).collect { sessions ->
                _activeSessions.value = sessions
            }
        }

        viewModelScope.launch {
            repository.getAuditLogs().collect { logs ->
                _auditLogs.value = logs
            }
        }
    }

    fun revokeSession(sessionId: String) {
        viewModelScope.launch {
            repository.revokeSession(sessionId, currentAdminId)
        }
    }
}
