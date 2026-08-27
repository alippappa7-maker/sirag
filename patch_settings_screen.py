import re

with open('app/src/main/java/com/siraj/app/features/settings/presentation/SettingsScreen.kt', 'r') as f:
    content = f.read()

nav_target = """                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.List,
                    title = "الجودة والأداء",
                    onClick = { /* TODO */ }
                )"""

nav_replacement = """                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.List,
                    title = "الجودة والأداء",
                    onClick = { /* TODO */ }
                )
                
                SettingsItem(
                    icon = Icons.Default.Person,
                    title = "إعدادات مساحة العمل",
                    onClick = { onNavigateToWorkspaceSettings() }
                )"""

content = content.replace(nav_target, nav_replacement)

# Also need to add onNavigateToWorkspaceSettings to the function signature
if "onNavigateToWorkspaceSettings: () -> Unit = {}" not in content:
    content = content.replace("fun SettingsScreen(", "fun SettingsScreen(\n    onNavigateToWorkspaceSettings: () -> Unit = {},")

with open('app/src/main/java/com/siraj/app/features/settings/presentation/SettingsScreen.kt', 'w') as f:
    f.write(content)
