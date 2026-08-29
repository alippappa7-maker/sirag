package com.siraj.app.domain.models.backup

enum class BackupType(val labelArabic: String) {
    FULL("نسخة كاملة (Full Cluster Snapshot)"),
    INCREMENTAL("نسخة تزايدية (Incremental PITR)"),
    METADATA_ONLY("نسخة بيانات وصفية فقط (Metadata Only)"),
    DISASTER_RECOVERY_SNAPSHOT("نسخة طوارئ شاملة (Disaster Recovery)")
}

enum class BackupStatus(val labelArabic: String) {
    PENDING("قيد الجدولة"),
    IN_PROGRESS("جارٍ النسخ والتشفير"),
    SUCCESS("مكتملة ومؤمنة"),
    VERIFIED_HEALTHY("تم فحص السلامة والاستعادة التجريبية"),
    FAILED("فشل النسخ"),
    RESTORED("تمت الاستعادة بنجاح")
}

enum class BackupEnvironment(val labelArabic: String) {
    DEV("بيئة التطوير (Development)"),
    STAGING("بيئة الاختبار (Staging)"),
    PROD("بيئة الإنتاج (Production - معزولة تماماً)")
}

enum class BackupScope(val labelArabic: String) {
    FIRESTORE_COLLECTIONS("قواعد بيانات Firestore"),
    STORAGE_ASSETS("ملفات الوسائط والمستندات في Cloud Storage"),
    AUDIT_AND_REVIEWS("سجلات التدقيق والمراجعات الشرعية"),
    ALL_TIERS("كافة المكونات وقواعد البيانات والوسائط")
}

data class BackupSnapshot(
    val id: String,
    val timestamp: Long,
    val backupType: BackupType,
    val status: BackupStatus,
    val environment: BackupEnvironment,
    val scope: BackupScope,
    val storageLocationUri: String,
    val encryptionAlgorithm: String = "AES-256-GCM / Google Cloud KMS (CMEK)",
    val cmekKeyId: String = "projects/siraj-vault/locations/europe-west2/keyRings/backup-ring/cryptoKeys/siraj-db-backup-key",
    val checksumSha256: String,
    val collectionsIncluded: List<String>,
    val documentCount: Long,
    val sizeBytes: Long,
    val purgedTombstonesCount: Int = 0,
    val rpoLatencyMinutes: Long = 15,
    val verifiedAt: Long? = null,
    val notes: String = ""
)

data class BackupRetentionPolicy(
    val dailyRetentionDays: Int = 30,
    val weeklyRetentionWeeks: Int = 12,
    val monthlyRetentionMonths: Int = 12,
    val coldArchiveYears: Int = 7,
    val isWormLocked: Boolean = true,
    val autoPurgeDeletedUsersAfterDays: Int = 30
)

enum class RestoreTargetEnvironment(val labelArabic: String) {
    ISOLATED_RECOVERY_SANDBOX("بيئة اختبار الاستعادة المعزولة (Isolated Sandbox)"),
    STAGING_VERIFICATION("بيئة التحقق (Staging Verification)"),
    PRODUCTION_EMERGENCY("بيئة الإنتاج الطارئة (Production Emergency Failover)")
}

enum class RestoreStatus(val labelArabic: String) {
    INITIALIZING("بدء عملية الاستعادة"),
    VALIDATING_SIGNATURES("التحقق من التوقيع الرقمي والتشفير"),
    PURGING_DELETED_USER_TOMBSTONES("استبعاد وتطهير بيانات المستخدمين المحذوفين (Right to be Forgotten)"),
    RESTORING_DOCUMENTS("استعادة المستندات والبيانات"),
    VERIFYING_INTEGRITY("فحص سلامة العلاقات والمراجع"),
    COMPLETED("اكتملت الاستعادة بنجاح"),
    FAILED("فشلت الاستعادة")
}

data class RestoreJob(
    val id: String,
    val snapshotId: String,
    val targetEnvironment: RestoreTargetEnvironment,
    val targetWorkspaceId: String? = null,
    val targetProjectId: String? = null,
    val status: RestoreStatus,
    val isDryRun: Boolean = true,
    val excludedDeletedUserIds: List<String> = emptyList(),
    val restoredDocumentsCount: Int = 0,
    val durationMs: Long = 0,
    val initiatedBy: String,
    val initiatedAt: Long,
    val completedAt: Long? = null,
    val logs: List<String> = emptyList(),
    val errorMessage: String? = null
)

data class DisasterRecoveryPlan(
    val rpoTargetMinutes: Int = 60,
    val rtoTargetMinutes: Int = 240,
    val projectRtoMinutes: Int = 15,
    val primaryRegion: String = "europe-west2 (London)",
    val failoverRegion: String = "europe-west1 (Belgium)",
    val isolatedBackupBucket: String = "gs://siraj-prod-backups-isolated-vault",
    val lastDryRunTestAt: Long = System.currentTimeMillis() - 86400000L * 2,
    val lastDryRunSuccess: Boolean = true,
    val complianceStatus: String = "COMPLIANT_GDPR_RIGHT_TO_BE_FORGOTTEN",
    val backupOperatorCount: Int = 2,
    val isMultiRegionReplicationActive: Boolean = true
)

enum class BackupAccessRole(val labelArabic: String, val permissions: List<String>) {
    BACKUP_ADMIN("مدير النسخ الاحتياطي (DR Administrator)", listOf("TRIGGER_BACKUP", "FULL_RESTORE", "UPDATE_POLICY", "PURGE_TOMBSTONES", "VIEW_LOGS")),
    BACKUP_OPERATOR("مشغل عمليات النسخ (Backup Operator)", listOf("TRIGGER_BACKUP", "DRY_RUN_RESTORE", "VIEW_LOGS")),
    AUDITOR_READONLY("مراجع الامتثال والتدقيق (Auditor Read-Only)", listOf("VIEW_SNAPSHOTS", "VERIFY_CHECKSUMS", "VIEW_COMPLIANCE_REPORTS")),
    REGULAR_USER("مستخدم عادي", emptyList())
}
