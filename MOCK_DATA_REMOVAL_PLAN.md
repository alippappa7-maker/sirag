# خطة التخلص من البيانات الوهمية (Mock Data Removal Plan)

تهدف هذه الخطة إلى تتبع وإزالة جميع البيانات التجريبية والوهمية (Mock/Dummy/Fake Data) من تطبيق "سراج" واستبدالها بالبيانات الفعلية ومزودات الإنتاج (Production Providers).

## 1. الواجهات والملفات المركزية
- [ ] حذف أو إيقاف الاعتماد على ملف `MockData.kt` بالكامل.
- [ ] تحديث `AudioLibraryScreen.kt`: ربط الشاشة بمستودع الصوتيات الحقيقي (ViewModel / AudioRepository) بدلاً من `MockData.audios`.
- [ ] تحديث `DetailsScreen.kt`: ربط الشاشة بمستودع المصادر الحقيقي بدلاً من `MockData.sources`.

## 2. المحتوى الديني والعبادي الأساسي
- [ ] `HadithRepositoryImpl.kt`: استبدال `sampleHadiths` بجلب البيانات من Firestore (مجموعة الأحاديث).
- [ ] `TafsirRepositoryImpl.kt`: استبدال القاموس التجريبي `tafsirCache` بجلب تفاسير حقيقية عبر Firestore.
- [ ] `AdhkarRepositoryImpl.kt`: نقل قائمة `mockAdhkar` إلى قاعدة بيانات محلية (Room) محملة مسبقاً أو سحابية.
- [ ] `PrayerRepositoryImpl.kt`: دمج مكتبة حساب مواقيت الصلاة الفلكية (مثل Adhan-Java) بدلاً من القيم الثابتة ومحاكاة الوقت.
- [ ] `QuranRepositoryImpl.kt`: استبدال الروابط الوهمية لملفات الصوت (dummy audio URL) بروابط حقيقية من API المصحف.

## 3. مستودعات الذاكرة المؤقتة (In-Memory "Firebase" Repositories)
يجب تحويل المستودعات التالية للاتصال الفعلي بـ Firebase Firestore / Firebase Storage بدلاً من `MutableStateFlow` و `Map`:
- [ ] `FirebaseFlashRepositoryImpl.kt`: ربط الومضات الدعوية (Flashes).
- [ ] `FirebaseFlashPublishingRepositoryImpl.kt`: ربط نشر الومضات وقاعدة البيانات.
- [ ] `FirebaseInteractionRepositoryImpl.kt`: ربط الإعجابات والمتابعات الحقيقية.
- [ ] `FirebaseSafetyRepositoryImpl.kt`: ربط بلاغات الأمان والمجتمع.
- [ ] `FirebaseSubscriptionRepositoryImpl.kt`: دمج خطط الاشتراكات مع Google Play Billing Backend.
- [ ] `FirebaseShariaReviewRepositoryImpl.kt`: مسح دالة `createInitialMockData()` الضخمة وربط طلبات المراجعة الشرعية الحقيقية.
- [ ] `FirebaseContentManagementRepositoryImpl.kt`: ربط إدارة المحتوى للمشرفين.
- [ ] `FirebaseSupportRepositoryImpl.kt`: ربط مقالات الدعم وتذاكر المساعدة.
- [ ] `FirebaseIncidentResponseRepositoryImpl.kt`: ربط سجلات الطوارئ وإدارة الحوادث.
- [ ] `FirebaseShareRepositoryImpl.kt`: حفظ الروابط المُولدة في قاعدة البيانات بدلاً من الذاكرة.
- [ ] `FirebaseCreatorAnalyticsRepositoryImpl.kt`: ربط الإحصائيات التحليلية بمحرك حسابي حقيقي بدلاً من الأرقام المحاكاة.
- [ ] `FirebaseNotificationRepositoryImpl.kt`: التوقف عن زرع إشعارات تجريبية (`sampleList`) عند بدء التشغيل.

## 4. مزودات البحث والذكاء الاصطناعي (Mock Providers)
- [ ] `ExternalMediaSearchViewModel.kt`: إزالة الحقن الافتراضي لـ `MockMediaSearchProviderImpl` واستخدام مزود حقيقي.
- [ ] `MockMediaSearchProviderImpl.kt`: حذفه بالكامل بعد توفير البديل.
- [ ] `FirebaseAiImageGeneratorServiceImpl.kt`: إزالة محرك الـ Fallback الذي يعتمد على `MockAiImageGeneratorServiceImpl` والاعتماد حصرياً على Cloud Function.
- [ ] `MockAiImageGeneratorServiceImpl.kt`: حذفه بالكامل بعد التأكد من عمل الوظيفة السحابية.
- [ ] `MockIdeaGeneratorServiceImpl.kt`: حذفه وتفعيل محرك الأفكار الفعلي المتصل بـ Gemini.

## 5. مستودعات الحوكمة والأنظمة الإدارية
- [ ] `ReviewerGovernanceRepositoryImpl.kt`: ربط حوكمة المراجعين وتوزيعهم بـ Firestore وتفريغ البيانات الوهمية.
- [ ] `ContentCorrectionRepositoryImpl.kt`: مسح بيانات الإصدارات الوهمية (`seedSampleData`).
- [ ] `ContentTaxonomyRepositoryImpl.kt`: تفريغ الداتا الأولية للمصنفات.
- [ ] `AdminSecurityRepositoryImpl.kt`: إزالة حساب الإدارة الافتراضي (`admin_1`) وربطه بنظام المصادقة.
- [ ] `MinorSafetyRepositoryImpl.kt`: إزالة الحسابات الوهمية الخاصة بسلامة القاصرين.
- [ ] `MigrationRepositoryImpl.kt` & `FirebaseBackupRepositoryImpl.kt`: ربط الترحيل والنسخ الاحتياطي بالبيئة الحقيقية.
- [ ] `CostManagementRepositoryImpl.kt`: ربط حساب استهلاك الأرصدة بالحسابات الفعلية.

## 6. إجراءات الأمان والبيئة (Environment Controls)
- [ ] `EnvironmentConfig.kt`: تفعيل فحص المتغير `allowMockData` في أي مستودع لا يزال قيد التطوير لمنع تسرب البيانات الوهمية تماماً في بيئة الإنتاج (`Production`).
