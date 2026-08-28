import sys

# Patch libs.versions.toml
with open("gradle/libs.versions.toml", "r") as f:
    toml = f.read()

new_toml_line = 'media3-ui = { group = "androidx.media3", name = "media3-ui", version.ref = "media3" }\nmedia3-session = { group = "androidx.media3", name = "media3-session", version.ref = "media3" }'
toml = toml.replace('media3-ui = { group = "androidx.media3", name = "media3-ui", version.ref = "media3" }', new_toml_line)

with open("gradle/libs.versions.toml", "w") as f:
    f.write(toml)

# Patch build.gradle.kts
with open("app/build.gradle.kts", "r") as f:
    gradle = f.read()

new_gradle_dep = '  implementation(libs.media3.ui)\n  implementation(libs.media3.session)'
gradle = gradle.replace('  implementation(libs.media3.ui)', new_gradle_dep)

with open("app/build.gradle.kts", "w") as f:
    f.write(gradle)
