# اشتراكات Google Play والتحقق الخادمي (Google Play Billing & Server-Side Validation)

تم بناء تكامل مع مكتبة `BillingClient` الخاصة بـ Google Play لإتاحة الاشتراكات لمستخدمي تطبيق سراج على نظام أندرويد.

## دورة الشراء الموثوقة
1. يقوم `GooglePlayBillingManager` بالاتصال بالمتجر وجلب تفاصيل الباقات `ProductDetails`.
2. يتم عرض الباقات للمستخدم.
3. عند الشراء، يتم عرض واجهة المتجر وعند نجاحها يعيد المتجر `Purchase`.
4. **تأكيد الشراء (الأهم):** لا يتم منح الصلاحية ولا يتم الاعتراف بالاشتراك محليًا. بدلاً من ذلك، يُرسل الـ `purchaseToken` و `productId` إلى الخادم (`verifyPurchase`).
5. **الخادم (Backend):** 
   - يتواصل مع `Google Play Developer API` (`purchases.subscriptions.get`).
   - يتأكد من صحة الشراء (`paymentState == 1`).
   - يتأكد أن التوكن لم يُستخدم مسبقاً (لحماية الاشتراك من التكرار لحسابات أخرى).
   - يحدّث قاعدة بيانات `Firestore` بالاشتراك الجديد (`Subscription` & `Entitlement`).
   - يُرجع نجاحاً للعميل.
6. بمجرد نجاح التحقق، ينادي العميل `acknowledgePurchase` لكي لا تقوم جوجل برد المبلغ.

## إشعارات المطورين اللحظية (RTDN)
لضمان التزامن الدائم (Renewal, Cancellation, Refund, Expiration):
- يجب إعداد **Real-time Developer Notifications** في `Google Play Console`.
- ربط إشعارات Pub/Sub بـ Cloud Function مثل `handlePlayStoreNotification`.
- تتكفل الدالة بتحديث تاريخ الانتهاء أو إيقاف الاشتراك فورياً إذا توقف الدفع.

## الأمان
- **الرموز الخام:** لا يُسمح بتسجيل الرموز الشرائية (Purchase Tokens) بالصيغة الخام في أنظمة الـ Logs. تُخزن فقط بصيغة Hashed إن تطلب الأمر للاستعلام.
- **التجديد والانتهاء:** الخادم هو المصدر الوحيد لحالة الانتهاء، ولا يتم الاعتماد على بيانات الذاكرة المحلية للجهاز.
