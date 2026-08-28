import sys

with open("app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

import re

if "import androidx.navigation.NavBackStackEntry" not in content:
    content = content.replace("import androidx.navigation.NavType\n", "import androidx.navigation.NavType\nimport androidx.navigation.NavBackStackEntry\n")

with open("app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt", "w") as f:
    f.write(content)
