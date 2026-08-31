package com.siraj.app.mock

import com.siraj.app.domain.models.*

/**
 * بيانات تجريبية (Mock Data) لاختبار الواجهات فقط.
 * يجب عدم استخدامها في بيئة الإنتاج أو خلطها مع مستودعات (Repositories) Firebase.
 */
object MockData {
    val currentUser =
        UserProfile(
            id = "usr_1",
            name = "أحمد محمد",
            email = "ahmed@example.com",
        )

    val projects =
        listOf(
            ProjectPreview(
                id = "proj_1",
                title = "مشروع شرح الأربعين النووية",
                description = "سلسلة فيديوهات قصيرة تشرح الأحاديث بأسلوب مبسط.",
                lastModified = "منذ ساعتين",
            ),
            ProjectPreview(
                id = "proj_2",
                title = "مقاطع السيرة النبوية",
                description = "مشاهد مختارة من السيرة النبوية العطرة للشباب.",
                lastModified = "منذ يومين",
            ),
        )

    val videos =
        listOf(
            VideoPreview(
                id = "vid_1",
                title = "فضل الصدقة في رمضان",
                duration = "01:30",
                thumbnailUrl = null,
            ),
            VideoPreview(
                id = "vid_2",
                title = "كيف تحافظ على صلاة الفجر؟",
                duration = "02:15",
                thumbnailUrl = null,
            ),
        )

    val audios =
        listOf(
            AudioItem(
                id = "aud_1",
                title = "سورة الكهف",
                reciter = "مشاري العفاسي",
                duration = "35:10",
            ),
            AudioItem(
                id = "aud_2",
                title = "أذكار الصباح",
                reciter = "محمد جبريل",
                duration = "15:20",
            ),
        )

    val flashes =
        listOf(
            FlashItem(
                id = "flash_1",
                content = "قال رسول الله ﷺ: (الطهور شطر الإيمان).",
                author = "رواه مسلم",
                timestamp = "منذ ساعة",
            ),
            FlashItem(
                id = "flash_2",
                content = "من لزم الاستغفار جعل الله له من كل هم فرجا، ومن كل ضيق مخرجا.",
                author = "رواه أبو داود",
                timestamp = "منذ 3 ساعات",
            ),
        )

    val sources =
        listOf(
            SourcePreview(
                id = "src_1",
                title = "صحيح البخاري",
                author = "محمد بن إسماعيل البخاري",
                verificationStatus = VerificationStatus.VERIFIED,
            ),
            SourcePreview(
                id = "src_2",
                title = "تفسير ابن كثير",
                author = "إسماعيل بن عمر بن كثير",
                verificationStatus = VerificationStatus.PENDING,
            ),
        )

    val subscription =
        SubscriptionPreview(
            id = "sub_1",
            planName = "الباقة الأساسية",
            status = "نشط",
            expiryDate = "2026-12-31",
        )
}
