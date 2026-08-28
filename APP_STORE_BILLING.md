# اشتراكات App Store والتحقق الخادمي (App Store Billing & Server-Side Validation)

تم تصميم بنية تطبيق سراج (بواجهات مشتركة) لدعم اشتراكات أبل عبر **StoreKit 2**. بما أن بيئة التطوير الحالية تدعم نظام Android، تم توثيق قواعد وتدفق الدفع الخاص بـ iOS ليتم تطبيقه بمجرد تفعيل بيئة الـ KMP وتوفر أجهزة macOS/Xcode.

## دورة الشراء الموثوقة (StoreKit 2)
1. يقوم `AppStoreBillingManager` بالاتصال بـ StoreKit لجلب تفاصيل الباقات `Product`.
2. عند شراء الخطة عبر `Product.purchase()`.
3. عند نجاح الشراء يعيد StoreKit **Signed Transaction** (أو JWS - JSON Web Signature).
4. **تأكيد الشراء:** يمنع منح صلاحية الوصول محلياً (لا تمنح الـ entitlement من العميل). بدلاً من ذلك، يُرسل الـ JWS Transaction بالكامل (بمثابة purchaseToken) إلى الخادم عبر دالة `verifyPurchase`.
5. **الخادم (Backend):** 
   - يتواصل مع **App Store Server API**.
   - يتأكد من صحة التوقيع عبر مفاتيح أبل وتطابق الـ `bundleId`.
   - يقرأ `originalTransactionId` للربط المستقبلي لعمليات التجديد، والتأكد من عدم ربط الاشتراك بحسابين مختلفين.
   - يحدّث قاعدة بيانات `Firestore` بالاشتراك الجديد (`Subscription` & `Entitlement`).
   - يُرجع نجاحاً للعميل.
6. بمجرد نجاح التحقق، يقوم تطبيق الـ iOS بالمناداة على `transaction.finish()` ليعترف للشراء أمام أبل.

## إشعارات المطورين اللحظية (App Store Server Notifications V2)
- يتم إعداد رابط Webhook في **App Store Connect** يستقبل POST Requests من أبل.
- تستقبل الـ Cloud Function الإشعارات من أبل (SUBSCRIBED, DID_RENEW, EXPIRED, REFUND, GRACE_PERIOD_EXPIRED).
- تتكفل هذه الدالة بتحديث `expiresAt` و `status` للاشتراك في قاعدة البيانات الخادمية (Firestore).
- **ملاحظة:** بناءً على إرشادات Apple، يجب إعداد Endpoint منفصل لبيئة הـ Sandbox و Endpoint لبيئة الـ Production.

## الأمان
- **لا تستخدم بيانات Sandbox في الإنتاج.**
- **التوقيع الرقمي:** لا تعتمد على تحقق العميل المحلي (Local Verification). التحقق يجب أن يكون خادمياً عبر مكتبة للتعامل مع JWS.
- **التجديد والانتهاء:** الخادم هو المصدر الوحيد لحالة الاشتراك (Source of Truth)، ولا يُعترف بأي صلاحية محلية منتهية في الخادم.
- **المفاتيح الخاصة:** جميع مفاتيح الوصول (Issuer ID, Key ID, Private Key) محفوضة في Secret Manager أو `.env` الخاصة بالخادم، وممنوع إرسالها أو دمجها في الكود المصدري للتطبيق.

## الترقية والتخفيض (Upgrade and Downgrade)
- تتم معالجتها تلقائياً بواسطة أبل من خلال المجموعات (Subscription Groups).
- عند حدوث الترقية (Upgrade)، ترسل أبل إشعاراً للحالة اللحظية بإنهاء الاشتراك الحالي بـ `UPGRADE` وبدء اشتراك جديد، ويقوم الخادم بتعديل الامتيازات فوراً.
- عند حدوث التخفيض (Downgrade)، تطبق أبل التخفيض في نهاية الفترة الحالية ويصل إشعار `DID_CHANGE_RENEWAL_PREF` ثم يتغير في نهاية المدة.

## استعادة المشتريات (Restore Purchases)
- يقوم العميل بمزامنة أحدث المعاملات (Active Transactions) واستدعاء `Transaction.currentEntitlements`.
- يُرسل أحدث JWS إلى الخادم، ويقوم الخادم بربط `originalTransactionId` بحساب المستخدم إذا كان غير مرتبط بحساب آخر.
