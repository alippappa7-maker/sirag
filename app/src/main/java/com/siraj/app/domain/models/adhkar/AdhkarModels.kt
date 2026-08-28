package com.siraj.app.domain.models.adhkar

enum class VerificationStatus {
    APPROVED, PENDING_REVIEW, REJECTED
}

data class DhikrCategory(
    val id: String,
    val name: String,
    val iconName: String
)

data class DhikrItem(
    val id: String,
    val categoryId: String,
    val text: String,
    val requiredCount: Int,
    val source: String,
    val narrator: String? = null,
    val grade: String? = null,
    val verificationStatus: VerificationStatus = VerificationStatus.PENDING_REVIEW
)

data class DhikrProgress(
    val dhikrId: String,
    val currentCount: Int,
    val date: String,
    val isCompleted: Boolean
)

data class AdhkarSettings(
    val quietMode: Boolean = false
)
