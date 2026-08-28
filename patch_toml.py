import re
with open('gradle/libs.versions.toml', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace(
    '[versions]',
    '[versions]\nbilling = "6.1.0"'
)

content = content.replace(
    '[libraries]',
    '[libraries]\nbilling-ktx = { group = "com.android.billingclient", name = "billing-ktx", version.ref = "billing" }'
)

with open('gradle/libs.versions.toml', 'w', encoding='utf-8') as f:
    f.write(content)
