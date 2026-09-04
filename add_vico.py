import re

with open("gradle/libs.versions.toml", "r") as f:
    content = f.read()

# Add to [versions]
content = re.sub(
    r"\[versions\]",
    "[versions]\nvico = \"1.15.0\"",
    content
)

# Add to [libraries]
content = re.sub(
    r"\[libraries\]",
    "[libraries]\nvico-compose = { group = \"com.patrykandpatrick.vico\", name = \"compose\", version.ref = \"vico\" }\nvico-compose-m3 = { group = \"com.patrykandpatrick.vico\", name = \"compose-m3\", version.ref = \"vico\" }\nvico-core = { group = \"com.patrykandpatrick.vico\", name = \"core\", version.ref = \"vico\" }",
    content
)

with open("gradle/libs.versions.toml", "w") as f:
    f.write(content)
