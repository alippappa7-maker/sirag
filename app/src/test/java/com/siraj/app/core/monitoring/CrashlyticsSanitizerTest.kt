package com.siraj.app.core.monitoring

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CrashlyticsSanitizerTest {

    @Test
    fun `sanitizeMessage returns empty for null or blank input`() {
        assertEquals("", CrashlyticsSanitizer.sanitizeMessage(null))
        assertEquals("", CrashlyticsSanitizer.sanitizeMessage(""))
        assertEquals("", CrashlyticsSanitizer.sanitizeMessage("   "))
    }

    @Test
    fun `sanitizeMessage masks Bearer tokens`() {
        val input = "Authorization: Bearer abcdef-12345.67890+/="
        val result = CrashlyticsSanitizer.sanitizeMessage(input)
        assertFalse("Bearer value must be masked", result.contains("abcdef-12345"))
        assertTrue("Bearer placeholder expected", result.contains("***"))
    }

    @Test
    fun `sanitizeMessage masks api keys and tokens in query strings`() {
        val input = "https://api.example.com/call?api_key=SECRET123&token=TOKEN456"
        val result = CrashlyticsSanitizer.sanitizeMessage(input)
        assertFalse(result.contains("SECRET123"))
        assertFalse(result.contains("TOKEN456"))
    }

    @Test
    fun `sanitizeMessage masks passwords`() {
        val input = "login failed password=mySuperSecretPwd"
        val result = CrashlyticsSanitizer.sanitizeMessage(input)
        assertFalse(result.contains("mySuperSecretPwd"))
        assertTrue(result.contains("***"))
    }

    @Test
    fun `sanitizeMessage masks emails`() {
        val input = "User contact: user@example.com reached out"
        val result = CrashlyticsSanitizer.sanitizeMessage(input)
        assertFalse("Email must be masked", result.contains("user@example.com"))
        assertTrue(result.contains("[MASKED_EMAIL]"))
    }

    @Test
    fun `sanitizeMessage truncates payloads longer than 500 chars`() {
        val longInput = "x".repeat(600)
        val result = CrashlyticsSanitizer.sanitizeMessage(longInput)
        assertTrue(result.length <= 500 + "... [TRUNCATED]".length)
        assertTrue(result.endsWith("... [TRUNCATED]"))
    }

    @Test
    fun `sanitizeMessage leaves ordinary technical messages intact`() {
        val input = "Firestore read failed for collection projects"
        val result = CrashlyticsSanitizer.sanitizeMessage(input)
        assertEquals(input, result)
    }

    @Test
    fun `isKeyAllowed accepts safe technical keys`() {
        assertTrue(CrashlyticsSanitizer.isKeyAllowed("screen_name"))
        assertTrue(CrashlyticsSanitizer.isKeyAllowed("appVersion"))
        assertTrue(CrashlyticsSanitizer.isKeyAllowed("error_category"))
        assertTrue(CrashlyticsSanitizer.isKeyAllowed("authenticated"))
    }

    @Test
    fun `isKeyAllowed rejects sensitive keys`() {
        assertFalse(CrashlyticsSanitizer.isKeyAllowed("password"))
        assertFalse(CrashlyticsSanitizer.isKeyAllowed("api_key"))
        assertFalse(CrashlyticsSanitizer.isKeyAllowed("userToken"))
        assertFalse(CrashlyticsSanitizer.isKeyAllowed("purchase_token"))
        assertFalse(CrashlyticsSanitizer.isKeyAllowed("quran_ayah"))
        assertFalse(CrashlyticsSanitizer.isKeyAllowed("draft_content"))
        assertFalse(CrashlyticsSanitizer.isKeyAllowed("email"))
        assertFalse(CrashlyticsSanitizer.isKeyAllowed("credit_card"))
    }

    @Test
    fun `anonymizeUserId returns null for null or blank`() {
        assertNull(CrashlyticsSanitizer.anonymizeUserId(null))
        assertNull(CrashlyticsSanitizer.anonymizeUserId(""))
        assertNull(CrashlyticsSanitizer.anonymizeUserId("   "))
    }

    @Test
    fun `anonymizeUserId is stable and non-reversible`() {
        val userId = "user_12345"
        val first = CrashlyticsSanitizer.anonymizeUserId(userId)
        val second = CrashlyticsSanitizer.anonymizeUserId(userId)
        assertEquals("Same input must produce same hash", first, second)
        assertNotEquals("Hash must differ from raw id", userId, first)
        assertFalse("Raw id must not appear in hash", first!!.contains("12345"))
        assertEquals(16, first.length)
    }

    @Test
    fun `anonymizeUserId differs for different users`() {
        val a = CrashlyticsSanitizer.anonymizeUserId("user_A")
        val b = CrashlyticsSanitizer.anonymizeUserId("user_B")
        assertNotEquals(a, b)
    }

    @Test
    fun `formatSafeBreadcrumb sanitizes message and filters sensitive attributes`() {
        val result = CrashlyticsSanitizer.formatSafeBreadcrumb(
            message = "Started job for user@example.com",
            attributes = mapOf(
                "screen" to "Home",
                "api_key" to "SECRET",
                "password" to "shouldNotAppear"
            )
        )
        assertFalse("Email masked", result.contains("user@example.com"))
        assertFalse("Sensitive attr value hidden", result.contains("SECRET"))
        assertFalse("Password attr hidden", result.contains("shouldNotAppear"))
        assertTrue("Safe attr kept", result.contains("screen=Home"))
    }

    @Test
    fun `formatSafeBreadcrumb without attributes returns clean message`() {
        val result = CrashlyticsSanitizer.formatSafeBreadcrumb(message = "plain breadcrumb")
        assertEquals("plain breadcrumb", result)
    }
}
