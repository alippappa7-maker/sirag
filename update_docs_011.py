import re

changelog_addition = """
## [PROMPT 011] - 2026-08-27 (نموذج بيانات المشاريع)
### أُضيف (Added)
- **نماذج البيانات الكاملة:** إنشاء `Project`, `ProjectMember`, `ProjectVersion`, `ProjectAsset`, `ProjectActivity` لتشمل جميع الحقول المطلوبة لبناء محرر الوسائط مستقبلاً.
- **حالات المشروع الجديدة:** تحديث الحالات لتتوافق مع نظام المعالجة (`DRAFT`, `PROCESSING`, `READY`, `EXPORTING`, `COMPLETED`, `FAILED`, `ARCHIVED`, `DELETED`).
- **تحديث مستودع المشاريع (`ProjectRepository`):**
  - دعم البحث (Search) والفرز (Sort) وتقسيم الصفحات (Pagination عبر `limit` حالياً).
  - إضافة واجهة لإنشاء وقراءة النسخ (Versioning).
  - تسجيل نشاطات المشروع (`ProjectActivity`) بشكل تلقائي عند الإنشاء أو التعديل (Auto-save) أو إنشاء نسخ.
- **تحديث قواعد `firestore.rules`:** حماية مجموعات `project_activities` و `project_versions`.
"""

with open('CHANGELOG.md', 'r') as f:
    content = f.read()

content = content.replace("# سجل تغييرات المشروع (Changelog)\n", "# سجل تغييرات المشروع (Changelog)\n" + changelog_addition)

with open('CHANGELOG.md', 'w') as f:
    f.write(content)

with open('PROJECT_CONTEXT.md', 'r') as f:
    content = f.read()

content = content.replace("010 (قسم الإعدادات والتفضيلات)", "011 (نموذج بيانات المشاريع)")
content = content.replace("- تم الانتهاء من الحزمة الأولى (الأساسيات، المصادقة، المشاريع، الإعدادات).", "- تم الانتهاء من تنفيذ PROMPT 011 (بناء النماذج وإنشاء طبقات التخزين لبيانات المشاريع).\n- تم الانتهاء من الحزمة الأولى (الأساسيات، المصادقة، المشاريع، الإعدادات).")
content = content.replace("الخطوة التالية:\nالبدء بحزمة البرومبتات التالية (استوديو المحتوى، قاعدة البيانات، معالجة الوسائط، إلخ).", "الخطوة التالية المرجوة:\nمتابعة تنفيذ حزمة استوديو المحتوى وإدارة المشاهد (PROMPT 012).")

with open('PROJECT_CONTEXT.md', 'w') as f:
    f.write(content)

with open('README.md', 'r') as f:
    content = f.read()

content = content.replace("## حالة المشروع الحالية (نهاية الحزمة الأولى)", "## حالة المشروع الحالية (بداية الحزمة الثانية)")
content = content.replace("5. **الإعدادات والتفضيلات**: تفضيلات متكاملة (المظهر، المواقيت، الجودة، لغة، الخ) مزامنة سحابياً مع `Firestore`.", "5. **الإعدادات والتفضيلات**: تفضيلات متكاملة (المظهر، المواقيت، الجودة، لغة، الخ) مزامنة سحابياً مع `Firestore`.\n6. **استوديو المحتوى (قيد التطوير)**: تم بناء نماذج البيانات المتقدمة للمشاريع (النسخ، الأنشطة، الأصول) وتجهيز المستودعات.")

with open('README.md', 'w') as f:
    f.write(content)

