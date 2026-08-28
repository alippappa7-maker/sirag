import sys
import re

with open("app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt", "r") as f:
    content = f.read()
    
# Let's fix missing Screen.Quran and Screen.Audio and others. We patched Screen.kt already.
# For some reason `composable` is not found, meaning `import androidx.navigation.compose.composable` is NOT hitting the right thing or is getting removed.

new_imports = """
import androidx.compose.animation.core.tween
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
content = re.sub(r'import .*\n(?:import .*\n)*', new_imports + '\n', content, count=1)

with open("app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt", "w") as f:
    f.write(content)

