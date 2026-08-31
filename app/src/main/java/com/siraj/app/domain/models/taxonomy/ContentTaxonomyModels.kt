package com.siraj.app.domain.models.taxonomy

/**
 * التصنيفات الموحدة للمحتوى في سراج وفق متطلبات المنظومة (PROMPT 088)
 */
enum class ContentOriginType(val code: String, val titleArabic: String, val descriptionArabic: String) {
    SYSTEM_CONTENT("system_content", "محتوى النظام", "محتوى ثابت أو إعدادات وواجهات أساسية موفرة من المنظومة"),
    EDITORIAL_CONTENT("editorial_content", "محتوى تحريري رسمي", "محتوى شرعي وتوعوي رسمي معتمد ومحرر بواسطة هيئة التحرير بسراج"),
    USER_GENERATED("user_generated", "محتوى المستخدمين (UGC)", "محتوى منشأ ومرفوع بواسطة مستخدمي وصناع محتوى المنصة"),
    AI_GENERATED("ai_generated", "محتوى مولد بالذكاء الاصطناعي", "مسودات أو أصول ولدت بواسطة خوارزميات ونماذج الذكاء الاصطناعي"),
    LICENSED_EXTERNAL("licensed_external", "محتوى مرخص خارجي", "مواد ووسائط مأخوذة من جهات ومصادر خارجية بموجب تراخيص محددة")
}

enum class ContentDisciplineType(val code: String, val titleArabic: String) {
    QURAN_TEXT("Quran_text", "نص قرآني"),
    TAFSIR("Tafsir", "تفسير القرآن"),
    HADITH("Hadith", "حديث شريف"),
    FIQH("Fiqh", "فقه وأحكام"),
    EDUCATIONAL("Educational", "تعليمي وتوعوي"),
    GENERAL("General", "عام ودعوي")
}

enum class ContentMediaType(val code: String, val titleArabic: String) {
    TEXT("Text", "نص ومقالة"),
    AUDIO("Audio", "صوتي وتلاوات"),
    VIDEO("Video", "مرئي وفيلم قصير"),
    IMAGE("Image", "صورة وتصميم"),
    TEMPLATE("Template", "قالب إنتاج"),
    INTERACTIVE("Interactive", "تفاعلي ومحراب")
}

enum class AuthorType(val code: String, val titleArabic: String) {
    SYSTEM("system", "النظام"),
    SCHOLAR_EDITOR("scholar_editor", "عالم / محرر شرعي معتمد"),
    CREATOR("creator", "صانع محتوى"),
    AI_ASSISTANT("ai_assistant", "مساعد ذكاء اصطناعي"),
    THIRD_PARTY_CREATOR("third_party_creator", "جهة خارجية / مؤلف خارجي")
}

enum class GenerationMethod(val code: String, val titleArabic: String) {
    MANUAL_HUMAN("manual_human", "إنشاء بشري يدوي"),
    AI_GENERATED("ai_generated", "توليد آلي بالذكاء الاصطناعي"),
    HYBRID_ASSISTED("hybrid_assisted", "بشري بمساعدة الذكاء الاصطناعي"),
    IMPORTED_DATASET("imported_dataset", "مستورد من قواعد بيانات موثقة (المصحف الشريف / كتب السنة)")
}

enum class TaxonomyVerificationStatus(val code: String, val titleArabic: String) {
    UNVERIFIED("unverified", "غير مدقق"),
    PENDING_REVIEW("pending_review", "قيد المراجعة الشرعية/التحريرية"),
    SHARIA_VERIFIED("sharia_verified", "معتمد وموثق شرعياً"),
    EDITORIAL_VERIFIED("editorial_verified", "معتمد تحريرياً"),
    REJECTED("rejected", "مرفوض")
}

enum class TaxonomyRightsStatus(val code: String, val titleArabic: String, val isPublicDomainOrLicensed: Boolean) {
    PUBLIC_DOMAIN("public_domain", "ملك عام (وقف/تراث)", true),
    SIRAJ_ORIGINAL("siraj_original", "حقوق محفوظة لمنصة سراج", true),
    LICENSED_CC("licensed_cc", "مرخص مشاع إبداعي (مع العزو)", true),
    LICENSED_COMMERCIAL("licensed_commercial", "مرخص تجارياً بموجب اتفاقية", true),
    RESTRICTED("restricted", "مقيد الاستخدام / خاص", false),
    UNKNOWN("unknown", "مجهول الترخيص (يحظر النشر)", false)
}

enum class TaxonomyVisibility(val code: String, val titleArabic: String) {
    PRIVATE("private", "خاص بالمستخدم"),
    WORKSPACE_ONLY("workspace_only", "مساحة العمل فقط"),
    PENDING_AUDIT("pending_audit", "معلق للمراجعة والتدقيق"),
    PUBLIC_APPROVED("public_approved", "عام معتمد"),
    RESTRICTED_SUSPENDED("restricted_suspended", "موقوف / معلق لخلل")
}

enum class ReviewPipelinePath(val code: String, val titleArabic: String) {
    SHARIA_SCHOLAR_MANDATORY("sharia_scholar_mandatory", "مسار المراجعة الشرعية الإلزامية"),
    EDITORIAL_STANDARD("editorial_standard", "مسار التدقيق التحريري واللغوي"),
    RIGHTS_AND_SAFETY_SCAN("rights_and_safety_scan", "مسار فحص الحقوق والسلامة العامة"),
    COMMUNITY_MODERATION("community_moderation", "مسار مراجعة محتوى المستخدمين"),
    LOCKED_IMMUTABLE_PASSTHROUGH("locked_immutable_passthrough", "نص قرآني محمي (لا يقبل التعديل)")
}

/**
 * البيانات الوصفية الشاملة لتصنيف المحتوى ومصدره (Metadata)
 */
data class ContentTaxonomyMetadata(
    val originType: ContentOriginType,
    val disciplineType: ContentDisciplineType,
    val mediaType: ContentMediaType,
    val authorType: AuthorType,
    val generationMethod: GenerationMethod,
    val verificationStatus: TaxonomyVerificationStatus,
    val rightsStatus: TaxonomyRightsStatus,
    val visibility: TaxonomyVisibility,
    val ownerId: String,
    val reviewerId: String? = null,
    val versionId: String = "v1",
    
    // Provenance and Citation details
    val sourceId: String? = null,
    val sourceTitle: String? = null,
    val sourceUrl: String? = null,
    val sourceReference: String? = null,
    val authorOrScholarName: String? = null,
    val licenseAttributionText: String? = null,
    
    // Safety, Integrity, and Mutability flags
    val isLockedImmutable: Boolean = false, // Quran text is ALWAYS locked
    val isQuranicText: Boolean = false,
    val isAiAssisted: Boolean = false,
    val reviewPipelinePath: ReviewPipelinePath = ReviewPipelinePath.COMMUNITY_MODERATION,
    val allowedRolesToEdit: List<String> = listOf("CREATOR", "ADMIN"),
    val clientReportedCategory: String? = null,
    val serverValidatedAt: Long = System.currentTimeMillis()
)

/**
 * عنصر محتوى مصنف بالكامل
 */
data class ClassifiedContentItem(
    val id: String,
    val title: String,
    val contentSnippet: String,
    val metadata: ContentTaxonomyMetadata,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * تقرير وتدقيق المحتوى غير المصنف أو المخالف للتصنيف
 */
data class TaxonomyAuditReport(
    val totalItemsCount: Int,
    val classifiedItemsCount: Int,
    val unclassifiedItemsCount: Int,
    val quranTextLockedCount: Int,
    val aiGeneratedItemsCount: Int,
    val userGeneratedItemsCount: Int,
    val editorialItemsCount: Int,
    val licensedExternalCount: Int,
    val rightsMissingCount: Int,
    val compliancePercentage: Float,
    val unclassifiedItemIds: List<String>,
    val auditSummary: String,
    val auditedAt: Long = System.currentTimeMillis()
)

/**
 * نموذج لترحيل البيانات القديمة إلى التصنيف الجديد
 */
data class LegacyContentItem(
    val id: String,
    val title: String,
    val rawCategory: String?,
    val rawSource: String?,
    val isQuran: Boolean = false,
    val isAi: Boolean = false,
    val ownerId: String
)

data class TaxonomyMigrationResult(
    val totalMigrated: Int,
    val successCount: Int,
    val failedCount: Int,
    val migratedItems: List<ClassifiedContentItem>,
    val migrationLog: List<String>,
    val completedAt: Long = System.currentTimeMillis()
)
