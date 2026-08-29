package com.siraj.app.core.privacy

import android.content.Context
import com.siraj.app.domain.models.privacy.StoredDataCategory
import com.siraj.app.domain.models.privacy.UserDataExportPackage
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PrivacyManager {

    private val FORBIDDEN_EXPORT_KEYS = setOf(
        "password", "passwordHash", "token", "rawPurchaseToken", "purchaseToken",
        "apiKey", "apiSecret", "privateKey", "secretKey", "authCredential",
        "refreshToken", "accessToken", "creditCardNumber", "cvv"
    )

    fun calculateDirectorySizeBytes(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var size = 0L
        val files = dir.listFiles() ?: return 0L
        for (file in files) {
            size += if (file.isDirectory) {
                calculateDirectorySizeBytes(file)
            } else {
                file.length()
            }
        }
        return size
    }

    fun clearDirectory(dir: File?): Boolean {
        if (dir == null || !dir.exists()) return true
        var success = true
        val files = dir.listFiles() ?: return true
        for (file in files) {
            if (file.isDirectory) {
                success = success && clearDirectory(file)
            }
            success = success && file.delete()
        }
        return success
    }

    fun sanitizeDataMap(map: Map<String, Any?>): Map<String, Any?> {
        val sanitized = mutableMapOf<String, Any?>()
        for ((k, v) in map) {
            if (FORBIDDEN_EXPORT_KEYS.any { forbidden -> k.contains(forbidden, ignoreCase = true) }) {
                continue // Strip forbidden sensitive keys completely
            }
            when (v) {
                is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    sanitized[k] = sanitizeDataMap(v as Map<String, Any?>)
                }
                is List<*> -> {
                    sanitized[k] = v.map { item ->
                        if (item is Map<*, *>) {
                            @Suppress("UNCHECKED_CAST")
                            sanitizeDataMap(item as Map<String, Any?>)
                        } else item
                    }
                }
                else -> sanitized[k] = v
            }
        }
        return sanitized
    }

    fun calculateSha256(content: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(content.toByteArray(Charsets.UTF_8))
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            "checksum_error"
        }
    }

    fun buildExportJsonString(exportPackage: UserDataExportPackage): String {
        val root = JSONObject()
        root.put("exportId", exportPackage.exportId)
        root.put("userId", exportPackage.userId)
        root.put("exportTimestamp", exportPackage.exportTimestamp)
        root.put("exportDateFormatted", exportPackage.exportDateFormatted)
        root.put("legalNotice", exportPackage.legalNotice)
        root.put("sha256Checksum", exportPackage.sha256Checksum)

        // Account Info
        root.put("accountInfo", JSONObject(sanitizeDataMap(exportPackage.accountInfo)))

        // Projects
        val projectsArray = JSONArray()
        exportPackage.projects.forEach { project ->
            projectsArray.put(JSONObject(sanitizeDataMap(project)))
        }
        root.put("projects", projectsArray)

        // Activity History
        val historyArray = JSONArray()
        exportPackage.activityHistory.forEach { history ->
            historyArray.put(JSONObject(sanitizeDataMap(history)))
        }
        root.put("activityHistory", historyArray)

        // Preferences
        root.put("preferences", JSONObject(sanitizeDataMap(exportPackage.preferences)))

        // Invoices summary
        val invoicesArray = JSONArray()
        exportPackage.anonymizedInvoicesSummary.forEach { invoice ->
            invoicesArray.put(JSONObject(sanitizeDataMap(invoice)))
        }
        root.put("anonymizedInvoicesSummary", invoicesArray)

        return root.toString(2)
    }

    fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes بايت"
        val kb = bytes / 1024.0
        if (kb < 1024) return "%.1f كيلوبايت".format(Locale.ENGLISH, kb)
        val mb = kb / 1024.0
        if (mb < 1024) return "%.1f ميجابايت".format(Locale.ENGLISH, mb)
        val gb = mb / 1024.0
        return "%.2f جيجابايت".format(Locale.ENGLISH, gb)
    }

    fun formatDate(timestamp: Long): String {
        if (timestamp <= 0) return "غير محدد"
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun getStandardRetentionPolicies(): List<StoredDataCategory> {
        return listOf(
            StoredDataCategory(
                id = "account_profile",
                title = "بيانات الحساب الشخصي والملف التعريفي",
                description = "الاسم والبريد الإلكتروني والصورة الرمزية ودور المستخدم وتفضيلات التطبيق.",
                storageLocation = "قاعدة بيانات Firestore (مجموعة users) و Firebase Auth المشفرة.",
                retentionPolicy = "يتم الاحتفاظ بها طوال فترة نشاط الحساب، وتُمسح بالكامل عند إتمام طلب حذف الحساب.",
                isPersonal = true,
                isLegalRequired = false
            ),
            StoredDataCategory(
                id = "projects_content",
                title = "المشاريع ومسودات الفيديوهات والمشاهد",
                description = "نصوص السيناريوهات والمشاهد وتوليدات الذكاء الاصطناعي والمصادر الشرعية المربوطة.",
                storageLocation = "قاعدة بيانات Firestore (مجموعة projects) وملفات الوسائط في Cloud Storage.",
                retentionPolicy = "حتى يقرر المستخدم حذف المشروع يدوياً، أو تُمسح فوراً عند اكتمال حذف الحساب.",
                isPersonal = true,
                isLegalRequired = false
            ),
            StoredDataCategory(
                id = "activity_history",
                title = "سجل المشاهدة والاستماع والقرآن والمحراب",
                description = "مواضع التوقف في التلاوات والفيديوهات، قائمة المشاهدة لاحقاً، وحالة الإكمال.",
                storageLocation = "تخزين محلي مؤقت مشفر وقاعدة بيانات Firestore المشتركة (إذا تم تفعيل المزامنة).",
                retentionPolicy = "يمكن للمستخدم مسحها فوراً بضغطة زر، أو تُحذف تلقائياً حسب سياسة الاحتفاظ (30 / 90 / 365 يوماً).",
                isPersonal = true,
                isLegalRequired = false
            ),
            StoredDataCategory(
                id = "cached_downloads",
                title = "الملفات المحملة والمؤقتة (Cache & Downloads)",
                description = "المقاطع الصوتية ومقاطع الفيديو المحفوظة للاستخدام في وضع عدم الاتصال وملفات التخزين المؤقت للصور.",
                storageLocation = "ذاكرة الجهاز المحلية فقط (App Private Storage).",
                retentionPolicy = "يتم التحكم بها محلياً ويمكن تفريغها بضغطة زر في أي وقت دون التأثير على البيانات السحابية.",
                isPersonal = false,
                isLegalRequired = false
            ),
            StoredDataCategory(
                id = "location_data",
                title = "بيانات الموقع الجغرافي والقبلة ومواقيت الصلاة",
                description = "اسم المدينة أو إحداثيات تقريبية تُستخدم محلياً فقط لحساب أوقات الصلاة واتجاه القبلة في المحراب.",
                storageLocation = "محلياً على جهاز المستخدم (لا يتم حفظ إحداثيات دقيقة في الخوادم إطلاقاً).",
                retentionPolicy = "لا يتم الاحتفاظ بالموقع الدقيق، ولا تُرسل الإحداثيات إلى أي جهة خارجية أو إعلانية.",
                isPersonal = true,
                isLegalRequired = false
            ),
            StoredDataCategory(
                id = "analytics_telemetry",
                title = "تحليلات الأداء واستخدام الواجهة (اختياري)",
                description = "بيانات مجهولة الهوية لتفاعل الشاشات ومعدل استجابة الميزات لتحسين تجربة المستخدم.",
                storageLocation = "Firebase Analytics (مع تعطيل جمع معرّفات الإعلانات وتطهير كافة المدخلات).",
                retentionPolicy = "فترة احتفاظ 90 يوماً فقط بصيغة إحصائية مجمعة، وتتوقف فوراً عند قيام المستخدم بتعطيلها من الإعدادات.",
                isPersonal = false,
                isLegalRequired = false
            ),
            StoredDataCategory(
                id = "financial_records",
                title = "سجلات الفواتير والاشتراكات القانونية",
                description = "ملخص تاريخ الاشتراكات، خطة الباقة، وتواريخ التجديد مع معرّف فاتورة مشفر.",
                storageLocation = "سجلات الفوترة المشفرة في Firestore و Google Play Billing.",
                retentionPolicy = "فترة احتفاظ قانونية (5 سنوات) وفق قوانين الضرائب والامتثال المالي والتجارة الإلكترونية، وتُحفظ بصيغة مجهولة الهوية (Anonymized) بعد حذف الحساب.",
                isPersonal = false,
                isLegalRequired = true
            )
        )
    }
}
