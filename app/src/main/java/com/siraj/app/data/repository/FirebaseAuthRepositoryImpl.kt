package com.siraj.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.UserProfile
import com.siraj.app.domain.models.UserRole
import com.siraj.app.domain.models.UserPreferences
import com.siraj.app.domain.models.ThemeMode
import com.siraj.app.domain.models.CalculationMethod
import com.siraj.app.domain.models.Madhab
import com.siraj.app.domain.models.VideoQuality
import com.siraj.app.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepositoryImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    override val currentUser: Flow<UserProfile?> = callbackFlow {
        var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
        
        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                listenerRegistration?.remove()
                listenerRegistration = firestore.collection("users").document(user.uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(null)
                            return@addSnapshotListener
                        }
                        if (snapshot != null && snapshot.exists()) {
                            val name = snapshot.getString("name") ?: user.displayName ?: "مستخدم"
                            val email = snapshot.getString("email") ?: user.email ?: ""
                            val avatarUrl = snapshot.getString("avatarUrl")
                            val roleStr = snapshot.getString("role") ?: "USER"
                            val role = try { UserRole.valueOf(roleStr) } catch(e: Exception) { UserRole.USER }
                            
                            val prefMap = snapshot.get("preferences") as? Map<String, Any>
                            val preferences = if (prefMap != null) {
                                UserPreferences(
                                    themeMode = ThemeMode.valueOf(prefMap["themeMode"] as? String ?: "SYSTEM"),
                                    reduceMotion = prefMap["reduceMotion"] as? Boolean ?: false,
                                    language = prefMap["language"] as? String ?: "ar",
                                    city = prefMap["city"] as? String ?: "",
                                    prayerNotifications = prefMap["prayerNotifications"] as? Boolean ?: true,
                                    adhkarNotifications = prefMap["adhkarNotifications"] as? Boolean ?: true,
                                    calculationMethod = CalculationMethod.valueOf(prefMap["calculationMethod"] as? String ?: "UMM_AL_QURA"),
                                    madhab = Madhab.valueOf(prefMap["madhab"] as? String ?: "SHAFI"),
                                    videoQuality = VideoQuality.valueOf(prefMap["videoQuality"] as? String ?: "HIGH"),
                                    downloadWifiOnly = prefMap["downloadWifiOnly"] as? Boolean ?: true,
                                    appLockEnabled = prefMap["appLockEnabled"] as? Boolean ?: false,
                                    activeWorkspaceId = prefMap["activeWorkspaceId"] as? String
                                )
                            } else {
                                UserPreferences()
                            }
                            
                            trySend(UserProfile(user.uid, name, email, avatarUrl, role, preferences))
                        } else {
                            trySend(UserProfile(user.uid, user.displayName ?: "مستخدم", user.email ?: ""))
                        }
                    }
            } else {
                listenerRegistration?.remove()
                trySend(null)
            }
        }
        auth.addAuthStateListener(authListener)
        awaitClose { 
            listenerRegistration?.remove()
            auth.removeAuthStateListener(authListener) 
        }
    }

    override suspend fun login(email: String, password: String): Resource<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Resource.Success(Unit)
        } catch (e: FirebaseAuthException) {
            Resource.Error(mapFirebaseAuthError(e.errorCode))
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "حدث خطأ غير معروف")
        }
    }

    override suspend fun register(name: String, email: String, password: String): Resource<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                val userMap = hashMapOf(
                    "id" to user.uid,
                    "name" to name,
                    "email" to email,
                    "role" to "USER",
                    "createdAt" to System.currentTimeMillis()
                )
                firestore.collection("users").document(user.uid).set(userMap).await()
            }
            Resource.Success(Unit)
        } catch (e: FirebaseAuthException) {
            Resource.Error(mapFirebaseAuthError(e.errorCode))
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "حدث خطأ غير معروف")
        }
    }

    override suspend fun logout(): Resource<Unit> {
        return try {
            auth.signOut()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("فشل تسجيل الخروج")
        }
    }

    override suspend fun resetPassword(email: String): Resource<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: FirebaseAuthException) {
            Resource.Error(mapFirebaseAuthError(e.errorCode))
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "حدث خطأ غير معروف")
        }
    }

    override suspend fun verifyEmail(): Resource<Unit> {
        return try {
            auth.currentUser?.sendEmailVerification()?.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("فشل إرسال رمز التحقق")
        }
    }

    override suspend fun deleteAccount(): Resource<Unit> {
        return try {
            val user = auth.currentUser
            user?.delete()?.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("يرجى تسجيل الدخول مجدداً لحذف الحساب.")
        }
    }

    override suspend fun updateProfile(name: String, avatarUrl: String?): Resource<Unit> {
        return try {
            val user = auth.currentUser ?: return Resource.Error("غير مسجل الدخول")
            val updates = mutableMapOf<String, Any>("name" to name)
            if (avatarUrl != null) updates["avatarUrl"] = avatarUrl
            
            firestore.collection("users").document(user.uid).update(updates).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "فشل تحديث الملف الشخصي")
        }
    }

    override suspend fun updatePreferences(preferences: UserPreferences): Resource<Unit> {
        return try {
            val user = auth.currentUser ?: return Resource.Error("غير مسجل الدخول")
            val prefMap = mapOf(
                "themeMode" to preferences.themeMode.name,
                "reduceMotion" to preferences.reduceMotion,
                "language" to preferences.language,
                "city" to preferences.city,
                "prayerNotifications" to preferences.prayerNotifications,
                "adhkarNotifications" to preferences.adhkarNotifications,
                "calculationMethod" to preferences.calculationMethod.name,
                "madhab" to preferences.madhab.name,
                "videoQuality" to preferences.videoQuality.name,
                "downloadWifiOnly" to preferences.downloadWifiOnly,
                "appLockEnabled" to preferences.appLockEnabled,
                "activeWorkspaceId" to preferences.activeWorkspaceId
            )
            firestore.collection("users").document(user.uid).update("preferences", prefMap).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "فشل تحديث الإعدادات")
        }
    }

    private fun mapFirebaseAuthError(errorCode: String): String {
        return when (errorCode) {
            "ERROR_INVALID_EMAIL", "ERROR_INVALID_CREDENTIAL" -> "البريد الإلكتروني أو كلمة المرور غير صحيحة."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "البريد الإلكتروني مسجل مسبقاً."
            "ERROR_USER_NOT_FOUND" -> "المستخدم غير موجود."
            "ERROR_USER_DISABLED" -> "تم حظر هذا الحساب."
            "ERROR_TOO_MANY_REQUESTS" -> "طلبات كثيرة جداً. حاول لاحقاً."
            "ERROR_OPERATION_NOT_ALLOWED" -> "العملية غير مسموحة."
            "ERROR_WEAK_PASSWORD" -> "كلمة المرور ضعيفة جداً."
            else -> "حدث خطأ: \$errorCode"
        }
    }
}
