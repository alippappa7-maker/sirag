package com.siraj.app.data.repository.analytics

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.siraj.app.domain.models.analytics.AnalyticsEvent
import com.siraj.app.domain.models.analytics.AnalyticsLog
import com.siraj.app.domain.repository.analytics.AnalyticsRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

class FirebaseAnalyticsRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AnalyticsRepository {
    
    private var isEnabled = false

    override suspend fun setAnalyticsEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    override suspend fun logEvent(event: AnalyticsEvent, properties: Map<String, String>) {
        if (!isEnabled) return
        
        // Strip out any potentially sensitive properties just in case
        val safeProperties = properties.filterKeys { 
            !it.contains("text", ignoreCase = true) && 
            !it.contains("password", ignoreCase = true) &&
            !it.contains("key", ignoreCase = true)
        }

        val currentUser = auth.currentUser
        val hashedUserId = currentUser?.uid?.let { hashString(it) }

        val log = AnalyticsLog(
            id = UUID.randomUUID().toString(),
            event = event.eventName,
            hashedUserId = hashedUserId,
            timestamp = System.currentTimeMillis(),
            properties = safeProperties
        )

        try {
            firestore.collection("analytics_events")
                .document(log.id)
                .set(log)
                .await()
        } catch (e: Exception) {
            // Silently fail for analytics to not disrupt user experience
        }
    }

    override suspend fun clearUserData() {
        val currentUser = auth.currentUser ?: return
        val hashedUserId = hashString(currentUser.uid)
        
        try {
            val snapshot = firestore.collection("analytics_events")
                .whereEqualTo("hashedUserId", hashedUserId)
                .get()
                .await()
                
            for (document in snapshot.documents) {
                document.reference.delete().await()
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    override fun getAggregatedEvents(): Flow<List<AnalyticsLog>> = callbackFlow {
        val subscription = firestore.collection("analytics_events")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val logs = snapshot.documents.mapNotNull { it.toObject(AnalyticsLog::class.java) }
                    trySend(logs)
                }
            }
            
        awaitClose { subscription.remove() }
    }

    private fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
