import sys

with open("app/src/main/java/com/siraj/app/core/navigation/Screen.kt", "r") as f:
    content = f.read()

if "object Quran" not in content:
    content = content.replace("object Mihrab : Screen(\"mihrab\")", "object Mihrab : Screen(\"mihrab\")\n    object Quran : Screen(\"quran\")\n    object Surah : Screen(\"surah/{surahId}\") { fun createRoute(id: Int) = \"surah/$id\" }")

with open("app/src/main/java/com/siraj/app/core/navigation/Screen.kt", "w") as f:
    f.write(content)

