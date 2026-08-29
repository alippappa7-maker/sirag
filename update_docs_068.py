import re

# Update PROJECT_CONTEXT.md
with open('PROJECT_CONTEXT.md', 'r', encoding='utf-8') as f:
    content = f.read()

new_header = """# سراج (Siraj)

## حالة التنفيذ
مكتمل بنجاح للمرحلة الحالية - تم إنشاء خطة اختبار قبول شاملة (UAT & Pre-release Test Plan) تغطي كافة رحلات المستخدم الأساسية (Core User Journeys) وحالات الاستخدام الاستثنائية والوصول. الخطة موثقة وجاهزة للتنفيذ اليدوي لضمان خلو التطبيق من العيوب الحرجة قبل نشره في المتاجر.

## آخر prompt منفذ
رقم البرومبت: PROMPT 068
اسم المرحلة: اختبار ما قبل النشر

## المرحلة الحالية
تم إنجاز التجهيزات النهائية للاختبارات:
1. **وثيقة خطة الاختبار (TEST_PLAN.md)**:
   - تفصيل سيناريوهات الاختبار لكافة أقسام التطبيق: التسجيل، مساحة العمل، المراجعة الشرعية، المحراب، الومضات، إعدادات الخصوصية، والاشتراكات الوهمية (Sandbox).
   - تضمين اختبارات حالات الحافة (Edge Cases): ضعف الشبكة، وضع عدم الاتصال (Offline)، تغيير اللغة والثيم، وتحديث التطبيق.
   - تضمين متطلبات إمكانية الوصول: توافقية قارئات الشاشة وتكبير الخطوط.
2. **تصنيف العيوب**:
   - تحديد وتصنيف الأخطاء (Blocker, Critical, Major, Minor) لضمان عدم إطلاق التطبيق بوجود أخطاء تمنع العمليات الأساسية.
3. **التجهيز للعملية اليدوية**:
   - الخطة مهيأة ليتم استخدامها من قبل المراجعين ومختبري الجودة، مع سجل فارغ لرصد الأخطاء وإصلاحها لاحقاً.
"""

content = re.sub(r'# سراج \(Siraj\).*?## التقنية', new_header + '\n## التقنية', content, flags=re.DOTALL)

with open('PROJECT_CONTEXT.md', 'w', encoding='utf-8') as f:
    f.write(content)

# Update CHANGELOG.md
with open('CHANGELOG.md', 'r', encoding='utf-8') as f:
    changelog = f.read()

new_log = """## [Unreleased] - Pre-release Testing Plan (PROMPT 068)
### Added
- إنشاء وثيقة `TEST_PLAN.md` الشاملة لاختبارات القبول (UAT).
- تحديد مسارات الاختبار الأساسية (Authentication, Studio, Review, Mihrab, Flash).
- تحديد سيناريوهات اختبار الشروط الاستثنائية والوصول الشامل (Offline, Weak Network, Screen Reader, Scaled Fonts, Deep Links).
- وضع هيكلية لتسجيل العيوب والأخطاء وتصنيفها لمنع تسرب الأعطال الحرجة (Blockers/Criticals) إلى بيئة الإنتاج.

"""
changelog = re.sub(r'## \[Unreleased\] - Build Environments Configuration', new_log + '## [Unreleased] - Build Environments Configuration', changelog)

with open('CHANGELOG.md', 'w', encoding='utf-8') as f:
    f.write(changelog)

