import sys

with open("app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

# Let's fix the fact that `composable` is not found, or `arguments` is not found
# by just ensuring the exact correct import is present.
imports_to_add = """
import androidx.navigation.compose.composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.compose.NavHost
"""

for imp in imports_to_add.strip().split('\n'):
    if imp not in content:
        content = content.replace("import androidx.navigation.compose.rememberNavController", imp + "\nimport androidx.navigation.compose.rememberNavController")

with open("app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt", "w") as f:
    f.write(content)

