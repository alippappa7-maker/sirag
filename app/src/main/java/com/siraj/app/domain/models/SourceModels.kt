package com.siraj.app.domain.models

import java.util.UUID

enum class SourceType { QURAN, HADITH, TAFSIR, FIQH, BOOK, LECTURE, OFFICIAL_INSTITUTION, OTHER }
enum class SourceVerificationStatus { UNVERIFIED, SUGGESTED, PENDING_REVIEW, VERIFIED, REJECTED, OUTDATED }

data class Source(
    val id: String = UUID.randomUUID().toString(),
    val type: SourceType = SourceType.OTHER,
    val title: String = "",
    val authorOrNarrator: String = "",
    val originalText: String = "",
    val reference: String = "",
    val url: String = "",
    val publisher: String = "",
    val language: String = "العربية",
    val grade: String = "",
    val retrievedAt: Long = System.currentTimeMillis(),
    val reviewStatus: SourceVerificationStatus = SourceVerificationStatus.UNVERIFIED,
    val reviewedBy: String? = null,
    val reviewedAt: Long? = null,
    val notes: String = "",
    val version: Int = 1
)

data class VerificationRecord(
    val id: String = UUID.randomUUID().toString(),
    val sourceId: String,
    val claimId: String,
    val reviewerId: String,
    val previousStatus: SourceVerificationStatus,
    val newStatus: SourceVerificationStatus,
    val decisionNotes: String,
    val timestamp: Long = System.currentTimeMillis()
)
