#!/bin/bash
awk '
/^## \[Unreleased\]/ {
    print $0
    print ""
    print "### Added"
    print "- صياغة سياسة المحتوى والاستخدام `CONTENT_POLICY.md` متضمنة مصفوفة العقوبات وقواعد المراجعين وقواعد التعامل مع الذكاء الاصطناعي."
    print "- صياغة سياسة الاعتراضات والتصحيح `APPEALS_POLICY.md`."
    print "- بناء واجهة المستخدم `ContentPolicyScreen` لعرض ملخص السياسات داخل التطبيق."
    print "- إضافة فئات الدعم الفني الجديدة `APPEAL_AND_POLICY` و `SOURCE_CORRECTION` في تذاكر الدعم لتمكين الاعتراضات وتصحيح المصادر."
    print "- ربط صفحة السياسات مع إعدادات المحتوى الشرعي في `SettingsScreen`."
    print ""
    next
}
{print $0}
' CHANGELOG.md > CHANGELOG_NEW.md
mv CHANGELOG_NEW.md CHANGELOG.md
