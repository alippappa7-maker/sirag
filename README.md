# تطبيق سراج (Siraj)

سراج منصة إسلامية عربية متكاملة تساعد المستخدم على إنتاج فيديوهات قصيرة ومحتوى مرئي ومسموع بسهولة، مع دمج ميزات المراجعة الشرعية للوثوقية والأرصدة المستندة إلى السحابة، والوصول الشامل.

## الهيكلية التقنية
- **اللغة:** Kotlin
- **إطار عمل واجهة المستخدم:** Jetpack Compose (Material 3)
- **الهندسة المعمارية:** Clean Architecture (MVVM)
- **الخدمات السحابية:** Firebase (Auth, Firestore, Storage, Functions, Crashlytics)
- **إدارة الحالة:** StateFlow و Coroutines
- **إدارة الاشتراكات:** Google Play Billing
- **مشغل الوسائط:** Media3 / ExoPlayer (مع دعم الشروحات والتفريغ النصي)
- **الوصول الشامل:** متوافق مع WCAG 2.1 AA/AAA وقارئات الشاشة (TalkBack/VoiceOver)

## الوصول الشامل (Universal Accessibility)
- وضع تباين فائق (High Contrast Mode) متوافق مع WCAG AAA.
- تكبير نصوص ديناميكي (100% - 150%) دون اقتطاع للنصوص العربية وتشكيلها.
- أوصاف دلالية ومناطق تحديث حية وترتيب قراءة RTL لقارئات الشاشة.
- ترجمة وشروحات نصية مرئية (`Captions`) وتفريغ نصي صوتي تفاعلي (`Transcripts`).
- للمزيد، يرجى مراجعة [ACCESSIBILITY_GUIDE.md](ACCESSIBILITY_GUIDE.md).

## بناء المشروع
لإجراء تجميع واختبار التطبيق:

### تشغيل التطبيق
```bash
gradle :app:assembleDebug
```

### تشغيل الاختبارات
يمكنك استخدام سكربت `run_tests.sh` أو أمر Gradle المباشر:
```bash
gradle :app:testDebugUnitTest
```
