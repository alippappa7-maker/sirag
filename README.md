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

> **ملاحظة:** تمت إزالة جميع سكربتات الصيانة المؤقتة (`patch_*.py`، `fix_*.py`، `update_*.py`، `*.sh`) من المستودع؛ البناء يعتمد على Gradle فقط ولا يحتاج إلى أي سكربت مساعد.

### تشغيل التطبيق
```bash
./gradlew :app:assembleDebug
```

### تشغيل الاختبارات
```bash
./gradlew :app:testDebugUnitTest
```

البناء المستمر عبر GitHub Actions معرّف في [`.github/workflows/build-apk.yml`](.github/workflows/build-apk.yml).
## المميزات الحالية
- إدارة المشاريع والمشاهد بمرونة.
- الذكاء الاصطناعي كمساعد إنتاج (توليد، تلخيص).
- التحقق الشرعي وربط المصادر بالمراجع.
- **حوكمة المراجعين الشرعيين (Reviewer Governance):** توثيق المؤهلات، تحديد نطاق الاختصاص، منع تعارض المصالح، المراجعة المزدوجة للموضوعات الحرجة، وسجل قرارات ثابت (راجع [REVIEWER_GOVERNANCE.md](REVIEWER_GOVERNANCE.md)).
- **نظام التصحيح والإصدارات الشرعية (Content Corrections & Versioning):** حظر التعديل الصامت للمحتوى المنشور، سجل إصدارات ثابت ببصمة مشفرة، إشعارات تصحيح معلنة، حصر المواد المتأثرة، والاعتماد الشرعي للاستدراكات (راجع [CONTENT_CORRECTIONS.md](CONTENT_CORRECTIONS.md)).
- المحراب والقرآن الكريم كجزء أساسي.
- إدارة التراخيص والحقوق (Rights Management) لحماية المحتوى المنشور.
- الإطلاق التدريجي وجاهزية بيئة الإنتاج.

## الوحدات الوظيفية الفعلية
الوحدات التالية موجودة فعلاً ضمن `app/src/main/java/com/siraj/app/features/` (المصدر: الكود):

| التصنيف | الوحدات |
|---|---|
| المصادقة والتهيئة | `auth`، `onboarding`، `splash` |
| الواجهة الرئيسية | `home`، `details` |
| المحراب والعبادات | `mihrab`، `quran`، `audio` |
| الإنتاج والمحتوى | `studio`، `project`، `flashes`، `ideation`، `ai`، `share` |
| الحوكمة والمراجعة | `review`، `rights`، `moderation`، `minor`، `community` |
| البحث والتصنيف | `search`، `taxonomy` |
| الإدارة والعمليات | `admin`، `cost`، `migration`، `notification`، `history` |
| الاشتراكات والدعم | `subscription`، `support`، `beta`، `settings` |

> إجمالي 30 وحدة وظيفية. أي وثيقة تذكر رقماً مختلفاً (مثل 10 أو 16) تعتبر غير دقيقة، والكود هو المرجع النهائي.
