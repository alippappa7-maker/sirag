import re

with open('app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt', 'r') as f:
    content = f.read()

pattern = re.compile(r'composable\(Screen\.Admin\.route\) \{.*?\n\s*\}', re.DOTALL)
replacement = """composable(Screen.Admin.route) {
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else {
                    val user = (authState as? Resource.Success)?.data
                    if (user?.role == UserRole.ADMIN || user?.role == UserRole.OWNER) {
                        AdminScreen()
                    } else {
                        ErrorScreen(
                            message = "ليس لديك صلاحية للوصول إلى لوحة الإدارة.",
                            onRetry = { navController.popBackStack() }
                        )
                    }
                }
            }"""

new_content = re.sub(pattern, replacement, content)

with open('app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt', 'w') as f:
    f.write(new_content)

