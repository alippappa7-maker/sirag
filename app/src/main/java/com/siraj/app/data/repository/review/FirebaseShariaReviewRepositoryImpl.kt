package com.siraj.app.data.repository.review

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.review.*
import com.siraj.app.domain.repository.review.ShariaReviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

class FirebaseShariaReviewRepositoryImpl : ShariaReviewRepository {

    private val _itemsFlow = MutableStateFlow<List<ShariaReviewItem>>(createInitialMockData())
    val itemsFlow = _itemsFlow.asStateFlow()

    override fun getReviewQueue(filter: ShariaReviewFilter): Flow<Resource<List<ShariaReviewItem>>> {
        return _itemsFlow.map { list ->
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
                    filtered = filtered.filter {
                        it.contentTitle.lowercase().contains(q) ||
                                it.fullContentText.lowercase().contains(q) ||
                                it.creatorName.lowercase().contains(q) ||
                                it.claims.any { c -> c.claimText.lowercase().contains(q) || c.sourceTitle.lowercase().contains(q) }
                    }
                }

                filtered = if (filter.sortByDateAscending) {
                    filtered.sortedBy { it.submittedAt }
                } else {
                    filtered.sortedByDescending { it.submittedAt }
                }

                Resource.Success(filtered)
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "حدث خطأ أثناء تحميل قائمة المراجعة الشرعية")
            }
        }
    }

    override fun getReviewItemById(itemId: String): Flow<Resource<ShariaReviewItem>> {
        return _itemsFlow.map { list ->
            val found = list.find { it.id == itemId }
            if (found != null) {
                Resource.Success(found)
            } else {
                Resource.Error("عنصر المراجعة غير موجود")
            }
        }
    }

    override suspend fun claimReview(itemId: String, reviewerId: String, reviewerName: String): Resource<Unit> {
        val currentList = _itemsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == itemId }
        if (index == -1) return Resource.Error("عنصر المراجعة غير موجود")

        val item = currentList[index]
        if (item.creatorId == reviewerId) {
            return Resource.Error("لا يحق لصانع المحتوى مراجعة أو حجز محتواه الخاص")
        }

        val updatedAuditLogs = item.auditLogs + ShariaAuditLog(
            itemId = itemId,
            reviewerId = reviewerId,
            reviewerName = reviewerName,
            action = "CLAIM_REVIEW",
            details = "قام المراجع بحجز المحتوى وبدء التدقيق الشرعي"
        )

        val updatedItem = item.copy(
            status = ShariaReviewStatus.IN_REVIEW,
            currentReviewerId = reviewerId,
            currentReviewerName = reviewerName,
            updatedAt = System.currentTimeMillis(),
            auditLogs = updatedAuditLogs
        )

        currentList[index] = updatedItem
        _itemsFlow.value = currentList
        return Resource.Success(Unit)
    }

    override suspend fun releaseReview(itemId: String, reviewerId: String, reviewerName: String): Resource<Unit> {
        val currentList = _itemsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == itemId }
        if (index == -1) return Resource.Error("عنصر المراجعة غير موجود")

        val item = currentList[index]
        val updatedAuditLogs = item.auditLogs + ShariaAuditLog(
            itemId = itemId,
            reviewerId = reviewerId,
            reviewerName = reviewerName,
            action = "RELEASE_REVIEW",
            details = "تم إلغاء حجز المراجعة وإعادة العنصر لقائمة الانتظار"
        )

        val updatedItem = item.copy(
            status = ShariaReviewStatus.PENDING,
            currentReviewerId = null,
            currentReviewerName = null,
            updatedAt = System.currentTimeMillis(),
            auditLogs = updatedAuditLogs
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
        scheduledReReviewDate: Long?
    ): Resource<Unit> {
        val currentList = _itemsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == itemId }
        if (index == -1) return Resource.Error("عنصر المراجعة غير موجود")

        val item = currentList[index]
        if (item.creatorId == reviewerId) {
            return Resource.Error("لا يمكن لصانع المحتوى اعتماد محتواه بنفسه حفاظاً على النزاهة الشرعية")
        }

        val isCritical = item.riskLevel == RiskLevel.CRITICAL ||
                item.criticalTopics.any { it != CriticalTopic.NONE }

        if (isCritical) {
            // Needs second reviewer confirmation
            val decision = ShariaReviewDecision(
                primaryReviewerId = reviewerId,
                primaryReviewerName = reviewerName,
                primaryStatus = ShariaReviewStatus.APPROVED,
                primaryNotes = reason,
                primaryTimestamp = System.currentTimeMillis(),
                isDualApprovalRequired = true,
                isDualApprovalCompleted = false,
                scheduledReReviewDate = scheduledReReviewDate
            )

            val updatedAuditLogs = item.auditLogs + ShariaAuditLog(
                itemId = itemId,
                reviewerId = reviewerId,
                reviewerName = reviewerName,
                action = "PRIMARY_APPROVAL",
                details = "تم الاعتماد الأولي. نظراً لحساسية الموضوع الشرعي (مستوى $isCritical)، يتطلب اعتماداً مشتركاً من مراجع ثانٍ: $reason"
            )

            val updatedItem = item.copy(
                status = ShariaReviewStatus.DUAL_APPROVAL_PENDING,
                isDualApprovalRequired = true,
                decision = decision,
                updatedAt = System.currentTimeMillis(),
                auditLogs = updatedAuditLogs
            )

            currentList[index] = updatedItem
            _itemsFlow.value = currentList
            return Resource.Success(Unit)
        } else {
            // Direct Approval
            val decision = ShariaReviewDecision(
                primaryReviewerId = reviewerId,
                primaryReviewerName = reviewerName,
                primaryStatus = ShariaReviewStatus.APPROVED,
                primaryNotes = reason,
                primaryTimestamp = System.currentTimeMillis(),
                isDualApprovalRequired = false,
                isDualApprovalCompleted = true,
                scheduledReReviewDate = scheduledReReviewDate
            )

            val updatedAuditLogs = item.auditLogs + ShariaAuditLog(
                itemId = itemId,
                reviewerId = reviewerId,
                reviewerName = reviewerName,
                action = "APPROVE",
                details = "تم اعتماد المحتوى شرعياً والموافقة على النشر: $reason"
            )

            val updatedItem = item.copy(
                status = ShariaReviewStatus.APPROVED,
                decision = decision,
                updatedAt = System.currentTimeMillis(),
                auditLogs = updatedAuditLogs
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
        reason: String
    ): Resource<Unit> {
        val currentList = _itemsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == itemId }
        if (index == -1) return Resource.Error("عنصر المراجعة غير موجود")

        val item = currentList[index]
        if (item.creatorId == reviewerId) {
            return Resource.Error("لا يمكن لصانع المحتوى تقييم محتواه الذاتي")
        }

        val decision = ShariaReviewDecision(
            primaryReviewerId = reviewerId,
            primaryReviewerName = reviewerName,
            primaryStatus = ShariaReviewStatus.REJECTED,
            primaryNotes = reason,
            primaryTimestamp = System.currentTimeMillis()
        )

        val updatedAuditLogs = item.auditLogs + ShariaAuditLog(
            itemId = itemId,
            reviewerId = reviewerId,
            reviewerName = reviewerName,
            action = "REJECT",
            details = "تم رفض المحتوى شرعياً لتعارضه مع الضوابط المعتمدة: $reason"
        )

        val updatedItem = item.copy(
            status = ShariaReviewStatus.REJECTED,
            decision = decision,
            updatedAt = System.currentTimeMillis(),
            auditLogs = updatedAuditLogs
        )

        currentList[index] = updatedItem
        _itemsFlow.value = currentList
        return Resource.Success(Unit)
    }

    override suspend fun requestChanges(
        itemId: String,
        reviewerId: String,
        reviewerName: String,
        requiredChanges: String
    ): Resource<Unit> {
        val currentList = _itemsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == itemId }
        if (index == -1) return Resource.Error("عنصر المراجعة غير موجود")

        val item = currentList[index]
        if (item.creatorId == reviewerId) {
            return Resource.Error("لا يمكن لصانع المحتوى تقييم محتواه الذاتي")
        }

        val decision = ShariaReviewDecision(
            primaryReviewerId = reviewerId,
            primaryReviewerName = reviewerName,
            primaryStatus = ShariaReviewStatus.CHANGES_REQUESTED,
            primaryNotes = requiredChanges,
            primaryTimestamp = System.currentTimeMillis()
        )

        val updatedAuditLogs = item.auditLogs + ShariaAuditLog(
            itemId = itemId,
            reviewerId = reviewerId,
            reviewerName = reviewerName,
            action = "REQUEST_CHANGES",
            details = "طُلب تعديل شرعي من المنشئ: $requiredChanges"
        )

        val updatedItem = item.copy(
            status = ShariaReviewStatus.CHANGES_REQUESTED,
            decision = decision,
            updatedAt = System.currentTimeMillis(),
            auditLogs = updatedAuditLogs
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
        reason: String
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

        val updatedAuditLogs = item.auditLogs + ShariaAuditLog(
            itemId = itemId,
            reviewerId = reviewerId,
            reviewerName = reviewerName,
            action = "ESCALATE_TO_SECOND_REVIEWER",
            details = "تم تحويل المحتوى إلى المراجع الشرعي ($targetReviewerName): $reason"
        )

        val updatedItem = item.copy(
            status = ShariaReviewStatus.ESCALATED_SECOND_REVIEW,
            currentReviewerId = targetReviewerId,
            currentReviewerName = targetReviewerName,
            updatedAt = System.currentTimeMillis(),
            auditLogs = updatedAuditLogs
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
        reason: String
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

        val existingDecision = item.decision ?: ShariaReviewDecision(
            primaryReviewerId = "unknown",
            primaryReviewerName = "المراجع الأول",
            primaryStatus = ShariaReviewStatus.PENDING,
            primaryNotes = ""
        )

        val newStatus = if (approve) ShariaReviewStatus.APPROVED else ShariaReviewStatus.CHANGES_REQUESTED

        val updatedDecision = existingDecision.copy(
            secondReviewerId = secondReviewerId,
            secondReviewerName = secondReviewerName,
            secondStatus = newStatus,
            secondNotes = reason,
            secondTimestamp = System.currentTimeMillis(),
            isDualApprovalCompleted = approve
        )

        val actionName = if (approve) "DUAL_APPROVAL_CONFIRMED" else "DUAL_APPROVAL_REJECTED"
        val updatedAuditLogs = item.auditLogs + ShariaAuditLog(
            itemId = itemId,
            reviewerId = secondReviewerId,
            reviewerName = secondReviewerName,
            action = actionName,
            details = "قرار المراجع الثاني: ${if (approve) "موافقة واعتماد نهائي" else "طلب مراجعة/تعديل"}: $reason"
        )

        val updatedItem = item.copy(
            status = newStatus,
            decision = updatedDecision,
            updatedAt = System.currentTimeMillis(),
            auditLogs = updatedAuditLogs
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
        comment: String
    ): Resource<Unit> {
        val currentList = _itemsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == itemId }
        if (index == -1) return Resource.Error("عنصر المراجعة غير موجود")

        val item = currentList[index]
        val updatedClaims = item.claims.map { claim ->
            if (claim.id == claimId) {
                claim.copy(reviewerComment = comment)
            } else claim
        }

        val updatedAuditLogs = item.auditLogs + ShariaAuditLog(
            itemId = itemId,
            reviewerId = reviewerId,
            reviewerName = reviewerName,
            action = "ADD_CLAIM_COMMENT",
            details = "إضافة تعليق موضعي على المطالبة: $comment"
        )

        val updatedItem = item.copy(
            claims = updatedClaims,
            updatedAt = System.currentTimeMillis(),
            auditLogs = updatedAuditLogs
        )

        currentList[index] = updatedItem
        _itemsFlow.value = currentList
        return Resource.Success(Unit)
    }

    override suspend fun addInternalNote(
        itemId: String,
        authorId: String,
        authorName: String,
        note: String
    ): Resource<Unit> {
        val currentList = _itemsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == itemId }
        if (index == -1) return Resource.Error("عنصر المراجعة غير موجود")

        val item = currentList[index]
        val newNote = InternalNote(
            authorId = authorId,
            authorName = authorName,
            noteText = note,
            createdAt = System.currentTimeMillis()
        )

        val updatedAuditLogs = item.auditLogs + ShariaAuditLog(
            itemId = itemId,
            reviewerId = authorId,
            reviewerName = authorName,
            action = "ADD_INTERNAL_NOTE",
            details = "تمت إضافة ملاحظة داخلية سرية بين المراجعين"
        )

        val updatedItem = item.copy(
            internalNotes = item.internalNotes + newNote,
            updatedAt = System.currentTimeMillis(),
            auditLogs = updatedAuditLogs
        )

        currentList[index] = updatedItem
        _itemsFlow.value = currentList
        return Resource.Success(Unit)
    }

    override suspend fun scheduleReReviewDate(
        itemId: String,
        reviewerId: String,
        reviewerName: String,
        reReviewTimestamp: Long
    ): Resource<Unit> {
        val currentList = _itemsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == itemId }
        if (index == -1) return Resource.Error("عنصر المراجعة غير موجود")

        val item = currentList[index]
        val currentDecision = item.decision ?: ShariaReviewDecision(
            primaryReviewerId = reviewerId,
            primaryReviewerName = reviewerName,
            primaryStatus = item.status,
            primaryNotes = "جدولة موعد إعادة المراجعة"
        )

        val updatedDecision = currentDecision.copy(
            scheduledReReviewDate = reReviewTimestamp
        )

        val updatedAuditLogs = item.auditLogs + ShariaAuditLog(
            itemId = itemId,
            reviewerId = reviewerId,
            reviewerName = reviewerName,
            action = "SCHEDULE_RE_REVIEW",
            details = "تم تحديد موعد لإعادة التدقيق الشرعي الدوري"
        )

        val updatedItem = item.copy(
            decision = updatedDecision,
            updatedAt = System.currentTimeMillis(),
            auditLogs = updatedAuditLogs
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
        newFullText: String
    ): Resource<Unit> {
        val currentList = _itemsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == itemId }
        if (index == -1) return Resource.Error("عنصر المراجعة غير موجود")

        val item = currentList[index]
        val newVersion = RevisionHistoryItem(
            versionNumber = item.revisions.size + 1,
            editedBy = editorName,
            editedAt = System.currentTimeMillis(),
            changeSummary = summary,
            fullTextSnapshot = newFullText
        )

        val updatedAuditLogs = item.auditLogs + ShariaAuditLog(
            itemId = itemId,
            reviewerId = editorId,
            reviewerName = editorName,
            action = "CONTENT_EDITED",
            details = "قام المنشئ بتعديل المحتوى. تم إعادة حالة المراجعة تلقائياً للتدقيق: $summary"
        )

        // Strict Rule: Modifying approved content resets it back to review
        val updatedItem = item.copy(
            status = ShariaReviewStatus.PENDING,
            contentVersion = item.contentVersion + 1,
            fullContentText = newFullText,
            revisions = item.revisions + newVersion,
            updatedAt = System.currentTimeMillis(),
            auditLogs = updatedAuditLogs
        )

        currentList[index] = updatedItem
        _itemsFlow.value = currentList
        return Resource.Success(Unit)
    }

    private fun createInitialMockData(): List<ShariaReviewItem> {
        return listOf(
            // Item 1: Hadith video (Low Risk, Pending)
            ShariaReviewItem(
                id = "review_item_001",
                projectId = "proj_001",
                contentTitle = "شرح حديث إنما الأعمال بالنيات",
                creatorId = "creator_ahmed",
                creatorName = "أحمد المنصور",
                fullContentText = "فيديو قصير يشرح أهمية الإخلاص في العمل الصالح انطلاقاً من حديث عمر بن الخطاب رضي الله عنه: سمعت رسول الله صلى الله عليه وسلم يقول: إنما الأعمال بالنيات وإنما لكل امرئ ما نوى. فمن كانت هجرته إلى الله ورسوله فهجرته إلى الله ورسوله.",
                category = "الحديث الشريف",
                riskLevel = RiskLevel.LOW,
                criticalTopics = listOf(CriticalTopic.NONE),
                status = ShariaReviewStatus.PENDING,
                claims = listOf(
                    ShariaClaim(
                        id = "claim_001_1",
                        claimText = "حديث إنما الأعمال بالنيات وإنما لكل امرئ ما نوى",
                        positionContext = "المشهد 1 (00:00 - 00:15) - قراءة الراوي",
                        sourceType = "HADITH",
                        sourceTitle = "صحيح البخاري",
                        sourceReference = "كتاب بدء الوحي، باب كيف كان بدء الوحي، رقم الحديث: 1",
                        sourceUrl = "https://sunnah.com/bukhari:1",
                        originalSourceText = "حَدَّثَنَا الْحُمَيْدِيُّ عَبْدُ اللَّهِ بْنُ الزُّبَيْرِ، قَالَ: حَدَّثَنَا سُفْيَانُ، قَالَ: حَدَّثَنَا يَحْيَى بْنُ سَعِيدٍ الأَنْصَارِيُّ، عَنْ مُحَمَّدِ بْنِ إِبْرَاهِيمَ التَّيْمِيِّ، أَنَّهُ سَمِعَ عَلْقَمَةَ بْنَ وَقَّاصٍ اللَّيْثِيَّ، يَقُولُ: سَمِعْتُ عُمَرَ بْنَ الْخَطَّابِ رَضِيَ اللَّهُ عَنْهُ عَلَى الْمِنْبَرِ قَالَ: سَمِعْتُ رَسُولَ اللَّهِ صلى الله عليه وسلم يَقُولُ: «إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى»",
                        hadithGrade = "صحيح متفق عليه (أخرجه البخاري ومسلم)",
                        hadithNarrator = "عمر بن الخطاب رضي الله عنه",
                        sourceVariations = listOf(
                            SourceVariation(
                                sourceName = "صحيح مسلم",
                                narratorOrScholar = "الإمام مسلم بن الحجاج",
                                text = "إنما الأعمال بالنية ولكل امرئ ما نوى",
                                grade = "صحيح - رقم 1907",
                                notes = "بلفظ الإفراد (بالنية)"
                            )
                        ),
                        isVerified = true
                    )
                ),
                revisions = listOf(
                    RevisionHistoryItem(
                        versionNumber = 1,
                        editedBy = "أحمد المنصور",
                        editedAt = System.currentTimeMillis() - 86400000L * 2,
                        changeSummary = "إنشاء المسودة الأولى وتوثيق الحديث",
                        fullTextSnapshot = "فيديو قصير يشرح أهمية الإخلاص في العمل الصالح انطلاقاً من حديث عمر بن الخطاب رضي الله عنه."
                    )
                ),
                internalNotes = listOf(
                    InternalNote(
                        authorId = "rev_khalid",
                        authorName = "د. خالد السعيد (مراجع شرعي)",
                        noteText = "النص سليم وتخريج الحديث دقيق من صحيح البخاري وصحيح مسلم.",
                        createdAt = System.currentTimeMillis() - 3600000L * 5
                    )
                ),
                auditLogs = listOf(
                    ShariaAuditLog(
                        itemId = "review_item_001",
                        reviewerId = "creator_ahmed",
                        reviewerName = "أحمد المنصور",
                        action = "SUBMIT",
                        details = "تم إرسال المحتوى إلى قائمة المراجعة الشرعية",
                        timestamp = System.currentTimeMillis() - 86400000L
                    )
                ),
                submittedAt = System.currentTimeMillis() - 86400000L,
                updatedAt = System.currentTimeMillis() - 3600000L * 5
            ),

            // Item 2: Critical Financial Transactions (Cryptocurrency & Staking Fiqh) - Dual Approval Required
            ShariaReviewItem(
                id = "review_item_002",
                projectId = "proj_002",
                contentTitle = "حكم المعاملات المالية بالعملات الرقمية وآلية التخزين (Staking)",
                creatorId = "creator_mahmoud",
                creatorName = "محمود الزهراني",
                fullContentText = "مقطع وثائقي يناقش الفروق الجوهرية بين التعدين والتخزين المقفل للعملات المشفرة مع بيان الحكم الفقهي لفوائد التخزين والربا المحتمل.",
                category = "المعاملات المالية",
                riskLevel = RiskLevel.CRITICAL,
                criticalTopics = listOf(CriticalTopic.FINANCIAL_TRANSACTIONS, CriticalTopic.FATWA),
                status = ShariaReviewStatus.DUAL_APPROVAL_PENDING,
                currentReviewerId = "rev_othman",
                currentReviewerName = "الشيخ عثمان الدوسري",
                claims = listOf(
                    ShariaClaim(
                        id = "claim_002_1",
                        claimText = "اشتراط التقابض في صرف العملات وتكييف أرباح الـ Staking كعقد مضاربة أو إجارة معاصرة",
                        positionContext = "المشهد 2 (00:30 - 01:10) - نص الشرح الفقهي",
                        sourceType = "FIQH",
                        sourceTitle = "قرارات مجمع الفقه الإسلامي الدولي",
                        sourceReference = "قرار رقم 63 (1/7) بشأن الأسواق المالية والعملات",
                        sourceUrl = "https://iifa-aifi.org",
                        originalSourceText = "العملات الورقية نقود اعتبارية فيها صفة الثمنية كاملة ولها أحكام النقود في وجوب الزكاة وجريان الربا بنوعيه فضلًا ونساءً.",
                        hadithGrade = "قرار مجمعي معتمد",
                        sourceVariations = listOf(
                            SourceVariation(
                                sourceName = "هيئة المحاسبة والمراجعة للمؤسسات المالية الإسلامية (AAOIFI)",
                                narratorOrScholar = "المعيار الشرعي رقم (57)",
                                text = "ضوابط التعامل بالذهب والفضة والعملات المشفرة وما يترتب على غررها",
                                grade = "معيار مؤسسي",
                                notes = "يوجد خلاف معاصر معتبر في تكييف عائد الـ Proof of Stake"
                            )
                        ),
                        isVerified = true,
                        reviewerComment = "التكييف الفقهي يحتاج تدقيقاً إضافياً من المراجع الثاني لتحديد إن كان يشترط التحذير من المخاطر"
                    )
                ),
                revisions = listOf(
                    RevisionHistoryItem(
                        versionNumber = 1,
                        editedBy = "محمود الزهراني",
                        editedAt = System.currentTimeMillis() - 86400000L * 4,
                        changeSummary = "المسودة المبدئية",
                        fullTextSnapshot = "مقطع يناقش حكم العملات الرقمية."
                    ),
                    RevisionHistoryItem(
                        versionNumber = 2,
                        editedBy = "محمود الزهراني",
                        editedAt = System.currentTimeMillis() - 86400000L * 2,
                        changeSummary = "إضافة نص قرار مجمع الفقه الإسلامي الدولي وحذف الفتوى الجازمة",
                        fullTextSnapshot = "مقطع وثائقي يناقش الفروق الجوهرية بين التعدين والتخزين المقفل للعملات المشفرة مع بيان الحكم الفقهي لفوائد التخزين والربا المحتمل."
                    )
                ),
                internalNotes = listOf(
                    InternalNote(
                        authorId = "rev_othman",
                        authorName = "الشيخ عثمان الدوسري",
                        noteText = "هذا الموضوع يدخل تحت المعاملات المالية المستحدثة عالية المخاطر. تم الاعتماد الأولي مع إحالة الملف لمراجع ثانٍ متخصص في فقه الاقتصاد الإسلامي.",
                        createdAt = System.currentTimeMillis() - 3600000L * 12
                    )
                ),
                auditLogs = listOf(
                    ShariaAuditLog(
                        itemId = "review_item_002",
                        reviewerId = "rev_othman",
                        reviewerName = "الشيخ عثمان الدوسري",
                        action = "PRIMARY_APPROVAL",
                        details = "اعتماد أولي مشروط - بانتظار الاعتماد المشترك من مراجع ثانٍ",
                        timestamp = System.currentTimeMillis() - 3600000L * 12
                    )
                ),
                decision = ShariaReviewDecision(
                    primaryReviewerId = "rev_othman",
                    primaryReviewerName = "الشيخ عثمان الدوسري",
                    primaryStatus = ShariaReviewStatus.APPROVED,
                    primaryNotes = "المحتوى منضبط شرعياً ولم يجزم بالفتوى بل عرض قرار المجمع الفقهي.",
                    primaryTimestamp = System.currentTimeMillis() - 3600000L * 12,
                    isDualApprovalRequired = true,
                    isDualApprovalCompleted = false
                ),
                submittedAt = System.currentTimeMillis() - 86400000L * 2,
                updatedAt = System.currentTimeMillis() - 3600000L * 12
            ),

            // Item 3: Creed and Scholar Attributions (High Risk, In Review)
            ShariaReviewItem(
                id = "review_item_003",
                projectId = "proj_003",
                contentTitle = "ضوابط العذر بالجهل عند أئمة الدعوة النجدية",
                creatorId = "creator_faisal",
                creatorName = "فيصل الحربي",
                fullContentText = "استعراض تحقيقي لأقوال الشيخ محمد بن عبد الوهاب والشيخ ابن تيمية في مسألة قيام الحجة والعذر بالجهل في المسائل الخفية والظاهرة.",
                category = "العقيدة",
                riskLevel = RiskLevel.CRITICAL,
                criticalTopics = listOf(CriticalTopic.TAKFIER, CriticalTopic.CREED_DISPUTES, CriticalTopic.SCHOLAR_ATTRIBUTIONS),
                status = ShariaReviewStatus.IN_REVIEW,
                currentReviewerId = "rev_tariq",
                currentReviewerName = "د. طارق السلمان",
                claims = listOf(
                    ShariaClaim(
                        id = "claim_003_1",
                        claimText = "قول ابن تيمية: ليس لأحد أن يكفر أحداً من المسلمين وإن أخطأ وغلط حتى تقام عليه الحجة وتبين له المحجة",
                        positionContext = "المشهد 3 (01:20 - 02:00) - اقتباس على الشاشة",
                        sourceType = "SCHOLAR_QUOTE",
                        sourceTitle = "مجموع الفتاوى",
                        sourceReference = "المجلد 12، الصفحة 466",
                        sourceUrl = "https://shamela.ws",
                        originalSourceText = "وَأَنَا دَائِمًا - وَمَنْ جَالَسَنِي يَعْلَمُ ذَلِكَ مِنِّي - مِنْ أَعْظَمِ النَّاسِ نَهْيًا عَنْ أَنْ يُنْسَبَ مُعَيَّنٌ إلَى تَكْفِيرٍ وَتَفْسِيقٍ وَمَعْصِيَةٍ، إلَّا إذَا عُلِمَ أَنَّهُ قَدْ قَامَتْ عَلَيْهِ الْحُجَّةُ الرِّسَالِيَّةُ الَّتِي مَنْ خَالَفَهَا كَانَ كَافِرًا تَارَةً وَفَاسِقًا أُخْرَى وَعَاصِيًا أُخْرَى",
                        hadithGrade = "نص معتمد وموثق في مجموع الفتاوى",
                        sourceVariations = listOf(
                            SourceVariation(
                                sourceName = "الدرر السنية في الأجوبة النجدية",
                                narratorOrScholar = "الشيخ عبد الله بن محمد بن عبد الوهاب",
                                text = "بيان مسألة التكفير المعين والتفريق بين المسائل الظاهرة والخفية",
                                grade = "مرجع تاريخي",
                                notes = "مجلد العقائد"
                            )
                        ),
                        isVerified = true
                    )
                ),
                revisions = listOf(
                    RevisionHistoryItem(
                        versionNumber = 1,
                        editedBy = "فيصل الحربي",
                        editedAt = System.currentTimeMillis() - 86400000L * 3,
                        changeSummary = "إيداع المسودة للمراجعة",
                        fullTextSnapshot = "استعراض تحقيقي لأقوال الشيخ محمد بن عبد الوهاب والشيخ ابن تيمية في مسألة قيام الحجة."
                    )
                ),
                internalNotes = listOf(
                    InternalNote(
                        authorId = "rev_tariq",
                        authorName = "د. طارق السلمان",
                        noteText = "مسألة شديدة الحساسية تتطلب التحقق من سياق الاقتباس كاملاً دون اجتزاء.",
                        createdAt = System.currentTimeMillis() - 3600000L * 2
                    )
                ),
                auditLogs = listOf(
                    ShariaAuditLog(
                        itemId = "review_item_003",
                        reviewerId = "rev_tariq",
                        reviewerName = "د. طارق السلمان",
                        action = "CLAIM_REVIEW",
                        details = "حجز المحتوى للتدقيق العقدي والتحقق من نسبة الأقوال",
                        timestamp = System.currentTimeMillis() - 3600000L * 2
                    )
                ),
                submittedAt = System.currentTimeMillis() - 86400000L * 3,
                updatedAt = System.currentTimeMillis() - 3600000L * 2
            ),

            // Item 4: Family & Divorce Rulings (Medium Risk, Changes Requested)
            ShariaReviewItem(
                id = "review_item_004",
                projectId = "proj_004",
                contentTitle = "أحكام طلاق الغضبان في الفقه الإسلامي",
                creatorId = "creator_yousef",
                creatorName = "يوسف القحطاني",
                fullContentText = "فيديو توعوي يشرح درجات الغضب الثلاث وأثر كل درجة على وقوع الطلاق استناداً لحديث لا طلاق في إغلاق.",
                category = "الأسرة",
                riskLevel = RiskLevel.HIGH,
                criticalTopics = listOf(CriticalTopic.DIVORCE, CriticalTopic.FAMILY),
                status = ShariaReviewStatus.CHANGES_REQUESTED,
                currentReviewerId = "rev_khalid",
                currentReviewerName = "د. خالد السعيد",
                claims = listOf(
                    ShariaClaim(
                        id = "claim_004_1",
                        claimText = "حديث لا طلاق ولا عتاق في إغلاق",
                        positionContext = "المشهد 1 (00:00 - 00:25) - شريط النص الرئيسي",
                        sourceType = "HADITH",
                        sourceTitle = "سنن أبي داود",
                        sourceReference = "كتاب الطلاق، باب في الطلاق على غلط، رقم الحديث: 2193",
                        sourceUrl = "https://sunnah.com/abudawud:2193",
                        originalSourceText = "حَدَّثَنَا هَارُونُ بْنُ عَبْدِ اللَّهِ، حَدَّثَنَا ابْنُ أَبِي فُدَيْكٍ، عَنِ الضَّحَّاكِ بْنِ عُثْمَانَ، عَنْ أَبِي النَّضْرِ، عَنْ أَبِي سَلَمَةَ، عَنْ عَائِشَةَ، قَالَتْ: سَمِعْتُ رَسُولَ اللَّهِ صلى الله عليه وسلم يَقُولُ: «لاَ طَلاَقَ وَلاَ عَتَاقَ فِي إِغْلاَقٍ»",
                        hadithGrade = "حسن (صححه الحاكم والألباني، وحسنه ابن حجر)",
                        hadithNarrator = "عائشة أم المؤمنين رضي الله عنها",
                        sourceVariations = listOf(
                            SourceVariation(
                                sourceName = "سنن ابن ماجه",
                                narratorOrScholar = "ابن ماجه",
                                text = "لا طلاق ولا عتاق في غلاق",
                                grade = "حسن - رقم 2046",
                                notes = "بلفظ غلاق"
                            )
                        ),
                        isVerified = true,
                        reviewerComment = "الحديث صحيح ولكن يجب إيضاح أن القضاء في مسائل الطلاق يرجع للمحاكم الشرعية ولا يفتى به للمعيّن عبر الإنترنت"
                    )
                ),
                revisions = listOf(
                    RevisionHistoryItem(
                        versionNumber = 1,
                        editedBy = "يوسف القحطاني",
                        editedAt = System.currentTimeMillis() - 86400000L * 5,
                        changeSummary = "المسودة الأساسية",
                        fullTextSnapshot = "فيديو توعوي يشرح درجات الغضب الثلاث وأثر كل درجة على وقوع الطلاق."
                    )
                ),
                internalNotes = listOf(
                    InternalNote(
                        authorId = "rev_khalid",
                        authorName = "د. خالد السعيد",
                        noteText = "يلزم إلزام المنشئ بوضع تنبيه وإخلاء مسؤولية أن الفتوى الخاصة بالطلاق تلزم حضور الزوجين للقضاء.",
                        createdAt = System.currentTimeMillis() - 3600000L * 8
                    )
                ),
                decision = ShariaReviewDecision(
                    primaryReviewerId = "rev_khalid",
                    primaryReviewerName = "د. خالد السعيد",
                    primaryStatus = ShariaReviewStatus.CHANGES_REQUESTED,
                    primaryNotes = "يرجى إضافة تنبيه صريح في نهاية الفيديو يوضح أن قضايا الطلاق ترفع للمحاكم المختصة ولا يُكتفى بالمقاطع العامة.",
                    primaryTimestamp = System.currentTimeMillis() - 3600000L * 8,
                    isDualApprovalRequired = true,
                    isDualApprovalCompleted = false
                ),
                auditLogs = listOf(
                    ShariaAuditLog(
                        itemId = "review_item_004",
                        reviewerId = "rev_khalid",
                        reviewerName = "د. خالد السعيد",
                        action = "REQUEST_CHANGES",
                        details = "تم طلب تعديل شرعي لإضافة إخلاء المسؤولية القضائية",
                        timestamp = System.currentTimeMillis() - 3600000L * 8
                    )
                ),
                submittedAt = System.currentTimeMillis() - 86400000L * 5,
                updatedAt = System.currentTimeMillis() - 3600000L * 8
            ),

            // Item 5: Approved Quran Content (Low Risk, Approved)
            ShariaReviewItem(
                id = "review_item_005",
                projectId = "proj_005",
                contentTitle = "تأملات في سورة الكهف - قصة الفتية",
                creatorId = "creator_sami",
                creatorName = "سامي عبد الرحمن",
                fullContentText = "مقطع قصير يستعرض الدروس المستفادة من ثبات فتية الكهف على التوحيد مع تلاوة مجودة للآيات من 9 إلى 16.",
                category = "القرآن وعلومه",
                riskLevel = RiskLevel.LOW,
                criticalTopics = listOf(CriticalTopic.NONE),
                status = ShariaReviewStatus.APPROVED,
                claims = listOf(
                    ShariaClaim(
                        id = "claim_005_1",
                        claimText = "الآيات 9-16 من سورة الكهف برواية حفص عن عاصم",
                        positionContext = "المشهد 1-4 (كامل المقطع)",
                        sourceType = "QURAN",
                        sourceTitle = "مصحف مجمع الملك فهد لطباعة المصحف الشريف",
                        sourceReference = "سورة الكهف، الآيات 9-16",
                        sourceUrl = "https://quran.ksu.edu.sa",
                        originalSourceText = "أَمْ حَسِبْتَ أَنَّ أَصْحَابَ الْكَهْفِ وَالرَّقِيمِ كَانُوا مِنْ آيَاتِنَا عَجَبًا * إِذْ أَوَى الْفِتْيَةُ إِلَى الْكَهْفِ فَقَالُوا رَبَّنَا آتِنَا مِن لَّدُنكَ رَحْمَةً وَهَيِّئْ لَنَا مِنْ أَمْرِنَا رَشَدًا",
                        hadithGrade = "نص قرآني متواتر معتمد ومراجع حرفياً بالرسم العثماني",
                        isVerified = true
                    )
                ),
                revisions = listOf(
                    RevisionHistoryItem(
                        versionNumber = 1,
                        editedBy = "سامي عبد الرحمن",
                        editedAt = System.currentTimeMillis() - 86400000L * 7,
                        changeSummary = "النسخة النهائية للمراجعة",
                        fullTextSnapshot = "مقطع قصير يستعرض الدروس المستفادة من ثبات فتية الكهف على التوحيد."
                    )
                ),
                decision = ShariaReviewDecision(
                    primaryReviewerId = "rev_khalid",
                    primaryReviewerName = "د. خالد السعيد",
                    primaryStatus = ShariaReviewStatus.APPROVED,
                    primaryNotes = "النص القرآني مضبوط تماماً والتلاوة متقنة والفوائد منضبطة بأقوال المفسرين المعتمدين.",
                    primaryTimestamp = System.currentTimeMillis() - 86400000L * 6,
                    isDualApprovalRequired = false,
                    isDualApprovalCompleted = true
                ),
                auditLogs = listOf(
                    ShariaAuditLog(
                        itemId = "review_item_005",
                        reviewerId = "rev_khalid",
                        reviewerName = "د. خالد السعيد",
                        action = "APPROVE",
                        details = "تم اعتماد النص القرآني والمحتوى للنشر",
                        timestamp = System.currentTimeMillis() - 86400000L * 6
                    )
                ),
                submittedAt = System.currentTimeMillis() - 86400000L * 7,
                updatedAt = System.currentTimeMillis() - 86400000L * 6
            )
        )
    }
}
