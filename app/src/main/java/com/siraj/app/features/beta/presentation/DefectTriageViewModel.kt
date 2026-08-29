package com.siraj.app.features.beta.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.data.repository.FirebaseBetaDefectManagementRepositoryImpl
import com.siraj.app.domain.models.beta.BetaDefectRecord
import com.siraj.app.domain.models.beta.DefectClassification
import com.siraj.app.domain.models.beta.DefectDomain
import com.siraj.app.domain.models.beta.DefectPriority
import com.siraj.app.domain.models.beta.DefectStatus
import com.siraj.app.domain.models.beta.DefectTriageSummary
import com.siraj.app.domain.repository.BetaDefectManagementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DefectTriageUiState(
    val allDefects: List<BetaDefectRecord> = emptyList(),
    val filteredDefects: List<BetaDefectRecord> = emptyList(),
    val summary: DefectTriageSummary = DefectTriageSummary(),
    val selectedClassification: DefectClassification? = null,
    val selectedDomain: DefectDomain? = null,
    val selectedStatus: DefectStatus? = null,
    val searchQuery: String = "",
    val isPrioritizedView: Boolean = true,
    val selectedDefect: BetaDefectRecord? = null,
    val isTriageDialogOpen: Boolean = false,
    val isStatusDialogOpen: Boolean = false,
    val isLoading: Boolean = false,
    val userMessage: String? = null,
    val errorMessage: String? = null
)

class DefectTriageViewModel(
    private val repository: BetaDefectManagementRepository = FirebaseBetaDefectManagementRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DefectTriageUiState(isLoading = true))
    val uiState: StateFlow<DefectTriageUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getAllDefects().collectLatest { defects ->
                _uiState.update { current ->
                    current.copy(
                        allDefects = defects,
                        isLoading = false
                    )
                }
                applyFilterAndSorting()
            }
        }

        viewModelScope.launch {
            repository.getTriageSummary().collectLatest { summary ->
                _uiState.update { it.copy(summary = summary) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilterAndSorting()
    }

    fun onSelectClassificationFilter(classification: DefectClassification?) {
        _uiState.update {
            it.copy(
                selectedClassification = if (it.selectedClassification == classification) null else classification
            )
        }
        applyFilterAndSorting()
    }

    fun onSelectDomainFilter(domain: DefectDomain?) {
        _uiState.update {
            it.copy(
                selectedDomain = if (it.selectedDomain == domain) null else domain
            )
        }
        applyFilterAndSorting()
    }

    fun onSelectStatusFilter(status: DefectStatus?) {
        _uiState.update {
            it.copy(
                selectedStatus = if (it.selectedStatus == status) null else status
            )
        }
        applyFilterAndSorting()
    }

    fun togglePrioritizedView(enabled: Boolean) {
        _uiState.update { it.copy(isPrioritizedView = enabled) }
        applyFilterAndSorting()
    }

    fun selectDefectForDetails(defect: BetaDefectRecord?) {
        _uiState.update { it.copy(selectedDefect = defect) }
    }

    fun openTriageDialog(defect: BetaDefectRecord) {
        _uiState.update { it.copy(selectedDefect = defect, isTriageDialogOpen = true) }
    }

    fun closeTriageDialog() {
        _uiState.update { it.copy(isTriageDialogOpen = false) }
    }

    fun openStatusDialog(defect: BetaDefectRecord) {
        _uiState.update { it.copy(selectedDefect = defect, isStatusDialogOpen = true) }
    }

    fun closeStatusDialog() {
        _uiState.update { it.copy(isStatusDialogOpen = false) }
    }

    fun dismissUserMessage() {
        _uiState.update { it.copy(userMessage = null, errorMessage = null) }
    }

    fun applyTriage(
        defectId: String,
        classification: DefectClassification,
        priority: DefectPriority,
        assignedRole: String,
        targetRelease: String
    ) {
        viewModelScope.launch {
            val result = repository.triageDefect(
                id = defectId,
                classification = classification,
                priority = priority,
                assignedRole = assignedRole,
                targetRelease = targetRelease
            )
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isTriageDialogOpen = false,
                        userMessage = "تم تحديث فرز وتصنيف العيب بنجاح"
                    )
                }
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message ?: "فشل في حفظ الفرز") }
            }
        }
    }

    fun updateStatus(
        defectId: String,
        newStatus: DefectStatus,
        resolutionNote: String?,
        closureReason: String?,
        verificationTest: String?
    ) {
        viewModelScope.launch {
            val result = repository.updateDefectStatus(
                id = defectId,
                newStatus = newStatus,
                resolutionNote = resolutionNote,
                closureReason = closureReason,
                verificationTest = verificationTest
            )
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isStatusDialogOpen = false,
                        userMessage = "تم تحديث حالة العيب بنجاح إلى: ${newStatus.titleAr}"
                    )
                }
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message ?: "فشل في تحديث الحالة") }
            }
        }
    }

    private fun applyFilterAndSorting() {
        val current = _uiState.value
        val filtered = current.allDefects.filter { defect ->
            val matchesClassification = current.selectedClassification == null || defect.classification == current.selectedClassification
            val matchesDomain = current.selectedDomain == null || defect.domain == current.selectedDomain
            val matchesStatus = current.selectedStatus == null || defect.status == current.selectedStatus
            val query = current.searchQuery.trim().lowercase()
            val matchesQuery = query.isEmpty() ||
                defect.id.lowercase().contains(query) ||
                defect.title.lowercase().contains(query) ||
                defect.description.lowercase().contains(query) ||
                defect.deviceModel.lowercase().contains(query) ||
                defect.assignedRole.lowercase().contains(query) ||
                defect.targetRelease.lowercase().contains(query)

            matchesClassification && matchesDomain && matchesStatus && matchesQuery
        }

        val sorted = if (current.isPrioritizedView) {
            filtered.sortedWith(
                compareBy<BetaDefectRecord> {
                    // الترتيب الصارم: مفتوح أولاً ثم حسب الأولوية ثم التصنيف
                    when (it.status) {
                        DefectStatus.CLOSED, DefectStatus.DEFERRED -> 1
                        else -> 0
                    }
                }
                .thenBy { it.priority.orderWeight }
                .thenBy {
                    when (it.classification) {
                        DefectClassification.BLOCKER -> 0
                        DefectClassification.CRITICAL -> 1
                        DefectClassification.MAJOR -> 2
                        DefectClassification.MINOR -> 3
                        DefectClassification.ENHANCEMENT -> 4
                        DefectClassification.DUPLICATE -> 5
                        DefectClassification.NOT_REPRODUCIBLE -> 6
                        DefectClassification.EXPECTED_BEHAVIOR -> 7
                    }
                }
                .thenByDescending { it.updatedAt }
            )
        } else {
            filtered.sortedByDescending { it.updatedAt }
        }

        _uiState.update { it.copy(filteredDefects = sorted) }
    }
}

class DefectTriageViewModelFactory(
    private val repository: BetaDefectManagementRepository = FirebaseBetaDefectManagementRepositoryImpl()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DefectTriageViewModel::class.java)) {
            return DefectTriageViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
