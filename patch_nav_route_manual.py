import re

with open('app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt', 'r') as f:
    content = f.read()

# Replace the ProfileScreen block inside Screen.Settings.route
target = """                    ProfileScreen(
                        onNavigateToWorkspaceSettings = { navController.navigate(Screen.WorkspaceSettings.route) },
                        onLogoutSuccess = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToAdmin = { navController.navigate(Screen.Admin.route) }
                    )"""

replacement = """                    com.siraj.app.features.settings.presentation.SettingsScreen(
                        onNavigateToWorkspaceSettings = { navController.navigate(Screen.WorkspaceSettings.route) },
                        onNavigateBack = { navController.popBackStack() },
                        onLogout = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt', 'w') as f:
    f.write(content)
