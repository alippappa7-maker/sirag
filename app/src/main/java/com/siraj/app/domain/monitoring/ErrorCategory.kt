package com.siraj.app.domain.monitoring

/**
 * High-level categorization of application errors for Crashlytics filtering.
 * Facilitates grouping and triage without leaking underlying sensitive payloads.
 */
enum class ErrorCategory(val key: String, val titleAr: String) {
    NETWORK("network", "أخطاء الشبكة والاتصال"),
    AUTH("auth", "أخطاء المصادقة والجلسات"),
    DATABASE("database", "أخطاء قاعدة البيانات"),
    STORAGE("storage", "أخطاء التخزين والملفات"),
    AI_PROVIDER("ai_provider", "أخطاء مزودي الذكاء الاصطناعي"),
    QUEUE("queue", "أخطاء طوابير المعالجة والتصيير"),
    PAYMENT("payment", "أخطاء الفوترة والاشتراكات"),
    LOCAL_EXECUTION("local_execution", "أخطاء المعالجة المحلية والتصيير"),
    UI_STATE("ui_state", "أخطاء واجهة المستخدم ودورة الحياة"),
    SECURITY("security", "أخطاء الأمان والتحقق"),
    SYSTEM("system", "أخطاء النظام والتشغيل"),
    UNKNOWN("unknown", "أخطاء عامة غير مصنفة")
}

enum class BreadcrumbType(val category: String) {
    NAVIGATION("navigation"),
    USER_ACTION("user_action"),
    LIFECYCLE("lifecycle"),
    NETWORK_CALL("network_call"),
    STATE_TRANSITION("state_transition"),
    SYSTEM_EVENT("system_event")
}

enum class ErrorSeverity(val level: String) {
    INFO("info"),
    WARNING("warning"),
    ERROR("error"),
    FATAL("fatal")
}
