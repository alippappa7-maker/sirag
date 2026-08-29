import re

with open('app/build.gradle.kts', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace("buildConfig = true\\n        resValues = true", "buildConfig = true\n        resValues = true")

with open('app/build.gradle.kts', 'w', encoding='utf-8') as f:
    f.write(content)
