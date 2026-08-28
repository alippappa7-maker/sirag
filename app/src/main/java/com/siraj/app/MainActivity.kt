package com.siraj.app

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import com.siraj.app.domain.models.ThemeMode

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
            
            LaunchedEffect(currentUser?.preferences?.themeMode) {
                currentUser?.preferences?.themeMode?.let { mode ->
                    isDarkTheme = when(mode) {
                        ThemeMode.DARK -> true
                        ThemeMode.LIGHT -> false
                        ThemeMode.SYSTEM -> systemTheme
                    }
                }
            }
            
            val navController = rememberNavController()
            
            MyApplicationTheme(darkTheme = isDarkTheme) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AppNavigation(
                        navController = navController,
                        toggleTheme = { isDarkTheme = !isDarkTheme }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AudioController.release()
    }
}
