import re

with open('app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Add import
if "import com.siraj.app.features.subscription.presentation.billing.UsageAndBillingScreen" not in content:
    content = content.replace(
        "import com.siraj.app.features.subscription.presentation.SubscriptionScreen",
        "import com.siraj.app.features.subscription.presentation.SubscriptionScreen\nimport com.siraj.app.features.subscription.presentation.billing.UsageAndBillingScreen"
    )

# Add callback to Settings
if "onNavigateToBilling = { navController.navigate(Screen.UsageAndBilling.route) }" not in content:
    content = content.replace(
        "onNavigateToActivityHistory = { navController.navigate(Screen.ActivityHistory.route) },",
        "onNavigateToActivityHistory = { navController.navigate(Screen.ActivityHistory.route) },\n                        onNavigateToBilling = { navController.navigate(Screen.UsageAndBilling.route) },"
    )

# Add route handling
billing_route = """
            composable(Screen.UsageAndBilling.route) {
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                UsageAndBillingScreen(
                    viewModel = subscriptionViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPlans = { navController.navigate(Screen.SubscriptionPlans.route) }
                )
            }
"""
if "composable(Screen.UsageAndBilling.route)" not in content:
    content = content.replace(
        "composable(Screen.SubscriptionPlans.route)",
        billing_route.strip() + "\n            composable(Screen.SubscriptionPlans.route)"
    )

with open('app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt', 'w', encoding='utf-8') as f:
    f.write(content)
