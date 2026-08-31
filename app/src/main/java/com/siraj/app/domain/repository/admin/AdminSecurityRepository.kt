package com.siraj.app.domain.repository.admin

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.admin.*
import kotlinx.coroutines.flow.Flow

interface AdminSecurityRepository {
    suspend fun getAdminSecurityConfig(adminId: String): Resource<AdminSecurityConfig>
    
    suspend fun createAdminSession(
        adminId: String, 
        device: AdminDevice,
        isMfaVerified: Boolean
    ): Resource<AdminSession>
    
    suspend fun revokeSession(sessionId: String, revokedByAdminId: String): Resource<Unit>
    
    suspend fun getActiveSessions(adminId: String): Flow<List<AdminSession>>
    
    suspend fun verifyMfaCode(adminId: String, code: String): Resource<Boolean>
    
    suspend fun logSensitiveOperation(log: SecurityAuditLog): Resource<Unit>
    
    suspend fun checkDeviceTrust(deviceId: String, ipAddress: String?): Resource<Boolean>
    
    fun getAuditLogs(): Flow<List<SecurityAuditLog>>
    
    suspend fun updateAdminRole(targetAdminId: String, newRole: AdminRole, executingAdminId: String): Resource<Unit>
}
