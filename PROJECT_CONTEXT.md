# اسم المشروع
سراج (Siraj) - منصة إسلامية عربية لإنتاج المحتوى

# حالة التنفيذ
قيد التنفيذ (مرحلة صفحة الخطط والأسعار اكتملت)

# آخر prompt منفذ
PROMPT 055 (صفحة الخطط والأسعار)

# المرحلة الحالية
في انتظار استلام المرحلة القادمة. تم الانتهاء مؤخراً من:
- موجز الومضات (Flashes Feed).
- نشر الومضات (Flash Publishing).
- التفاعل والسلامة المجتمعية (Interaction & Safety).
- لوحة إدارة المحتوى (Content Management).
- حماية مركزية لقواعد بيانات Firestore و Storage (Security Rules).
- نماذج الاشتراكات والخطط والأرصدة (Subscriptions, Plans, Entitlements).
- نظام متكامل لإدارة الأرصدة وحجز الاستهلاك للعمليات المكلفة (Credits & Usage Limits).
- تكامل مع Google Play Billing للشراء من المتجر مع تحقق خادمي (Server-Side Validation).
- التأسيس لاشتراكات App Store وStoreKit 2 مع توثيق شامل للتحقق الخادمي وإشعارات V2 (App Store Server Notifications).
- تصميم وبناء صفحة الخطط والأسعار مع مقارنات المميزات، الأرصدة، وعرض شفاف لسياسة الإلغاء والتجديد.
- التحقق من الصلاحيات و Custom Claims.

# التقنية
- Kotlin و Jetpack Compose و MVVM Architecture
- Clean Architecture (Domain, Data, Presentation)
- Firebase (Auth, Firestore, Storage)
- Navigation Component (Compose)

# بنية الوحدات
- `core`: الأدوات، الثيمات، المكونات المشتركة، التوجيه.
- `domain`: النماذج، المستودعات، حالات الاستخدام.
- `data`: التنفيذ الفعلي (Firebase/Local Repositories).
- `features`: واجهات المستخدم مقسمة حسب الوظيفة (admin, community, moderation, flashes, etc).

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
- بناء لوحة إدارة المحتوى (Content Management Dashboard) مع سجل تدقيق (Audit Log) وحالات محتوى (Approved, Suspended, Archived, Pending).
- دعم أرشفة واستعادة المحتوى بدلاً من الحذف النهائي بناءً على سياسات التطبيق.
- تطبيق الحماية على جميع مستندات Firestore بوضع مقفل (Locked Down) بناءً على Custom Claims و OwnerId.
- فصل الخطة (Plan) عن الاشتراك (Subscription) وعن الامتيازات (Entitlement).
- تأكيد مبدأ التحقق الخادمي (Server-side validation) لعمليات الشراء، وعدم الاحتفاظ بالـ PurchaseToken الخام.
- تأسيس دوال الحجز الذري (Reservation) وتأكيد الاستهلاك (Confirmation) والإرجاع (Refund) لعمليات الذكاء الاصطناعي مع دعم Idempotency.
- الاعتماد على الخادم فقط لمنح الامتيازات بعد التحقق من `purchaseToken` عبر Google Play Developer API لضمان الأمان.
- تطبيق نفس مبدأ التحقق الخادمي (Server-Side) لمشتريات iOS عبر JWS وإشعارات أبل لتأمين الـ Entitlement ومعالجة الـ originalTransactionId.
- عرض السعر كما يأتي من المتجر، والتوضيح الصريح للمستخدمين حول سياسة التجديد التلقائي للامتثال للمعايير الأخلاقية والمتاجر.
- التأسيس لاستقبال إشعارات المطورين (RTDN) عبر Pub/Sub لتحديث الحالات دون الاعتماد على العميل.
- تطبيق قواعد أمان تمنع إرسال الأرصدة من العميل.
- كتابة قواعد Cloud Storage لحماية ملفات المستخدمين وتأمين الملفات العامة.
- كتابة اختبارات Firebase Rules باستخدام `rules-unit-testing` للتحقق من عدم تجاوز الصلاحيات.

# الخطوة التالية
انتظار توجيه المستخدم للمرحلة التالية.
