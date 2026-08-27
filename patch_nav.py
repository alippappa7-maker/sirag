with open('app/src/main/java/com/siraj/app/core/navigation/Screen.kt', 'r') as f:
    content = f.read()

if 'object WorkspaceSettings : Screen("workspace_settings")' not in content:
    content = content.replace('object Settings : Screen("settings")', 'object Settings : Screen("settings")\n    object WorkspaceSettings : Screen("workspace_settings")')

with open('app/src/main/java/com/siraj/app/core/navigation/Screen.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt', 'r') as f:
    content = f.read()

import_statement = "import com.siraj.app.features.settings.presentation.WorkspaceSettingsScreen"
if import_statement not in content:
    content = content.replace("import com.siraj.app.features.settings.presentation.ProfileScreen", "import com.siraj.app.features.settings.presentation.ProfileScreen\n" + import_statement)

screen_route = """            composable(Screen.WorkspaceSettings.route) {
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else {
                    WorkspaceSettingsScreen(onNavigateBack = { navController.popBackStack() })
                }
            }
"""

if 'Screen.WorkspaceSettings.route' not in content:
    content = content.replace("composable(Screen.Settings.route) {", screen_route + "            composable(Screen.Settings.route) {")

content = content.replace("onNavigateToWorkspaceSettings = { /* TODO */ }", "onNavigateToWorkspaceSettings = { navController.navigate(Screen.WorkspaceSettings.route) }")
# Since the default signature update didn't put /* TODO */, I'll just use regex

import re
content = re.sub(r'ProfileScreen\(\s*onLogoutSuccess =', r'ProfileScreen(\n                        onNavigateToWorkspaceSettings = { navController.navigate(Screen.WorkspaceSettings.route) },\n                        onLogoutSuccess =', content)

with open('app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt', 'w') as f:
    f.write(content)

