package com.siraj.app.data.repository.beta

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.siraj.app.core.monitoring.CrashMonitoringManager
import com.siraj.app.domain.models.beta.BetaFeedback
import com.siraj.app.domain.models.beta.FeedbackCategory
import com.siraj.app.domain.models.beta.FeedbackSeverity
import com.siraj.app.domain.repository.BetaFeedbackRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseBetaFeedbackRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : BetaFeedbackRepository {
    private val feedbackCollection = firestore.collection("beta_feedback")

    override suspend fun submitFeedback(feedback: BetaFeedback): Result<String> =
        try {
            val docId = if (feedback.id.isNotBlank()) feedback.id else "fb_${UUID.randomUUID()}"
            val feedbackToSave =
                feedback.copy(
                    id = docId,
                    timestamp = if (feedback.timestamp > 0) feedback.timestamp else System.currentTimeMillis(),
                )

            val dataMap =
                hashMapOf<String, Any>(
                    "id" to feedbackToSave.id,
                    "userId" to feedbackToSave.userId,
                    "userEmail" to feedbackToSave.userEmail,
                    "userName" to feedbackToSave.userName,
                    "category" to feedbackToSave.category.name,
                    "severity" to feedbackToSave.severity.name,
                    "title" to feedbackToSave.title,
                    "description" to feedbackToSave.description,
                    "stepsToReproduce" to feedbackToSave.stepsToReproduce,
                    "currentRoute" to feedbackToSave.currentRoute,
                    "appVersion" to feedbackToSave.appVersion,
                    "deviceModel" to feedbackToSave.deviceModel,
                    "androidOsVersion" to feedbackToSave.androidOsVersion,
                    "timestamp" to feedbackToSave.timestamp,
                    "status" to feedbackToSave.status,
                )

            feedbackCollection.document(docId).set(dataMap).await()

            // Log diagnostic info to monitoring
            CrashMonitoringManager.logBreadcrumb(
                "Beta Feedback Submitted: ${feedbackToSave.category.name} - ${feedbackToSave.title} (Severity: ${feedbackToSave.severity.name})",
            )
            if (feedbackToSave.severity == FeedbackSeverity.CRITICAL || feedbackToSave.severity == FeedbackSeverity.HIGH) {
                CrashMonitoringManager.triggerTestNonFatalError(
                    "High-Severity Beta Tester Report: [${feedbackToSave.category.name}] ${feedbackToSave.title} - Route: ${feedbackToSave.currentRoute}",
                )
            }

            Result.success(docId)
        } catch (e: Exception) {
            CrashMonitoringManager.recordException(e)
            Result.failure(e)
        }

    override fun getMyFeedback(userId: String): Flow<List<BetaFeedback>> =
        callbackFlow {
            if (userId.isBlank()) {
                trySend(emptyList())
                close()
                return@callbackFlow
            }

            val listener =
                feedbackCollection
                    .whereEqualTo("userId", userId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }

                        val items =
                            snapshot?.documents?.mapNotNull { doc ->
                                try {
                                    BetaFeedback(
                                        id = doc.getString("id") ?: doc.id,
                                        userId = doc.getString("userId") ?: "",
                                        userEmail = doc.getString("userEmail") ?: "",
                                        userName = doc.getString("userName") ?: "",
                                        category =
                                            doc.getString("category")?.let { enumValueOf<FeedbackCategory>(it) } ?: FeedbackCategory.BUG,
                                        severity =
                                            doc.getString("severity")?.let { enumValueOf<FeedbackSeverity>(it) } ?: FeedbackSeverity.MEDIUM,
                                        title = doc.getString("title") ?: "",
                                        description = doc.getString("description") ?: "",
                                        stepsToReproduce = doc.getString("stepsToReproduce") ?: "",
                                        currentRoute = doc.getString("currentRoute") ?: "",
                                        appVersion = doc.getString("appVersion") ?: "",
                                        deviceModel = doc.getString("deviceModel") ?: "",
                                        androidOsVersion = doc.getString("androidOsVersion") ?: "",
                                        timestamp = doc.getLong("timestamp") ?: 0L,
                                        status = doc.getString("status") ?: "NEW",
                                    )
                                } catch (e: Exception) {
                                    null
                                }
                            } ?: emptyList()

                        trySend(items)
                    }

            awaitClose { listener.remove() }
        }
}
