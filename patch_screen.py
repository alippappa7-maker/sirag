with open('app/src/main/java/com/siraj/app/core/navigation/Screen.kt', 'r') as f:
    content = f.read()

new_route = """
    object ProjectEditor : Screen("project/{id}") {
        fun createRoute(id: String) = "project/$id"
    }
}
"""

content = content.replace("}", new_route)

with open('app/src/main/java/com/siraj/app/core/navigation/Screen.kt', 'w') as f:
    f.write(content)
