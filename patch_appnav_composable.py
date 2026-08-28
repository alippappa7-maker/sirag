import sys

def main():
    try:
        with open("app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt", "r") as f:
            content = f.read()

        analytics_composable = """
            composable(Screen.CreatorAnalytics.route) {
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else {
                    val authRepository = remember { com.siraj.app.data.repository.FirebaseAuthRepositoryImpl() }
                    val analyticsRepository = remember { com.siraj.app.data.repository.analytics.FirebaseCreatorAnalyticsRepositoryImpl() }
                    val viewModel = remember { com.siraj.app.features.studio.presentation.analytics.CreatorAnalyticsViewModel(analyticsRepository, authRepository) }
                    com.siraj.app.features.studio.presentation.analytics.CreatorAnalyticsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
"""
        
        if "composable(Screen.CreatorAnalytics.route)" not in content:
            content = content.replace("composable(Screen.Studio.route) {", analytics_composable + "\n            composable(Screen.Studio.route) {")

        with open("app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt", "w") as f:
            f.write(content)
        print("Patched AppNavigation composable")
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    main()
