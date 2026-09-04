package com.siraj.app.data.repository.adhkar

import com.google.firebase.firestore.FirebaseFirestore
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.adhkar.AdhkarSettings
import com.siraj.app.domain.models.adhkar.DhikrCategory
import com.siraj.app.domain.models.adhkar.DhikrItem
import com.siraj.app.domain.models.adhkar.VerificationStatus
import com.siraj.app.domain.repository.adhkar.AdhkarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await

class FirebaseAdhkarRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AdhkarRepository {

    private val settingsFlow = MutableStateFlow(AdhkarSettings())

    private val defaultCategories = emptyList<DhikrCategory>()

    private val defaultAdhkar = emptyMap<String, List<DhikrItem>>()

    override suspend fun getCategories(): Resource<List<DhikrCategory>> {
        return try {
            val snapshot = firestore.collection("adhkar_categories").get().await()
            if (!snapshot.isEmpty) {
                val list = snapshot.documents.mapNotNull { doc ->
                    val id = doc.id
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val iconName = doc.getString("iconName") ?: "wb_sunny"
                    DhikrCategory(id, name, iconName)
                }
                Resource.Success(list)
            } else {
                Resource.Success(defaultCategories)
            }
        } catch (e: Exception) {
            Resource.Success(defaultCategories)
        }
    }

    override suspend fun getAdhkarByCategory(categoryId: String): Resource<List<DhikrItem>> {
        return try {
            val snapshot = firestore.collection("adhkar")
                .whereEqualTo("categoryId", categoryId)
                .get()
                .await()
            if (!snapshot.isEmpty) {
                val list = snapshot.documents.mapNotNull { doc ->
                    val id = doc.id
                    val text = doc.getString("text") ?: return@mapNotNull null
                    val count = doc.getLong("requiredCount")?.toInt() ?: 1
                    val source = doc.getString("source") ?: ""
                    val narrator = doc.getString("narrator")
                    val grade = doc.getString("grade")
                    val statusStr = doc.getString("verificationStatus") ?: "APPROVED"
                    val status = try { VerificationStatus.valueOf(statusStr) } catch (e: Exception) { VerificationStatus.APPROVED }
                    DhikrItem(id, categoryId, text, count, source, narrator, grade, status)
                }
                Resource.Success(list)
            } else {
                val fallback = defaultAdhkar[categoryId] ?: emptyList()
                Resource.Success(fallback)
            }
        } catch (e: Exception) {
            val fallback = defaultAdhkar[categoryId] ?: emptyList()
            Resource.Success(fallback)
        }
    }

    override fun getSettings(): Flow<AdhkarSettings> = settingsFlow

    override suspend fun updateSettings(settings: AdhkarSettings) {
        settingsFlow.value = settings
    }
}
