import sys

with open("app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

import_dp = "import androidx.compose.ui.unit.dp\n"
if "import androidx.compose.ui.unit.dp" not in content:
    content = content.replace("import androidx.compose.ui.Modifier\n", "import androidx.compose.ui.Modifier\n" + import_dp)
    
with open("app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt", "w") as f:
    f.write(content)
