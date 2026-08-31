package com.siraj.app.domain.models

import org.junit.Assert.assertEquals
import org.junit.Test

class ModelsTest {
    @Test
    fun testUserProfile() {
        val user = UserProfile(id = "u1", name = "Ali", email = "ali@example.com")
        assertEquals("u1", user.id)
        assertEquals("Ali", user.name)
        assertEquals("ali@example.com", user.email)
    }

    @Test
    fun testWorkspace() {
        val workspace = Workspace(id = "w1", ownerId = "u1", name = "My Workspace", type = WorkspaceType.PERSONAL)
        assertEquals("w1", workspace.id)
        assertEquals("My Workspace", workspace.name)
        assertEquals(WorkspaceType.PERSONAL, workspace.type)
    }

    @Test
    fun testProject() {
        val project = Project(id = "p1", workspaceId = "w1", title = "My Project")
        assertEquals("p1", project.id)
        assertEquals("My Project", project.title)
    }

    @Test
    fun testSource() {
        val source =
            Source(
                id = "s1",
                type = SourceType.TAFSIR,
                title = "تفسير ابن كثير",
                authorOrNarrator = "ابن كثير",
                reviewStatus = SourceVerificationStatus.VERIFIED,
            )
        assertEquals(SourceVerificationStatus.VERIFIED, source.reviewStatus)
        assertEquals("تفسير ابن كثير", source.title)
    }

    @Test
    fun testAsset() {
        val asset =
            Asset(
                id = "a1",
                projectId = "p1",
                type = AssetType.IMAGE,
                license = "CC-BY",
                status = AssetStatus.READY,
            )
        assertEquals(AssetStatus.READY, asset.status)
        assertEquals(AssetType.IMAGE, asset.type)
    }

    @Test
    fun testScene() {
        val scene =
            Scene(
                id = "sc1",
                projectId = "p1",
                orderIndex = 1,
                title = "A scene",
                durationMs = 5000L,
            )
        assertEquals(1, scene.orderIndex)
        assertEquals(5000L, scene.durationMs)
    }
}
