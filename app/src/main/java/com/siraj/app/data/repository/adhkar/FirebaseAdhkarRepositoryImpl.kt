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

    private val defaultCategories = listOf(
        DhikrCategory("morning", "أذكار الصباح", "wb_sunny"),
        DhikrCategory("evening", "أذكار المساء", "nights_stay"),
        DhikrCategory("sleep", "أذكار النوم", "bedtime"),
        DhikrCategory("after_prayer", "أذكار بعد الصلاة", "mosque"),
        DhikrCategory("waking_up", "أذكار الاستيقاظ", "alarm")
    )

    private val defaultAdhkar = mapOf(
        "morning" to listOf(
            DhikrItem(
                id = "m_1",
                categoryId = "morning",
                text = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ.",
                requiredCount = 1,
                source = "صحيح مسلم",
                narrator = "عبد الله بن مسعود",
                grade = "صحيح",
                verificationStatus = VerificationStatus.APPROVED
            ),
            DhikrItem(
                id = "m_2",
                categoryId = "morning",
                text = "اللَّهُمَّ أَنْتَ رَبِّي لاَ إِلَهَ إِلاَّ أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لاَ يَغْفِرُ الذُّنُوبَ إِلاَّ أَنْتَ.",
                requiredCount = 1,
                source = "صحيح البخاري",
                narrator = "شداد بن أوس",
                grade = "سيد الاستغفار - صحيح",
                verificationStatus = VerificationStatus.APPROVED
            ),
            DhikrItem(
                id = "m_3",
                categoryId = "morning",
                text = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ.",
                requiredCount = 3,
                source = "صحيح مسلم",
                narrator = "جويرية بنت الحارث",
                grade = "صحيح",
                verificationStatus = VerificationStatus.APPROVED
            )
        ),
        "evening" to listOf(
            DhikrItem(
                id = "e_1",
                categoryId = "evening",
                text = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ.",
                requiredCount = 1,
                source = "صحيح مسلم",
                narrator = "عبد الله بن مسعود",
                grade = "صحيح",
                verificationStatus = VerificationStatus.APPROVED
            ),
            DhikrItem(
                id = "e_2",
                categoryId = "evening",
                text = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ.",
                requiredCount = 3,
                source = "صحيح مسلم",
                narrator = "أبو هريرة",
                grade = "صحيح",
                verificationStatus = VerificationStatus.APPROVED
            )
        )
    )

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
