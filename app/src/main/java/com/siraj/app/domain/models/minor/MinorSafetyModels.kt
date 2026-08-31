package com.siraj.app.domain.models.minor

/**
 * الفئة العمرية للمستخدم في نظام سراج (PROMPT 089)
 */
enum class UserAgeBracket(
    val code: String,
    val titleArabic: String,
    val descriptionArabic: String,
    val minimumAge: Int,
    val isMinor: Boolean,
    val requiresParentalConsent: Boolean
) {
    ADULT_18_PLUS(
        code = "adult_18_plus",
        titleArabic = "بالغ (18 سنة فما فوق)",
        descriptionArabic = "حساب كامل المزايا لصانعي المحتوى والباحثين المستقلين",
        minimumAge = 18,
        isMinor = false,
        requiresParentalConsent = false
    ),
    TEEN_13_TO_17(
        code = "teen_13_to_17",
        titleArabic = "يافع (13 - 17 سنة)",
        descriptionArabic = "حساب محمي بقيود أمان افتراضية وحظر الرسائل المباشرة والإعلانات الموجهة",
        minimumAge = 13,
        isMinor = true,
        requiresParentalConsent = false
    ),
    CHILD_UNDER_13(
        code = "child_under_13",
        titleArabic = "قاصر / طفل (تحت 13 سنة)",
        descriptionArabic = "حساب مقيد كلياً يتطلب موافقة صريحة وإشرافاً مستمراً من ولي الأمر",
        minimumAge = 0,
        isMinor = true,
        requiresParentalConsent = true
    ),
    UNSPECIFIED(
        code = "unspecified",
        titleArabic = "غير محدد (الوضع الآمن افتراضياً)",
        descriptionArabic = "يتم تطبيق أعلى معايير الحماية حتى تحديد العمر",
        minimumAge = 0,
        isMinor = true,
        requiresParentalConsent = false
    )
}

/**
 * التصنيف العمري المعتمد للمتجر والتطبيق (App Store / Google Play Age Rating)
 */
enum class StoreAgeRating(
    val ratingCode: String,
    val authority: String,
    val titleArabic: String,
    val disclosureArabic: String
) {
    PEGI_3(
        ratingCode = "PEGI 3",
        authority = "PEGI",
        titleArabic = "مناسب لجميع الأعمار",
        disclosureArabic = "محتوى إسلامي وتعليمي آمن خالٍ تماماً من أي محتوى عنيف أو غير لائق"
    ),
    GOOGLE_PLAY_TEEN(
        ratingCode = "Everyone 10+ / Teen",
        authority = "Google Play IARC",
        titleArabic = "الجميع 10+ / يافعون",
        disclosureArabic = "أدوات إنتاج محتوى وسيناريو بمساعدة الذكاء الاصطناعي مع إشراف الوالدين"
    )
}

/**
 * سياسة وضوابط حماية القاصرين المطبقة على الحساب
 */
data class MinorSafetyPolicy(
    val userId: String = "",
    val ageBracket: UserAgeBracket = UserAgeBracket.UNSPECIFIED,
    val isMinorProtectionActive: Boolean = true,
    val isPrivateByDefault: Boolean = true,
    val blockDirectMessages: Boolean = true,
    val disableFineLocation: Boolean = true,
    val disablePersonalizedAds: Boolean = true,
    val disableModelTrainingOnData: Boolean = true,
    val blockVoiceCloning: Boolean = true,
    val blockBiometricDataCollection: Boolean = true,
    val requireParentalApprovalForPublishing: Boolean = true,
    val requireParentalConsentForAiFeatures: Boolean = true,
    val hideFromPublicDiscovery: Boolean = true,
    val disableIndividualAnalyticsProfiling: Boolean = true,
    val allowOnlyCuratedEducationalContent: Boolean = false,
    val parentalGuardianEmail: String? = null,
    val isParentalConsentVerified: Boolean = false,
    val policyVersion: String = "1.0.0",
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * حالة ونموذج توثيق موافقة ولي الأمر (Parental Consent)
 */
enum class ParentalConsentStatus(val titleArabic: String) {
    NOT_REQUESTED("لم يُطلب بعد"),
    PENDING_VERIFICATION("بانتظار تأكيد ولي الأمر"),
    APPROVED_VERIFIED("معتمد وموثق رسمياً"),
    REJECTED("مرفوض من ولي الأمر"),
    REVOKED("تم سحب الموافقة")
}

data class ParentalConsentRecord(
    val consentId: String = "",
    val childUserId: String = "",
    val guardianEmail: String = "",
    val guardianName: String = "",
    val status: ParentalConsentStatus = ParentalConsentStatus.NOT_REQUESTED,
    val verificationCodeHash: String = "",
    val requestedAt: Long = System.currentTimeMillis(),
    val verifiedAt: Long? = null,
    val permissionsGranted: List<String> = emptyList(),
    val legalNotice: String = "بموافقتك كولي أمر، فإنك تمنح الإذن للقاصر باستخدام أدوات سراج التعليمية مع التزام سراج الكامل بعدم بيع البيانات أو استغلالها إعلانياً أو تدريب نماذج الذكاء الاصطناعي عليها."
)

/**
 * تصنيف بلاغات استغلال أو إساءة موجهة للأطفال (Child Safety Incident)
 */
enum class ChildSafetyIncidentType(val code: String, val titleArabic: String, val defaultUrgency: IncidentUrgency) {
    EXPLOITATION_OR_ABUSE("exploitation", "استغلال أو إساءة موجهة للأطفال", IncidentUrgency.CRITICAL_EMERGENCY),
    INAPPROPRIATE_CONTENT("inappropriate_content", "محتوى غير لائق للأطفال", IncidentUrgency.HIGH),
    BULLYING_OR_HARASSMENT("bullying", "تنمر أو مضايقة", IncidentUrgency.HIGH),
    SUSPICIOUS_CONTACT("suspicious_contact", "محاولة تواصل مشبوهة أو طلب بيانات خاصة", IncidentUrgency.CRITICAL_EMERGENCY),
    UNAUTHORIZED_DATA_COLLECTION("data_privacy_violation", "اشتباه في جمع بيانات قاصر بلا إذن", IncidentUrgency.HIGH)
}

enum class IncidentUrgency(val titleArabic: String, val maxResponseSlaMinutes: Int) {
    CRITICAL_EMERGENCY("طارئ وحرج جداً (تصعيد فوري)", 15),
    HIGH("أولوية عالية", 60),
    MEDIUM("أولوية متوسطة", 240)
}

enum class IncidentResolutionStatus(val titleArabic: String) {
    OPEN_ESCALATED("مفتوح ومصعد للتدخل الفوري"),
    UNDER_INVESTIGATION("قيد التحقيق من فريق حماية الأطفال"),
    RESOLVED_SUSPENDED("تم الحل بحظر المتسبب وحذف المادة"),
    RESOLVED_FALSE_ALARM("تم التحقق ولا توجد مخالفة"),
    ESCALATED_TO_AUTHORITIES("تم الإبلاغ للجهات المختصة والمنظمات الرسمية")
}

data class ChildSafetyIncidentReport(
    val reportId: String = "",
    val incidentType: ChildSafetyIncidentType = ChildSafetyIncidentType.EXPLOITATION_OR_ABUSE,
    val urgency: IncidentUrgency = IncidentUrgency.CRITICAL_EMERGENCY,
    val reportedUserId: String? = null,
    val reportedContentId: String? = null,
    val reporterUserId: String = "anonymous_reporter",
    val reporterContactEmail: String? = null,
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: IncidentResolutionStatus = IncidentResolutionStatus.OPEN_ESCALATED,
    val internalNotes: String = "",
    val legalNotificationSent: Boolean = false,
    val slaDeadlineTimestamp: Long = System.currentTimeMillis() + (15 * 60 * 1000L)
)

/**
 * تقرير فحص وملاءمة المحتوى التعليمي للأطفال
 */
data class EducationalContentSafetyCheck(
    val contentId: String,
    val isChildSafe: Boolean,
    val ageRecommendation: String,
    val hasDeceptiveSubscriptionTriggers: Boolean,
    val hasViolentOrFrighteningElements: Boolean,
    val isShariaCompliantForMinors: Boolean,
    val audioVisualSafetyScore: Float, // 0.0 to 1.0
    val safetyNotes: List<String>
)

/**
 * ملخص حذف وتطهير بيانات القاصر (Right to Erasure)
 */
data class MinorDataDeletionSummary(
    val deletionRequestId: String,
    val childUserId: String,
    val executedAt: Long = System.currentTimeMillis(),
    val deletedRecordingsCount: Int,
    val deletedProjectsCount: Int,
    val deletedProfileData: Boolean,
    val deletedActivityLogs: Boolean,
    val confirmationReceiptHash: String,
    val legalComplianceStatement: String = "تم مسح وتطهير كافة البيانات والتسجيلات والمشاريع الخاصة بالقاصر نهائياً من خوادم سراج دون استبقاء أي نسخة."
)
