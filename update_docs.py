import re

changelog_addition = """
## [PROMPT 010] - 2026-08-27 (قسم الإعدادات والتفضيلات)
### أُضيف (Added)
- **شاشة الإعدادات الشاملة (SettingsScreen):** تصميم جديد كلياً يعتمد على قائمة رئيسية مع قوائم فرعية للتفضيلات.
- **إدارة التفضيلات (UserPreferences):** توسيع النموذج ليشمل السمة (المظهر)، تقليل الحركة، لغة العرض، المدينة (لحساب المواقيت)، إعدادات الإشعارات، إعدادات المحراب، الجودة، وقفل التطبيق.
- **مزامنة التفضيلات (Sync):** تم تحديث `AuthRepository` و `FirebaseAuthRepositoryImpl` لحفظ الإعدادات بشكل متزامن في Firestore وتطبيقها محلياً فوراً.
- **حذف الحساب:** إضافة خيار لحذف الحساب نهائياً مع نافذة تأكيد تحذيرية لحماية المستخدم.
- **السمة التلقائية:** تم ربط `MainActivity` مع التفضيلات لتحديث سمة التطبيق (فاتح/داكن/تلقائي) بمجرد تغييرها.
"""

with open('CHANGELOG.md', 'r') as f:
    content = f.read()

content = content.replace("# سجل تغييرات المشروع (Changelog)\n", "# سجل تغييرات المشروع (Changelog)\n" + changelog_addition)

with open('CHANGELOG.md', 'w') as f:
    f.write(content)

with open('PROJECT_CONTEXT.md', 'r') as f:
    content = f.read()

content = content.replace("009 (الرئيسية وإدارة المشاريع)", "010 (قسم الإعدادات والتفضيلات)")
content = content.replace("- تم الانتهاء من تنفيذ PROMPT 009", "- تم الانتهاء من تنفيذ PROMPT 010 (قسم الإعدادات والتفضيلات الشامل).\n- تم الانتهاء من تنفيذ PROMPT 009")
content = content.replace("الخطوة التالية:\nالبدء في PROMPT 010", "الخطوة التالية:\nالبدء في استوديو المحتوى (PROMPT 011)")

with open('PROJECT_CONTEXT.md', 'w') as f:
    f.write(content)

