with open('app/src/main/java/com/siraj/app/features/subscription/presentation/SubscriptionScreen.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if '"• يتم تجديد' in line:
        lines[i] = '                                    "• يتم تجديد الاشتراك تلقائياً ما لم يتم الإلغاء قبل 24 ساعة من نهاية الفترة الحالية.\\n" +\n'
    if '"• سيتم الخصم' in line:
        lines[i] = '                                    "• سيتم الخصم من حسابك في متجر Play عند تأكيد الشراء.\\n" +\n'
    if '"• يمكنك إدارة' in line:
        lines[i] = '                                    "• يمكنك إدارة اشتراكك أو إلغاؤه في أي وقت من إعدادات حساب Google Play.\\n" +\n'
    if '"• الأسعار المعروضة' in line:
        lines[i] = '                                    "• الأسعار المعروضة قد تختلف حسب دولتك وتتضمن الضرائب المطبقة.",\n'

with open('app/src/main/java/com/siraj/app/features/subscription/presentation/SubscriptionScreen.kt', 'w', encoding='utf-8') as f:
    f.writelines(lines)
