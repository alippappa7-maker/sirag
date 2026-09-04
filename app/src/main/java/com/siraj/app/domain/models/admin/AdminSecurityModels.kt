package com.siraj.app.domain.models.admin

enum class AdminRole {
    USER,
    CREATOR,
    REVIEWER,
    ADMIN,
    OWNER
}

enum class SensitiveOperationType {
    CHANGE_PERMISSION,
    MODIFY_VERIFIED_SOURCE,
    DELETE_CONTENT,
    CHANGE_BALANCE,
    MODIFY_SUBSCRIPTION,
    CHANGE_COST_LIMITS,
    ROTATE_SECRET,
    BULK_PUBLISH,
    EXPORT_DATA
}

data class AdminDevice(
    val deviceId: String,
    val deviceName: String,
    val isTrusted: Boolean,
    val lastIpAddress: String?,
    val riskScore: Int = 0 // 0 to 100
)

data class AdminSession(
    val sessionId: String,
    val adminId: String,
    val role: AdminRole,
    val device: AdminDevice,
    val startedAt: Long,
    val expiresAt: Long,
    val isMfaVerified: Boolean,
    val lastActiveAt: Long,
    val isRevoked: Boolean = false
)

data class AdminSecurityConfig(
    val adminId: String,
    val role: AdminRole,
    val isMfaEnabled: Boolean,
    val mfaPhoneNumber: String? = null,
    val lastLoginAt: Long? = null,
    val isAccountActive: Boolean = true,
    val requireReAuthForSensitiveOps: Boolean = true,
    val maxIdleTimeMillis: Long = 15 * 60 * 1000 // 15 mins default
)

data class SecurityAuditLog(
    val logId: String,
    val adminId: String,
    val operation: SensitiveOperationType,
    val timestamp: Long,
    val ipAddress: String?,
    val deviceId: String?,
    val isSuccess: Boolean,
    val failureReason: String? = null
)
