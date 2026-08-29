package com.siraj.app

// Activity entry point
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.rememberNavController
import com.siraj.app.core.audio.AudioController
import com.siraj.app.core.navigation.AppNavigation
import com.siraj.app.ui.theme.MyApplicationTheme
import com.siraj.app.data.repository.FirebaseAuthRepositoryImpl
import com.siraj.app.core.analytics.AnalyticsManager
import com.siraj.app.core.monitoring.CrashMonitoringManager
import com.siraj.app.domain.models.ThemeMode
import com.siraj.app.domain.models.analytics.AnalyticsEvent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ensure Firebase initialization
        SirajApplication.ensureFirebase(this)
        
        // Initialize Audio Controller for background playback
        AudioController.initialize(this)
        
        enableEdgeToEdge()
        setContent {
            val systemTheme = isSystemInDarkTheme()
            val authRepository = remember { FirebaseAuthRepositoryImpl() }
            val currentUser by authRepository.currentUser.collectAsState(initial = null)
            
            var isDarkTheme by remember { mutableStateOf(systemTheme) }
            var isHighContrast by remember { mutableStateOf(false) }
            var fontScaleMultiplier by remember { mutableStateOf(1.0f) }
            var language by remember { mutableStateOf("ar") }
            var accessibilityConfig by remember { mutableStateOf(com.siraj.app.core.accessibility.AccessibilityConfig()) }
            
            LaunchedEffect(currentUser) {
                val user = currentUser
                if (user != null) {
                    CrashMonitoringManager.setUserId(user.id)
                    CrashMonitoringManager.setCustomKey("authenticated", true)
                    CrashMonitoringManager.setCustomKey("user_role", user.role.name)
                    
                    val prefs = user.preferences
                    isDarkTheme = when(prefs.themeMode) {
                        ThemeMode.DARK -> true
                        ThemeMode.LIGHT -> false
                        ThemeMode.SYSTEM -> systemTheme
                    }
                    isHighContrast = prefs.highContrastMode
                    fontScaleMultiplier = prefs.fontScaleMultiplier
                    accessibilityConfig = com.siraj.app.core.accessibility.AccessibilityConfig.fromPreferences(prefs)
                    language = prefs.language
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(prefs.language))

                    AnalyticsManager.setAnalyticsEnabled(prefs.analyticsOptIn)
                    CrashMonitoringManager.setCrashlyticsCollectionEnabled(prefs.crashReportsOptIn)
                    AnalyticsManager.logEvent(AnalyticsEvent.APP_OPENED)
                } else {
                    CrashMonitoringManager.setUserId(null)
                    CrashMonitoringManager.setCustomKey("authenticated", false)
                }
            }
            
            val navController = rememberNavController()
            
            MyApplicationTheme(
                darkTheme = isDarkTheme,
                highContrast = isHighContrast,
                fontScaleMultiplier = fontScaleMultiplier
            ) {
                com.siraj.app.core.accessibility.ProvideAccessibilityConfig(accessibilityConfig) {
                    val currentLayoutDirection = if (language == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr
                    CompositionLocalProvider(LocalLayoutDirection provides currentLayoutDirection) {
                        AppNavigation(
                            navController = navController,
                            toggleTheme = { isDarkTheme = !isDarkTheme }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AudioController.release()
    }
}
