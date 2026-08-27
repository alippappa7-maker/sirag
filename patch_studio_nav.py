with open('app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt', 'r') as f:
    content = f.read()

studio_old = """composable(Screen.Studio.route) {
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else { StudioScreen(onNavigateToSettings = { navController.navigate(Screen.Settings.route) }) }
            }"""

studio_new = """composable(Screen.Studio.route) {
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else { 
                    StudioScreen(
                        onNavigateToProject = { projectId ->
                            navController.navigate(Screen.ProjectEditor.createRoute(projectId))
                        }
                    ) 
                }
            }"""

content = content.replace(studio_old, studio_new)

with open('app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt', 'w') as f:
    f.write(content)
