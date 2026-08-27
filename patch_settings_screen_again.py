import re

with open('app/src/main/java/com/siraj/app/features/settings/presentation/SettingsScreen.kt', 'r') as f:
    content = f.read()

target = 'SettingsItemData("الحساب", Icons.Default.Person, SettingsPage.ACCOUNT),'
replacement = 'SettingsItemData("الحساب", Icons.Default.Person, SettingsPage.ACCOUNT),\n        SettingsItemData("مساحة العمل", Icons.Default.Build, SettingsPage.WORKSPACE),'

content = content.replace(target, replacement)

# Add SettingsPage.WORKSPACE
content = content.replace('ACCOUNT("الحساب"),', 'ACCOUNT("الحساب"),\n    WORKSPACE("مساحة العمل"),')

# In SettingsScreen when
when_target = 'SettingsPage.ACCOUNT -> AccountSettings(uiState, viewModel, onLogout)'
when_replacement = 'SettingsPage.ACCOUNT -> AccountSettings(uiState, viewModel, onLogout)\n                SettingsPage.WORKSPACE -> onNavigateToWorkspaceSettings()'
content = content.replace(when_target, when_replacement)

with open('app/src/main/java/com/siraj/app/features/settings/presentation/SettingsScreen.kt', 'w') as f:
    f.write(content)
