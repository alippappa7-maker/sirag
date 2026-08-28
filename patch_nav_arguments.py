import sys

with open("app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

import re

# Looks like it's complaining about `arguments` on NavBackStackEntry which needs `androidx.navigation.compose.*` or similar. Wait, it's actually `backStackEntry.arguments`
# Sometimes `backStackEntry` type is missing if `composable` is not imported properly.
if "import androidx.navigation.compose.composable" not in content:
    content = content.replace("import androidx.navigation.compose.NavHost", "import androidx.navigation.compose.NavHost\nimport androidx.navigation.compose.composable\n")

if "import androidx.navigation.navArgument" not in content:
    content = content.replace("import androidx.navigation.compose.NavHost", "import androidx.navigation.compose.NavHost\nimport androidx.navigation.navArgument\nimport androidx.navigation.NavType\nimport androidx.navigation.navDeepLink\n")

with open("app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt", "w") as f:
    f.write(content)
