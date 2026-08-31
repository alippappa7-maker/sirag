package com.siraj.app.domain.models.beta

/**
 * التصنيف الإجباري للملاحظة أو العيب حسب المواصفات:
 * - blocker: معطل كلي يمنع استخدام وظيفة حيوية أو يسبب انهياراً عاماً
 * - critical: حرج جداً (بما يشمل أي خطأ في نص قرآني، حديث، أو عزو شرعي)
 * - major: رئيسي يؤثر على تجربة مستخدم رئيسية ولكن يوجد مسار بديل
 * - minor: ثانوي، عيب طفيف في التنسيق أو الرسوميات
 * - enhancement: اقتراح تحسين ميزة قائمة أو إضافة جديدة
 * - duplicate: بلاغ مكرر لعيب مسجل مسبقاً
 * - not_reproducible: تعذر إعادة إنتاجه بعد محاولات موثقة
 * - expected_behavior: سلوك مقصود ومطابق للتصميم والمواصفات
 */
enum class DefectClassification(
    val key: String,
    val titleAr: String,
    val description: String,
    val isActionableBug: Boolean,
) {
    BLOCKER(
        key = "blocker",
        titleAr = "معطل كلي (Blocker)",
        description = "عطل مانع للعمل، انهيار متكرر، أو تعطيل كامل لوظيفة أساسية",
        isActionableBug = true,
    ),
    CRITICAL(
        key = "critical",
        titleAr = "حرج جداً (Critical)",
        description = "خطأ في نص شرعي/قرآني/حديث، خلل في الخصوصية والأمان، أو فقدان بيانات",
        isActionableBug = true,
    ),
    MAJOR(
        key = "major",
        titleAr = "رئيسي (Major)",
        description = "خلل وظيفي كبير يؤثر على تجربة المستخدم بدون بديل مباشر",
        isActionableBug = true,
    ),
    MINOR(
        key = "minor",
        titleAr = "ثانوي (Minor)",
        description = "عيب طفيف في المحاذاة، التباين، الترجمة، أو الحركة الانتقالية",
        isActionableBug = true,
    ),
    ENHANCEMENT(
        key = "enhancement",
        titleAr = "اقتراح تحسين (Enhancement)",
        description = "طلب إضافة ميزة جديدة أو تحسين تدفق عمل حالي",
        isActionableBug = false,
    ),
    DUPLICATE(
        key = "duplicate",
        titleAr = "مكرر (Duplicate)",
        description = "تم الإبلاغ عنه وتوثيقه مسبقاً في تذكرة أخرى",
        isActionableBug = false,
    ),
    NOT_REPRODUCIBLE(
        key = "not_reproducible",
        titleAr = "غير قابل لإعادة الإنتاج (Not Reproducible)",
        description = "تعذر تكرار المشكلة في بيئة الاختبار والمحاكاة",
        isActionableBug = false,
    ),
    EXPECTED_BEHAVIOR(
        key = "expected_behavior",
        titleAr = "سلوك متوقع (Expected Behavior)",
        description = "البرنامج يعمل وفق المواصفات الشرعية والتقنية المصممة",
        isActionableBug = false,
    ),
    ;

    companion object {
        fun fromKey(key: String): DefectClassification = values().find { it.key.equals(key, ignoreCase = true) } ?: MINOR
    }
}

/**
 * مجال ومجال الاختصاص للعيب:
 * - SHARIA_CONTENT: يعامل بأعلى درجات الحساسية ويصنف دائماً كـ Critical أو Blocker
 */
enum class DefectDomain(
    val titleAr: String,
    val iconName: String,
) {
    SHARIA_CONTENT("المحتوى والنصوص الشرعية", "MenuBook"),
    MEDIA_STUDIO("استوديو إنتاج الفيديو والصوت", "VideoLibrary"),
    AUTH_ACCOUNT("المصادقة والحساب والصلاحيات", "Security"),
    OFFLINE_SYNC("المزامنة وقواعد البيانات المحلية", "CloudSync"),
    UI_ACCESSIBILITY("الواجهة والوصول وقارئات الشاشة", "Accessibility"),
    PERFORMANCE_STABILITY("الأداء والذاكرة واستهلاك البطارية", "Speed"),
    BILLING_CREDITS("الاشتراكات وحساب الأرصدة", "CreditCard"),
}

/**
 * مستوى أولوية الإصلاح في قائمة العمل:
 */
enum class DefectPriority(
    val code: String,
    val titleAr: String,
    val orderWeight: Int,
) {
    P0_IMMEDIATE("P0", "فوري / طارئ (P0)", 0),
    P1_HIGH("P1", "عالي الأولوية (P1)", 1),
    P2_MEDIUM("P2", "متوسط الأولوية (P2)", 2),
    P3_LOW("P3", "منخفض الأولوية (P3)", 3),
}

/**
 * دورة حياة حالة العيب:
 */
enum class DefectStatus(
    val titleAr: String,
) {
    REPORTED("مسجل جديد"),
    TRIAGED("تم الفرز والتصنيف"),
    IN_PROGRESS("قيد المعالجة والإصلاح"),
    RESOLVED("تم الإصلاح"),
    VERIFIED("تم التحقق وإعادة الاختبار"),
    DEFERRED("مؤجل للإصدار القادم"),
    CLOSED("مغلق مع التبرير"),
}

/**
 * نموذج العيب البرمجي الشامل وفق متطلبات المرحلة:
 */
data class BetaDefectRecord(
    val id: String = "", // e.g. "BUG-001"
    val title: String = "",
    val description: String = "",
    val classification: DefectClassification = DefectClassification.MINOR,
    val domain: DefectDomain = DefectDomain.UI_ACCESSIBILITY,
    val priority: DefectPriority = DefectPriority.P2_MEDIUM,
    val status: DefectStatus = DefectStatus.REPORTED,
    val deviceModel: String = "", // e.g. "Google Pixel 8 Pro", "Samsung Galaxy S24"
    val osVersion: String = "", // e.g. "Android 14 (API 34)"
    val appVersion: String = "1.0.0-beta.1",
    val buildCode: Int = 100,
    val stepsToReproduce: List<String> = emptyList(),
    val expectedResult: String = "",
    val actualResult: String = "",
    val safeLogsOrBreadcrumbs: String = "", // سجلات وتشخيصات آمنة بدون أي بيانات شخصية
    val assignedRole: String = "", // المسؤول (مثل: هيئة المراجعة الشرعية، مهندس الوسائط)
    val targetRelease: String = "1.0.0-beta.2",
    val resolutionNote: String? = null,
    val closureReason: String? = null,
    val verificationTest: String? = null,
    val reportedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

/**
 * ملخص إحصائيات الفرز والتصنيف:
 */
data class DefectTriageSummary(
    val totalCount: Int = 0,
    val blockerCount: Int = 0,
    val criticalCount: Int = 0,
    val majorCount: Int = 0,
    val minorCount: Int = 0,
    val enhancementCount: Int = 0,
    val duplicateCount: Int = 0,
    val notReproducibleCount: Int = 0,
    val expectedBehaviorCount: Int = 0,
    val openOrInProgressCount: Int = 0,
    val resolvedOrVerifiedCount: Int = 0,
    val deferredCount: Int = 0,
    val closedCount: Int = 0,
    val shariaDomainCount: Int = 0,
)
