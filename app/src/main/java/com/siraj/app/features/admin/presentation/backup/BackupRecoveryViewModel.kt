package com.siraj.app.features.admin.presentation.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siraj.app.domain.models.backup.BackupEnvironment
import com.siraj.app.domain.models.backup.BackupRetentionPolicy
import com.siraj.app.domain.models.backup.BackupSnapshot
import com.siraj.app.domain.models.backup.BackupType
import com.siraj.app.domain.models.backup.DisasterRecoveryPlan
import com.siraj.app.domain.models.backup.RestoreJob
import com.siraj.app.domain.models.backup.RestoreTargetEnvironment
import com.siraj.app.domain.repository.backup.BackupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class BackupRecoveryUiState(
    val snapshots: List<BackupSnapshot> = emptyList(),
    val filteredSnapshots: List<BackupSnapshot> = emptyList(),
    val restoreJobs: List<RestoreJob> = emptyList(),
    val drPlan: DisasterRecoveryPlan = DisasterRecoveryPlan(),
    val retentionPolicy: BackupRetentionPolicy = BackupRetentionPolicy(),
    val selectedEnvironment: BackupEnvironment? = null,
    val selectedSnapshot: BackupSnapshot? = null,
    val activeDryRunJob: RestoreJob? = null,
    val isLoading: Boolean = false,
    val isTriggeringBackup: Boolean = false,
    val isExecutingDryRun: Boolean = false,
    val showCreateBackupDialog: Boolean = false,
    val showDryRunModal: Boolean = false,
    val showProjectRestoreModal: Boolean = false,
    val showDrRunbookModal: Boolean = false,
    val bannerMessage: String? = null,
    val deletedUsersTombstoneCount: Int = 14,
)

class BackupRecoveryViewModel(
    private val backupRepository: BackupRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BackupRecoveryUiState())
    val uiState: StateFlow<BackupRecoveryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            combine(
                backupRepository.getBackupSnapshots(),
                backupRepository.getRestoreJobs(),
                backupRepository.getDisasterRecoveryPlan(),
                backupRepository.getRetentionPolicy(),
            ) { snapshots, jobs, drPlan, retention ->
                val env = _uiState.value.selectedEnvironment
                val filtered = if (env != null) snapshots.filter { it.environment == env } else snapshots
                _uiState.value.copy(
                    snapshots = snapshots,
                    filteredSnapshots = filtered,
                    restoreJobs = jobs,
                    drPlan = drPlan,
                    retentionPolicy = retention,
                    isLoading = false,
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }

        viewModelScope.launch {
            val count = backupRepository.getDeletedUsersTombstoneCount()
            _uiState.value = _uiState.value.copy(deletedUsersTombstoneCount = count)
        }
    }

    fun setEnvironmentFilter(environment: BackupEnvironment?) {
        val currentSnapshots = _uiState.value.snapshots
        val filtered = if (environment != null) currentSnapshots.filter { it.environment == environment } else currentSnapshots
        _uiState.value =
            _uiState.value.copy(
                selectedEnvironment = environment,
                filteredSnapshots = filtered,
            )
    }

    fun selectSnapshot(snapshot: BackupSnapshot?) {
        _uiState.value = _uiState.value.copy(selectedSnapshot = snapshot)
    }

    fun setCreateBackupDialogVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showCreateBackupDialog = visible)
    }

    fun setDryRunModalVisible(
        visible: Boolean,
        snapshot: BackupSnapshot? = null,
    ) {
        _uiState.value =
            _uiState.value.copy(
                showDryRunModal = visible,
                selectedSnapshot = snapshot ?: _uiState.value.selectedSnapshot,
            )
    }

    fun setProjectRestoreModalVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showProjectRestoreModal = visible)
    }

    fun setDrRunbookModalVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showDrRunbookModal = visible)
    }

    fun clearBannerMessage() {
        _uiState.value = _uiState.value.copy(bannerMessage = null)
    }

    fun triggerNewBackup(
        type: BackupType,
        environment: BackupEnvironment,
        notes: String,
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTriggeringBackup = true, showCreateBackupDialog = false)
            val result = backupRepository.triggerBackup(type, environment, notes)
            result
                .onSuccess { snapshot ->
                    _uiState.value =
                        _uiState.value.copy(
                            isTriggeringBackup = false,
                            bannerMessage = "تم إنشاء النسخة الاحتياطية بنجاح (${snapshot.id}) ومطابقتها بتشفير CMEK",
                        )
                }.onFailure { error ->
                    _uiState.value =
                        _uiState.value.copy(
                            isTriggeringBackup = false,
                            bannerMessage = "فشل إنشاء النسخة: ${error.localizedMessage}",
                        )
                }
        }
    }

    fun executeDryRunRestore(
        snapshotId: String,
        targetEnv: RestoreTargetEnvironment = RestoreTargetEnvironment.ISOLATED_RECOVERY_SANDBOX,
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExecutingDryRun = true)
            val result = backupRepository.executeDryRunRestore(snapshotId, targetEnv)
            result
                .onSuccess { job ->
                    _uiState.value =
                        _uiState.value.copy(
                            isExecutingDryRun = false,
                            activeDryRunJob = job,
                            bannerMessage = "نجح اختبار الاستعادة التجريبي للنسخة $snapshotId مع استبعاد وتطهير بيانات الحذف بالكامل",
                        )
                }.onFailure { error ->
                    _uiState.value =
                        _uiState.value.copy(
                            isExecutingDryRun = false,
                            bannerMessage = "فشل اختبار الاستعادة: ${error.localizedMessage}",
                        )
                }
        }
    }

    fun executeProjectRestore(
        projectId: String,
        targetWorkspaceId: String,
        snapshotId: String,
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, showProjectRestoreModal = false)
            val result = backupRepository.restoreProjectFromSnapshot(snapshotId, projectId, targetWorkspaceId)
            result
                .onSuccess { job ->
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            bannerMessage = "تمت استعادة المشروع $projectId بنجاح في مساحة العمل $targetWorkspaceId",
                        )
                }.onFailure { error ->
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            bannerMessage = "فشلت استعادة المشروع: ${error.localizedMessage}",
                        )
                }
        }
    }
}
