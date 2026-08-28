package com.siraj.app.data.repository.adhkar

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.adhkar.AdhkarSettings
import com.siraj.app.domain.models.adhkar.DhikrCategory
import com.siraj.app.domain.models.adhkar.DhikrItem
import com.siraj.app.domain.models.adhkar.VerificationStatus
import com.siraj.app.domain.repository.adhkar.AdhkarRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class AdhkarRepositoryImpl : AdhkarRepository {

    private val settingsFlow = MutableStateFlow(AdhkarSettings())

    private val categories = listOf(
        DhikrCategory("morning", "أذكار الصباح", "wb_sunny"),
        DhikrCategory("evening", "أذكار المساء", "nights_stay"),
        DhikrCategory("after_prayer", "أذكار بعد الصلاة", "mosque"),
        DhikrCategory("sleep", "أذكار النوم", "bedtime"),
        DhikrCategory("waking", "أذكار الاستيقاظ", "alarm"),
        DhikrCategory("travel", "أذكار السفر", "flight"),
        DhikrCategory("food", "أذكار الطعام", "restaurant")
    )

    private val mockAdhkar = listOf(
        DhikrItem(
            id = "m1",
            categoryId = "morning",
            text = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ لا إِلَهَ إِلا اللَّهُ، وَحْدَهُ لا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَذَا الْيَوْمِ وَخَيْرَ مَا بَعْدَهُ، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَذَا الْيَوْمِ وَشَرِّ مَا بَعْدَهُ، رَبِّ أَعُوذُ بِكَ مِنَ الْكَسَلِ وَسُوءِ الْكِبَرِ، رَبِّ أَعُوذُ بِكَ مِنْ عَذَابٍ فِي النَّارِ وَعَذَابٍ فِي الْقَبْرِ.",
            requiredCount = 1,
            source = "صحيح مسلم",
            narrator = "عبد الله بن مسعود رضي الله عنه",
            grade = "صحيح",
            verificationStatus = VerificationStatus.APPROVED
        ),
        DhikrItem(
            id = "m2",
            categoryId = "morning",
            text = "اللَّهُمَّ أَنْتَ رَبِّي لا إِلَهَ إِلا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ لَكَ بِذَنْبِي فَاغْفِرْ لِي، فَإِنَّهُ لا يَغْفِرُ الذُّنُوبَ إِلا أَنْتَ.",
            requiredCount = 1,
            source = "صحيح البخاري",
            narrator = "شداد بن أوس رضي الله عنه",
            grade = "صحيح (سيد الاستغفار)",
            verificationStatus = VerificationStatus.APPROVED
        ),
        DhikrItem(
            id = "e1",
            categoryId = "evening",
            text = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ...",
            requiredCount = 1,
            source = "صحيح مسلم",
            narrator = "عبد الله بن مسعود رضي الله عنه",
            grade = "صحيح",
            verificationStatus = VerificationStatus.APPROVED
        ),
        DhikrItem(
            id = "s1",
            categoryId = "sleep",
            text = "بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي، وَبِكَ أَرْفَعُهُ، إِنْ أَمْسَكْتَ نَفْسِي فَارْحَمْهَا، وَإِنْ أَرْسَلْتَهَا فَاحْفَظْهَا بِمَا تَحْفَظُ بِهِ عِبَادَكَ الصَّالِحِينَ.",
            requiredCount = 1,
            source = "صحيح البخاري ومسلم",
            narrator = "أبو هريرة رضي الله عنه",
            grade = "صحيح",
            verificationStatus = VerificationStatus.APPROVED
        ),
        DhikrItem(
            id = "s2",
            categoryId = "sleep",
            text = "دعاء عام غير موثق لتجربة حالة المراجعة...",
            requiredCount = 1,
            source = "غير محدد",
            narrator = null,
            grade = null,
            verificationStatus = VerificationStatus.PENDING_REVIEW
        )
    )

    override suspend fun getCategories(): Resource<List<DhikrCategory>> {
        delay(300)
        return Resource.Success(categories)
    }

    override suspend fun getAdhkarByCategory(categoryId: String): Resource<List<DhikrItem>> {
        delay(300)
        // Filter only approved ones as per requirement: "لا يظهر محتوى غير معتمد في القائمة العامة"
        val filtered = mockAdhkar.filter { it.categoryId == categoryId && it.verificationStatus == VerificationStatus.APPROVED }
        return Resource.Success(filtered)
    }

    override fun getSettings(): Flow<AdhkarSettings> = settingsFlow

    override suspend fun updateSettings(settings: AdhkarSettings) {
        settingsFlow.value = settings
    }
}
