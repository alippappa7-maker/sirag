with open('PROJECT_CONTEXT.md', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace("آخر برومبت تم تنفيذه: PROMPT 055", "آخر برومبت تم تنفيذه: PROMPT 056")
content = content.replace("المرحلة الحالية: صفحة الخطط والأسعار", "المرحلة الحالية: الاستخدام والفوترة")

with open('PROJECT_CONTEXT.md', 'w', encoding='utf-8') as f:
    f.write(content)

with open('CHANGELOG.md', 'r', encoding='utf-8') as f:
    content = f.read()

new_log = """
### [v0.1.0-alpha.56] - 2026-08-28
- إضافة شاشة `UsageAndBillingScreen` لعرض الاستخدام والفوترة.
- عرض الخطة الحالية وتاريخ التجديد/الانتهاء باستخدام بيانات الخادم (Firestore).
- إضافة أزرار لإدارة الاشتراك في المتجر (Google Play) وتغيير الخطط.
- ربط رصيد المستخدم بالمؤشرات المرئية للاستهلاك وتحذير المستخدم عند تجاوز 80%.
- إظهار أكثر العمليات استهلاكاً للرصيد وسجل المعاملات السابقة.
- تحديث `SubscriptionViewModel` لدعم سجل المعاملات (CreditTransaction).
- تحديث `SettingsScreen` لتضمين رابط "الاستخدام والفوترة" وتحديث `AppNavigation` للتنقل.

"""

content = content.replace("## [Unreleased]", "## [Unreleased]\n" + new_log)

with open('CHANGELOG.md', 'w', encoding='utf-8') as f:
    f.write(content)

with open('README.md', 'a', encoding='utf-8') as f:
    f.write("- **الاستخدام والفوترة:** صفحة تتبع الأرصدة، عرض المعاملات، وتنبيهات الاستهلاك المرتفع (PROMPT 056).\n")
