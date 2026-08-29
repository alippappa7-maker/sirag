# سراج (Siraj)

## حالة التنفيذ
مكتمل بنجاح للمرحلة الحالية - تم إنشاء خطة اختبار قبول شاملة (UAT & Pre-release Test Plan) تغطي كافة رحلات المستخدم الأساسية (Core User Journeys) وحالات الاستخدام الاستثنائية والوصول. الخطة موثقة وجاهزة للتنفيذ اليدوي لضمان خلو التطبيق من العيوب الحرجة قبل نشره في المتاجر.

## آخر prompt منفذ
رقم البرومبت: PROMPT 068
اسم المرحلة: اختبار ما قبل النشر

## المرحلة الحالية
تم إنجاز التجهيزات النهائية للاختبارات:
1. **وثيقة خطة الاختبار (TEST_PLAN.md)**:
   - تفصيل سيناريوهات الاختبار لكافة أقسام التطبيق: التسجيل، مساحة العمل، المراجعة الشرعية، المحراب، الومضات، إعدادات الخصوصية، والاشتراكات الوهمية (Sandbox).
   - تضمين اختبارات حالات الحافة (Edge Cases): ضعف الشبكة، وضع عدم الاتصال (Offline)، تغيير اللغة والثيم، وتحديث التطبيق.
   - تضمين متطلبات إمكانية الوصول: توافقية قارئات الشاشة وتكبير الخطوط.
2. **تصنيف العيوب**:
   - تحديد وتصنيف الأخطاء (Blocker, Critical, Major, Minor) لضمان عدم إطلاق التطبيق بوجود أخطاء تمنع العمليات الأساسية.
3. **التجهيز للعملية اليدوية**:
   - الخطة مهيأة ليتم استخدامها من قبل المراجعين ومختبري الجودة، مع سجل فارغ لرصد الأخطاء وإصلاحها لاحقاً.

## التقنية
- Kotlin & Jetpack Compose
- Firebase Authentication, Cloud Firestore, Cloud Storage
- SHA-256 Security Hashes & Clean Data Sanitization
- FileProvider URI Sharing for Android
- Architecture: MVVM, Clean Architecture, Repository Pattern
- Testing: Robolectric, JUnit4, MockK

## بنية الوحدات
- `app/src/main/java/com/siraj/app/`
  - `core/privacy`:
    - `PrivacyManager.kt`: تطهير البيانات، حساب بصمات التشفير، وصياغة حزم التصدير.
  - `domain/models/privacy`:
    - `PrivacyModels.kt`: نماذج حزمة التصدير، طلبات الحذف والتصحيح، وفئات سياسات الاحتفاظ.
  - `domain/repository/privacy`:
    - `PrivacyRepository.kt`: واجهة مستودع الخصوصية.
  - `data/repository`:
    - `FirebasePrivacyRepositoryImpl.kt`: تنفيذ التعامل مع Firestore والتخزين وتوليد الملفات.
  - `features/settings/presentation/privacy`:
    - `PrivacyCenterScreen.kt`: واجهة مركز الخصوصية والتحكم التفاعلي بالبيانات.
    - `PrivacyCenterViewModel.kt`: إدارة حالات الخصوصية والحذف والتصدير والتفريغ.
  - `features/settings/presentation`:
    - `SettingsScreen.kt` & `SettingsPages.kt`: ربط مركز الخصوصية الجديد بلوحة الإعدادات والتخزين.

## الخدمات المربوطة
- Firebase Auth & Cloud Firestore
- Android FileProvider
- Firebase Crashlytics & Analytics (مع الالتزام بعدم تسجيل أو طبع الأسرار أو البيانات الحساسة)

## البيئة الحالية
- Development / Staging / Production

## المخاطر المعروفة
- الحذف النهائي من الخادم بعد انقضاء فترة السماح 14 يوماً إجراء غير قابل للتراجع ويجب توضيح ذلك جلياً للمستخدم قبل التأكيد.

## القرارات التقنية
- حجب كافة المفاتيح الأمنية وكلمات المرور والرموز المالية الخام تلقائياً أثناء تجهيز حزمة التصدير لضمان الأمان الأقصى للمستخدم.
- استخدام `SHA-256` لتوفير الموثوقية وتسهيل تحقق المستخدم من أن ملف التصدير لم يتعرض لأي تعديل أو تغيير.

## الخطوة التالية
المضي قدماً في البرومبتات والمراحل التالية بحسب خطة المشروع.

