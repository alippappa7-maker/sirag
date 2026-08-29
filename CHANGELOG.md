# سجل التغييرات (Changelog)

## [Unreleased] - Incident Response Plan & Emergency Action Hub (PROMPT 076)
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
- إنشاء نماذج إدارة وتوزيع المختبرين `TesterDistributionModels.kt` التي تغطي مجموعات المختبرين (`TesterGroup`), حالات النشاط (`TesterStatus`), المسارات الحرجة (`CriticalJourney`), واستبيانات التجربة (`TesterExperienceSurvey`).
- تنفيذ مستودع توزيع المختبرين `BetaTesterDistributionRepository` عبر `FirebaseBetaTesterDistributionRepositoryImpl` لتسجيل الجلسات، حفظ التقدم في المسارات، وتجميع الاستبيانات.
- بناء شاشة مركز المختبرين `TesterHubScreen` مع دعم تتبع المسارات الأساسية، أدلة تثبيت وتحديث التطبيق لأجهزة Android و iOS، نموذج استبيان الرضا الشامل، وملاحظات الإصدار.
- ربط المسار `Screen.TesterHub` في نظام التنقل الرئيسي `AppNavigation` وتوفير الوصول إليه عبر شريط البيتا `BetaBadgeBanner` وفي شاشة الإعدادات `SettingsScreen`.
- تحديث `firestore.rules` لتأمين مجموعات `/beta_testers` و `/beta_experience_surveys` وتطبيق مبدأ أقل صلاحية.
- كتابة وثيقة سياسات وإجراءات التوزيع `BETA_DISTRIBUTION.md`.
- كتابة وتمرير الاختبارات الوحدوية `TesterDistributionUnitTest`.

## [Unreleased] - Beta Version Setup & Feedback Mechanism (PROMPT 071)
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
- إصدار وثيقة وقرار نطاق الإصدار الأول `MVP_SCOPE.md` واعتماد قرار الجاهزية **GO**.
- وضع تصنيف شامل لكافة الميزات وتحديد النطاق الأساسي وتأجيل الميزات غير المستقرة (Text-to-Video, Live Audio).
- إنشاء قائمة التحقق للنشر `RELEASE_CHECKLIST.md` لضبط إجراءات البناء وإعدادات المتاجر والخوادم.
- إنشاء وثيقة الحدود والقيود المعروفة `KNOWN_LIMITATIONS.md`.
- تحديد خطة الإطلاق التجريبي (Internal Alpha -> Closed Beta -> Staged Production Rollout) ومعايير إيقاف الإصدار التلقائية.

## [Unreleased] - Security Audit & Hardening (PROMPT 069)
### Added
- إنشاء تقرير المراجعة الأمنية الشامل `SECURITY_REVIEW.md`.
- تفعيل `Firebase App Check` باستخدام `PlayIntegrity` في تطبيق الأندرويد لزيادة الأمان.

### Fixed
- تصحيح ثغرة في `firestore.rules` تمنع إضافة أعضاء لمساحة العمل بطريقة غير مصرحة.
- تصحيح ثغرة في `storage.rules` لمنع الرفع العشوائي للملفات في مساحات العمل بدون استخدام واجهات الخادم الموثوقة.

## [Unreleased] - Pre-release Testing Plan (PROMPT 068)
### Added
- إنشاء وثيقة `TEST_PLAN.md` الشاملة لاختبارات القبول (UAT).
- تحديد مسارات الاختبار الأساسية (Authentication, Studio, Review, Mihrab, Flash).
- تحديد سيناريوهات اختبار الشروط الاستثنائية والوصول الشامل (Offline, Weak Network, Screen Reader, Scaled Fonts, Deep Links).
- وضع هيكلية لتسجيل العيوب والأخطاء وتصنيفها لمنع تسرب الأعطال الحرجة (Blockers/Criticals) إلى بيئة الإنتاج.
- تحديد سيناريوهات اختبار الشروط الاستثنائية والوصول الشامل (Offline, Weak Network, Screen Reader, Scaled Fonts, Deep Links).
- وضع هيكلية لتسجيل العيوب والأخطاء وتصنيفها لمنع تسرب الأعطال الحرجة (Blockers/Criticals) إلى بيئة الإنتاج.

## [Unreleased] - Build Environments Configuration (PROMPT 067)
### Added
- تكوين `productFlavors` لبيئات `dev`, `staging`, و `prod` في `build.gradle.kts`.
- تخصيص لواحق معرّف الحزمة (`applicationIdSuffix`) واسم التطبيق (`app_name`) لكل بيئة لتسهيل التمييز والفصل.
- تهيئة البنية التحتية لملفات `google-services.json` منفصلة لكل بيئة لتفادي تداخل مشاريع Firebase.
- دمج `BuildConfig` لتوفير متغير `ENVIRONMENT` يتم استخدامه لتوجيه الطلبات والتحكم باللوجز محلياً.
- ضمان عدم حفظ أي أسرار، أو ملفات التوقيع (Keystores)، أو مفاتيح إنتاج حقيقية داخل المستودع.

## [Unreleased] - Store Listing Preparation (PROMPT 066)
### Added
- إنشاء وثيقة `STORE_LISTING.md` تحتوي على وصف التطبيق الكامل والقصير للمتاجر باللغتين العربية والإنجليزية.
- إعداد نصوص ومقترحات لقطات الشاشة والفيديو الترويجي بطريقة تعكس وظائف التطبيق الفعلية.
- صياغة سياسات المحتوى الشرعي، مبادئ الإبلاغ عن المحتوى، والتأكيد بوضوح على أن الذكاء الاصطناعي لا يمثل جهة إفتاء.
- تجهيز فقرات ملاحظات المراجعين (Reviewer Notes) وتوضيح كيفية عمل التطبيق والإشعارات والصلاحيات لتسهيل مراجعته من قبل Google Play و App Store.

## [Unreleased] - Localization & Internationalization (PROMPT 065)
### Added
- تم تفعيل التدويل الأساسي وإنشاء ملفات `strings.xml` للغتين العربية (افتراضية) والإنجليزية كبنية أولية.
- استبدال النصوص الصلبة المتكررة بكثرة (أزرار وإجراءات عامة) بنصوص من `strings.xml` للبدء بخطة التدويل وتفادي كسر الـ RTL.
- تمكين تغيير اللغة ديناميكياً من شاشة الإعدادات باستخدام `AppCompatDelegate` و `LocaleManager`.
- إنشاء `LocalizationUtils` لتنسيق التواريخ، والأرقام، والعملات حسب لغة النظام بشكل آمن ودون اللجوء للترجمة الصلبة.
- تعديل `MainActivity` لوراثة `AppCompatActivity` بدلاً من `ComponentActivity` لدعم الـ LocaleManager وتحديث `LayoutDirection` ديناميكياً لضمان سلامة تخطيط الـ RTL و LTR.
- إضافة إعداد `android:localeConfig` في `AndroidManifest.xml` لدعم Android 13+.

## [Unreleased] - Universal Accessibility (PROMPT 063)
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
