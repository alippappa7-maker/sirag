import sys

# 1. NavItem.kt Fix
with open("app/src/main/java/com/siraj/app/core/navigation/NavItem.kt", "r") as f:
    navitem_content = f.read()

# Make sure Screen.Flashes and Screen.Mihrab exist, wait actually we hardcoded strings already.
navitem_content = """package com.siraj.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavItem(val title: String, val icon: ImageVector, val route: String) {
    object Home : NavItem("الرئيسية", Icons.Default.Home, Screen.Home.route)
    object Studio : NavItem("الاستوديو", Icons.Default.CameraAlt, Screen.Studio.route)
    object Flashes : NavItem("فلاشات", Icons.Default.PlayCircle, "flashes")
    object Library : NavItem("المكتبة", Icons.Default.LibraryBooks, Screen.Library.route)
    object Mihrab : NavItem("المحراب", Icons.Default.Star, "mihrab")
}
"""
with open("app/src/main/java/com/siraj/app/core/navigation/NavItem.kt", "w") as f:
    f.write(navitem_content)


# 2. AppNavigation Fix
# The composable unresolved reference usually means the import is completely broken or we have an issue with the lambda parameter
with open("app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt", "r") as f:
    appnav_content = f.read()
    
# Let's just make sure all imports are properly placed at the top.
imports_block = """import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import java.net.URLDecoder
"""

# replace everything before "import com.siraj.app.core.ui.components.ErrorScreen" with imports_block
appnav_content = appnav_content[appnav_content.find("import com.siraj.app.core.ui.components.ErrorScreen"):]
appnav_content = "package com.siraj.app.core.navigation\n\n" + imports_block + appnav_content

with open("app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt", "w") as f:
    f.write(appnav_content)

