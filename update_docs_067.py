import re

# Update PROJECT_CONTEXT.md
with open('PROJECT_CONTEXT.md', 'r', encoding='utf-8') as f:
    content = f.read()

new_header = """# سراج (Siraj)

## حالة التنفيذ
مكتمل بنجاح للمرحلة الحالية - تم فصل إعدادات البناء وتكوين البيئات (Development, Staging, Production) لضمان عدم اختلاط البيانات أو المفاتيح. تم استخدام Product Flavors وتخصيص `applicationId` و `versionName` وموارد النظام لكل بيئة مع تجهيز ملفات `google-services.json` منفصلة دون رفع أي أسرار للمستودع.

## آخر prompt منفذ
رقم البرومبت: PROMPT 067
اسم المرحلة: إعدادات البناء والنشر

## المرحلة الحالية
تم تنفيذ بنية متكاملة للبيئات وإعدادات البناء:
1. **بيئات العمل (Product Flavors)**:
   - تم إنشاء ثلاث بيئات: `dev`, `staging`, و `prod` باستخدام `flavorDimensions`.
   - تخصيص `applicationIdSuffix` لكل بيئة (مثلاً `.dev` و `.staging`) لضمان إمكانية تثبيت نسخ متعددة على نفس الجهاز للاختبار والمراجعة.
   - تخصيص `app_name` لكل بيئة (مثل "سراج (Dev)") لتمييزها بصرياً فور التثبيت.
2. **إعدادات Firebase**:
   - تهيئة هيكل مجلدات منفصل لملفات `google-services.json` الخاصة بكل بيئة.
   - إزالة الملف الأساسي وتوفير ملفات نموذجية دون أسرار حقيقية، لتُحقن لاحقاً عبر أدوات CI/CD.
3. **التكوينات والأمان (Configs & Security)**:
   - تمكين إضافة `buildConfigField` لتحديد الـ ENVIRONMENT النشط في الكود.
   - لا تُرفع ملفات الكيستور (Keystore) أو المفاتيح إلى المستودع.
   - الحفاظ على قواعد الأمان التي تمنع خلط الإنتاج ببيئة التطوير (sandbox).
4. **دعم R8**:
   - تفعيل `isMinifyEnabled` و `isShrinkResources` لبيئة `release` لتقليص حجم ملفات التصدير وحماية الكود.
"""

content = re.sub(r'# سراج \(Siraj\).*?## التقنية', new_header + '\n## التقنية', content, flags=re.DOTALL)

with open('PROJECT_CONTEXT.md', 'w', encoding='utf-8') as f:
    f.write(content)

# Update CHANGELOG.md
with open('CHANGELOG.md', 'r', encoding='utf-8') as f:
    changelog = f.read()

new_log = """## [Unreleased] - Build Environments Configuration (PROMPT 067)
### Added
- تكوين `productFlavors` لبيئات `dev`, `staging`, و `prod` في `build.gradle.kts`.
- تخصيص لواحق معرّف الحزمة (`applicationIdSuffix`) واسم التطبيق (`app_name`) لكل بيئة لتسهيل التمييز والفصل.
- تهيئة البنية التحتية لملفات `google-services.json` منفصلة لكل بيئة لتفادي تداخل مشاريع Firebase.
- دمج `BuildConfig` لتوفير متغير `ENVIRONMENT` يتم استخدامه لتوجيه الطلبات والتحكم باللوجز محلياً.
- ضمان عدم حفظ أي أسرار، أو ملفات التوقيع (Keystores)، أو مفاتيح إنتاج حقيقية داخل المستودع.

"""
changelog = re.sub(r'## \[Unreleased\] - Store Listing', new_log + '## [Unreleased] - Store Listing', changelog)

with open('CHANGELOG.md', 'w', encoding='utf-8') as f:
    f.write(changelog)

