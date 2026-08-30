package com.siraj.app.domain.repository.community

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.community.*

interface SafetyRepository {
    // Terms of Service Acceptance
    suspend fun acceptTermsOfService(userId: String, version: String = "1.2.0"): Resource<TermsOfServiceConsent>
    suspend fun hasAcceptedTerms(userId: String, version: String = "1.2.0"): Resource<Boolean>

    // Automated Scanning & Pre-filtering
    suspend fun scanUgcContent(
        title: String,
        description: String,
        mediaType: String,
        tags: List<String> = emptyList()
    ): Resource<PreUploadScanResult>

    // UGC Lifecycle Queue
    suspend fun submitUgcItem(item: UgcItem): Resource<UgcItem>
    suspend fun getUgcQueue(role: String, filterState: UgcState? = null): Resource<List<UgcItem>>
    suspend fun takeModeratorActionOnUgc(
        ugcId: String,
        moderatorId: String,
        action: ModeratorAction,
        notes: String
    ): Resource<Unit>

    // Reporting
    suspend fun submitReport(
        reporterId: String,
        targetType: ReportTargetType,
        targetId: String,
        targetOwnerId: String,
        reportType: ReportType,
        description: String
    ): Resource<Unit>

    suspend fun getPendingReports(reviewerRole: String): Resource<List<Report>>
    
    suspend fun resolveReport(
        reportId: String,
        resolverId: String,
        resolution: String, // "DISMISS", "TAKE_DOWN", "WARN_USER", "SUSPEND_USER"
        notes: String
    ): Resource<Unit>

    // Appeals
    suspend fun submitAppeal(
        ugcId: String,
        ugcTitle: String,
        userId: String,
        originalReason: String,
        appealJustification: String
    ): Resource<UgcAppeal>

    suspend fun getAppeals(): Resource<List<UgcAppeal>>

    suspend fun resolveAppeal(
        appealId: String,
        moderatorId: String,
        isApproved: Boolean,
        notes: String
    ): Resource<Unit>

    // User Blocking & Account Moderation
    suspend fun blockUser(userId: String, blockedUserId: String): Resource<Unit>
    suspend fun unblockUser(userId: String, blockedUserId: String): Resource<Unit>
    suspend fun getBlockedUsers(userId: String): Resource<List<String>>
    suspend fun suspendUserAccount(userId: String, moderatorId: String, reason: String, durationDays: Int): Resource<Unit>

    // Audit Logging
    suspend fun getModerationLogs(targetId: String): Resource<List<ModerationDecisionLog>>
    suspend fun getAllModerationLogs(): Resource<List<ModerationDecisionLog>>
}

