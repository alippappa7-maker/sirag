import re

with open('CHANGELOG.md', 'r', encoding='utf-8') as f:
    content = f.read()

new_entry = """## [PROMPT 054] - 2026-08-28 (اشتراكات App Store)
### أُضيف (Added)
- وثيقة `APP_STORE_BILLING.md` كدليل شامل لتكامل StoreKit 2 والتعامل مع التحقق الخادمي عبر App Store Server API.
- تحديث المتغيرات البيئية الخادمية (`.env.example`) لتشمل إعدادات App Store Connect (Issuer ID, Key ID, Environment).
- تحديث محاكي خادم الاشتراكات (`FirebaseSubscriptionRepositoryImpl`) لدعم واجهة `app_store` وتوثيق طريقة التحقق من المعاملات والتوقيع الرقمي (JWS).

"""

content = new_entry + content
with open('CHANGELOG.md', 'w', encoding='utf-8') as f:
    f.write(content)


with open('README.md', 'r', encoding='utf-8') as f:
    content = f.read()

new_item = "22. **اشتراكات App Store**: التأسيس النظري والخادمي للربط مع StoreKit 2 ومعالجة اشتراكات iOS بإشعارات App Store Server Notifications V2."

content = content.replace(
    "21. **اشتراكات Google Play**: دمج عمليات الشراء عبر متجر جوجل باستخدام `BillingClient` مع توثيق التحقق الخادمي الإلزامي لمنح الامتيازات وحماية `purchaseToken`.",
    "21. **اشتراكات Google Play**: دمج عمليات الشراء عبر متجر جوجل باستخدام `BillingClient` مع توثيق التحقق الخادمي الإلزامي لمنح الامتيازات وحماية `purchaseToken`.\n" + new_item
)

with open('README.md', 'w', encoding='utf-8') as f:
    f.write(content)
