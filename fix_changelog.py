import re

with open('CHANGELOG.md', 'r', encoding='utf-8') as f:
    content = f.read()

new_entry = """## [PROMPT 049] - 2026-08-28 (إدارة المحتوى)
### أُضيف (Added)
- لوحة الإدارة: قائمة لإدارة المواد مع التصفية والبحث وتحديث الحالات.
- سجل التدقيق (Audit Log): تسجيل التغييرات والقيم القديمة والجديدة.
- حالات المحتوى: اعتماد، تعليق، أرشفة واستعادة.
- تصدير التقارير الإدارية (Mock).

"""

content = new_entry + content

with open('CHANGELOG.md', 'w', encoding='utf-8') as f:
    f.write(content)
