# سراج (Siraj)

## حالة التنفيذ
مكتمل بنجاح للمرحلة الحالية - تم إجراء تدقيق أمني شامل (Security Audit) على التطبيق والخادم. تم تأمين قواعد البيانات، وإغلاق الثغرات المحتملة في إدارة مساحات العمل ومخزن الملفات، وتفعيل App Check لحماية واجهات Firebase. التطبيق الآن جاهز تقنياً وأمنياً للإطلاق.

## آخر prompt منفذ
رقم البرومبت: PROMPT 069
اسم المرحلة: تدقيق الأمان

## المرحلة الحالية
- فحص الأسرار في المستودع وسجلات git (نظيفة، تستخدم قيم وهمية).
- مراجعة وتحديث `firestore.rules` و `storage.rules` (إصلاح ثغرات إضافة الأعضاء ورفع الملفات).
- تفعيل `FirebaseAppCheck` مع `PlayIntegrity` في أندرويد.
- التأكد من أمان عمليات الخصم والأرصدة داخل Cloud Functions.
- إنشاء تقرير `SECURITY_REVIEW.md` الموثق.

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

