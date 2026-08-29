# سجل التغييرات (Changelog)

## [Unreleased] - Privacy & User Rights Management (PROMPT 064)
### Fixed
- تم إصلاح الخطأ المسبّب لنهو التطبيق المفاجئ (`IllegalStateException: The Crashlytics build ID is missing`) بتفعيل إضافة `com.google.firebase.crashlytics` في ملفات `gradle/libs.versions.toml` و `build.gradle.kts` و `app/build.gradle.kts` لإنشاء معرفات البناء المطلوبة تلقائياً عند التهيئة.

### Fixed
- إصلاح خطأ `IllegalStateException` وانهيار التطبيق عند الفتح عبر تصحيح ثيم `MainActivity` ليتوافق مع `AppCompatActivity` (`Theme.AppCompat.Light.NoActionBar`).

### Added
- إضافة **مركز الخصوصية وحقوق البيانات (Privacy Center)** المتكامل في إعدادات التطبيق متوافقاً مع متطلبات GDPR وCCPA.
- إضافة `PrivacyManager` المخصص لتطهير البيانات الحساسة (`sanitizeDataMap`) وتوليد حزم التصدير وصياغة ملفات JSON/Txt وتوليد بصمة التشفير `SHA-256 Checksum`.
- دعم **تصدير واستخراج البيانات الشاملة (Data Portability)** بجميع تفاصيل الحساب، والمشاريع، وسجل المشاهدة والتفاعل، والتفضيلات، والملخص المالي المجهول مع خيار الحفظ والمشاركة عبر `FileProvider`.
- دعم **طلب حذف الحساب النهائي (Account Deletion Request)** مع تحديد السبب وفترة السماح الأمان (14 يوماً) وزر إلغاء الطلب واستعادة الحساب قبل المسح النهائي.
- التحكم الكامل في **تفريغ سجل المشاهدة والتفاعلات، وتفريغ التنزيلات والوسائط محلياً، ومسح الذاكرة المؤقتة (Cache)**.
- إتاحة **تقديم طلب تصحيح البيانات الشخصية (Data Correction Request)** لكتّاب المحتوى والمراجعين الشرعيين.
- عرض **شفافية سياسات الاحتفاظ بالبيانات (Data Retention Policies)** لكل فئة بيانات بشكل تفصيلي ومبسط.
- تحديث **قواعد Firestore الألمانية (`firestore.rules`)** لحماية مسارات حذف الحسابات وتصحيح البيانات وسجلات النشاط والفواتير.
- إضافة `FileProvider` و `file_paths.xml` في `AndroidManifest.xml` لمشاركة ملفات البيانات الآمنة.
- إضافة اختبارات شاملة `PrivacyManagerTest` و `PrivacyCenterViewModelTest` للتحقق من التطهير وبصمة التشفير وإدارة خيارات الحذف والتصدير بنجاح.

## [Unreleased] - Security Audit & Hardening (PROMPT 069)
### Added
- إنشاء تقرير المراجعة الأمنية الشامل `SECURITY_REVIEW.md`.
- تفعيل `Firebase App Check` باستخدام `PlayIntegrity` في تطبيق الأندرويد لزيادة الأمان.

### Fixed
- تصحيح ثغرة في `firestore.rules` تمنع إضافة أعضاء لمساحة العمل بطريقة غير مصرحة.
- تصحيح ثغرة في `storage.rules` لمنع الرفع العشوائي للملفات في مساحات العمل بدون استخدام واجهات الخادم الموثوقة.

## [Unreleased] - Pre-release Testing Plan (PROMPT 068)
### Added
- إنشاء وثيقة `TEST_PLAN.md` الشاملة لاختبارات القبول (UAT).
- تحديد مسارات الاختبار الأساسية (Authentication, Studio, Review, Mihrab, Flash).
- تحديد سيناريوهات اختبار الشروط الاستثنائية والوصول الشامل (Offline, Weak Network, Screen Reader, Scaled Fonts, Deep Links).
- وضع هيكلية لتسجيل العيوب والأخطاء وتصنيفها لمنع تسرب الأعطال الحرجة (Blockers/Criticals) إلى بيئة الإنتاج.

## [Unreleased] - Security Audit & Hardening (PROMPT 069)
### Added
- إنشاء تقرير المراجعة الأمنية الشامل `SECURITY_REVIEW.md`.
- تفعيل `Firebase App Check` باستخدام `PlayIntegrity` في تطبيق الأندرويد لزيادة الأمان.

### Fixed
- تصحيح ثغرة في `firestore.rules` تمنع إضافة أعضاء لمساحة العمل بطريقة غير مصرحة.
- تصحيح ثغرة في `storage.rules` لمنع الرفع العشوائي للملفات في مساحات العمل بدون استخدام واجهات الخادم الموثوقة.

## [Unreleased] - Pre-release Testing Plan (PROMPT 068)
### Added
- إنشاء وثيقة `TEST_PLAN.md` الشاملة لاختبارات القبول (UAT).
- تحديد مسارات الاختبار الأساسية (Authentication, Studio, Review, Mihrab, Flash).
- تحديد سيناريوهات اختبار الشروط الاستثنائية والوصول الشامل (Offline, Weak Network, Screen Reader, Scaled Fonts, Deep Links).
- وضع هيكلية لتسجيل العيوب والأخطاء وتصنيفها لمنع تسرب الأعطال الحرجة (Blockers/Criticals) إلى بيئة الإنتاج.

## [Unreleased] - Build Environments Configuration (PROMPT 067)
### Added
- تكوين `productFlavors` لبيئات `dev`, `staging`, و `prod` في `build.gradle.kts`.
- تخصيص لواحق معرّف الحزمة (`applicationIdSuffix`) واسم التطبيق (`app_name`) لكل بيئة لتسهيل التمييز والفصل.
- تهيئة البنية التحتية لملفات `google-services.json` منفصلة لكل بيئة لتفادي تداخل مشاريع Firebase.
- دمج `BuildConfig` لتوفير متغير `ENVIRONMENT` يتم استخدامه لتوجيه الطلبات والتحكم باللوجز محلياً.
- ضمان عدم حفظ أي أسرار، أو ملفات التوقيع (Keystores)، أو مفاتيح إنتاج حقيقية داخل المستودع.

## [Unreleased] - Store Listing Preparation (PROMPT 066)
### Added
- إنشاء وثيقة `STORE_LISTING.md` تحتوي على وصف التطبيق الكامل والقصير للمتاجر باللغتين العربية والإنجليزية.
- إعداد نصوص ومقترحات لقطات الشاشة والفيديو الترويجي بطريقة تعكس وظائف التطبيق الفعلية.
- صياغة سياسات المحتوى الشرعي، مبادئ الإبلاغ عن المحتوى، والتأكيد بوضوح على أن الذكاء الاصطناعي لا يمثل جهة إفتاء.
- تجهيز فقرات ملاحظات المراجعين (Reviewer Notes) وتوضيح كيفية عمل التطبيق والإشعارات والصلاحيات لتسهيل مراجعته من قبل Google Play و App Store.

## [Unreleased] - Localization & Internationalization (PROMPT 065)
### Added
- تم تفعيل التدويل الأساسي وإنشاء ملفات `strings.xml` للغتين العربية (افتراضية) والإنجليزية كبنية أولية.
- استبدال النصوص الصلبة المتكررة بكثرة (أزرار وإجراءات عامة) بنصوص من `strings.xml` للبدء بخطة التدويل وتفادي كسر الـ RTL.
- تمكين تغيير اللغة ديناميكياً من شاشة الإعدادات باستخدام `AppCompatDelegate` و `LocaleManager`.
- إنشاء `LocalizationUtils` لتنسيق التواريخ، والأرقام، والعملات حسب لغة النظام بشكل آمن ودون اللجوء للترجمة الصلبة.
- تعديل `MainActivity` لوراثة `AppCompatActivity` بدلاً من `ComponentActivity` لدعم الـ LocaleManager وتحديث `LayoutDirection` ديناميكياً لضمان سلامة تخطيط الـ RTL و LTR.
- إضافة إعداد `android:localeConfig` في `AndroidManifest.xml` لدعم Android 13+.

## [Unreleased] - Universal Accessibility (PROMPT 063)
### Added
- إضافة منظومة الوصول الشامل (Universal Accessibility) المتوافقة مع معايير WCAG 2.1 AA و AAA.
- إضافة `ColorContrastHelper` لحساب نسب التباين ومعادلات التوافق مع نصوص WCAG العادية والكبيرة.
- إضافة لوحات ألوان فائقة التباين `HighContrastLightColorScheme` و `HighContrastDarkColorScheme` للأشخاص ذوي ضعف البصر.
- إضافة نظام التحجيم المرن للنصوص `getScaledTypography` مع دعم مضاعفات التكبير (100% إلى 150%) دون اقتطاع الحروف العربية.
- إنشاء `AccessibilitySemantics` التي توفر معدلات مساحة اللمس الأدنى (48×48 نقطة)، وتنظيم مسارات القراءة لقارئات الشاشة باللغة العربية (RTL Traversal)، ومناطق التحديثات الحية (`liveRegion`).
- ترقية مكونات الواجهة الأساسية (`SirajButton`, `SirajTextField`, `StateScreens`) لتضمين الأوصاف الدلالية، ومؤشرات الخطأ المقروءة صوتياً، والمظهر عالي التباين.
- دعم الشروحات والترجمة النصية المصاحبة (`Closed Captions`) في مشغل الفيديو `SirajVideoPlayer`.
- دعم التفريغ النصي الصوتي (`Audio Transcripts`) في مشغل المقاطع والتلاوات `MiniPlayer` و `AudioController`.
- إضافة شاشة تفضيلات "إمكانية الوصول والشمول" داخل إعدادات التطبيق مع بطاقة معاينة حية وتشخيص التوافق.
- إضافة اختبارات شاملة `AccessibilityTest` للتحقق من نسب التباين وتكبير الخطوط والإعدادات التلقائية والمخصصة.

## [Unreleased] - Crash Monitoring and Firebase Crashlytics (PROMPT 062)
### Added
- دمج `Firebase Crashlytics` لتسجيل الاستثناءات والأعطال البرمجية بأمان وتصنيف منظم.
- إنشاء `CrashlyticsSanitizer` للتطهير الكامل وحجب أي مفاتيح حساسة (API keys, Tokens, Passwords, Purchase Tokens)، تشفير معرّفات المستخدمين (`SHA-256`) ومنع وصول أي نصوص قرآنية أو أحاديث أو محتوى خاص للمستخدم إلى السجلات.
- إضافة `CrashMonitoringManager` و `CrashMonitoringService` كواجهة موحدة لإدارة سجلات الأعطال والـ Breadcrumbs الآمنة عبر المنصات.
- إضافة تصنيف الأخطاء حسب الفئات (`ErrorCategory`) ومستوى الخطورة (`ErrorSeverity`).
- تسجيل تلقائي لمسارات التنقل (`Navigation Breadcrumbs`) وإجراءات المستخدم الآمنة.
- إضافة خيار تفعيل/تعطيل تقارير الأعطال في إعدادات الخصوصية (`PrivacySettings`).
- إضافة واجهة تشخيص واختبار الأعطال التجريبية في صفحة الدعم الفني (`SupportSettings`) مع إمكانية إرسال تقرير غير قاتل أو محاكاة عطل تجريبي.
- إنشاء وثائق الوصول للوحة التحكم وسياسة الاحتفاظ والمراجعة الدورية (`CRASH_MONITORING.md` و `CRASH_RETENTION_POLICY.md`).
- كتابة اختبارات شاملة `CrashMonitoringTest` تغطي التطهير والتصنيف والترميز والـ Breadcrumbs.

## غير مُصدر
- إضافة اختبارات وحدات (Unit Tests) للتحقق من نماذج البيانات (Models) مثل `ProjectTest` و `CreatorAnalyticsDashboardTest`.
- إضافة اختبارات وحدات للتحقق من عمل المحولات (ViewModels) مثل:
  - `AuthViewModelTest`: اختبار نجاح وفشل تسجيل الدخول والتسجيل.
  - `CreatorAnalyticsViewModelTest`: اختبار استجابة واجهة تحليلات الأداء بناءً على تسجيل الدخول.
  - `NotificationViewModelTest`: اختبار تحميل الإشعارات وتطبيق الفلاتر (Filters).
  - `ProjectEditorViewModelTest`: اختبار الحفظ التلقائي (Auto-save).
  - `ShariaReviewViewModelTest`: اختبار تحديثات الحالة بعد الاعتماد والرفض.
  - `StudioViewModelTest`: اختبار فلاتر المشاريع في لوحة الاستوديو.
  - `SubscriptionViewModelTest`: اختبار الأرصدة والاشتراكات.
  - `WorkspaceViewModelTest`: اختبار قيود الصلاحيات عند دعوة الأعضاء.
- إنشاء السكريبت `run_tests.sh` لتشغيل الاختبارات بأمر واحد بسهولة.
- تصحيح إعدادات MockK و JVM و Robolectric لتشغيل كافة الاختبارات بنجاح تام (100% Passed).

## [Unreleased] - Performance Improvements (PROMPT 060)
### Added
- Image Cache configuration using Coil with memory limits (15%) and disk limits (2%).
- Offline persistence cache limit (50MB) for Firebase Firestore.
- Network RetryInterceptor with exponential backoff for slow connections in OkHttp Client.
- DefaultLoadControl configurations for ExoPlayer to minimize buffering memory overhead.

### Changed
- R8 (isMinifyEnabled, isShrinkResources, isCrunchPngs) enabled for `release` builds to significantly reduce APK size.
- LazyColumn/VerticalPager optimized with `key` arguments across `HomeScreen` and `FlashesScreen` to avoid unnecessary recomposition and reduce lag during scrolling.
- Reduced HTTP logging level to BASIC in OkHttp to improve performance and prevent sensitive data leakage.

## [Unreleased] - Error Handling and Recovery System (PROMPT 061)
### Added
- Unified `AppError` sealed class for consistent error mapping (Network, Auth, Database, Storage, Payment, etc.).
- `ErrorHandler` utility to sanitize technical messages and prevent exposure of sensitive data (tokens, keys) to the UI.
- Secure logging mechanism via `SirajLogger` to trace errors using uniquely generated reference IDs without exposing user-identifiable patterns.
- Upgraded `Resource` wrapper class to optionally carry an `AppError` payload.
- New `@Composable AppErrorScreen` component allowing for fallback UI and structured retry actions based on error categories.

### Changed
- Standardized error-catching blocks across data layer repositories (`FirebaseAuthRepositoryImpl`, `FirebaseProjectRepositoryImpl`, etc.) to process exceptions through `ErrorHandler.handle(e)`.
