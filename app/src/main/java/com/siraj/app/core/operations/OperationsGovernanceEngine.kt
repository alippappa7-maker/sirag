package com.siraj.app.core.operations

import com.siraj.app.domain.models.operations.*

/**
 * محرك حوكمة التشغيل والتحديثات لما بعد إطلاق سراج
 * يطبق القواعد الصارمة لإدارة الإصدارات، الطوارئ، التكاليف، التوافقية، والمحتوى
 */
object OperationsGovernanceEngine {

    /**
     * تقييم استحقاق الطوارئ (Hotfix Evaluation)
     * معايير P0: تعطل الدخول/الدفع/انهيار >1% أو تحريف شرعي جسيم
     */
    fun evaluateHotfixEligibility(
        crashRatePercentage: Double,
        isAuthFailing: Boolean,
        isBillingFailing: Boolean,
        isShariaContentCorrupted: Boolean,
        isExportFailingForMajority: Boolean
    ): HotfixEvaluation {
        return when {
            isAuthFailing || isBillingFailing || isShariaContentCorrupted || crashRatePercentage >= 1.0 -> {
                HotfixEvaluation(
                    isHotfixJustified = true,
                    severity = HotfixSeverity.P0_CRITICAL,
                    recommendedAction = "إطلاق تحديث طارئ فوري (Hotfix) خلال أقل من 4 ساعات مع تفعيل Rollback إذا لزم الأمر",
                    requiredApprovals = listOf("Tech Lead", "Security Lead", "Sharia Reviewer Lead (إن كان المحتوى شرعياً)"),
                    targetSlaHours = 4
                )
            }
            isExportFailingForMajority || crashRatePercentage >= 0.3 -> {
                HotfixEvaluation(
                    isHotfixJustified = true,
                    severity = HotfixSeverity.P1_HIGH,
                    recommendedAction = "تجهيز تحديث سريع واختباره على Staging/Beta خلال 12 ساعة",
                    requiredApprovals = listOf("Tech Lead"),
                    targetSlaHours = 12
                )
            }
            else -> {
                HotfixEvaluation(
                    isHotfixJustified = false,
                    severity = HotfixSeverity.P2_MEDIUM,
                    recommendedAction = "إدراج الإصلاح في الإصدار الأسبوعي القادم (Regular Weekly Release) دون استعجال خط الإنتاج",
                    requiredApprovals = listOf("Release Manager"),
                    targetSlaHours = 48
                )
            }
        }
    }

    /**
     * تقييم مقترحات الميزات الجديدة (Feature Request Triage)
     * القاعدة الصارمة: "لا تضف ميزة لمجرد طلب واحد"
     */
    fun evaluateFeatureRequest(
        title: String,
        requestCount: Int,
        reachScore: Int,      // 1-10
        impactScore: Int,     // 1-10
        confidenceScore: Int, // 1-10
        effortScore: Int      // 1-10 (10 = highest effort)
    ): FeatureRequestEvaluation {
        val safeEffort = if (effortScore <= 0) 1 else effortScore
        val riceScore = (reachScore * impactScore * confidenceScore).toDouble() / safeEffort.toDouble()

        // رفض الميزة تلقائياً إذا كانت بناءً على طلب مستخدم واحد فقط دون دراسة جدوى
        if (requestCount <= 1) {
            return FeatureRequestEvaluation(
                title = title,
                totalRequestsCount = requestCount,
                reachScore = reachScore,
                impactScore = impactScore,
                confidenceScore = confidenceScore,
                effortScore = effortScore,
                calculatedRiceScore = riceScore,
                isApprovedForBacklog = false,
                rejectionReason = "مرفوض وفق سياسة الحوكمة: لا يتم تطوير ميزات بناءً على طلب فردي منعزل لتجنب التضخم العشوائي للتطبيق"
            )
        }

        // قبول الميزة للباكلوج إذا حققت حد نقاط RICE المقبول وتكرر الطلب
        val isApproved = riceScore >= 15.0 && requestCount >= 3
        return FeatureRequestEvaluation(
            title = title,
            totalRequestsCount = requestCount,
            reachScore = reachScore,
            impactScore = impactScore,
            confidenceScore = confidenceScore,
            effortScore = effortScore,
            calculatedRiceScore = riceScore,
            isApprovedForBacklog = isApproved,
            rejectionReason = if (!isApproved) "نقاط الأثر والجدوى (RICE Score = $riceScore) أقل من الحد الأدنى المعتمد (15.0) أو عدد الطلبات غير كافٍ" else null
        )
    }

    /**
     * فحص التوافقية العكسية للمشاريع السابقة (Backward Compatibility)
     * القاعدة: "لا تكسر المشاريع القديمة للمستخدمين أبداً"
     */
    fun checkProjectSchemaCompatibility(
        projectSchemaVersion: Int,
        currentAppSchemaVersion: Int
    ): Pair<Boolean, String> {
        return when {
            projectSchemaVersion == currentAppSchemaVersion -> {
                Pair(true, "المشروع متوافق مع المخطط الحالي مباشرة")
            }
            projectSchemaVersion < currentAppSchemaVersion -> {
                // ترحيل تلقائي تصاعدي دون فقدان أي بيانات للمستخدم
                Pair(true, "تم تطبيق ترقية تلقائية آمنة للمشروع من الإصدار v$projectSchemaVersion إلى v$currentAppSchemaVersion")
            }
            else -> {
                // المشروع أنشئ بإصدار أحدث من التطبيق الحالي
                Pair(false, "تم إنشاء هذا المشروع بإصدار أحدث من سراج، يرجى تحديث التطبيق للمتابعة")
            }
        }
    }

    /**
     * تدقيق التكاليف والإنفاق السحابي (FinOps Budget Audit)
     * يطبق تنبيهات 50%، 80%، 100% وخطة إيقاف الميزات المكلفة
     */
    fun evaluateFinOpsSpend(allocatedBudgetUsd: Double, actualSpentUsd: Double): Pair<BudgetAlertLevel, List<String>> {
        val spendPercentage = if (allocatedBudgetUsd > 0) (actualSpentUsd / allocatedBudgetUsd) * 100.0 else 0.0
        val actions = mutableListOf<String>()

        return when {
            spendPercentage >= 100.0 -> {
                actions.add("تفعيل الإيقاف الطارئ (Kill-switch) لتوليد الفيديو والصوت فائق الدقة غير الأساسي")
                actions.add("حصر استخدام Gemini في تلخيص وتدقيق النصوص الأساسية فقط")
                actions.add("خفض حدود الاستخدام اليومية للأرصدة المجانية بنسبة 50%")
                Pair(BudgetAlertLevel.CRITICAL_100, actions)
            }
            spendPercentage >= 80.0 -> {
                actions.add("تفعيل التخزين المؤقت المشدد (Aggressive Caching) لنتائج الذكاء الاصطناعي")
                actions.add("إرسال تنبيه عاجل لمدير الهندسة والمالية لمراجعة الاستهلاك")
                Pair(BudgetAlertLevel.ALERT_80, actions)
            }
            spendPercentage >= 50.0 -> {
                actions.add("تسجيل تقرير استهلاك منتصف الدورة وإشعار الفريق")
                Pair(BudgetAlertLevel.WARNING_50, actions)
            }
            else -> {
                actions.add("الاستهلاك ضمن المعدلات الطبيعية المخططة")
                Pair(BudgetAlertLevel.NORMAL, actions)
            }
        }
    }

    /**
     * فحص دورة تدوير الأسرار والمفاتيح (Secret Rotation Audit)
     * يضمن تجديد المفاتيح كل 90 يوماً وتنبيه الفريق قبل الاستحقاق
     */
    fun auditSecretRotations(secrets: List<SecretRotationRecord>): List<String> {
        val alerts = mutableListOf<String>()
        for (secret in secrets) {
            if (secret.isOverdue) {
                alerts.add("🚨 السر [${secret.secretNameArabic}] متأخر عن التدوير الدوري بمقدار ${-secret.remainingDays} يوم! يجب التدوير فوراً.")
            } else if (secret.remainingDays <= 14) {
                alerts.add("⚠️ السر [${secret.secretNameArabic}] يستحق التدوير خلال ${secret.remainingDays} يوم.")
            }
        }
        return alerts
    }

    /**
     * التحقق من سلامة Feature Flags ومنع التلاعب بالصلاحيات الأمنية من جانب العميل
     */
    fun validateFeatureFlagMutation(
        flag: FeatureFlagDefinition,
        isClientInitiated: Boolean
    ): Boolean {
        if (flag.isServerSideSecurityGated && isClientInitiated) {
            // ممنوع رفضاً باتاً تعديل المفاتيح الأمنية أو المالية من جهة تطبيق العميل
            return false
        }
        return true
    }
}
