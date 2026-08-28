# تطبيق سراج (Siraj)

سراج منصة إسلامية عربية تساعد المستخدم على إنتاج فيديوهات قصيرة ومحتوى مرئي ومسموع بسهولة، مع دمج ميزات المراجعة الشرعية للوثوقية والأرصدة المستندة إلى السحابة.

## الهيكلية التقنية
- **اللغة:** Kotlin
- **إطار عمل واجهة المستخدم:** Jetpack Compose (Material 3)
- **الهندسة المعمارية:** Clean Architecture (MVVM)
- **الخدمات السحابية:** Firebase (Auth, Firestore, Storage)
- **إدارة الحالة:** StateFlow و Coroutines
- **إدارة الاشتراكات:** Google Play Billing

## بناء المشروع
لإجراء تجميع واختبار التطبيق، يمكنك استخدام سطر الأوامر (Terminal).

### تشغيل التطبيق
```bash
./gradlew :app:assembleDebug
```

### تشغيل الاختبارات
يمكنك استخدام سكربت `run_tests.sh` للتحقق من جميع الاختبارات المتوفرة:
```bash
./run_tests.sh
```
أو عبر أمر Gradle المباشر:
```bash
./gradlew :app:testDebugUnitTest
```

ملاحظة: بيئة الاختبارات الحالية تتطلب JDK متوافق مع `mockk` لتفادي أخطاء الـ ByteBuddy Agent.
