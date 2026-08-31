import sys

with open('PROJECT_CONTEXT.md', 'r', encoding='utf-8') as f:
    content = f.read()

new_entry = """## آخر برومبت منفذ
آخر برومبت منفذ: تعميم الهوية التقنية (المرحلة 1: الواجهات المركزية) (PROMPT 54 continued)
المرحلة: التطوير البصري (الهوية التقنية)
الحالة: مكتمل
الموانع: لا توجد"""

content = content.replace("## آخر برومبت منفذ\nآخر برومبت منفذ: إضافة الحركة والتدقيق البصري النهائي (PROMPT 54)\nالمرحلة: التطوير البصري (الهوية التقنية)\nالحالة: مكتمل\nالموانع: لا توجد", new_entry)

with open('PROJECT_CONTEXT.md', 'w', encoding='utf-8') as f:
    f.write(content)
