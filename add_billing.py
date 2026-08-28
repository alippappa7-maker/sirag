import re
with open('app/build.gradle.kts', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace(
    'implementation(libs.androidx.core.ktx)',
    'implementation(libs.androidx.core.ktx)\n  implementation(libs.billing.ktx)'
)

with open('app/build.gradle.kts', 'w', encoding='utf-8') as f:
    f.write(content)
