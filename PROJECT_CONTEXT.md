# سراج (Siraj)

## حالة التنفيذ
مكتمل بنجاح للمرحلة الحالية - تم بناء وتفعيل مركز الخصوصية الشامل وإدارة حقوق البيانات (Privacy Center & User Rights Management) في منصة "سراج" وفق معايير الخصوصية الصارمة (GDPR/CCPA compliant)، بما يشمل حق التصدير الشامل للبيانات بنسق JSON مع SHA-256 Checksum والتوقيع الرقمي، وحق الحذف النهائي للحساب مع فترة السماح الأمان (14 يوماً)، وإدارة تفريغ التخزين المؤقت وسجل المشاهدة والتنزيلات، وطلب تصحيح البيانات الشخصية، وشفافية سياسات الاحتفاظ بالبيانات.

## آخر prompt منفذ
رقم البرومبت: PROMPT 064 (مُعدّل - إصلاح Crashlytics Build ID Plugin)
اسم المرحلة: إدارة الخصوصية وحقوق البيانات والتكامل مع Firebase Crashlytics

## المرحلة الحالية
تم تنفيذ منظومة كاملة للخصوصية وحماية الحقوق الرقمية للمستخدم داخل تطبيق "سراج":
1. **تصدير البيانات الشامل (Data Portability & Export)**:
   - توليد وتجميع كافة بيانات الحساب، والمشاريع، وسجل النشاط، والتفضيلات، والملخص المالي المجهول.
   - تنقية البيانات وتطهيرها تلقائياً (`sanitizeDataMap`) من أي كلمات مرور، أو هاش، أو Tokens الشراء، أو مفاتيح API الحساسة.
   - حساب بصمة التشفير `SHA-256` لضمان سلامة الملف وتنزيل حزمة Txt/JSON آمنة مع خيار المشاركة عبر `FileProvider`.
2. **حق الحذف النهائي وفترة السماح الأمان (Account Purge & Grace Period)**:
   - إتاحة خيار طلب حذف الحساب مع تحديد السبب وفترة سماح افتراضية (14 يوماً).
   - توفير زر لإلغاء طلب الحذف أثناء فترة السماح واستعادة الحساب كاملاً قبل المسح النهائي من Cloud Functions.
3. **التحكم بالبيانات والتخزين المحلي (Storage & History Control)**:
   - مسح سجل المشاهدة والتفاعل بكبسة زر واحدة.
   - تفريغ ملفات التنزيلات والوسائط المحفوظة محلياً مع تحديث مؤشرات المساحة التخزينية.
   - تفريغ الذاكرة المؤقتة (Cache) بنقرة واحدة لتحرير المساحة فوراً.
4. **تصحيح البيانات وتعديل البيانات الشخصية (Data Correction Request)**:
   - إرسال طلب رسمي لتصحيح أو تعديل أي حقل شخصي خاطئ لكتّاب المحتوى والمراجعين الشرعيين ومتابعتها خادميًا.
5. **شفافية وسياسات الاحتفاظ (Data Retention Transparency)**:
   - عرض سياسات وجداول الاحتفاظ بكل فئة بيانات (الملف الشخصي، المشاريع، سجل النشاط، الوسائط المؤقتة، الموقع الجغرافي، والسجلات المالية المطلوبة قانونياً).
6. **تحديث قواعد Firestore وFileProvider**:
   - إضافة قواعد أمان لحماية `account_deletion_requests` و `data_correction_requests` و `activity_history` و `invoices`.
   - إضافة `FileProvider` في `AndroidManifest.xml` لتصدير وحفظ ملفات البيانات بأمان.

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

