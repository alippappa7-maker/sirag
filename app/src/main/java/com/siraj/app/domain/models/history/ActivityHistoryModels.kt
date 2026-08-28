package com.siraj.app.domain.models.history

enum class ActivityEntityType {
    VIDEO,
    AUDIO,
    FLASH,
    QURAN_RECITATION
}

enum class SyncStatus {
    SYNCED,
    PENDING_SYNC,
    PENDING_DELETE
}

enum class RetentionPolicy(val days: Int, val titleArabic: String) {
    DAYS_30(30, "30 يوماً"),
    DAYS_90(90, "90 يوماً (مستحسن)"),
    YEAR_1(365, "سنة واحدة"),
    FOREVER(-1, "احتفاظ دائم (بلا حذف تلقائي)")
}

enum class ActivityTab(val title: String) {
    ALL("الكل"),
    VIDEO("الفيديو والومضات"),
    AUDIO("الصوت والتلاوات"),
    WATCH_LATER("المتابعة لاحقاً"),
    DOWNLOADED("التنزيلات"),
    COMPLETED("المكتملة")
}

data class UserActivityItem(
    val id: String,
    val userId: String,
    val entityType: ActivityEntityType,
    val entityId: String,
    val title: String,
    val subtitle: String? = null,
    val mediaUrl: String? = null,
    val thumbnailUrl: String? = null,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val progressPercent: Float = 0f,
    val completed: Boolean = false,
    val lastPlayedAt: Long = System.currentTimeMillis(),
    val deviceId: String = "",
    val isWatchLater: Boolean = false,
    val isDownloaded: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
) {
    fun getFormattedPosition(): String {
        return formatTime(positionMs)
    }

    fun getFormattedDuration(): String {
        return formatTime(durationMs)
    }

    fun getRemainingTimeText(): String {
        if (durationMs <= 0) return ""
        val remaining = (durationMs - positionMs).coerceAtLeast(0)
        return "تبقى ${formatTime(remaining)}"
    }

    private fun formatTime(millis: Long): String {
        val totalSecs = (millis / 1000).coerceAtLeast(0)
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        val hours = mins / 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, mins % 60, secs)
        } else {
            String.format("%02d:%02d", mins, secs)
        }
    }
}

data class ActivityHistoryPreferences(
    val isHistoryEnabled: Boolean = true,
    val isSyncEnabled: Boolean = true,
    val retentionPolicy: RetentionPolicy = RetentionPolicy.DAYS_90,
    val saveWatchHistory: Boolean = true,
    val saveListenHistory: Boolean = true,
    val saveDownloadsHistory: Boolean = true
)
