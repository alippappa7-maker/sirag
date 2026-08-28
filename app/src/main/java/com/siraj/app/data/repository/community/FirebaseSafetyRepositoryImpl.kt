package com.siraj.app.data.repository.community

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.community.*
import com.siraj.app.domain.repository.community.SafetyRepository
import kotlinx.coroutines.delay

class FirebaseSafetyRepositoryImpl : SafetyRepository {

    private val reports = mutableListOf<Report>()
    private val moderationLogs = mutableListOf<ModerationDecisionLog>()
    private val reportTimestamps = mutableMapOf<String, MutableList<Long>>() // userId -> list of report timestamps

    override suspend fun submitReport(
        reporterId: String,
        targetType: ReportTargetType,
        targetId: String,
        targetOwnerId: String,
        reportType: ReportType,
        description: String
    ): Resource<Unit> {
        delay(600)
        
        // Rate Limiting (Mock: Max 5 reports per minute)
        val now = System.currentTimeMillis()
        val userTimestamps = reportTimestamps.getOrPut(reporterId) { mutableListOf() }
        userTimestamps.removeAll { now - it > 60000 }
        if (userTimestamps.size >= 5) {
            return Resource.Error("تجاوزت الحد الأقصى للإبلاغات. يرجى المحاولة لاحقاً.")
        }
        
        // Prevent Duplicate Reports for the same target by the same user
        val duplicate = reports.find { it.reporterId == reporterId && it.targetId == targetId && it.status != ReportStatus.RESOLVED && it.status != ReportStatus.DISMISSED }
        if (duplicate != null) {
            return Resource.Error("لقد قمت بالإبلاغ عن هذا المحتوى مسبقاً وجاري مراجعته.")
        }

        userTimestamps.add(now)

        val report = Report(
            reporterId = reporterId,
            targetType = targetType,
            targetId = targetId,
            targetOwnerId = targetOwnerId,
            reportType = reportType,
            description = description
        )
        reports.add(report)

        return Resource.Success(Unit)
    }

    override suspend fun getPendingReports(reviewerRole: String): Resource<List<Report>> {
        delay(400)
        // Routing Rules:
        // REVIEWER gets Religious Errors
        // ADMIN gets Copyright, Spam, Harassment, etc.
        val pending = reports.filter { it.status == ReportStatus.PENDING || it.status == ReportStatus.IN_REVIEW }
        
        val filtered = if (reviewerRole == "REVIEWER") {
            pending.filter { it.reportType == ReportType.RELIGIOUS_ERROR }
        } else if (reviewerRole == "ADMIN" || reviewerRole == "OWNER") {
            pending.filter { it.reportType != ReportType.RELIGIOUS_ERROR }
        } else {
            emptyList()
        }
        
        return Resource.Success(filtered)
    }

    override suspend fun resolveReport(
        reportId: String,
        resolverId: String,
        resolution: String,
        notes: String
    ): Resource<Unit> {
        delay(500)
        val index = reports.indexOfFirst { it.id == reportId }
        if (index == -1) return Resource.Error("البلاغ غير موجود")

        val report = reports[index]
        val newStatus = if (resolution == "DISMISS") ReportStatus.DISMISSED else ReportStatus.RESOLVED

        reports[index] = report.copy(
            status = newStatus,
            resolvedAt = System.currentTimeMillis(),
            resolverId = resolverId,
            resolutionNotes = notes
        )

        moderationLogs.add(
            ModerationDecisionLog(
                reportId = reportId,
                moderatorId = resolverId,
                decision = resolution,
                notes = notes
            )
        )

        // Note: In a real system, resolving a report as "TAKE_DOWN" would trigger an event to update the Flash/Project state.
        return Resource.Success(Unit)
    }

    override suspend fun getModerationLogs(reportId: String): Resource<List<ModerationDecisionLog>> {
        delay(200)
        return Resource.Success(moderationLogs.filter { it.reportId == reportId })
    }
}
