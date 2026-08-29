# سراج (Siraj)

## حالة التنفيذ
مكتمل بنجاح للمرحلة الحالية - تم تنفيذ نظام إدارة الحقوق والتراخيص (Rights Management) بالكامل، وإدراج شاشات المراجعة وحالات الحقوق للأصول لمنع التصدير قبل التوثيق.

## آخر برومبت منفذ
رقم البرومبت: PROMPT 081
المرحلة: إدارة الحقوق والتراخيص
الحالة: جاهز ومكتمل

## التقنية
- Kotlin & Jetpack Compose (Material 3)
- Firebase Firestore, Cloud Functions, Firebase Remote Config
- Architecture: MVVM, Clean Architecture, Repository Pattern
- Testing: JUnit4, MockK, Coroutines Test
- Play Console Tracks (Staged Rollout).

## بنية الوحدات 
- `features/rights/`: مدير ونماذج وشاشات حقوق الملكية والتراخيص لكل Asset.
- `RIGHTS_POLICY.md`: سياسة التعامل مع التراخيص والإثباتات الداخلية.
- بقية وثائق الإصدار النهائي (Release Notes, Production Checklist, الخ).

## الخدمات المربوطة
- Google Play Console، Firebase Crashlytics، Firebase Remote Config، Firebase App Check، Cloud Firestore، Firebase Storage.

## البيئة الحالية
- Production (الإصدار الأول).

## المخاطر المعروفة
- الارتفاع غير المتوقع في تكلفة الـ AI، يتم إيقافه عبر Feature Flag.
- راجع `KNOWN_LIMITATIONS.md`.

## القرارات التقنية
- يجب أن يكون `rightsStatus` معتمداً (`COMMERCIAL_ALLOWED` أو مشابه) ليتمكن المستخدم من التصدير.
- لا تحذف إثباتات الترخيص ولا تحفظ في مستودعات عامة، بل تدون في سجل آمن `rights_decisions`.

## الخطوة التالية
بانتظار تعليمات المرحلة القادمة أو مراجعات الإطلاق النهائي.
