import sys

with open("app/src/main/java/com/siraj/app/core/navigation/NavItem.kt", "r") as f:
    content = f.read()

content = content.replace("Screen.Flashes.route", "\"flashes\"").replace("Screen.Mihrab.route", "\"mihrab\"")

with open("app/src/main/java/com/siraj/app/core/navigation/NavItem.kt", "w") as f:
    f.write(content)
