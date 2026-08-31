package com.siraj.app.features.admin

import com.siraj.app.core.utils.Resource
import com.siraj.app.data.repository.admin.AdminSecurityRepositoryImpl
import com.siraj.app.domain.models.admin.*
import com.siraj.app.features.admin.domain.AdminSecurityEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AdminSecurityTest {
    private lateinit var engine: AdminSecurityEngine
    private lateinit var repository: AdminSecurityRepositoryImpl

    private val ownerId = "admin_1" // pre-configured as OWNER
    private val adminId = "admin_2"
    private val reviewerId = "admin_3"

    @Before
    fun setup() {
        engine = AdminSecurityEngine()
        repository = AdminSecurityRepositoryImpl(engine)
    }

    @Test
    fun `test MFA is enforced for sensitive operations`() =
        runTest {
            val device = AdminDevice("device1", "My Phone", true, "127.0.0.1")

            // Attempt to create session without MFA
            val sessionResult = repository.createAdminSession(ownerId, device, false)
            assertTrue(sessionResult is Resource.Error)
            assertEquals("MFA verification required for this admin account", (sessionResult as Resource.Error).message)

            // Create with MFA
            val mfaVerifiedSession = repository.createAdminSession(ownerId, device, true)
            assertTrue(mfaVerifiedSession is Resource.Success)
        }

    @Test
    fun `test engine role based access control`() {
        val device = AdminDevice("device1", "My Phone", true, "127.0.0.1")
        val config = AdminSecurityConfig("admin_id", AdminRole.REVIEWER, true)
        val session =
            AdminSession(
                "sess1",
                "admin_id",
                AdminRole.REVIEWER,
                device,
                System.currentTimeMillis(),
                System.currentTimeMillis() + 10000,
                true,
                System.currentTimeMillis(),
            )

        // Reviewer can delete content
        assertTrue(
            engine.canPerformSensitiveOperation(
                session,
                config,
                SensitiveOperationType.DELETE_CONTENT,
                false,
            ),
        )

        // Reviewer CANNOT change permissions
        assertFalse(
            engine.canPerformSensitiveOperation(
                session,
                config,
                SensitiveOperationType.CHANGE_PERMISSION,
                false,
            ),
        )
    }

    @Test
    fun `test idle timeout prevents operation`() {
        val device = AdminDevice("device1", "My Phone", true, "127.0.0.1")
        val config = AdminSecurityConfig("admin_id", AdminRole.OWNER, true, maxIdleTimeMillis = 1000)

        // Session idle for 5 seconds (greater than max 1 sec)
        val session =
            AdminSession(
                "sess1",
                "admin_id",
                AdminRole.OWNER,
                device,
                System.currentTimeMillis() - 5000,
                System.currentTimeMillis() + 10000,
                true,
                System.currentTimeMillis() - 5000,
            )

        assertFalse(
            engine.canPerformSensitiveOperation(
                session,
                config,
                SensitiveOperationType.CHANGE_BALANCE,
                false,
            ),
        )
    }

    @Test
    fun `test role assignment hierarchy`() =
        runTest {
            // Owner assigns Admin (Success)
            val assign1 = repository.updateAdminRole(adminId, AdminRole.ADMIN, ownerId)
            assertTrue(assign1 is Resource.Success)

            // Admin assigns Owner (Fail)
            val assign2 = repository.updateAdminRole("new_owner", AdminRole.OWNER, adminId)
            assertTrue(assign2 is Resource.Error)

            // Admin assigns Reviewer (Success)
            val assign3 = repository.updateAdminRole(reviewerId, AdminRole.REVIEWER, adminId)
            assertTrue(assign3 is Resource.Success)
        }

    @Test
    fun `test session revocation`() =
        runTest {
            val device = AdminDevice("device1", "My Phone", true, "127.0.0.1")
            val sessionRes = repository.createAdminSession(ownerId, device, true)
            assertTrue(sessionRes is Resource.Success)
            val sessionId = (sessionRes as Resource.Success).data.sessionId

            // Verify active
            val activeSessionsBefore = repository.getActiveSessions(ownerId).first()
            assertEquals(1, activeSessionsBefore.size)

            // Revoke
            val revokeRes = repository.revokeSession(sessionId, ownerId)
            assertTrue(revokeRes is Resource.Success)

            // Verify revoked
            val activeSessionsAfter = repository.getActiveSessions(ownerId).first()
            assertEquals(0, activeSessionsAfter.size)
        }
}
