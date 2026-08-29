import re

with open('CHANGELOG.md', 'r', encoding='utf-8') as f:
    content = f.read()

new_log = """## [Unreleased] - Localization & Internationalization (PROMPT 065)
### Added
- تم تفعيل التدويل الأساسي وإنشاء ملفات `strings.xml` للغتين العربية (افتراضية) والإنجليزية كبنية أولية.
- استبدال النصوص الصلبة المتكررة بكثرة (أزرار وإجراءات عامة) بنصوص من `strings.xml` للبدء بخطة التدويل وتفادي كسر الـ RTL.
- تمكين تغيير اللغة ديناميكياً من شاشة الإعدادات باستخدام `AppCompatDelegate` و `LocaleManager`.
- إنشاء `LocalizationUtils` لتنسيق التواريخ، والأرقام، والعملات حسب لغة النظام بشكل آمن ودون اللجوء للترجمة الصلبة.
- تعديل `MainActivity` لوراثة `AppCompatActivity` بدلاً من `ComponentActivity` لدعم الـ LocaleManager وتحديث `LayoutDirection` ديناميكياً لضمان سلامة تخطيط الـ RTL و LTR.
- إضافة إعداد `android:localeConfig` في `AndroidManifest.xml` لدعم Android 13+.

"""

content = re.sub(r'## \[Unreleased\] - Universal Accessibility', new_log + '## [Unreleased] - Universal Accessibility', content)

with open('CHANGELOG.md', 'w', encoding='utf-8') as f:
    f.write(content)

with open('README.md', 'r', encoding='utf-8') as f:
    readme_content = f.read()

new_feature = "- التدويل الكامل ودعم اللغات المتعددة وتغيير لغة العرض ديناميكياً (عربي وإنجليزي)"
if "التدويل الكامل" not in readme_content:
    readme_content = readme_content.replace("- لوحات ألوان واجهات متكيفة وأنماط داكنة وفائقة التباين", "- لوحات ألوان واجهات متكيفة وأنماط داكنة وفائقة التباين\n" + new_feature)

with open('README.md', 'w', encoding='utf-8') as f:
    f.write(readme_content)

