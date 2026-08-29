package com.siraj.app.features.settings.privacy

import com.siraj.app.core.privacy.PrivacyManager
import com.siraj.app.domain.models.privacy.UserDataExportPackage
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PrivacyManagerTest {

    @Test
    fun `sanitizeDataMap strips forbidden security keys`() {
        val rawData = mapOf(
            "userId" to "user_123",
            "name" to "أحمد علي",
            "password" to "super_secret_password",
            "passwordHash" to "sha256_hash_value",
            "token" to "oauth_bearer_token",
            "rawPurchaseToken" to "play_store_token_123",
            "apiKey" to "AIzaSySecretApiKey",
            "nested" to mapOf(
                "publicInfo" to "عام",
                "secretKey" to "hidden_secret"
            ),
            "listItems" to listOf(
                mapOf("title" to "مشروع 1", "accessToken" to "token_val"),
                mapOf("title" to "مشروع 2", "safeField" to "قيمة آمنة")
            )
        )

        val sanitized = PrivacyManager.sanitizeDataMap(rawData)

        assertEquals("user_123", sanitized["userId"])
        assertEquals("أحمد علي", sanitized["name"])
        assertNull(sanitized["password"])
        assertNull(sanitized["passwordHash"])
        assertNull(sanitized["token"])
        assertNull(sanitized["rawPurchaseToken"])
        assertNull(sanitized["apiKey"])

        val nested = sanitized["nested"] as Map<*, *>
        assertEquals("عام", nested["publicInfo"])
        assertNull(nested["secretKey"])

        val list = sanitized["listItems"] as List<*>
        val item1 = list[0] as Map<*, *>
        assertEquals("مشروع 1", item1["title"])
        assertNull(item1["accessToken"])

        val item2 = list[1] as Map<*, *>
        assertEquals("مشروع 2", item2["title"])
        assertEquals("قيمة آمنة", item2["safeField"])
    }

    @Test
    fun `calculateSha256 produces valid deterministic hash`() {
        val sampleContent = "Siraj Islamic Platform User Data Export"
        val hash1 = PrivacyManager.calculateSha256(sampleContent)
        val hash2 = PrivacyManager.calculateSha256(sampleContent)

        assertNotNull(hash1)
        assertEquals(64, hash1.length) // SHA-256 hex is 64 chars
        assertEquals(hash1, hash2)
    }

    @Test
    fun `buildExportJsonString produces valid JSON structure`() {
        val exportPackage = UserDataExportPackage(
            exportId = "SIRAJ-EXP-TEST1",
            userId = "user_456",
            exportTimestamp = 1700000000000L,
            exportDateFormatted = "2023-11-14 22:13",
            accountInfo = mapOf("name" to "محمد", "email" to "m@example.com"),
            projects = listOf(mapOf("title" to "فيديو آية الكرسي", "status" to "APPROVED")),
            activityHistory = listOf(mapOf("type" to "QURAN_LISTEN", "surah" to 1)),
            preferences = mapOf("themeMode" to "DARK"),
            anonymizedInvoicesSummary = listOf(mapOf("invoiceId" to "INV-123***", "amount" to 10)),
            sha256Checksum = "sample_sha256"
        )

        val json = PrivacyManager.buildExportJsonString(exportPackage)

        assertTrue(json.contains("\"exportId\": \"SIRAJ-EXP-TEST1\""))
        assertTrue(json.contains("\"userId\": \"user_456\""))
        assertTrue(json.contains("فيديو آية الكرسي"))
        assertTrue(json.contains("INV-123***"))
    }

    @Test
    fun `formatBytes formats sizes accurately`() {
        assertEquals("500 بايت", PrivacyManager.formatBytes(500))
        assertEquals("1.0 كيلوبايت", PrivacyManager.formatBytes(1024))
        assertEquals("1.5 ميجابايت", PrivacyManager.formatBytes((1.5 * 1024 * 1024).toLong()))
    }

    @Test
    fun `getStandardRetentionPolicies contains all required categories`() {
        val policies = PrivacyManager.getStandardRetentionPolicies()

        assertTrue(policies.any { it.id == "account_profile" })
        assertTrue(policies.any { it.id == "projects_content" })
        assertTrue(policies.any { it.id == "activity_history" })
        assertTrue(policies.any { it.id == "cached_downloads" })
        assertTrue(policies.any { it.id == "location_data" })
        assertTrue(policies.any { it.id == "financial_records" && it.isLegalRequired })
    }
}
