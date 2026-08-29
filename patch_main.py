import re

with open('app/src/main/java/com/siraj/app/MainActivity.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Add LayoutDirection import if needed
if "import androidx.compose.ui.unit.LayoutDirection" not in content:
    content = content.replace("import androidx.compose.ui.platform.LocalLayoutDirection", "import androidx.compose.ui.platform.LocalLayoutDirection\nimport androidx.compose.ui.unit.LayoutDirection")

# Add prefs.language to the remembered values
if "var language by remember { mutableStateOf(\"ar\") }" not in content:
    content = content.replace("var fontScaleMultiplier by remember { mutableStateOf(1.0f) }", "var fontScaleMultiplier by remember { mutableStateOf(1.0f) }\n            var language by remember { mutableStateOf(\"ar\") }")
    
content = content.replace("accessibilityConfig = com.siraj.app.core.accessibility.AccessibilityConfig.fromPreferences(prefs)", "accessibilityConfig = com.siraj.app.core.accessibility.AccessibilityConfig.fromPreferences(prefs)\n                    language = prefs.language")

old_composition = "CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl)"
new_composition = "val currentLayoutDirection = if (language == \"ar\") LayoutDirection.Rtl else LayoutDirection.Ltr\n                    CompositionLocalProvider(LocalLayoutDirection provides currentLayoutDirection)"
content = content.replace(old_composition, new_composition)

with open('app/src/main/java/com/siraj/app/MainActivity.kt', 'w', encoding='utf-8') as f:
    f.write(content)
