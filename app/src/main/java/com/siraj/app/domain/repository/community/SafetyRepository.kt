package com.siraj.app.domain.repository.community

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.community.Report
import com.siraj.app.domain.models.community.ReportType
import com.siraj.app.domain.models.community.ReportTargetType
import com.siraj.app.domain.models.community.ModerationDecisionLog

interface SafetyRepository {
    // Reporting
    suspend fun submitReport(
        reporterId: String,
        targetType: ReportTargetType,
        targetId: String,
        targetOwnerId: String,
        reportType: ReportType,
        description: String
    ): Resource<Unit>

    // Moderation
    suspend fun getPendingReports(reviewerRole: String): Resource<List<Report>>
    
    suspend fun resolveReport(
        reportId: String,
        resolverId: String,
        resolution: String, // "DISMISS", "TAKE_DOWN", "WARN_USER", "SUSPEND_USER"
        notes: String
    ): Resource<Unit>

    suspend fun getModerationLogs(reportId: String): Resource<List<ModerationDecisionLog>>
}
