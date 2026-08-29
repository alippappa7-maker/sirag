# دليل مراقبة الأعطال وإدارة Firebase Crashlytics (Siraj Crash Monitoring & Diagnostics)

تعتمد منصة سراج نظاماً دقيقاً وآمناً لمراقبة الأعطال والاستثناءات البرمجية عبر **Firebase Crashlytics**، مع الالتزام التام بمبادئ الخصوصية الصارمة وقدسية البيانات الإسلامية.

---

## 1. الوصول إلى لوحة تحكم Crashlytics (Console Access)

### خطوات الوصول:
1. التوجه إلى [Firebase Console](https://console.firebase.google.com).
2. اختيار مشروع سراج في البيئة المطلوبة (`siraj-dev` أو `siraj-prod`).
3. من القائمة الجانبية (Build/Release & Monitor)، اختر **Crashlytics**.
4. اختيار النظام الأساسي (**Android** أو **iOS**).

### تصفية الأعطال وتصنيفها (Filters & Custom Keys):
توفر منصة سراج مفاتيح مخصصة مشفرة ومفلترة تتيح تتبع الأعطال بدقة:
- **`environment`**: تصفية الأعطال بحسب البيئة (`DEVELOPMENT`, `STAGING`, `PRODUCTION`).
- **`error_category`**: تصفية بنوع الخطأ:
  - `NETWORK`: أخطاء الاتصال بالشبكة والـ Timeout.
  - `AUTH`: أخطاء المصادقة والجلسات.
  - `DATABASE`: أخطاء Cloud Firestore.
  - `STORAGE`: أخطاء Cloud Storage.
  - `MEDIA_PROCESSING`: أخطاء إنتاج الصوتيات والمرئيات.
  - `AI_GENERATION`: أخطاء واجهات الذكاء الاصطناعي الخلفية.
  - `SHARIA_REVIEW`: أخطاء مسار المراجعة والتدقيق.
  - `BILLING`: أخطاء الفوترة والاشتراكات.
  - `SYSTEM`: استثناءات النظام العامة.
- **`error_severity`**: درجة الخطورة (`FATAL`, `ERROR`, `WARNING`, `INFO`).
- **`request_id`**: تتبع الخطأ في السجلات الخلفية دون الحاجة إلى معرّفات شخصية.
- **`user_role`**: تصنيف الخطأ حسب صلاحية المستخدم (`USER`, `CREATOR`, `REVIEWER`, `ADMIN`, `OWNER`).
- **`app_version`** و **`build_number`**: إصدار وبناء التطبيق.

---

## 2. سياسة الخصوصية وتطهير البيانات (Sanitization & Privacy Guard)

يمنع منعاً باتاً وصول أي من البيانات التالية إلى خوادم Crashlytics:
1. **النصوص القرآنية والأحاديث الشريفة**: استبعاد قاطع لأي نصوص شرعية من الـ Breadcrumbs أو Custom Keys.
2. **محتوى المستخدم الخاص**: مسودات النصوص، نصوص الـ Prompts، السيناريوهات، أو الملاحظات الخاصة.
3. **الأسرار والمفاتيح**:
   - مفاتيح API (`AIzaSy...`, `key=***`, `token=***`).
   - رموز المرور وتوكنات المصادقة (`Bearer ***`, `password=***`).
   - رموز الشراء (`purchase_token=***`).
4. **البيانات الشخصية المباشرة (PII)**:
   - البريد الإلكتروني (يتم استبداله بـ `[MASKED_EMAIL]`).
   - معرّف المستخدم (يتم تحويله إلى كود غير قابل للعكس عبر `SHA-256` مشفر بطول 16 رمزاً).

---

## 3. مسار التقاط أخطاء Flutter والواجهات متعددة المنصات (Multiplatform & Flutter Error Pipeline)

لتسجيل كافة أخطاء Flutter وDart وتوجيهها إلى Crashlytics:

```dart
// 1. التقاط أخطاء Flutter framework:
FlutterError.onError = (FlutterErrorDetails details) {
  FlutterError.presentError(details);
  FirebaseCrashlytics.instance.recordFlutterFatalError(details);
};

// 2. التقاط الأخطاء غير المعالجة في Dart Asynchronous zone:
PlatformDispatcher.instance.onError = (error, stack) {
  FirebaseCrashlytics.instance.recordError(
    error,
    stack,
    fatal: true,
    information: ['Category: SYSTEM', 'DartUncaught: true']
  );
  return true;
};
```

---

## 4. اختبار الأعطال تجريبياً (Crash Testing & Diagnostics)

يوفر التطبيق واجهة تشخيص مدمجة في صفحة **الإعدادات -> الدعم الفني**:
1. **إرسال تقرير غير قاتل (Send Non-Fatal Test)**:
   - ينشئ استثناءً تجريبياً مصنفاً ويوجهه فوراً لاختبار استقبال التقارير.
2. **محاكاة عطل تجريبي (Trigger Test Crash)**:
   - يتيح لمهندسي الجودة والتطوير محاكاة عطل فوري لإغلاق التطبيق وفحص وصول السجل إلى لوحة Firebase بعد إعادة التشغيل.
