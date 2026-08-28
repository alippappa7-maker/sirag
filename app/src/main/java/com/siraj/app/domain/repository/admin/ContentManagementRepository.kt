package com.siraj.app.domain.repository.admin

import com.siraj.app.domain.models.admin.AdminContentItem
import com.siraj.app.domain.models.admin.AdminContentStatus
import com.siraj.app.domain.models.admin.AuditLogEntry
import com.siraj.app.domain.models.admin.ContentManagementFilter
import kotlinx.coroutines.flow.Flow

interface ContentManagementRepository {
    fun getManagedContent(filter: ContentManagementFilter, page: Int, limit: Int): Flow<List<AdminContentItem>>
    suspend fun updateContentStatus(contentId: String, newStatus: AdminContentStatus, reason: String?)
    suspend fun archiveContent(contentId: String)
    suspend fun restoreContent(contentId: String)
    suspend fun updateContentMetadata(contentId: String, metadata: Map<String, Any>)
    suspend fun scheduleContent(contentId: String, publishAt: Long)
    suspend fun getAuditLogs(entityId: String): List<AuditLogEntry>
    suspend fun exportAdminReport(): String // URL to report
}
