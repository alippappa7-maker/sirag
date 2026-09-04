package com.siraj.app.data.repository.subscription

import com.google.firebase.firestore.FirebaseFirestore
import com.siraj.app.domain.models.subscription.*
import com.siraj.app.domain.repository.subscription.SubscriptionRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class FirebaseSubscriptionRepositoryImpl(
    private val firestore: FirebaseFirestore = try { FirebaseFirestore.getInstance() } catch (e: Exception) { throw e }
) : SubscriptionRepository {

    override fun getAvailablePlans(): Flow<List<Plan>> = callbackFlow {
        val listener = firestore.collection("subscription_plans")
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val plans = snapshot.documents.mapNotNull { it.toObject(Plan::class.java) }
                    trySend(plans)
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getCurrentSubscription(
        userId: String,
        workspaceId: String?,
    ): Flow<Subscription?> = callbackFlow {
        if (userId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val query = if (workspaceId != null) {
            firestore.collection("subscriptions")
                .whereEqualTo("workspaceId", workspaceId)
        } else {
            firestore.collection("subscriptions")
                .whereEqualTo("userId", userId)
        }
        
        val listener = query.whereIn("status", listOf(SubscriptionStatus.ACTIVE.name, SubscriptionStatus.TRIAL.name))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val subscription = snapshot.documents.firstOrNull()?.toObject(Subscription::class.java)
                    trySend(subscription)
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getCurrentEntitlement(
        userId: String,
        workspaceId: String?,
    ): Flow<Entitlement?> = getCurrentSubscription(userId, workspaceId).flatMapLatest { sub ->
        if (sub != null && (sub.status == SubscriptionStatus.ACTIVE || sub.status == SubscriptionStatus.TRIAL)) {
            callbackFlow {
                val listener = firestore.collection("subscription_plans").document(sub.planId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null && snapshot.exists()) {
                            val plan = snapshot.toObject(Plan::class.java)
                            if (plan != null) {
                                trySend(
                                    Entitlement(
                                        id = "ent_${sub.id}",
                                        features = plan.features,
                                        limits = plan.limits,
                                    )
                                )
                            } else {
                                trySend(null)
                            }
                        } else {
                            trySend(null)
                        }
                    }
                awaitClose { listener.remove() }
            }
        } else {
            flowOf(null)
        }
    }

    override fun getCreditBalance(
        userId: String,
        workspaceId: String?,
    ): Flow<CreditBalance?> = callbackFlow {
        if (userId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val targetId = workspaceId ?: userId
        val collection = if (workspaceId != null) "workspaces" else "users"
        
        val listener = firestore.collection(collection).document(targetId)
            .collection("credits").document("balance")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.toObject(CreditBalance::class.java))
                } else {
                    trySend(
                        CreditBalance(
                            userId = userId,
                            workspaceId = workspaceId,
                            availableCredits = 0,
                            totalPurchased = 0,
                            totalUsed = 0,
                            lastUpdated = System.currentTimeMillis(),
                        )
                    )
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getCreditTransactions(
        userId: String,
        workspaceId: String?,
        limit: Int,
    ): Flow<List<CreditTransaction>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val targetId = workspaceId ?: userId
        val collection = if (workspaceId != null) "workspaces" else "users"
        
        val listener = firestore.collection(collection).document(targetId)
            .collection("credit_transactions")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.documents.mapNotNull { it.toObject(CreditTransaction::class.java) })
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun reserveCredits(
        userId: String,
        workspaceId: String?,
        jobId: String,
        operationType: String,
        amount: Int,
    ): Result<CreditTransaction> {
        return Result.failure(IllegalStateException("Credit reservation requires Cloud Functions"))
    }

    override suspend fun confirmCredits(transactionId: String): Result<Boolean> {
        return Result.success(true)
    }

    override suspend fun refundCredits(
        transactionId: String,
        reason: String,
    ): Result<Boolean> {
        return Result.success(true)
    }

    override suspend fun verifyPurchase(
        platform: String,
        productId: String,
        purchaseToken: String,
        workspaceId: String?,
    ): Result<Subscription> {
        return Result.failure(IllegalStateException("Purchase verification requires Server-Side API"))
    }
}
