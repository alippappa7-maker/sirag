import sys

def main():
    try:
        with open("app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt", "r") as f:
            content = f.read()

        import_statement = "import com.siraj.app.features.studio.presentation.analytics.CreatorAnalyticsScreen\nimport com.siraj.app.features.studio.presentation.analytics.CreatorAnalyticsViewModel\nimport com.siraj.app.data.repository.analytics.FirebaseCreatorAnalyticsRepositoryImpl\n"
        if import_statement not in content:
            content = content.replace("import com.siraj.app.features.studio.presentation.StudioViewModel", import_statement + "import com.siraj.app.features.studio.presentation.StudioViewModel")

        old_studio = """StudioScreen(
                        onNavigateToProject = { projectId ->
                            navController.navigate(Screen.ProjectEditor.createRoute(projectId))
                        },
                        onNavigateToIdeation = {
                            navController.navigate(Screen.Ideation.route)
                        },
                        onNavigateToFlashPublishing = {
                            navController.navigate(Screen.FlashPublishing.route)
                        }
                    )"""

        new_studio = """StudioScreen(
                        onNavigateToProject = { projectId ->
                            navController.navigate(Screen.ProjectEditor.createRoute(projectId))
                        },
                        onNavigateToIdeation = {
                            navController.navigate(Screen.Ideation.route)
                        },
                        onNavigateToFlashPublishing = {
                            navController.navigate(Screen.FlashPublishing.route)
                        },
                        onNavigateToAnalytics = {
                            navController.navigate(Screen.CreatorAnalytics.route)
                        }
                    )"""
        
        content = content.replace(old_studio, new_studio)
        
        analytics_composable = """
            composable(Screen.CreatorAnalytics.route) {
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else {
                    val authRepository = remember { com.siraj.app.data.repository.FirebaseAuthRepositoryImpl() }
                    val analyticsRepository = remember { FirebaseCreatorAnalyticsRepositoryImpl() }
                    val viewModel = remember { CreatorAnalyticsViewModel(analyticsRepository, authRepository) }
                    CreatorAnalyticsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
"""
        
        if "Screen.CreatorAnalytics.route" not in content:
            content = content.replace("composable(Screen.Studio.route) {", analytics_composable + "\n            composable(Screen.Studio.route) {")

        with open("app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt", "w") as f:
            f.write(content)
        print("Patched AppNavigation.kt")
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    main()
