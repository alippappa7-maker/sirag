import re

with open('README.md', 'r', encoding='utf-8') as f:
    content = f.read()

new_item = "19. **نموذج الاشتراكات والخطط**: نماذج قابلة للتوسع للاشتراكات والأرصدة والامتيازات (Entitlements) مع فصل كامل وتأسيس للتحقق الخادمي الآمن."

content = content.replace(
    "18. **الحماية وقواعد الوصول**: تأمين مركزي لـ Firestore و Storage باستخدام Firebase Security Rules و Custom Claims، مع حماية مسارات الدفع والتعديل من العميل.",
    "18. **الحماية وقواعد الوصول**: تأمين مركزي لـ Firestore و Storage باستخدام Firebase Security Rules و Custom Claims، مع حماية مسارات الدفع والتعديل من العميل.\n" + new_item
)

with open('README.md', 'w', encoding='utf-8') as f:
    f.write(content)
