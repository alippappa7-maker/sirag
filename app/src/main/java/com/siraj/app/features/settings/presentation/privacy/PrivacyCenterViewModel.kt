package com.siraj.app.features.settings.presentation.privacy

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.analytics.AnalyticsManager
import com.siraj.app.core.monitoring.CrashMonitoringManager
import com.siraj.app.core.utils.Resource
import com.siraj.app.data.repository.FirebasePrivacyRepositoryImpl
import com.siraj.app.domain.models.UserPreferences
import com.siraj.app.domain.models.privacy.*
import com.siraj.app.domain.repository.privacy.PrivacyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

enum class PrivacyDialogType {
    NONE,
    STORED_DATA_OVERVIEW,
    EXPORT_DATA,
    EXPORT_SUCCESS,
    CLEAR_HISTORY_CONFIRM,
    CLEAR_DOWNLOADS_CONFIRM,
    CLEAR_CACHE_CONFIRM,
    DATA_CORRECTION,
    DELETE_ACCOUNT_WARNING,
    DELETE_ACCOUNT_CONFIRM,
    RETENTION_POLICY_DETAILS,
    TERMS_AND_PRIVACY_VIEWER,
}

data class PrivacyCenterUiState(
    val overview: PrivacyOverviewData = PrivacyOverviewData(),
    val isLoading: Boolean = false,
    val isExporting: Boolean = false,
    val isPerformingAction: Boolean = false,
    val activeDialog: PrivacyDialogType = PrivacyDialogType.NONE,
    val exportedJson: String? = null,
    val exportedFile: File? = null,
    val lastExportChecksum: String? = null,
    val actionMessage: String? = null,
    val errorMessage: String? = null,
)

class PrivacyCenterViewModel(
    application: Application,
    private val repository: PrivacyRepository = FirebasePrivacyRepositoryImpl(application),
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(PrivacyCenterUiState())
    val uiState = _uiState.asStateFlow()

    fun loadOverview(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val overview = repository.getPrivacyOverview(userId)
            _uiState.update {
                it.copy(
                    overview = overview,
                    isLoading = false,
                )
            }
        }
    }

    fun showDialog(type: PrivacyDialogType) {
        _uiState.update { it.copy(activeDialog = type) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(activeDialog = PrivacyDialogType.NONE) }
    }

    fun exportUserData(
        context: Context,
        userId: String,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, actionMessage = null, errorMessage = null) }
            val exportResult = repository.generateUserDataExport(userId)
            if (exportResult is Resource.Success && exportResult.data != null) {
                val jsonResult = repository.exportUserDataToJson(userId)
                if (jsonResult is Resource.Success && jsonResult.data != null) {
                    val fileResult = repository.saveExportJsonToFile(context, jsonResult.data)
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            exportedJson = jsonResult.data,
                            exportedFile = (fileResult as? Resource.Success)?.data,
                            lastExportChecksum = exportResult.data.sha256Checksum,
                            activeDialog = PrivacyDialogType.EXPORT_SUCCESS,
                            actionMessage = "تم تجهيز ملف التصدير بنجاح وتأمينه بتشفير SHA-256",
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            errorMessage =
                                (jsonResult as? Resource.Error)?.message ?: "فشل استخراج التصدير",
                        )
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        errorMessage =
                            (exportResult as? Resource.Error)?.message ?: "فشل توليد التصدير",
                    )
                }
            }
        }
    }

    fun clearWatchHistory(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPerformingAction = true) }
            val result = repository.clearWatchHistory(userId)
            if (result is Resource.Success) {
                _uiState.update {
                    it.copy(
                        isPerformingAction = false,
                        activeDialog = PrivacyDialogType.NONE,
                        actionMessage = "تم مسح سجل المشاهدة والاستماع بنجاح",
                    )
                }
                loadOverview(userId)
            } else {
                _uiState.update {
                    it.copy(isPerformingAction = false, errorMessage = (result as? Resource.Error)?.message ?: "فشل مسح السجل")
                }
            }
        }
    }

    fun clearDownloads(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPerformingAction = true) }
            val result = repository.clearDownloads(userId)
            if (result is Resource.Success) {
                _uiState.update {
                    it.copy(
                        isPerformingAction = false,
                        activeDialog = PrivacyDialogType.NONE,
                        actionMessage = "تم حذف كافة المقاطع المحملة محلياً",
                    )
                }
                loadOverview(userId)
            } else {
                _uiState.update {
                    it.copy(
                        isPerformingAction = false,
                        errorMessage =
                            (result as? Resource.Error)?.message ?: "فشل حذف التنزيلات",
                    )
                }
            }
        }
    }

    fun clearAppCache(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPerformingAction = true) }
            val result = repository.clearLocalCache(context)
            if (result is Resource.Success) {
                _uiState.update {
                    it.copy(
                        isPerformingAction = false,
                        activeDialog = PrivacyDialogType.NONE,
                        actionMessage = "تم تفريغ الذاكرة المؤقتة بنجاح",
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isPerformingAction = false,
                        errorMessage =
                            (result as? Resource.Error)?.message ?: "فشل تفريغ الذاكرة",
                    )
                }
            }
        }
    }

    fun deleteProject(
        projectId: String,
        userId: String,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPerformingAction = true) }
            val result = repository.deleteUserProject(projectId)
            if (result is Resource.Success) {
                _uiState.update {
                    it.copy(
                        isPerformingAction = false,
                        actionMessage = "تم حذف المشروع نهائياً",
                    )
                }
                loadOverview(userId)
            } else {
                _uiState.update {
                    it.copy(
                        isPerformingAction = false,
                        errorMessage =
                            (result as? Resource.Error)?.message ?: "فشل حذف المشروع",
                    )
                }
            }
        }
    }

    fun toggleAnalytics(
        enabled: Boolean,
        onUpdatePref: (UserPreferences) -> Unit,
    ) {
        AnalyticsManager.setAnalyticsEnabled(enabled)
        onUpdatePref(UserPreferences(analyticsOptIn = enabled))
        _uiState.update {
            it.copy(actionMessage = if (enabled) "تم تفعيل مشاركة بيانات الاستخدام المجهولة" else "تم إيقاف وتعطيل جمع التحليلات بالكامل")
        }
    }

    fun toggleCrashReports(
        enabled: Boolean,
        onUpdatePref: (UserPreferences) -> Unit,
    ) {
        CrashMonitoringManager.setCrashlyticsCollectionEnabled(enabled)
        onUpdatePref(UserPreferences(crashReportsOptIn = enabled))
        _uiState.update {
            it.copy(actionMessage = if (enabled) "تم تفعيل مشاركة تقارير الأعطال الفنية" else "تم تعطيل إرسال تقارير الأعطال")
        }
    }

    fun togglePersonalization(
        enabled: Boolean,
        onUpdatePref: (UserPreferences) -> Unit,
    ) {
        onUpdatePref(UserPreferences(personalizationOptIn = enabled))
        _uiState.update {
            it.copy(
                actionMessage = if (enabled) "تم تفعيل تخصيص المحتوى والتوصيات" else "تم تعطيل التخصيص، سيتم عرض المحتوى زمنيًا ومحرريًا فقط",
            )
        }
    }

    fun toggleLocationOptIn(
        enabled: Boolean,
        onUpdatePref: (UserPreferences) -> Unit,
    ) {
        onUpdatePref(UserPreferences(locationOptIn = enabled))
        _uiState.update {
            it.copy(
                actionMessage = if (enabled) "تم تفعيل استخدام الموقع لمواقيت الصلاة والقبلة" else "تم تعطيل استخدام الموقع الجغرافي بالكامل",
            )
        }
    }

    fun togglePreciseLocation(
        enabled: Boolean,
        onUpdatePref: (UserPreferences) -> Unit,
    ) {
        onUpdatePref(UserPreferences(preciseLocationOptIn = enabled))
        _uiState.update {
            it.copy(actionMessage = if (enabled) "تم تفعيل الموقع الدقيق (GPS)" else "تم تفعيل وضع الموقع التقريبي (حماية الخصوصية)")
        }
    }

    fun submitDataCorrection(
        userId: String,
        fieldName: String,
        currentValue: String,
        requestedValue: String,
        reason: String,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPerformingAction = true) }
            val req =
                DataCorrectionRequest(
                    userId = userId,
                    fieldName = fieldName,
                    currentValue = currentValue,
                    requestedValue = requestedValue,
                    reason = reason,
                )
            val result = repository.submitDataCorrection(req)
            if (result is Resource.Success) {
                _uiState.update {
                    it.copy(
                        isPerformingAction = false,
                        activeDialog = PrivacyDialogType.NONE,
                        actionMessage = "تم إرسال طلب تصحيح البيانات إلى فريق الخصوصية بنجاح",
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isPerformingAction = false,
                        errorMessage =
                            (result as? Resource.Error)?.message ?: "فشل تقديم طلب التصحيح",
                    )
                }
            }
        }
    }

    fun requestAccountDeletion(
        userId: String,
        reason: String,
        gracePeriodDays: Int = 14,
        onScheduled: () -> Unit,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPerformingAction = true) }
            val result = repository.requestAccountDeletion(userId, reason, gracePeriodDays)
            if (result is Resource.Success) {
                _uiState.update {
                    it.copy(
                        isPerformingAction = false,
                        activeDialog = PrivacyDialogType.NONE,
                        actionMessage = "تم تسجيل طلب حذف الحساب. ستبدأ فترة سماح لمدة $gracePeriodDays يوماً قبل الحذف النهائي والتطهير.",
                    )
                }
                loadOverview(userId)
                onScheduled()
            } else {
                _uiState.update {
                    it.copy(
                        isPerformingAction = false,
                        errorMessage =
                            (result as? Resource.Error)?.message ?: "فشل تقديم طلب الحذف",
                    )
                }
            }
        }
    }

    fun cancelAccountDeletion(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPerformingAction = true) }
            val result = repository.cancelAccountDeletion(userId)
            if (result is Resource.Success) {
                _uiState.update {
                    it.copy(
                        isPerformingAction = false,
                        actionMessage = "تم إلغاء طلب حذف الحساب بنجاح واستعادة الحساب بالكامل.",
                    )
                }
                loadOverview(userId)
            } else {
                _uiState.update {
                    it.copy(
                        isPerformingAction = false,
                        errorMessage =
                            (result as? Resource.Error)?.message ?: "فشل إلغاء طلب الحذف",
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(actionMessage = null, errorMessage = null) }
    }
}

class PrivacyCenterViewModelFactory(
    private val application: Application,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrivacyCenterViewModel::class.java)) {
            return PrivacyCenterViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
