# سجل التغييرات (Changelog)

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

## ملاحظات
- حاليا تواجه بيئة الاختبار خطأ يتعلق بـ `java.lang.NoClassDefFoundError` بسبب `ByteBuddyAgent` الخاص بـ `mockk` والذي يحتاج إلى إعداد JVM معين للإصدارات الحديثة (JDK 17/21). ومع ذلك، بناء التطبيق نفسه ناجح وجميع مسارات العمل تعمل وتمر بمرحلة الكومبايل (Compile) بسلام.

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
