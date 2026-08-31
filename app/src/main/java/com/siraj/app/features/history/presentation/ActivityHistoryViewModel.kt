package com.siraj.app.features.history.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.history.ActivityHistoryManager
import com.siraj.app.data.repository.FirebaseActivityHistoryRepositoryImpl
import com.siraj.app.domain.models.history.ActivityEntityType
import com.siraj.app.domain.models.history.ActivityHistoryPreferences
import com.siraj.app.domain.models.history.ActivityTab
import com.siraj.app.domain.models.history.RetentionPolicy
import com.siraj.app.domain.models.history.UserActivityItem
import com.siraj.app.domain.repository.history.ActivityHistoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.siraj.app.core.error.GlobalErrorHandler

data class ActivityHistoryUiState(
    val selectedTab: ActivityTab = ActivityTab.ALL,
    val items: List<UserActivityItem> = emptyList(),
    val recentResumeItem: UserActivityItem? = null,
    val preferences: ActivityHistoryPreferences = ActivityHistoryPreferences(),
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val currentOffset: Int = 0,
    val pageSize: Int = 30,
    val hasMore: Boolean = true,
    val searchQuery: String = "",
    val message: String? = null,
    val error: String? = null
)

class ActivityHistoryViewModel(
    application: Application,
    private val repository: ActivityHistoryRepository = FirebaseActivityHistoryRepositoryImpl(application),
    private val userId: String = "user_default"
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ActivityHistoryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        ActivityHistoryManager.initialize(application)
        ActivityHistoryManager.setCurrentUser(userId)
        loadPreferences()
        loadRecentResumeItem()
        loadHistory(reset = true)
        applyRetentionPolicy()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            repository.observePreferences(userId).collect { prefs ->
                _uiState.update { it.copy(preferences = prefs) }
            }
        }
    }

    private fun loadRecentResumeItem() {
        viewModelScope.launch {
            repository.getRecentResumeItem(userId).collect { resumeItem ->
                _uiState.update { it.copy(recentResumeItem = resumeItem) }
            }
        }
    }

    fun selectTab(tab: ActivityTab) {
        if (_uiState.value.selectedTab == tab) return
        _uiState.update { it.copy(selectedTab = tab) }
        loadHistory(reset = true)
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun loadHistory(reset: Boolean = false) {
        val offset = if (reset) 0 else _uiState.value.currentOffset
        val limit = _uiState.value.pageSize
        val tab = _uiState.value.selectedTab

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.observeHistory(userId, tab, limit = limit, offset = offset)
                .catch { err ->
                    _uiState.update { it.copy(isLoading = false, error = "حدث خطأ في تحميل السجل: ${err.message}") }
                }
                .collect { list ->
                    _uiState.update { state ->
                        val combined = if (reset) list else (state.items + list).distinctBy { it.id }
                        state.copy(
                            items = combined,
                            isLoading = false,
                            currentOffset = if (reset) list.size else state.currentOffset + list.size,
                            hasMore = list.size >= limit
                        )
                    }
                }
        }
    }

    fun loadMore() {
        if (_uiState.value.isLoading || !_uiState.value.hasMore) return
        loadHistory(reset = false)
    }

    fun toggleHistoryRecording(enabled: Boolean) {
        viewModelScope.launch {
            val updated = _uiState.value.preferences.copy(isHistoryEnabled = enabled)
            repository.updatePreferences(userId, updated)
            _uiState.update {
                it.copy(
                    preferences = updated,
                    message = if (enabled) "تم تفعيل حفظ سجل المشاهدة والاستماع" else "تم إيقاف حفظ السجل مؤقتاً"
                )
            }
        }
    }

    fun toggleSync(enabled: Boolean) {
        viewModelScope.launch {
            val updated = _uiState.value.preferences.copy(isSyncEnabled = enabled)
            repository.updatePreferences(userId, updated)
            _uiState.update {
                it.copy(
                    preferences = updated,
                    message = if (enabled) "تم تفعيل المزامنة السحابية للسجل" else "تم إيقاف المزامنة السحابية"
                )
            }
        }
    }

    fun updateRetentionPolicy(policy: RetentionPolicy) {
        viewModelScope.launch {
            val updated = _uiState.value.preferences.copy(retentionPolicy = policy)
            repository.updatePreferences(userId, updated)
            _uiState.update {
                it.copy(
                    preferences = updated,
                    message = "تم تحديث سياسة الاحتفاظ بالسجل: ${policy.titleArabic}"
                )
            }
        }
    }

    fun deleteItem(item: UserActivityItem) {
        viewModelScope.launch {
            repository.deleteItem(userId, item.id)
            _uiState.update { state ->
                state.copy(
                    items = state.items.filterNot { it.id == item.id },
                    recentResumeItem = if (state.recentResumeItem?.id == item.id) null else state.recentResumeItem,
                    message = "تم حذف العنصر من السجل"
                )
            }
        }
    }

    fun toggleWatchLater(item: UserActivityItem) {
        viewModelScope.launch {
            repository.toggleWatchLater(userId, item)
            _uiState.update { state ->
                val newWatchLaterState = !item.isWatchLater
                state.copy(
                    items = state.items.map {
                        if (it.id == item.id) it.copy(isWatchLater = newWatchLaterState) else it
                    },
                    message = if (newWatchLaterState) "تمت الإضافة إلى المتابعة لاحقاً" else "تمت الإزالة من المتابعة لاحقاً"
                )
            }
        }
    }

    fun toggleDownloaded(item: UserActivityItem) {
        viewModelScope.launch {
            repository.toggleDownloaded(userId, item)
            _uiState.update { state ->
                val newDownloadState = !item.isDownloaded
                state.copy(
                    items = state.items.map {
                        if (it.id == item.id) it.copy(isDownloaded = newDownloadState) else it
                    },
                    message = if (newDownloadState) "تمت الإضافة إلى سجل التنزيلات" else "تمت الإزالة من سجل التنزيلات"
                )
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory(userId)
            _uiState.update {
                it.copy(
                    items = emptyList(),
                    recentResumeItem = null,
                    message = "تم مسح سجل النشاط بالكامل بنجاح"
                )
            }
        }
    }

    fun clearCompleted() {
        viewModelScope.launch {
            repository.clearCompleted(userId)
            loadHistory(reset = true)
            _uiState.update { it.copy(message = "تم مسح المقاطع المكتملة من السجل") }
        }
    }

    fun clearDownloads() {
        viewModelScope.launch {
            repository.clearDownloads(userId)
            loadHistory(reset = true)
            _uiState.update { it.copy(message = "تم مسح سجل التنزيلات") }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            try {
                repository.syncPending(userId)
                loadHistory(reset = true)
                _uiState.update { it.copy(isSyncing = false, message = "تمت المزامنة بنجاح") }
            } catch (e: Exception) { GlobalErrorHandler.handle(e); _uiState.update { it.copy(isSyncing = false, error = "تعذرت المزامنة: ${e.message} }") }
            }
        }
    }

    private fun applyRetentionPolicy() {
        viewModelScope.launch {
            try {
                repository.applyRetentionPolicy(userId)
            } catch (e: Exception) {
            GlobalErrorHandler.handle(e)}
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
    }
}

class ActivityHistoryViewModelFactory(
    private val application: Application,
    private val userId: String = "user_default"
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActivityHistoryViewModel::class.java)) {
            return ActivityHistoryViewModel(application, userId = userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
