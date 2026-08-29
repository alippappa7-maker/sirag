import re

with open('app/src/main/java/com/siraj/app/MainActivity.kt', 'r', encoding='utf-8') as f:
    content = f.read()

import_appcompat = "import androidx.appcompat.app.AppCompatDelegate\nimport androidx.core.os.LocaleListCompat\n"
if "AppCompatDelegate" not in content:
    content = content.replace("import androidx.appcompat.app.AppCompatActivity", "import androidx.appcompat.app.AppCompatActivity\n" + import_appcompat)

old_line = 'language = prefs.language'
new_line = 'language = prefs.language\n                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(prefs.language))'
content = content.replace(old_line, new_line)

with open('app/src/main/java/com/siraj/app/MainActivity.kt', 'w', encoding='utf-8') as f:
    f.write(content)
