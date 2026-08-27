import re

with open('app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt', 'r') as f:
    content = f.read()

# I will replace ProfileScreen with SettingsScreen inside Screen.Settings.route
target_route = """            composable(Screen.Settings.route) {
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else {
                    ProfileScreen(
                        onNavigateToWorkspaceSettings = { navController.navigate(Screen.WorkspaceSettings.route) },
                        onLogoutSuccess = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToAdmin = { navController.navigate(Screen.Admin.route) }
                    )
                }
            }"""

replacement_route = """            composable(Screen.Settings.route) {
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else {
                    com.siraj.app.features.settings.presentation.SettingsScreen(
                        onNavigateToWorkspaceSettings = { navController.navigate(Screen.WorkspaceSettings.route) },
                        onNavigateBack = { navController.popBackStack() },
                        onLogout = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }"""

content = content.replace(target_route, replacement_route)

# Fallback regex if the exact match fails due to whitespace
if 'com.siraj.app.features.settings.presentation.SettingsScreen' not in content:
    content = re.sub(r'composable\(Screen\.Settings\.route\)\s*\{[^}]*ProfileScreen\([^\)]*\)[^}]*\}', replacement_route, content)

with open('app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt', 'w') as f:
    f.write(content)
