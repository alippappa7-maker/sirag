package com.siraj.app.domain.models.analytics

enum class AnalyticsEvent(val eventName: String) {
    APP_OPENED("app_opened"),
    ONBOARDING_COMPLETED("onboarding_completed"),
    PROJECT_CREATED("project_created"),
    PROJECT_EXPORT_STARTED("project_export_started"),
    PROJECT_EXPORT_COMPLETED("project_export_completed"),
    TEMPLATE_USED("template_used"),
    SOURCE_ATTACHED("source_attached"),
    REVIEW_SUBMITTED("review_submitted"),
    FLASH_PUBLISHED("flash_published"),
    AUDIO_STARTED("audio_started"),
    VIDEO_STARTED("video_started"),
    SUBSCRIPTION_VIEWED("subscription_viewed"),
    PURCHASE_COMPLETED("purchase_completed"),
    ERROR_OCCURRED("error_occurred")
}

data class AnalyticsLog(
    val id: String = "",
    val event: String = "",
    val hashedUserId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val properties: Map<String, String> = emptyMap()
)
