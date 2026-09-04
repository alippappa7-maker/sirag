package com.siraj.app.data.repository.review

import com.siraj.app.core.utils.Resource
import com.siraj.app.core.error.ErrorHandler
import com.siraj.app.domain.models.review.*
import com.siraj.app.domain.repository.review.ShariaReviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FirebaseShariaReviewRepositoryImpl : ShariaReviewRepository {
    private val _itemsFlow = MutableStateFlow<List<ShariaReviewItem>>(emptyList())
    val itemsFlow = _itemsFlow.asStateFlow()

    override fun getReviewQueue(filter: ShariaReviewFilter): Flow<Resource<List<ShariaReviewItem>>> =
        _itemsFlow.map { list ->
            try {
                var filtered = list

                filter.riskLevel?.let { risk ->
                    filtered = filtered.filter { it.riskLevel == risk }
                }

                filter.category?.let { cat ->
                    if (cat.isNotBlank()) {
                        filtered = filtered.filter { it.category == cat }
                    }
                }

                filter.status?.let { st ->
                    filtered = filtered.filter { it.status == st }
                }

                filter.criticalTopic?.let { topic ->
                    if (topic != CriticalTopic.NONE) {
                        filtered = filtered.filter { it.criticalTopics.contains(topic) }
                    }
                }

                if (filter.searchQuery.isNotBlank()) {
                    val q = filter.searchQuery.trim().lowercase()
                    filtered =
                        filtered.filter {
                            it.contentTitle.lowercase().contains(q) ||
                                it.fullContentText.lowercase().contains(q) ||
                                it.creatorName.lowercase().contains(q) ||
                                it.claims.any { c -> c.claimText.lowercase().contains(q) || c.sourceTitle.lowercase().contains(q) }
                        }
                }

                filtered =
                    if (filter.sortByDateAscending) {
                        filtered.sortedBy { it.submittedAt }
                    } else {
                        filtered.sortedByDescending { it.submittedAt }
                    }

                Resource.Success(filtered)
            } catch (e: Exception) {
                val error = ErrorHandler.handle(e)
                Resource.Error(error.userMessage, error)
            }
        }

    override fun getReviewItemById(itemId: String): Flow<Resource<ShariaReviewItem>> =
        _itemsFlow.map { list ->
            val found = list.find { it.id == itemId }
            if (found != null) {
                Resource.Success(found)
            } else {
                Resource.Error("عنصر المراجعة غير موجود")
            }
        }

    override suspend fun claimReview(
        itemId: String,
        reviewerId: String,
        reviewerName: String,
    ): Resource<Unit> {
        val currentList = _itemsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == itemId }
        if (index == -1) return Resource.Error("عنصر المراجعة غير موجود")

        val item = currentList[index]
        if (item.creatorId == reviewerId) {
            return Resource.Error("لا يحق لصانع المحتوى مراجعة أو حجز محتواه الخاص")
        }

        val updatedAuditLogs =
            item.auditLogs +
                ShariaAuditLog(
                    itemId = itemId,
                    reviewerId = reviewerId,
                    reviewerName = reviewerName,
                    action = "CLAIM_REVIEW",
                    details = "قام المراجع بحجز المحتوى وبدء التدقيق الشرعي",
                )

        val updatedItem =
            item.copy(
                status = ShariaReviewStatus.IN_REVIEW,
                currentReviewerId = reviewerId,
                currentReviewerName = reviewerName,
                updatedAt = System.currentTimeMillis(),
                auditLogs = updatedAuditLogs,
            )

        currentList[index] = updatedItem
        _itemsFlow.value = currentList
        return Resource.Success(Unit)
    }

    override suspend fun releaseReview(
        itemId: String,
        reviewerId: String,
        reviewerName: String,
    ): Resource<Unit> {
        val currentList = _itemsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == itemId }
        if (index == -1) return Resource.Error("عنصر المراجعة غير موجود")

        val item = currentList[index]
        val updatedAuditLogs =
            item.auditLogs +
                ShariaAuditLog(
                    itemId = itemId,
                    reviewerId = reviewerId,
                    reviewerName = reviewerName,
                    action = "RELEASE_REVIEW",
                    details = "تم إلغاء حجز المراجعة وإعادة العنصر لقائمة الانتظار",
                )

        val updatedItem =
            item.copy(
                status = ShariaReviewStatus.PENDING,
                currentReviewerId = null,
                currentReviewerName = null,
                updatedAt = System.currentTimeMillis(),
                auditLogs = updatedAuditLogs,
            )

        currentList[index] = updatedItem
        _itemsFlow.value = currentList
        return Resource.Success(Unit)
    }

    override suspend fun approveItem(
        itemId: String,
        reviewerId: String,
        reviewerName: String,
        reason: String,
        scheduledReReviewDate: Long?,
    ): Resource<Unit> {
        val currentList = _itemsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == itemId }
        if (index == -1) return Resource.Error("عنصر المراجعة غير موجود")

        val item = currentList[index]
        if (item.creatorId == reviewerId) {
            return Resource.Error("لا يمكن لصانع المحتوى اعتماد محتواه بنفسه حفاظاً على النزاهة الشرعية")
        }

        val isCritical =
            item.riskLevel == RiskLevel.CRITICAL ||
                item.criticalTopics.any { it != CriticalTopic.NONE }

        if (isCritical) {
            // Needs second reviewer confirmation
            val decision =
                ShariaReviewDecision(
                    primaryReviewerId = reviewerId,
                    primaryReviewerName = reviewerName,
                    primaryStatus = ShariaReviewStatus.APPROVED,
                    primaryNotes = reason,
                    primaryTimestamp = System.currentTimeMillis(),
                    isDualApprovalRequired = true,
                    isDualApprovalCompleted = false,
                    scheduledReReviewDate = scheduledReReviewDate,
                )

            val updatedAuditLogs =
                item.auditLogs +
                    ShariaAuditLog(
                        itemId = itemId,
                        reviewerId = reviewerId,
                        reviewerName = reviewerName,
                        action = "PRIMARY_APPROVAL",
                        details = "تم الاعتماد الأولي. نظراً لحساسية الموضوع الشرعي (مستوى $isCritical)، يتطلب اعتماداً مشتركاً من مراجع ثانٍ: $reason",
                    )

            val updatedItem =
                item.copy(
                    status = ShariaReviewStatus.DUAL_APPROVAL_PENDING,
                    isDualApprovalRequired = true,
                    decision = decision,
                    updatedAt = System.currentTimeMillis(),
                    auditLogs = updatedAuditLogs,
                )

            currentList[index] = updatedItem
            _itemsFlow.value = currentList
            return Resource.Success(Unit)
        } else {
            // Direct Approval
            val decision =
                ShariaReviewDecision(
                    primaryReviewerId = reviewerId,
                    primaryReviewerName = reviewerName,
                    primaryStatus = ShariaReviewStatus.APPROVED,
                    primaryNotes = reason,
                    primaryTimestamp = System.currentTimeMillis(),
                    isDualApprovalRequired = false,
                    isDualApprovalCompleted = true,
                    scheduledReReviewDate = scheduledReReviewDate,
                )

            val updatedAuditLogs =
                item.auditLogs +
                    ShariaAuditLog(
                        itemId = itemId,
                        reviewerId = reviewerId,
                        reviewerName = reviewerName,
                        action = "APPROVE",
                        details = "تم اعتماد المحتوى شرعياً والموافقة على النشر: $reason",
                    )

            val updatedItem =
                item.copy(
                    status = ShariaReviewStatus.APPROVED,
                    decision = decision,
                    updatedAt = System.currentTimeMillis(),
                    auditLogs = updatedAuditLogs,
                )

            currentList[index] = updatedItem
            _itemsFlow.value = currentList
            return Resource.Success(Unit)
        }
    }

    override suspend fun rejectItem(
        itemId: String,
        reviewerId: String,
        reviewerName: String,
        reason: String,
    ): Resource<Unit> {
        val currentList = _itemsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == itemId }
        if (index == -1) return Resource.Error("عنصر المراجعة غير موجود")

        val item = currentList[index]
        if (item.creatorId == reviewerId) {
            return Resource.Error("لا يمكن لصانع المحتوى تقييم محتواه الذاتي")
        }

        val decision =
            ShariaReviewDecision(
                primaryReviewerId = reviewerId,
                primaryReviewerName = reviewerName,
                primaryStatus = ShariaReviewStatus.REJECTED,
                primaryNotes = reason,
                primaryTimestamp = System.currentTimeMillis(),
            )

        val updatedAuditLogs =
            item.auditLogs +
                ShariaAuditLog(
                    itemId = itemId,
                    reviewerId = reviewerId,
                    reviewerName = reviewerName,
                    action = "REJECT",
                    details = "تم رفض المحتوى شرعياً لتعارضه مع الضوابط المعتمدة: $reason",
                )

        val updatedItem =
            item.copy(
                status = ShariaReviewStatus.REJECTED,
                decision = decision,
                updatedAt = System.currentTimeMillis(),
                auditLogs = updatedAuditLogs,
            )

        currentList[index] = updatedItem
        _itemsFlow.value = currentList
        return Resource.Success(Unit)
    }

    override suspend fun requestChanges(
        itemId: String,
        reviewerId: String,
        reviewerName: String,
        requiredChanges: String,
    ): Resource<Unit> {
        val currentList = _itemsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == itemId }
        if (index == -1) return Resource.Error("عنصر المراجعة غير موجود")

        val item = currentList[index]
        if (item.creatorId == reviewerId) {
            return Resource.Error("لا يمكن لصانع المحتوى تقييم محتواه الذاتي")
        }

        val decision =
            ShariaReviewDecision(
                primaryReviewerId = reviewerId,
                primaryReviewerName = reviewerName,
                primaryStatus = ShariaReviewStatus.CHANGES_REQUESTED,
                primaryNotes = requiredChanges,
                primaryTimestamp = System.currentTimeMillis(),
            )

        val updatedAuditLogs =
            item.auditLogs +
                ShariaAuditLog(
                    itemId = itemId,
                    reviewerId = reviewerId,
                    reviewerName = reviewerName,
                    action = "REQUEST_CHANGES",
                    details = "طُلب تعديل شرعي من المنشئ: $requiredChanges",
                )

        val updatedItem =
            item.copy(
                status = ShariaReviewStatus.CHANGES_REQUESTED,
                decision = decision,
                updatedAt = System.currentTimeMillis(),
                auditLogs = updatedAuditLogs,
            )

        currentList[index] = updatedItem
        _itemsFlow.value = currentList
        return Resource.Success(Unit)
    }

    override suspend fun escalateToSecondReviewer(
        itemId: String,
        reviewerId: String,
        reviewerName: String,
        targetReviewerId: String,
        targetReviewerName: String,
        reason: String,
    ): Resource<Unit> {
        val currentList = _itemsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == itemId }
        if (index == -1) return Resource.Error("عنصر المراجعة غير موجود")

        val item = currentList[index]
        if (targetReviewerId == item.creatorId) {
            return Resource.Error("لا يمكن تحويل المراجعة لصانع المحتوى نفسه")
        }
        if (targetReviewerId == reviewerId) {
            return Resource.Error("المراجع الثاني يجب أن يكون شخصاً مختلفاً ومؤهلاً")
        }

        val updatedAuditLogs =
            item.auditLogs +
                ShariaAuditLog(
                    itemId = itemId,
                    reviewerId = reviewerId,
                    reviewerName = reviewerName,
                    action = "ESCALATE_TO_SECOND_REVIEWER",
                    details = "تم تحويل المحتوى إلى المراجع الشرعي ($targetReviewerName): $reason",
                )

        val updatedItem =
            item.copy(
                status = ShariaReviewStatus.ESCALATED_SECOND_REVIEW,
                currentReviewerId = targetReviewerId,
                currentReviewerName = targetReviewerName,
                updatedAt = System.currentTimeMillis(),
                auditLogs = updatedAuditLogs,
            )

        currentList[index] = updatedItem
        _itemsFlow.value = currentList
        return Resource.Success(Unit)
    }

    override suspend fun submitSecondReviewDecision(
        itemId: String,
        secondReviewerId: String,
        secondReviewerName: String,
        approve: Boolean,
        reason: String,
    ): Resource<Unit> {
        val currentList = _itemsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == itemId }
        if (index == -1) return Resource.Error("عنصر المراجعة غير موجود")

        val item = currentList[index]
        if (item.creatorId == secondReviewerId) {
            return Resource.Error("لا يمكن لصانع المحتوى اعتماد محتواه بنفسه")
        }
        if (item.decision?.primaryReviewerId == secondReviewerId) {
            return Resource.Error("المراجع الثاني يجب أن يكون مراجعاً مستقلاً عن المراجع الأول")
        }

        val existingDecision =
            item.decision ?: ShariaReviewDecision(
                primaryReviewerId = "unknown",
                primaryReviewerName = "المراجع الأول",
                primaryStatus = ShariaReviewStatus.PENDING,
                primaryNotes = "",
            )

        val newStatus = if (approve) ShariaReviewStatus.APPROVED else ShariaReviewStatus.CHANGES_REQUESTED

        val updatedDecision =
            existingDecision.copy(
                secondReviewerId = secondReviewerId,
                secondReviewerName = secondReviewerName,
                secondStatus = newStatus,
                secondNotes = reason,
                secondTimestamp = System.currentTimeMillis(),
                isDualApprovalCompleted = approve,
            )

        val actionName = if (approve) "DUAL_APPROVAL_CONFIRMED" else "DUAL_APPROVAL_REJECTED"
        val updatedAuditLogs =
            item.auditLogs +
                ShariaAuditLog(
                    itemId = itemId,
                    reviewerId = secondReviewerId,
                    reviewerName = secondReviewerName,
                    action = actionName,
                    details = "قرار المراجع الثاني: ${if (approve) "موافقة واعتماد نهائي" else "طلب مراجعة/تعديل"}: $reason",
                )

        val updatedItem =
            item.copy(
                status = newStatus,
                decision = updatedDecision,
                updatedAt = System.currentTimeMillis(),
                auditLogs = updatedAuditLogs,
            )

        currentList[index] = updatedItem
        _itemsFlow.value = currentList
        return Resource.Success(Unit)
    }

    override suspend fun addClaimComment(
        itemId: String,
        claimId: String,
        reviewerId: String,
        reviewerName: String,
        comment: String,
    ): Resource<Unit> {
        val currentList = _itemsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == itemId }
        if (index == -1) return Resource.Error("عنصر المراجعة غير موجود")

        val item = currentList[index]
        val updatedClaims =
            item.claims.map { claim ->
                if (claim.id == claimId) {
                    claim.copy(reviewerComment = comment)
                } else {
                    claim
                }
            }

        val updatedAuditLogs =
            item.auditLogs +
                ShariaAuditLog(
                    itemId = itemId,
                    reviewerId = reviewerId,
                    reviewerName = reviewerName,
                    action = "ADD_CLAIM_COMMENT",
                    details = "إضافة تعليق موضعي على المطالبة: $comment",
                )

        val updatedItem =
            item.copy(
                claims = updatedClaims,
                updatedAt = System.currentTimeMillis(),
                auditLogs = updatedAuditLogs,
            )

        currentList[index] = updatedItem
        _itemsFlow.value = currentList
        return Resource.Success(Unit)
    }

    override suspend fun addInternalNote(
        itemId: String,
        authorId: String,
        authorName: String,
        note: String,
    ): Resource<Unit> {
        val currentList = _itemsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == itemId }
        if (index == -1) return Resource.Error("عنصر المراجعة غير موجود")

        val item = currentList[index]
        val newNote =
            InternalNote(
                authorId = authorId,
                authorName = authorName,
                noteText = note,
                createdAt = System.currentTimeMillis(),
            )

        val updatedAuditLogs =
            item.auditLogs +
                ShariaAuditLog(
                    itemId = itemId,
                    reviewerId = authorId,
                    reviewerName = authorName,
                    action = "ADD_INTERNAL_NOTE",
                    details = "تمت إضافة ملاحظة داخلية سرية بين المراجعين",
                )

        val updatedItem =
            item.copy(
                internalNotes = item.internalNotes + newNote,
                updatedAt = System.currentTimeMillis(),
                auditLogs = updatedAuditLogs,
            )

        currentList[index] = updatedItem
        _itemsFlow.value = currentList
        return Resource.Success(Unit)
    }

    override suspend fun scheduleReReviewDate(
        itemId: String,
        reviewerId: String,
        reviewerName: String,
        reReviewTimestamp: Long,
    ): Resource<Unit> {
        val currentList = _itemsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == itemId }
        if (index == -1) return Resource.Error("عنصر المراجعة غير موجود")

        val item = currentList[index]
        val currentDecision =
            item.decision ?: ShariaReviewDecision(
                primaryReviewerId = reviewerId,
                primaryReviewerName = reviewerName,
                primaryStatus = item.status,
                primaryNotes = "جدولة موعد إعادة المراجعة",
            )

        val updatedDecision =
            currentDecision.copy(
                scheduledReReviewDate = reReviewTimestamp,
            )

        val updatedAuditLogs =
            item.auditLogs +
                ShariaAuditLog(
                    itemId = itemId,
                    reviewerId = reviewerId,
                    reviewerName = reviewerName,
                    action = "SCHEDULE_RE_REVIEW",
                    details = "تم تحديد موعد لإعادة التدقيق الشرعي الدوري",
                )

        val updatedItem =
            item.copy(
                decision = updatedDecision,
                updatedAt = System.currentTimeMillis(),
                auditLogs = updatedAuditLogs,
            )

        currentList[index] = updatedItem
        _itemsFlow.value = currentList
        return Resource.Success(Unit)
    }

    override suspend fun notifyContentModified(
        itemId: String,
        editorId: String,
        editorName: String,
        summary: String,
        newFullText: String,
    ): Resource<Unit> {
        val currentList = _itemsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == itemId }
        if (index == -1) return Resource.Error("عنصر المراجعة غير موجود")

        val item = currentList[index]
        val newVersion =
            RevisionHistoryItem(
                versionNumber = item.revisions.size + 1,
                editedBy = editorName,
                editedAt = System.currentTimeMillis(),
                changeSummary = summary,
                fullTextSnapshot = newFullText,
            )

        val updatedAuditLogs =
            item.auditLogs +
                ShariaAuditLog(
                    itemId = itemId,
                    reviewerId = editorId,
                    reviewerName = editorName,
                    action = "CONTENT_EDITED",
                    details = "قام المنشئ بتعديل المحتوى. تم إعادة حالة المراجعة تلقائياً للتدقيق: $summary",
                )

        // Strict Rule: Modifying approved content resets it back to review
        val updatedItem =
            item.copy(
                status = ShariaReviewStatus.PENDING,
                contentVersion = item.contentVersion + 1,
                fullContentText = newFullText,
                revisions = item.revisions + newVersion,
                updatedAt = System.currentTimeMillis(),
                auditLogs = updatedAuditLogs,
            )

        currentList[index] = updatedItem
        _itemsFlow.value = currentList
        return Resource.Success(Unit)
    }
}
