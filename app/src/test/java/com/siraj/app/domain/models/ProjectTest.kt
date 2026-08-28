package com.siraj.app.domain.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectTest {

    @Test
    fun `default project initialization`() {
        val project = Project(
            id = "test-id",
            workspaceId = "workspace-id",
            ownerId = "owner-id",
            title = "Test Project"
        )
        
        assertEquals("test-id", project.id)
        assertEquals(ProjectStatus.DRAFT, project.status)
        assertEquals(emptyList<Scene>(), project.scenes)
        assertTrue(project.createdAt > 0)
        assertTrue(project.updatedAt > 0)
    }

    @Test
    fun `copy project modifies ID and times`() {
        val project = Project(
            id = "test-id",
            workspaceId = "workspace-id",
            ownerId = "owner-id",
            title = "Test Project"
        )
        
        val copiedProject = project.copy(
            id = "new-id",
            title = "Test Project (Copy)"
        )
        
        assertNotEquals(project.id, copiedProject.id)
        assertEquals("Test Project (Copy)", copiedProject.title)
    }
}
