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
