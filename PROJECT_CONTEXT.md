# سراج (Siraj)

## حالة التنفيذ
مكتمل بنجاح للمرحلة الحالية - تم إعداد وتكامل نظام تحليل ملاحظات النسخة التجريبية وإدارة العيوب (Beta Defect Triage & Management System) وتطوير لوحة فرز العيوب والملاحظات `DefectTriageScreen`، بما يشمل التصنيفات الثمانية المعتمدة، وضوابط السلامة الشرعية الصارمة، وقواعد منع الإغلاق دون تبرير، وقائمة العمل المرتبة حسب الأولويات (P0 إلى P3)، وسجلات التشخيص الآمنة الخالية من البيانات الشخصية.

## آخر prompt منفذ
رقم البرومبت: PROMPT 073
اسم المرحلة: تحليل ملاحظات Beta

## المرحلة الحالية
- إنشاء نماذج إدارة العيوب والفرز `DefectManagementModels.kt` (التصنيفات الثمانية: blocker, critical, major, minor, enhancement, duplicate, not_reproducible, expected_behavior، والأولويات P0-P3، والنطاقات والحالات).
- تعريف واجهة المستودع `BetaDefectManagementRepository` وتنفيذها في `FirebaseBetaDefectManagementRepositoryImpl` مع تطبيق الضوابط الصارمة:
  - أي خطأ في مصدر أو نص ديني يصنف critical/blocker تلقائياً ولا يخفض.
  - إلزامية تقديم سبب الإغلاق `closureReason` عند نقل العيب إلى مغلق أو مؤجل.
  - إلزامية تقديم تفاصيل الحل `resolutionNote` ومرجع الاختبار للأعطال الحرجة.
  - حساب القائمة المرتبة حسب الأولويات والتصنيفات بدقة.
- تطوير شاشة الفرز والتحليل `DefectTriageScreen` و `DefectTriageViewModel` مع فلاتر التصنيف، البحث، فلاتر النطاقات، بطاقات العيوب، وحوارات التعديل والتصنيف وتحديث دورة حياة العيب.
- ربط شاشة فرز العيوب بنظام التنقل (`Screen.DefectTriage`) ومركز المختبرين (`TesterHubScreen`).
- تحديث قواعد الحماية `firestore.rules` لتأمين مجموعة `beta_defects`.
- إنشاء الوثيقة التوثيقية `BETA_FEEDBACK.md`.
- كتابة وتمرير الاختبارات الوحدوية الشاملة `DefectManagementUnitTest`.

## التقنية
- Kotlin & Jetpack Compose (Material 3)
- Gradle Product Flavors (`dev`, `staging`, `prod`)
- Firebase Authentication, Cloud Firestore, Firebase Crashlytics
- Architecture: MVVM, Clean Architecture, Repository Pattern
- Testing: JUnit4, MockK, Coroutines Test

## بنية الوحدات
- `app/src/main/java/com/siraj/app/`
  - `core/config/EnvironmentConfig.kt`: إدارة بيئة التشغيل، رقم الإصدار، وبيانات البيتا.
  - `domain/models/beta/BetaFeedback.kt`: نماذج ملاحظات المختبرين، التصنيفات، ومستويات الأهمية.
  - `domain/repository/BetaFeedbackRepository.kt`: واجهة مستودع ملاحظات البيتا.
  - `data/repository/beta/FirebaseBetaFeedbackRepositoryImpl.kt`: حفظ الملاحظات وربطها بالتشخيص وCrashlytics.
  - `features/beta/BetaFeedbackViewModel.kt`: إدارة حالة تقديم الملاحظات للمختبرين.
  - `core/ui/components/`:
    - `BetaFeedbackDialog.kt`: حوار إرسال الملاحظات مع الفحص التلقائي لمواصفات الجهاز والنظام.
    - `BetaBadgeBanner.kt`: شريط التمييز البصري والزر العائم للنسخة التجريبية.
  - `features/settings/presentation/SettingsPages.kt`: عرض تفاصيل إصدار البيتا وزر الملاحظات في الدعم وحول التطبيق.
  - `features/home/presentation/HomeScreen.kt`: تضمين شريط البيتا أعلى الصفحة الرئيسية.

## الخدمات المربوطة
- Firebase Auth, Firestore (`beta_feedback`), Crashlytics (مع تصنيف `is_beta = true`).

## البيئة الحالية
- Staging / Beta (`com.siraj.app.beta`) & Development (`com.siraj.app.dev`)

## المخاطر المعروفة
- حظر استخدام بيانات أو مفاتيح إنتاج حقيقية أثناء اختبارات البيتا، وقصر المشتريات على بيئة Sandbox.

## القرارات التقنية
- جمع التشخيصات الفنية (طراز الجهاز وإصدار النظام والشاشة الحالية) تلقائياً مع ملاحظات المختبر لتسهيل حل المشاكل دون جمع أي بيانات شخصية حساسة.
- فصل حزمة البيتا (`applicationIdSuffix = ".beta"`) لتمكين المختبرين من تثبيت نسختي الإنتاج والبيتا جنباً إلى جنب.

## الخطوة التالية
المضي قدماً في البرومبتات والمراحل التالية بحسب خطة المشروع.
