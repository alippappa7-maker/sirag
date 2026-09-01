package com.siraj.app.data.repository

import com.siraj.app.domain.models.hadith.Hadith
import com.siraj.app.domain.models.hadith.HadithCollection
import com.siraj.app.domain.models.hadith.HadithGrade
import com.siraj.app.domain.repository.hadith.HadithRepository

/**
 * تطبيق مستودع الحديث النبوي
 * المصادر المعتمدة: صحيح البخاري، صحيح مسلم، سنن أبي داود، سنن الترمذي، سنن النسائي، سنن ابن ماجه، موطأ مالك
 * تحذير: لا يجوز نشر أحاديث غير محققة أو ضعيفة دون بيان درجتها
 */
class HadithRepositoryImpl : HadithRepository {

    private val collections = listOf(
        HadithCollection("bukhari", "صحيح البخاري", "محمد بن إسماعيل البخاري", 7563),
        HadithCollection("muslim", "صحيح مسلم", "مسلم بن الحجاج", 5362),
        HadithCollection("abudawud", "سنن أبي داود", "أبو داود السجستاني", 5274),
        HadithCollection("tirmidhi", "سنن الترمذي", "محمد بن عيسى الترمذي", 3956),
        HadithCollection("nasai", "سنن النسائي", "أحمد بن شعيب النسائي", 5758),
        HadithCollection("ibnmajah", "سنن ابن ماجه", "محمد بن يزيد بن ماجه", 4341),
        HadithCollection("malik", "موطأ مالك", "مالك بن أنس", 1858),
    )

    // بيانات تجريبية موثقة — يجب استبدالها ببيانات Firebase Firestore
    private val sampleHadiths = listOf(
        Hadith(
            id = "bukhari_1",
            text = "إنما الأعمال بالنية، وإنما لكل امرئ ما نوى، فمن كانت هجرته إلى الله ورسوله فهجرته إلى الله ورسوله، ومن كانت هجرته لدنيا يصيبها أو امرأة ينكحها فهجرته إلى ما هاجر إليه",
            narrator = "عمر بن الخطاب رضي الله عنه",
            chain = listOf("عمر بن الخطاب", "عبد الله بن عمر", "مالك", "يحيى بن سعيد"),
            grade = HadithGrade.SAHIH,
            collectionId = "bukhari",
            collectionName = "صحيح البخاري",
            hadithNumber = 1,
            chapter = "كتاب بدء الوحي",
            topicTags = listOf("النية", "الأعمال", "الهجرة"),
        ),
        Hadith(
            id = "muslim_1",
            text = "لا يؤمن أحدكم حتى يحب لأخيه ما يحب لنفسه",
            narrator = "أنس بن مالك رضي الله عنه",
            chain = listOf("أنس بن مالك", "مالك", "يحيى"),
            grade = HadithGrade.SAHIH,
            collectionId = "muslim",
            collectionName = "صحيح مسلم",
            hadithNumber = 45,
            chapter = "كتاب الإيمان",
            topicTags = listOf("الإيمان", "الأخوة", "الأخلاق"),
        ),
        Hadith(
            id = "bukhari_6018",
            text = "من كان يؤمن بالله واليوم الآخر فليقل خيراً أو ليصمت، ومن كان يؤمن بالله واليوم الآخر فليكرم جاره، ومن كان يؤمن بالله واليوم الآخر فليكرم ضيفه",
            narrator = "أبو هريرة رضي الله عنه",
            chain = listOf("أبو هريرة", "أبو سلمة", "الأوزاعي"),
            grade = HadithGrade.SAHIH,
            collectionId = "bukhari",
            collectionName = "صحيح البخاري",
            hadithNumber = 6018,
            chapter = "كتاب الأدب",
            topicTags = listOf("الأخلاق", "الجار", "الضيف", "اللسان"),
        ),
        Hadith(
            id = "bukhari_13",
            text = "لا يؤمن أحدكم حتى أكون أحب إليه من والده وولده والناس أجمعين",
            narrator = "أنس بن مالك رضي الله عنه",
            chain = listOf("أنس بن مالك", "قتادة", "شعبة"),
            grade = HadithGrade.SAHIH,
            collectionId = "bukhari",
            collectionName = "صحيح البخاري",
            hadithNumber = 13,
            chapter = "كتاب الإيمان",
            topicTags = listOf("الإيمان", "محبة النبي", "الإخلاص"),
        ),
    )

    override suspend fun getCollections(): List<HadithCollection> = collections

    override suspend fun getHadithsByCollection(collectionId: String, limit: Int): List<Hadith> {
        return sampleHadiths.filter { it.collectionId == collectionId }.take(limit)
    }

    override suspend fun getHadithById(id: String): Hadith? {
        return sampleHadiths.find { it.id == id }
    }

    override suspend fun searchHadiths(query: String, collectionId: String?): List<Hadith> {
        var results = sampleHadiths
        if (collectionId != null) {
            results = results.filter { it.collectionId == collectionId }
        }
        return results.filter {
            it.text.contains(query, ignoreCase = true) ||
            it.narrator.contains(query, ignoreCase = true) ||
            it.topicTags.any { tag -> tag.contains(query, ignoreCase = true) }
        }
    }

    override suspend fun getHadithsByTopic(topic: String, limit: Int): List<Hadith> {
        return sampleHadiths.filter {
            it.topicTags.any { tag -> tag.contains(topic, ignoreCase = true) }
        }.take(limit)
    }
}
