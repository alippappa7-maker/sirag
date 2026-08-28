import sys

with open("app/src/main/java/com/siraj/app/core/navigation/Screen.kt", "r") as f:
    content = f.read()

if "object Flashes" not in content:
    content = content.replace("object Audio : Screen(\"audio\")", "object Flashes : Screen(\"flashes\")\n    object Mihrab : Screen(\"mihrab\")\n    object Audio : Screen(\"audio\")")

with open("app/src/main/java/com/siraj/app/core/navigation/Screen.kt", "w") as f:
    f.write(content)

