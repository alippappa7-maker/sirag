import re

with open('app/src/main/java/com/siraj/app/features/subscription/presentation/SubscriptionScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Replace any occurrence of the old or new icon imports with the correct AutoMirrored one (since the compiler complains about ArrowBack missing entirely now, maybe the import is completely gone or malformed).
# Let's ensure the imports and usages are perfectly matched to AutoMirrored, which is standard in M3.

content = re.sub(r'import androidx\.compose\.material\.icons\.filled\.ArrowBack', 'import androidx.compose.material.icons.automirrored.filled.ArrowBack', content)
content = re.sub(r'Icons\.Default\.ArrowBack', 'Icons.AutoMirrored.Filled.ArrowBack', content)
content = re.sub(r'Icons\.Filled\.ArrowBack', 'Icons.AutoMirrored.Filled.ArrowBack', content)

with open('app/src/main/java/com/siraj/app/features/subscription/presentation/SubscriptionScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)
