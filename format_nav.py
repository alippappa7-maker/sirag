with open('app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt', 'r') as f:
    content = f.read()

import re
content = re.sub(r'^.*?package com.siraj.app.core.navigation', 'package com.siraj.app.core.navigation', content, flags=re.DOTALL)

imports = """
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.siraj.app.core.utils.Resource
import com.siraj.app.features.admin.presentation.AdminScreen
import com.siraj.app.features.audio.presentation.AudioLibraryScreen
import com.siraj.app.features.auth.presentation.AuthViewModel
import com.siraj.app.features.auth.presentation.AuthViewModelFactory
import com.siraj.app.features.auth.presentation.LoginScreen
import com.siraj.app.features.auth.presentation.RegisterScreen
import com.siraj.app.features.details.presentation.DetailsScreen
import com.siraj.app.features.flashes.presentation.FlashesScreen
import com.siraj.app.features.home.presentation.HomeScreen
import com.siraj.app.features.mihrab.presentation.MihrabScreen
import com.siraj.app.features.onboarding.presentation.OnboardingScreen
import com.siraj.app.features.settings.presentation.ProfileScreen
import com.siraj.app.features.splash.presentation.SplashScreen
import com.siraj.app.features.studio.presentation.StudioScreen
import com.siraj.app.domain.models.UserRole
import com.siraj.app.core.navigation.Screen
import com.siraj.app.core.navigation.MainShellScreen
import com.siraj.app.core.ui.components.ErrorScreen
"""

# Let's clean up any duplicate imports if present, but since we regexed everything before package it's fine.
content = content.replace("package com.siraj.app.core.navigation", "package com.siraj.app.core.navigation\n" + imports)

# Remove old imports that are still lingering if any
# We'll just trust the regex removed them.

with open('app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt', 'w') as f:
    f.write(content)
