# إدارة الأسرار والمفاتيح ودورة حياتها (Secrets & Key Management Lifecycle)
**منصة سراج (Siraj Platform)**

---

## 1. الهوية والهدف
تحدد هذه الوثيقة السياسة الشاملة، والضوابط التقنية، ودورة الحياة الكاملة لكافة المفاتيح والرموز السرية وبيانات الاعتماد المستخدمة في منصة سراج، بما يضمن:
- عدم ظهور أي مفتاح أو سر في تطبيق العميل (Zero Secrets in Client APK).
- حظر وجود الأسرار في مستودعات Git أو السجلات (Zero Secrets in Git & Logs).
- إدارة مركزية صارمة عبر **Google Cloud Secret Manager** و **Cloud KMS**.
- فصل كامل ومعزول بين بيئات التطوير والاختبار والإنتاج (Environment Segregation).
- تطبيق مبدأ أقل صلاحية (Principle of Least Privilege) ونظام التدوير والإبطال الفوري.

---

## 2. تصنيف الأسرار والمفاتيح (Secrets Taxonomy)

| # | اسم السر / الفئة | المالك المسؤول | البيئة المعزولة | موقع التخزين المعتمد | دورة التدوير (Rotation) | آلية الاستخدام |
|---|---|---|---|---|---|---|
| 1 | **Gemini API Key** | فريق الذكاء الاصطناعي (AI Core Team) | Production / Staging / Dev | Google Cloud Secret Manager | كل 60 يوماً | عبر Cloud Functions Proxy فقط |
| 2 | **Image Provider Key** | فريق خدمات الوسائط (Media Infra Team) | Production / Staging / Dev | Google Cloud Secret Manager | كل 90 يوماً | عبر Backend Media Proxy |
| 3 | **Audio Provider Key (TTS)** | فريق خدمات الوسائط (Media Infra Team) | Production / Staging / Dev | Google Cloud Secret Manager | كل 90 يوماً | عبر Backend Audio Proxy |
| 4 | **Video Provider Key** | فريق خدمات الوسائط (Media Infra Team) | Production / Staging / Dev | Google Cloud Secret Manager | كل 90 يوماً | عبر Backend Transcoder |
| 5 | **Google Play Service Account** | فريق الفوترة والاشتراكات (Billing Ops) | Production / Staging | Google Cloud Secret Manager | كل 180 يوماً | التحقق الخادمي من الاشتراكات |
| 6 | **Apple Private Key (p8)** | فريق الفوترة والاشتراكات (Billing Ops) | Production / Staging | Google Cloud Secret Manager / KMS | كل 180 يوماً | التحقق الخادمي من StoreKit |
| 7 | **Firebase Admin Credentials** | فريق العمليات (DevOps / SecOps) | Production / Staging / Dev | Google Cloud Secret Manager | كل 90 يوماً | Cloud Run & Cloud Functions |
| 8 | **Webhook Signing Secrets** | فريق الفوترة والاشتراكات (Billing Ops) | Production / Staging | Google Cloud Secret Manager | كل 90 يوماً | التحقق من توقيع HMAC-SHA256 |
| 9 | **Release Signing Keystore** | فريق إطلاق التطبيقات (Mobile Release) | Production | Cloud Secret Manager / Cloud KMS | سنوي (365 يوماً) | توقيع حزم التطبيق في CI/CD |
| 10 | **Database Credentials** | فريق العمليات والبيانات (DevOps Team) | Production / Staging / Dev | Google Cloud Secret Manager | كل 60 يوماً | اتصالات خوادم قواعد البيانات |

---

## 3. المبادئ والقواعد غير القابلة للتفاوض (Non-Negotiable Rules)

1. **حظر تام للأسرار في تطبيق العميل (Zero Secrets in Client APK):**
   - يُحظر تضمين أي مفتاح API خارجي (Gemini, Media, Firebase Admin, Private Keys) داخل كود Android أو iOS أو `BuildConfig` الموجه للعميل.
   - كافة النداءات للذكاء الاصطناعي ومزودي الوسائط تمر حصرياً عبر **Backend Cloud Functions / Cloud Run** بعد مصادقة المستخدم والتحقق من اشتراكه ورصيده.

2. **عدم الاعتماد على الإخفاء البرمجي (No Security Through Obfuscation):**
   - لا يُعتبر تشفير السلاسل النصية (String Encryption/Obfuscation) أو استخدام ProGuard حلاً لحفظ الأسرار في العميل، لأن حزم APK يمكن تفكيكها بسهولة.

3. **حظر أسرار الإنتاج في البيئات المحلية والتطويرية:**
   - لكل بيئة (Development, Staging, Production) مشروع سحابي ومفاتيح اعتماد مستقلة تماماً.
   - يُمنع منعاً باتاً استخدام مفاتيح الإنتاج في أجهزة المطورين أو بيئات الاختبار.

4. **حظر رفع الأسرار إلى Git ومستودعات الكود:**
   - ملف `.env` و `.env.*` و `*.keystore` و `*.p12` و `*.p8` و `*service-account*.json` و `google-services.json` مستبعدة بالكامل في `.gitignore`.
   - ملف `.env.example` يحتوي فقط على أسماء المتغيرات بقيم افتراضية عامة (`placeholder_only`).
   - تفعيل فحص الأسرار التلقائي (Secret Scanning & Push Protection) لمنع أي Commit يحتوي على نمط سر حرج.

5. **منع الأسرار من الظهور في السجلات وتقارير الأخطاء (Zero Leak Logs):**
   - استخدام محرك التطهير `SanitizedLogger` لحجب أي رمز API، مفتاح خاص، رمز Bearer، أو كلمة مرور تلقائياً واستبدالها بـ `[REDACTED_SECRET]`.
   - حظر طباعة الأسرار في Logcat أو Firebase Crashlytics أو Screenshots.

---

## 4. دورة حياة السر (Secret Lifecycle Workflow)

### أ. الإنشاء والتسجيل (Creation & Provisioning)
1. إنشاء السر داخل مشروع Google Cloud Secret Manager المخصص للبيئة.
2. تقييد صلاحية الوصول (IAM Roles) لحساب الخدمة (Service Account) المخول فقط دون منح صلاحيات واسعة.
3. تسجيل البيانات الوصفية للسر (المالك، دورة التدوير، البصمة المشفرة `SHA-256`) في سجل حوكمة الأسرار.

### ب. التدوير الدوري المنظم (Periodic Rotation)
1. قبل حلول موعد التدوير بـ 7 أيام، يتم إرسال تنبيه آلي للفريق المالك للسر.
2. إنشاء نسخة جديدة (New Version) في Secret Manager مع الإبقاء على النسخة السابقة نشطة لفترة سماح (Grace Period) مدتها 24 ساعة لضمان استقرار الخوادم.
3. تحديث خدمات الـ Backend لاستخدام الإصدار الجديد.
4. تعطيل وحذف الإصدار القديم بعد التأكد من نجاح الانتقال.
5. توثيق عملية التدوير في سجل التدقيق (Audit Log) متضمناً الإصدار الجديد وتاريخ التنفيذ.

### ج. الإبطال الفوري والاستجابة للطوارئ (Instant Revocation & Incident Response)
عند الاشتباه بحدوث تسرب لسر أو اختراق نقطة اتصال:
1. **الاحتواء الفوري (Containment - < 5 دقائق):**
   - إبطال وتعطيل نسخة السر في Google Cloud Secret Manager فوراً.
   - تعطيل المفتاح من لوحة تحكم المزود الخارجي (Google Cloud Console / Apple Developer / Stripe).
2. **عزل الحادثة (Isolation):**
   - إيقاف العمليات السحابية التي تعتمد على المفتاح المسرب مؤقتاً لحين استبداله.
3. **التدوير الطارئ (Emergency Rotation):**
   - توليد مفتاح جديد مشفر وإيداعه في Secret Manager وتحديث Backend بنسخة Hotfix فورية.
4. **حصر الأثر والتحقيق (Blast Radius Assessment):**
   - مراجعة سجلات الوصول (Access Logs) خلال الـ 48 ساعة السابقة لتحديد ما إذا تم استغلال المفتاح.
5. **التوثيق وإغلاق الحادثة (Post-Mortem):**
   - فتح وتوثيق ملف الحادثة (`SecretLeakIncident`) وتحديد سبب التسرب والتدابير الوقائية.

---

## 5. إدارة وتأمين الـ Webhooks (Webhook Security)

- **خوارزمية التوقيع:** استخدام **HMAC-SHA256** لحساب توقيع كل إشعار مرسل من بوابات الدفع أو المتاجر.
- **مقارنة زمنية آمنة (Constant-Time Comparison):** استخدام `MessageDigest.isEqual` لمنع هجمات التوقيت (Timing Attacks).
- **حماية إعادة الإرسال (Replay Attack Defense):** التحقق من ترويسة الطابع الزمني (`Timestamp Header`) ورفض أي طلب يتجاوز فارقه الزمني 300 ثانية (5 دقائق).
- **مفتاح سر مستقل لكل نقطة نهاية (Endpoint-Specific Secret):** عزل أسرار Webhooks لكل بوابة وعدم مشاركة سر موحد.

---

## 6. قائمة تدقيق الأسرار قبل إصدار الإنتاج (Pre-Release Checklist)

قبل تصدير حزمة الإنتاج (Release APK / AAB):
1. [x] خلو حزمة العميل من أي مفاتيح API أو أسرار (Zero Secrets in Client).
2. [x] ربط كافة الخدمات بـ Google Cloud Secret Manager مع تفعيل قيود IAM.
3. [x] تأكيد خلو المستودع من أي مفاتيح مسربة عبر أداة `SecretScannerEngine`.
4. [x] سريان فترات التدوير الدوري لجميع الأسرار وعدم وجود أسرار متأخرة.
5. [x] تفعيل التوقيع الرقمي HMAC-SHA256 وحماية الإعادة لكافة مسارات Webhook.
6. [x] تفعيل مسجل السجلات الآمن `SanitizedLogger` لحجب أي أسرار في السجلات.

---

## 7. سجل المراجعة والاعتماد
- **المحرر:** مهندس الأمن والأنظمة السحابية - منصة سراج.
- **الحالة:** معتمد ونافذ برمجياً وتنظيمياً (PROMPT 090).
- **التوافق:** متوافق مع كافة معايير الأمان السحابي وسياسات متاجر التطبيقات.
