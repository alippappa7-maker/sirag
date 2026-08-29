#!/bin/bash
awk '
/^## \[Unreleased\]/ {
    print $0
    print ""
    print "### Added"
    print "- تصميم وبناء واجهات مركز الدعم الفني، المقالات، وتذاكر المساعدة (`HelpCenterScreen`, `HelpArticleDetailScreen`, `CreateTicketScreen`, `TicketListScreen`, `TicketDetailScreen`, `ServiceStatusScreen`)."
    print "- دمج مركز الدعم الفني وتوجيه المستخدمين ضمن شاشة إعدادات التطبيق `SettingsScreen` وربط جميع مسارات التنقل الجديدة في `AppNavigation`."
    print "- حل مشكلة توافق `combine` في `SupportViewModel` بدمج المجموعات بدلاً من استخدام دوال زائدة."
    print ""
    next
}
{print $0}
' CHANGELOG.md > CHANGELOG_NEW.md
mv CHANGELOG_NEW.md CHANGELOG.md
