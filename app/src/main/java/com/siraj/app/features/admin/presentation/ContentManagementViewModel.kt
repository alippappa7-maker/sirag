package com.siraj.app.features.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siraj.app.domain.models.admin.AdminContentItem
import com.siraj.app.domain.models.admin.AdminContentStatus
import com.siraj.app.domain.models.admin.ContentManagementFilter
import com.siraj.app.domain.models.admin.AuditLogEntry
import com.siraj.app.domain.repository.admin.ContentManagementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ContentManagementState(
    val isLoading: Boolean = false,
    val items: List<AdminContentItem> = emptyList(),
    val filter: ContentManagementFilter = ContentManagementFilter(),
    val error: String? = null,
    val selectedAuditLogs: List<AuditLogEntry> = emptyList(),
    val reportUrl: String? = null,
)

class ContentManagementViewModel(
    private val repository: ContentManagementRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ContentManagementState())
    val state: StateFlow<ContentManagementState> = _state.asStateFlow()

    init {
        loadContent()
    }

    private fun loadContent() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                repository
                    .getManagedContent(_state.value.filter, page = 1, limit = 20)
                    .collectLatest { items ->
                        _state.update { it.copy(isLoading = false, items = items) }
                    }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateFilter(
        query: String? = null,
        type: String? = null,
        status: AdminContentStatus? = null,
    ) {
        _state.update {
            it.copy(
                filter =
                    it.filter.copy(
                        query = query ?: it.filter.query,
                        type = type,
                        status = status,
                    ),
            )
        }
        loadContent() // Re-trigger collection
    }

    fun approveContent(contentId: String) {
        viewModelScope.launch {
            repository.updateContentStatus(contentId, AdminContentStatus.APPROVED, "Approved by Admin")
        }
    }

    fun suspendContent(contentId: String) {
        viewModelScope.launch {
            repository.updateContentStatus(contentId, AdminContentStatus.SUSPENDED, "Suspended by Admin")
        }
    }

    fun archiveContent(contentId: String) {
        viewModelScope.launch {
            repository.archiveContent(contentId)
        }
    }

    fun restoreContent(contentId: String) {
        viewModelScope.launch {
            repository.restoreContent(contentId)
        }
    }

    fun loadAuditLogs(contentId: String) {
        viewModelScope.launch {
            val logs = repository.getAuditLogs(contentId)
            _state.update { it.copy(selectedAuditLogs = logs) }
        }
    }

    fun exportReport() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val url = repository.exportAdminReport()
                _state.update { it.copy(isLoading = false, reportUrl = url) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun clearReportUrl() {
        _state.update { it.copy(reportUrl = null) }
    }
}
