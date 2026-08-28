import re

with open('README.md', 'r', encoding='utf-8') as f:
    content = f.read()

new_item = "18. **الحماية وقواعد الوصول**: تأمين مركزي لـ Firestore و Storage باستخدام Firebase Security Rules و Custom Claims، مع حماية مسارات الدفع والتعديل من العميل."

content = content.replace(
    "17. **لوحة الإدارة**: إدارة المحتوى (اعتماد، تعليق، أرشفة) وتصنيفه مع سجل تدقيق لكل الإجراءات.",
    "17. **لوحة الإدارة**: إدارة المحتوى (اعتماد، تعليق، أرشفة) وتصنيفه مع سجل تدقيق لكل الإجراءات.\n" + new_item
)

with open('README.md', 'w', encoding='utf-8') as f:
    f.write(content)
