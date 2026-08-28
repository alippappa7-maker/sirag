package com.siraj.app.domain.models.community

import java.util.UUID

enum class ReportType(val titleArabic: String) {
    MISINFORMATION("معلومات مضللة"),
    RELIGIOUS_ERROR("خطأ شرعي"),
    COPYRIGHT("حقوق النشر"),
    HARASSMENT("مضايقة أو إساءة"),
    SPAM("محتوى مزعج"),
    INAPPROPRIATE("محتوى غير لائق"),
    PRIVACY("انتهاك الخصوصية"),
    OTHER("أخرى")
}

enum class ReportStatus {
    PENDING,
    IN_REVIEW,
    RESOLVED,
    DISMISSED
}

enum class ReportTargetType {
    FLASH, PROJECT, AUDIO, SOURCE, USER
}

data class Report(
    val id: String = UUID.randomUUID().toString(),
    val reporterId: String, // Cannot be seen by the reported user
    val targetType: ReportTargetType,
    val targetId: String,
    val targetOwnerId: String, // The user being reported
    val reportType: ReportType,
    val description: String,
    val status: ReportStatus = ReportStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null,
    val resolverId: String? = null,
    val resolutionNotes: String? = null
)

data class ModerationDecisionLog(
    val id: String = UUID.randomUUID().toString(),
    val reportId: String,
    val moderatorId: String,
    val decision: String,
    val notes: String,
    val timestamp: Long = System.currentTimeMillis()
)
