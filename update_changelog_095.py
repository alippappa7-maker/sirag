import re

with open('CHANGELOG.md', 'r', encoding='utf-8') as f:
    content = f.read()

new_entry = """# سجل التغييرات (Changelog)

## [1.1.0] - Final Project Review (PROMPT 095)
### Added
- **المراجعة النهائية (Final Legal, Sharia, and Technical Review):**
  - إنشاء `FINAL_LEGAL_REVIEW.md` لتوثيق الجاهزية القانونية وحماية القاصرين وشروط الاستخدام.
  - إنشاء `FINAL_SHARIA_REVIEW.md` لتوثيق الجاهزية الشرعية، فصل النصوص، الاعتمادات، وحماية الفتاوى.
  - إنشاء `FINAL_TECHNICAL_REVIEW.md` لتوثيق الأمان التقني وقواعد البيانات والأسرار.
  - إنشاء `RELEASE_BLOCKERS.md` لتوثيق موانع الإطلاق والاعتمادات البشرية المطلوبة.
  - إصدار قرار الجاهزية الرسمي `FINAL_GO_NO_GO.md` بحالة `CONDITIONAL_GO`.
  - عدم إضافة أي ميزات برمجية جديدة (مرحلة حوكمة ومراجعة حصرية).

"""

content = content.replace("# سجل التغييرات (Changelog)\n", new_entry)

with open('CHANGELOG.md', 'w', encoding='utf-8') as f:
    f.write(content)
