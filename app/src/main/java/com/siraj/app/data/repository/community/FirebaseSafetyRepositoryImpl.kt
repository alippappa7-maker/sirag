package com.siraj.app.data.repository.community

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.community.*
import com.siraj.app.domain.repository.community.SafetyRepository
import kotlinx.coroutines.delay

class FirebaseSafetyRepositoryImpl : SafetyRepository {
    private val termsConsents = mutableMapOf<String, TermsOfServiceConsent>()
    private val reports = mutableListOf<Report>()
    private val moderationLogs = mutableListOf<ModerationDecisionLog>()
    private val reportTimestamps = mutableMapOf<String, MutableList<Long>>() // userId -> list of report timestamps
    private val blockedUsers = mutableMapOf<String, MutableSet<String>>() // userId -> set of blockedUserIds
    private val suspendedUsers = mutableMapOf<String, Long>() // userId -> suspensionUntilTimestamp
    private val ugcItems = mutableListOf<UgcItem>()
    private val appeals = mutableListOf<UgcAppeal>()

    init {
        // Populate sample initial mock UGC items, reports, and appeals for development & demonstration
        val sampleUgc1 =
            UgcItem(
                id = "ugc_1",
                title = "قصة صبر النبي أيوب عليه السلام",
                description = "مقطع مؤثر عن فضيلة الصبر والاحتساب من القرآن الكريم",
                creatorId = "user_ahmed",
                creatorName = "أحمد المنصور",
                mediaType = "VIDEO",
                state = UgcState.APPROVED,
                scanResult =
                    PreUploadScanResult(
                        isSpam = false,
                        hasHarmfulContent = false,
                        hasCopyrightIssue = false,
                        hasReligiousSensitivity = true,
                        requiresHumanReview = true,
                        passedAutoFilter = true,
                        recommendedState = UgcState.APPROVED,
                    ),
                assignedReviewerRole = "REVIEWER",
            )
        val sampleUgc2 =
            UgcItem(
                id = "ugc_2",
                title = "حمل التطبيق واربح آلاف الدولارات فوراً",
                description = "اضغط هنا للدخول على الرابط وربح فوري بدون مجهود",
                creatorId = "spammer_99",
                creatorName = "مجهول التسويق",
                mediaType = "VIDEO",
                state = UgcState.LIMITED,
                scanResult =
                    PreUploadScanResult(
                        isSpam = true,
                        spamScore = 0.95f,
                        hasHarmfulContent = false,
                        passedAutoFilter = false,
                        detectedFlags = listOf("رابط احتيالي", "كلمات ترويجية مكررة"),
                        recommendedState = UgcState.LIMITED,
                    ),
                rejectionReason = "تم تقييد الظهور لاحتوائه على روابط دعائية مشبوهة (Spam)",
                assignedReviewerRole = "ADMIN",
            )
        val sampleUgc3 =
            UgcItem(
                id = "ugc_3",
                title = "تفسير آية الكرسي برأي غير معتمد",
                description = "تأويلات شخصية غير منسوبة لمصادر التفسير المعتمدة",
                creatorId = "user_creator_2",
                creatorName = "بسام التائب",
                mediaType = "VIDEO",
                state = UgcState.PENDING_REVIEW,
                scanResult =
                    PreUploadScanResult(
                        hasReligiousSensitivity = true,
                        requiresHumanReview = true,
                        passedAutoFilter = true,
                        detectedFlags = listOf("محتوى تفسيري يتطلب توثيق المصدر"),
                        recommendedState = UgcState.PENDING_REVIEW,
                    ),
                assignedReviewerRole = "REVIEWER",
            )
        ugcItems.addAll(listOf(sampleUgc1, sampleUgc2, sampleUgc3))

        // Initial Reports
        val sampleReport1 =
            Report(
                id = "rep_101",
                reporterId = "user_safe_1",
                targetType = ReportTargetType.FLASH,
                targetId = "ugc_2",
                targetOwnerId = "spammer_99",
                reportType = ReportType.SPAM,
                description = "فيديو إعلاني مزعج يروّج لروابط خارجية وهمية",
                status = ReportStatus.PENDING,
                createdAt = System.currentTimeMillis() - (2 * 3600 * 1000L), // 2 hours ago
            )
        val sampleReport2 =
            Report(
                id = "rep_102",
                reporterId = "user_safe_2",
                targetType = ReportTargetType.FLASH,
                targetId = "ugc_3",
                targetOwnerId = "user_creator_2",
                reportType = ReportType.RELIGIOUS_ERROR,
                description = "ذكر حديث دون ذكر راويه ويحتوي ألفاظاً غير صحيحة",
                status = ReportStatus.PENDING,
                createdAt = System.currentTimeMillis() - (18 * 3600 * 1000L), // 18 hours ago
            )
        reports.addAll(listOf(sampleReport1, sampleReport2))

        // Initial Appeal
        val sampleAppeal =
            UgcAppeal(
                id = "appeal_1",
                ugcId = "ugc_2",
                ugcTitle = "حمل التطبيق واربح آلاف الدولارات فوراً",
                userId = "spammer_99",
                originalReason = "تم تقييد الظهور لاحتوائه على روابط دعائية مشبوهة (Spam)",
                appealJustification = "قمت بحذف الروابط وأرجو إعادة فحص الفيديو واعتماده",
                status = AppealStatus.PENDING,
                createdAt = System.currentTimeMillis() - (5 * 3600 * 1000L),
            )
        appeals.add(sampleAppeal)
    }

    // ==================== Terms of Service ====================

    override suspend fun acceptTermsOfService(
        userId: String,
        version: String,
    ): Resource<TermsOfServiceConsent> {
        delay(200)
        val consent =
            TermsOfServiceConsent(
                userId = userId,
                termsVersion = version,
                acceptedAt = System.currentTimeMillis(),
            )
        termsConsents[userId] = consent
        return Resource.Success(consent)
    }

    override suspend fun hasAcceptedTerms(
        userId: String,
        version: String,
    ): Resource<Boolean> {
        delay(100)
        val consent = termsConsents[userId]
        return Resource.Success(consent != null && consent.termsVersion == version)
    }

    // ==================== Pre-upload Scanning ====================

    override suspend fun scanUgcContent(
        title: String,
        description: String,
        mediaType: String,
        tags: List<String>,
    ): Resource<PreUploadScanResult> {
        delay(400)
        val combinedText = "$title $description ${tags.joinToString(" ")}".lowercase()
        val flags = mutableListOf<String>()

        // 1. Spam Detection Heuristic
        val spamKeywords =
            listOf(
                "ربح سريع",
                "ربح",
                "دولار",
                "دولارات",
                "ثراء فاحش",
                "احصل على مجانا",
                "اضغط هنا",
                "تداول مضمون",
                "cash",
                "crypto",
                "free money",
            )
        var spamScore = 0.05f
        for (kw in spamKeywords) {
            if (combinedText.contains(kw)) {
                spamScore += 0.45f
                flags.add("مؤشر سبام: $kw")
            }
        }
        val isSpam = spamScore >= 0.5f

        // 2. Harmful / Harassment / Hate Detection Heuristic
        val harmfulKeywords = listOf("كافر", "مرتد", "دمار", "قتل", "تحريض", "كراهية", "إساءة", "شتيمة")
        var hasHarmful = false
        var harmfulReason: String? = null
        for (kw in harmfulKeywords) {
            if (combinedText.contains(kw)) {
                hasHarmful = true
                harmfulReason = "تم اكتشاف ألفاظ تحريضية أو مسيئة: $kw"
                flags.add("محتوى محظور: $kw")
                break
            }
        }

        // 3. Copyright / Unauthorized Re-upload Heuristic
        val copyrightKeywords = listOf("حقوق محفوظة", "mbc", "bein", "rotana", "تلفزيون", "مسلسل كامل")
        var hasCopyright = false
        var copyrightDetails: String? = null
        for (kw in copyrightKeywords) {
            if (combinedText.contains(kw)) {
                hasCopyright = true
                copyrightDetails = "اشتباه في إعادة نشر مادة تلفزيونية أو إعلامية محمية: $kw"
                flags.add("اشتباه حقوق نشر: $kw")
                break
            }
        }

        // 4. Religious / Sharia Sensitivity
        val religiousKeywords = listOf("تفسير", "فتوى", "حديث", "حكم شرعي", "قال الله", "رواه", "فقه", "سورة", "آية")
        val hasReligiousSensitivity = religiousKeywords.any { combinedText.contains(it) }
        if (hasReligiousSensitivity) {
            flags.add("يتضمن محتوى شرعياً يتطلب مراجعة موثقة")
        }

        val passedAutoFilter = !hasHarmful && !isSpam && !hasCopyright
        val recommendedState =
            when {
                hasHarmful -> UgcState.REJECTED
                isSpam -> UgcState.LIMITED
                hasCopyright -> UgcState.LIMITED
                hasReligiousSensitivity -> UgcState.PENDING_REVIEW
                else -> UgcState.APPROVED
            }

        val result =
            PreUploadScanResult(
                isSpam = isSpam,
                spamScore = spamScore.coerceAtMost(1.0f),
                hasHarmfulContent = hasHarmful,
                harmfulDetails = harmfulReason,
                hasCopyrightIssue = hasCopyright,
                copyrightDetails = copyrightDetails,
                hasReligiousSensitivity = hasReligiousSensitivity,
                requiresHumanReview = hasReligiousSensitivity || hasCopyright || isSpam,
                passedAutoFilter = passedAutoFilter,
                detectedFlags = flags,
                recommendedState = recommendedState,
            )
        return Resource.Success(result)
    }

    // ==================== UGC Queue ====================

    override suspend fun submitUgcItem(item: UgcItem): Resource<UgcItem> {
        delay(300)
        ugcItems.removeAll { it.id == item.id }
        ugcItems.add(item)
        return Resource.Success(item)
    }

    override suspend fun getUgcQueue(
        role: String,
        filterState: UgcState?,
    ): Resource<List<UgcItem>> {
        delay(200)
        var filtered = ugcItems.toList()
        if (filterState != null) {
            filtered = filtered.filter { it.state == filterState }
        }

        // Role based routing
        filtered =
            if (role == "REVIEWER") {
                filtered.filter { it.assignedReviewerRole == "REVIEWER" || it.scanResult?.hasReligiousSensitivity == true }
            } else {
                filtered // Admin/Owner see everything
            }
        return Resource.Success(filtered.sortedByDescending { it.createdAt })
    }

    override suspend fun takeModeratorActionOnUgc(
        ugcId: String,
        moderatorId: String,
        action: ModeratorAction,
        notes: String,
    ): Resource<Unit> {
        delay(300)
        val index = ugcItems.indexOfFirst { it.id == ugcId }
        if (index == -1) return Resource.Error("عنصر المحتوى غير موجود")

        val item = ugcItems[index]
        val previousState = item.state.name

        val newState =
            when (action) {
                ModeratorAction.APPROVE -> UgcState.APPROVED
                ModeratorAction.LIMIT -> UgcState.LIMITED
                ModeratorAction.REJECT -> UgcState.REJECTED
                ModeratorAction.SUSPEND -> UgcState.SUSPENDED
                ModeratorAction.REMOVE -> UgcState.REMOVED
                ModeratorAction.RESTORE -> UgcState.RESTORED
                ModeratorAction.WARN_USER -> item.state
                ModeratorAction.SUSPEND_USER -> UgcState.SUSPENDED
                ModeratorAction.DISMISS_REPORT -> item.state
            }

        ugcItems[index] =
            item.copy(
                state = newState,
                rejectionReason =
                    if (action in
                        listOf(ModeratorAction.REJECT, ModeratorAction.SUSPEND, ModeratorAction.REMOVE, ModeratorAction.LIMIT)
                    ) {
                        notes
                    } else {
                        item.rejectionReason
                    },
                updatedAt = System.currentTimeMillis(),
            )

        moderationLogs.add(
            ModerationDecisionLog(
                targetId = ugcId,
                targetType = "UGC",
                moderatorId = moderatorId,
                action = action.name,
                notes = notes,
                previousState = previousState,
                newState = newState.name,
            ),
        )
        return Resource.Success(Unit)
    }

    // ==================== Reporting ====================

    override suspend fun submitReport(
        reporterId: String,
        targetType: ReportTargetType,
        targetId: String,
        targetOwnerId: String,
        reportType: ReportType,
        description: String,
    ): Resource<Unit> {
        delay(400)

        // Rate Limiting: Max 5 reports per minute per user
        val now = System.currentTimeMillis()
        val userTimestamps = reportTimestamps.getOrPut(reporterId) { mutableListOf() }
        userTimestamps.removeAll { now - it > 60000 }
        if (userTimestamps.size >= 5) {
            return Resource.Error("تجاوزت الحد الأقصى للإبلاغات. يرجى المحاولة لاحقاً.")
        }

        // Prevent Duplicate Reports for the same target by the same user
        val duplicate =
            reports.find {
                it.reporterId == reporterId &&
                    it.targetId == targetId &&
                    it.status != ReportStatus.RESOLVED &&
                    it.status != ReportStatus.DISMISSED
            }
        if (duplicate != null) {
            return Resource.Error("لقد قمت بالإبلاغ عن هذا المحتوى مسبقاً وجاري مراجعته.")
        }

        userTimestamps.add(now)

        val report =
            Report(
                reporterId = reporterId,
                targetType = targetType,
                targetId = targetId,
                targetOwnerId = targetOwnerId,
                reportType = reportType,
                description = description,
                createdAt = now,
            )
        reports.add(report)

        // Increment UGC report count if matching
        val ugcIndex = ugcItems.indexOfFirst { it.id == targetId }
        if (ugcIndex != -1) {
            val ugc = ugcItems[ugcIndex]
            ugcItems[ugcIndex] = ugc.copy(reportCount = ugc.reportCount + 1)
        }

        return Resource.Success(Unit)
    }

    override suspend fun getPendingReports(reviewerRole: String): Resource<List<Report>> {
        delay(300)
        val pending = reports.filter { it.status == ReportStatus.PENDING || it.status == ReportStatus.IN_REVIEW }

        val filtered =
            if (reviewerRole == "REVIEWER") {
                pending.filter { it.reportType == ReportType.RELIGIOUS_ERROR }
            } else if (reviewerRole == "ADMIN" || reviewerRole == "OWNER") {
                pending.filter { it.reportType != ReportType.RELIGIOUS_ERROR }
            } else {
                emptyList()
            }

        return Resource.Success(filtered.sortedBy { it.createdAt })
    }

    override suspend fun resolveReport(
        reportId: String,
        resolverId: String,
        resolution: String,
        notes: String,
    ): Resource<Unit> {
        delay(300)
        val index = reports.indexOfFirst { it.id == reportId }
        if (index == -1) return Resource.Error("البلاغ غير موجود")

        val report = reports[index]
        val newStatus = if (resolution == "DISMISS") ReportStatus.DISMISSED else ReportStatus.RESOLVED

        reports[index] =
            report.copy(
                status = newStatus,
                resolvedAt = System.currentTimeMillis(),
                resolverId = resolverId,
                resolutionNotes = notes,
            )

        moderationLogs.add(
            ModerationDecisionLog(
                targetId = reportId,
                targetType = "REPORT",
                moderatorId = resolverId,
                action = resolution,
                notes = notes,
                previousState = report.status.name,
                newState = newStatus.name,
            ),
        )

        // If resolution is TAKE_DOWN or SUSPEND, also update the target UGC state
        if (resolution == "TAKE_DOWN" || resolution == "SUSPEND") {
            val ugcIndex = ugcItems.indexOfFirst { it.id == report.targetId }
            if (ugcIndex != -1) {
                ugcItems[ugcIndex] =
                    ugcItems[ugcIndex].copy(
                        state = if (resolution == "TAKE_DOWN") UgcState.REMOVED else UgcState.SUSPENDED,
                        rejectionReason = notes,
                    )
            }
        }

        return Resource.Success(Unit)
    }

    // ==================== Appeals ====================

    override suspend fun submitAppeal(
        ugcId: String,
        ugcTitle: String,
        userId: String,
        originalReason: String,
        appealJustification: String,
    ): Resource<UgcAppeal> {
        delay(300)
        val appeal =
            UgcAppeal(
                ugcId = ugcId,
                ugcTitle = ugcTitle,
                userId = userId,
                originalReason = originalReason,
                appealJustification = appealJustification,
                createdAt = System.currentTimeMillis(),
            )
        appeals.add(appeal)

        // Mark UGC item as APPEALED
        val ugcIndex = ugcItems.indexOfFirst { it.id == ugcId }
        if (ugcIndex != -1) {
            ugcItems[ugcIndex] = ugcItems[ugcIndex].copy(state = UgcState.APPEALED)
        }

        return Resource.Success(appeal)
    }

    override suspend fun getAppeals(): Resource<List<UgcAppeal>> {
        delay(200)
        return Resource.Success(appeals.sortedByDescending { it.createdAt })
    }

    override suspend fun resolveAppeal(
        appealId: String,
        moderatorId: String,
        isApproved: Boolean,
        notes: String,
    ): Resource<Unit> {
        delay(300)
        val index = appeals.indexOfFirst { it.id == appealId }
        if (index == -1) return Resource.Error("طلب الاستئناف غير موجود")

        val appeal = appeals[index]
        val newStatus = if (isApproved) AppealStatus.APPROVED else AppealStatus.REJECTED

        appeals[index] =
            appeal.copy(
                status = newStatus,
                resolvedAt = System.currentTimeMillis(),
                resolverId = moderatorId,
                resolverNotes = notes,
            )

        // Update corresponding UGC
        val ugcIndex = ugcItems.indexOfFirst { it.id == appeal.ugcId }
        if (ugcIndex != -1) {
            val restoredState = if (isApproved) UgcState.RESTORED else UgcState.REJECTED
            ugcItems[ugcIndex] = ugcItems[ugcIndex].copy(state = restoredState)
        }

        moderationLogs.add(
            ModerationDecisionLog(
                targetId = appealId,
                targetType = "APPEAL",
                moderatorId = moderatorId,
                action = if (isApproved) "APPEAL_APPROVED" else "APPEAL_REJECTED",
                notes = notes,
                previousState = appeal.status.name,
                newState = newStatus.name,
            ),
        )

        return Resource.Success(Unit)
    }

    // ==================== User Blocking & Suspension ====================

    override suspend fun blockUser(
        userId: String,
        blockedUserId: String,
    ): Resource<Unit> {
        delay(200)
        val set = blockedUsers.getOrPut(userId) { mutableSetOf() }
        set.add(blockedUserId)
        return Resource.Success(Unit)
    }

    override suspend fun unblockUser(
        userId: String,
        blockedUserId: String,
    ): Resource<Unit> {
        delay(200)
        blockedUsers[userId]?.remove(blockedUserId)
        return Resource.Success(Unit)
    }

    override suspend fun getBlockedUsers(userId: String): Resource<List<String>> {
        delay(100)
        return Resource.Success(blockedUsers[userId]?.toList() ?: emptyList())
    }

    override suspend fun suspendUserAccount(
        userId: String,
        moderatorId: String,
        reason: String,
        durationDays: Int,
    ): Resource<Unit> {
        delay(300)
        val suspensionUntil = System.currentTimeMillis() + (durationDays * 24 * 3600 * 1000L)
        suspendedUsers[userId] = suspensionUntil

        moderationLogs.add(
            ModerationDecisionLog(
                targetId = userId,
                targetType = "USER",
                moderatorId = moderatorId,
                action = "SUSPEND_USER ($durationDays days)",
                notes = reason,
            ),
        )
        return Resource.Success(Unit)
    }

    // ==================== Logs ====================

    override suspend fun getModerationLogs(targetId: String): Resource<List<ModerationDecisionLog>> {
        delay(150)
        return Resource.Success(moderationLogs.filter { it.targetId == targetId })
    }

    override suspend fun getAllModerationLogs(): Resource<List<ModerationDecisionLog>> {
        delay(150)
        return Resource.Success(moderationLogs.sortedByDescending { it.timestamp })
    }
}
