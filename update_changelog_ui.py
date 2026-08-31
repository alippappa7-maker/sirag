import re

with open('CHANGELOG.md', 'r', encoding='utf-8') as f:
    content = f.read()

new_entry = """# سجل التغييرات (Changelog)

## [1.1.1] - Visual Identity Phase 1 (UI Audit)
### Changed
- **تأسيس الهوية البصرية (Visual Identity Setup):**
  - تحديث نظام الألوان المركزي `Color.kt` ليشمل الهوية اللونية الرسمية لتطبيق سراج (أخضر زمردي، ذهبي، وخلفيات مريحة للعين).
  - توحيد نظام الخطوط `Type.kt` وزيادة تباعد الأسطر (Line Height) بنسبة 15% لتناسب النصوص العربية.
  - تعريف وتحديث نظام الأشكال `Shape.kt` (البطاقات 16.dp، القوائم المنبثقة 24.dp، الأزرار 14.dp).
  - إنشاء نظام المسافات المركزي `Spacing.kt` وتوفيره عبر `MaterialTheme.spacing` (8.dp, 16.dp, 20.dp, 24.dp, 32.dp).
  - تطبيق النظام الجديد على `SplashScreen` و `OnboardingScreen`.

"""

content = content.replace("# سجل التغييرات (Changelog)\n", new_entry)

with open('CHANGELOG.md', 'w', encoding='utf-8') as f:
    f.write(content)
