package com.siraj.app.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.siraj.app.core.privacy.PrivacyManager
import com.siraj.app.core.error.ErrorHandler
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.privacy.*
import com.siraj.app.domain.repository.privacy.PrivacyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.util.UUID

class FirebasePrivacyRepositoryImpl(
    private val context: Context,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : PrivacyRepository {
    override fun observePrivacyOverview(userId: String): Flow<PrivacyOverviewData> =
        callbackFlow {
            val userDocRef = firestore.collection("users").document(userId)
            val deletionReqRef = firestore.collection("account_deletion_requests").document(userId)

            val listener =
                userDocRef.addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        trySend(calculateLocalPrivacyOverview(userId, null))
                        return@addSnapshotListener
                    }

                    deletionReqRef
                        .get()
                        .addOnSuccessListener { deletionDoc ->
                            val deletionReq =
                                if (deletionDoc != null && deletionDoc.exists()) {
                                    AccountDeletionRequest(
                                        requestId = deletionDoc.getString("requestId") ?: "",
                                        userId = userId,
                                        status =
                                            try {
                                                DeletionStatus.valueOf(deletionDoc.getString("status") ?: "NONE")
                                            } catch (_: Exception) {
                                                DeletionStatus.NONE
                                            },
                                        requestedAt = deletionDoc.getLong("requestedAt") ?: 0L,
                                        scheduledPurgeAt = deletionDoc.getLong("scheduledPurgeAt") ?: 0L,
                                        gracePeriodDays = deletionDoc.getLong("gracePeriodDays")?.toInt() ?: 14,
                                        reason = deletionDoc.getString("reason") ?: "",
                                    )
                                } else {
                                    null
                                }

                            trySend(calculateLocalPrivacyOverview(userId, deletionReq))
                        }.addOnFailureListener {
                            trySend(calculateLocalPrivacyOverview(userId, null))
                        }
                }

            awaitClose {
                listener.remove()
            }
        }

    private fun calculateLocalPrivacyOverview(
        userId: String,
        deletionReq: AccountDeletionRequest?,
    ): PrivacyOverviewData {
        val cacheSizeBytes = PrivacyManager.calculateDirectorySizeBytes(context.cacheDir)
        val downloadsDir = File(context.filesDir, "downloads")
        val downloadSizeBytes = PrivacyManager.calculateDirectorySizeBytes(downloadsDir)
        val downloadCount = downloadsDir.listFiles()?.size ?: 0

        val standardPolicies = PrivacyManager.getStandardRetentionPolicies()

        return PrivacyOverviewData(
            totalStorageBytes = cacheSizeBytes + downloadSizeBytes,
            projectsCount = 0, // updated asynchronously
            historyCount = 0,
            downloadsCount = downloadCount,
            downloadsSizeBytes = downloadSizeBytes,
            cacheSizeBytes = cacheSizeBytes,
            categories = standardPolicies,
            deletionRequest = deletionReq,
        )
    }

    override suspend fun getPrivacyOverview(userId: String): PrivacyOverviewData =
        withContext(Dispatchers.IO) {
            try {
                var projectsCount = 0
                val projectsSnap =
                    firestore
                        .collection("projects")
                        .whereEqualTo("ownerId", userId)
                        .get()
                        .await()
                projectsCount = projectsSnap.size()

                val deletionDoc =
                    firestore
                        .collection("account_deletion_requests")
                        .document(userId)
                        .get()
                        .await()
                val deletionReq =
                    if (deletionDoc != null && deletionDoc.exists()) {
                        AccountDeletionRequest(
                            requestId = deletionDoc.getString("requestId") ?: "",
                            userId = userId,
                            status =
                                try {
                                    DeletionStatus.valueOf(deletionDoc.getString("status") ?: "NONE")
                                } catch (_: Exception) {
                                    DeletionStatus.NONE
                                },
                            requestedAt = deletionDoc.getLong("requestedAt") ?: 0L,
                            scheduledPurgeAt = deletionDoc.getLong("scheduledPurgeAt") ?: 0L,
                            gracePeriodDays = deletionDoc.getLong("gracePeriodDays")?.toInt() ?: 14,
                            reason = deletionDoc.getString("reason") ?: "",
                        )
                    } else {
                        null
                    }

                val cacheSize = PrivacyManager.calculateDirectorySizeBytes(context.cacheDir)
                val downloadsDir = File(context.filesDir, "downloads")
                val downloadSize = PrivacyManager.calculateDirectorySizeBytes(downloadsDir)
                val downloadCount = downloadsDir.listFiles()?.size ?: 0

                val categories =
                    getStandardRetentionPolicies().map { cat ->
                        when (cat.id) {
                            "projects_content" -> cat.copy(itemCount = projectsCount)
                            "cached_downloads" -> cat.copy(itemCount = downloadCount, sizeBytes = downloadSize + cacheSize)
                            else -> cat
                        }
                    }

                PrivacyOverviewData(
                    totalStorageBytes = cacheSize + downloadSize,
                    projectsCount = projectsCount,
                    historyCount = 0,
                    downloadsCount = downloadCount,
                    downloadsSizeBytes = downloadSize,
                    cacheSizeBytes = cacheSize,
                    categories = categories,
                    deletionRequest = deletionReq,
                )
            } catch (_: Exception) {
                calculateLocalPrivacyOverview(userId, null)
            }
        }

    override suspend fun generateUserDataExport(userId: String): Resource<UserDataExportPackage> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val userSnap =
                    firestore
                        .collection("users")
                        .document(userId)
                        .get()
                        .await()
                val userData = userSnap.data ?: emptyMap<String, Any>()

                val projectsSnap =
                    firestore
                        .collection("projects")
                        .whereEqualTo("ownerId", userId)
                        .get()
                        .await()
                val projectsList = projectsSnap.documents.mapNotNull { it.data }

                val historySnap =
                    firestore
                        .collection("activity_history")
                        .whereEqualTo("userId", userId)
                        .get()
                        .await()
                val historyList = historySnap.documents.mapNotNull { it.data }

                val invoicesSnap =
                    firestore
                        .collection("invoices")
                        .whereEqualTo("userId", userId)
                        .get()
                        .await()
                val anonymizedInvoices =
                    invoicesSnap.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        mapOf(
                            "invoiceId" to doc.id.take(8) + "***",
                            "planName" to (data["planName"] ?: "سراج بلس"),
                            "amount" to (data["amount"] ?: 0),
                            "currency" to (data["currency"] ?: "USD"),
                            "date" to (data["createdAt"] ?: System.currentTimeMillis()),
                            "status" to (data["status"] ?: "PAID"),
                        )
                    }

                val timestamp = System.currentTimeMillis()
                val exportId = "SIRAJ-EXP-${UUID.randomUUID().toString().take(8).uppercase()}"
                val formattedDate = PrivacyManager.formatDate(timestamp)

                val exportPackage =
                    UserDataExportPackage(
                        exportId = exportId,
                        userId = userId,
                        exportTimestamp = timestamp,
                        exportDateFormatted = formattedDate,
                        accountInfo = userData,
                        projects = projectsList,
                        activityHistory = historyList,
                        preferences = (userData["preferences"] as? Map<String, Any>) ?: emptyMap(),
                        anonymizedInvoicesSummary = anonymizedInvoices,
                        sha256Checksum = "", // will compute below
                    )

                val jsonContent = PrivacyManager.buildExportJsonString(exportPackage)
                val checksum = PrivacyManager.calculateSha256(jsonContent)

                Resource.Success(exportPackage.copy(sha256Checksum = checksum))
            } catch (e: Exception) {
                val error = ErrorHandler.handle(e)
                Resource.Error("فشل استخراج وتجهيز بيانات المستخدم: ${error.userMessage}", error)
            }
        }

    override suspend fun exportUserDataToJson(userId: String): Resource<String> =
        withContext(Dispatchers.IO) {
            val exportResult = generateUserDataExport(userId)
            return@withContext if (exportResult is Resource.Success && exportResult.data != null) {
                val json = PrivacyManager.buildExportJsonString(exportResult.data)
                Resource.Success(json)
            } else {
                Resource.Error((exportResult as? Resource.Error)?.message ?: "تعذر إنشاء ملف التصدير")
            }
        }

    override suspend fun saveExportJsonToFile(
        context: Context,
        json: String,
    ): Resource<File> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val exportDir = File(context.cacheDir, "exports")
                if (!exportDir.exists()) exportDir.mkdirs()
                val fileName = "siraj_export_${System.currentTimeMillis()}.json"
                val file = File(exportDir, fileName)
                FileWriter(file).use { it.write(json) }
                Resource.Success(file)
            } catch (e: Exception) {
                Resource.Error("فشل حفظ ملف التصدير محلياً: ${e.message}")
            }
        }

    override suspend fun clearWatchHistory(userId: String): Resource<Unit> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val snap =
                    firestore
                        .collection("activity_history")
                        .whereEqualTo("userId", userId)
                        .get()
                        .await()
                for (doc in snap.documents) {
                    doc.reference.delete().await()
                }
                Resource.Success(Unit)
            } catch (e: Exception) {
                val error = ErrorHandler.handle(e)
                Resource.Error("فشل مسح سجل المشاهدة: ${error.userMessage}", error)
            }
        }

    override suspend fun clearDownloads(userId: String): Resource<Unit> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val downloadsDir = File(context.filesDir, "downloads")
                PrivacyManager.clearDirectory(downloadsDir)
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error("فشل مسح التنزيلات: ${e.message}")
            }
        }

    override suspend fun clearLocalCache(context: Context): Resource<Long> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val initialSize = PrivacyManager.calculateDirectorySizeBytes(context.cacheDir)
                PrivacyManager.clearDirectory(context.cacheDir)
                Resource.Success(initialSize)
            } catch (e: Exception) {
                Resource.Error("فشل تفريغ الذاكرة المؤقتة: ${e.message}")
            }
        }

    override suspend fun deleteUserProject(projectId: String): Resource<Unit> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                firestore
                    .collection("projects")
                    .document(projectId)
                    .delete()
                    .await()
                Resource.Success(Unit)
            } catch (e: Exception) {
                val error = ErrorHandler.handle(e)
                Resource.Error("فشل حذف المشروع: ${error.userMessage}", error)
            }
        }

    override suspend fun requestAccountDeletion(
        userId: String,
        reason: String,
        gracePeriodDays: Int,
    ): Resource<AccountDeletionRequest> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val now = System.currentTimeMillis()
                val scheduledPurge = now + (gracePeriodDays.toLong() * 24 * 60 * 60 * 1000L)
                val requestId = "DEL-REQ-${UUID.randomUUID().toString().take(8).uppercase()}"

                val request =
                    AccountDeletionRequest(
                        requestId = requestId,
                        userId = userId,
                        status = DeletionStatus.GRACE_PERIOD_ACTIVE,
                        requestedAt = now,
                        scheduledPurgeAt = scheduledPurge,
                        gracePeriodDays = gracePeriodDays,
                        reason = reason,
                    )

                val map =
                    mapOf(
                        "requestId" to request.requestId,
                        "userId" to request.userId,
                        "status" to request.status.name,
                        "requestedAt" to request.requestedAt,
                        "scheduledPurgeAt" to request.scheduledPurgeAt,
                        "gracePeriodDays" to request.gracePeriodDays,
                        "reason" to request.reason,
                        "legalFinancialRecordsNotice" to request.legalFinancialRecordsNotice,
                    )

                firestore
                    .collection("account_deletion_requests")
                    .document(userId)
                    .set(map, SetOptions.merge())
                    .await()

                // Update user status to restrict further operations
                firestore
                    .collection("users")
                    .document(userId)
                    .update(
                        mapOf(
                            "preferences.accountDeletionStatus" to DeletionStatus.GRACE_PERIOD_ACTIVE.name,
                            "preferences.accountDeletionScheduledAt" to scheduledPurge,
                        ),
                    ).await()

                Resource.Success(request)
            } catch (e: Exception) {
                val error = ErrorHandler.handle(e)
                Resource.Error("فشل تقديم طلب حذف الحساب: ${error.userMessage}", error)
            }
        }

    override suspend fun cancelAccountDeletion(userId: String): Resource<Unit> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                firestore
                    .collection("account_deletion_requests")
                    .document(userId)
                    .delete()
                    .await()
                firestore
                    .collection("users")
                    .document(userId)
                    .update(
                        mapOf(
                            "preferences.accountDeletionStatus" to DeletionStatus.NONE.name,
                            "preferences.accountDeletionScheduledAt" to null,
                        ),
                    ).await()
                Resource.Success(Unit)
            } catch (e: Exception) {
                val error = ErrorHandler.handle(e)
                Resource.Error("فشل إلغاء طلب حذف الحساب: ${error.userMessage}", error)
            }
        }

    override suspend fun submitDataCorrection(request: DataCorrectionRequest): Resource<Unit> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val reqId = if (request.id.isEmpty()) UUID.randomUUID().toString() else request.id
                val map =
                    mapOf(
                        "id" to reqId,
                        "userId" to request.userId,
                        "fieldName" to request.fieldName,
                        "currentValue" to request.currentValue,
                        "requestedValue" to request.requestedValue,
                        "reason" to request.reason,
                        "submittedAt" to request.submittedAt,
                        "status" to request.status,
                    )
                firestore
                    .collection("data_correction_requests")
                    .document(reqId)
                    .set(map)
                    .await()
                Resource.Success(Unit)
            } catch (e: Exception) {
                val error = ErrorHandler.handle(e)
                Resource.Error("فشل تقديم طلب تصحيح البيانات: ${error.userMessage}", error)
            }
        }

    override fun getStandardRetentionPolicies(): List<StoredDataCategory> = PrivacyManager.getStandardRetentionPolicies()
}
