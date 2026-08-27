package com.siraj.app.domain.models

import java.util.UUID

enum class ClaimType { GENERAL, QURAN, HADITH, TAFSIR, FIQH, BIOGRAPHY, QUOTE }
enum class SourceStatus { MISSING, PENDING_VERIFICATION, VERIFIED, REJECTED }
enum class ReviewStatus { DRAFT, PENDING_REVIEW, APPROVED, REJECTED }

data class ContentClaim(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val type: ClaimType = ClaimType.GENERAL,
    val sourceStatus: SourceStatus = SourceStatus.MISSING,
    val sourceId: String? = null,
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val reviewStatus: ReviewStatus = ReviewStatus.DRAFT,
    val contextNote: String = "",
    val attachedSource: Source? = null,
    val verificationRecords: List<VerificationRecord> = emptyList()
)

data class ContentPlan(
    val title: String = "",
    val hook: String = "",
    val mainPoints: String = "", // Storing as single text block for easier editing in MVP
    val conclusion: String = "",
    val callToAction: String = "",
    val estimatedDuration: String = "",
    val claims: List<ContentClaim> = emptyList(),
    val requiredSources: List<String> = emptyList(),
    val reviewLevel: RiskLevel = RiskLevel.LOW,
    val warnings: List<String> = emptyList(),
    val version: Int = 1,
    val lastEditedAt: Long = System.currentTimeMillis()
)
