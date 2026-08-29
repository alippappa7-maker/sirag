import re

# Update PROJECT_CONTEXT.md
with open('PROJECT_CONTEXT.md', 'r', encoding='utf-8') as f:
    content = f.read()

new_header = """# سراج (Siraj)

## حالة التنفيذ
مكتمل بنجاح للمرحلة الحالية - تم تجهيز حزمة نصوص ومواد المتاجر (Store Listing) لتطبيقي Google Play و App Store. تمت صياغة المحتوى التسويقي، والوصف، والسياسات، ونصوص لقطات الشاشة باللغتين العربية والإنجليزية بشكل يتوافق مع ميزات التطبيق الفعلية وبما يتناسب مع شروط الشفافية وعدم التضليل.

## آخر prompt منفذ
رقم البرومبت: PROMPT 066
اسم المرحلة: صفحات المتاجر

## المرحلة الحالية
تم إنشاء وتوثيق المواد اللازمة لنشر التطبيق في المتاجر:
1. **وثيقة STORE_LISTING.md**:
   - اسم التطبيق، العنوان القصير، والوصف الكامل باللغتين العربية والإنجليزية.
   - تحديد الكلمات المفتاحية والفئات المناسبة.
   - تحديد وتوضيح سياسات الخصوصية والاشتراكات.
   - توفير ملاحظات شفافة للمراجعين (Reviewer Notes) لتسهيل القبول في المتاجر.
   - اقتراح أفكار للقطات الشاشة والنصوص المصاحبة لها.
   - صياغة وصف مقترح للفيديو الترويجي.
   - تحديد سياسة الإبلاغ عن المحتوى المسيء والتأكيد الصارم على سياسة المحتوى الشرعي وعدم اعتبار مخرجات الذكاء الاصطناعي بمثابة فتاوى.
"""

content = re.sub(r'# سراج \(Siraj\).*?## التقنية', new_header + '\n## التقنية', content, flags=re.DOTALL)

with open('PROJECT_CONTEXT.md', 'w', encoding='utf-8') as f:
    f.write(content)

# Update CHANGELOG.md
with open('CHANGELOG.md', 'r', encoding='utf-8') as f:
    changelog = f.read()

new_log = """## [Unreleased] - Store Listing Preparation (PROMPT 066)
### Added
- إنشاء وثيقة `STORE_LISTING.md` تحتوي على وصف التطبيق الكامل والقصير للمتاجر باللغتين العربية والإنجليزية.
- إعداد نصوص ومقترحات لقطات الشاشة والفيديو الترويجي بطريقة تعكس وظائف التطبيق الفعلية.
- صياغة سياسات المحتوى الشرعي، مبادئ الإبلاغ عن المحتوى، والتأكيد بوضوح على أن الذكاء الاصطناعي لا يمثل جهة إفتاء.
- تجهيز فقرات ملاحظات المراجعين (Reviewer Notes) وتوضيح كيفية عمل التطبيق والإشعارات والصلاحيات لتسهيل مراجعته من قبل Google Play و App Store.

"""
changelog = re.sub(r'## \[Unreleased\] - Localization', new_log + '## [Unreleased] - Localization', changelog)

with open('CHANGELOG.md', 'w', encoding='utf-8') as f:
    f.write(changelog)

# Update README.md
with open('README.md', 'r', encoding='utf-8') as f:
    readme = f.read()

if "مواد النشر والمتاجر" not in readme:
    readme = readme.replace("## حالة المشروع", "## مواد النشر والمتاجر\nيحتوي المشروع على وثيقة متكاملة `STORE_LISTING.md` تتضمن كافة المواد، النصوص، والسياسات اللازمة لنشر التطبيق على متجري Google Play و App Store مع التركيز على الشفافية وعدم التضليل.\n\n## حالة المشروع")

with open('README.md', 'w', encoding='utf-8') as f:
    f.write(readme)

