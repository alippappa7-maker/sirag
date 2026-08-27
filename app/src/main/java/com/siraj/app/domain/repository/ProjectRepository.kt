package com.siraj.app.domain.repository

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.Project
import com.siraj.app.domain.models.ProjectActivity
import com.siraj.app.domain.models.ProjectVersion
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    fun getRecentProjects(workspaceId: String, limit: Int = 5): Flow<Resource<List<Project>>>
    fun getAllProjects(workspaceId: String): Flow<Resource<List<Project>>>
    
    // Pagination, sorting, and searching
    suspend fun getProjects(workspaceId: String, limit: Int, offset: Int = 0, query: String = "", sortBy: String = "updatedAt"): Resource<List<Project>>
    
    suspend fun getProject(projectId: String): Resource<Project>
    suspend fun createProject(project: Project): Resource<String>
    suspend fun updateProject(project: Project): Resource<Unit>
    suspend fun deleteProject(projectId: String): Resource<Unit>
    suspend fun restoreProject(projectId: String): Resource<Unit>
    suspend fun archiveProject(projectId: String): Resource<Unit>
    suspend fun copyProject(projectId: String, newOwnerId: String): Resource<String>
    
    // Activities and Versions
    suspend fun logActivity(activity: ProjectActivity): Resource<Unit>
    suspend fun getProjectActivities(projectId: String): Resource<List<ProjectActivity>>
    
    suspend fun createVersion(version: ProjectVersion): Resource<String>
    suspend fun getProjectVersions(projectId: String): Resource<List<ProjectVersion>>
}
