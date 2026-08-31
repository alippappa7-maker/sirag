package com.siraj.app.features.notification.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.siraj.app.core.notification.NotificationHelper
import com.siraj.app.data.repository.FirebaseNotificationRepositoryImpl
import com.siraj.app.domain.models.notification.*
import com.siraj.app.domain.repository.notification.NotificationRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class NotificationCenterUiState(
    val notifications: List<SirajNotification> = emptyList(),
    val filteredNotifications: List<SirajNotification> = emptyList(),
    val unreadCount: Int = 0,
    val selectedFilter: NotificationFilter = NotificationFilter.ALL,
    val preferences: NotificationPreferences = NotificationPreferences(),
    val isLoading: Boolean = false,
    val userMessage: String? = null,
)

class NotificationViewModel(
    application: Application,
    private val repository: NotificationRepository = FirebaseNotificationRepositoryImpl(application),
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(NotificationCenterUiState(isLoading = true))
    val uiState: StateFlow<NotificationCenterUiState> = _uiState.asStateFlow()

    private val currentUserId: String
        get() =
            try {
                FirebaseAuth.getInstance().currentUser?.uid ?: "user_default_id"
            } catch (e: Exception) {
                "user_default_id"
            }

    init {
        try {
            NotificationHelper.createNotificationChannels(application)
        } catch (e: Exception) {
            // Ignore channel creation in unit tests
        }
        observeNotifications()
        observePreferences()
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            repository
                .getNotificationsFlow(currentUserId)
                .combine(repository.getUnreadCountFlow(currentUserId)) { notifs, unread ->
                    Pair(notifs, unread)
                }.catch { e ->
                    _uiState.update { it.copy(isLoading = false, userMessage = "خطأ في تحميل الإشعارات: ${e.message}") }
                }.collect { (notifs, unread) ->
                    val filtered = applyFilter(notifs, _uiState.value.selectedFilter)
                    _uiState.update {
                        it.copy(
                            notifications = notifs,
                            filteredNotifications = filtered,
                            unreadCount = unread,
                            isLoading = false,
                        )
                    }
                }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            repository
                .getPreferencesFlow(currentUserId)
                .collect { prefs ->
                    _uiState.update { it.copy(preferences = prefs) }
                }
        }
    }

    fun setFilter(filter: NotificationFilter) {
        _uiState.update {
            val filtered = applyFilter(it.notifications, filter)
            it.copy(selectedFilter = filter, filteredNotifications = filtered)
        }
    }

    private fun applyFilter(
        list: List<SirajNotification>,
        filter: NotificationFilter,
    ): List<SirajNotification> =
        when (filter) {
            NotificationFilter.ALL -> list
            NotificationFilter.UNREAD -> list.filter { !it.isRead }
            NotificationFilter.PROJECTS ->
                list.filter {
                    it.type == NotificationType.VIDEO_GENERATION_COMPLETED ||
                        it.type == NotificationType.EXPORT_FAILED ||
                        it.type == NotificationType.PROJECT_COMMENT_UPDATE
                }
            NotificationFilter.REVIEW ->
                list.filter {
                    it.type == NotificationType.REVIEW_REQUESTED ||
                        it.type == NotificationType.REVIEW_RESULT
                }
            NotificationFilter.MIHRAB ->
                list.filter {
                    it.type == NotificationType.PRAYER_REMINDER ||
                        it.type == NotificationType.MORNING_EVENING_ADHKAR
                }
            NotificationFilter.CONTENT ->
                list.filter {
                    it.type == NotificationType.NEW_AUDIO_CONTENT ||
                        it.type == NotificationType.NEW_FLASH
                }
            NotificationFilter.SYSTEM ->
                list.filter {
                    it.type == NotificationType.SYSTEM_MESSAGE ||
                        it.type == NotificationType.SUBSCRIPTION_BILLING
                }
        }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            repository.markAsRead(currentUserId, notificationId)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            val result = repository.markAllAsRead(currentUserId)
            if (result.isSuccess) {
                _uiState.update { it.copy(userMessage = "تم تحديد جميع الإشعارات كمقروءة") }
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            val result = repository.deleteNotification(currentUserId, notificationId)
            if (result.isSuccess) {
                _uiState.update { it.copy(userMessage = "تم حذف الإشعار") }
            }
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            val result = repository.clearAllNotifications(currentUserId)
            if (result.isSuccess) {
                _uiState.update { it.copy(userMessage = "تم مسح جميع الإشعارات") }
            }
        }
    }

    fun updatePreferences(newPreferences: NotificationPreferences) {
        viewModelScope.launch {
            val result = repository.updatePreferences(currentUserId, newPreferences)
            if (result.isSuccess) {
                _uiState.update { it.copy(preferences = newPreferences, userMessage = "تم حفظ إعدادات الإشعارات بنجاح") }
            }
        }
    }

    fun sendTestNotification(
        type: NotificationType,
        customTitle: String? = null,
        customBody: String? = null,
        isSensitive: Boolean = false,
        entityType: String? = null,
        entityId: String? = null,
    ) {
        viewModelScope.launch {
            val title =
                customTitle?.ifBlank { null } ?: when (type) {
                    NotificationType.VIDEO_GENERATION_COMPLETED -> "اكتمل تصيير الفيديو بنجاح"
                    NotificationType.EXPORT_FAILED -> "تعذر تصدير الفيديو: يرجى مراجعة المدة"
                    NotificationType.REVIEW_REQUESTED -> "طلب تدقيق شرعي جديد لمشروعك"
                    NotificationType.REVIEW_RESULT -> "تم اعتماد مشروعك ونشره بنجاح"
                    NotificationType.PROJECT_COMMENT_UPDATE -> "تحديث جديد في مشهد سورة الكهف"
                    NotificationType.NEW_AUDIO_CONTENT -> "تلاوة خاشعة جديدة: سورة الرحمن"
                    NotificationType.NEW_FLASH -> "ومضة دعوية جديدة: فضل صلاة الفجر"
                    NotificationType.PRAYER_REMINDER -> "حان الآن موعد أذان العصر"
                    NotificationType.MORNING_EVENING_ADHKAR -> "أذكار الصباح: حصن المسلم"
                    NotificationType.SUBSCRIPTION_BILLING -> "تم تجديد اشتراك باقة صناع المحتوى"
                    NotificationType.SYSTEM_MESSAGE -> "تحديث جديد في منصة سراج v1.1"
                }

            val body =
                customBody?.ifBlank { null } ?: when (type) {
                    NotificationType.VIDEO_GENERATION_COMPLETED -> "أصبح الفيديو جاهزاً للتنزيل والمشاركة بدقة 1080p."
                    NotificationType.EXPORT_FAILED -> "تحقق من اتصال الإنترنت وأعد المحاولة مرة أخرى."
                    NotificationType.REVIEW_REQUESTED -> "تم إرسال السيناريو إلى لجنة المراجعة المختصة."
                    NotificationType.REVIEW_RESULT -> "حصل المشروع على وسم 'موثق ومعتمد'."
                    NotificationType.PROJECT_COMMENT_UPDATE -> "أضاف المراجع تعليقاً على المشهد الثاني."
                    NotificationType.NEW_AUDIO_CONTENT -> "بصوت القارئ عبد الباسط عبد الصمد بجودة عالية."
                    NotificationType.NEW_FLASH -> "شاهد مقطع ومضة مدته 45 ثانية بجودة فائقة."
                    NotificationType.PRAYER_REMINDER -> "حسب التوقيت المحلي لمدينتك، تقبل الله منا ومنكم."
                    NotificationType.MORNING_EVENING_ADHKAR -> "ابدأ يومك بذكر الله والأدعية المأثورة."
                    NotificationType.SUBSCRIPTION_BILLING -> "تمت ترقية رصيد الإنتاج السحابي لهذا الشهر."
                    NotificationType.SYSTEM_MESSAGE -> "تم تحسين أداء محرك تركيب الفيديو وإضافة خطوط جديدة."
                }

            val testNotif =
                SirajNotification(
                    id = "test_${System.currentTimeMillis()}",
                    userId = currentUserId,
                    type = type,
                    title = title,
                    body = body,
                    entityType =
                        entityType ?: when (type) {
                            NotificationType.VIDEO_GENERATION_COMPLETED, NotificationType.EXPORT_FAILED, NotificationType.PROJECT_COMMENT_UPDATE -> "PROJECT"
                            NotificationType.REVIEW_REQUESTED, NotificationType.REVIEW_RESULT -> "REVIEW"
                            NotificationType.NEW_AUDIO_CONTENT -> "AUDIO"
                            NotificationType.NEW_FLASH -> "FLASH"
                            NotificationType.PRAYER_REMINDER, NotificationType.MORNING_EVENING_ADHKAR -> "MIHRAB"
                            NotificationType.SUBSCRIPTION_BILLING -> "BILLING"
                            NotificationType.SYSTEM_MESSAGE -> "SYSTEM"
                        },
                    entityId = entityId ?: "test_entity_1",
                    readAt = null,
                    createdAt = System.currentTimeMillis(),
                    deliveryStatus = DeliveryStatus.DELIVERED,
                    isSensitive = isSensitive,
                )

            // Save to repository
            repository.saveNotification(testNotif)

            // Trigger system push notification
            NotificationHelper.showSystemNotification(
                context = getApplication(),
                notification = testNotif,
                preferences = _uiState.value.preferences,
            )

            _uiState.update { it.copy(userMessage = "تم إرسال إشعار تجريبي بنجاح") }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}

class NotificationViewModelFactory(
    private val application: Application,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
