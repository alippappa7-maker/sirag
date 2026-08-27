package com.siraj.app.domain.repository

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.ContentTemplate
import com.siraj.app.domain.models.TemplateStatus
import kotlinx.coroutines.flow.Flow

interface TemplateRepository {
    fun getActiveTemplates(): Flow<Resource<List<ContentTemplate>>>
    fun getFavoriteTemplates(userId: String): Flow<Resource<List<String>>> // Returns list of template IDs
    
    suspend fun toggleFavorite(userId: String, templateId: String, isFavorite: Boolean): Resource<Unit>
    suspend fun seedDefaultTemplates(): Resource<Unit>
    
    // Admin functions
    suspend fun createTemplate(template: ContentTemplate): Resource<String>
    suspend fun updateTemplateStatus(templateId: String, status: TemplateStatus): Resource<Unit>
    suspend fun updateTemplate(template: ContentTemplate): Resource<Unit>
}
