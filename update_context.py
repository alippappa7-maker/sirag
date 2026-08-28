import re

with open('PROJECT_CONTEXT.md', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace(
    "قيد التنفيذ (مرحلة اشتراكات Google Play اكتملت)",
    "قيد التنفيذ (مرحلة اشتراكات App Store اكتملت)"
).replace(
    "PROMPT 053 (اشتراكات Google Play)",
    "PROMPT 054 (اشتراكات App Store)"
)

added_text = "- التأسيس لاشتراكات App Store وStoreKit 2 مع توثيق شامل للتحقق الخادمي وإشعارات V2 (App Store Server Notifications)."
content = content.replace(
    "- تكامل مع Google Play Billing للشراء من المتجر مع تحقق خادمي (Server-Side Validation).",
    "- تكامل مع Google Play Billing للشراء من المتجر مع تحقق خادمي (Server-Side Validation).\n" + added_text
)

decisions_text = "- تطبيق نفس مبدأ التحقق الخادمي (Server-Side) لمشتريات iOS عبر JWS وإشعارات أبل لتأمين الـ Entitlement ومعالجة الـ originalTransactionId."
content = content.replace(
    "- الاعتماد على الخادم فقط لمنح الامتيازات بعد التحقق من `purchaseToken` عبر Google Play Developer API لضمان الأمان.",
    "- الاعتماد على الخادم فقط لمنح الامتيازات بعد التحقق من `purchaseToken` عبر Google Play Developer API لضمان الأمان.\n" + decisions_text
)

with open('PROJECT_CONTEXT.md', 'w', encoding='utf-8') as f:
    f.write(content)
