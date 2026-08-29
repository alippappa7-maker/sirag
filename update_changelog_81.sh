#!/bin/bash
awk '
/^## \[Unreleased\]/ {
    print $0
    print ""
    print "### Added"
    print "- نظام متكامل لإدارة الحقوق والتراخيص للأصول (Rights Management) مع سجل قرارات."
    print "- تحديث `Asset` model لدعم معلومات الترخيص، وحالات الحقوق (Unknown, Commercial, Expired...)."
    print "- سياسة إدارة الحقوق `RIGHTS_POLICY.md` لضمان توافق جميع المحتويات."
    print ""
    next
}
{print $0}
' CHANGELOG.md > CHANGELOG_NEW.md
mv CHANGELOG_NEW.md CHANGELOG.md
