import re

with open('CHANGELOG.md', 'r', encoding='utf-8') as f:
    content = f.read()

new_entry = """## [PROMPT 052] - 2026-08-28 (الأرصدة وحدود الاستخدام)
### أُضيف (Added)
- نظام أرصدة للعمليات المكلفة (AI Generation / Rendering).
- نماذج مطورة لـ `CreditTransaction` تشمل `status` (RESERVED, COMPLETED, FAILED, REFUNDED) وتفاصيل الرصيد قبل وبعد.
- دوال لمستودع الاشتراكات لمحاكاة عمليات الحجز، التأكيد، والإرجاع (Reserve, Confirm, Refund).
- وثيقة سياسة الأرصدة `CREDITS_POLICY.md` التي تنظم حماية الأرصدة عبر الخادم وتمنع الازدواجية (Idempotency).

"""

content = new_entry + content

with open('CHANGELOG.md', 'w', encoding='utf-8') as f:
    f.write(content)
