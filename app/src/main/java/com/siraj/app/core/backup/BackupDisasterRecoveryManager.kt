package com.siraj.app.core.backup

import com.siraj.app.domain.models.backup.BackupEnvironment
import com.siraj.app.domain.models.backup.BackupSnapshot
import java.security.MessageDigest

object BackupDisasterRecoveryManager {
    private val FORBIDDEN_BACKUP_EXPOSURE_KEYS =
        setOf(
            "password",
            "passwordHash",
            "token",
            "rawPurchaseToken",
            "purchaseToken",
            "apiKey",
            "apiSecret",
            "privateKey",
            "secretKey",
            "authCredential",
            "serviceAccountJson",
        )

    fun calculateSha256(data: String): String =
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(data.toByteArray(Charsets.UTF_8))
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            "sha256_fallback_checksum"
        }

    fun verifySnapshotIntegrity(
        snapshot: BackupSnapshot,
        calculatedHash: String,
    ): Boolean = snapshot.checksumSha256.equals(calculatedHash, ignoreCase = true)

    /**
     * Strict GDPR / Privacy Compliance: Right to be Forgotten.
     * When restoring from an older backup snapshot, any record belonging to a user ID that
     * is in the deleted user tombstone list MUST be permanently filtered out and purged.
     */
    fun filterDeletedUserTombstones(
        rawRecords: List<Map<String, Any?>>,
        deletedUserIds: Set<String>,
    ): Pair<List<Map<String, Any?>>, Int> {
        var purgedCount = 0
        val sanitizedList = mutableListOf<Map<String, Any?>>()

        for (record in rawRecords) {
            val ownerId =
                record["userId"] as? String
                    ?: record["authorId"] as? String
                    ?: record["ownerId"] as? String
                    ?: record["creatorId"] as? String

            if (ownerId != null && deletedUserIds.contains(ownerId)) {
                purgedCount++
                continue // Exclude and discard this record permanently
            }
            sanitizedList.add(record)
        }

        return Pair(sanitizedList, purgedCount)
    }

    /**
     * Strip secret credentials and private keys from backup metadata before logging
     */
    fun sanitizeMetadataForAudit(metadata: Map<String, Any?>): Map<String, Any?> {
        val sanitized = mutableMapOf<String, Any?>()
        for ((key, value) in metadata) {
            if (FORBIDDEN_BACKUP_EXPOSURE_KEYS.any { key.contains(it, ignoreCase = true) }) {
                sanitized[key] = "[REDACTED_BY_CMEK_POLICY]"
            } else if (value is Map<*, *>) {
                @Suppress("UNCHECKED_CAST")
                sanitized[key] = sanitizeMetadataForAudit(value as Map<String, Any?>)
            } else {
                sanitized[key] = value
            }
        }
        return sanitized
    }

    /**
     * Check if the latest backup satisfies the RPO (Recovery Point Objective) SLA of <= 60 minutes
     */
    fun isRpoCompliant(
        lastBackupTimestamp: Long,
        targetMinutes: Int = 60,
    ): Boolean {
        if (lastBackupTimestamp <= 0) return false
        val diffMs = System.currentTimeMillis() - lastBackupTimestamp
        val diffMinutes = diffMs / (1000 * 60)
        return diffMinutes <= targetMinutes
    }

    /**
     * Get the isolated vault storage bucket name according to environment
     */
    fun getIsolatedBackupBucketUri(environment: BackupEnvironment): String =
        when (environment) {
            BackupEnvironment.DEV -> "gs://siraj-dev-backups-vault"
            BackupEnvironment.STAGING -> "gs://siraj-staging-backups-vault"
            BackupEnvironment.PROD -> "gs://siraj-prod-backups-isolated-vault-eu"
        }
}
