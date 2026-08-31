# ═══════════════════════════════════════════════════════════════
#  Siraj ProGuard Rules — Release Build Configuration
# ═══════════════════════════════════════════════════════════════

# ─── Preserve line numbers for stack traces ───
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ─── Preserve annotations ───
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# ─── Kotlin ───
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlinx.** { volatile <fields>; }
-dontwarn kotlinx.serialization.**
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# ─── Firebase ───
-keep class com.google.firebase.** { *; }
-keep class com.firebase.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.firebase.**
-keep class com.google.firebase.auth.FirebaseAuthException { *; }
-keep class com.google.firebase.firestore.FirebaseFirestoreException { *; }
-keep class com.google.firebase.storage.StorageException { *; }

# ─── OkHttp / Retrofit ───
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * { @retrofit2.http.* <methods>; }
-keep class okhttp3.internal.platform.** { *; }

# ─── Moshi ───
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class * { @com.squareup.moshi.JsonClass <methods>; }
-keepclassmembers class ** { @com.squareup.moshi.Json <fields>; }

# ─── Room ───
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-dontwarn androidx.room.**

# ─── Coil ───
-dontwarn coil.**

# ─── ExoPlayer / Media3 ───
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# ─── Lottie ───
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# ─── Google Play Billing ───
-keep class com.android.billingclient.** { *; }

# ─── Siraj App ───
-keep class com.siraj.app.** { *; }
-keep class com.siraj.app.core.error.** { *; }
-keep class com.siraj.app.domain.models.** { *; }

# ─── Remove logging in release ───
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
}
# Keep GlobalErrorHandler's Log calls for crash reporting
-keep class com.siraj.app.core.error.GlobalErrorHandler { *; }

# ─── Optimization ───
-allowaccessmodification
-repackageclasses ''
