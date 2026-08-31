import sys

with open('CHANGELOG.md', 'r', encoding='utf-8') as f:
    content = f.read()

new_entry = """# سجل التغييرات (Changelog)

## [1.1.7] - Visual Identity Phase 3 - Motion & Accessibility (PROMPT 54)
### Changed
- **تحسين الحركة (Motion) والتدقيق البصري:**
  - تطبيق حركة انتقالية مرنة (`animateContentSize` و `animateDpAsState` و `animateFloatAsState`) على حاويات التوهج (`SirajGlowContainer`) والبطاقات.
  - إضافة `Crossfade` سلس لتأثيرات تحميل وتبديل البيانات (قوائم الصوتيات والمشاريع).
  - الالتزام التام بتوظيف الحركة البسيطة فقط وتجنب الحركات المستمرة والمقلقة للعين، لتتناسب مع الطابع التقني الوقور.
  - الحفاظ على المنطق الوظيفي وعدم إضافة أية بيانات وهمية أو خدمات غير مكتملة.

## [1.1.6] - Visual Identity Phase 2 - Mihrab & Audio (PROMPT 53)"""

content = content.replace("# سجل التغييرات (Changelog)\n\n## [1.1.6] - Visual Identity Phase 2 - Mihrab & Audio (PROMPT 53)", new_entry)

with open('CHANGELOG.md', 'w', encoding='utf-8') as f:
    f.write(content)
