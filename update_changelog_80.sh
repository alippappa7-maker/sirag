#!/bin/bash
awk '
/^## \[Unreleased\]/ {
    print $0
    print ""
    print "### Added"
    print "- إعداد وثائق الإصدار النهائي (Production Readiness):"
    print "  - `RELEASE_NOTES.md`: ملاحظات الإصدار 1.0.0."
    print "  - `PRODUCTION_CHECKLIST.md`: قائمة المراجعة للإنتاج (الكود، البنية التحتية، المتاجر، السياسات)."
    print "  - `LAUNCH_DAY_RUNBOOK.md`: خطة مراقبة يوم الإطلاق (أول 72 ساعة)."
    print "  - `ROLLBACK_PLAN.md`: خطة التراجع على مستوى المتجر والـ Backend."
    print "  - `KNOWN_LIMITATIONS.md`: القيود الفنية في الإصدار الأول."
    print "  - `SUPPORT_ESCALATION.md`: خطة تصعيد الدعم الفني."
    print ""
    next
}
{print $0}
' CHANGELOG.md > CHANGELOG_NEW.md
mv CHANGELOG_NEW.md CHANGELOG.md
