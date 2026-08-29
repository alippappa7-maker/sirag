package com.siraj.app

import android.app.Application
import android.content.Context
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.siraj.app.core.config.EnvironmentConfig
import com.siraj.app.core.monitoring.CrashMonitoringManager

class SirajApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        ensureFirebase(this)
        
        // Initialize Crashlytics & Error Monitoring
        try {
            CrashMonitoringManager.initialize(
                environment = EnvironmentConfig.currentEnvironment.name,
                appVersion = BuildConfig.VERSION_NAME,
                buildNumber = BuildConfig.VERSION_CODE.toString()
            )
            // By default enable in Staging/Production, configurable in Dev
            val shouldEnableCrashlytics = !EnvironmentConfig.isDebugEnabled || true
            CrashMonitoringManager.setCrashlyticsCollectionEnabled(shouldEnableCrashlytics)
        } catch (e: Exception) {
            Log.e("SirajApplication", "Could not initialize CrashMonitoringManager", e)
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.15) // Limit memory to 15%
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02) // Limit disk to 2%
                    .build()
            }
            .crossfade(true)
            .respectCacheHeaders(false) // Optimize for slow networks by enforcing local cache if available
            .build()
    }

    companion object {
        fun ensureFirebase(context: Context) {
            try {
                if (FirebaseApp.getApps(context).isEmpty()) {
                    val options = FirebaseOptions.Builder()
                        .setApplicationId(context.packageName.ifEmpty { "com.aistudio.siraj" })
                        .setApiKey(BuildConfig.FIREBASE_API_KEY)
                        .setProjectId("siraj-applet-dev")
                        .setStorageBucket("siraj-applet-dev.appspot.com")
                        .build()
                    FirebaseApp.initializeApp(context.applicationContext, options)
                    Log.d("SirajApplication", "Firebase initialized with fallback configuration.")
                }
                
                // Configure Firestore Settings for Offline Support and Cache Limit
                val firestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(
                        PersistentCacheSettings.newBuilder()
                            .setSizeBytes(50L * 1024L * 1024L) // 50 MB Cache
                            .build()
                    )
                    .build()
                FirebaseFirestore.getInstance().firestoreSettings = firestoreSettings
                Log.d("SirajApplication", "Firestore settings configured for offline persistence (50MB cache).")

            } catch (e: Exception) {
                Log.e("SirajApplication", "Could not initialize FirebaseApp", e)
            }
        }
    }
}
