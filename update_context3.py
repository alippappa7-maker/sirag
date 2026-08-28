import re

with open('PROJECT_CONTEXT.md', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace(
    "قيد التنفيذ (مرحلة الحماية وقواعد الوصول اكتملت)",
    "قيد التنفيذ (مرحلة نموذج الاشتراكات والخطط اكتملت)"
).replace(
    "PROMPT 050 (الحماية وقواعد الوصول)",
    "PROMPT 051 (نموذج الاشتراكات والخطط)"
)

added_text = "- نماذج الاشتراكات والخطط والأرصدة (Subscriptions, Plans, Entitlements)."
content = content.replace(
    "- حماية مركزية لقواعد بيانات Firestore و Storage (Security Rules).",
    "- حماية مركزية لقواعد بيانات Firestore و Storage (Security Rules).\n" + added_text
)

decisions_text = "- فصل الخطة (Plan) عن الاشتراك (Subscription) وعن الامتيازات (Entitlement).\n- تأكيد مبدأ التحقق الخادمي (Server-side validation) لعمليات الشراء، وعدم الاحتفاظ بالـ PurchaseToken الخام."
content = content.replace(
    "- تطبيق الحماية على جميع مستندات Firestore بوضع مقفل (Locked Down) بناءً على Custom Claims و OwnerId.",
    "- تطبيق الحماية على جميع مستندات Firestore بوضع مقفل (Locked Down) بناءً على Custom Claims و OwnerId.\n" + decisions_text
)

with open('PROJECT_CONTEXT.md', 'w', encoding='utf-8') as f:
    f.write(content)
