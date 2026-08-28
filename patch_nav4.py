import sys

with open("app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

# Let's completely nuke java.net references
content = content.replace("java.net.", "")
content = content.replace("URLDecoder", "java.net.URLDecoder")

with open("app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt", "w") as f:
    f.write(content)
