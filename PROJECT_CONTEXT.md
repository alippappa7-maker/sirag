# سراج (Siraj)

## حالة التنفيذ
مكتمل بنجاح للمرحلة الحالية - تم إعداد وتكامل خطة الاستجابة للحوادث والطوارئ (Incident Response Plan) ولوحة التحكم الإدارية للطوارئ `IncidentResponseScreen`، مع تغطية شاملة لـ 10 أنواع من الحوادث الحرجة، وتوفير أدوات التدخل الفوري (زر إيقاف النشر العام Kill-Switch، تدوير الأسرار والمفاتيح، سحب وتعليق المحتوى، التصحيح الشرعي المعتمد بمراجعة مزدوجة، واسترداد الأرصدة والمدفوعات المكررة)، ومصفوفة جهات الاتصال وفرق الطوارئ 24/7، مع الحفاظ الصارم على أسرار البنية التحتية وصون النص الشرعي.

## آخر prompt منفذ
رقم البرومبت: PROMPT 076
اسم المرحلة: الاستجابة للحوادث

## المرحلة الحالية
- إنشاء نماذج الاستجابة للحوادث `IncidentResponseModels.kt` (الأنواع الـ 10، المراحل الـ 8، الأدوار المسؤولة، مستويات الخطورة، تقارير ما بعد الحادث Post-Mortems، والتصحيح الشرعي المزدوج).
- إنشاء محرك الاستجابة للطوارئ `IncidentResponseEngine.kt`:
  - التحقق من اعتماد مراجعين شرعيين اثنين (Double Review) لتصويب النصوص القرآنية والدينية قبل النشر.
  - تجريد إشعارات الحوادث العامة للمستخدمين لمنع تسرب أي أسرار أو تفاصيل أمنية يستغلها مهاجم.
  - التحقق من تفويض الأدوار (Separation of Duties) للإجراءات الطارئة كالإيقاف الشامل وتدوير المفاتيح.
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

## التقنية
- Kotlin & Jetpack Compose (Material 3)
- Firebase Firestore, Cloud Functions, Cloud Secret Manager
- Architecture: MVVM, Clean Architecture, Repository Pattern
- Testing: JUnit4, MockK, Coroutines Test

## بنية الوحدات
- `app/src/main/java/com/siraj/app/`
  - `domain/models/incident/IncidentResponseModels.kt`: نماذج أنواع الحوادث، الإجراءات الطارئة، والتصحيحات الشرعية.
  - `domain/repository/incident/IncidentResponseRepository.kt`: واجهة مستودع الاستجابة للحوادث.
  - `core/incident/IncidentResponseEngine.kt`: محرك تدقيق الإجراءات الطارئة والتصحيح الشرعي وتجريد الرسائل.
  - `data/repository/incident/FirebaseIncidentResponseRepositoryImpl.kt`: تطبيق المستودع وتدفق البيانات الحية.
  - `features/admin/presentation/incident/`:
    - `IncidentResponseScreen.kt`: واجهة لوحة إدارة الطوارئ والـ Playbooks والتقارير ومصفوفة الاتصال.
    - `IncidentResponseViewModel.kt`: إدارة حالات الطوارئ والإجراءات وسجلات التدقيق.
  - `features/admin/presentation/AdminScreen.kt`: تضمين تبويب الاستجابة للحوادث.

## الخدمات المربوطة
- Firestore (`incident_reports`, `emergency_actions`, `sharia_corrections`, `escalation_contacts`), Cloud Functions (`executeEmergencyContainmentAction`), Secret Manager.

## البيئة الحالية
- Development, Staging, Production (Isolated incident and audit telemetry).

## المخاطر المعروفة
- منع إخفاء الحوادث المؤثرة على المستخدمين أو البيانات.
- منع تسريب تفاصيل تقنية أو أمنية في بيانات المستخدمين قد يستغلها مهاجم.
- اشتراط مراجعين شرعيين اثنين لأي تصويب في النصوص الدينية.

## القرارات التقنية
- تسجيل كافة الإجراءات الطارئة في سجل تدقيق غير قابل للحذف (Immutable Emergency Audit Trail).
- قفل النشر فورياً عبر زر الطوارئ العام في الحوادث الحرجة من نوع P0.

## الخطوة التالية
المضي قدماً في البرومبتات والمراحل التالية بحسب خطة المشروع.
