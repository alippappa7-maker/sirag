import re

with open('app/src/main/java/com/siraj/app/data/repository/FirebaseAuthRepositoryImpl.kt', 'r') as f:
    content = f.read()

# Add imports
content = content.replace(
    "import com.siraj.app.domain.models.UserRole",
    "import com.siraj.app.domain.models.UserRole\nimport com.siraj.app.domain.models.UserPreferences\nimport com.siraj.app.domain.models.ThemeMode\nimport com.siraj.app.domain.models.CalculationMethod\nimport com.siraj.app.domain.models.Madhab\nimport com.siraj.app.domain.models.VideoQuality"
)

# Parse preferences
new_parsing = """                        if (snapshot != null && snapshot.exists()) {
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
                                    appLockEnabled = prefMap["appLockEnabled"] as? Boolean ?: false
                                )
                            } else {
                                UserPreferences()
                            }
                            
                            trySend(UserProfile(user.uid, name, email, avatarUrl, role, preferences))
                        } else {"""

content = re.sub(r'                        if \(snapshot != null && snapshot\.exists\(\)\) \{.*?                        \} else \{', new_parsing, content, flags=re.DOTALL)

# Add updatePreferences method
update_pref_method = """    override suspend fun updatePreferences(preferences: UserPreferences): Resource<Unit> {
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
                "appLockEnabled" to preferences.appLockEnabled
            )
            firestore.collection("users").document(user.uid).update("preferences", prefMap).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "فشل تحديث الإعدادات")
        }
    }

"""

content = content.replace("    private fun mapFirebaseAuthError", update_pref_method + "    private fun mapFirebaseAuthError")

with open('app/src/main/java/com/siraj/app/data/repository/FirebaseAuthRepositoryImpl.kt', 'w') as f:
    f.write(content)
