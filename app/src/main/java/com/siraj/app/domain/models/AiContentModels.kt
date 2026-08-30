package com.siraj.app.domain.models

enum class AiSafetyStatus {
    SAFE,
    PENDING_REVIEW,
    FLAGGED,
    REJECTED
}

data class AiMetadata(
    val generatedByAI: Boolean = false,
    val provider: String? = null,
    val model: String? = null,
    val promptVersion: String? = null,
    val generatedAt: Long? = null,
    val editedByUser: Boolean = false,
    val humanReviewed: Boolean = false,
    val disclosureRequired: Boolean = false,
    val safetyStatus: AiSafetyStatus = AiSafetyStatus.SAFE,
    val aiDisclaimers: List<String> = emptyList()
)

data class AiContentReport(
    val reportId: String = java.util.UUID.randomUUID().toString(),
    val contentId: String,
    val contentType: String,
    val reporterId: String,
    val reason: String,
    val details: String = "",
    val reportedAt: Long = System.currentTimeMillis(),
    val status: String = "PENDING"
)

enum class AiProviderStatus {
    ACTIVE,
    DISABLED_TEMPORARILY,
    DISABLED_PERMANENTLY
}

data class AiProviderConfig(
    val id: String,
    val name: String,
    val status: AiProviderStatus = AiProviderStatus.ACTIVE,
    val disableReason: String? = null
)
