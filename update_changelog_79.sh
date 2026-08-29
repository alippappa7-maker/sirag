#!/bin/bash
awk '
/^## \[Unreleased\]/ {
    print $0
    print ""
    print "### Added"
    print "- صياغة خطة الإطلاق التدريجي (Phased Rollout Plan) في ملف `ROLLOUT_PLAN.md` لضمان إطلاق آمن ومستقر."
    print "- تحديد مقاييس المراقبة الأساسية (Crashes, ANR, AI Costs, Billing Drops) وشروط الإيقاف (Kill-switches) لكل مرحلة."
    print ""
    next
}
{print $0}
' CHANGELOG.md > CHANGELOG_NEW.md
mv CHANGELOG_NEW.md CHANGELOG.md
