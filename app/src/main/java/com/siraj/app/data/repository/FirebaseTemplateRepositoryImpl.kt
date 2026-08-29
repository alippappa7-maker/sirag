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
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : TemplateRepository {

    private val templatesCol = firestore.collection("templates")
    private val favoritesCol = firestore.collection("template_favorites")

    override fun getActiveTemplates(): Flow<Resource<List<ContentTemplate>>> = callbackFlow {
        trySend(Resource.Loading)
        val registration = templatesCol
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

    override fun getFavoriteTemplates(userId: String): Flow<Resource<List<String>>> = callbackFlow {
        val registration = favoritesCol
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

    override suspend fun toggleFavorite(userId: String, templateId: String, isFavorite: Boolean): Resource<Unit> {
        return try {
            val favId = "${userId}_${templateId}"
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
    }

    override suspend fun seedDefaultTemplates(): Resource<Unit> {
        return try {
            val defaultTemplates = listOf(
                ContentTemplate(id = "tpl_aya_1", name = "آية وتدبر", description = "عرض آية قرآنية مع تدبر أو تفسير مبسط", targetAudience = "عام", recommendedDuration = "قصير (أقل من دقيقة)", sceneStyle = "موشن جرافيك", hasQuran = true, requiredFields = listOf("الآية", "التدبر")),
                ContentTemplate(id = "tpl_hadith_1", name = "حديث موثق", description = "نشر حديث نبوي مع شرحه وتخريجه", targetAudience = "عام", recommendedDuration = "قصير (أقل من دقيقة)", sceneStyle = "موشن جرافيك", hasHadith = true, requiredFields = listOf("نص الحديث", "التخريج")),
                ContentTemplate(id = "tpl_story_1", name = "قصة من السيرة", description = "سرد قصة قصيرة من سيرة النبي أو الصحابة", targetAudience = "شباب", recommendedDuration = "متوسط (1-3 دقائق)", sceneStyle = "وايت بورد", requiredFields = listOf("القصة", "العبرة المستفادة")),
                ContentTemplate(id = "tpl_dua_1", name = "دعاء موثق", description = "تصميم روحاني لدعاء من الكتاب أو السنة", targetAudience = "عام", recommendedDuration = "قصير (أقل من دقيقة)", sceneStyle = "تصوير حي", hasQuran = false, hasHadith = true, requiredFields = listOf("نص الدعاء")),
                ContentTemplate(id = "tpl_reminder_1", name = "تذكير قصير", description = "خاطرة سريعة أو تذكير بعمل صالح", targetAudience = "عام", recommendedDuration = "قصير (أقل من دقيقة)", sceneStyle = "موشن جرافيك", requiredFields = listOf("نص التذكير")),
                ContentTemplate(id = "tpl_qa_1", name = "سؤال وجواب", description = "طرح سؤال شرعي أو فقهي والإجابة عليه", targetAudience = "عام", recommendedDuration = "متوسط (1-3 دقائق)", sceneStyle = "موشن جرافيك", hasFatwa = true, requiredFields = listOf("السؤال", "الإجابة")),
                ContentTemplate(id = "tpl_summary_1", name = "ملخص درس", description = "تلخيص لأهم النقاط من درس أو خطبة", targetAudience = "أكاديمي/متخصص", recommendedDuration = "طويل (أكثر من 3 دقائق)", sceneStyle = "موشن جرافيك", requiredFields = listOf("عنوان الدرس", "النقاط الرئيسية")),
                ContentTemplate(id = "tpl_kids_1", name = "درس للأطفال", description = "محتوى مبسط ومرح موجه للأطفال", targetAudience = "أطفال", recommendedDuration = "متوسط (1-3 دقائق)", sceneStyle = "موشن جرافيك", requiredFields = listOf("القيم المستهدفة")),
                ContentTemplate(id = "tpl_mosque_1", name = "إعلان مسجد أو مركز", description = "إعلان عن فعالية أو دورة في مسجد", targetAudience = "عام", recommendedDuration = "قصير (أقل من دقيقة)", sceneStyle = "نصي فقط", requiredFields = listOf("الفعالية", "الزمان", "المكان")),
                ContentTemplate(id = "tpl_scholar_1", name = "بطاقة كتاب أو عالم", description = "تعريف بكتاب إسلامي أو نبذة عن عالم", targetAudience = "أكاديمي/متخصص", recommendedDuration = "قصير (أقل من دقيقة)", sceneStyle = "موشن جرافيك", requiredFields = listOf("اسم الكتاب/العالم", "النبذة"))
            )
            
            firestore.runBatch { batch ->
                defaultTemplates.forEach { tpl ->
                    batch.set(templatesCol.document(tpl.id), tpl)
                }
            }.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            val error = ErrorHandler.handle(e)
            Resource.Error(error.userMessage, error)
        }
    }

    override suspend fun createTemplate(template: ContentTemplate): Resource<String> {
        return try {
            val id = if (template.id.isBlank()) UUID.randomUUID().toString() else template.id
            val newTemplate = template.copy(id = id, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())
            templatesCol.document(id).set(newTemplate).await()
            Resource.Success(id)
        } catch (e: Exception) {
            val error = ErrorHandler.handle(e)
            Resource.Error(error.userMessage, error)
        }
    }

    override suspend fun updateTemplateStatus(templateId: String, status: TemplateStatus): Resource<Unit> {
        return try {
            templatesCol.document(templateId).update(
                "status", status.name,
                "updatedAt", System.currentTimeMillis()
            ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            val error = ErrorHandler.handle(e)
            Resource.Error(error.userMessage, error)
        }
    }

    override suspend fun updateTemplate(template: ContentTemplate): Resource<Unit> {
        return try {
            templatesCol.document(template.id).set(template).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            val error = ErrorHandler.handle(e)
            Resource.Error(error.userMessage, error)
        }
    }
}
