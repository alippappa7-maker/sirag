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
4. **التوثيق التفصيلي الشامل لكافة الوحدات الوظيفية والشاشات (All 16 System Modules - كل صغيرة وكبيرة)**
   - 4.1. وحدة المصادقة والتهيئة (Authentication & Onboarding Engine)
   - 4.2. وحدة الواجهة الرئيسية ولوحة التحكم (Home Dashboard & Daily Pulse)
   - 4.3. وحدة المحراب والعبادات (Mihrab & Spiritual Engine - مواقيت، أذكار، قبلة، تقويم)
   - 4.4. وحدة المصحف الشريف والمشغل الصوتي (Quran Reader & Audio Core)
   - 4.5. استوديو الإنتاج وصناعة الفيديو وإدارة الوظائف (Studio & Project Production Engine)
   - 4.6. وحدة توليد الأفكار والمخطط الذكي (Ideation & Brainstorming Engine)
   - 4.7. وحدة ومضات سراج والمحتوى السريع (Siraj Flashes & Social Engine)
   - 4.8. منظومة الحوكمة والتدقيق الشرعي وتصحيح المحتوى (Sharia Governance & Content Correction)
   - 4.9. منظومة فحص التراخيص وحقوق الملكية (Asset Rights & Licensing Engine)
   - 4.10. منظومة حماية القاصرين والأمان المجتمعي والإشراف (Minor Safety & Moderation)
   - 4.11. منظومة مساحات العمل والتعاون الجماعي (Workspaces & Collaboration)
   - 4.12. مركز الخصوصية والأمان وحذف البيانات وتاريخ النشاط (Privacy Center & Activity Audit)
   - 4.13. منظومة إدارة التكاليف وحصص الذكاء الاصطناعي وإدارة الأسرار (Cost Management & Budget Caps)
   - 4.14. منظومة المراقبة والاستجابة للطوارئ والترقيات (Crash Monitoring, Incident Room & Migration)
   - 4.15. منظومة البحث الموحد والتصنيف المعرفي (Unified Search & Content Taxonomy)
   - 4.16. منظومة الاشتراكات والدعم الفني ومركز المختبرين (Subscriptions, Support & Beta Tester Hub)
5. **قاموس نماذج البيانات والكيانات البرمجية (Data Entities & Schemas Dictionary)**
6. **مصفوفة الأمان والأذونات وسياسات التشغيل (Security Matrix, Permissions & Runtime Policies)**
7. **إقرار التوثيق العدلي والإقفال الرسمي (Notary Attestation & Sealing)**

---

# 1. ديباجة وميثاق التأسيس (Foundational Charter)

تُعد هذه الوثيقة هي **السند التوثيقي العدلي الرسمي** لتطبيق **سراج (Siraj)**. كُتبت هذه الوثيقة لتكون المرجع النهائي الشامل غير القابل للبس لكل سطر برمجي، شاشة، خوارزمية، حالة، ونموذج بيانات داخل المنظومة.

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

# 4. التوثيق التفصيلي الشامل لكافة الوحدات الوظيفية والشاشات

---

## 4.1. وحدة المصادقة والتهيئة (Authentication & Onboarding Engine)
*   **الملفات:**
    *   `features/auth/presentation/AuthScreen.kt`
    *   `features/auth/presentation/AuthViewModel.kt`
    *   `features/onboarding/presentation/OnboardingScreen.kt`
    *   `features/splash/presentation/SplashScreen.kt`
*   **الوظائف:**
    1.  **شاشة البداية (Splash):** التحقق من سلامة الجلسة، تهيئة قواعد البيانات، والتحقق من إصدار التطبيق وحالة التحديثات.
    2.  **جولة التهيئة (Onboarding Tour):** استعراض بصري تفاعلي لأهداف سراج (الإنتاج الهادف، التدقيق الشرعي، والمحراب الرقمي).
    3.  **تسجيل الدخول والتسجيل:** دعم البريد الإلكتروني وكلمة المرور، تسجيل الدخول بحساب Google، وتأكيد الهوية عبر Firebase Auth.
    4.  **استعادة كلمة المرور وإدارة الجلسات:** إرسال روابط إعادة التعيين والتحقق الآمن من الصلاحيات.

---

## 4.2. وحدة الواجهة الرئيسية ولوحة التحكم (Home Dashboard & Daily Pulse)
*   **الملفات:**
    *   `features/home/presentation/HomeScreen.kt`
    *   `features/home/presentation/HomeViewModel.kt`
*   **الوظائف:**
    1.  **النبض اليومي (Daily Pulse):** عرض الآية اليومية المختارة، الحديث النبوي، وحالة الصلاة القادمة.
    2.  **إجراءات الإنتاج السريعة:** أزرار سريعة لبدء مشروع جديد، كتابة فكرة، أو تسجيل صوتي.
    3.  **المشاريع النشطة:** استعراض المشاريع قيد التنفيذ ونسبة اكتمال كل مشروع.
    4.  **شريط التنقل الموحد:** وصول مباشر للمحراب، الاستوديو، الومضات، والبحث.

---

## 4.3. وحدة المحراب والعبادات (Mihrab & Spiritual Engine)
*   **الملفات:**
    *   `features/mihrab/presentation/MihrabScreen.kt`
    *   `features/mihrab/presentation/MihrabViewModel.kt`
    *   `features/mihrab/presentation/adhkar/AdhkarScreen.kt`
    *   `features/mihrab/presentation/prayer/PrayerTimesScreen.kt`
*   **الوظائف:**
    1.  **حساب مواقيت الصلاة الدقيقة:** حساب فلكي دقيق حسب الموقع الجغرافي مع تنبيهات الأذان والعد التنازلي.
    2.  **شاشة الأذكار المتكاملة:** أذكار الصباح والمساء، أذكار الصلاة والنوم، مع عداد لمسي تفاعلي (Haptic Feedback).
    3.  **بوصلة القبلة والتقويم الهجري:** تحديد زاوية الكعبة المشرفة وحساب المناسبات والأيام الفاضلة.

---

## 4.4. وحدة المصحف الشريف والمشغل الصوتي (Quran Reader & Audio Core)
*   **الملفات:**
    *   `features/quran/presentation/QuranScreens.kt`
    *   `features/quran/presentation/QuranViewModels.kt`
*   **الوظائف:**
    1.  **فهرس السور والأجزاء:** قائمة السور الـ 114 مع معلومات النزول (مكية / مدنية) وعدد الآيات.
    2.  **قارئ الآيات التفاعلي:** عرض النص بالرسم العثماني المضبوط، مع إمكانية تكبير الخط والوضع الليلي.
    3.  **المشغل الصوتي المتقدم (ExoPlayer):** استماع للآيات مع إبراز الآية المقروءة بتوهج `SirajTechCard (isActive = isPlaying)`.
    4.  **شريط المشغل السفلي:** تحكم كامل في التكرار والسرعات (1x, 1.25x, 1.5x) مغلف بـ `SirajGlowContainer`.
    5.  **العلامات المرجعية والخواطر:** حفظ الفواصل وتدوين الملاحظات التفسيرية لكل آية محلياً.

---

## 4.5. استوديو الإنتاج وصناعة الفيديو (Studio & Project Engine)
*   **الملفات:**
    *   `features/project/presentation/ProjectEditorScreen.kt`
    *   `features/project/presentation/scenes/ScenesScreen.kt`
    *   `features/project/presentation/scenes/SceneEditorScreen.kt`
    *   `features/project/presentation/plan/ContentPlanScreen.kt`
    *   `features/project/presentation/audio/AudioStudioScreen.kt`
    *   `features/project/presentation/subtitles/SubtitleEditorScreen.kt`
    *   `features/project/presentation/ai/AiImageGeneratorScreen.kt`
    *   `features/project/presentation/export/ProjectExportScreen.kt`
    *   `features/project/presentation/jobs/ProductionJobsScreen.kt`
    *   `features/studio/presentation/StudioScreen.kt`
    *   `features/studio/presentation/analytics/CreatorAnalyticsScreen.kt`
*   **الوظائف:**
    1.  **مخطط المحتوى الذكي (Content Plan):** هيكلة المحتوى وتوليد محاور الأفكار بالذكاء الاصطناعي.
    2.  **محرر المشاهد واللقطات:** ترتيب الخط الزمني (Timeline) وتحديد مدد المشاهد وانتقالات الكاميرا.
    3.  **استوديو الصوتيات:** تسجيل التعليق الصوتي ودمج المؤثرات الصوتية الخالية من المحاذير.
    4.  **محرر الترجمة والشروح:** تزامن الكلمات مع الصوت (Karaoke Highlight) وتخصيص الخطوط والظلال.
    5.  **توليد الصور بالذكاء الاصطناعي:** توليد صور وخلفيات متوافقة مع الضوابط الإسلامية والأخلاقية.
    6.  **إدارة مهام المعالجة والتصدير (Export Engine):** دعم دقات متعددة (1080p, 4K) وصيغ Shorts و Reels و YouTube.
    7.  **تحليلات صانع المحتوى (Creator Analytics):** إحصائيات المشاهدات، معدل الإنجاز، وأداء المشاريع.

---

## 4.6. وحدة توليد الأفكار والمخطط الذكي (Ideation & Brainstorming Engine)
*   **الملفات:**
    *   `features/ideation/presentation/IdeationScreen.kt`
    *   `features/ideation/presentation/IdeationViewModel.kt`
    *   `domain/services/IdeaGeneratorService.kt`
*   **الوظائف:**
    1.  **توليد الزوايا الإبداعية:** اقتراح محاور مبتكرة للمواضيع الإسلامية والتربوية والتاريخية.
    2.  **ربط الأفكار بالمصادر:** اقتراح آيات وأحاديث ملائمة لكل فكرة مطروحة.
    3.  **تحويل الفكرة إلى مشروع فوري:** تحويل مخطط الفكرة بنقرة واحدة إلى خطة إنتاج كاملة في الاستوديو.

---

## 4.7. وحدة ومضات سراج والمحتوى السريع (Siraj Flashes & Social Engine)
*   **الملفات:**
    *   `features/flashes/presentation/FlashesScreen.kt`
    *   `domain/models/flash/FlashModels.kt`
*   **الوظائف:**
    1.  **المشاهدة الرأسية (Vertical Feed):** استعراض المقاطع القصيرة الهادفة بانتقال عمودي سلس.
    2.  **النشر والمشاركة:** إمكانية نشر ومضات سريعة وتداولها وحفظها في المفضلة.

---

## 4.8. منظومة الحوكمة والتدقيق الشرعي وتصحيح المحتوى (Sharia Governance Engine)
*   **الملفات:**
    *   `features/review/presentation/ShariaReviewQueueScreen.kt`
    *   `features/review/presentation/ShariaReviewDetailScreen.kt`
    *   `features/review/presentation/corrections/ContentCorrectionHistoryScreen.kt`
    *   `features/review/presentation/governance/ReviewerGovernanceDashboardScreen.kt`
    *   `features/review/domain/ContentCorrectionEngine.kt`
    *   `features/review/domain/ReviewerGovernanceEngine.kt`
*   **الوظائف:**
    1.  **طابور المراجعة الشرعية:** استلام المحتوى وفرزه حسب درجة الحساسية والأولوية.
    2.  **تدقيق المزاعم والشواهد (Claim Review):** مطابقة النصوص بالأصول المعتمدة وتحديد درجة التوثيق (صحيح، حسن، معتبر).
    3.  **محرك تصحيح المحتوى وسجل التعديلات:** توثيق كل تصحيح شرعي مع بيان العلة الشرعية وهوية المراجع.
    4.  **لوحة حوكمة المراجعين:** قياس موثوقية وأداء المراجعين ومتابعة قرارات الاعتماد.

---

## 4.9. منظومة فحص التراخيص وحقوق الملكية (Asset Rights & Licensing Engine)
*   **الملفات:**
    *   `features/rights/presentation/AssetRightsScreen.kt`
    *   `features/rights/presentation/AssetRightsViewModel.kt`
*   **الوظائف:**
    1.  **فحص التراخيص:** التأكد من مشروعية استخدام الصور والخطوط والمؤثرات الصوتية.
    2.  **توليد بطاقة الإسناد (Attribution Card):** توثيق أسماء المبدعين والمصادر المفتوحة المعتمدة.

---

## 4.10. منظومة حماية القاصرين والأمان المجتمعي والإشراف (Minor Safety & Moderation)
*   **الملفات:**
    *   `features/minor/presentation/MinorSafetyScreen.kt`
    *   `features/moderation/presentation/ModerationScreen.kt`
*   **الوظائف:**
    1.  **التصنيف العمري والرقابة الأبوية:** قفل المحتوى الحساس وتحديد ساعات الاستخدام للأطفال.
    2.  **التصفية الآلية ونظام البلاغات:** منع المحتوى المخالف والتعامل الفوري مع بلاغات المجتمع.

---

## 4.11. منظومة مساحات العمل والتعاون الجماعي (Workspaces & Collaboration)
*   **الملفات:**
    *   `features/settings/presentation/WorkspaceSettingsScreen.kt`
    *   `features/settings/presentation/WorkspaceViewModel.kt`
*   **الوظائف:**
    1.  **مساحات العمل المشتركة:** إدارة مشاريع الفرق والمؤسسات الدعوية والإنتاجية.
    2.  **نظام الصلاحيات (RBAC):** تحديد أدوار المالك، المخرج، المراجع، والكاتب بدقة.

---

## 4.12. مركز الخصوصية والأمان وحذف البيانات وتاريخ النشاط (Privacy & Audit Ledger)
*   **الملفات:**
    *   `features/settings/presentation/privacy/PrivacyCenterScreen.kt`
    *   `features/history/presentation/ActivityHistoryScreen.kt`
*   **الوظائف:**
    1.  **التحكم في البيانات والتشخيص:** تمكين أو تعطيل جمع بيانات الأداء والتشخيص.
    2.  **تصدير وحذف البيانات (GDPR Compliance):** تصدير كامل البيانات أو حذف الحساب نهائياً وفورياً.
    3.  **سجل النشاط غير القابل للتلاعب:** تدوين حركات النظام وتعديلات المشاريع لحماية الملكية.

---

## 4.13. منظومة إدارة التكاليف وحصص الذكاء الاصطناعي وإدارة الأسرار (Cost & Secret Management)
*   **الملفات:**
    *   `features/cost/presentation/CostManagementScreen.kt`
*   **الوظائف:**
    1.  **حساب التكاليف واستهلاك الـ Tokens:** متابعة دقيقة لكل استدعاء للذكاء الاصطناعي.
    2.  **الميزانيات والأسقف المالية (Budget Caps):** إيقاف الاستهلاك تلقائياً عند تجاوز الحد المحدد مع تنبيهات مسبقة.
    3.  **عزل المفاتيح الأمنية:** حماية مفاتيح API عبر Secrets Plugin و BuildConfig دون كشفها في الكود.

---

## 4.14. منظومة المراقبة والاستجابة للطوارئ والترقيات (Crash Monitoring & Migration)
*   **الملفات:**
    *   `features/monitoring/presentation/MonitoringDashboardScreen.kt`
    *   `features/incident/presentation/IncidentResponseScreen.kt`
    *   `features/migration/presentation/MigrationScreen.kt`
*   **الوظائف:**
    1.  **مراقبة الأعطال المباشرة:** تصنيف الاستثناءات عبر Crashlytics وتحليل أسبابها.
    2.  **غرفة طوارئ الحوادث:** تفعيل وضع الصيانة، إيقاف الميزات المتأثرة، وإشعار المستخدمين.
    3.  **محرك الترقية السلسة للبيانات:** ترقية قواعد بيانات Room وهياكل البيانات دون فقدان أي عمل للمستخدمين.

---

## 4.15. منظومة البحث الموحد والتصنيف المعرفي (Unified Search & Taxonomy)
*   **الملفات:**
    *   `features/search/presentation/SearchScreen.kt`
    *   `features/search/presentation/SearchResultCard.kt`
    *   `features/search/presentation/SearchFilterBottomSheet.kt`
    *   `features/taxonomy/presentation/ContentTaxonomyManagementScreen.kt`
*   **الوظائف:**
    1.  **البحث الفوري الشامل:** البحث في السور، الآيات، المشاريع، الشروح، والأصول.
    2.  **شجرة التصنيف المعرفي الإسلامي:** تصنيف المحتوى في العقيدة، الفقه، السيرة، والتاريخ.
    3.  **التصفية الذكية:** حصر النتائج حسب التوثيق الشرعي، النوع، أو المشاريع الخاصة.

---

## 4.16. منظومة الاشتراكات والدعم الفني ومركز المختبرين (Subscriptions, Support & Beta)
*   **الملفات:**
    *   `features/subscription/presentation/SubscriptionScreen.kt`
    *   `features/subscription/presentation/billing/UsageAndBillingScreen.kt`
    *   `features/support/presentation/HelpCenterScreen.kt`
    *   `features/support/presentation/CreateTicketScreen.kt`
    *   `features/support/presentation/ServiceStatusScreen.kt`
    *   `features/beta/presentation/TesterHubScreen.kt`
    *   `features/beta/presentation/BetaFeedbackScreen.kt`
*   **الوظائف:**
    1.  **باقات الاشتراك والفوترة:** إدارة الخطط الشهرية والسنوية والوصول للميزات الاحترافية.
    2.  **مركز الدعم والتذاكر:** تقديم تذاكر الدعم الفني ومتابعتها وحالة الخوادم المباشرة.
    3.  **مركز المختبرين (Tester Hub):** أدوات لفريق الجودة لاختبار مسارات الاستخدام والإبلاغ عن أي خلل.

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
| **`PrayerTimes`** | `domain/models/prayer/PrayerModels.kt` | `fajr, sunrise, dhuhr, asr, maghrib, isha, date, qiblaDirection` | مواقيت الصلاة واتجاه القبلة |
| **`AdhkarItem`** | `domain/models/adhkar/AdhkarModels.kt` | `id, category, text, count, reward, source` | نص الذكر وعدد التكرار وفضله |
| **`Workspace`** | `domain/models/Workspace.kt` | `id, name, ownerId, members, role, plan` | مساحة العمل وصلاحيات الأعضاء |

---

# 6. مصفوفة الأمان والأذونات وسياسات التشغيل (Security Matrix & Runtime Policies)

1.  **أذونات النظام في `AndroidManifest.xml`:**
    *   `android.permission.INTERNET`: للاتصال بالخدمات السحابية، تحميل التلاوات، والذكاء الاصطناعي.
    *   `android.permission.ACCESS_NETWORK_STATE`: لمراقبة حالة الاتصال والتوجيه للوضع غير المتصل تلقائياً.
    *   `android.permission.ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`: لحساب مواقيت الصلاة واتجاه القبلة فقط بعد موافقة صريحة من المستخدم.
    *   `android.permission.POST_NOTIFICATIONS`: لإرسال تنبيهات الصلاة، الإشعارات، وتحديثات المعالجة.
2.  **الأمان البرمجي وتشفير المفاتيح:**
    *   ممنوع منعاً باتاً تضمين مفاتيح الـ API أو أسرار السيرفر داخل الكود المصدري.
    *   تشفير البيانات الحساسة داخل التخزين المحلي باستخدام Android EncryptedSharedPreferences / Room SQLCipher.
3.  **سياسة الصيانة وعدم كسر الاستقرار:**
    *   تخضع كافة التعديلات لاختبارات أوتوماتيكية شاملة (Unit Test Suites) تضمن استقرار منطق الأعمال وعدم تراجع الأداء.

---

# 7. إقرار التوثيق العدلي والإقفال الرسمي (Notary Attestation & Sealing)

**إشهاد عدلي وتقني:**  
نقر ونشهد بأن هذا الملف يمثل التوصيف الهندسي والشرعي والجمالي الكامل والشامل لتطبيق **سراج (Siraj)**. كُتبت كل فقرة فيه بناءً على الفحص الفعلي للكود المصدري، ونماذج البيانات، ومكونات الواجهة، والخدمات المربوطة حتى تاريخه، دون أي افتراضات غير واقعية أو نواقص.

*   **حالة النظام:** مكتمل، مستقر، ومحصن بصرياً وتقنياً.
*   **خاتم التوثيق:** `SIRAJ-TECHNO-SPIRITUAL-NOTARY-SEAL-2026`
*   **الحفظ والاعتماد:** تم الحفظ بصيغة Markdown الرسمية في جذر المشروع كمرجع تأسيسي دائم.

---
**انتهت وثيقة التوثيق العدلي لتطبيق سراج.**
