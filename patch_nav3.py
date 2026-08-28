import re

with open('app/src/main/java/com/siraj/app/core/navigation/Screen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Add UsageAndBilling to Screen object properly
if "object UsageAndBilling" not in content:
    content = content.replace(
        "object SubscriptionPlans : Screen(\"subscription_plans\")",
        "object SubscriptionPlans : Screen(\"subscription_plans\")\n    object UsageAndBilling : Screen(\"usage_and_billing\")"
    )

with open('app/src/main/java/com/siraj/app/core/navigation/Screen.kt', 'w', encoding='utf-8') as f:
    f.write(content)


with open('app/src/main/java/com/siraj/app/features/settings/presentation/SettingsScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Fix SettingsScreen when statement
if "SettingsPage.BILLING -> onNavigateToBilling()" not in content:
    content = content.replace(
        "SettingsPage.ABOUT -> AboutSettings()",
        "SettingsPage.ABOUT -> AboutSettings()\n                SettingsPage.BILLING -> onNavigateToBilling()"
    )

with open('app/src/main/java/com/siraj/app/features/settings/presentation/SettingsScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)

