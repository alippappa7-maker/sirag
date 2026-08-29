package com.siraj.app.domain.models.privacy

enum class DeletionStatus(val titleArabic: String) {
    NONE("لا يوجد طلب حذف"),
    GRACE_PERIOD_ACTIVE("فترة السماح نشطة (الحساب مقفل)"),
    IN_PROGRESS("جاري مسح البيانات نهائياً"),
    COMPLETED("تم الحذف والتطهير"),
    CANCELLED("تم إلغاء طلب الحذف")
}

data class AccountDeletionRequest(
    val requestId: String = "",
    val userId: String = "",
    val status: DeletionStatus = DeletionStatus.NONE,
    val requestedAt: Long = 0L,
    val scheduledPurgeAt: Long = 0L,
    val gracePeriodDays: Int = 14,
    val reason: String = "",
    val legalFinancialRecordsNotice: String = "يتم الاحتفاظ بملخص المعاملات المالية والفواتير الضريبية مجهولة الهوية لمدة 5 سنوات لأغراض الامتثال المالي والقانوني وفق الأنظمة السارية، مع مسح وتطهير كافة البيانات والمشاريع والملفات الشخصية فوراً."
)

data class DataCorrectionRequest(
    val id: String = "",
    val userId: String = "",
    val fieldName: String = "",
    val currentValue: String = "",
    val requestedValue: String = "",
    val reason: String = "",
    val submittedAt: Long = System.currentTimeMillis(),
    val status: String = "قيد المراجعة من فريق الخصوصية"
)

data class StoredDataCategory(
    val id: String,
    val title: String,
    val description: String,
    val storageLocation: String,
    val retentionPolicy: String,
    val itemCount: Int = 0,
    val sizeBytes: Long = 0L,
    val isPersonal: Boolean = true,
    val isLegalRequired: Boolean = false
)

data class PrivacyOverviewData(
    val totalStorageBytes: Long = 0L,
    val projectsCount: Int = 0,
    val historyCount: Int = 0,
    val downloadsCount: Int = 0,
    val downloadsSizeBytes: Long = 0L,
    val cacheSizeBytes: Long = 0L,
    val categories: List<StoredDataCategory> = emptyList(),
    val deletionRequest: AccountDeletionRequest? = null
)

data class UserDataExportPackage(
    val exportId: String,
    val userId: String,
    val exportTimestamp: Long,
    val exportDateFormatted: String,
    val accountInfo: Map<String, Any?>,
    val projects: List<Map<String, Any?>>,
    val activityHistory: List<Map<String, Any?>>,
    val preferences: Map<String, Any?>,
    val anonymizedInvoicesSummary: List<Map<String, Any?>>,
    val sha256Checksum: String,
    val legalNotice: String = "تم استخراج هذا التصدير وفق معايير الخصوصية لمنصة سراج. لا يحتوي هذا الملف على أي كلمات مرور، أو مفاتيح تشفير، أو رموز دفع خام."
)
