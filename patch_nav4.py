import re

with open('app/src/main/java/com/siraj/app/core/navigation/Screen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Add UsageAndBilling
if "object UsageAndBilling" not in content:
    content = content.replace(
        "object SubscriptionPlans : Screen(\"subscription_plans\")",
        "object SubscriptionPlans : Screen(\"subscription_plans\")\n    object UsageAndBilling : Screen(\"usage_and_billing\")"
    )
elif "object UsageAndBilling" in content:
    pass # Already added, wait for next build

with open('app/src/main/java/com/siraj/app/core/navigation/Screen.kt', 'w', encoding='utf-8') as f:
    f.write(content)
