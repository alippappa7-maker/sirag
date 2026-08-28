package com.siraj.app.domain.models.notification

/**
 * Supported notification types in Siraj Platform.
 */
enum class NotificationType(
    val titleAr: String,
    val categoryAr: String,
    val iconName: String,
    val channelId: String
) {
    VIDEO_GENERATION_COMPLETED(
        titleAr = "اكتمال إنشاء الفيديو",
        categoryAr = "المشاريع والإنتاج",
        iconName = "video_call",
        channelId = "siraj_projects_channel"
    ),
    EXPORT_FAILED(
        titleAr = "فشل تصدير الفيديو",
        categoryAr = "المشاريع والإنتاج",
        iconName = "error_outline",
        channelId = "siraj_projects_channel"
    ),
    REVIEW_REQUESTED(
        titleAr = "طلب مراجعة جديد",
        categoryAr = "المراجعة والتدقيق",
        iconName = "rate_review",
        channelId = "siraj_review_channel"
    ),
    REVIEW_RESULT(
        titleAr = "نتيجة المراجعة والاعتماد",
        categoryAr = "المراجعة والتدقيق",
        iconName = "verified",
        channelId = "siraj_review_channel"
    ),
    PROJECT_COMMENT_UPDATE(
        titleAr = "تعليق أو تحديث في المشروع",
        categoryAr = "المشاريع والإنتاج",
        iconName = "comment",
        channelId = "siraj_projects_channel"
    ),
    NEW_AUDIO_CONTENT(
        titleAr = "محتوى صوتي وتلاوة جديدة",
        categoryAr = "المحتوى الصوتي",
        iconName = "headphones",
        channelId = "siraj_content_channel"
    ),
    NEW_FLASH(
        titleAr = "ومضة دعوية جديدة",
        categoryAr = "الومضات",
        iconName = "bolt",
        channelId = "siraj_content_channel"
    ),
    PRAYER_REMINDER(
        titleAr = "تذكير بموعد الصلاة",
        categoryAr = "المحراب",
        iconName = "access_time",
        channelId = "siraj_prayers_channel"
    ),
    MORNING_EVENING_ADHKAR(
        titleAr = "أذكار الصباح والمساء",
        categoryAr = "المحراب",
        iconName = "menu_book",
        channelId = "siraj_prayers_channel"
    ),
    SUBSCRIPTION_BILLING(
        titleAr = "الاشتراك والفوترة",
        categoryAr = "الحساب والفوترة",
        iconName = "credit_card",
        channelId = "siraj_billing_channel"
    ),
    SYSTEM_MESSAGE(
        titleAr = "رسائل وتنبيهات النظام",
        categoryAr = "النظام",
        iconName = "notifications",
        channelId = "siraj_system_channel"
    )
}

/**
 * Delivery status of the notification.
 */
enum class DeliveryStatus {
    PENDING,
    DELIVERED,
    READ,
    FAILED
}

/**
 * Notification model representing an in-app and push notification in Siraj.
 */
data class SirajNotification(
    val id: String,
    val userId: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val entityType: String? = null, // "PROJECT", "REVIEW", "AUDIO", "FLASH", "PRAYER", "BILLING", "SYSTEM"
    val entityId: String? = null,
    val readAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null,
    val deliveryStatus: DeliveryStatus = DeliveryStatus.DELIVERED,
    val isSensitive: Boolean = false, // If true, hides preview on lock screen if unapproved
    val actionUrl: String? = null,
    val metadata: Map<String, String> = emptyMap()
) {
    val isRead: Boolean
        get() = readAt != null

    val isExpired: Boolean
        get() = expiresAt != null && System.currentTimeMillis() > expiresAt
}

/**
 * Registered device token info for Firebase Cloud Messaging (FCM).
 */
data class DeviceTokenInfo(
    val token: String,
    val deviceModel: String,
    val platform: String = "ANDROID",
    val lastUpdated: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val appVersion: String = "1.0"
)

/**
 * User notification preferences and quiet hours configuration.
 */
data class NotificationPreferences(
    val videoGeneration: Boolean = true,
    val exportStatus: Boolean = true,
    val reviewRequests: Boolean = true,
    val reviewResults: Boolean = true,
    val projectComments: Boolean = true,
    val newAudio: Boolean = true,
    val newFlashes: Boolean = true,
    val prayerReminders: Boolean = true,
    val adhkarReminders: Boolean = true,
    val subscriptionBilling: Boolean = true,
    val systemMessages: Boolean = true,
    val marketingAllowed: Boolean = false, // Strict default: false
    val quietHoursEnabled: Boolean = false,
    val quietHoursStartHour: Int = 22, // 10:00 PM
    val quietHoursStartMinute: Int = 0,
    val quietHoursEndHour: Int = 6, // 6:00 AM
    val quietHoursEndMinute: Int = 0,
    val hideSensitiveOnLockScreen: Boolean = true
) {
    /**
     * Checks if current local time falls into quiet hours.
     */
    fun isQuietHourNow(): Boolean {
        if (!quietHoursEnabled) return false
        val calendar = java.util.Calendar.getInstance()
        val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(java.util.Calendar.MINUTE)
        val currentTotalMinutes = currentHour * 60 + currentMinute

        val startTotalMinutes = quietHoursStartHour * 60 + quietHoursStartMinute
        val endTotalMinutes = quietHoursEndHour * 60 + quietHoursEndMinute

        return if (startTotalMinutes <= endTotalMinutes) {
            currentTotalMinutes in startTotalMinutes until endTotalMinutes
        } else {
            // Over midnight (e.g. 22:00 to 06:00)
            currentTotalMinutes >= startTotalMinutes || currentTotalMinutes < endTotalMinutes
        }
    }

    /**
     * Checks if a notification type is allowed by user settings.
     */
    fun isTypeEnabled(type: NotificationType): Boolean {
        return when (type) {
            NotificationType.VIDEO_GENERATION_COMPLETED -> videoGeneration
            NotificationType.EXPORT_FAILED -> exportStatus
            NotificationType.REVIEW_REQUESTED -> reviewRequests
            NotificationType.REVIEW_RESULT -> reviewResults
            NotificationType.PROJECT_COMMENT_UPDATE -> projectComments
            NotificationType.NEW_AUDIO_CONTENT -> newAudio
            NotificationType.NEW_FLASH -> newFlashes
            NotificationType.PRAYER_REMINDER -> prayerReminders
            NotificationType.MORNING_EVENING_ADHKAR -> adhkarReminders
            NotificationType.SUBSCRIPTION_BILLING -> subscriptionBilling
            NotificationType.SYSTEM_MESSAGE -> systemMessages
        }
    }
}

/**
 * Filter categories for Notification Center tabs.
 */
enum class NotificationFilter(val titleAr: String) {
    ALL("الكل"),
    UNREAD("غير مقروء"),
    PROJECTS("المشاريع والإنتاج"),
    REVIEW("المراجعة والاعتماد"),
    MIHRAB("المحراب والصلاة"),
    CONTENT("المحتوى الصوتي والومضات"),
    SYSTEM("النظام والفوترة")
}
