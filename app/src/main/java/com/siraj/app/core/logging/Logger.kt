package com.siraj.app.core.logging

import android.util.Log
import com.siraj.app.core.config.EnvironmentConfig

interface Logger {
    fun d(tag: String, message: String)
    fun i(tag: String, message: String)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}

class SirajLogger : Logger {
    override fun d(tag: String, message: String) {
        if (EnvironmentConfig.isDebugEnabled) {
            Log.d(tag, message)
        }
    }

    override fun i(tag: String, message: String) {
        Log.i(tag, message)
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        Log.e(tag, message, throwable)
        // Crashlytics or remote logging can be hooked here in the future
    }
}
