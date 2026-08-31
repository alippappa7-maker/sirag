import re

with open('PROJECT_CONTEXT.md', 'r', encoding='utf-8') as f:
    content = f.read()

# Replace the top section
new_top = """# سراج (Siraj)

## حالة التنفيذ
قيد التنفيذ - مرحلة التطوير البصري (الهوية التقنية Techno-Spiritual).

## آخر برومبت منفذ
آخر برومبت منفذ: UI Audit - Phase 2 (Techno-Spiritual Identity)
المرحلة: التطوير البصري (الهوية التقنية)
الحالة: قيد العمل
الموانع: لا توجد
المخاطر المقبولة: لا توجد
الاعتمادات المطلوبة: لا توجد
الخطوة التالية: تعميم الهوية التقنية والعمق البصري على باقي شاشات التطبيق عند الطلب.

## التقنية"""

content = re.sub(r'# سراج \(Siraj\).*?## التقنية', new_top, content, flags=re.DOTALL)

with open('PROJECT_CONTEXT.md', 'w', encoding='utf-8') as f:
    f.write(content)
