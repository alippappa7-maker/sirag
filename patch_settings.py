import re

with open('app/src/main/java/com/siraj/app/features/settings/presentation/SettingsScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Add to enum
content = content.replace(
    "WORKSPACE(\"مساحة العمل\"),",
    "WORKSPACE(\"مساحة العمل\"),\n    BILLING(\"الاستخدام والفوترة\"),"
)

# Add to MainSettingsList parameters
content = content.replace(
    "fun MainSettingsList(onPageSelect: (SettingsPage) -> Unit, onLogout: () -> Unit) {",
    "fun MainSettingsList(onPageSelect: (SettingsPage) -> Unit, onLogout: () -> Unit, onNavigateToBilling: () -> Unit) {"
)

# Add item to list
content = content.replace(
    "SettingsItemData(\"الحساب\", Icons.Default.Person, SettingsPage.ACCOUNT),",
    "SettingsItemData(\"الحساب\", Icons.Default.Person, SettingsPage.ACCOUNT),\n        SettingsItemData(\"الاستخدام والفوترة\", Icons.Default.CreditCard, SettingsPage.BILLING),"
)

# Handle item click
content = content.replace(
    "onClick = { onPageSelect(item.page) }",
    "onClick = {\n                    if (item.page == SettingsPage.BILLING) {\n                        onNavigateToBilling()\n                    } else {\n                        onPageSelect(item.page)\n                    }\n                }"
)

# Add to SettingsScreen parameters
content = content.replace(
    "onNavigateToActivityHistory: () -> Unit = {},",
    "onNavigateToActivityHistory: () -> Unit = {},\n    onNavigateToBilling: () -> Unit = {},"
)

# Call with billing param
content = content.replace(
    "onLogout = { viewModel.logout(onLogout) }",
    "onLogout = { viewModel.logout(onLogout) },\n                    onNavigateToBilling = onNavigateToBilling"
)

with open('app/src/main/java/com/siraj/app/features/settings/presentation/SettingsScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)
