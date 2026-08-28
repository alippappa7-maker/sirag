# اسم المشروع
سراج (Siraj) - منصة إسلامية عربية لإنتاج المحتوى

# حالة التنفيذ
قيد التنفيذ (مرحلة التفاعل والسلامة المجتمعية اكتملت)

# آخر prompt منفذ
PROMPT 047 (التفاعل والسلامة المجتمعية)

# المرحلة الحالية
في انتظار استلام المرحلة القادمة. تم الانتهاء مؤخراً من:
- موجز الومضات (Flashes Feed).
- نشر الومضات (Flash Publishing).
- التفاعل والسلامة المجتمعية (Interaction & Safety).

# التقنية
- Kotlin و Jetpack Compose و MVVM Architecture
- Clean Architecture (Domain, Data, Presentation)
- Firebase (Auth, Firestore, Storage)
- Navigation Component (Compose)

# بنية الوحدات
- `core`: الأدوات، الثيمات، المكونات المشتركة، التوجيه.
- `domain`: النماذج، المستودعات، حالات الاستخدام.
- `data`: التنفيذ الفعلي (Firebase/Local Repositories).
- `features`: واجهات المستخدم مقسمة حسب الوظيفة (community, moderation, flashes, etc).

# الخدمات المربوطة
- Firebase Authentication & Firestore (Mocked in MVP)
- ExoPlayer (Media3) للمقاطع.

# البيئة الحالية
- Development (Mock/Local memory for MVP to ensure rapid building without backend dependency).

# المخاطر المعروفة
- تشغيل ExoPlayer في قوائم التمرير قد يستهلك الذاكرة.

# القرارات التقنية
- تطبيق فصل كامل بين التفاعلات (`InteractionRepository`) والسلامة المجتمعية (`SafetyRepository`).
- بناء شاشة Moderation Screen مخصصة للمراجعين والمديرين (Reviewers/Admins) لعرض وحل البلاغات بناءً على صلاحياتهم.
- دعم حظر المستخدمين، الإبلاغ، إخفاء المحتوى مباشرة من موجز الومضات باستخدام Bottom Sheet.

# الخطوة التالية
انتظار توجيه المستخدم للمرحلة التالية.
