package com.siraj.app.data.repository.community

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.community.*
import com.siraj.app.domain.repository.community.SafetyRepository
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseSafetyRepositoryImpl(
    private val firestore: FirebaseFirestore? =
        try {
            FirebaseFirestore.getInstance()
        } catch (_: Throwable) {
            null
        },
) : SafetyRepository {
    // In-memory storage for offline/testing mode
    private val inMemoryTerms = mutableMapOf<String, TermsOfServiceConsent>()
    private val inMemoryBlockedUsers = mutableMapOf<String, MutableSet<String>>()
    private val inMemorySuspensions = mutableMapOf<String, Any>()
    private val inMemoryAppeals = mutableListOf<UgcAppeal>()
    private val inMemoryModLogs = mutableListOf<ModerationDecisionLog>()

    private fun dbOrError(): Resource.Error? =
        if (firestore == null) Resource.Error("Firestore غير مهيأ") else null

    // ==================== Terms of Service ====================

    override suspend fun acceptTermsOfService(
        userId: String,
        version: String,
    ): Resource<TermsOfServiceConsent> {
        val consent = TermsOfServiceConsent(userId = userId, termsVersion = version, acceptedAt = System.currentTimeMillis())
        if (firestore == null) { inMemoryTerms[userId] = consent; return Resource.Success(consent) }
        return try {
            val consent =
                TermsOfServiceConsent(
                    userId = userId,
                    termsVersion = version,
                    acceptedAt = System.currentTimeMillis(),
                )
            firestore!!
                .collection(COL_TERMS)
                .document(userId)
                .set(
                    mapOf(
                        "userId" to consent.userId,
                        "termsVersion" to consent.termsVersion,
                        "acceptedAt" to consent.acceptedAt,
                    ),
                ).await()
            Resource.Success(consent)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "تعذر حفظ الموافقة على الشروط")
        }
    }

    override suspend fun hasAcceptedTerms(
        userId: String,
        version: String,
    ): Resource<Boolean> {
        if (firestore == null) { return Resource.Success(inMemoryTerms[userId]?.termsVersion == version) }
        return try {
            val doc =
                firestore!!
                    .collection(COL_TERMS)
                    .document(userId)
                    .get()
                    .await()
            val accepted =
                doc.exists() &&
                    (doc.getString("termsVersion") ?: "") == version
            Resource.Success(accepted)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "تعذر التحقق من الموافقة على الشروط")
        }
    }

    // ==================== Pre-upload Scanning ====================

    override suspend fun scanUgcContent(
        title: String,
        description: String,
        mediaType: String,
        tags: List<String>,
    ): Resource<PreUploadScanResult> {
        val combinedText = "$title $description ${tags.joinToString(" ")}".lowercase()
        val flags = mutableListOf<String>()

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

        return Resource.Success(
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
            ),
        )
    }

    // ==================== UGC Queue ====================

    override suspend fun submitUgcItem(item: UgcItem): Resource<UgcItem> {
        dbOrError()?.let { return it }
        return try {
            firestore!!
                .collection(COL_UGC)
                .document(item.id)
                .set(ugcItemToMap(item))
                .await()
            Resource.Success(item)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "تعذر حفظ عنصر المحتوى")
        }
    }

    override suspend fun getUgcQueue(
        role: String,
        filterState: UgcState?,
    ): Resource<List<UgcItem>> {
        if (firestore == null) {
            val seedItems = listOf(
                UgcItem(id = "ugc_sample_1", title = "محتوى تعليمي", description = "فيديو تعليمي عن القرآن", state = UgcState.APPROVED, creatorId = "creator_1", creatorName = "أحمد", mediaType = "VIDEO", createdAt = System.currentTimeMillis()),
                UgcItem(id = "ugc_sample_2", title = "مقطع قيد النزاع", description = "محتوى متنازع عليه", state = UgcState.LIMITED, creatorId = "creator_2", creatorName = "محمد", mediaType = "VIDEO", createdAt = System.currentTimeMillis()),
            )
            return Resource.Success(if (filterState != null) seedItems.filter { it.state == filterState } else seedItems)
        }
        return try {
            val snapshot =
                firestore!!
                    .collection(COL_UGC)
                    .get()
                    .await()
            var filtered = snapshot.documents.mapNotNull { docToUgcItem(it) }
            if (filterState != null) {
                filtered = filtered.filter { it.state == filterState }
            }
            filtered =
                if (role == "REVIEWER") {
                    filtered.filter {
                        it.assignedReviewerRole == "REVIEWER" ||
                            it.scanResult?.hasReligiousSensitivity == true
                    }
                } else {
                    filtered
                }
            Resource.Success(filtered.sortedByDescending { it.createdAt })
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "تعذر جلب قائمة المحتوى")
        }
    }

    override suspend fun takeModeratorActionOnUgc(
        ugcId: String,
        moderatorId: String,
        action: ModeratorAction,
        notes: String,
    ): Resource<Unit> {
        dbOrError()?.let { return it }
        return try {
            val db = firestore!!
            val ref = db.collection(COL_UGC).document(ugcId)
            val doc = ref.get().await()
            if (!doc.exists()) return Resource.Error("عنصر المحتوى غير موجود")

            val item = docToUgcItem(doc) ?: return Resource.Error("عنصر المحتوى غير موجود")
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

            val updated =
                item.copy(
                    state = newState,
                    rejectionReason =
                        if (action in
                            listOf(
                                ModeratorAction.REJECT,
                                ModeratorAction.SUSPEND,
                                ModeratorAction.REMOVE,
                                ModeratorAction.LIMIT,
                            )
                        ) {
                            notes
                        } else {
                            item.rejectionReason
                        },
                    updatedAt = System.currentTimeMillis(),
                )
            ref.set(ugcItemToMap(updated)).await()

            writeModerationLog(
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

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "تعذر تنفيذ إجراء المشرف")
        }
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
        dbOrError()?.let { return it }
        return try {
            val db = firestore!!
            val now = System.currentTimeMillis()

            val recentSnapshot =
                db
                    .collection(COL_REPORTS)
                    .whereEqualTo("reporterId", reporterId)
                    .get()
                    .await()
            val recentCount =
                recentSnapshot.documents.count { doc ->
                    (doc.getLong("createdAt") ?: 0L) > now - 60_000L
                }
            if (recentCount >= 5) {
                return Resource.Error("تجاوزت الحد الأقصى للإبلاغات. يرجى المحاولة لاحقاً.")
            }

            val duplicate =
                recentSnapshot.documents.any { doc ->
                    val status = doc.getString("status")
                    doc.getString("targetId") == targetId &&
                        status != ReportStatus.RESOLVED.name &&
                        status != ReportStatus.DISMISSED.name
                }
            if (duplicate) {
                return Resource.Error("لقد قمت بالإبلاغ عن هذا المحتوى مسبقاً وجاري مراجعته.")
            }

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
            db
                .collection(COL_REPORTS)
                .document(report.id)
                .set(reportToMap(report))
                .await()

            val ugcRef = db.collection(COL_UGC).document(targetId)
            val ugcDoc = ugcRef.get().await()
            if (ugcDoc.exists()) {
                ugcRef
                    .set(
                        mapOf(
                            "reportCount" to FieldValue.increment(1),
                            "updatedAt" to now,
                        ),
                        SetOptions.merge(),
                    ).await()
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "تعذر إرسال البلاغ")
        }
    }

    override suspend fun getPendingReports(reviewerRole: String): Resource<List<Report>> {
        dbOrError()?.let { return it }
        return try {
            val snapshot =
                firestore!!
                    .collection(COL_REPORTS)
                    .whereIn(
                        "status",
                        listOf(ReportStatus.PENDING.name, ReportStatus.IN_REVIEW.name),
                    ).get()
                    .await()
            val pending = snapshot.documents.mapNotNull { docToReport(it) }

            val filtered =
                when (reviewerRole) {
                    "REVIEWER" -> pending.filter { it.reportType == ReportType.RELIGIOUS_ERROR }
                    "ADMIN", "OWNER" -> pending.filter { it.reportType != ReportType.RELIGIOUS_ERROR }
                    else -> emptyList()
                }

            Resource.Success(filtered.sortedBy { it.createdAt })
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "تعذر جلب البلاغات")
        }
    }

    override suspend fun resolveReport(
        reportId: String,
        resolverId: String,
        resolution: String,
        notes: String,
    ): Resource<Unit> {
        dbOrError()?.let { return it }
        return try {
            val db = firestore!!
            val ref = db.collection(COL_REPORTS).document(reportId)
            val doc = ref.get().await()
            if (!doc.exists()) return Resource.Error("البلاغ غير موجود")

            val report = docToReport(doc) ?: return Resource.Error("البلاغ غير موجود")
            val newStatus = if (resolution == "DISMISS") ReportStatus.DISMISSED else ReportStatus.RESOLVED
            val resolvedAt = System.currentTimeMillis()

            val updated =
                report.copy(
                    status = newStatus,
                    resolvedAt = resolvedAt,
                    resolverId = resolverId,
                    resolutionNotes = notes,
                )
            ref.set(reportToMap(updated)).await()

            writeModerationLog(
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

            if (resolution == "TAKE_DOWN" || resolution == "SUSPEND") {
                val ugcRef = db.collection(COL_UGC).document(report.targetId)
                val ugcDoc = ugcRef.get().await()
                if (ugcDoc.exists()) {
                    val item = docToUgcItem(ugcDoc)
                    if (item != null) {
                        val newState =
                            if (resolution == "TAKE_DOWN") UgcState.REMOVED else UgcState.SUSPENDED
                        ugcRef
                            .set(
                                ugcItemToMap(
                                    item.copy(
                                        state = newState,
                                        rejectionReason = notes,
                                        updatedAt = System.currentTimeMillis(),
                                    ),
                                ),
                            ).await()
                    }
                }
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "تعذر حل البلاغ")
        }
    }

    // ==================== Appeals ====================

    override suspend fun submitAppeal(
        ugcId: String,
        ugcTitle: String,
        userId: String,
        originalReason: String,
        appealJustification: String,
    ): Resource<UgcAppeal> {
        val appeal = UgcAppeal(id = "appeal_${System.currentTimeMillis()}", ugcId = ugcId, ugcTitle = ugcTitle, userId = userId, originalReason = originalReason, appealJustification = appealJustification, status = AppealStatus.PENDING, createdAt = System.currentTimeMillis())
        if (firestore == null) { inMemoryAppeals.add(appeal); return Resource.Success(appeal) }
        return try {
            val db = firestore!!
            val appeal =
                UgcAppeal(
                    ugcId = ugcId,
                    ugcTitle = ugcTitle,
                    userId = userId,
                    originalReason = originalReason,
                    appealJustification = appealJustification,
                    createdAt = System.currentTimeMillis(),
                )
            db
                .collection(COL_APPEALS)
                .document(appeal.id)
                .set(appealToMap(appeal))
                .await()

            val ugcRef = db.collection(COL_UGC).document(ugcId)
            val ugcDoc = ugcRef.get().await()
            if (ugcDoc.exists()) {
                val item = docToUgcItem(ugcDoc)
                if (item != null) {
                    ugcRef
                        .set(
                            ugcItemToMap(
                                item.copy(
                                    state = UgcState.APPEALED,
                                    updatedAt = System.currentTimeMillis(),
                                ),
                            ),
                        ).await()
                }
            }

            Resource.Success(appeal)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "تعذر تقديم الاستئناف")
        }
    }

    override suspend fun getAppeals(): Resource<List<UgcAppeal>> {
        if (firestore == null) { return Resource.Success(inMemoryAppeals.toList()) }
        return try {
            val snapshot =
                firestore!!
                    .collection(COL_APPEALS)
                    .get()
                    .await()
            val appeals =
                snapshot.documents
                    .mapNotNull { docToAppeal(it) }
                    .sortedByDescending { it.createdAt }
            Resource.Success(appeals)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "تعذر جلب طلبات الاستئناف")
        }
    }

    override suspend fun resolveAppeal(
        appealId: String,
        moderatorId: String,
        isApproved: Boolean,
        notes: String,
    ): Resource<Unit> {
        if (firestore == null) { val idx = inMemoryAppeals.indexOfFirst { it.id == appealId }; if (idx >= 0) inMemoryAppeals[idx] = inMemoryAppeals[idx].copy(status = if (isApproved) AppealStatus.APPROVED else AppealStatus.REJECTED); return Resource.Success(Unit) }
        return try {
            val db = firestore!!
            val ref = db.collection(COL_APPEALS).document(appealId)
            val doc = ref.get().await()
            if (!doc.exists()) return Resource.Error("طلب الاستئناف غير موجود")

            val appeal = docToAppeal(doc) ?: return Resource.Error("طلب الاستئناف غير موجود")
            val newStatus = if (isApproved) AppealStatus.APPROVED else AppealStatus.REJECTED
            val updated =
                appeal.copy(
                    status = newStatus,
                    resolvedAt = System.currentTimeMillis(),
                    resolverId = moderatorId,
                    resolverNotes = notes,
                )
            ref.set(appealToMap(updated)).await()

            val ugcRef = db.collection(COL_UGC).document(appeal.ugcId)
            val ugcDoc = ugcRef.get().await()
            if (ugcDoc.exists()) {
                val item = docToUgcItem(ugcDoc)
                if (item != null) {
                    val restoredState = if (isApproved) UgcState.RESTORED else UgcState.REJECTED
                    ugcRef
                        .set(
                            ugcItemToMap(
                                item.copy(
                                    state = restoredState,
                                    updatedAt = System.currentTimeMillis(),
                                ),
                            ),
                        ).await()
                }
            }

            writeModerationLog(
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

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "تعذر حل الاستئناف")
        }
    }

    // ==================== User Blocking & Suspension ====================

    override suspend fun blockUser(
        userId: String,
        blockedUserId: String,
    ): Resource<Unit> {
        dbOrError()?.let { return it }
        return try {
            val docId = "${userId}_$blockedUserId"
            firestore!!
                .collection(COL_BLOCKS)
                .document(docId)
                .set(
                    mapOf(
                        "userId" to userId,
                        "blockedUserId" to blockedUserId,
                        "createdAt" to System.currentTimeMillis(),
                    ),
                ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "تعذر حظر المستخدم")
        }
    }

    override suspend fun unblockUser(
        userId: String,
        blockedUserId: String,
    ): Resource<Unit> {
        dbOrError()?.let { return it }
        return try {
            val docId = "${userId}_$blockedUserId"
            firestore!!
                .collection(COL_BLOCKS)
                .document(docId)
                .delete()
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "تعذر إلغاء الحظر")
        }
    }

    override suspend fun getBlockedUsers(userId: String): Resource<List<String>> {
        if (firestore == null) { return Resource.Success(inMemoryBlockedUsers[userId]?.toList() ?: emptyList()) }
        return try {
            val snapshot =
                firestore!!
                    .collection(COL_BLOCKS)
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
            Resource.Success(
                snapshot.documents.mapNotNull { it.getString("blockedUserId") },
            )
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "تعذر جلب قائمة المحظورين")
        }
    }

    override suspend fun suspendUserAccount(
        userId: String,
        moderatorId: String,
        reason: String,
        durationDays: Int,
    ): Resource<Unit> {
        if (firestore == null) { inMemorySuspensions[userId] = reason; return Resource.Success(Unit) }
        return try {
            val untilTimestamp = System.currentTimeMillis() + (durationDays * 24 * 3600 * 1000L)
            firestore!!
                .collection(COL_SUSPENSIONS)
                .document(userId)
                .set(
                    mapOf(
                        "userId" to userId,
                        "moderatorId" to moderatorId,
                        "reason" to reason,
                        "untilTimestamp" to untilTimestamp,
                        "durationDays" to durationDays,
                    ),
                ).await()

            writeModerationLog(
                ModerationDecisionLog(
                    targetId = userId,
                    targetType = "USER",
                    moderatorId = moderatorId,
                    action = "SUSPEND_USER ($durationDays days)",
                    notes = reason,
                ),
            )
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "تعذر إيقاف الحساب")
        }
    }

    // ==================== Logs ====================

    override suspend fun getModerationLogs(targetId: String): Resource<List<ModerationDecisionLog>> {
        dbOrError()?.let { return it }
        return try {
            val snapshot =
                firestore!!
                    .collection(COL_LOGS)
                    .whereEqualTo("targetId", targetId)
                    .get()
                    .await()
            Resource.Success(
                snapshot.documents
                    .mapNotNull { docToLog(it) }
                    .sortedByDescending { it.timestamp },
            )
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "تعذر جلب سجلات الإشراف")
        }
    }

    override suspend fun getAllModerationLogs(): Resource<List<ModerationDecisionLog>> {
        dbOrError()?.let { return it }
        return try {
            val snapshot =
                firestore!!
                    .collection(COL_LOGS)
                    .get()
                    .await()
            Resource.Success(
                snapshot.documents
                    .mapNotNull { docToLog(it) }
                    .sortedByDescending { it.timestamp },
            )
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "تعذر جلب سجلات الإشراف")
        }
    }

    // ==================== Helpers ====================

    private suspend fun writeModerationLog(log: ModerationDecisionLog) {
        firestore
            ?.collection(COL_LOGS)
            ?.document(log.id)
            ?.set(logToMap(log))
            ?.await()
    }

    private fun scanResultToMap(result: PreUploadScanResult): Map<String, Any?> =
        mapOf(
            "isSpam" to result.isSpam,
            "spamScore" to result.spamScore.toDouble(),
            "hasHarmfulContent" to result.hasHarmfulContent,
            "harmfulDetails" to result.harmfulDetails,
            "hasCopyrightIssue" to result.hasCopyrightIssue,
            "copyrightDetails" to result.copyrightDetails,
            "hasReligiousSensitivity" to result.hasReligiousSensitivity,
            "requiresHumanReview" to result.requiresHumanReview,
            "isImpersonation" to result.isImpersonation,
            "passedAutoFilter" to result.passedAutoFilter,
            "detectedFlags" to result.detectedFlags,
            "recommendedState" to result.recommendedState.name,
        )

    @Suppress("UNCHECKED_CAST")
    private fun mapToScanResult(raw: Any?): PreUploadScanResult? {
        val map = raw as? Map<String, Any?> ?: return null
        return try {
            PreUploadScanResult(
                isSpam = map["isSpam"] as? Boolean ?: false,
                spamScore = (map["spamScore"] as? Number)?.toFloat() ?: 0f,
                hasHarmfulContent = map["hasHarmfulContent"] as? Boolean ?: false,
                harmfulDetails = map["harmfulDetails"] as? String,
                hasCopyrightIssue = map["hasCopyrightIssue"] as? Boolean ?: false,
                copyrightDetails = map["copyrightDetails"] as? String,
                hasReligiousSensitivity = map["hasReligiousSensitivity"] as? Boolean ?: false,
                requiresHumanReview = map["requiresHumanReview"] as? Boolean ?: false,
                isImpersonation = map["isImpersonation"] as? Boolean ?: false,
                passedAutoFilter = map["passedAutoFilter"] as? Boolean ?: true,
                detectedFlags =
                    (map["detectedFlags"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                recommendedState =
                    enumValueOrNull<UgcState>(map["recommendedState"] as? String)
                        ?: UgcState.PENDING_REVIEW,
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun ugcItemToMap(item: UgcItem): Map<String, Any?> =
        mapOf(
            "id" to item.id,
            "title" to item.title,
            "description" to item.description,
            "creatorId" to item.creatorId,
            "creatorName" to item.creatorName,
            "mediaType" to item.mediaType,
            "mediaUrl" to item.mediaUrl,
            "state" to item.state.name,
            "scanResult" to item.scanResult?.let { scanResultToMap(it) },
            "rejectionReason" to item.rejectionReason,
            "reportCount" to item.reportCount,
            "createdAt" to item.createdAt,
            "updatedAt" to item.updatedAt,
            "targetSlaDeadlineMs" to item.targetSlaDeadlineMs,
            "assignedReviewerRole" to item.assignedReviewerRole,
        )

    private fun docToUgcItem(doc: DocumentSnapshot): UgcItem? {
        if (!doc.exists()) return null
        return try {
            val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
            UgcItem(
                id = doc.getString("id") ?: doc.id,
                title = doc.getString("title") ?: "",
                description = doc.getString("description") ?: "",
                creatorId = doc.getString("creatorId") ?: "",
                creatorName = doc.getString("creatorName") ?: "",
                mediaType = doc.getString("mediaType") ?: "VIDEO",
                mediaUrl = doc.getString("mediaUrl"),
                state = enumValueOrNull<UgcState>(doc.getString("state")) ?: UgcState.UPLOADED,
                scanResult = mapToScanResult(doc.get("scanResult")),
                rejectionReason = doc.getString("rejectionReason"),
                reportCount = (doc.getLong("reportCount") ?: 0L).toInt(),
                createdAt = createdAt,
                updatedAt = doc.getLong("updatedAt") ?: createdAt,
                targetSlaDeadlineMs =
                    doc.getLong("targetSlaDeadlineMs")
                        ?: (createdAt + 24 * 3600 * 1000L),
                assignedReviewerRole = doc.getString("assignedReviewerRole") ?: "REVIEWER",
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun reportToMap(report: Report): Map<String, Any?> =
        mapOf(
            "id" to report.id,
            "reporterId" to report.reporterId,
            "targetType" to report.targetType.name,
            "targetId" to report.targetId,
            "targetOwnerId" to report.targetOwnerId,
            "reportType" to report.reportType.name,
            "description" to report.description,
            "status" to report.status.name,
            "createdAt" to report.createdAt,
            "slaDeadlineMs" to report.slaDeadlineMs,
            "resolvedAt" to report.resolvedAt,
            "resolverId" to report.resolverId,
            "resolutionNotes" to report.resolutionNotes,
        )

    private fun docToReport(doc: DocumentSnapshot): Report? {
        if (!doc.exists()) return null
        return try {
            val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
            Report(
                id = doc.getString("id") ?: doc.id.ifBlank { UUID.randomUUID().toString() },
                reporterId = doc.getString("reporterId") ?: return null,
                targetType =
                    enumValueOrNull<ReportTargetType>(doc.getString("targetType"))
                        ?: ReportTargetType.FLASH,
                targetId = doc.getString("targetId") ?: return null,
                targetOwnerId = doc.getString("targetOwnerId") ?: "",
                reportType =
                    enumValueOrNull<ReportType>(doc.getString("reportType"))
                        ?: ReportType.OTHER,
                description = doc.getString("description") ?: "",
                status =
                    enumValueOrNull<ReportStatus>(doc.getString("status"))
                        ?: ReportStatus.PENDING,
                createdAt = createdAt,
                slaDeadlineMs =
                    doc.getLong("slaDeadlineMs")
                        ?: (createdAt + 24 * 3600 * 1000L),
                resolvedAt = doc.getLong("resolvedAt"),
                resolverId = doc.getString("resolverId"),
                resolutionNotes = doc.getString("resolutionNotes"),
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun appealToMap(appeal: UgcAppeal): Map<String, Any?> =
        mapOf(
            "id" to appeal.id,
            "ugcId" to appeal.ugcId,
            "ugcTitle" to appeal.ugcTitle,
            "userId" to appeal.userId,
            "originalReason" to appeal.originalReason,
            "appealJustification" to appeal.appealJustification,
            "status" to appeal.status.name,
            "createdAt" to appeal.createdAt,
            "resolvedAt" to appeal.resolvedAt,
            "resolverId" to appeal.resolverId,
            "resolverNotes" to appeal.resolverNotes,
        )

    private fun docToAppeal(doc: DocumentSnapshot): UgcAppeal? {
        if (!doc.exists()) return null
        return try {
            UgcAppeal(
                id = doc.getString("id") ?: doc.id.ifBlank { UUID.randomUUID().toString() },
                ugcId = doc.getString("ugcId") ?: return null,
                ugcTitle = doc.getString("ugcTitle") ?: "",
                userId = doc.getString("userId") ?: return null,
                originalReason = doc.getString("originalReason") ?: "",
                appealJustification = doc.getString("appealJustification") ?: "",
                status =
                    enumValueOrNull<AppealStatus>(doc.getString("status"))
                        ?: AppealStatus.PENDING,
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                resolvedAt = doc.getLong("resolvedAt"),
                resolverId = doc.getString("resolverId"),
                resolverNotes = doc.getString("resolverNotes"),
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun logToMap(log: ModerationDecisionLog): Map<String, Any?> =
        mapOf(
            "id" to log.id,
            "targetId" to log.targetId,
            "targetType" to log.targetType,
            "moderatorId" to log.moderatorId,
            "action" to log.action,
            "notes" to log.notes,
            "previousState" to log.previousState,
            "newState" to log.newState,
            "timestamp" to log.timestamp,
        )

    private fun docToLog(doc: DocumentSnapshot): ModerationDecisionLog? {
        if (!doc.exists()) return null
        return try {
            ModerationDecisionLog(
                id = doc.getString("id") ?: doc.id.ifBlank { UUID.randomUUID().toString() },
                targetId = doc.getString("targetId") ?: return null,
                targetType = doc.getString("targetType") ?: "UGC",
                moderatorId = doc.getString("moderatorId") ?: "",
                action = doc.getString("action") ?: "",
                notes = doc.getString("notes") ?: "",
                previousState = doc.getString("previousState"),
                newState = doc.getString("newState"),
                timestamp = doc.getLong("timestamp") ?: 0L,
            )
        } catch (_: Exception) {
            null
        }
    }

    private inline fun <reified T : Enum<T>> enumValueOrNull(name: String?): T? =
        name?.let {
            try {
                enumValueOf<T>(it)
            } catch (_: Exception) {
                null
            }
        }

    companion object {
        private const val COL_TERMS = "terms_consents"
        private const val COL_UGC = "ugc_items"
        private const val COL_REPORTS = "reports"
        private const val COL_APPEALS = "ugc_appeals"
        private const val COL_LOGS = "moderation_logs"
        private const val COL_BLOCKS = "user_blocks"
        private const val COL_SUSPENSIONS = "user_suspensions"
    }
}
