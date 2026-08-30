# سراج (Siraj)

## حالة التنفيذ
مكتمل بنجاح للمرحلة الحالية - تم إعداد وتطبيق خطة التشغيل والتحديثات الشاملة لما بعد الإطلاق (Operations Plan - First 90 Days) وخارطة طريق الـ 90 يوماً الأولى (Post-Launch Roadmap)، متضمنةً محرك حوكمة التشغيل والإصدارات الطارئة (OperationsGovernanceEngine)، سياسات الـ Hotfix والـ Rollback، مصفوفة مراقبة المحاور الـ 14 الإلزامية، حوكمة Feature Flags والأسرار، مراجعات FinOps والمحتوى والأمان الشهرية، وسياسة التوافقية العكسية الصارمة.

## آخر برومبت منفذ
رقم البرومبت: PROMPT 084
المرحلة: التشغيل والتحديثات (Operations and Post-Launch Roadmap)
الحالة: جاهز ومكتمل ومختبر بنجاح تام

## التقنية
- Kotlin & Jetpack Compose (Material 3)
- Firebase Firestore, Cloud Functions, Firebase Storage, Firebase Remote Config
- Architecture: MVVM, Clean Architecture, Repository Pattern, Operations Governance Engine
- Testing: JUnit4, MockK, Coroutines Test
- Compliance: Play Console & App Store Lifecycle Guidelines, RICE Scoring Framework, FinOps Cloud Principles

## بنية الوحدات
- `core/operations/`: محرك حوكمة العمليات والتحديثات `OperationsGovernanceEngine` لإدارة شروط الـ Hotfix، تقييم RICE للباكلوج، تدقيق التكاليف وتدوير الأسرار والتوافقية العكسية.
- `domain/models/operations/`: نماذج بيانات التشغيل `OperationsModels.kt` (المراحل، تصنيف الطوارئ، التدوير، مراجعات FinOps والأمان).
- `OPERATIONS_PLAN.md`: الخطة التشغيلية المعيارية للـ 90 يوماً الأولى وقواعد المراقبة والتحكم.
- `POST_LAUNCH_ROADMAP.md`: خارطة طريق ما بعد الإطلاق والمراحل الخمس ومصفوفة الـ OKRs.
- `features/moderation/`: لوحة تحكم المشرفين وشاشات وقواعد المجتمع.
- `features/flashes/`: شاشة النشر `FlashPublishingScreen` مع التحقق من شروط الاستخدام والفحص الأمني الاستباقي.
- `data/repository/community/`: مستودع الأمان والمشرفين `FirebaseSafetyRepositoryImpl`.

## الخدمات المربوطة
- Google Play Console، Google Cloud Secret Manager، Firebase Crashlytics، Firebase Remote Config، Firebase App Check، Cloud Firestore، Firebase Storage.

## البيئة الحالية
- Development / Staging / Production Ready.

## المخاطر المعروفة
- تقلبات استهلاك تكلفة الذكاء الاصطناعي، يتم تغطيتها بنظام تنبيهات 50%/80%/100% وخطة الإيقاف التلقائي للميزات المكلفة (Kill-switch).
- راجع `KNOWN_LIMITATIONS.md` و `OPERATIONS_PLAN.md`.

## القرارات التقنية
- لا تضاف أي ميزة لمجرد طلب فردي منعزل؛ تطبيق إلزامي لنموذج RICE واشتراط تكرار الطلب (≥3).
- التوافقية العكسية إلزامية؛ لا تكسر المشاريع القديمة للمستخدمين أبداً مع توفير ترحيل تصاعدي آمن للمخطط.
- منع التعديل الصامت على أي محتوى شرعي منشور واشتراط توثيق التصحيح واعتماده من المراجع.
- عزل مفاتيح الأمان والـ Feature Flags الحساسة في الـ Backend ومنع تعديلها من تطبيق العميل.
- الالتزام بدورة الإصدارات المنضبطة (Staging -> Beta -> Phased Rollout).

## الخطوة التالية
بانتظار تعليمات المرحلة القادمة (PROMPT التالي).
