package com.siraj.app

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class SirajApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ensureFirebase(this)
    }

    companion object {
        fun ensureFirebase(context: Context) {
            try {
                if (FirebaseApp.getApps(context).isEmpty()) {
                    val options = FirebaseOptions.Builder()
                        .setApplicationId(context.packageName.ifEmpty { "com.aistudio.siraj" })
                        .setApiKey("AIzaSySirajDevClientDefaultKey123456789")
                        .setProjectId("siraj-applet-dev")
                        .setStorageBucket("siraj-applet-dev.appspot.com")
                        .build()
                    FirebaseApp.initializeApp(context.applicationContext, options)
                    Log.d("SirajApplication", "Firebase initialized with fallback configuration.")
                }
            } catch (e: Exception) {
                Log.e("SirajApplication", "Could not initialize FirebaseApp", e)
            }
        }
    }
}
