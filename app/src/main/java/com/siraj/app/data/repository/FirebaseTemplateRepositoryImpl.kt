package com.siraj.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.siraj.app.core.utils.Resource
import com.siraj.app.core.error.ErrorHandler
import com.siraj.app.domain.models.ContentTemplate
import com.siraj.app.domain.models.TemplateFavorite
import com.siraj.app.domain.models.TemplateStatus
import com.siraj.app.domain.repository.TemplateRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseTemplateRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : TemplateRepository {
    private val templatesCol = firestore.collection("templates")
    private val favoritesCol = firestore.collection("template_favorites")

    override fun getActiveTemplates(): Flow<Resource<List<ContentTemplate>>> =
        callbackFlow {
            trySend(Resource.Loading)
            val registration =
                templatesCol
                    .whereEqualTo("status", TemplateStatus.ACTIVE.name)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(Resource.Error(error.localizedMessage ?: "Failed to fetch templates"))
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val templates = snapshot.documents.mapNotNull { it.toObject(ContentTemplate::class.java) }
                            trySend(Resource.Success(templates))
                        }
                    }
            awaitClose { registration.remove() }
        }

    override fun getFavoriteTemplates(userId: String): Flow<Resource<List<String>>> =
        callbackFlow {
            val registration =
                favoritesCol
                    .whereEqualTo("userId", userId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(Resource.Error(error.localizedMessage ?: "Failed to fetch favorites"))
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val templateIds = snapshot.documents.mapNotNull { it.getString("templateId") }
                            trySend(Resource.Success(templateIds))
                        }
                    }
            awaitClose { registration.remove() }
        }

    override suspend fun toggleFavorite(
        userId: String,
        templateId: String,
        isFavorite: Boolean,
    ): Resource<Unit> =
        try {
            val favId = "${userId}_$templateId"
            if (isFavorite) {
                val favorite = TemplateFavorite(id = favId, userId = userId, templateId = templateId)
                favoritesCol.document(favId).set(favorite).await()
            } else {
                favoritesCol.document(favId).delete().await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            val error = ErrorHandler.handle(e)
            Resource.Error(error.userMessage, error)
        }

    override suspend fun seedDefaultTemplates(): Resource<Unit> =
        try {
            val defaultTemplates = emptyList<ContentTemplate>()

            firestore
                .runBatch { batch ->
                    defaultTemplates.forEach { tpl ->
                        batch.set(templatesCol.document(tpl.id), tpl)
                    }
                }.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            val error = ErrorHandler.handle(e)
            Resource.Error(error.userMessage, error)
        }

    override suspend fun createTemplate(template: ContentTemplate): Resource<String> =
        try {
            val id = if (template.id.isBlank()) UUID.randomUUID().toString() else template.id
            val newTemplate = template.copy(id = id, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())
            templatesCol.document(id).set(newTemplate).await()
            Resource.Success(id)
        } catch (e: Exception) {
            val error = ErrorHandler.handle(e)
            Resource.Error(error.userMessage, error)
        }

    override suspend fun updateTemplateStatus(
        templateId: String,
        status: TemplateStatus,
    ): Resource<Unit> =
        try {
            templatesCol
                .document(templateId)
                .update(
                    "status",
                    status.name,
                    "updatedAt",
                    System.currentTimeMillis(),
                ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            val error = ErrorHandler.handle(e)
            Resource.Error(error.userMessage, error)
        }

    override suspend fun updateTemplate(template: ContentTemplate): Resource<Unit> =
        try {
            templatesCol.document(template.id).set(template).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            val error = ErrorHandler.handle(e)
            Resource.Error(error.userMessage, error)
        }
}
