package com.siraj.app.data.repository.admin

import com.siraj.app.domain.models.admin.AdminContentItem
import com.siraj.app.domain.models.admin.AdminContentStatus
import com.siraj.app.domain.models.admin.AuditLogEntry
import com.siraj.app.domain.models.admin.ContentManagementFilter
import com.siraj.app.domain.repository.admin.ContentManagementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

class FirebaseContentManagementRepositoryImpl : ContentManagementRepository {
    private val mockItems =
        MutableStateFlow(
            listOf(
                AdminContentItem(
                    id = "item-1",
                    title = "نصيحة الصباح",
                    type = "FLASH",
                    status = AdminContentStatus.APPROVED,
                    ownerId = "user123",
                    createdAt = System.currentTimeMillis() - 86400000,
                    isReligiousText = false,
                ),
                AdminContentItem(
                    id = "item-2",
                    title = "تلاوة سورة الفاتحة",
                    type = "AUDIO",
                    status = AdminContentStatus.PENDING_REVIEW,
                    ownerId = "user456",
                    createdAt = System.currentTimeMillis() - 3600000,
                    isReligiousText = true,
                ),
            ),
        )

    private val mockAuditLogs = mutableMapOf<String, MutableList<AuditLogEntry>>()

    override fun getManagedContent(
        filter: ContentManagementFilter,
        page: Int,
        limit: Int,
    ): Flow<List<AdminContentItem>> =
        mockItems.map { items ->
            items
                .filter { item ->
                    (filter.type == null || item.type == filter.type) &&
                        (filter.status == null || item.status == filter.status) &&
                        (filter.query.isEmpty() || item.title.contains(filter.query, ignoreCase = true))
                }.take(limit) // simple mock pagination
        }

    override suspend fun updateContentStatus(
        contentId: String,
        newStatus: AdminContentStatus,
        reason: String?,
    ) {
        val currentItems = mockItems.value.toMutableList()
        val index = currentItems.indexOfFirst { it.id == contentId }
        if (index != -1) {
            val oldItem = currentItems[index]
            val newItem = oldItem.copy(status = newStatus)
            currentItems[index] = newItem
            mockItems.value = currentItems

            addAuditLog(contentId, oldItem.type, "STATUS_CHANGE", oldItem.status.name, newStatus.name)
        }
    }

    override suspend fun archiveContent(contentId: String) {
        updateContentStatus(contentId, AdminContentStatus.ARCHIVED, "Admin archived")
    }

    override suspend fun restoreContent(contentId: String) {
        updateContentStatus(contentId, AdminContentStatus.PENDING_REVIEW, "Admin restored")
    }

    override suspend fun updateContentMetadata(
        contentId: String,
        metadata: Map<String, Any>,
    ) {
        addAuditLog(contentId, "UNKNOWN", "METADATA_UPDATE", "Old Metadata", "New Metadata: $metadata")
    }

    override suspend fun scheduleContent(
        contentId: String,
        publishAt: Long,
    ) {
        addAuditLog(contentId, "UNKNOWN", "SCHEDULE_PUBLISH", "None", "Publish at: $publishAt")
    }

    override suspend fun getAuditLogs(entityId: String): List<AuditLogEntry> = mockAuditLogs[entityId] ?: emptyList()

    override suspend fun exportAdminReport(): String = "https://siraj.app/reports/admin_report_${System.currentTimeMillis()}.pdf"

    private fun addAuditLog(
        entityId: String,
        entityType: String,
        action: String,
        oldState: String?,
        newState: String?,
    ) {
        val list = mockAuditLogs.getOrPut(entityId) { mutableListOf() }
        list.add(
            AuditLogEntry(
                id = UUID.randomUUID().toString(),
                entityId = entityId,
                entityType = entityType,
                action = action,
                performedByUserId = "current_admin_id",
                performedByRole = "ADMIN",
                timestamp = System.currentTimeMillis(),
                previousState = oldState,
                newState = newState,
            ),
        )
    }
}
