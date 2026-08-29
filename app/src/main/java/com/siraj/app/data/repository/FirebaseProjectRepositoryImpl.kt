package com.siraj.app.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.siraj.app.core.utils.Resource
import com.siraj.app.core.error.ErrorHandler
import com.siraj.app.domain.models.*
import com.siraj.app.domain.repository.ProjectRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseProjectRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ProjectRepository {

    private val projectsCollection = firestore.collection("projects")
    private val activitiesCollection = firestore.collection("project_activities")
    private val versionsCollection = firestore.collection("project_versions")

    override fun getRecentProjects(userId: String, limit: Int): Flow<Resource<List<Project>>> = callbackFlow {
        trySend(Resource.Loading)
        val registration = projectsCollection
            .whereEqualTo("workspaceId", userId)
            .whereNotEqualTo("status", ProjectStatus.DELETED.name)
            .orderBy("status")
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "حدث خطأ"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val projects = snapshot.documents.mapNotNull { it.toObject(Project::class.java) }
                    trySend(Resource.Success(projects))
                }
            }
        awaitClose { registration.remove() }
    }

    override fun getAllProjects(userId: String): Flow<Resource<List<Project>>> = callbackFlow {
        trySend(Resource.Loading)
        val registration = projectsCollection
            .whereEqualTo("workspaceId", userId)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "حدث خطأ"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val projects = snapshot.documents.mapNotNull { it.toObject(Project::class.java) }
                    trySend(Resource.Success(projects))
                }
            }
        awaitClose { registration.remove() }
    }

    override suspend fun getProjects(
        userId: String,
        limit: Int,
        offset: Int,
        query: String,
        sortBy: String
    ): Resource<List<Project>> {
        return try {
            // Firestore pagination using offset is expensive for large sets, 
            // but fine for simple cases without a startAfter cursor if offset is small.
            var firestoreQuery = projectsCollection
                .whereEqualTo("workspaceId", userId)
                
            firestoreQuery = when(sortBy) {
                "createdAt" -> firestoreQuery.orderBy("createdAt", Query.Direction.DESCENDING)
                "title" -> firestoreQuery.orderBy("title", Query.Direction.ASCENDING)
                else -> firestoreQuery.orderBy("updatedAt", Query.Direction.DESCENDING)
            }
            
            val snapshot = firestoreQuery
                .limit(limit.toLong())
                // .offset(offset) // Note: offset requires a specific index and cost, omitting for MVP
                .get().await()
                
            var projects = snapshot.documents.mapNotNull { it.toObject(Project::class.java) }
            
            if (query.isNotBlank()) {
                val lowercaseQuery = query.lowercase()
                projects = projects.filter { 
                    it.title.lowercase().contains(lowercaseQuery) || 
                    it.description.lowercase().contains(lowercaseQuery) 
                }
            }
            Resource.Success(projects)
        } catch (e: Exception) {
            val error = ErrorHandler.handle(e)
            Resource.Error(error.userMessage, error)
        }
    }

    override suspend fun getProject(projectId: String): Resource<Project> {
        return try {
            val snapshot = projectsCollection.document(projectId).get().await()
            val project = snapshot.toObject(Project::class.java)
            if (project != null) {
                Resource.Success(project)
            } else {
                Resource.Error("المشروع غير موجود")
            }
        } catch (e: Exception) {
            val error = ErrorHandler.handle(e)
            Resource.Error(error.userMessage, error)
        }
    }

    override suspend fun createProject(project: Project): Resource<String> {
        return try {
            val id = project.id.ifEmpty { UUID.randomUUID().toString() }
            val newProject = project.copy(
                id = id,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            projectsCollection.document(id).set(newProject).await()
            
            logActivity(ProjectActivity(
                projectId = id,
                userId = project.ownerId,
                type = ActivityType.CREATED,
                details = "تم إنشاء المشروع"
            ))
            
            Resource.Success(id)
        } catch (e: Exception) {
            val error = ErrorHandler.handle(e)
            Resource.Error(error.userMessage, error)
        }
    }

    override suspend fun updateProject(project: Project): Resource<Unit> {
        return try {
            val updatedProject = project.copy(
                updatedAt = System.currentTimeMillis()
                /* versioning handled via ProjectVersion entity */
            )
            projectsCollection.document(project.id).set(updatedProject).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            val error = ErrorHandler.handle(e)
            Resource.Error(error.userMessage, error)
        }
    }

    override suspend fun deleteProject(projectId: String): Resource<Unit> {
        return try {
            projectsCollection.document(projectId).update("status", ProjectStatus.DELETED.name).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            val error = ErrorHandler.handle(e)
            Resource.Error(error.userMessage, error)
        }
    }

    override suspend fun restoreProject(projectId: String): Resource<Unit> {
        return try {
            projectsCollection.document(projectId).update("status", ProjectStatus.DRAFT.name).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            val error = ErrorHandler.handle(e)
            Resource.Error(error.userMessage, error)
        }
    }

    override suspend fun archiveProject(projectId: String): Resource<Unit> {
        return try {
            projectsCollection.document(projectId).update(
                mapOf(
                    "status" to ProjectStatus.ARCHIVED.name,
                    "archivedAt" to System.currentTimeMillis()
                )
            ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            val error = ErrorHandler.handle(e)
            Resource.Error(error.userMessage, error)
        }
    }

    override suspend fun copyProject(projectId: String, newOwnerId: String): Resource<String> {
        return try {
            val snapshot = projectsCollection.document(projectId).get().await()
            val project = snapshot.toObject(Project::class.java)
                ?: return Resource.Error("المشروع غير موجود")
            
            val newId = UUID.randomUUID().toString()
            val copiedProject = project.copy(
                id = newId,
                ownerId = newOwnerId,
                title = project.title + " (نسخة)",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                status = ProjectStatus.DRAFT,
                archivedAt = null
            )
            
            projectsCollection.document(newId).set(copiedProject).await()
            
            logActivity(ProjectActivity(
                projectId = newId,
                userId = newOwnerId,
                type = ActivityType.CREATED,
                details = "تم نسخ المشروع من ${project.title}"
            ))
            
            Resource.Success(newId)
        } catch (e: Exception) {
            val error = ErrorHandler.handle(e)
            Resource.Error(error.userMessage, error)
        }
    }

    override suspend fun logActivity(activity: ProjectActivity): Resource<Unit> {
        return try {
            val id = activity.id.ifEmpty { UUID.randomUUID().toString() }
            val act = activity.copy(id = id, timestamp = System.currentTimeMillis())
            activitiesCollection.document(id).set(act).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            val error = ErrorHandler.handle(e)
            Resource.Error(error.userMessage, error)
        }
    }

    override suspend fun getProjectActivities(projectId: String): Resource<List<ProjectActivity>> {
        return try {
            val snapshot = activitiesCollection
                .whereEqualTo("projectId", projectId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().await()
            val activities = snapshot.documents.mapNotNull { it.toObject(ProjectActivity::class.java) }
            Resource.Success(activities)
        } catch (e: Exception) {
            val error = ErrorHandler.handle(e)
            Resource.Error(error.userMessage, error)
        }
    }

    override suspend fun createVersion(version: ProjectVersion): Resource<String> {
         return try {
            val id = version.id.ifEmpty { UUID.randomUUID().toString() }
            val ver = version.copy(id = id, createdAt = System.currentTimeMillis())
            versionsCollection.document(id).set(ver).await()
            
            // Update project with currentVersionId
            projectsCollection.document(ver.projectId).update("currentVersionId", id).await()
            
            logActivity(ProjectActivity(
                projectId = ver.projectId,
                userId = ver.createdBy,
                type = ActivityType.VERSION_CREATED,
                details = "تم إنشاء نسخة جديدة: ${ver.description}"
            ))
            
            Resource.Success(id)
        } catch (e: Exception) {
            val error = ErrorHandler.handle(e)
            Resource.Error(error.userMessage, error)
        }
    }

    override suspend fun getProjectVersions(projectId: String): Resource<List<ProjectVersion>> {
         return try {
            val snapshot = versionsCollection
                .whereEqualTo("projectId", projectId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
            val versions = snapshot.documents.mapNotNull { it.toObject(ProjectVersion::class.java) }
            Resource.Success(versions)
        } catch (e: Exception) {
            val error = ErrorHandler.handle(e)
            Resource.Error(error.userMessage, error)
        }
    }
}
