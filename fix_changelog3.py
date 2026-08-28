import re

with open('CHANGELOG.md', 'r', encoding='utf-8') as f:
    content = f.read()

new_entry = """## [PROMPT 051] - 2026-08-28 (نموذج الاشتراكات والخطط)
### أُضيف (Added)
- نماذج الاشتراكات (Subscription Models): تم بناء `Plan`, `Subscription`, `Entitlement`, `CreditBalance`, و `CreditTransaction`.
- الامتيازات والاستهلاك: فصل ميزات الاستهلاك (Usage Limits) عن الميزات المتاحة (Features).
- مستودع الاشتراكات (Subscription Repository): واجهة ومستودع لتقديم بيانات الخطط والأرصدة والامتيازات بشكل آمن والتأسيس للتحقق الخادمي.

"""

content = new_entry + content

with open('CHANGELOG.md', 'w', encoding='utf-8') as f:
    f.write(content)
