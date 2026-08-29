import re

with open('app/src/main/java/com/siraj/app/features/settings/presentation/SettingsPages.kt', 'r', encoding='utf-8') as f:
    content = f.read()

import_appcompat = "import androidx.appcompat.app.AppCompatDelegate\nimport androidx.core.os.LocaleListCompat\n"

if import_appcompat not in content:
    content = content.replace("import androidx.compose.ui.Alignment", import_appcompat + "import androidx.compose.ui.Alignment")

language_settings_old = """@Composable
fun LanguageSettings(uiState: SettingsUiState, viewModel: SettingsViewModel) {
    val prefs = uiState.profile?.preferences ?: return
    Column(modifier = Modifier.padding(16.dp)) {
        Text("اللغة", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = prefs.language == "ar", onClick = { viewModel.updatePreferences { it.copy(language = "ar") } })
            Text("العربية")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(selected = prefs.language == "en", onClick = { viewModel.updatePreferences { it.copy(language = "en") } })
            Text("English (Coming soon)")
        }
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = prefs.city,
            onValueChange = { viewModel.updatePreferences { p -> p.copy(city = it) } },
            label = { Text("المدينة (لحساب المواقيت)") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}"""

language_settings_new = """@Composable
fun LanguageSettings(uiState: SettingsUiState, viewModel: SettingsViewModel) {
    val prefs = uiState.profile?.preferences ?: return
    Column(modifier = Modifier.padding(16.dp)) {
        Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.language), style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = prefs.language == "ar", onClick = { 
                viewModel.updatePreferences { it.copy(language = "ar") }
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ar"))
            })
            Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.language_arabic))
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(selected = prefs.language == "en", onClick = { 
                viewModel.updatePreferences { it.copy(language = "en") }
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
            })
            Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.language_english))
        }
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = prefs.city,
            onValueChange = { viewModel.updatePreferences { p -> p.copy(city = it) } },
            label = { Text("المدينة (لحساب المواقيت)") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}"""

content = content.replace(language_settings_old, language_settings_new)

with open('app/src/main/java/com/siraj/app/features/settings/presentation/SettingsPages.kt', 'w', encoding='utf-8') as f:
    f.write(content)
