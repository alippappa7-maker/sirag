package com.siraj.app.features.admin.domain

import com.siraj.app.domain.models.admin.AdminRole
import com.siraj.app.domain.models.admin.AdminSecurityConfig
import com.siraj.app.domain.models.admin.AdminSession
import com.siraj.app.domain.models.admin.SensitiveOperationType

class AdminSecurityEngine {
    fun canPerformSensitiveOperation(
        session: AdminSession,
        config: AdminSecurityConfig,
        operation: SensitiveOperationType,
        isRecentlyReAuthenticated: Boolean,
    ): Boolean {
        // 1. Session must be active and not revoked
        if (session.isRevoked) return false
        if (System.currentTimeMillis() > session.expiresAt) return false

        // 2. Idle timeout check
        if (System.currentTimeMillis() - session.lastActiveAt > config.maxIdleTimeMillis) return false

        // 3. MFA must be verified for sensitive operations
        if (!session.isMfaVerified) return false

        // 4. Untrusted device requires re-auth for sensitive ops
        if (!session.device.isTrusted && !isRecentlyReAuthenticated) return false

        // 5. Check role-based access
        return hasPermissionForOperation(session.role, operation)
    }

    private fun hasPermissionForOperation(
        role: AdminRole,
        operation: SensitiveOperationType,
    ): Boolean =
        when (role) {
            AdminRole.OWNER -> true
            AdminRole.ADMIN -> {
                // Admins can do most things, but maybe not change owner permissions or export ALL data
                operation != SensitiveOperationType.EXPORT_DATA &&
                    operation != SensitiveOperationType.CHANGE_PERMISSION
            }
            AdminRole.REVIEWER -> {
                // Reviewers can only modify verified sources and delete content (with limits)
                operation == SensitiveOperationType.MODIFY_VERIFIED_SOURCE ||
                    operation == SensitiveOperationType.DELETE_CONTENT
            }
            else -> false // User and Creator have no admin permissions
        }

    fun validateRoleAssignment(
        executorRole: AdminRole,
        targetNewRole: AdminRole,
    ): Boolean {
        // Only Owner can assign Owner or Admin
        if (targetNewRole == AdminRole.OWNER || targetNewRole == AdminRole.ADMIN) {
            return executorRole == AdminRole.OWNER
        }

        // Admins can assign Reviewer
        if (targetNewRole == AdminRole.REVIEWER) {
            return executorRole == AdminRole.OWNER || executorRole == AdminRole.ADMIN
        }

        return false
    }

    fun requiresReAuthentication(
        operation: SensitiveOperationType,
        isDeviceTrusted: Boolean,
    ): Boolean {
        // High-risk operations always require re-auth if device is untrusted
        if (!isDeviceTrusted) return true

        return when (operation) {
            SensitiveOperationType.CHANGE_PERMISSION,
            SensitiveOperationType.EXPORT_DATA,
            SensitiveOperationType.ROTATE_SECRET,
            SensitiveOperationType.BULK_PUBLISH,
            SensitiveOperationType.DELETE_CONTENT,
            -> true
            else -> false
        }
    }
}
