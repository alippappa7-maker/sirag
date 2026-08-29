import re

with open("app/src/main/java/com/siraj/app/features/home/presentation/HomeScreen.kt", "r") as f:
    content = f.read()

content = content.replace("items(projectsRes.data) { project ->", "items(projectsRes.data, key = { it.id }) { project ->")

with open("app/src/main/java/com/siraj/app/features/home/presentation/HomeScreen.kt", "w") as f:
    f.write(content)
