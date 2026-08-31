# سجل التغييرات (Changelog)

## [1.1.0] - Final Project Review (PROMPT 095)
### Added
- **المراجعة النهائية (Final Legal, Sharia, and Technical Review):**
  - إنشاء `FINAL_LEGAL_REVIEW.md` لتوثيق الجاهزية القانونية وحماية القاصرين وشروط الاستخدام.
  - إنشاء `FINAL_SHARIA_REVIEW.md` لتوثيق الجاهزية الشرعية، فصل النصوص، الاعتمادات، وحماية الفتاوى.
  - إنشاء `FINAL_TECHNICAL_REVIEW.md` لتوثيق الأمان التقني وقواعد البيانات والأسرار.
  - إنشاء `RELEASE_BLOCKERS.md` لتوثيق موانع الإطلاق والاعتمادات البشرية المطلوبة.
  - إصدار قرار الجاهزية الرسمي `FINAL_GO_NO_GO.md` بحالة `CONDITIONAL_GO`.
  - عدم إضافة أي ميزات برمجية جديدة (مرحلة حوكمة ومراجعة حصرية).


## [1.0.9] - Disaster Recovery & Business Continuity (PROMPT 094)
### Added
- **نظام استمرارية الخدمة والتعافي من الكوارث (Disaster Recovery & Business Continuity):**
  - بناء وتحديث `FeatureFlagManager` لدعم أعلام الطوارئ المتقدمة (`FEATURE_SYSTEM_READ_ONLY_MODE` و `FEATURE_MAINTENANCE_MODE`).
  - تطبيق وضع القراءة فقط (Read-Only Mode) عند توقف الخدمات الحرجة لضمان استمرار تصفح المستخدمين للقرآن والمشاريع المصدرة.
  - إرساء سياسات الاسترداد والتعافي وتوثيق قيم RTO و RPO للأقسام الحرجة.
  - تعزيز حماية الفوترة عبر اختبار سياسة الـ Idempotency لمنع أي عمليات دفع أو خصم مزدوج أثناء انهيار الخدمة أو الشبكة.
  - وضع خطط استجابة تشغيلية للحوادث غير التقنية (مثل غياب المراجعين الشرعيين، تسرب أمني، خطأ مصدري شامل).
  - التوثيق المرجعي الشامل في وثيقتي `DISASTER_RECOVERY.md` و `BUSINESS_CONTINUITY.md`.
  - إجراء اختبار التعافي عبر `DisasterRecoveryTest` لضمان عمل وضع القراءة فقط وأنظمة الحماية (نجاح 100%).

## [1.0.8] - Schema Versioning & Data Migrations (PROMPT 093)
### Added
- **نظام الترحيل وإصدارات المخطط (Schema Versioning & Data Migrations):**
  - بناء محرك ترحيل خلفي (`MigrationEngine`) لترقية مخططات البيانات (Schema Versions) بشكل آمن ومنفصل عن تطبيق العميل.
  - دعم خصائص الترحيل المتقدمة:
    - **التشغيل التجريبي (Dry Run):** محاكاة الترقيات واكتشاف الأخطاء بدون حفظ فعلي.
    - **الترحيل على دفعات (Batching):** معالجة الوثائق بدفعات محدودة الحجم (100 وثيقة) لتفادي حدود Firestore.
    - **اكتشاف الوثائق القديمة:** فحص `schemaVersion` لكل وثيقة بشكل ديناميكي.
  - حوكمة الأمان والبيانات:
    - التحقق المسبق الإلزامي من وجود نسخة احتياطية (Pre-Migration Backup Gate) للترحيل الحقيقي.
    - نظام قفل التزامن (Concurrent Lock) لمنع تضارب عمليات الترحيل في نفس الوقت.
    - الحفاظ التام على التوافق الخلفي (عدم حذف الحقول القديمة وعدم تغيير معانيها).
  - نماذج البيانات (`domain/models/migration/`):
    - `MigrationJob`, `MigrationStatus`, `MigrationError`, `DocumentSchemaState`, `SchemaConstants`.
  - واجهة وتطبيق المستودع الميداني: `MigrationRepository` و `MigrationRepositoryImpl`.
  - التوثيق المرجعي الشامل في وثيقة `DATA_MIGRATIONS.md`.
  - اختبارات الوحدة الشاملة: `MigrationTest` لاختبار نجاح الـ Dry Run، والتأكد من توافق المخطط، واختبار قفل التزامن (نجاح 100%).

## [1.0.7] - Admin Security Controls (PROMPT 092)
### Added
- **أمان الحسابات الإدارية (Admin Security Controls):**
  - فرض المصادقة ثنائية العوامل (MFA) لكافة أدوار الإدارة (`OWNER`, `ADMIN`, `REVIEWER`).
  - نماذج البيانات (`domain/models/admin/`):
    - `AdminRole`, `SensitiveOperationType`, `AdminDevice`, `AdminSession`, `AdminSecurityConfig`, `SecurityAuditLog`.
  - محرك حوكمة الإدارة (`AdminSecurityEngine`):
    - إعادة التحقق التلقائي للعمليات الحساسة (مثل `DELETE_CONTENT`، `EXPORT_DATA`) من الأجهزة غير الموثوقة.
    - تقييد الجلسات الخاملة (Idle Timeout) وتعليقها التلقائي.
    - تطبيق مبدأ أقل امتيازاً ومنع تجاوز الصلاحيات (Admin vs Owner).
  - واجهة وتطبيق المستودع الميداني: `AdminSecurityRepository` و `AdminSecurityRepositoryImpl`.
  - شاشات أمان الإدارة: `AdminSecurityDashboardScreen` لرؤية الأجهزة النشطة والـ IPs، وإمكانية إبطال (Revoke) أي جلسة مشبوهة.
  - التوثيق المرجعي الشامل في وثيقة `ADMIN_SECURITY.md`.
  - اختبارات الوحدة الشاملة: `AdminSecurityTest` للتحقق من منع الجلسات بدون MFA، إبطال الجلسات، واختبار مهلة الجمود (نجاح 100%).

## [1.0.6] - Cost & Limits Management System (PROMPT 091)
### Added
- **إدارة التكلفة والحدود (Cost & Limits Management System):**
  - تصنيف وحوكمة الحدود بـ 6 فئات رئيسية: حد يومي، حد شهري، حد مستخدم، حد عملية، حد إعادة توليد، حدود فيزيائية (حجم/مدة).
  - نماذج البيانات (`domain/models/cost/`):
    - `CostProvider`, `OperationType`, `WorkspaceLimits`, `UsageMetrics`, `CostTransaction`, `CostEstimate`, `AlertLevel`, `WorkspaceUsageStatus`, `ProviderEmergencyStatus`.
  - محرك التكاليف (`CostEngine`):
    - فحص دقيق للحدود قبل التنفيذ بناءً على الحجز (Reservation) لمنع تجاوز السقف المحدد.
  - واجهة وتطبيق المستودع الميداني: `CostManagementRepository` و `CostManagementRepositoryImpl`.
  - حماية الازدواجية (Idempotency):
    - منع تكرار الخصم لنفس العملية عند إعادة المحاولة بسبب انقطاع الشبكة.
  - شاشات المالك: `CostDashboardScreen` لرؤية الاستهلاك الكلي، وتفعيل مفتاح الطوارئ `Emergency Switch`.
  - التوثيق المرجعي الشامل في وثيقة `COST_CONTROLS.md`.
  - اختبارات الوحدة الشاملة: `CostManagementTest` للتحقق من سلامة الحجز، التنبيهات، وحماية الدفع المزدوج (نجاح 100%).

## [1.0.5] - Secrets & Key Management Lifecycle (PROMPT 090)
### Added
- **إدارة الأسرار والمفاتيح ودورة حياتها الشاملة (Secrets & Key Management Lifecycle):**
  - تصنيف وحوكمة 10 فئات أسرار رئيسية:
    - `GEMINI_API_KEY`, `IMAGE_PROVIDER_KEY`, `AUDIO_PROVIDER_KEY`, `VIDEO_PROVIDER_KEY`, `GOOGLE_PLAY_CREDENTIALS`, `APPLE_PRIVATE_KEY`, `FIREBASE_ADMIN_CREDENTIALS`, `WEBHOOK_SECRET`, `SIGNING_KEY`, `DATABASE_CREDENTIALS`.
  - نماذج البيانات الوصفية وسجلات التدقيق وحوادث الطوارئ (`domain/models/secrets/`):
    - `SecretCategory`, `SecretEnvironment`, `SecretStatus`, `SecretOwnerTeam`, `SecretMetadata`, `SecretAccessAuditLog`, `SecretScanFinding`, `SecretScanReport`, `WebhookVerificationConfig`, `SecretLeakIncident`, `PreReleaseSecretsChecklist`.
  - محرك تطهير السجلات وحظر التسريب (`SanitizedLogger`):
    - تطهير فوري لكافة أنماط المفاتيح (Google AIza, OpenAI sk-, Bearer tokens, private keys, passwords, database URLs) واستبدالها بـ `[REDACTED_SECRET]`.
  - محرك الفحص الثابت للأسرار (`SecretScannerEngine`):
    - فحص الكود وملفات التكوين للكشف عن الأسرار ومنع الرفع إلى المستودع (Push Protection).
  - محرك حوكمة دورة حياة الأسرار (`SecretsLifecycleEngine`):
    - فرض عزل العميل التام (Zero Secrets in Client APK) وتمرير كافة الطلبات الحساسة عبر Cloud Functions Backend.
    - تدوير دوري مجدول للأسرار وإصدار نسخ جديدة وتوثيق في سجلات التدقيق المشفرة.
    - بروتوكول الإبطال الفوري لحالات الطوارئ مع فتح ملف حادثة واحتواء الأثر.
    - التحقق المشفر من صحة إشعارات Webhook باستخدام HMAC-SHA256 وحماية إعادة الإرسال (Replay Defense).
    - بوابة تدقيق ما قبل الإصدار (Pre-Release Secrets Gate) لمنع تصدير التطبيق عند وجود أي تسريب.
  - واجهة وتطبيق المستودع الميداني: `SecretsLifecycleRepository` و `SecretsLifecycleRepositoryImpl`.
  - شاشات وبوابات إدارة الأسرار: `SecretsLifecycleScreen`, `SecretsLifecycleComponents`, `SecretsLifecycleViewModel`.
  - التوثيق المرجعي الشامل في وثيقة `SECRETS_LIFECYCLE.md`.
  - حزمة الاختبارات الآلية الشاملة: `SecretsLifecycleTest` بنسبة نجاح 100%.

## [1.0.4] - Minor Safety & Child Protection System (PROMPT 089)
### Added
- **نظام وسياسة حماية القاصرين وسلامة الأطفال (Minor Safety & Child Protection System):**
  - بناء نماذج بيانات حماية القاصرين:
    - الفئات العمرية: `ADULT_18_PLUS`, `TEEN_13_TO_17`, `CHILD_UNDER_13`, `UNSPECIFIED` (وضع الحماية القصوى الافتراضي).
    - تصنيفات المتاجر الرسمية: `PEGI 3`, `Everyone 10+ / Teen`.
    - سياسة وضوابط الحماية المفروضة: `MinorSafetyPolicy`.
    - نماذج موافقة ولي الأمر: `ParentalConsentRecord`, `ParentalConsentStatus`.
    - بلاغات الطوارئ واستغلال الأطفال: `ChildSafetyIncidentReport`, `ChildSafetyIncidentType`, `IncidentUrgency`, `IncidentResolutionStatus`.
    - تدقيق المحتوى التعليمي: `EducationalContentSafetyCheck`.
    - حق المحو وتطهير بيانات القاصر: `MinorDataDeletionSummary`.
  - محرك سياسات وضوابط حماية القاصرين (`MinorSafetyEngine`):
    - إنفاذ نمط "خاص افتراضياً" (`Private by Default`) لكافة حسابات القاصرين واليافعين.
    - الحظر البرمجي للرسائل الخاصة المباشرة (Zero DMs) ومنع أي اتصال غير مراقب.
    - الحظر المطلق لجمع الإحداثيات الدقيقة (Zero Fine GPS Location) والاعتماد على التقريب العام لمواقيت الصلاة.
    - التعطيل الصارم للإعلانات الموجهة وبناء الملفات التعريفية التتبعية (COPPA / GDPR-K).
    - الحظر التام لاستخدام بيانات ومدخلات القاصرين لتدريب نماذج الذكاء الاصطناعي (Zero AI Model Training).
    - الحظر الصارم لاستنساخ أصوات الأطفال بالذكاء الاصطناعي (Voice Cloning Ban).
    - مسار الفرز والتصعيد الفوري لبلاغات استغلال أو إساءة الأطفال بمهلة استجابة عاجلة (SLA < 15 mins).
    - فاحص المحتوى التعليمي للأطفال وخلوه من محفزات الشراء المضللة والعناصر المخيفة.
    - محرك التطهير الفوري لبيانات وتسجيلات القاصر مع توليد إيصال أمني موثق (SHA-256 Checksum).
  - واجهة المستودع وتنفيذه الميداني: `MinorSafetyRepository` و `MinorSafetyRepositoryImpl`.
  - شاشات وبوابة ومكونات حماية القاصرين: `MinorSafetyScreen`, `MinorSafetyComponents`, `MinorSafetyViewModel`.
  - التوثيق المرجعي الكامل في وثيقة `MINOR_SAFETY.md`.
  - حزمة اختبارات الوحدة الشاملة: `MinorSafetyTest` بنسبة نجاح 100%.

## [1.0.3] - Content Taxonomy & Provenance System (PROMPT 088)
### Added
- **نظام تصنيف المحتوى ومصدره وحوكمة الأصول (Content Taxonomy & Provenance System):**
  - بناء نماذج البيانات المكتملة للتصنيف الموحد:
    - أنواع الأصول ومصدر النشأة: `system_content`, `editorial_content`, `user_generated`, `ai_generated`, `licensed_external`.
    - المجالات والمعارف الشرعية: `Quran_text`, `Tafsir`, `Hadith`, `Fiqh`, `Educational`, `General`.
    - أنواع الوسائط: `Text`, `Audio`, `Video`, `Image`, `Template`, `Interactive`.
    - البيانات الوصفية المركزية: `ContentTaxonomyMetadata`, `ClassifiedContentItem`, `TaxonomyAuditReport`, `TaxonomyMigrationResult`.
  - محرك حوكمة وتصنيف المحتوى (`ContentTaxonomyEngine`):
    - إقفال وحماية تامة للنص القرآني (`Quran_text`) وجعله غير قابل للتعديل (Immutable) مع قصر صلاحيات التعديل على القائمة الفارغة `[]` ومنع أي تعديل أو إسناد دور له.
    - الفصل القاطع بين النص القرآني والتفاسير والترجمات.
    - منع مساواة المحتوى المولد بالذكاء الاصطناعي (`ai_generated`) بالمحتوى التحريري الموثق شرعياً، وفرض المراجعة البشرية وفحص السلامة والحقوق.
    - منع ادعاء ملكية سراج (`SIRAJ_ORIGINAL`) للمواد المرخصة خارجياً (`licensed_external`) وإلزام ذكر التراخيص والعزو.
    - تعليق وتقييد فوري للمواد مجهولة الحقوق والترخيص (`UNKNOWN`) وحجبها عن النشر العام.
    - عزل محتوى المستخدمين (`user_generated`) عن التحرير الرسمي ومنع أي مستخدم من التعديل على محتوى مستخدم آخر.
    - التحقق الخادمي الصارم من التصنيفات وعدم الاعتماد على القيمة القادمة من العميل وحدها.
    - توجيه المحتوى تلقائياً إلى مسار المراجعة المناسب (`ReviewPipelinePath`).
    - محرك تدقيق الامتثال وحصر المواد غير المصنفة ومحرك ترحيل البيانات السابقة للمخطط الموحد.
  - واجهة المستودع وتنفيذه الميداني: `ContentTaxonomyRepository` و `ContentTaxonomyRepositoryImpl` مع فلاتر البحث المتقدم.
  - شاشات ووسوم ومكونات التصنيف والمصادر: `ContentTaxonomyManagementScreen`, `TaxonomyComponents`, `ContentTaxonomyViewModel`.
  - التوثيق المرجعي الشامل في وثيقة `CONTENT_TAXONOMY.md`.
  - حزمة اختبارات الوحدة الشاملة: `ContentTaxonomyTest` بنسبة نجاح 100%.

## [1.0.2] - Content Corrections & Sharia Versioning (PROMPT 087)
### Added
- **نظام التصحيح والإصدارات الشرعية (Content Corrections & Versioning System):**
  - بناء نماذج البيانات المكتملة: `ContentVersion`, `CorrectionNotice`, `SourceRevision`, `AffectedAsset`, `CorrectionReview`, `ImpactReport`.
  - دعم أنواع التصحيحات السبعة: `SOURCE_ERROR`, `WORDING_ERROR`, `ATTRIBUTION_ERROR`, `RIGHTS_ISSUE`, `TECHNICAL_ERROR`, `SAFETY_ISSUE`, `OTHER`.
  - دعم جهات الاكتشاف: `USER_REPORT`, `REVIEWER_AUDIT`, `CREATOR_SELF_DISCOVERY`, `SYSTEM_SCAN`.
  - محرك التصحيح والإصدارات (`ContentCorrectionEngine`):
    - حظر التعديل الصامت للمحتوى المنشور، وإنشاء إصدار جديد (`v_N+1`) مع الحفاظ الكامل على الإصدارات القديمة في السجل الثابت بصفتها (`SUPERSEDED`).
    - توثيق البصمة الرقمية للنزاهة المشفرة (`immutableHash - SHA-256`) لكل إصدار.
    - الإيقاف والتعليق الفوري للنسخ القديمة في قضايا الحقوق (`RIGHTS_ISSUE`) وقضايا السلامة (`SAFETY_ISSUE`).
    - الإحالة الإلزامية لأخطاء المصادر والألفاظ والأقوال إلى مراجع شرعي مؤهل.
    - تتبع وحصر كافة المشاهد ومقاطع الفيديو والومضات المتأثرة بالتصحيح مع توليد تقرير أثر إحصائي شامل (`ImpactReport`).
    - النشر الشفاف مع إشعار المستخدمين عبر إفصاح تصحيحي عام (`Public Correction Notice`).
  - واجهة المستودع وتنفيذه الميداني: `ContentCorrectionRepository` و `ContentCorrectionRepositoryImpl`.
  - شاشة سجل وتاريخ الإصدارات والتصحيحات: `ContentCorrectionHistoryScreen` و `ContentCorrectionViewModel`.
  - التوثيق المرجعي الكامل في وثيقة `CONTENT_CORRECTIONS.md`.
  - حزمة اختبارات الوحدة الشاملة: `ContentCorrectionTest` بنسبة نجاح 100%.

## [1.0.1] - Sharia Reviewer Governance (PROMPT 086)
### Added
- **منظومة حوكمة المراجعين الشرعيين (Sharia Reviewer Governance):**
  - بناء نماذج الحوكمة المكتملة: `ReviewerProfile`, `ReviewerQualification`, `ReviewerScope`, `ReviewerConflict`, `ReviewerAssignment`, `ReviewerDecision`.
  - دعم مجالات الاختصاص الشرعي الثمانية: `QURAN`, `HADITH`, `TAFSIR`, `FIQH`, `AQEEDAH`, `SEERAH`, `EDUCATION`, `GENERAL`.
  - إدارة حالات المراجعين: `ACTIVE`, `PENDING_VERIFICATION`, `SUSPENDED`.
  - محرك حوكمة المراجعين (`ReviewerGovernanceEngine`):
    - التحقق الصارم من الأهلية والمؤهلات وتاريخ التوثيق من المالك (Owner Verification).
    - التحقق من سقف مستوى الخطورة وتطبيق استثناء الموضوعات المحظورة لكل مراجع.
    - منع تعارض المصالح قطعيّاً (منع الصانع من مراجعة عمله وفحص القيود المؤسسية والشخصية).
    - فرض المراجعة المزدوجة (Second Reviewer) إلزامياً للموضوعات والمسائل الحرجة (Critical Topics).
    - تسجيل القرارات الشرعية في سجل ثابت غير قابل للتعديل أو الحذف (Immutable Decision Log) مرتبط برقم النسخة.
    - توليد بطاقة مراجع عامة آمنة (Sanitized Profile) لحماية البيانات الحساسة للمؤهلات.
  - واجهة مستودع الحوكمة وتنفيذه الميداني: `ReviewerGovernanceRepository` و `ReviewerGovernanceRepositoryImpl`.
  - لوحة تحكم المالك لإدارة المراجعين: `ReviewerGovernanceDashboardScreen` و `ReviewerGovernanceViewModel`.
  - توثيق شامل للحوكمة في وثيقة `REVIEWER_GOVERNANCE.md`.
  - اختبارات الوحدة الشاملة: `ReviewerGovernanceTest` بنسبة نجاح 100%.

## [1.0.0] - Final Audit & Readiness Decision (PROMPT 085)
### Added
- **التدقيق النهائي الشامل للمنظومة (25-Pillar Comprehensive Audit):** مراجعة كاملة ودقيقة لـ 25 محوراً تشغيلياً وأمنياً وشرعياً وفنياً تغطي الوظائف الأساسية، رندرة الفيديو، التوثيق والمراجعة الشرعية، موجز الومضات، تراخيص الوسائط، المصادقة والصلاحيات (RBAC)، قواعد Firestore وStorage، دوال السحابة، الذكاء الاصطناعي كمساعد إنتاج، الاشتراكات والأرصدة، المحراب وحماية القرآن، الخصوصية وحذف الحسابات، الإشراف على المحتوى، مكافحة الأعطال وتطهير السجلات، الأداء والتخزين المؤقت، إمكانية الوصول وRTL، خطة الطوارئ والنسخ الاحتياطي، وجاهزية المتاجر.
- **قرار الجاهزية الرسمي (`GO_NO_GO.md`):** إصدار قرار الجاهزية الرسمي **CONDITIONAL GO** المشروط باستكمال التجهيزات الإجرائية لبيئة الإنتاج السحابية وشهادات المتاجر.
- **مصفوفة موانع النشر والإطلاق (`RELEASE_BLOCKERS.md`):** تأكيد خلو المنظومة التام من أي مانع برمجي أو أمني أو شرعي (Zero Code Blockers).
- **التقرير النهائي لتنفيذ الاختبارات (`FINAL_TEST_REPORT.md`):** توثيق اجتياز 32 حزمة اختبارات أحادية وتكاملية بنسبة نجاح 100% وخلو المشروع من أي أخطاء بناء.
- **التقرير النهائي للأمان والبيانات (`FINAL_SECURITY_STATUS.md`):** توثيق عزل الأسرار، وتطبيق قواعد أمان Firestore وStorage، وتطهير السجلات، ومطابقة متطلبات GDPR وGoogle Play.
- **التقرير النهائي لسلامة المحتوى والامتثال الشرعي (`FINAL_CONTENT_STATUS.md`):** توثيق مناعة النص القرآني من التعديل (Immutable)، وتوثيق الأحاديث بالمراجع، وإلزامية المراجعة البشرية وسجل التصحيحات غير القابل للحذف.

## [Unreleased] - Operations & Post-Launch Roadmap (PROMPT 084)
### Added
- **خطة التشغيل لأول 90 يوماً (Operations Plan - First 90 Days):** صياغة وثيقة `OPERATIONS_PLAN.md` متضمنةً المراحل الخمس (أول 24 ساعة، أول أسبوع، أول 30 يوماً، أول 60 يوماً، أول 90 يوماً)، ومصفوفة مراقبة المحاور الـ 14 الإلزامية (الأعطال، ANR، الدخول، التصدير، الذكاء الاصطناعي، التكاليف، الاشتراكات، البلاغات، المصادر، حذف الحسابات، الاستبقاء، التقييمات، التذاكر، واستخدام الميزات).
- **خارطة طريق ما بعد الإطلاق (Post-Launch Roadmap):** صياغة وثيقة `POST_LAUNCH_ROADMAP.md` متضمنةً المعالم الخمسة ومصفوفة الأهداف والنتائج الرئيسية (OKRs) وخطة إدارة المخاطر.
- **محرك حوكمة العمليات والتحديثات (`OperationsGovernanceEngine`):**
  - تقييم حالات الطوارئ وتصنيف الـ Hotfix (P0/P1/P2) وتطبيق معايير الـ SLA الصارمة.
  - فرز مقترحات الميزات وفق نموذج RICE وتطبيق قاعدة منع إضافة أي ميزة بناءً على طلب فردي منعزل ("لا تضف ميزة لمجرد طلب واحد").
  - فحص التوافقية العكسية للمشاريع وضمان عدم كسر أي مشروع قديم مع توفير ترقية تلقائية آمنة.
  - تدقيق ميزانية FinOps وتفعيل تنبيهات 50% و80% و100% وإيقاف الميزات المكلفة تلقائياً عند بلوغ 100%.
  - تدقيق تدوير الأسرار والمفاتيح كل 90 يوماً وتنبيه الفريق قبل الاستحقاق.
  - منع تعديل مفاتيح الـ Feature Flags الأمنية والمالية من جهة تطبيق العميل.
  - سياسة التوافقية العكسية وفرض التحديثات الإلزامية عند الحاجة.
- **نماذج بيانات التشغيل (`OperationsModels.kt`):** نماذج دورة حياة الإطلاق، تقييم الـ Hotfix، تقييم RICE، مراجعات FinOps والأمان والمحتوى، وتدوير الأسرار.
- **اختبارات الوحدة الشاملة (`OperationsGovernanceTest.kt`):** 11 اختبار وحدة يغطي كافة قواعد الحوكمة والـ SLA وفرز الميزات والتوافقية والتكاليف والأمان بنجاح تام.

## [Unreleased] - UGC Moderation & Community Guidelines (PROMPT 083)
### Added
- **نظام فحص المحتوى قبل الرفع (Pre-Upload Scanning):** فحص آلي للـ Spam، خطابات الكراهية، انتهاكات الملكية، واكتشاف المحتوى الديني وتحويله تلقائياً للمراجعة البشرية.
- **إلزامية شروط الاستخدام (Terms of Service Consent):** إدراج خيار موافقة إلزامي في شاشة نشر الفلاشات `FlashPublishingScreen` وتوثيق تاريخ وإصدار الشروط.
- **لوحة تحكم الإشراف الموسعة (Moderation Dashboard):** واجهة متكاملة من 4 أقسام (`بلاغات المجتمع`، `طابور UGC`، `طلبات الاستئناف`، `مؤشرات الأداء وSLA`).
- **نظام الاستئناف وتصحيح القرارات (Appeals System):** دورة حياة كاملة للاستئناف مع إمكانية استعادة المحتوى أو تأكيد الرفض.
- **شاشة قواعد المجتمع (Community Guidelines Screen):** واجهة مخصصة لعرض ميثاق المجتمع وإرشادات النشر الإسلامي والأخلاقي، مرتبطة بالإعدادات والتنقل العام.
- **توثيق سياسة الإشراف وقواعد المجتمع:** إنشاء ملفي `UGC_MODERATION.md` و `COMMUNITY_GUIDELINES.md`.
- **اختبارات الوحدة الشاملة (Unit Tests):** إضافة `UgcModerationTest` للتحقق من كافة مسارات الفحص والقرارات والاستئناف وSLA.

## [Unreleased] - AI Content Generation Policy & Safety (PROMPT 082)
### Added
- Added `AiMetadata` model to track AI generation metadata (provider, model, humanReviewed, safetyStatus).
- Integrated `AiMetadata` into the `Asset` model to track AI details for assets.
- Created `AiContentLabel` and `AiDisclosureBottomSheet` to display "Generated by AI" tags and required disclosures.
- Created `AiReportDialog` to allow users to report abusive or misleading AI content.
- Created `AiPolicyScreen` to display the in-app AI policy and integrated it into `SettingsScreen` (About section).
- Authored `AI_CONTENT_POLICY.md` detailing guidelines, disclosure requirements, and safety policies for AI-generated content.

## [Unreleased] - Bugfix: Firebase Crashlytics Development Settings (PROMPT 077)

### Added
- نظام متكامل لإدارة الحقوق والتراخيص للأصول (Rights Management) مع سجل قرارات.
- تحديث `Asset` model لدعم معلومات الترخيص، وحالات الحقوق (Unknown, Commercial, Expired...).
- سياسة إدارة الحقوق `RIGHTS_POLICY.md` لضمان توافق جميع المحتويات.


### Added
- إعداد وثائق الإصدار النهائي (Production Readiness):
  - `RELEASE_NOTES.md`: ملاحظات الإصدار 1.0.0.
  - `PRODUCTION_CHECKLIST.md`: قائمة المراجعة للإنتاج (الكود، البنية التحتية، المتاجر، السياسات).
  - `LAUNCH_DAY_RUNBOOK.md`: خطة مراقبة يوم الإطلاق (أول 72 ساعة).
  - `ROLLBACK_PLAN.md`: خطة التراجع على مستوى المتجر والـ Backend.
  - `KNOWN_LIMITATIONS.md`: القيود الفنية في الإصدار الأول.
  - `SUPPORT_ESCALATION.md`: خطة تصعيد الدعم الفني.


### Added
- صياغة خطة الإطلاق التدريجي (Phased Rollout Plan) في ملف `ROLLOUT_PLAN.md` لضمان إطلاق آمن ومستقر.
- تحديد مقاييس المراقبة الأساسية (Crashes, ANR, AI Costs, Billing Drops) وشروط الإيقاف (Kill-switches) لكل مرحلة.


### Added
- صياغة سياسة المحتوى والاستخدام `CONTENT_POLICY.md` متضمنة مصفوفة العقوبات وقواعد المراجعين وقواعد التعامل مع الذكاء الاصطناعي.
- صياغة سياسة الاعتراضات والتصحيح `APPEALS_POLICY.md`.
- بناء واجهة المستخدم `ContentPolicyScreen` لعرض ملخص السياسات داخل التطبيق.
- إضافة فئات الدعم الفني الجديدة `APPEAL_AND_POLICY` و `SOURCE_CORRECTION` في تذاكر الدعم لتمكين الاعتراضات وتصحيح المصادر.
- ربط صفحة السياسات مع إعدادات المحتوى الشرعي في `SettingsScreen`.


### Added
- تصميم وبناء واجهات مركز الدعم الفني، المقالات، وتذاكر المساعدة (`HelpCenterScreen`, `HelpArticleDetailScreen`, `CreateTicketScreen`, `TicketListScreen`, `TicketDetailScreen`, `ServiceStatusScreen`).
- دمج مركز الدعم الفني وتوجيه المستخدمين ضمن شاشة إعدادات التطبيق `SettingsScreen` وربط جميع مسارات التنقل الجديدة في `AppNavigation`.
- حل مشكلة توافق `combine` في `SupportViewModel` بدمج المجموعات بدلاً من استخدام دوال زائدة.

### Fixed
- تصحيح إعدادات جمع بيانات Firebase Crashlytics في بيئة التطوير (`Development`) لمنع طلب الإعدادات الشبكية التلقائية ببيانات وهمية (`dummy credentials`) التي كانت تسبب خطأ `FileNotFoundException` في خوادم Crashlytics.
- إضافة `firebase_crashlytics_collection_enabled = false` كبيانات وصفية (meta-data) في `AndroidManifest.xml` لتعطيل الجمع التلقائي عند الإقلاع وتفعيله فقط للإنتاج عبر `SirajApplication.kt`.

## [Unreleased] - Incident Response Plan & Emergency Action Hub (PROMPT 076)

### Added
- نظام متكامل لإدارة الحقوق والتراخيص للأصول (Rights Management) مع سجل قرارات.
- تحديث `Asset` model لدعم معلومات الترخيص، وحالات الحقوق (Unknown, Commercial, Expired...).
- سياسة إدارة الحقوق `RIGHTS_POLICY.md` لضمان توافق جميع المحتويات.


### Added
- إعداد وثائق الإصدار النهائي (Production Readiness):
  - `RELEASE_NOTES.md`: ملاحظات الإصدار 1.0.0.
  - `PRODUCTION_CHECKLIST.md`: قائمة المراجعة للإنتاج (الكود، البنية التحتية، المتاجر، السياسات).
  - `LAUNCH_DAY_RUNBOOK.md`: خطة مراقبة يوم الإطلاق (أول 72 ساعة).
  - `ROLLBACK_PLAN.md`: خطة التراجع على مستوى المتجر والـ Backend.
  - `KNOWN_LIMITATIONS.md`: القيود الفنية في الإصدار الأول.
  - `SUPPORT_ESCALATION.md`: خطة تصعيد الدعم الفني.


### Added
- صياغة خطة الإطلاق التدريجي (Phased Rollout Plan) في ملف `ROLLOUT_PLAN.md` لضمان إطلاق آمن ومستقر.
- تحديد مقاييس المراقبة الأساسية (Crashes, ANR, AI Costs, Billing Drops) وشروط الإيقاف (Kill-switches) لكل مرحلة.


### Added
- صياغة سياسة المحتوى والاستخدام `CONTENT_POLICY.md` متضمنة مصفوفة العقوبات وقواعد المراجعين وقواعد التعامل مع الذكاء الاصطناعي.
- صياغة سياسة الاعتراضات والتصحيح `APPEALS_POLICY.md`.
- بناء واجهة المستخدم `ContentPolicyScreen` لعرض ملخص السياسات داخل التطبيق.
- إضافة فئات الدعم الفني الجديدة `APPEAL_AND_POLICY` و `SOURCE_CORRECTION` في تذاكر الدعم لتمكين الاعتراضات وتصحيح المصادر.
- ربط صفحة السياسات مع إعدادات المحتوى الشرعي في `SettingsScreen`.


### Added
- تصميم وبناء واجهات مركز الدعم الفني، المقالات، وتذاكر المساعدة (`HelpCenterScreen`, `HelpArticleDetailScreen`, `CreateTicketScreen`, `TicketListScreen`, `TicketDetailScreen`, `ServiceStatusScreen`).
- دمج مركز الدعم الفني وتوجيه المستخدمين ضمن شاشة إعدادات التطبيق `SettingsScreen` وربط جميع مسارات التنقل الجديدة في `AppNavigation`.
- حل مشكلة توافق `combine` في `SupportViewModel` بدمج المجموعات بدلاً من استخدام دوال زائدة.

### Added
- إنشاء نماذج الاستجابة للحوادث `IncidentResponseModels.kt` (الأنواع الـ 10، المراحل الـ 8، الأدوار المسؤولة، مستويات الخطورة P0 إلى P3، تقارير ما بعد الحادث Post-Mortems، والتصحيح الشرعي المزدوج).
- إنشاء محرك الاستجابة للطوارئ `IncidentResponseEngine.kt`:
  - التحقق من اعتماد مراجعين شرعيين اثنين (Double Review) لتصويب النصوص القرآنية والدينية قبل النشر مع رفع رقم الإصدار وتوثيق السند.
  - تجريد إشعارات الحوادث العامة للمستخدمين لمنع تسرب أي أسرار أو تفاصيل أمنية يستغلها مهاجم.
  - التحقق من تفويض الأدوار (Separation of Duties) للإجراءات الطارئة كالإيقاف الشامل وتدوير المفاتيح والاسترداد المالي.
- تعريف واجهة المستودع `IncidentResponseRepository` وتطبيقها في `FirebaseIncidentResponseRepositoryImpl`.
- تطوير لوحة الاستجابة للطوارئ `IncidentResponseScreen` و `IncidentResponseViewModel` ودمجها كالتبويب الخامس في `AdminScreen`:
  - مركز التدخل السريع والإجراءات الطارئة الفورية (Kill-Switch، تدوير المفاتيح، تصحيح شرعي، استرداد مالي، وسحب مشاريع).
  - أدلة وخطط الاستجابة التفاعلية (Playbooks) للأنواع الـ 10 من الحوادث مع المراحل الـ 8 وصيغ التواصل العامة.
  - إدارة تقارير ما بعد الحادث (Post-Mortem Reports) وعرض أسبابها الجذرية وإجراءاتها التصحيحية والوقائية.
  - مصفوفة جهات الاتصال وقنوات التصعيد المشفرة 24/7.
- تأمين قواعد `firestore.rules` لمجموعات `incident_reports` و `emergency_actions` و `sharia_corrections` و `escalation_contacts`.
- إضافة نقطة التدخل الطارئ السحابية `executeEmergencyContainmentAction` في `functions/src/index.ts`.
- إنشاء الوثيقة التوثيقية الشاملة `INCIDENT_RESPONSE.md`.
- كتابة وتمرير الاختبارات الشاملة `IncidentResponseTest.kt`.

## [Unreleased] - Platform Services Health Monitoring (PROMPT 075)

### Added
- نظام متكامل لإدارة الحقوق والتراخيص للأصول (Rights Management) مع سجل قرارات.
- تحديث `Asset` model لدعم معلومات الترخيص، وحالات الحقوق (Unknown, Commercial, Expired...).
- سياسة إدارة الحقوق `RIGHTS_POLICY.md` لضمان توافق جميع المحتويات.


### Added
- إعداد وثائق الإصدار النهائي (Production Readiness):
  - `RELEASE_NOTES.md`: ملاحظات الإصدار 1.0.0.
  - `PRODUCTION_CHECKLIST.md`: قائمة المراجعة للإنتاج (الكود، البنية التحتية، المتاجر، السياسات).
  - `LAUNCH_DAY_RUNBOOK.md`: خطة مراقبة يوم الإطلاق (أول 72 ساعة).
  - `ROLLBACK_PLAN.md`: خطة التراجع على مستوى المتجر والـ Backend.
  - `KNOWN_LIMITATIONS.md`: القيود الفنية في الإصدار الأول.
  - `SUPPORT_ESCALATION.md`: خطة تصعيد الدعم الفني.


### Added
- صياغة خطة الإطلاق التدريجي (Phased Rollout Plan) في ملف `ROLLOUT_PLAN.md` لضمان إطلاق آمن ومستقر.
- تحديد مقاييس المراقبة الأساسية (Crashes, ANR, AI Costs, Billing Drops) وشروط الإيقاف (Kill-switches) لكل مرحلة.


### Added
- صياغة سياسة المحتوى والاستخدام `CONTENT_POLICY.md` متضمنة مصفوفة العقوبات وقواعد المراجعين وقواعد التعامل مع الذكاء الاصطناعي.
- صياغة سياسة الاعتراضات والتصحيح `APPEALS_POLICY.md`.
- بناء واجهة المستخدم `ContentPolicyScreen` لعرض ملخص السياسات داخل التطبيق.
- إضافة فئات الدعم الفني الجديدة `APPEAL_AND_POLICY` و `SOURCE_CORRECTION` في تذاكر الدعم لتمكين الاعتراضات وتصحيح المصادر.
- ربط صفحة السياسات مع إعدادات المحتوى الشرعي في `SettingsScreen`.


### Added
- تصميم وبناء واجهات مركز الدعم الفني، المقالات، وتذاكر المساعدة (`HelpCenterScreen`, `HelpArticleDetailScreen`, `CreateTicketScreen`, `TicketListScreen`, `TicketDetailScreen`, `ServiceStatusScreen`).
- دمج مركز الدعم الفني وتوجيه المستخدمين ضمن شاشة إعدادات التطبيق `SettingsScreen` وربط جميع مسارات التنقل الجديدة في `AppNavigation`.
- حل مشكلة توافق `combine` في `SupportViewModel` بدمج المجموعات بدلاً من استخدام دوال زائدة.

### Added
- إنشاء نماذج المراقبة الشاملة `MonitoringModels.kt` (13 خدمة ومزوداً تشغيلياً، حالات الصحة، مستويات الخطورة P0 إلى P3، بلاغات الأعطال، والتنبيهات).
- إنشاء محرك فحص الصحة ومطابقة الأخطاء `HealthMonitoringEngine.kt` الذي يتضمن:
  - منع تكرار التنبيهات عبر `computeDeduplicationHash` (MD5 hashing).
  - تجريد أسرار البنية التحتية والمسارات والـ tokens وتوليد رسائل عربية مهذبة للمستخدمين `sanitizeForUser`.
  - تقييم شروط قاطع الدائرة `shouldTripCircuitBreaker` عند تكرار 3 أخطاء متتالية أو ارتفاع زمن الاستجابة عن المهلة أو تجاوز معدل الخطأ 50%.
  - استخدام رموز فحص اصطناعية محايدة ومجردة من أي نصوص قرآنية أو دينية (`SIRAJ_SYSTEM_HEALTH_CHECK_SYNTHETIC_PING_2026`).
- تعريف واجهة المستودع `MonitoringRepository` وتنفيذها في `FirebaseMonitoringRepositoryImpl`.
- تطوير لوحة المراقبة `MonitoringDashboardScreen` و `MonitoringDashboardViewModel` ودمجها داخل شاشة الإدارة `AdminScreen`:
  - شريط إحصائيات عامة (Uptime, Avg Latency, Crash-free users, Active Incidents).
  - قائمة حالة الخدمات الـ 13 مع فلترة بالتصنيف وإمكانية الفحص اليدوي المباشر وتفعيل/تعطيل قواطع الدائرة.
  - إدارة بلاغات الأعطال الحية والتاريخية (Incidents) مع خط زمني تفاعلي وتحديث الحالات.
  - سجل التنبيهات النشطة وإمكانية إقرارها وحلها.
  - نافذة دليل تشغيل الأعطال التفاعلي (Runbooks Modal) المخصص لكل خدمة.
- تأمين قواعد `firestore.rules` لمجموعات `system_health_probes` و `service_incidents` و `monitoring_alerts`.
- إضافة دالة `checkSystemHealth` في `functions/src/index.ts`.
- إنشاء الوثائق التوثيقية الشاملة `MONITORING.md` و `INCIDENT_RUNBOOK.md`.
- كتابة وتمرير الاختبارات الشاملة `ServiceHealthMonitoringTest.kt`.

## [Unreleased] - Backup & Disaster Recovery Policy (PROMPT 074)

### Added
- نظام متكامل لإدارة الحقوق والتراخيص للأصول (Rights Management) مع سجل قرارات.
- تحديث `Asset` model لدعم معلومات الترخيص، وحالات الحقوق (Unknown, Commercial, Expired...).
- سياسة إدارة الحقوق `RIGHTS_POLICY.md` لضمان توافق جميع المحتويات.


### Added
- إعداد وثائق الإصدار النهائي (Production Readiness):
  - `RELEASE_NOTES.md`: ملاحظات الإصدار 1.0.0.
  - `PRODUCTION_CHECKLIST.md`: قائمة المراجعة للإنتاج (الكود، البنية التحتية، المتاجر، السياسات).
  - `LAUNCH_DAY_RUNBOOK.md`: خطة مراقبة يوم الإطلاق (أول 72 ساعة).
  - `ROLLBACK_PLAN.md`: خطة التراجع على مستوى المتجر والـ Backend.
  - `KNOWN_LIMITATIONS.md`: القيود الفنية في الإصدار الأول.
  - `SUPPORT_ESCALATION.md`: خطة تصعيد الدعم الفني.


### Added
- صياغة خطة الإطلاق التدريجي (Phased Rollout Plan) في ملف `ROLLOUT_PLAN.md` لضمان إطلاق آمن ومستقر.
- تحديد مقاييس المراقبة الأساسية (Crashes, ANR, AI Costs, Billing Drops) وشروط الإيقاف (Kill-switches) لكل مرحلة.


### Added
- صياغة سياسة المحتوى والاستخدام `CONTENT_POLICY.md` متضمنة مصفوفة العقوبات وقواعد المراجعين وقواعد التعامل مع الذكاء الاصطناعي.
- صياغة سياسة الاعتراضات والتصحيح `APPEALS_POLICY.md`.
- بناء واجهة المستخدم `ContentPolicyScreen` لعرض ملخص السياسات داخل التطبيق.
- إضافة فئات الدعم الفني الجديدة `APPEAL_AND_POLICY` و `SOURCE_CORRECTION` في تذاكر الدعم لتمكين الاعتراضات وتصحيح المصادر.
- ربط صفحة السياسات مع إعدادات المحتوى الشرعي في `SettingsScreen`.


### Added
- تصميم وبناء واجهات مركز الدعم الفني، المقالات، وتذاكر المساعدة (`HelpCenterScreen`, `HelpArticleDetailScreen`, `CreateTicketScreen`, `TicketListScreen`, `TicketDetailScreen`, `ServiceStatusScreen`).
- دمج مركز الدعم الفني وتوجيه المستخدمين ضمن شاشة إعدادات التطبيق `SettingsScreen` وربط جميع مسارات التنقل الجديدة في `AppNavigation`.
- حل مشكلة توافق `combine` في `SupportViewModel` بدمج المجموعات بدلاً من استخدام دوال زائدة.

### Added
- إنشاء نماذج واستراتيجية النسخ الاحتياطي والتعافي من الكوارث `BackupModels.kt` (الأنواع: Full, Incremental, Metadata-only, Disaster Recovery، الحالات، السياسات، وأدوار الصلاحيات).
- إنشاء معالج إدارة وفحص النسخ `BackupDisasterRecoveryManager.kt` للتحقق من تواقيع SHA-256، وتجريد الأسرار من البيانات الوصفية، وتطهير واستبعاد بيانات الحسابات المحذوفة (Tombstones).
- تعريف واجهة المستودع `BackupRepository` وتنفيذها في `FirebaseBackupRepositoryImpl` للتكامل مع Firestore و Cloud Storage مع مستودعات معزولة.
- تطوير لوحة التحكم `BackupRecoveryScreen` و `BackupRecoveryViewModel` داخل شاشة الإدارة `AdminScreen` لدعم إطلاق النسخ المشفر، واختبارات الاستعادة التجريبية (Dry-run) في Sandbox معزول، واستعادة المشاريع الفردية، ودليل الطوارئ.
- تأمين قواعد `firestore.rules` لحصر الوصول لمجموعات `backup_snapshots` و `backup_logs` و `restore_jobs` على المديرين فقط (Admin Claim).
- إضافة دوال Cloud Functions (`triggerBackupSnapshot`, `executeDryRunRestoreTest`) في `functions/src/index.ts`.
- إنشاء الوثيقة التوثيقية الشاملة `BACKUP_POLICY.md` متضمنة RPO (< 1 ساعة) و RTO (< 4 ساعات) وسياسات التشفير CMEK وقفل WORM وحق النسيان.
- كتابة وتمرير الاختبارات الشاملة `BackupRecoveryTest.kt`.

## [Unreleased] - Beta Feedback Triage & Defect Management (PROMPT 073)

### Added
- نظام متكامل لإدارة الحقوق والتراخيص للأصول (Rights Management) مع سجل قرارات.
- تحديث `Asset` model لدعم معلومات الترخيص، وحالات الحقوق (Unknown, Commercial, Expired...).
- سياسة إدارة الحقوق `RIGHTS_POLICY.md` لضمان توافق جميع المحتويات.


### Added
- إعداد وثائق الإصدار النهائي (Production Readiness):
  - `RELEASE_NOTES.md`: ملاحظات الإصدار 1.0.0.
  - `PRODUCTION_CHECKLIST.md`: قائمة المراجعة للإنتاج (الكود، البنية التحتية، المتاجر، السياسات).
  - `LAUNCH_DAY_RUNBOOK.md`: خطة مراقبة يوم الإطلاق (أول 72 ساعة).
  - `ROLLBACK_PLAN.md`: خطة التراجع على مستوى المتجر والـ Backend.
  - `KNOWN_LIMITATIONS.md`: القيود الفنية في الإصدار الأول.
  - `SUPPORT_ESCALATION.md`: خطة تصعيد الدعم الفني.


### Added
- صياغة خطة الإطلاق التدريجي (Phased Rollout Plan) في ملف `ROLLOUT_PLAN.md` لضمان إطلاق آمن ومستقر.
- تحديد مقاييس المراقبة الأساسية (Crashes, ANR, AI Costs, Billing Drops) وشروط الإيقاف (Kill-switches) لكل مرحلة.


### Added
- صياغة سياسة المحتوى والاستخدام `CONTENT_POLICY.md` متضمنة مصفوفة العقوبات وقواعد المراجعين وقواعد التعامل مع الذكاء الاصطناعي.
- صياغة سياسة الاعتراضات والتصحيح `APPEALS_POLICY.md`.
- بناء واجهة المستخدم `ContentPolicyScreen` لعرض ملخص السياسات داخل التطبيق.
- إضافة فئات الدعم الفني الجديدة `APPEAL_AND_POLICY` و `SOURCE_CORRECTION` في تذاكر الدعم لتمكين الاعتراضات وتصحيح المصادر.
- ربط صفحة السياسات مع إعدادات المحتوى الشرعي في `SettingsScreen`.


### Added
- تصميم وبناء واجهات مركز الدعم الفني، المقالات، وتذاكر المساعدة (`HelpCenterScreen`, `HelpArticleDetailScreen`, `CreateTicketScreen`, `TicketListScreen`, `TicketDetailScreen`, `ServiceStatusScreen`).
- دمج مركز الدعم الفني وتوجيه المستخدمين ضمن شاشة إعدادات التطبيق `SettingsScreen` وربط جميع مسارات التنقل الجديدة في `AppNavigation`.
- حل مشكلة توافق `combine` في `SupportViewModel` بدمج المجموعات بدلاً من استخدام دوال زائدة.

### Added
- إنشاء نماذج إدارة العيوب والفرز `DefectManagementModels.kt` مع التصنيفات الثمانية: `blocker`, `critical`, `major`, `minor`, `enhancement`, `duplicate`, `not_reproducible`, و `expected_behavior`، والأولويات P0 إلى P3، ومجالات الاختصاص (المحتوى الشرعي، ستوديو المونتاج، التشغيل الصوتي، دون اتصال، إمكانية الوصول، والأداء).
- بناء وتكامل مستودع الفرز وإدارة العيوب `BetaDefectManagementRepository` عبر `FirebaseBetaDefectManagementRepositoryImpl` مع ربطه بمجموعة `beta_defects` في Cloud Firestore.
- تطبيق الضوابط الصارمة لفرز العيوب:
  - أي خطأ في مصدر أو نص ديني يصنف تلقائياً كـ `critical` أو `blocker` ولا يجوز خفضه لتصنيف منخفض.
  - إلزامية كتابة سبب الإغلاق أو التأجيل `closureReason` عند نقل أي عيب لحالة الإغلاق.
  - إلزامية توثيق تفاصيل الحل الفني `resolutionNote` ومرجع الاختبار للأعطال الحرجة والمانعة للإطلاق.
  - منع تفعيل أو تنفيذ تذاكر التحسينات (`enhancement`) قبل إغلاق الأعطال الحرجة.
  - توفير سجلات تشخيص آمنة تخلو تماماً من البيانات الشخصية والمعلومات الحساسة (PII).
- تطوير شاشة الفرز التفاعلية `DefectTriageScreen` و `DefectTriageViewModel` مع بطاقات مؤشرات القياس، شريط البحث، فلاتر التصنيفات الثمانية، فلاتر النطاقات، بطاقات تفاصيل العيوب، وحوارات الفرز وتحديث دورة حياة العيب.
- ربط مسار `Screen.DefectTriage` بنظام التنقل الرئيسي ومركز خدمات المختبرين `TesterHubScreen`.
- تحديث قواعد الحماية `firestore.rules` لتأمين مجموعة `beta_defects`.
- إنشاء الوثيقة التوثيقية الشاملة `BETA_FEEDBACK.md`.
- كتابة وتمرير الاختبارات الوحدوية الشاملة `DefectManagementUnitTest`.

## [Unreleased] - Tester Distribution & Experience Hub (PROMPT 072)

### Added
- نظام متكامل لإدارة الحقوق والتراخيص للأصول (Rights Management) مع سجل قرارات.
- تحديث `Asset` model لدعم معلومات الترخيص، وحالات الحقوق (Unknown, Commercial, Expired...).
- سياسة إدارة الحقوق `RIGHTS_POLICY.md` لضمان توافق جميع المحتويات.


### Added
- إعداد وثائق الإصدار النهائي (Production Readiness):
  - `RELEASE_NOTES.md`: ملاحظات الإصدار 1.0.0.
  - `PRODUCTION_CHECKLIST.md`: قائمة المراجعة للإنتاج (الكود، البنية التحتية، المتاجر، السياسات).
  - `LAUNCH_DAY_RUNBOOK.md`: خطة مراقبة يوم الإطلاق (أول 72 ساعة).
  - `ROLLBACK_PLAN.md`: خطة التراجع على مستوى المتجر والـ Backend.
  - `KNOWN_LIMITATIONS.md`: القيود الفنية في الإصدار الأول.
  - `SUPPORT_ESCALATION.md`: خطة تصعيد الدعم الفني.


### Added
- صياغة خطة الإطلاق التدريجي (Phased Rollout Plan) في ملف `ROLLOUT_PLAN.md` لضمان إطلاق آمن ومستقر.
- تحديد مقاييس المراقبة الأساسية (Crashes, ANR, AI Costs, Billing Drops) وشروط الإيقاف (Kill-switches) لكل مرحلة.


### Added
- صياغة سياسة المحتوى والاستخدام `CONTENT_POLICY.md` متضمنة مصفوفة العقوبات وقواعد المراجعين وقواعد التعامل مع الذكاء الاصطناعي.
- صياغة سياسة الاعتراضات والتصحيح `APPEALS_POLICY.md`.
- بناء واجهة المستخدم `ContentPolicyScreen` لعرض ملخص السياسات داخل التطبيق.
- إضافة فئات الدعم الفني الجديدة `APPEAL_AND_POLICY` و `SOURCE_CORRECTION` في تذاكر الدعم لتمكين الاعتراضات وتصحيح المصادر.
- ربط صفحة السياسات مع إعدادات المحتوى الشرعي في `SettingsScreen`.


### Added
- تصميم وبناء واجهات مركز الدعم الفني، المقالات، وتذاكر المساعدة (`HelpCenterScreen`, `HelpArticleDetailScreen`, `CreateTicketScreen`, `TicketListScreen`, `TicketDetailScreen`, `ServiceStatusScreen`).
- دمج مركز الدعم الفني وتوجيه المستخدمين ضمن شاشة إعدادات التطبيق `SettingsScreen` وربط جميع مسارات التنقل الجديدة في `AppNavigation`.
- حل مشكلة توافق `combine` في `SupportViewModel` بدمج المجموعات بدلاً من استخدام دوال زائدة.

### Added
- إنشاء نماذج إدارة وتوزيع المختبرين `TesterDistributionModels.kt` التي تغطي مجموعات المختبرين (`TesterGroup`), حالات النشاط (`TesterStatus`), المسارات الحرجة (`CriticalJourney`), واستبيانات التجربة (`TesterExperienceSurvey`).
- تنفيذ مستودع توزيع المختبرين `BetaTesterDistributionRepository` عبر `FirebaseBetaTesterDistributionRepositoryImpl` لتسجيل الجلسات، حفظ التقدم في المسارات، وتجميع الاستبيانات.
- بناء شاشة مركز المختبرين `TesterHubScreen` مع دعم تتبع المسارات الأساسية، أدلة تثبيت وتحديث التطبيق لأجهزة Android و iOS، نموذج استبيان الرضا الشامل، وملاحظات الإصدار.
- ربط المسار `Screen.TesterHub` في نظام التنقل الرئيسي `AppNavigation` وتوفير الوصول إليه عبر شريط البيتا `BetaBadgeBanner` وفي شاشة الإعدادات `SettingsScreen`.
- تحديث `firestore.rules` لتأمين مجموعات `/beta_testers` و `/beta_experience_surveys` وتطبيق مبدأ أقل صلاحية.
- كتابة وثيقة سياسات وإجراءات التوزيع `BETA_DISTRIBUTION.md`.
- كتابة وتمرير الاختبارات الوحدوية `TesterDistributionUnitTest`.

## [Unreleased] - Beta Version Setup & Feedback Mechanism (PROMPT 071)

### Added
- نظام متكامل لإدارة الحقوق والتراخيص للأصول (Rights Management) مع سجل قرارات.
- تحديث `Asset` model لدعم معلومات الترخيص، وحالات الحقوق (Unknown, Commercial, Expired...).
- سياسة إدارة الحقوق `RIGHTS_POLICY.md` لضمان توافق جميع المحتويات.


### Added
- إعداد وثائق الإصدار النهائي (Production Readiness):
  - `RELEASE_NOTES.md`: ملاحظات الإصدار 1.0.0.
  - `PRODUCTION_CHECKLIST.md`: قائمة المراجعة للإنتاج (الكود، البنية التحتية، المتاجر، السياسات).
  - `LAUNCH_DAY_RUNBOOK.md`: خطة مراقبة يوم الإطلاق (أول 72 ساعة).
  - `ROLLBACK_PLAN.md`: خطة التراجع على مستوى المتجر والـ Backend.
  - `KNOWN_LIMITATIONS.md`: القيود الفنية في الإصدار الأول.
  - `SUPPORT_ESCALATION.md`: خطة تصعيد الدعم الفني.


### Added
- صياغة خطة الإطلاق التدريجي (Phased Rollout Plan) في ملف `ROLLOUT_PLAN.md` لضمان إطلاق آمن ومستقر.
- تحديد مقاييس المراقبة الأساسية (Crashes, ANR, AI Costs, Billing Drops) وشروط الإيقاف (Kill-switches) لكل مرحلة.


### Added
- صياغة سياسة المحتوى والاستخدام `CONTENT_POLICY.md` متضمنة مصفوفة العقوبات وقواعد المراجعين وقواعد التعامل مع الذكاء الاصطناعي.
- صياغة سياسة الاعتراضات والتصحيح `APPEALS_POLICY.md`.
- بناء واجهة المستخدم `ContentPolicyScreen` لعرض ملخص السياسات داخل التطبيق.
- إضافة فئات الدعم الفني الجديدة `APPEAL_AND_POLICY` و `SOURCE_CORRECTION` في تذاكر الدعم لتمكين الاعتراضات وتصحيح المصادر.
- ربط صفحة السياسات مع إعدادات المحتوى الشرعي في `SettingsScreen`.


### Added
- تصميم وبناء واجهات مركز الدعم الفني، المقالات، وتذاكر المساعدة (`HelpCenterScreen`, `HelpArticleDetailScreen`, `CreateTicketScreen`, `TicketListScreen`, `TicketDetailScreen`, `ServiceStatusScreen`).
- دمج مركز الدعم الفني وتوجيه المستخدمين ضمن شاشة إعدادات التطبيق `SettingsScreen` وربط جميع مسارات التنقل الجديدة في `AppNavigation`.
- حل مشكلة توافق `combine` في `SupportViewModel` بدمج المجموعات بدلاً من استخدام دوال زائدة.

### Added
- تكوين إعدادات إصدار النسخة التجريبية `سراج (Beta)` مع معرّف الحزمة المستقل `com.siraj.app.beta` ورقم الإصدار `1.0.0-beta.1`.
- تحديث `EnvironmentConfig.kt` لدعم معرّفات الإصدار والبناء (`versionName`, `versionCode`, `isBeta`, `allowMockData`).
- إنشاء نموذج ومستودع ملاحظات البيتا `BetaFeedback` و `FirebaseBetaFeedbackRepositoryImpl` لحفظ تقارير المختبرين وربطها سحابياً في Firestore `/beta_feedback`.
- تصميم مكونات التمييز البصري للنسخة التجريبية: `BetaBadgeBanner` للشاشات، `BetaFloatingFeedbackButton` للوصول السريع، و `BetaFeedbackDialog` المتكامل.
- تجميع التشخيصات الفنية التلقائية (موديل الجهاز، إصدار أندرويد، المسار الحالي، النسخة) بأمان عند إرسال الملاحظة.
- ربط آلية تقديم الملاحظات داخل `HomeScreen` وفي شاشتي الدعم وحول التطبيق في `SettingsPages`.
- إضافة قواعد الأمان الصارمة لمجموعة `beta_feedback` في `firestore.rules`.
- إنشاء الوثيقة المرجعية الشاملة لإطلاق البيتا `BETA_RELEASE.md`.
- كتابة وتمرير الاختبارات الوحدوية `BetaFeedbackViewModelTest`.

## [Unreleased] - MVP Readiness Decision & Scope Definition (PROMPT 070)

### Added
- نظام متكامل لإدارة الحقوق والتراخيص للأصول (Rights Management) مع سجل قرارات.
- تحديث `Asset` model لدعم معلومات الترخيص، وحالات الحقوق (Unknown, Commercial, Expired...).
- سياسة إدارة الحقوق `RIGHTS_POLICY.md` لضمان توافق جميع المحتويات.


### Added
- إعداد وثائق الإصدار النهائي (Production Readiness):
  - `RELEASE_NOTES.md`: ملاحظات الإصدار 1.0.0.
  - `PRODUCTION_CHECKLIST.md`: قائمة المراجعة للإنتاج (الكود، البنية التحتية، المتاجر، السياسات).
  - `LAUNCH_DAY_RUNBOOK.md`: خطة مراقبة يوم الإطلاق (أول 72 ساعة).
  - `ROLLBACK_PLAN.md`: خطة التراجع على مستوى المتجر والـ Backend.
  - `KNOWN_LIMITATIONS.md`: القيود الفنية في الإصدار الأول.
  - `SUPPORT_ESCALATION.md`: خطة تصعيد الدعم الفني.


### Added
- صياغة خطة الإطلاق التدريجي (Phased Rollout Plan) في ملف `ROLLOUT_PLAN.md` لضمان إطلاق آمن ومستقر.
- تحديد مقاييس المراقبة الأساسية (Crashes, ANR, AI Costs, Billing Drops) وشروط الإيقاف (Kill-switches) لكل مرحلة.


### Added
- صياغة سياسة المحتوى والاستخدام `CONTENT_POLICY.md` متضمنة مصفوفة العقوبات وقواعد المراجعين وقواعد التعامل مع الذكاء الاصطناعي.
- صياغة سياسة الاعتراضات والتصحيح `APPEALS_POLICY.md`.
- بناء واجهة المستخدم `ContentPolicyScreen` لعرض ملخص السياسات داخل التطبيق.
- إضافة فئات الدعم الفني الجديدة `APPEAL_AND_POLICY` و `SOURCE_CORRECTION` في تذاكر الدعم لتمكين الاعتراضات وتصحيح المصادر.
- ربط صفحة السياسات مع إعدادات المحتوى الشرعي في `SettingsScreen`.


### Added
- تصميم وبناء واجهات مركز الدعم الفني، المقالات، وتذاكر المساعدة (`HelpCenterScreen`, `HelpArticleDetailScreen`, `CreateTicketScreen`, `TicketListScreen`, `TicketDetailScreen`, `ServiceStatusScreen`).
- دمج مركز الدعم الفني وتوجيه المستخدمين ضمن شاشة إعدادات التطبيق `SettingsScreen` وربط جميع مسارات التنقل الجديدة في `AppNavigation`.
- حل مشكلة توافق `combine` في `SupportViewModel` بدمج المجموعات بدلاً من استخدام دوال زائدة.

### Added
- إصدار وثيقة وقرار نطاق الإصدار الأول `MVP_SCOPE.md` واعتماد قرار الجاهزية **GO**.
- وضع تصنيف شامل لكافة الميزات وتحديد النطاق الأساسي وتأجيل الميزات غير المستقرة (Text-to-Video, Live Audio).
- إنشاء قائمة التحقق للنشر `RELEASE_CHECKLIST.md` لضبط إجراءات البناء وإعدادات المتاجر والخوادم.
- إنشاء وثيقة الحدود والقيود المعروفة `KNOWN_LIMITATIONS.md`.
- تحديد خطة الإطلاق التجريبي (Internal Alpha -> Closed Beta -> Staged Production Rollout) ومعايير إيقاف الإصدار التلقائية.

## [Unreleased] - Security Audit & Hardening (PROMPT 069)

### Added
- نظام متكامل لإدارة الحقوق والتراخيص للأصول (Rights Management) مع سجل قرارات.
- تحديث `Asset` model لدعم معلومات الترخيص، وحالات الحقوق (Unknown, Commercial, Expired...).
- سياسة إدارة الحقوق `RIGHTS_POLICY.md` لضمان توافق جميع المحتويات.


### Added
- إعداد وثائق الإصدار النهائي (Production Readiness):
  - `RELEASE_NOTES.md`: ملاحظات الإصدار 1.0.0.
  - `PRODUCTION_CHECKLIST.md`: قائمة المراجعة للإنتاج (الكود، البنية التحتية، المتاجر، السياسات).
  - `LAUNCH_DAY_RUNBOOK.md`: خطة مراقبة يوم الإطلاق (أول 72 ساعة).
  - `ROLLBACK_PLAN.md`: خطة التراجع على مستوى المتجر والـ Backend.
  - `KNOWN_LIMITATIONS.md`: القيود الفنية في الإصدار الأول.
  - `SUPPORT_ESCALATION.md`: خطة تصعيد الدعم الفني.


### Added
- صياغة خطة الإطلاق التدريجي (Phased Rollout Plan) في ملف `ROLLOUT_PLAN.md` لضمان إطلاق آمن ومستقر.
- تحديد مقاييس المراقبة الأساسية (Crashes, ANR, AI Costs, Billing Drops) وشروط الإيقاف (Kill-switches) لكل مرحلة.


### Added
- صياغة سياسة المحتوى والاستخدام `CONTENT_POLICY.md` متضمنة مصفوفة العقوبات وقواعد المراجعين وقواعد التعامل مع الذكاء الاصطناعي.
- صياغة سياسة الاعتراضات والتصحيح `APPEALS_POLICY.md`.
- بناء واجهة المستخدم `ContentPolicyScreen` لعرض ملخص السياسات داخل التطبيق.
- إضافة فئات الدعم الفني الجديدة `APPEAL_AND_POLICY` و `SOURCE_CORRECTION` في تذاكر الدعم لتمكين الاعتراضات وتصحيح المصادر.
- ربط صفحة السياسات مع إعدادات المحتوى الشرعي في `SettingsScreen`.


### Added
- تصميم وبناء واجهات مركز الدعم الفني، المقالات، وتذاكر المساعدة (`HelpCenterScreen`, `HelpArticleDetailScreen`, `CreateTicketScreen`, `TicketListScreen`, `TicketDetailScreen`, `ServiceStatusScreen`).
- دمج مركز الدعم الفني وتوجيه المستخدمين ضمن شاشة إعدادات التطبيق `SettingsScreen` وربط جميع مسارات التنقل الجديدة في `AppNavigation`.
- حل مشكلة توافق `combine` في `SupportViewModel` بدمج المجموعات بدلاً من استخدام دوال زائدة.

### Added
- إنشاء تقرير المراجعة الأمنية الشامل `SECURITY_REVIEW.md`.
- تفعيل `Firebase App Check` باستخدام `PlayIntegrity` في تطبيق الأندرويد لزيادة الأمان.

### Fixed
- تصحيح ثغرة في `firestore.rules` تمنع إضافة أعضاء لمساحة العمل بطريقة غير مصرحة.
- تصحيح ثغرة في `storage.rules` لمنع الرفع العشوائي للملفات في مساحات العمل بدون استخدام واجهات الخادم الموثوقة.

## [Unreleased] - Pre-release Testing Plan (PROMPT 068)

### Added
- نظام متكامل لإدارة الحقوق والتراخيص للأصول (Rights Management) مع سجل قرارات.
- تحديث `Asset` model لدعم معلومات الترخيص، وحالات الحقوق (Unknown, Commercial, Expired...).
- سياسة إدارة الحقوق `RIGHTS_POLICY.md` لضمان توافق جميع المحتويات.


### Added
- إعداد وثائق الإصدار النهائي (Production Readiness):
  - `RELEASE_NOTES.md`: ملاحظات الإصدار 1.0.0.
  - `PRODUCTION_CHECKLIST.md`: قائمة المراجعة للإنتاج (الكود، البنية التحتية، المتاجر، السياسات).
  - `LAUNCH_DAY_RUNBOOK.md`: خطة مراقبة يوم الإطلاق (أول 72 ساعة).
  - `ROLLBACK_PLAN.md`: خطة التراجع على مستوى المتجر والـ Backend.
  - `KNOWN_LIMITATIONS.md`: القيود الفنية في الإصدار الأول.
  - `SUPPORT_ESCALATION.md`: خطة تصعيد الدعم الفني.


### Added
- صياغة خطة الإطلاق التدريجي (Phased Rollout Plan) في ملف `ROLLOUT_PLAN.md` لضمان إطلاق آمن ومستقر.
- تحديد مقاييس المراقبة الأساسية (Crashes, ANR, AI Costs, Billing Drops) وشروط الإيقاف (Kill-switches) لكل مرحلة.


### Added
- صياغة سياسة المحتوى والاستخدام `CONTENT_POLICY.md` متضمنة مصفوفة العقوبات وقواعد المراجعين وقواعد التعامل مع الذكاء الاصطناعي.
- صياغة سياسة الاعتراضات والتصحيح `APPEALS_POLICY.md`.
- بناء واجهة المستخدم `ContentPolicyScreen` لعرض ملخص السياسات داخل التطبيق.
- إضافة فئات الدعم الفني الجديدة `APPEAL_AND_POLICY` و `SOURCE_CORRECTION` في تذاكر الدعم لتمكين الاعتراضات وتصحيح المصادر.
- ربط صفحة السياسات مع إعدادات المحتوى الشرعي في `SettingsScreen`.


### Added
- تصميم وبناء واجهات مركز الدعم الفني، المقالات، وتذاكر المساعدة (`HelpCenterScreen`, `HelpArticleDetailScreen`, `CreateTicketScreen`, `TicketListScreen`, `TicketDetailScreen`, `ServiceStatusScreen`).
- دمج مركز الدعم الفني وتوجيه المستخدمين ضمن شاشة إعدادات التطبيق `SettingsScreen` وربط جميع مسارات التنقل الجديدة في `AppNavigation`.
- حل مشكلة توافق `combine` في `SupportViewModel` بدمج المجموعات بدلاً من استخدام دوال زائدة.

### Added
- إنشاء وثيقة `TEST_PLAN.md` الشاملة لاختبارات القبول (UAT).
- تحديد مسارات الاختبار الأساسية (Authentication, Studio, Review, Mihrab, Flash).
- تحديد سيناريوهات اختبار الشروط الاستثنائية والوصول الشامل (Offline, Weak Network, Screen Reader, Scaled Fonts, Deep Links).
- وضع هيكلية لتسجيل العيوب والأخطاء وتصنيفها لمنع تسرب الأعطال الحرجة (Blockers/Criticals) إلى بيئة الإنتاج.
- تحديد سيناريوهات اختبار الشروط الاستثنائية والوصول الشامل (Offline, Weak Network, Screen Reader, Scaled Fonts, Deep Links).
- وضع هيكلية لتسجيل العيوب والأخطاء وتصنيفها لمنع تسرب الأعطال الحرجة (Blockers/Criticals) إلى بيئة الإنتاج.

## [Unreleased] - Build Environments Configuration (PROMPT 067)

### Added
- نظام متكامل لإدارة الحقوق والتراخيص للأصول (Rights Management) مع سجل قرارات.
- تحديث `Asset` model لدعم معلومات الترخيص، وحالات الحقوق (Unknown, Commercial, Expired...).
- سياسة إدارة الحقوق `RIGHTS_POLICY.md` لضمان توافق جميع المحتويات.


### Added
- إعداد وثائق الإصدار النهائي (Production Readiness):
  - `RELEASE_NOTES.md`: ملاحظات الإصدار 1.0.0.
  - `PRODUCTION_CHECKLIST.md`: قائمة المراجعة للإنتاج (الكود، البنية التحتية، المتاجر، السياسات).
  - `LAUNCH_DAY_RUNBOOK.md`: خطة مراقبة يوم الإطلاق (أول 72 ساعة).
  - `ROLLBACK_PLAN.md`: خطة التراجع على مستوى المتجر والـ Backend.
  - `KNOWN_LIMITATIONS.md`: القيود الفنية في الإصدار الأول.
  - `SUPPORT_ESCALATION.md`: خطة تصعيد الدعم الفني.


### Added
- صياغة خطة الإطلاق التدريجي (Phased Rollout Plan) في ملف `ROLLOUT_PLAN.md` لضمان إطلاق آمن ومستقر.
- تحديد مقاييس المراقبة الأساسية (Crashes, ANR, AI Costs, Billing Drops) وشروط الإيقاف (Kill-switches) لكل مرحلة.


### Added
- صياغة سياسة المحتوى والاستخدام `CONTENT_POLICY.md` متضمنة مصفوفة العقوبات وقواعد المراجعين وقواعد التعامل مع الذكاء الاصطناعي.
- صياغة سياسة الاعتراضات والتصحيح `APPEALS_POLICY.md`.
- بناء واجهة المستخدم `ContentPolicyScreen` لعرض ملخص السياسات داخل التطبيق.
- إضافة فئات الدعم الفني الجديدة `APPEAL_AND_POLICY` و `SOURCE_CORRECTION` في تذاكر الدعم لتمكين الاعتراضات وتصحيح المصادر.
- ربط صفحة السياسات مع إعدادات المحتوى الشرعي في `SettingsScreen`.


### Added
- تصميم وبناء واجهات مركز الدعم الفني، المقالات، وتذاكر المساعدة (`HelpCenterScreen`, `HelpArticleDetailScreen`, `CreateTicketScreen`, `TicketListScreen`, `TicketDetailScreen`, `ServiceStatusScreen`).
- دمج مركز الدعم الفني وتوجيه المستخدمين ضمن شاشة إعدادات التطبيق `SettingsScreen` وربط جميع مسارات التنقل الجديدة في `AppNavigation`.
- حل مشكلة توافق `combine` في `SupportViewModel` بدمج المجموعات بدلاً من استخدام دوال زائدة.

### Added
- تكوين `productFlavors` لبيئات `dev`, `staging`, و `prod` في `build.gradle.kts`.
- تخصيص لواحق معرّف الحزمة (`applicationIdSuffix`) واسم التطبيق (`app_name`) لكل بيئة لتسهيل التمييز والفصل.
- تهيئة البنية التحتية لملفات `google-services.json` منفصلة لكل بيئة لتفادي تداخل مشاريع Firebase.
- دمج `BuildConfig` لتوفير متغير `ENVIRONMENT` يتم استخدامه لتوجيه الطلبات والتحكم باللوجز محلياً.
- ضمان عدم حفظ أي أسرار، أو ملفات التوقيع (Keystores)، أو مفاتيح إنتاج حقيقية داخل المستودع.

## [Unreleased] - Store Listing Preparation (PROMPT 066)

### Added
- نظام متكامل لإدارة الحقوق والتراخيص للأصول (Rights Management) مع سجل قرارات.
- تحديث `Asset` model لدعم معلومات الترخيص، وحالات الحقوق (Unknown, Commercial, Expired...).
- سياسة إدارة الحقوق `RIGHTS_POLICY.md` لضمان توافق جميع المحتويات.


### Added
- إعداد وثائق الإصدار النهائي (Production Readiness):
  - `RELEASE_NOTES.md`: ملاحظات الإصدار 1.0.0.
  - `PRODUCTION_CHECKLIST.md`: قائمة المراجعة للإنتاج (الكود، البنية التحتية، المتاجر، السياسات).
  - `LAUNCH_DAY_RUNBOOK.md`: خطة مراقبة يوم الإطلاق (أول 72 ساعة).
  - `ROLLBACK_PLAN.md`: خطة التراجع على مستوى المتجر والـ Backend.
  - `KNOWN_LIMITATIONS.md`: القيود الفنية في الإصدار الأول.
  - `SUPPORT_ESCALATION.md`: خطة تصعيد الدعم الفني.


### Added
- صياغة خطة الإطلاق التدريجي (Phased Rollout Plan) في ملف `ROLLOUT_PLAN.md` لضمان إطلاق آمن ومستقر.
- تحديد مقاييس المراقبة الأساسية (Crashes, ANR, AI Costs, Billing Drops) وشروط الإيقاف (Kill-switches) لكل مرحلة.


### Added
- صياغة سياسة المحتوى والاستخدام `CONTENT_POLICY.md` متضمنة مصفوفة العقوبات وقواعد المراجعين وقواعد التعامل مع الذكاء الاصطناعي.
- صياغة سياسة الاعتراضات والتصحيح `APPEALS_POLICY.md`.
- بناء واجهة المستخدم `ContentPolicyScreen` لعرض ملخص السياسات داخل التطبيق.
- إضافة فئات الدعم الفني الجديدة `APPEAL_AND_POLICY` و `SOURCE_CORRECTION` في تذاكر الدعم لتمكين الاعتراضات وتصحيح المصادر.
- ربط صفحة السياسات مع إعدادات المحتوى الشرعي في `SettingsScreen`.


### Added
- تصميم وبناء واجهات مركز الدعم الفني، المقالات، وتذاكر المساعدة (`HelpCenterScreen`, `HelpArticleDetailScreen`, `CreateTicketScreen`, `TicketListScreen`, `TicketDetailScreen`, `ServiceStatusScreen`).
- دمج مركز الدعم الفني وتوجيه المستخدمين ضمن شاشة إعدادات التطبيق `SettingsScreen` وربط جميع مسارات التنقل الجديدة في `AppNavigation`.
- حل مشكلة توافق `combine` في `SupportViewModel` بدمج المجموعات بدلاً من استخدام دوال زائدة.

### Added
- إنشاء وثيقة `STORE_LISTING.md` تحتوي على وصف التطبيق الكامل والقصير للمتاجر باللغتين العربية والإنجليزية.
- إعداد نصوص ومقترحات لقطات الشاشة والفيديو الترويجي بطريقة تعكس وظائف التطبيق الفعلية.
- صياغة سياسات المحتوى الشرعي، مبادئ الإبلاغ عن المحتوى، والتأكيد بوضوح على أن الذكاء الاصطناعي لا يمثل جهة إفتاء.
- تجهيز فقرات ملاحظات المراجعين (Reviewer Notes) وتوضيح كيفية عمل التطبيق والإشعارات والصلاحيات لتسهيل مراجعته من قبل Google Play و App Store.

## [Unreleased] - Localization & Internationalization (PROMPT 065)

### Added
- نظام متكامل لإدارة الحقوق والتراخيص للأصول (Rights Management) مع سجل قرارات.
- تحديث `Asset` model لدعم معلومات الترخيص، وحالات الحقوق (Unknown, Commercial, Expired...).
- سياسة إدارة الحقوق `RIGHTS_POLICY.md` لضمان توافق جميع المحتويات.


### Added
- إعداد وثائق الإصدار النهائي (Production Readiness):
  - `RELEASE_NOTES.md`: ملاحظات الإصدار 1.0.0.
  - `PRODUCTION_CHECKLIST.md`: قائمة المراجعة للإنتاج (الكود، البنية التحتية، المتاجر، السياسات).
  - `LAUNCH_DAY_RUNBOOK.md`: خطة مراقبة يوم الإطلاق (أول 72 ساعة).
  - `ROLLBACK_PLAN.md`: خطة التراجع على مستوى المتجر والـ Backend.
  - `KNOWN_LIMITATIONS.md`: القيود الفنية في الإصدار الأول.
  - `SUPPORT_ESCALATION.md`: خطة تصعيد الدعم الفني.


### Added
- صياغة خطة الإطلاق التدريجي (Phased Rollout Plan) في ملف `ROLLOUT_PLAN.md` لضمان إطلاق آمن ومستقر.
- تحديد مقاييس المراقبة الأساسية (Crashes, ANR, AI Costs, Billing Drops) وشروط الإيقاف (Kill-switches) لكل مرحلة.


### Added
- صياغة سياسة المحتوى والاستخدام `CONTENT_POLICY.md` متضمنة مصفوفة العقوبات وقواعد المراجعين وقواعد التعامل مع الذكاء الاصطناعي.
- صياغة سياسة الاعتراضات والتصحيح `APPEALS_POLICY.md`.
- بناء واجهة المستخدم `ContentPolicyScreen` لعرض ملخص السياسات داخل التطبيق.
- إضافة فئات الدعم الفني الجديدة `APPEAL_AND_POLICY` و `SOURCE_CORRECTION` في تذاكر الدعم لتمكين الاعتراضات وتصحيح المصادر.
- ربط صفحة السياسات مع إعدادات المحتوى الشرعي في `SettingsScreen`.


### Added
- تصميم وبناء واجهات مركز الدعم الفني، المقالات، وتذاكر المساعدة (`HelpCenterScreen`, `HelpArticleDetailScreen`, `CreateTicketScreen`, `TicketListScreen`, `TicketDetailScreen`, `ServiceStatusScreen`).
- دمج مركز الدعم الفني وتوجيه المستخدمين ضمن شاشة إعدادات التطبيق `SettingsScreen` وربط جميع مسارات التنقل الجديدة في `AppNavigation`.
- حل مشكلة توافق `combine` في `SupportViewModel` بدمج المجموعات بدلاً من استخدام دوال زائدة.

### Added
- تم تفعيل التدويل الأساسي وإنشاء ملفات `strings.xml` للغتين العربية (افتراضية) والإنجليزية كبنية أولية.
- استبدال النصوص الصلبة المتكررة بكثرة (أزرار وإجراءات عامة) بنصوص من `strings.xml` للبدء بخطة التدويل وتفادي كسر الـ RTL.
- تمكين تغيير اللغة ديناميكياً من شاشة الإعدادات باستخدام `AppCompatDelegate` و `LocaleManager`.
- إنشاء `LocalizationUtils` لتنسيق التواريخ، والأرقام، والعملات حسب لغة النظام بشكل آمن ودون اللجوء للترجمة الصلبة.
- تعديل `MainActivity` لوراثة `AppCompatActivity` بدلاً من `ComponentActivity` لدعم الـ LocaleManager وتحديث `LayoutDirection` ديناميكياً لضمان سلامة تخطيط الـ RTL و LTR.
- إضافة إعداد `android:localeConfig` في `AndroidManifest.xml` لدعم Android 13+.

## [Unreleased] - Universal Accessibility (PROMPT 063)

### Added
- نظام متكامل لإدارة الحقوق والتراخيص للأصول (Rights Management) مع سجل قرارات.
- تحديث `Asset` model لدعم معلومات الترخيص، وحالات الحقوق (Unknown, Commercial, Expired...).
- سياسة إدارة الحقوق `RIGHTS_POLICY.md` لضمان توافق جميع المحتويات.


### Added
- إعداد وثائق الإصدار النهائي (Production Readiness):
  - `RELEASE_NOTES.md`: ملاحظات الإصدار 1.0.0.
  - `PRODUCTION_CHECKLIST.md`: قائمة المراجعة للإنتاج (الكود، البنية التحتية، المتاجر، السياسات).
  - `LAUNCH_DAY_RUNBOOK.md`: خطة مراقبة يوم الإطلاق (أول 72 ساعة).
  - `ROLLBACK_PLAN.md`: خطة التراجع على مستوى المتجر والـ Backend.
  - `KNOWN_LIMITATIONS.md`: القيود الفنية في الإصدار الأول.
  - `SUPPORT_ESCALATION.md`: خطة تصعيد الدعم الفني.


### Added
- صياغة خطة الإطلاق التدريجي (Phased Rollout Plan) في ملف `ROLLOUT_PLAN.md` لضمان إطلاق آمن ومستقر.
- تحديد مقاييس المراقبة الأساسية (Crashes, ANR, AI Costs, Billing Drops) وشروط الإيقاف (Kill-switches) لكل مرحلة.


### Added
- صياغة سياسة المحتوى والاستخدام `CONTENT_POLICY.md` متضمنة مصفوفة العقوبات وقواعد المراجعين وقواعد التعامل مع الذكاء الاصطناعي.
- صياغة سياسة الاعتراضات والتصحيح `APPEALS_POLICY.md`.
- بناء واجهة المستخدم `ContentPolicyScreen` لعرض ملخص السياسات داخل التطبيق.
- إضافة فئات الدعم الفني الجديدة `APPEAL_AND_POLICY` و `SOURCE_CORRECTION` في تذاكر الدعم لتمكين الاعتراضات وتصحيح المصادر.
- ربط صفحة السياسات مع إعدادات المحتوى الشرعي في `SettingsScreen`.


### Added
- تصميم وبناء واجهات مركز الدعم الفني، المقالات، وتذاكر المساعدة (`HelpCenterScreen`, `HelpArticleDetailScreen`, `CreateTicketScreen`, `TicketListScreen`, `TicketDetailScreen`, `ServiceStatusScreen`).
- دمج مركز الدعم الفني وتوجيه المستخدمين ضمن شاشة إعدادات التطبيق `SettingsScreen` وربط جميع مسارات التنقل الجديدة في `AppNavigation`.
- حل مشكلة توافق `combine` في `SupportViewModel` بدمج المجموعات بدلاً من استخدام دوال زائدة.

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
- نظام متكامل لإدارة الحقوق والتراخيص للأصول (Rights Management) مع سجل قرارات.
- تحديث `Asset` model لدعم معلومات الترخيص، وحالات الحقوق (Unknown, Commercial, Expired...).
- سياسة إدارة الحقوق `RIGHTS_POLICY.md` لضمان توافق جميع المحتويات.


### Added
- إعداد وثائق الإصدار النهائي (Production Readiness):
  - `RELEASE_NOTES.md`: ملاحظات الإصدار 1.0.0.
  - `PRODUCTION_CHECKLIST.md`: قائمة المراجعة للإنتاج (الكود، البنية التحتية، المتاجر، السياسات).
  - `LAUNCH_DAY_RUNBOOK.md`: خطة مراقبة يوم الإطلاق (أول 72 ساعة).
  - `ROLLBACK_PLAN.md`: خطة التراجع على مستوى المتجر والـ Backend.
  - `KNOWN_LIMITATIONS.md`: القيود الفنية في الإصدار الأول.
  - `SUPPORT_ESCALATION.md`: خطة تصعيد الدعم الفني.


### Added
- صياغة خطة الإطلاق التدريجي (Phased Rollout Plan) في ملف `ROLLOUT_PLAN.md` لضمان إطلاق آمن ومستقر.
- تحديد مقاييس المراقبة الأساسية (Crashes, ANR, AI Costs, Billing Drops) وشروط الإيقاف (Kill-switches) لكل مرحلة.


### Added
- صياغة سياسة المحتوى والاستخدام `CONTENT_POLICY.md` متضمنة مصفوفة العقوبات وقواعد المراجعين وقواعد التعامل مع الذكاء الاصطناعي.
- صياغة سياسة الاعتراضات والتصحيح `APPEALS_POLICY.md`.
- بناء واجهة المستخدم `ContentPolicyScreen` لعرض ملخص السياسات داخل التطبيق.
- إضافة فئات الدعم الفني الجديدة `APPEAL_AND_POLICY` و `SOURCE_CORRECTION` في تذاكر الدعم لتمكين الاعتراضات وتصحيح المصادر.
- ربط صفحة السياسات مع إعدادات المحتوى الشرعي في `SettingsScreen`.


### Added
- تصميم وبناء واجهات مركز الدعم الفني، المقالات، وتذاكر المساعدة (`HelpCenterScreen`, `HelpArticleDetailScreen`, `CreateTicketScreen`, `TicketListScreen`, `TicketDetailScreen`, `ServiceStatusScreen`).
- دمج مركز الدعم الفني وتوجيه المستخدمين ضمن شاشة إعدادات التطبيق `SettingsScreen` وربط جميع مسارات التنقل الجديدة في `AppNavigation`.
- حل مشكلة توافق `combine` في `SupportViewModel` بدمج المجموعات بدلاً من استخدام دوال زائدة.

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
- نظام متكامل لإدارة الحقوق والتراخيص للأصول (Rights Management) مع سجل قرارات.
- تحديث `Asset` model لدعم معلومات الترخيص، وحالات الحقوق (Unknown, Commercial, Expired...).
- سياسة إدارة الحقوق `RIGHTS_POLICY.md` لضمان توافق جميع المحتويات.


### Added
- إعداد وثائق الإصدار النهائي (Production Readiness):
  - `RELEASE_NOTES.md`: ملاحظات الإصدار 1.0.0.
  - `PRODUCTION_CHECKLIST.md`: قائمة المراجعة للإنتاج (الكود، البنية التحتية، المتاجر، السياسات).
  - `LAUNCH_DAY_RUNBOOK.md`: خطة مراقبة يوم الإطلاق (أول 72 ساعة).
  - `ROLLBACK_PLAN.md`: خطة التراجع على مستوى المتجر والـ Backend.
  - `KNOWN_LIMITATIONS.md`: القيود الفنية في الإصدار الأول.
  - `SUPPORT_ESCALATION.md`: خطة تصعيد الدعم الفني.


### Added
- صياغة خطة الإطلاق التدريجي (Phased Rollout Plan) في ملف `ROLLOUT_PLAN.md` لضمان إطلاق آمن ومستقر.
- تحديد مقاييس المراقبة الأساسية (Crashes, ANR, AI Costs, Billing Drops) وشروط الإيقاف (Kill-switches) لكل مرحلة.


### Added
- صياغة سياسة المحتوى والاستخدام `CONTENT_POLICY.md` متضمنة مصفوفة العقوبات وقواعد المراجعين وقواعد التعامل مع الذكاء الاصطناعي.
- صياغة سياسة الاعتراضات والتصحيح `APPEALS_POLICY.md`.
- بناء واجهة المستخدم `ContentPolicyScreen` لعرض ملخص السياسات داخل التطبيق.
- إضافة فئات الدعم الفني الجديدة `APPEAL_AND_POLICY` و `SOURCE_CORRECTION` في تذاكر الدعم لتمكين الاعتراضات وتصحيح المصادر.
- ربط صفحة السياسات مع إعدادات المحتوى الشرعي في `SettingsScreen`.


### Added
- تصميم وبناء واجهات مركز الدعم الفني، المقالات، وتذاكر المساعدة (`HelpCenterScreen`, `HelpArticleDetailScreen`, `CreateTicketScreen`, `TicketListScreen`, `TicketDetailScreen`, `ServiceStatusScreen`).
- دمج مركز الدعم الفني وتوجيه المستخدمين ضمن شاشة إعدادات التطبيق `SettingsScreen` وربط جميع مسارات التنقل الجديدة في `AppNavigation`.
- حل مشكلة توافق `combine` في `SupportViewModel` بدمج المجموعات بدلاً من استخدام دوال زائدة.

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
- نظام متكامل لإدارة الحقوق والتراخيص للأصول (Rights Management) مع سجل قرارات.
- تحديث `Asset` model لدعم معلومات الترخيص، وحالات الحقوق (Unknown, Commercial, Expired...).
- سياسة إدارة الحقوق `RIGHTS_POLICY.md` لضمان توافق جميع المحتويات.


### Added
- إعداد وثائق الإصدار النهائي (Production Readiness):
  - `RELEASE_NOTES.md`: ملاحظات الإصدار 1.0.0.
  - `PRODUCTION_CHECKLIST.md`: قائمة المراجعة للإنتاج (الكود، البنية التحتية، المتاجر، السياسات).
  - `LAUNCH_DAY_RUNBOOK.md`: خطة مراقبة يوم الإطلاق (أول 72 ساعة).
  - `ROLLBACK_PLAN.md`: خطة التراجع على مستوى المتجر والـ Backend.
  - `KNOWN_LIMITATIONS.md`: القيود الفنية في الإصدار الأول.
  - `SUPPORT_ESCALATION.md`: خطة تصعيد الدعم الفني.


### Added
- صياغة خطة الإطلاق التدريجي (Phased Rollout Plan) في ملف `ROLLOUT_PLAN.md` لضمان إطلاق آمن ومستقر.
- تحديد مقاييس المراقبة الأساسية (Crashes, ANR, AI Costs, Billing Drops) وشروط الإيقاف (Kill-switches) لكل مرحلة.


### Added
- صياغة سياسة المحتوى والاستخدام `CONTENT_POLICY.md` متضمنة مصفوفة العقوبات وقواعد المراجعين وقواعد التعامل مع الذكاء الاصطناعي.
- صياغة سياسة الاعتراضات والتصحيح `APPEALS_POLICY.md`.
- بناء واجهة المستخدم `ContentPolicyScreen` لعرض ملخص السياسات داخل التطبيق.
- إضافة فئات الدعم الفني الجديدة `APPEAL_AND_POLICY` و `SOURCE_CORRECTION` في تذاكر الدعم لتمكين الاعتراضات وتصحيح المصادر.
- ربط صفحة السياسات مع إعدادات المحتوى الشرعي في `SettingsScreen`.


### Added
- تصميم وبناء واجهات مركز الدعم الفني، المقالات، وتذاكر المساعدة (`HelpCenterScreen`, `HelpArticleDetailScreen`, `CreateTicketScreen`, `TicketListScreen`, `TicketDetailScreen`, `ServiceStatusScreen`).
- دمج مركز الدعم الفني وتوجيه المستخدمين ضمن شاشة إعدادات التطبيق `SettingsScreen` وربط جميع مسارات التنقل الجديدة في `AppNavigation`.
- حل مشكلة توافق `combine` في `SupportViewModel` بدمج المجموعات بدلاً من استخدام دوال زائدة.

### Added
- Unified `AppError` sealed class for consistent error mapping (Network, Auth, Database, Storage, Payment, etc.).
- `ErrorHandler` utility to sanitize technical messages and prevent exposure of sensitive data (tokens, keys) to the UI.
- Secure logging mechanism via `SirajLogger` to trace errors using uniquely generated reference IDs without exposing user-identifiable patterns.
- Upgraded `Resource` wrapper class to optionally carry an `AppError` payload.
- New `@Composable AppErrorScreen` component allowing for fallback UI and structured retry actions based on error categories.

### Changed
- Standardized error-catching blocks across data layer repositories (`FirebaseAuthRepositoryImpl`, `FirebaseProjectRepositoryImpl`, etc.) to process exceptions through `ErrorHandler.handle(e)`.
