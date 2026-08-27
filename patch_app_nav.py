with open('app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt', 'r') as f:
    content = f.read()

# Replace HomeScreen route
home_old = """composable(Screen.Home.route) {
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else { HomeScreen(toggleTheme = toggleTheme) }
            }"""

home_new = """composable(Screen.Home.route) {
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else { 
                    HomeScreen(
                        toggleTheme = toggleTheme,
                        onNavigateToProject = { projectId ->
                            navController.navigate(Screen.ProjectEditor.createRoute(projectId))
                        }
                    ) 
                }
            }"""

content = content.replace(home_old, home_new)

# Add ProjectEditor route after Admin
admin_route_end_marker = "            }\n\n            composable("

project_editor_route = """
            composable(
                route = Screen.ProjectEditor.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                if (!isLoggedIn) { 
                    LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } 
                } else {
                    val id = backStackEntry.arguments?.getString("id") ?: ""
                    com.siraj.app.features.project.presentation.ProjectEditorScreen(
                        projectId = id,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
"""

content = content.replace("            composable(\n                route = Screen.Details.route,", project_editor_route + "\n            composable(\n                route = Screen.Details.route,")

with open('app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt', 'w') as f:
    f.write(content)

