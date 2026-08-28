import sys

def main():
    try:
        with open("app/src/main/java/com/siraj/app/features/studio/presentation/StudioScreen.kt", "r") as f:
            content = f.read()

        old_sig = """fun StudioScreen(
    onNavigateToProject: (String) -> Unit = {},
    onNavigateToIdeation: () -> Unit = {},
    onNavigateToFlashPublishing: () -> Unit = {},
    viewModel: StudioViewModel = viewModel(factory = StudioViewModelFactory())
) {"""
        new_sig = """import androidx.compose.material.icons.filled.Assessment
fun StudioScreen(
    onNavigateToProject: (String) -> Unit = {},
    onNavigateToIdeation: () -> Unit = {},
    onNavigateToFlashPublishing: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    viewModel: StudioViewModel = viewModel(factory = StudioViewModelFactory())
) {"""
        content = content.replace(old_sig, new_sig)

        old_fab = """ExtendedFloatingActionButton(
                    onClick = onNavigateToFlashPublishing,
                    icon = { Icon(Icons.Default.Add, contentDescription = "نشر ومضة") },
                    text = { Text("نشر ومضة") }
                )"""
        new_fab = """ExtendedFloatingActionButton(
                    onClick = onNavigateToAnalytics,
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "التحليلات") },
                    text = { Text("التحليلات") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                ExtendedFloatingActionButton(
                    onClick = onNavigateToFlashPublishing,
                    icon = { Icon(Icons.Default.Add, contentDescription = "نشر ومضة") },
                    text = { Text("نشر ومضة") }
                )"""
        content = content.replace(old_fab, new_fab)

        with open("app/src/main/java/com/siraj/app/features/studio/presentation/StudioScreen.kt", "w") as f:
            f.write(content)
        print("Patched StudioScreen.kt")
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    main()
