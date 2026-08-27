package com.siraj.app.features.project.domain.models

enum class ValidationSeverity {
    BLOCKER, // تمنع التصدير نهائياً
    WARNING  // تحذير ينصح بمعالجته ولكن لا يمنع التصدير
}

enum class ValidationIssueType(val title: String) {
    SCENE_WITHOUT_MEDIA("مشهد بدون وسائط مرئية"),
    MISSING_AUDIO("التعليق الصوتي مفقود للمشهد"),
    OVERLAPPING_SUBTITLES("تداخل في توقيت أسطر الترجمة"),
    UNREVIEWED_CLAIM("ادعاء/محتوى شرعي غير معتمد"),
    UNLICENSED_ASSET("أصل وسائط بدون ترخيص محدد"),
    INVALID_DURATION("مدة المشهد غير صالحة أو صفرية"),
    TEXT_OVERFLOW("نص الشاشة يتجاوز الحدود المرئية"),
    MISSING_MANDATORY_SOURCE("مصدر شرعي مطلوب غير معروض")
}

data class PreExportValidationIssue(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sceneId: String? = null,
    val sceneIndex: Int? = null,
    val sceneTitle: String? = null,
    val issueType: ValidationIssueType,
    val severity: ValidationSeverity,
    val message: String,
    val fixRecommendation: String
)

data class PreExportReport(
    val issues: List<PreExportValidationIssue> = emptyList(),
    val checkedAt: Long = System.currentTimeMillis()
) {
    val blockerCount: Int get() = issues.count { it.severity == ValidationSeverity.BLOCKER }
    val warningCount: Int get() = issues.count { it.severity == ValidationSeverity.WARNING }
    val hasWarnings: Boolean get() = warningCount > 0
    val isExportAllowed: Boolean get() = blockerCount == 0
}
