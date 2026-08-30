package com.siraj.app.features.operations

import com.siraj.app.core.operations.OperationsGovernanceEngine
import com.siraj.app.domain.models.operations.*
import org.junit.Assert.*
import org.junit.Test

class OperationsGovernanceTest {

    @Test
    fun testHotfixEligibility_p0CriticalWhenAuthFails() {
        val evaluation = OperationsGovernanceEngine.evaluateHotfixEligibility(
            crashRatePercentage = 0.05,
            isAuthFailing = true,
            isBillingFailing = false,
            isShariaContentCorrupted = false,
            isExportFailingForMajority = false
        )

        assertTrue(evaluation.isHotfixJustified)
        assertEquals(HotfixSeverity.P0_CRITICAL, evaluation.severity)
        assertEquals(4, evaluation.targetSlaHours)
    }

    @Test
    fun testHotfixEligibility_p0CriticalWhenHighCrashRate() {
        val evaluation = OperationsGovernanceEngine.evaluateHotfixEligibility(
            crashRatePercentage = 1.4,
            isAuthFailing = false,
            isBillingFailing = false,
            isShariaContentCorrupted = false,
            isExportFailingForMajority = false
        )

        assertTrue(evaluation.isHotfixJustified)
        assertEquals(HotfixSeverity.P0_CRITICAL, evaluation.severity)
    }

    @Test
    fun testHotfixEligibility_p1WhenExportFails() {
        val evaluation = OperationsGovernanceEngine.evaluateHotfixEligibility(
            crashRatePercentage = 0.1,
            isAuthFailing = false,
            isBillingFailing = false,
            isShariaContentCorrupted = false,
            isExportFailingForMajority = true
        )

        assertTrue(evaluation.isHotfixJustified)
        assertEquals(HotfixSeverity.P1_HIGH, evaluation.severity)
        assertEquals(12, evaluation.targetSlaHours)
    }

    @Test
    fun testHotfixEligibility_p2WhenMinorIssue() {
        val evaluation = OperationsGovernanceEngine.evaluateHotfixEligibility(
            crashRatePercentage = 0.05,
            isAuthFailing = false,
            isBillingFailing = false,
            isShariaContentCorrupted = false,
            isExportFailingForMajority = false
        )

        assertFalse(evaluation.isHotfixJustified)
        assertEquals(HotfixSeverity.P2_MEDIUM, evaluation.severity)
        assertEquals(48, evaluation.targetSlaHours)
    }

    @Test
    fun testFeatureRequestRejection_whenSingleUserRequest() {
        // اختبار القاعدة: "لا تضف ميزة لمجرد طلب واحد"
        val evaluation = OperationsGovernanceEngine.evaluateFeatureRequest(
            title = "إضافة فلتر صوتي خاص بألعاب الفيديو",
            requestCount = 1,
            reachScore = 3,
            impactScore = 8,
            confidenceScore = 8,
            effortScore = 4
        )

        assertFalse(evaluation.isApprovedForBacklog)
        assertNotNull(evaluation.rejectionReason)
        assertTrue(evaluation.rejectionReason!!.contains("طلب فردي"))
    }

    @Test
    fun testFeatureRequestApproval_whenMultiUserHighRiceScore() {
        val evaluation = OperationsGovernanceEngine.evaluateFeatureRequest(
            title = "تصدير الفيديو بجودة 1080p بمعدل إطارات 60fps",
            requestCount = 28,
            reachScore = 8,
            impactScore = 8,
            confidenceScore = 9,
            effortScore = 3
        )

        assertTrue(evaluation.isApprovedForBacklog)
        assertNull(evaluation.rejectionReason)
        assertTrue(evaluation.calculatedRiceScore > 100.0)
    }

    @Test
    fun testBackwardCompatibility_olderSchemaMigratesSafely() {
        // اختبار القاعدة: "لا تكسر المشاريع القديمة"
        val (isCompatible, message) = OperationsGovernanceEngine.checkProjectSchemaCompatibility(
            projectSchemaVersion = 1,
            currentAppSchemaVersion = 3
        )

        assertTrue(isCompatible)
        assertTrue(message.contains("ترقية تلقائية آمنة"))
    }

    @Test
    fun testBackwardCompatibility_futureSchemaRequiresUpdate() {
        val (isCompatible, message) = OperationsGovernanceEngine.checkProjectSchemaCompatibility(
            projectSchemaVersion = 4,
            currentAppSchemaVersion = 3
        )

        assertFalse(isCompatible)
        assertTrue(message.contains("تحديث التطبيق"))
    }

    @Test
    fun testFinOpsBudgetEvaluation_triggers100PercentKillSwitch() {
        val (alertLevel, actions) = OperationsGovernanceEngine.evaluateFinOpsSpend(
            allocatedBudgetUsd = 1000.0,
            actualSpentUsd = 1050.0
        )

        assertEquals(BudgetAlertLevel.CRITICAL_100, alertLevel)
        assertTrue(actions.any { it.contains("Kill-switch") || it.contains("الإيقاف الطارئ") })
    }

    @Test
    fun testFinOpsBudgetEvaluation_triggers80PercentWarning() {
        val (alertLevel, actions) = OperationsGovernanceEngine.evaluateFinOpsSpend(
            allocatedBudgetUsd = 1000.0,
            actualSpentUsd = 850.0
        )

        assertEquals(BudgetAlertLevel.ALERT_80, alertLevel)
        assertTrue(actions.any { it.contains("Caching") || it.contains("التخزين المؤقت") })
    }

    @Test
    fun testSecretRotationAudit_detectsOverdueKeys() {
        val currentTime = System.currentTimeMillis()
        val overdueSecret = SecretRotationRecord(
            secretId = "sec_gemini_server_proxy",
            secretNameArabic = "مفتاح خادم بروكسي Gemini السحابي",
            targetService = "Cloud Run Secret Manager",
            lastRotatedTimestamp = currentTime - (100L * 24 * 60 * 60 * 1000), // 100 days ago (>90d)
            rotationIntervalDays = 90
        )

        val freshSecret = SecretRotationRecord(
            secretId = "sec_firebase_admin",
            secretNameArabic = "شهادة Firebase Admin SDK",
            targetService = "Cloud Functions",
            lastRotatedTimestamp = currentTime - (10L * 24 * 60 * 60 * 1000), // 10 days ago
            rotationIntervalDays = 90
        )

        val alerts = OperationsGovernanceEngine.auditSecretRotations(listOf(overdueSecret, freshSecret))

        assertEquals(1, alerts.size)
        assertTrue(alerts.first().contains("متأخر عن التدوير الدوري"))
    }

    @Test
    fun testSecurityFlagMutation_blocksClientSideModification() {
        val securityFlag = FeatureFlagDefinition(
            flagKey = "flag_enforce_app_check",
            nameArabic = "إلزامية App Check لحماية الخوادم",
            defaultValue = true,
            isServerSideSecurityGated = true,
            rolloutPercentage = 100,
            expirationDate = "2027-01-01",
            description = "مفتاح أمان خادمي لمنع الطلبات غير الموثوقة"
        )

        val isClientMutationAllowed = OperationsGovernanceEngine.validateFeatureFlagMutation(
            flag = securityFlag,
            isClientInitiated = true
        )

        assertFalse(isClientMutationAllowed)
    }

    @Test
    fun testVersionCompatibility_forcedUpdatePolicy() {
        val policy = VersionCompatibilityPolicy(
            currentProductionVersion = "1.2.0",
            currentVersionCode = 120,
            minSupportedVersion = "1.0.0",
            minSupportedVersionCode = 100,
            forcedUpdateMessageArabic = "يتطلب هذا الإصدار تحديثاً هاماً للاستمرار في استخدام سراج بأمان",
            storeUpdateUrl = "market://details?id=com.siraj.app",
            supportedDatabaseSchemaVersion = 3
        )

        assertTrue(policy.isVersionSupported(105))
        assertFalse(policy.isUpdateRequired(105))

        assertFalse(policy.isVersionSupported(95))
        assertTrue(policy.isUpdateRequired(95))
    }
}
