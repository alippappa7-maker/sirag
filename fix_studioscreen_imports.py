import sys

def main():
    try:
        with open("app/src/main/java/com/siraj/app/features/studio/presentation/StudioScreen.kt", "r") as f:
            content = f.read()

        content = content.replace("import androidx.compose.material.icons.filled.Assessment\npackage com.siraj.app.features.studio.presentation", "package com.siraj.app.features.studio.presentation\nimport androidx.compose.material.icons.filled.Assessment")
        content = content.replace("@Composable\n@Composable", "@Composable")

        with open("app/src/main/java/com/siraj/app/features/studio/presentation/StudioScreen.kt", "w") as f:
            f.write(content)
        print("Fixed StudioScreen.kt")
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    main()
