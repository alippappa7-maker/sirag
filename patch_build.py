import re

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

# Add Vico dependencies
vico_deps = """
    // Vico Charts
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)
    implementation(libs.vico.core)
"""

content = re.sub(
    r"dependencies \{",
    f"dependencies {{{vico_deps}",
    content,
    count=1
)

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
