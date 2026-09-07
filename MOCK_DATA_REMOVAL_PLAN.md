# خطة التخلص من البيانات الوهمية (Mock Data Removal Plan)

تهدف هذه الخطة إلى تتبع وإزالة جميع البيانات التجريبية والوهمية (Mock/Dummy/Fake Data) من تطبيق "سراج" واستبدالها بالبيانات الفعلية ومزودات الإنتاج (Production Providers).

## 1. الواجهات والملفات المركزية
- [x] حذف أو إيقاف الاعتماد على ملف `MockData.kt` بالكامل. *(الملف غير موجود)*
- [x] تحديث `AudioLibraryScreen.kt`: لا يعتمد على MockData؛ يستقبل قائمة من المستودع.
- [x] تحديث `DetailsScreen.kt` / الشاشات المرتبطة: لا يوجد اعتماد على MockData.

## 2. المحتوى الديني والعبادي الأساسي
- [x] `FirebaseHadithRepositoryImpl.kt`: جلب من Firestore (`hadiths` / `hadith_collections`).
- [x] `FirebaseTafsirRepositoryImpl.kt`: جلب عبر Firestore.
- [x] `FirebaseAdhkarRepositoryImpl.kt`: جلب من Firestore (`adhkar` / `adhkar_categories`).
- [x] `AladhanPrayerRepositoryImpl.kt`: مواقيت عبر مزود Aladhan.
- [x] `QuranRepositoryImpl.kt`: API المصحف + روابط everyayah الحقيقية للتلاوة.

## 3. مستودعات Firebase (كانت In-Memory)
- [x] `FirebaseFlashRepositoryImpl.kt`: تغذية من Firestore + تفاعلات/بلاغات حقيقية.
- [x] `FirebaseFlashPublishingRepositoryImpl.kt`: نشر الومضات عبر Firestore.
- [x] `FirebaseInteractionRepositoryImpl.kt`: **مكتمل** — مجموعة `interactions` + `interaction_counters`.
- [x] `FirebaseSafetyRepositoryImpl.kt`: **مكتمل** — `reports` / `ugc_items` / `ugc_appeals` / `moderation_logs` / إلخ.
- [ ] `FirebaseSubscriptionRepositoryImpl.kt`: التحقق من دمج Google Play Billing Backend بالكامل.
- [ ] `FirebaseShariaReviewRepositoryImpl.kt`: مراجعة خلوها من seed mock.
- [ ] `FirebaseContentManagementRepositoryImpl.kt`: audit logs ما زالت جزئياً محلية.
- [ ] `FirebaseSupportRepositoryImpl.kt`: التحقق من الربط الكامل.
- [ ] `FirebaseIncidentResponseRepositoryImpl.kt`: جزء من الحالة ما زال محلياً.
- [x] `FirebaseShareRepositoryImpl.kt`: **مكتمل** — مجموعة `share_links`.
- [ ] `FirebaseCreatorAnalyticsRepositoryImpl.kt`: التحقق من الأرقام الحية.
- [x] `FirebaseNotificationRepositoryImpl.kt`: Room + Firestore sync (بدون sampleList).

## 4. مزودات البحث والذكاء الاصطناعي
- [x] لا توجد ملفات `Mock*ServiceImpl.kt` في الشجرة الحالية.
- [x] `FirebaseAiImageGeneratorServiceImpl.kt` / `FirebaseIdeaGeneratorServiceImpl.kt`: Cloud Functions.

## 5. مستودعات الحوكمة والأنظمة الإدارية (متبقية جزئياً)
- [ ] `ReviewerGovernanceRepositoryImpl.kt`
- [ ] `ContentCorrectionRepositoryImpl.kt` (maps محلية)
- [ ] `ContentTaxonomyRepositoryImpl.kt`
- [ ] `AdminSecurityRepositoryImpl.kt`
- [ ] `MinorSafetyRepositoryImpl.kt`
- [ ] `MigrationRepositoryImpl.kt` (in-memory store)
- [ ] `CostManagementRepositoryImpl.kt` (جزئي)

## 6. إجراءات الأمان والبيئة
- [x] `EnvironmentConfig.allowMockData`: مفعّل — `false` في Production.
- [x] قواعد Firestore محدّثة لمجموعات: `share_links`, `ugc_*`, `moderation_logs`, `interaction_counters`, `user_blocks`, `user_suspensions`, `terms_consents`.

## تقدم هذه الجلسة
ربط التفاعلات، المشاركة، الأمان/UGC، وتفاعلات الومضات بـ Firestore وإزالة التخزين في الذاكرة لهذه المسارات.
