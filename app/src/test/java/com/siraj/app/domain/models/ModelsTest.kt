package com.siraj.app.domain.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ModelsTest {

    @Test
    fun testUserProfileValidation() {
        val user = UserProfile("u1", "Ali", "ali@example.com")
        assertEquals("u1", user.id)
        
        assertThrows(IllegalArgumentException::class.java) {
            UserProfile("", "Ali", "ali@example.com")
        }
        
        assertThrows(IllegalArgumentException::class.java) {
            UserProfile("u2", "Ali", "invalid-email")
        }
    }

    @Test
    fun testWorkspaceValidation() {
        val workspace = Workspace("w1", "u1", "My Workspace")
        assertEquals("w1", workspace.id)
        
        assertThrows(IllegalArgumentException::class.java) {
            Workspace("", "u1", "My Workspace")
        }
    }

    @Test
    fun testProjectValidation() {
        val project = Project("p1", "w1", "My Project")
        assertEquals("p1", project.id)
        
        assertThrows(IllegalArgumentException::class.java) {
            Project("p1", "w1", "")
        }
    }

    @Test
    fun testSourceReferenceValidation() {
        val source = SourceReference(
            id = "s1",
            sourceUrl = "http://example.com",
            sourceTitle = "Title",
            author = "Author",
            contentType = ContentType.EDUCATIONAL
        )
        assertEquals(ContentState.DRAFT, source.verificationStatus)
        
        assertThrows(IllegalArgumentException::class.java) {
            SourceReference(
                id = "",
                sourceUrl = "http://example.com",
                sourceTitle = "Title",
                author = "Author",
                contentType = ContentType.EDUCATIONAL
            )
        }
    }

    @Test
    fun testAssetValidation() {
        val asset = Asset(
            id = "a1",
            projectId = "p1",
            contentType = ContentType.IMAGE,
            creatorName = "Creator",
            provider = "Provider",
            licenseType = "CC-BY"
        )
        assertEquals("unknown", asset.rightsStatus)
        
        assertThrows(IllegalArgumentException::class.java) {
            Asset(
                id = "a1",
                projectId = "p1",
                contentType = ContentType.IMAGE,
                creatorName = "",
                provider = "Provider",
                licenseType = "CC-BY"
            )
        }
    }

    @Test
    fun testSceneValidation() {
        val scene = Scene("sc1", "script1", 1, "A scene")
        assertEquals(1, scene.sequenceNumber)
        
        assertThrows(IllegalArgumentException::class.java) {
            Scene("sc1", "script1", -1, "A scene")
        }
    }

    @Test
    fun testWalletValidation() {
        val wallet = Wallet("wal1", "w1", 100.0)
        assertEquals(100.0, wallet.balance, 0.0)
        
        assertThrows(IllegalArgumentException::class.java) {
            Wallet("wal1", "w1", -50.0)
        }
    }
}
