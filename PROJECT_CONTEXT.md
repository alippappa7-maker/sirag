# سراج (Siraj)

## حالة التنفيذ
قيد التنفيذ - مرحلة التطوير البصري (الهوية التقنية Techno-Spiritual).

## آخر برومبت منفذ
آخر برومبت منفذ: تعميم الهوية التقنية (المرحلة 2: واجهات المحتوى والبحث - القرآن والإشعارات والبحث)
المرحلة: التطوير البصري (الهوية التقنية)
الحالة: مكتمل
الموانع: لا توجد
المخاطر المقبولة: لا توجد
الاعتمادات المطلوبة: لا توجد
الخطوة التالية: المتابعة في تحسين الشاشات والمكونات الإضافية وفق التوجيهات.

## التقنية
- Kotlin & Jetpack Compose (Material 3)
- Centralized Design Tokens (Theme, ColorScheme, Typography, Shapes, Elevations)
- Material3 Semantic Colors with Extended Color Roles (Success, Warning, Processing)
- Firebase Firestore, Cloud Functions, Firebase Storage, Firebase Remote Config
- Google Cloud Secret Manager, Cloud KMS, Static Secret Scanner, Sanitized Logger, HMAC-SHA256 Webhook Protection
- Architecture: MVVM, Clean Architecture, Repository Pattern, Cost Governance Engine
- Testing: JUnit4, MockK, Coroutines Test (38 Test Suites, 100% Green Pass Rate)
- Compliance: Strict Cost Limits, Zero Double Billing, Idempotency Checks, Emergency Provider Switch, Owner Dashboards.

## بنية الوحدات
- `DISASTER_RECOVERY.md` & `BUSINESS_CONTINUITY.md`: وثائق مرجعية لخطط التعافي من الكوارث واستمرارية الخدمة والتشغيل.
- `DATA_MIGRATIONS.md`: الوثيقة المرجعية الشاملة لسياسة ترحيل البيانات وإصدارات المخطط.
- `ADMIN_SECURITY.md`: الوثيقة المرجعية الشاملة لسياسة أمان الحسابات الإدارية.
- `domain/models/admin/`: نماذج بيانات الجلسات والصلاحيات الإدارية وسجلات التدقيق (`AdminRole`, `AdminSession`, `AdminDevice`, `SensitiveOperationType`, `SecurityAuditLog`).
- `features/admin/domain/`: محرك فحص وحوكمة صلاحيات الإدارة `AdminSecurityEngine`.
- `domain/repository/admin/`: واجهة مستودع أمان الإدارة `AdminSecurityRepository`.
- `data/repository/admin/`: التنفيذ الميداني لمستودع أمان الإدارة `AdminSecurityRepositoryImpl`.
- `features/admin/presentation/`: شاشة أمان الإدارة وإنهاء الجلسات `AdminSecurityDashboardScreen`, `AdminSecurityViewModel`.
- `COST_CONTROLS.md`: الوثيقة المرجعية الشاملة لسياسة وحدود التكاليف.
- `domain/models/cost/`: نماذج بيانات حدود الاستهلاك والعمليات والتكلفة (`CostProvider`, `CostTransaction`, `WorkspaceLimits`, `UsageMetrics`, `AlertLevel`).
- `features/cost/domain/`: محرك فحص وحساب التكلفة والحدود `CostEngine`.
- `domain/repository/cost/`: واجهة المستودع `CostManagementRepository`.
- `data/repository/cost/`: التنفيذ الميداني لمستودع التكاليف وحماية الفوترة `CostManagementRepositoryImpl`.
- `features/cost/presentation/`: شاشة ومكونات لوحة التكاليف `CostDashboardScreen`, `CostDashboardViewModel`.
- `SECRETS_LIFECYCLE.md`: الوثيقة المرجعية الشاملة لسياسة ودورة حياة الأسرار والمفاتيح.
- `MINOR_SAFETY.md`: الوثيقة المرجعية الشاملة لسياسة وضوابط حماية القاصرين.
- `CONTENT_TAXONOMY.md`: الوثيقة المرجعية الشاملة لنظام تصنيف المحتوى ومصدره.
- `domain/models/secrets/`: نماذج بيانات دورة حياة الأسرار (`SecretCategory`, `SecretEnvironment`, `SecretStatus`, `SecretOwnerTeam`, `SecretMetadata`, `SecretAccessAuditLog`, `SecretScanFinding`, `SecretScanReport`, `WebhookVerificationConfig`, `SecretLeakIncident`, `PreReleaseSecretsChecklist`).
- `core/security/`: مسجل السجلات الآمن والمطهر للأسرار `SanitizedLogger`.
- `features/secrets/domain/`: محرك دورة حياة الأسرار `SecretsLifecycleEngine` ومحرك فحص الكود الثابت `SecretScannerEngine`.
- `domain/repository/secrets/`: واجهة المستودع `SecretsLifecycleRepository`.
- `data/repository/secrets/`: التنفيذ الميداني لمستودع الأسرار `SecretsLifecycleRepositoryImpl`.
- `features/secrets/presentation/`: شاشات وبوابات ومكونات إدارة الأسرار `SecretsLifecycleScreen`, `SecretsLifecycleComponents`, `SecretsLifecycleViewModel`.
- `CONTENT_CORRECTIONS.md`: الوثيقة المرجعية الشاملة لنظام التصحيح والإصدارات وحصر الأثر.
- `REVIEWER_GOVERNANCE.md`: الوثيقة المرجعية الشاملة لحوكمة واختصاصات المراجعين الشرعيين.
- `domain/models/minor/`: نماذج بيانات حماية القاصرين (`UserAgeBracket`, `StoreAgeRating`, `MinorSafetyPolicy`, `ParentalConsentRecord`, `ChildSafetyIncidentReport`, `EducationalContentSafetyCheck`, `MinorDataDeletionSummary`).
- `features/minor/domain/`: محرك سياسات وضوابط حماية القاصرين المركزي `MinorSafetyEngine`.
- `domain/repository/minor/`: واجهة المستودع `MinorSafetyRepository`.
- `data/repository/minor/`: التنفيذ الميداني لمستودع حماية القاصرين `MinorSafetyRepositoryImpl`.
- `features/minor/presentation/`: شاشات وبوابة ومكونات حماية القاصرين `MinorSafetyScreen`, `MinorSafetyComponents`, `MinorSafetyViewModel`.
- `domain/models/taxonomy/`: نماذج بيانات التصنيف والمصادر والبيانات الوصفية.
- `features/taxonomy/domain/`: محرك الحوكمة المركزي `ContentTaxonomyEngine`.
- `FINAL_AUDIT.md`: المراجعة الشاملة للمحاور الـ 25 مع الأدلة والمخاطر.
- `GO_NO_GO.md`: القرار الرسمي للجاهزية وخطة الإطلاق التدريجي.
- `RELEASE_BLOCKERS.md`: مصفوفة تدقيق الموانع وتأكيد خلو الكود من أي موانع حرجة.
- `FINAL_TEST_REPORT.md`: التقرير الشامل لتنفيذ الاختبارات وتغطية الوحدات.
- `FINAL_SECURITY_STATUS.md`: تقرير الحالة الأمنية وحماية البيانات والأسرار.
- `FINAL_CONTENT_STATUS.md`: تقرير السلامة الشرعية وحماية النص القرآني والتراخيص.
- `OPERATIONS_PLAN.md`: خطة التشغيل لأول 90 يوماً وإدارة الطوارئ.
- `POST_LAUNCH_ROADMAP.md`: خارطة طريق ما بعد الإطلاق.

## الخدمات المربوطة
- Google Play Console، Google Cloud Secret Manager، Firebase Crashlytics، Firebase Remote Config، Firebase App Check، Cloud Firestore، Firebase Storage.

## البيئة الحالية
- Development / Staging / Production Ready.

## المخاطر المعروفة
- لا توجد مخاطر برمجية مفتوحة؛ يتم التعامل مع أي طارئ ميداني بنظام الـ Hotfix والمراقبة اللحظية في غرفة العمليات.

## القرارات التقنية
- توجيه التطبيق في MVP للبالغين وصناع المحتوى (18+ / General Audience) مع تفعيل وضع الحماية الإلزامي الصارم لأي حساب قاصر.
- إقفال تام للرسائل الخاصة المباشرة (Zero DMs) ومنع جمع الإحداثيات الدقيقة (Zero Fine GPS Location) على مستوى التطبيق.
- حظر استنساخ أصوات الأطفال (Voice Cloning Ban) وحظر القياسات الحيوية كلياً.
- حظر استخدام بيانات ومدخلات القاصرين في تدريب نماذج الذكاء الاصطناعي (Zero AI Model Training).
- فرض نظام موافقة ولي الأمر بالرمز السري (Parental Consent OTP) قبل استخدام الأطفال دون 13 للذكاء الاصطناعي أو نشر المشاريع.
- مسار طوارئ فوري لبلاغات استغلال أو إساءة موجهة للأطفال بمهلة استجابة SLA أقل من 15 دقيقة مع التوثيق للجهات الرسمية.
- تمكين حق المحو والتطهير الشامل لبيانات القاصر بضغطة زر مع إيصال أمني مشفر (SHA-256).

## الخطوة التالية
اكتمال المشروع بنجاح تام وفق أعلى معايير الجودة والأمانة.
