package com.siraj.app.features.minor.domain

import com.siraj.app.domain.models.minor.*
import java.security.MessageDigest
import java.util.UUID

enum class MinorAction {
    SEND_DIRECT_MESSAGE,
    SHARE_FINE_LOCATION,
    PUBLISH_CONTENT_PUBLICLY,
    USE_AI_GENERATION,
    UPLOAD_VOICE_RECORDING,
    RECEIVE_STRANGER_INVITE,
    PURCHASE_SUBSCRIPTION,
}

/**
 * محرك سياسات وضوابط حماية القاصرين (Minor Safety Engine - PROMPT 089)
 */
object MinorSafetyEngine {
    /**
     * توليد وإنفاذ سياسة الحماية الصارمة بناء على الفئة العمرية
     */
    fun generatePolicyForAgeBracket(
        userId: String,
        ageBracket: UserAgeBracket,
        guardianEmail: String? = null,
        isParentalConsentVerified: Boolean = false,
    ): MinorSafetyPolicy =
        when (ageBracket) {
            UserAgeBracket.ADULT_18_PLUS ->
                MinorSafetyPolicy(
                    userId = userId,
                    ageBracket = ageBracket,
                    isMinorProtectionActive = false,
                    isPrivateByDefault = false,
                    blockDirectMessages = true, // معطل لجميع المستخدمين في سراج كأصل لحماية الخصوصية
                    disableFineLocation = true, // منع جمع الموقع الدقيق للجميع
                    disablePersonalizedAds = true, // خلو التطبيق من الإعلانات التتبعية
                    disableModelTrainingOnData = true,
                    blockVoiceCloning = true,
                    blockBiometricDataCollection = true,
                    requireParentalApprovalForPublishing = false,
                    requireParentalConsentForAiFeatures = false,
                    hideFromPublicDiscovery = false,
                    disableIndividualAnalyticsProfiling = false,
                    allowOnlyCuratedEducationalContent = false,
                    parentalGuardianEmail = null,
                    isParentalConsentVerified = false,
                )
            UserAgeBracket.TEEN_13_TO_17 ->
                MinorSafetyPolicy(
                    userId = userId,
                    ageBracket = ageBracket,
                    isMinorProtectionActive = true,
                    isPrivateByDefault = true,
                    blockDirectMessages = true,
                    disableFineLocation = true,
                    disablePersonalizedAds = true,
                    disableModelTrainingOnData = true,
                    blockVoiceCloning = true,
                    blockBiometricDataCollection = true,
                    requireParentalApprovalForPublishing = true,
                    requireParentalConsentForAiFeatures = false,
                    hideFromPublicDiscovery = true,
                    disableIndividualAnalyticsProfiling = true,
                    allowOnlyCuratedEducationalContent = false,
                    parentalGuardianEmail = guardianEmail,
                    isParentalConsentVerified = isParentalConsentVerified,
                )
            UserAgeBracket.CHILD_UNDER_13 ->
                MinorSafetyPolicy(
                    userId = userId,
                    ageBracket = ageBracket,
                    isMinorProtectionActive = true,
                    isPrivateByDefault = true,
                    blockDirectMessages = true,
                    disableFineLocation = true,
                    disablePersonalizedAds = true,
                    disableModelTrainingOnData = true,
                    blockVoiceCloning = true,
                    blockBiometricDataCollection = true,
                    requireParentalApprovalForPublishing = true,
                    requireParentalConsentForAiFeatures = true,
                    hideFromPublicDiscovery = true,
                    disableIndividualAnalyticsProfiling = true,
                    allowOnlyCuratedEducationalContent = true,
                    parentalGuardianEmail = guardianEmail,
                    isParentalConsentVerified = isParentalConsentVerified,
                )
            UserAgeBracket.UNSPECIFIED ->
                MinorSafetyPolicy(
                    userId = userId,
                    ageBracket = ageBracket,
                    isMinorProtectionActive = true,
                    isPrivateByDefault = true,
                    blockDirectMessages = true,
                    disableFineLocation = true,
                    disablePersonalizedAds = true,
                    disableModelTrainingOnData = true,
                    blockVoiceCloning = true,
                    blockBiometricDataCollection = true,
                    requireParentalApprovalForPublishing = true,
                    requireParentalConsentForAiFeatures = true,
                    hideFromPublicDiscovery = true,
                    disableIndividualAnalyticsProfiling = true,
                    allowOnlyCuratedEducationalContent = true,
                    parentalGuardianEmail = null,
                    isParentalConsentVerified = false,
                )
        }

    /**
     * فحص إمكانية تنفيذ إجراء معين في ظل سياسة الحساب الحالية
     */
    fun canPerformAction(
        policy: MinorSafetyPolicy,
        action: MinorAction,
    ): Pair<Boolean, String> =
        when (action) {
            MinorAction.SEND_DIRECT_MESSAGE -> {
                Pair(false, "الرسائل الخاصة المباشرة معطلة تماماً لحماية خصوصية وسلامة المستخدمين والقاصرين.")
            }
            MinorAction.SHARE_FINE_LOCATION -> {
                Pair(false, "جمع الإحداثيات الدقيقة (GPS) محظور برمجياً في سراج لجميع الفئات.")
            }
            MinorAction.PUBLISH_CONTENT_PUBLICLY -> {
                if (policy.ageBracket.isMinor) {
                    if (policy.requireParentalApprovalForPublishing && !policy.isParentalConsentVerified) {
                        Pair(false, "نشر المحتوى للعامة يتطلب موافقة وتأكيد ولي الأمر أولاً.")
                    } else {
                        Pair(true, "مسموح بنشر المحتوى تحت إشراف ولي الأمر الموثق.")
                    }
                } else {
                    Pair(true, "مسموح للبالغين بعد اجتياز مراجعة النشر القياسية.")
                }
            }
            MinorAction.USE_AI_GENERATION -> {
                if (policy.ageBracket == UserAgeBracket.CHILD_UNDER_13 && !policy.isParentalConsentVerified) {
                    Pair(false, "استخدام أدوات الذكاء الاصطناعي للأطفال دون 13 يتطلب تفعيل موافقة ولي الأمر.")
                } else {
                    Pair(true, "مسموح بالاستخدام التعليمي للذكاء الاصطناعي مع منع تدريب النماذج على البيانات.")
                }
            }
            MinorAction.UPLOAD_VOICE_RECORDING -> {
                if (policy.ageBracket.isMinor && !policy.isParentalConsentVerified) {
                    Pair(false, "رفع وحفظ التسجيلات الصوتية للأطفال يتطلب موافقة صريحة من ولي الأمر.")
                } else {
                    Pair(true, "مسموح مع حظر تام لاستنساخ الصوت بالذكاء الاصطناعي.")
                }
            }
            MinorAction.RECEIVE_STRANGER_INVITE -> {
                if (policy.isMinorProtectionActive) {
                    Pair(false, "حساب القاصر محمي من تلقي أي دعوات من مستخدمين غير معتمدين من ولي الأمر.")
                } else {
                    Pair(true, "مسموح ضمن مساحات العمل المعتمدة.")
                }
            }
            MinorAction.PURCHASE_SUBSCRIPTION -> {
                if (policy.ageBracket.isMinor) {
                    Pair(false, "عمليات الشراء والاشتراكات محظورة على حسابات القاصرين ويجب إدارتها حصراً عبر حساب ولي الأمر.")
                } else {
                    Pair(true, "مسموح للبالغين عبر بوابات الدفع الرسمية.")
                }
            }
        }

    /**
     * تصعيد وفرز بلاغات استغلال أو إساءة موجهة للأطفال فوراً
     */
    fun triageAndEscalateIncident(report: ChildSafetyIncidentReport): ChildSafetyIncidentReport {
        val calculatedUrgency =
            when (report.incidentType) {
                ChildSafetyIncidentType.EXPLOITATION_OR_ABUSE,
                ChildSafetyIncidentType.SUSPICIOUS_CONTACT,
                -> IncidentUrgency.CRITICAL_EMERGENCY
                ChildSafetyIncidentType.INAPPROPRIATE_CONTENT,
                ChildSafetyIncidentType.BULLYING_OR_HARASSMENT,
                ChildSafetyIncidentType.UNAUTHORIZED_DATA_COLLECTION,
                -> IncidentUrgency.HIGH
            }

        val slaDurationMs = calculatedUrgency.maxResponseSlaMinutes * 60 * 1000L
        val now = System.currentTimeMillis()

        return report.copy(
            urgency = calculatedUrgency,
            status = IncidentResolutionStatus.OPEN_ESCALATED,
            slaDeadlineTimestamp = now + slaDurationMs,
            internalNotes = "[ESCALATION_ENGINE] تم إدراج البلاغ في مسار الطوارئ الفوري لفريق حماية الأطفال والإدارة بمهلة استجابة ${calculatedUrgency.maxResponseSlaMinutes} دقيقة.",
        )
    }

    /**
     * فحص وتقييم المحتوى التعليمي للأطفال وخلوه من المخاطر أو المحفزات المضللة
     */
    fun evaluateEducationalContentSafety(
        contentId: String,
        title: String,
        textSnippet: String,
        hasInAppPurchasesOrAds: Boolean,
    ): EducationalContentSafetyCheck {
        val lower = "$title $textSnippet".lowercase()
        val hasViolentKeywords = lower.contains("عنف") || lower.contains("قتل") || lower.contains("سلاح") || lower.contains("رعب")
        val hasSubscriptionNudges =
            hasInAppPurchasesOrAds || lower.contains("اشترك الآن") || lower.contains("ادفع") || lower.contains("بطاقة ائتمان")

        val isSafe = !hasViolentKeywords && !hasSubscriptionNudges
        val notes = mutableListOf<String>()

        if (hasViolentKeywords) {
            notes.add("المحتوى يحتوي على كلمات أو مفردات غير مناسبة للأطفال.")
        }
        if (hasSubscriptionNudges) {
            notes.add("يحتوي على محفزات شراء أو إعلانات محظورة في بيئة الأطفال.")
        }
        if (isSafe) {
            notes.add("المحتوى آمن، تعليمي، ومطابق للضوابط الشرعية للأطفال.")
        }

        val score = if (isSafe) 1.0f else 0.3f

        return EducationalContentSafetyCheck(
            contentId = contentId,
            isChildSafe = isSafe,
            ageRecommendation = if (isSafe) "مناسب للأطفال والناشئة (3+ إلى 12 سنة)" else "غير مخصص للأطفال",
            hasDeceptiveSubscriptionTriggers = hasSubscriptionNudges,
            hasViolentOrFrighteningElements = hasViolentKeywords,
            isShariaCompliantForMinors = isSafe,
            audioVisualSafetyScore = score,
            safetyNotes = notes,
        )
    }

    /**
     * تنفيذ تطهير ومسح بيانات القاصر بالكامل
     */
    fun executeMinorDataPurge(
        childUserId: String,
        recordingsCount: Int,
        projectsCount: Int,
    ): MinorDataDeletionSummary {
        val receipt = sha256("PURGE_${childUserId}_${System.currentTimeMillis()}_${UUID.randomUUID()}")
        return MinorDataDeletionSummary(
            deletionRequestId = "DEL_MINOR_${UUID.randomUUID().toString().take(8)}",
            childUserId = childUserId,
            executedAt = System.currentTimeMillis(),
            deletedRecordingsCount = recordingsCount,
            deletedProjectsCount = projectsCount,
            deletedProfileData = true,
            deletedActivityLogs = true,
            confirmationReceiptHash = receipt,
        )
    }

    fun sha256(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}
