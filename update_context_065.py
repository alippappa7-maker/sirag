import re

with open('PROJECT_CONTEXT.md', 'r', encoding='utf-8') as f:
    content = f.read()

new_header = """# سراج (Siraj)

## حالة التنفيذ
مكتمل بنجاح للمرحلة الحالية - تم بناء البنية التحتية لتدويل وتوطين التطبيق (Localization & Internationalization) في "سراج". تمت إضافة ملفات `strings.xml` لدعم اللغتين العربية (كلغة أساسية وافتراضية) والإنجليزية كبنية أولية. تم دمج تغيير اللغة ديناميكياً من الإعدادات باستخدام `AppCompatDelegate` و `LocaleManager` مع حفظ تفضيلات المستخدم.

## آخر prompt منفذ
رقم البرومبت: PROMPT 065
اسم المرحلة: اللغة والتدويل

## المرحلة الحالية
تم تنفيذ نظام التدويل (Localization) الشامل للتطبيق:
1. **استخراج النصوص المركزية**:
   - تم استخدام نصوص `stringResource(R.string.x)` في أكثر العناصر تكراراً (مثل أزرار: إلغاء، حفظ، رجوع، مشاركة، حذف، عام) لدعم الترجمة فوراً وتأسيس بنية صحيحة في `values/strings.xml` و `values-en/strings.xml`.
2. **تبديل اللغة ديناميكياً (Dynamic Locale Switch)**:
   - تم ربط شاشة اللغة في `SettingsPages.kt` لتحديث تفضيلات المستخدم واستدعاء `AppCompatDelegate.setApplicationLocales` لتبديل لغة التطبيق على مستوى النظام بشكل فوري دون الحاجة لإعادة التشغيل.
   - تم تعديل `MainActivity.kt` لترث من `AppCompatActivity` بدلاً من `ComponentActivity` لدعم دوال التدويل المعيارية، مع تبديل `LayoutDirection` لضمان صحة عرض الـ RTL و LTR.
3. **التواريخ والأرقام (Dates & Numbers)**:
   - تم إنشاء ملف `LocalizationUtils.kt` كمرجع موحد لتهيئة وتنسيق التواريخ، والأرقام، والعملات باستخدام `Locale.getDefault()` للتكيف مع لغة العرض وتفادي التنسيق الخاطئ للتواريخ الهجرية وغيرها.
   - تم تعديل `HijriCalendarViewModel` ليعتمد على لغة التطبيق النشطة في عرض التواريخ، مع إبقاء نصوص الأشهر الهجرية كما هي للحفاظ على الدقة.
4. **دعم الاتجاه (RTL/LTR)**:
   - تطبيق إعدادات `android:localeConfig` في `AndroidManifest.xml` لدعم Android 13+.
"""

content = re.sub(r'# سراج \(Siraj\).*?## التقنية', new_header + '\n## التقنية', content, flags=re.DOTALL)

with open('PROJECT_CONTEXT.md', 'w', encoding='utf-8') as f:
    f.write(content)
