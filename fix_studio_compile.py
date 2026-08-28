import sys

def main():
    try:
        with open("app/src/main/java/com/siraj/app/features/studio/presentation/StudioScreen.kt", "r") as f:
            content = f.read()

        content = content.replace("import androidx.compose.material.icons.filled.Assessment\nfun StudioScreen(", "@Composable\nfun StudioScreen(")
        content = "import androidx.compose.material.icons.filled.Assessment\n" + content

        with open("app/src/main/java/com/siraj/app/features/studio/presentation/StudioScreen.kt", "w") as f:
            f.write(content)
        print("Fixed StudioScreen.kt")
        
        with open("app/src/main/java/com/siraj/app/features/studio/presentation/analytics/CreatorAnalyticsViewModel.kt", "r") as f:
            vm_content = f.read()
            
        if "import kotlinx.coroutines.flow.firstOrNull" not in vm_content:
            vm_content = vm_content.replace("import kotlinx.coroutines.flow.MutableStateFlow", "import kotlinx.coroutines.flow.firstOrNull\nimport kotlinx.coroutines.flow.MutableStateFlow")
            
        vm_content = vm_content.replace("authRepository.getCurrentUserSync()", "authRepository.currentUser.firstOrNull()")
        vm_content = vm_content.replace("user.uid", "user.id")
        
        with open("app/src/main/java/com/siraj/app/features/studio/presentation/analytics/CreatorAnalyticsViewModel.kt", "w") as f:
            f.write(vm_content)
        print("Fixed CreatorAnalyticsViewModel.kt")
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    main()
