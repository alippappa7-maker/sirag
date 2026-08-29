package com.siraj.app.domain.models.beta

enum class FeedbackCategory(val title: String, val description: String) {
    BUG("عطل برمجى (Bug)", "خطأ فني، انهيار، أو تجمد في الشاشة"),
    SHARIA_ISSUE("ملاحظة شرعية (Sharia)", "خطأ في نص، تشكيل، حديث، أو ضبط شرعي"),
    UX_IMPROVEMENT("تجربة المستخدم (UX/UI)", "صعوبة في الاستخدام، تباين، أو تنسيق"),
    PERFORMANCE("الأداء والسرعة (Performance)", "بطء في التحميل، استهلاك بطارية أو ذاكرة"),
    FEATURE_REQUEST("اقتراح ميزة (Suggestion)", "فكرة لتحسين سير العمل في التطبيق")
}

enum class FeedbackSeverity(val label: String, val levelColorHex: Long) {
    LOW("بسيط", 0xFF4CAF50),
    MEDIUM("متوسط", 0xFFFF9800),
    HIGH("عالي", 0xFFF44336),
    CRITICAL("حرج / مانع للعمل", 0xFFB71C1C)
}

data class BetaFeedback(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val userName: String = "",
    val category: FeedbackCategory = FeedbackCategory.BUG,
    val severity: FeedbackSeverity = FeedbackSeverity.MEDIUM,
    val title: String = "",
    val description: String = "",
    val stepsToReproduce: String = "",
    val currentRoute: String = "",
    val appVersion: String = "",
    val deviceModel: String = "",
    val androidOsVersion: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "NEW" // NEW, IN_REVIEW, RESOLVED, REJECTED
)
