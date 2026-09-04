package com.siraj.app.data.repository.admin

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.admin.*
import com.siraj.app.domain.repository.admin.AdminSecurityRepository
import com.siraj.app.features.admin.domain.AdminSecurityEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

class AdminSecurityRepositoryImpl(
    private val engine: AdminSecurityEngine,
) : AdminSecurityRepository {
    private val configs = mutableMapOf<String, AdminSecurityConfig>()
    private val activeSessions = MutableStateFlow<List<AdminSession>>(emptyList())
    private val auditLogs = MutableStateFlow<List<SecurityAuditLog>>(emptyList())

    override suspend fun getAdminSecurityConfig(adminId: String): Resource<AdminSecurityConfig> {
        val config = configs[adminId] ?: return Resource.Error("Admin config not found")
        if (!config.isAccountActive) return Resource.Error("Admin account is disabled")
        return Resource.Success(config)
    }

    override suspend fun createAdminSession(
        adminId: String,
        device: AdminDevice,
        isMfaVerified: Boolean,
    ): Resource<AdminSession> {
        val config = configs[adminId] ?: return Resource.Error("Admin config not found")

        if (config.isMfaEnabled && !isMfaVerified) {
            return Resource.Error("MFA verification required for this admin account")
        }

        val session =
            AdminSession(
                sessionId = "sess_${UUID.randomUUID()}",
                adminId = adminId,
                role = config.role,
                device = device,
                startedAt = System.currentTimeMillis(),
                expiresAt = System.currentTimeMillis() + (12 * 60 * 60 * 1000), // 12 hours
                isMfaVerified = isMfaVerified,
                lastActiveAt = System.currentTimeMillis(),
            )

        val currentSessions = activeSessions.value.toMutableList()
        // Terminate old sessions for same device to prevent session hijacking
        currentSessions.removeAll { it.adminId == adminId && it.device.deviceId == device.deviceId }
        currentSessions.add(session)
        activeSessions.value = currentSessions

        return Resource.Success(session)
    }

    override suspend fun revokeSession(
        sessionId: String,
        revokedByAdminId: String,
    ): Resource<Unit> {
        val currentSessions = activeSessions.value.toMutableList()
        val index = currentSessions.indexOfFirst { it.sessionId == sessionId }
        if (index == -1) return Resource.Error("Session not found")

        val session = currentSessions[index]
        currentSessions[index] = session.copy(isRevoked = true, expiresAt = System.currentTimeMillis())
        activeSessions.value = currentSessions

        return Resource.Success(Unit)
    }

    override suspend fun getActiveSessions(adminId: String): Flow<List<AdminSession>> =
        activeSessions.map { sessions ->
            sessions.filter { it.adminId == adminId && !it.isRevoked && it.expiresAt > System.currentTimeMillis() }
        }

    override suspend fun verifyMfaCode(
        adminId: String,
        code: String,
    ): Resource<Boolean> {
        if (code.length == 6 && code.all { it.isDigit() }) {
            return Resource.Success(true)
        }
        return Resource.Error("Invalid MFA code")
    }

    override suspend fun logSensitiveOperation(log: SecurityAuditLog): Resource<Unit> {
        val currentLogs = auditLogs.value.toMutableList()
        currentLogs.add(log)
        auditLogs.value = currentLogs
        return Resource.Success(Unit)
    }

    override suspend fun checkDeviceTrust(
        deviceId: String,
        ipAddress: String?,
    ): Resource<Boolean> {
        val isTrusted = deviceId.isNotBlank()
        return Resource.Success(isTrusted)
    }

    override fun getAuditLogs(): Flow<List<SecurityAuditLog>> = auditLogs.map { logs -> logs.sortedByDescending { it.timestamp } }

    override suspend fun updateAdminRole(
        targetAdminId: String,
        newRole: AdminRole,
        executingAdminId: String,
    ): Resource<Unit> {
        val executorConfig = configs[executingAdminId] ?: return Resource.Error("Executor config not found")

        if (!engine.validateRoleAssignment(executorConfig.role, newRole)) {
            return Resource.Error("Insufficient permissions to assign this role")
        }

        val targetConfig = configs[targetAdminId] ?: AdminSecurityConfig(targetAdminId, AdminRole.USER, false)
        configs[targetAdminId] = targetConfig.copy(role = newRole)

        return Resource.Success(Unit)
    }
}
