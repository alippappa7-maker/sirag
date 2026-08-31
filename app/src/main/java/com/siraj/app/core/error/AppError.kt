package com.siraj.app.core.error

import java.util.UUID

sealed class AppError(
    val title: String,
    val userMessage: String,
    val isRetryable: Boolean,
    val technicalDetails: String? = null,
    val referenceId: String =
        UUID
            .randomUUID()
            .toString()
            .take(8)
            .uppercase(),
) : Exception(userMessage) {
    class Network(
        message: String = "تعذر الاتصال بالشبكة. يرجى التحقق من اتصالك والمحاولة مرة أخرى.",
        details: String? = null,
    ) : AppError("خطأ في الاتصال", message, true, details)

    class Auth(
        message: String = "حدث خطأ في المصادقة. يرجى تسجيل الدخول مرة أخرى.",
        details: String? = null,
    ) : AppError("خطأ في المصادقة", message, false, details)

    class Permission(
        message: String = "لا تملك الصلاحيات الكافية لإجراء هذه العملية.",
        details: String? = null,
    ) : AppError("صلاحيات غير كافية", message, false, details)

    class Database(
        message: String = "تعذر قراءة أو حفظ البيانات. يرجى المحاولة لاحقاً.",
        details: String? = null,
    ) : AppError("خطأ في قاعدة البيانات", message, true, details)

    class Storage(
        message: String = "حدث خطأ أثناء التعامل مع الملفات.",
        details: String? = null,
    ) : AppError("خطأ في الملفات", message, true, details)

    class AiProvider(
        message: String = "الخدمة الذكية غير متوفرة حالياً. جرب مرة أخرى بعد قليل.",
        details: String? = null,
    ) : AppError("خطأ في الذكاء الاصطناعي", message, true, details)

    class Queue(
        message: String = "تأخرت معالجة الطلب في الطابور.",
        details: String? = null,
    ) : AppError("تأخير في المعالجة", message, true, details)

    class Payment(
        message: String = "فشلت عملية الدفع. يرجى المحاولة لاحقاً.",
        details: String? = null,
    ) : AppError("خطأ في الدفع", message, true, details)

    class LocalExecution(
        message: String = "حدث خطأ داخلي في التطبيق أثناء التشغيل.",
        details: String? = null,
    ) : AppError("خطأ داخلي", message, false, details)

    class Unknown(
        message: String = "حدث خطأ غير متوقع. يرجى المحاولة لاحقاً.",
        details: String? = null,
    ) : AppError("خطأ غير معروف", message, true, details)
}
