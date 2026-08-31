package com.siraj.app.domain.repository.adhkar

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.adhkar.AdhkarSettings
import com.siraj.app.domain.models.adhkar.DhikrCategory
import com.siraj.app.domain.models.adhkar.DhikrItem
import kotlinx.coroutines.flow.Flow

interface AdhkarRepository {
    suspend fun getCategories(): Resource<List<DhikrCategory>>

    suspend fun getAdhkarByCategory(categoryId: String): Resource<List<DhikrItem>>

    fun getSettings(): Flow<AdhkarSettings>

    suspend fun updateSettings(settings: AdhkarSettings)
}
