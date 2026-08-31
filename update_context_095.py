import re

with open('PROJECT_CONTEXT.md', 'r', encoding='utf-8') as f:
    content = f.read()

# Replace the top section
new_top = """# سراج (Siraj)

## حالة التنفيذ
مكتمل بنجاح للمشروع بالكامل - تم إجراء المراجعة النهائية (Final Review) القانونية والشرعية والتقنية.

## آخر برومبت منفذ
آخر برومبت منفذ: PROMPT 095
المرحلة: المراجعة النهائية
الحالة: CONDITIONAL_GO
الموانع: توقيع المستشار القانوني على السياسات، اعتماد اللجنة الشرعية للمصادر، تفعيل App Check و Production Keys، موافقة متاجر التطبيقات.
المخاطر المقبولة: احتمالية خطأ (Hallucination) في التوليد الآلي (مشمولة بنظام المراجعة البشرية وإجبارية المصادر).
الاعتمادات المطلوبة: توقيع المالك (Owner)، المستشار القانوني، اللجنة الشرعية.
الخطوة التالية: إطلاق التطبيق بعد استيفاء الموافقات والاعتمادات الرسمية.

## التقنية"""

content = re.sub(r'# سراج \(Siraj\).*?## التقنية', new_top, content, flags=re.DOTALL)

with open('PROJECT_CONTEXT.md', 'w', encoding='utf-8') as f:
    f.write(content)
