# سياسة الأمان (Security Policy)

## إدارة الأدوار والصلاحيات (Role-Based Access Control)
تم تطبيق نظام إدارة أدوار متكامل في تطبيق سراج لحماية المسارات والبيانات:

### الأدوار المتاحة:
- **USER (مستخدم عادي):** يمكنه قراءة المحتوى العام، إدارة ملفه الشخصي ومشاريعه الخاصة فقط.
- **CREATOR (منشئ محتوى):** يمكنه إنشاء المحتوى وإرساله للمراجعة.
- **REVIEWER (مُراجع):** يمكنه مراجعة المحتوى الذي تم إنشاؤه من قبل المبدعين.
- **ADMIN (مدير):** يملك صلاحيات الإدارة للبيانات العامة للمنصة ومعالجة البلاغات. لا يمكنه ترقية مستخدم إلى OWNER.
- **OWNER (المالك):** أعلى مستوى من الصلاحيات، يمتلك التحكم الكامل بالمنصة والإعدادات الحساسة وترقية الأدوار.

### سياسة الأمان في Firestore Rules:
- تم منع المستخدمين من قراءة بيانات مستخدمين آخرين إلا في الحالات المصرح بها للمدراء (Admins و Owners).
- لا يثق التطبيق إطلاقاً في قيم `role`, `credits`, `plan` المرسلة من العميل في طلبات التحديث. يتم رفض أي محاولة لتغييرها من قبل مستخدم عادي.
- العمليات الإدارية الحساسة تتطلب أن يكون المستخدم بصلاحية تعادل `ADMIN` أو `OWNER`.
- تتوفر القواعد التفصيلية في ملف `firestore.rules`.

## التعامل مع الأسرار (Secrets)
- يُحظر تماماً وضع أي مفاتيح API (مثل Gemini) داخل كود التطبيق (`frontend`).
- يجب إدارة الأسرار عن طريق Cloud Functions / Secret Manager.


## Workspace Security Model
The app implements a strict, role-based access control (RBAC) system for workspaces:
- **Roles**: `OWNER`, `MANAGER`, `EDITOR`, `REVIEWER`, `VIEWER`.
- **Enforcement**: Roles are validated in Firestore Security Rules (`hasWorkspaceRole`).
- **Data Isolation**: Projects are completely isolated within their `workspaceId`. A user cannot read a project unless they have at least a `VIEWER` role in its workspace.
- **Privilege Escalation Protection**: Firestore rules explicitly prevent a `MANAGER` from upgrading a user to `OWNER` or demoting an `OWNER`. Only the current `OWNER` can transfer ownership.
- **Audit Logs**: All sensitive workspace operations (invites, role changes, removals, ownership transfers) are logged to the `audit_logs` collection.

## مصفوفة الصلاحيات وقواعد الوصول (Access Control Matrix)

تم بناء حماية شاملة للبيانات باستخدام **Firebase Security Rules (Firestore & Storage)** و **Custom Claims**:

- **Custom Claims**: يُستخدم التوكن (`request.auth.token.role`) لتحديد أدوار الإدارة (`ADMIN`, `OWNER`). لا يُعتمد على وثائق Firestore لتحديد صلاحية المدير لتسريع العمليات وتأمينها.
- **App Check**: جميع مسارات قاعدة البيانات والملفات مفعلة افتراضياً لتلقي الطلبات من التطبيق الأصلي فقط.
- **تحديث الدور والرصيد**: يُمنع العميل تماماً من تعديل مفاتيح `role`, `credits`, `plan`, `balance`, `entitlements`.
- **الملفات (Cloud Storage)**: 
  - مسار `users/{userId}` و `exports/{userId}` محمي بالمالك أو المدير.
  - مسار `public` متاح للقراءة للجميع والكتابة للمدير.
  - لا توجد قاعدة `allow read, write: if true`.
- **المحتوى (Flashes & Audio)**:
  - المحتوى العام المعتمد (`status == 'APPROVED'` و `isPrivate == false`) متاح للقراءة.
  - التعديل محصور للمالك أو الإدارة.
- **السجلات (Audit Logs)**: القراءة والكتابة للإدارة فقط وممنوعة من التعديل (Immutable).
- **الإبلاغات (Reports)**: المالك الأصلي يكتبها فقط، وتقرؤها الإدارة فقط.

### Rate Limiting & Backend:
يتم تطبيق حدود الطلبات على Cloud Functions للحماية من طلبات الـ AI أو المدفوعات الوهمية.
