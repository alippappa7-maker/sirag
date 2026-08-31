# وثيقة التوثيق العدلي الشاملة لتطبيق سراج
## Siraj: The Master Notary Document & Technical Blueprint
**تاريخ التوثيق والتثبيت:** 31 أغسطس 2026  
**الإصدار المعتمد:** Version 1.1.8 (Techno-Spiritual Edition)  
**الهوية المعمارية:** Clean Architecture / Kotlin Jetpack Compose M3 / Local-First / Server-Side Gemini API  
**المرجعية الشرعية والتقنية:** ميثاق سراج للحوكمة والإنتاج الإسلامي الرصين  

---

## الفهرس العام للوثيقة

1. **ديباجة وميثاق التأسيس (Foundational Charter)**
2. **فلسفة الهوية الجمالية: سراج التقني الروحاني (Siraj Techno-Spiritual Design System)**
3. **الهيكلية المعمارية الكلية للنظام (System Architecture & Framework Foundations)**
4. **التوثيق التفصيلي للوحدات الوظيفية والشاشات (Every Feature & Screen - كل صغيرة وكبيرة)**
   - 4.1. وحدة المحراب والعبادات (Mihrab & Spiritual Engine)
   - 4.2. وحدة المصحف الشريف والمشغل الصوتي (Quran Reader & Audio Core)
   - 4.3. استوديو الإنتاج وصناعة الفيديو (Studio & Project Production Engine)
   - 4.4. وحدة ومضات سراج (Siraj Flashes & Social Engine)
   - 4.5. منظومة الحوكمة والتدقيق الشرعي وتصحيح المحتوى (Sharia Governance & Content Correction)
   - 4.6. منظومة حماية القاصرين والأمان المجتمعي (Minor Safety & Community Moderation)
   - 4.7. منظومة مساحات العمل والتعاون الجماعي (Workspaces & Collaboration)
   - 4.8. مركز الخصوصية والأمان وحذف البيانات (Privacy Center & Audit Ledger)
   - 4.9. منظومة إدارة التكاليف وحصص الذكاء الاصطناعي وإدارة الأسرار (Cost Guards & Secret Management)
   - 4.10. منظومة المراقبة والاستجابة للطوارئ والترقيات (Crash Monitoring & Incident Response)
   - 4.11. منظومة البحث الموحد والتصنيف الموضوعي (Unified Search & Content Taxonomy)
   - 4.12. منظومة الدعم الفني ومركز المختبرين (Support System & Beta Tester Hub)
5. **قاموس نماذج البيانات والكيانات البرمجية (Data Entities & Schemas Dictionary)**
6. **مصفوفة الأمان والأذونات وسياسات التشغيل (Security Matrix, Permissions & Runtime Policies)**
7. **إقرار التوثيق العدلي والإقفال الرسمي (Notary Attestation & Sealing)**

---

# 1. ديباجة وميثاق التأسيس (Foundational Charter)

تُعد هذه الوثيقة هي **السند التوثيقي العدلي الرسمي** لتطبيق **سراج (Siraj)**. كُتبت هذه الوثيقة لتكون المرجع النهائي غير القابل للبس لكل سطر برمجي، شاشة، خوارزمية، حالة، ونموذج بيانات داخل المنظومة.

### رسالة المنظومة:
تمكين صناع المحتوى والمسلمين حول العالم من إنتاج، مراجعة، ونشر محتوى إسلامي وتقني وإبداعي بأعلى درجات الدقة والجمال، مدعوماً بالذكاء الاصطناعي التوليدي المنضبط بالضوابط الشرعية الصارمة، مع توفير تجربة روحانية متكاملة تشمل القرآن الكريم، مواقيت الصلاة، الأذكار، والمحراب الرقمي.

---

# 2. فلسفة الهوية الجمالية: سراج التقني الروحاني (Siraj Techno-Spiritual Design System)

تعتمد المنظومة لغة تصميم فريدة تدمج بين **الوقار والسكينة الإسلامية** و**التقنية العالية المعاصرة**، مجسدة في المعايير التالية:

### 2.1. لوحة الألوان الرسمية (Strict Color Palette)
تلتزم الواجهات بدقة بنظام **Material 3 Semantic Roles** دون أي قيم لونية ثابتة مشوهة داخل الواجهات:

*   **الوضع الداكن (Dark Mode):**
    *   الخلفية العميقة (`background`): `#0A1113` (سواد مائل للزرقة الليلية الساكنة).
    *   الأسطح الرئيسية (`surface`): `#101A1B` (سطح تقني معتم).
    *   الأسطح الثانوية (`surfaceVariant`): `#172728` (عمق لوني فاصل).
    *   اللون الأساسي (`primary`): `#1A8068` (أخضر زمردي إسلامي وقور).
    *   حاوية الأساسي (`primaryContainer`): `#0D4038` (أخضر عميق للبطاقات الفعالة).
    *   اللون الثانوي المضيء (`secondary`): `#D2A84A` (ذهب عربي إشعاعي للإنجاز والتوهج).
    *   اللون التقني الثالث (`tertiary`): `#55D6C2` (سماوي رقمي يرمز للذكاء الاصطناعي والمعالجة).
    *   النصوص والرموز الأساسية (`onBackground` / `onSurface`): `#EAF4F0`.
    *   النصوص والرموز الثانوية (`onSurfaceVariant`): `#A4B8B2`.

*   **الوضع الفاتح (Light Mode):**
    *   الخلفية (`background`): `#F7F8F5` (بياض لؤلؤي هادئ ومريح للعين).
    *   الأسطح (`surface`): `#FFFFFF`.
    *   الأسطح الثانوية (`surfaceVariant`): `#E7EFEB`.
    *   اللون الأساسي (`primary`): `#155C4A` (أخضر نقي عميق).
    *   حاوية الأساسي (`primaryContainer`): `#CBE8DC`.
    *   اللون الثانوي (`secondary`): `#A87824` (ذهب نحاسي كلاسيكي).
    *   اللون التقني الثالث (`tertiary`): `#147A70` (فيروزي عميق).
    *   النصوص (`onSurface`): `#17211D`.

### 2.2. مكونات الهوية البصرية التأسيسية (`Core UI Components`)
1.  **`SirajTechCard`**: بطاقة برمجية مصممة خصيصاً للتطبيق، تستخدم زوايا مقطوعة بدقة 12dp/16dp مع حدود تقنية متناهية الدقة (0.75dp إلى 1dp)، وتتوهج بضوء خافت ذكي عند تفعيل حالتها (`isActive = true`).
2.  **`SirajGlowContainer`**: غلاف إشعاعي يحيط بالعناصر النشطة حالياً (مثل تشغيل قارئ القرآن أو معالجة الذكاء الاصطناعي) بتوهج ناعم يعكس نبض النظام.
3.  **`SirajSpacing`**: شبكة مقاسات قياسية قائمة على مضاعفات 4dp و 8dp، تضمن تباعداً فسيحاً ومريحاً وتمنع التكدس البصري.
4.  **`SirajTypography`**: دعم متكامل للخطوط العربية الأصيلة (Amiri للقرآن والشواهد، و Cairo/Noto Sans للواجهات والنصوص التقنية)، مع مراعاة كاملة لعلامات التشكيل ومسافات الأسطر.

---

# 3. الهيكلية المعمارية الكلية للنظام (System Architecture)

يعمل تطبيق سراج وفق معمارية النطاقات النقية المقسمة إلى 3 طبقات رئيسية (Layered Architecture):

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  (Jetpack Compose UI, ViewModels, States, Intents, M3)      │
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│                       Domain Layer                          │
│  (Pure Kotlin Models, Business Engines, UseCases, Repos)    │
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│                        Data Layer                           │
│  (Room Local DB, Firestore, Firebase Auth, Gemini, Media3)  │
└─────────────────────────────────────────────────────────────┘
```

### المبادئ الصارمة للتنفيذ:
1.  **عدم المساس بالبيانات (Immutability & Safety):** كافّة التدفقات معتمدة على `StateFlow` و `SharedFlow` غير القابلة للتعديل الخارجي.
2.  **العمل دون اتصال أولاً (Offline-First Local Database):** قاعدة بيانات Room المحلية تخزن المشاريع والمشاهد والتلاوات والأذكار لتعمل بكفاءة حتى عند انقطاع الإنترنت.
3.  **التعامل مع السيرفر والذكاء الاصطناعي (Server-Side Gemini Integration):** توليد النصوص، تحليل المحتوى، وتوليد المشاهد يتم عبر قنوات آمنة ومراقبة بتكلفة دقيقة.

---

# 4. التوثيق التفصيلي للوحدات الوظيفية والشاشات (Feature Deep Dive)

---

## 4.1. وحدة المحراب والعبادات (Mihrab & Spiritual Engine)
*   **الملفات الرئيسية:**
    *   `features/mihrab/presentation/MihrabScreen.kt`
    *   `features/mihrab/presentation/MihrabViewModel.kt`
    *   `features/mihrab/presentation/adhkar/AdhkarScreen.kt`
    *   `features/mihrab/presentation/prayer/PrayerTimesScreen.kt`
    *   `domain/models/prayer/PrayerModels.kt`
    *   `domain/models/adhkar/AdhkarModels.kt`
*   **الوظائف والشاشات:**
    1.  **حساب مواقيت الصلاة الدقيقة:** يدعم الحساب الفلكي الموثوق مع تحديد الموقع الجغرافي واتجاه القبلة، والتنبيه التنازلي للصلاة القادمة.
    2.  **شاشة الأذكار المتكاملة (Adhkar System):** أذكار الصباح والمساء، أذكار الصلاة، وأدعية مختارة، مزودة بعداد رقمي تفاعلي لكل ذكر مع اهتزاز لمسي (Haptic Feedback) عند اكتمال العدد.
    3.  **التقويم الهجري والمناسبات:** عرض التاريخ الهجري المعتمد والتذكير بالأيام البيض والمناسبات الشرعية.

---

## 4.2. وحدة المصحف الشريف والمشغل الصوتي (Quran & Audio Engine)
*   **الملفات الرئيسية:**
    *   `features/quran/presentation/QuranScreens.kt`
    *   `features/quran/presentation/QuranViewModels.kt`
    *   `domain/models/quran/QuranModels.kt`
    *   `data/repository/QuranRepositoryImpl.kt`
*   **الوظائف والشاشات:**
    1.  **فهرس السور والأجزاء:** تصفح السور الـ 114 مع بيانات مكان النزول (مكية / مدنية)، عدد الآيات، واسم السورة المترجم.
    2.  **قارئ الآيات التفاعلي (Ayah Reader):** عرض النص القرآني بالرسم العثماني المضبوط، مع إمكانية تكبير الخط، التبديل للوضع الليلي، ونسخ ومشاركة الآيات.
    3.  **المشغل الصوتي المتقدم (ExoPlayer Audio Core):**
        *   الاستماع للتلاوة آية بآية مع إبراز الآية المقروءة بتوهج `SirajTechCard (isActive = isPlaying)`.
        *   شريط تحكم سفلي عائم مغلف بـ `SirajGlowContainer` للتحكم في السرعة (1x, 1.25x, 1.5x)، التكرار، والإيقاف المؤقت.
    4.  **نظام الملاحظات والفواصل (Bookmarks & Notes):** حفظ الفواصل المرجعية وإضافة خواطر وتأملات على كل آية بشكل محلي ومحمي.

---

## 4.3. استوديو الإنتاج وصناعة الفيديو (Studio & Project Engine)
*   **الملفات الرئيسية:**
    *   `features/project/presentation/ProjectEditorScreen.kt`
    *   `features/project/presentation/scenes/ScenesScreen.kt`
    *   `features/project/presentation/scenes/SceneEditorScreen.kt`
    *   `features/project/presentation/plan/ContentPlanScreen.kt`
    *   `features/project/presentation/audio/AudioStudioScreen.kt`
    *   `features/project/presentation/subtitles/SubtitleEditorScreen.kt`
    *   `features/project/presentation/ai/AiImageGeneratorScreen.kt`
    *   `features/project/presentation/export/ProjectExportScreen.kt`
    *   `features/project/presentation/jobs/ProductionJobsScreen.kt`
    *   `features/rights/presentation/AssetRightsScreen.kt`
*   **الوظائف والشاشات:**
    1.  **مخطط المحتوى الذكي (Content Plan):** توليد وهيكلة الأفكار الإبداعية، المحاور، النصوص المقترحة، والكلمات المفتاحية بمساعدة Gemini.
    2.  **محرر المشاهد واللقطات (Scene Editor & Timeline):**
        *   ترتيب المشاهد البصرية مع تحديد المدة الزمنية لكل مشهد بالثواني.
        *   تخصيص الانتقالات (Transitions) وحركات الكاميرا (Pan & Zoom).
    3.  **استوديو الهندسة الصوتية (Audio Studio):** دمج التسجيل الصوتي البشري، التلاوات، والمؤثرات الصوتية الطبيعية الخالية من المحاذير مع التحكم في مستويات الصوت (Volume Ducking).
    4.  **محرر الترجمة والشروح النصية (Subtitle Editor):** مزامنة النصوص تلقائياً مع الصوت، وتخصيص خطوط الترجمة، الظلال، وألوان الكلمات النشطة (Karaoke Highlight).
    5.  **مولد المشاهد والصور بالذكاء الاصطناعي (AI Image Generator):** إنشاء خلفيات وصور رمزية متوافقة مع الضوابط الأخلاقية والشرعية بدقة عالية.
    6.  **فاحص حقوق الملكية والتراخيص (Asset Rights & Licensing):** التحقق من تراخيص كافة الصور والمقاطع والأصوات وتوثيق مصدرها لضمان النزاهة القانونية والشرعية.
    7.  **محرك التصدير وإدارة الوظائف (Export & Production Jobs):** تصدير الفيديو بجودات متعددة (1080p, 4K, Reels, Shorts, YouTube)، ومتابعة طابور المعالجة لحظياً.

---

## 4.4. وحدة ومضات سراج (Siraj Flashes Engine)
*   **الملفات الرئيسية:**
    *   `features/flashes/presentation/FlashesScreen.kt`
    *   `domain/models/flash/FlashModels.kt`
*   **الوظائف والشاشات:**
    1.  **التغذية البصرية القصيرة (Vertical Feed):** استعراض مقاطع ومضات قصيرة هادفة بملء الشاشة مع انتقال عمودي ناعم وتفاعل سريع.
    2.  **النشر والتفاعل:** إمكانية نشر ومضة سريعة (نصية، صوتية، أو مرئية) وحفظها في المفضلة ومشاركتها عبر المنصات.

---

## 4.5. منظومة الحوكمة والتدقيق الشرعي وتصحيح المحتوى (Sharia Governance Engine)
*   **الملفات الرئيسية:**
    *   `features/review/presentation/ShariaReviewQueueScreen.kt`
    *   `features/review/presentation/ShariaReviewDetailScreen.kt`
    *   `features/review/presentation/corrections/ContentCorrectionHistoryScreen.kt`
    *   `features/review/presentation/governance/ReviewerGovernanceDashboardScreen.kt`
    *   `features/review/domain/ContentCorrectionEngine.kt`
    *   `features/review/domain/ReviewerGovernanceEngine.kt`
*   **الوظائف والشاشات:**
    1.  **طابور المراجعة الشرعية (Review Queue):** استلام المشاريع والمحتوى المرشح للنشر، وفرزه حسب الأولوية ومستوى التدقيق المطلوب.
    2.  **شاشة فحص المزاعم والشواهد (Claim & Source Review):** فحص الأحاديث، الآيات، الفتاوى، والاقتباسات، وربطها بالمصادر الأصلية الموثوقة مع تقييم درجة التوثيق (صحيح، حسن، ضعيف، رأي فقهي معتبر).
    3.  **محرك وسجل تصحيح المحتوى (Content Correction & Versioning):** توثيق كل تعديل شرعي، مَن قام به، السبب الشرعي، والتاريخ مع إمكانية المقارنة المزدوجة (Diff Viewer).
    4.  **لوحة حوكمة المراجعين (Reviewer Governance Dashboard):** قياس أداء المراجعين، معدلات الإنجاز، ومستويات الاعتماد (معتمد، يحتاج مراجعة ثانية، مرفوض مع التوجيه).

---

## 4.6. منظومة حماية القاصرين والأمان المجتمعي (Minor Safety & Moderation)
*   **الملفات الرئيسية:**
    *   `features/minor/presentation/MinorSafetyScreen.kt`
    *   `domain/models/minor/MinorSafetyModels.kt`
    *   `domain/models/community/SafetyModels.kt`
*   **الوظائف والشاشات:**
    1.  **التصنيف العمري للمحتوى:** وسم المحتوى بالفئات العمرية المناسبة (الكل، يافعين، بالغين).
    2.  **الرقابة الأبوية (Parental Guard):** قفل المحتوى الحساس أو المتقدم برمز سري وتحديد ساعات الاستخدام.
    3.  **نظام الإبلاغ الفوري والتصفية الآلية:** فحص النصوص والصور لمنع أي محتوى خادش، مضلل، أو غير لائق مجتمعياً وشرعياً.

---

## 4.7. منظومة مساحات العمل والتعاون الجماعي (Workspaces & Collaboration)
*   **الملفات الرئيسية:**
    *   `features/settings/presentation/WorkspaceSettingsScreen.kt`
    *   `features/settings/presentation/WorkspaceViewModel.kt`
    *   `domain/models/Workspace.kt`
*   **الوظائف والشاشات:**
    1.  **مساحات عمل متعددة (Multi-Workspace):** إنشاء مساحات عمل فردية أو لمؤسسات ومراكز الإنتاج.
    2.  **إدارة الأعضاء والأدوار (RBAC):** تعيين الأدوار بدقة (مالك، مخرج، كاتب محتوى، مراجع شرعي، مشاهد).

---

## 4.8. مركز الخصوصية والأمان وحذف البيانات (Privacy Center & Audit Ledger)
*   **الملفات الرئيسية:**
    *   `features/settings/presentation/privacy/PrivacyCenterScreen.kt`
    *   `features/settings/presentation/privacy/PrivacyCenterViewModel.kt`
    *   `domain/models/privacy/PrivacyModels.kt`
*   **الوظائف والشاشات:**
    1.  **تفضيلات جمع البيانات والتحليلات:** خيارات صريحة للتحكم في ملفات التشخيص وتقارير الأعطال.
    2.  **تصدير البيانات الشخصية (Data Portability):** تصدير كامل بيانات المستخدم في ملف JSON مشفر.
    3.  **حق الحذف الشامل (Right to Erasure):** حذف الحساب وكافة المشاريع والسجلات المرتبطة به نهائياً من الخوادم بضغطة واحدة مع تأكيد أمني.
    4.  **سجل النشاطات غير القابل للتلاعب (Activity Audit Log):** تتبع عمليات تسجيل الدخول وتعديل المشاريع والحذف.

---

## 4.9. منظومة إدارة التكاليف وحصص الذكاء الاصطناعي وإدارة الأسرار (Cost & Secret Management)
*   **الملفات الرئيسية:**
    *   `features/cost/presentation/CostManagementScreen.kt`
    *   `domain/models/cost/CostModels.kt`
    *   `domain/repository/cost/CostManagementRepository.kt`
*   **الوظائف والشاشات:**
    1.  **مراقبة الـ Tokens والتكاليف:** حساب تكلفة كل استدعاء لنماذج الذكاء الاصطناعي بدقة متناهية.
    2.  **حواجز الأمان والميزانيات (Budget Caps):** وضع سقف مالي يومي وشهري لمنع استنزاف الميزانية مع إشعارات تنبيهية عند بلوغ 80% و 100%.
    3.  **إدارة الأسرار:** عزل مفاتيح الـ API تماماً داخل Secrets Manager والوصول إليها فقط عبر سيرفر آمن و BuildConfig المحمي.

---

## 4.10. منظومة المراقبة والاستجابة للطوارئ والترقيات (Monitoring & Incident Response)
*   **الملفات الرئيسية:**
    *   `features/monitoring/presentation/MonitoringDashboardScreen.kt`
    *   `features/incident/presentation/IncidentResponseScreen.kt`
    *   `features/migration/presentation/MigrationScreen.kt`
    *   `domain/models/monitoring/MonitoringModels.kt`
    *   `domain/models/incident/IncidentResponseModels.kt`
*   **الوظائف والشاشات:**
    1.  **مراقبة الأعطال الحية (Crashlytics Monitoring):** حصر وتصنيف الاستثناءات والأخطاء بحسب درجة خطورتها (Low, Medium, Critical).
    2.  **غرفة عمليات الاستجابة للحوادث (Incident Room):** تفعيل وضع الطوارئ، إيقاف الخدمات المتعطلة جزئياً، ونشر رسائل إشعار للمستخدمين.
    3.  **محرك الترقية ونقل البيانات (Data Migration Engine):** نقل ومزامنة هياكل البيانات وقواعد بيانات Room بسلاسة وأمان مع ميزة الرجوع للخلف في حال التعثر (Rollback Safe).

---

## 4.11. منظومة البحث الموحد والتصنيف الموضوعي (Unified Search & Taxonomy)
*   **الملفات الرئيسية:**
    *   `features/search/presentation/SearchScreen.kt`
    *   `features/search/presentation/SearchResultCard.kt`
    *   `features/search/presentation/SearchFilterBottomSheet.kt`
    *   `features/taxonomy/presentation/ContentTaxonomyManagementScreen.kt`
*   **الوظائف والشاشات:**
    1.  **البحث الشامل الفوري:** البحث عبر السور، الآيات، المشاريع، الشروح، والأصول بكلمة مفتاحية واحدة.
    2.  **شجرة التصنيف الموضوعي (Knowledge Taxonomy):** شجرة تصنيف إسلامية ومعرفية تغطي العقيدة، الفقه، السيرة، الأخلاق، والتاريخ مع وسوم ذكية.
    3.  **تصفية متقدمة (Smart Filters):** تصفية النتائج حسب النوع، درجة التوثيق الشرعي، تاريخ الإنشاء، أو حصر البحث بالمشاريع الخاصة.

---

## 4.12. منظومة الدعم الفني ومركز المختبرين (Support & Tester Hub)
*   **الملفات الرئيسية:**
    *   `features/support/presentation/HelpCenterScreen.kt`
    *   `features/support/presentation/CreateTicketScreen.kt`
    *   `features/support/presentation/ServiceStatusScreen.kt`
    *   `features/beta/presentation/TesterHubScreen.kt`
    *   `features/beta/presentation/BetaFeedbackScreen.kt`
*   **الوظائف والشاشات:**
    1.  **مركز المساعدة وقاعدة المعرفة:** مقالات إرشادية وفيديوهات تعليمية لاستخدام المنظومة.
    2.  **نظام التذاكر والدعم الفني:** رفع تذكرة دعم فني مع إرفاق السجلات ولقطات الشاشة ومتابعة حالتها (جديدة، قيد المتابعة، مغلقة).
    3.  **شاشة حالة الخدمات (System Status):** عرض حالة سيرفرات الذكاء الاصطناعي، قواعد البيانات، والتخزين السحابي لحظة بلحظة.
    4.  **مركز المختبرين المعتمد (Tester Hub):** مسار مخصص لفريق مختبري الجودة (Beta Testers) لتقييم الشاشات، الإبلاغ عن العيوب، وتتبع مسارات الاستخدام الأساسية.

---

# 5. قاموس نماذج البيانات والكيانات البرمجية (Data Entities & Schemas Dictionary)

| اسم الكيان (Entity) | الملف المصدر | الحقول الرئيسية | الوصف والوظيفة |
| :--- | :--- | :--- | :--- |
| **`Project`** | `domain/models/Project.kt` | `id, title, description, createdAt, updatedAt, status, scenes, audioTrack, workspaceId` | يمثل المشروع الكامل لإنتاج الفيديو |
| **`Scene`** | `domain/models/Project.kt` | `id, order, durationSeconds, visualPrompt, textOverlay, mediaUrl, transitionType` | المشهد البصري المستقل داخل الفيديو |
| **`Ayah`** | `domain/models/quran/QuranModels.kt` | `verseKey, verseNumber, textArabic, pageNumber, audio, isBookmarked` | الآية القرآنية وبياناتها الصوتية والتوثيقية |
| **`Surah`** | `domain/models/quran/QuranModels.kt` | `chapterNumber, nameArabic, nameTranslated, versesCount, revelationPlace` | بيانات السورة في المصحف الشريف |
| **`ShariaClaim`** | `domain/models/review/ShariaReviewModels.kt` | `id, projectId, text, source, verificationLevel, reviewerNotes, status` | الزعم أو النص الخاضع للتدقيق الشرعي |
| **`ContentCorrection`**| `domain/models/correction/ContentCorrectionModels.kt`| `id, claimId, originalText, correctedText, rationale, reviewerId, timestamp` | سجل التعديل والتوثيق الشرعي للنص |
| **`SirajNotification`**| `domain/models/notification/NotificationModels.kt`| `id, title, message, timestamp, isRead, type, targetRoute` | الإشعار الصادر للمستخدم مع توجيهه |
| **`SearchResultItem`** | `domain/models/search/SearchModels.kt` | `id, title, snippet, category, isVerified, route, metadata` | نتيجة البحث الفردية الموحدة |
| **`CostReport`** | `domain/models/cost/CostModels.kt` | `periodStart, periodEnd, totalTokens, estimatedCostUsd, modelBreakdown` | تقرير استهلاك الذكاء الاصطناعي والتكلفة |
| **`SupportTicket`** | `domain/models/support/SupportModels.kt` | `id, userId, subject, description, priority, status, messages, createdAt` | تذكرة الدعم الفني بين المستخدم والفريق |

---

# 6. مصفوفة الأمان والأذونات وسياسات التشغيل (Security Matrix & Runtime Policies)

1.  **أذونات النظام في `AndroidManifest.xml`:**
    *   `android.permission.INTERNET`: للاتصال بالخدمات السحابية، تحميل التلاوات، والذكاء الاصطناعي.
    *   `android.permission.ACCESS_NETWORK_STATE`: لمراقبة حالة الاتصال والتوجيه للوضع غير المتصل تلقائياً.
    *   `android.permission.ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`: لحساب مواقيت الصلاة واتجاه القبلة فقط بعد موافقة صريحة من المستخدم.
    *   `android.permission.POST_NOTIFICATIONS`: لإرسال تنبيهات الصلاة، الإشعارات، وتحديثات المعالجة.
2.  **الأمان البرمجي وتشفير المفاتيح:**
    *   ممنوع منعاً باتاً تضمين مفاتيح الـ API أو أسرار السيرفر داخل الكود المصدري.
    *   تشفير البيانات الحساسة داخل التخزين المحلي باستخدام Android EncryptedSharedPreferences / Room SQLCipher عند اللزوم.
3.  **سياسة الصيانة وعدم كسر الاستقرار:**
    *   تخضع كافة التعديلات لـ 38 وحدة اختبار تلقائية (Unit Test Suites) تضمن استقرار منطق الأعمال وعدم تراجع الأداء.

---

# 7. إقرار التوثيق العدلي والإقفال الرسمي (Notary Attestation & Sealing)

**إشهاد عدلي وتقني:**  
نقر ونشهد بأن هذا الملف يمثل التوصيف الهندسي والشرعي والجمالي الكامل والشامل لتطبيق **سراج (Siraj)**. كُتبت كل فقرة فيه بناءً على الفحص الفعلي للكود المصدري، ونماذج البيانات، ومكونات الواجهة، والخدمات المربوطة حتى تاريخه، دون أي افتراضات غير واقعية أو نواقص.

*   **حالة النظام:** مكتمل، مستقر، ومحصن بصرياً وتقنياً.
*   **خاتم التوثيق:** `SIRAJ-TECHNO-SPIRITUAL-NOTARY-SEAL-2026`
*   **الحفظ والاعتماد:** تم الحفظ بصيغة Markdown الرسمية في جذر المشروع كمرجع تأسيسي دائم.

---
**انتهت وثيقة التوثيق العدلي لتطبيق سراج.**
