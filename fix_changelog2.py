import re

with open('CHANGELOG.md', 'r', encoding='utf-8') as f:
    content = f.read()

new_entry = """## [PROMPT 050] - 2026-08-28 (الحماية وقواعد الوصول)
### أُضيف (Added)
- Security Rules: قواعد حماية متكاملة لـ Firestore و Cloud Storage بناءً على Custom Claims و OwnerId.
- الحماية من العميل: منع المستخدم العادي من تعديل قيم الرصيد، الدور، وخطة الاشتراك.
- حماية السجلات: تأمين `audit_logs` و `reports` لتصبح مقروءة فقط للإدارة.
- اختبارات القواعد: إعداد بيئة اختبار باستخدام `@firebase/rules-unit-testing` للتأكد من حماية المسارات.

"""

content = new_entry + content

with open('CHANGELOG.md', 'w', encoding='utf-8') as f:
    f.write(content)
