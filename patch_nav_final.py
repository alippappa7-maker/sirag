import sys

with open("app/src/main/java/com/siraj/app/core/navigation/MainShellScreen.kt", "r") as f:
    content = f.read()

content = content.replace("Screen.Audio.route", "\"audio\"").replace("Screen.Quran.route", "\"quran\"")

with open("app/src/main/java/com/siraj/app/core/navigation/MainShellScreen.kt", "w") as f:
    f.write(content)

